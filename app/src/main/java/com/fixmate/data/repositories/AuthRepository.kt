package com.fixmate.data.repositories

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.fixmate.data.models.ProviderStatus
import com.fixmate.data.models.Service
import com.fixmate.data.models.ServiceLocation
import com.fixmate.data.models.ServiceProvider
import com.fixmate.data.models.User
import com.fixmate.data.models.UserType
import com.fixmate.utils.Constants
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.util.Locale
import java.util.Properties
import javax.inject.Inject
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.random.Random

interface AuthRepository {
    val currentUser: Flow<FirebaseUser?>
    
    suspend fun login(email: String, password: String): Result<FirebaseUser>
    suspend fun signInWithGoogle(idToken: String): Result<FirebaseUser>
    suspend fun sendVerificationCode(email: String, fullName: String): String
    suspend fun verifyCode(email: String, code: String): Boolean
    suspend fun registerUser(email: String, password: String, fullName: String, userType: UserType): Result<FirebaseUser>
    suspend fun createGoogleUser(email: String, fullName: String, userType: UserType): Result<FirebaseUser>
    suspend fun createGoogleServiceProvider(email: String, fullName: String, userType: UserType): Result<FirebaseUser>
    suspend fun getCurrentUserId(): String?
    suspend fun getCustomerName(): String?
    suspend fun getUserData(userId: String): Result<User>
    suspend fun updateUserData(user: User): Result<Unit>
    suspend fun logout()
    suspend fun registerServiceProvider(email: String, password: String, fullName: String, userType: UserType): Result<FirebaseUser>
    suspend fun updateServiceProviderServices(providerId: String, services: List<Service>): Result<Unit>
    suspend fun updateServiceProviderLocation(providerId: String, serviceLocation: ServiceLocation, serviceRadius: Double): Result<Unit>
    suspend fun getServiceProviderServices(providerId: String): Result<List<Service>>
    suspend fun checkServiceProviderExists(userId: String): Result<Boolean>
    suspend fun createServiceProviderFromCustomer(userId: String): Result<Unit>
    suspend fun updateFCMToken(userId: String, token: String): Result<Unit>
}

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {
    
    private val verificationCodes = mutableMapOf<String, String>()
    
    override val currentUser: Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override suspend fun login(email: String, password: String): Result<FirebaseUser> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val user = authResult.user ?: return Result.failure(Exception("Authentication failed"))
            
            Timber.d("User logged in successfully: ${user.uid}")
            Result.success(user)
        } catch (e: Exception) {
            Timber.e(e, "Login failed for email: $email")
            Result.failure(e)
        }
    }

    override suspend fun signInWithGoogle(idToken: String): Result<FirebaseUser> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            val user = authResult.user ?: return Result.failure(Exception("Google authentication failed"))
            
            Timber.d("User logged in with Google successfully: ${user.uid}")
            Result.success(user)
        } catch (e: Exception) {
            Timber.e(e, "Google sign-in failed")
            Result.failure(e)
        }
    }

    override suspend fun sendVerificationCode(email: String, fullName: String): String {
        val code = generateVerificationCode()
        verificationCodes[email] = code
        
        try {
            // Send verification email with OTP
            Timber.d("Sending verification code to $email: $code")
            sendVerificationEmail(email, fullName, code)
            return code
        } catch (e: Exception) {
            Timber.e(e, "Failed to send verification email")
            throw e
        }
    }
    
    override suspend fun verifyCode(email: String, code: String): Boolean {
        val storedCode = verificationCodes[email]
        return if (storedCode == code) {
            true
        } else {
            Timber.w("Code verification failed. Expected: $storedCode, Got: $code")
            false
        }
    }
    
    override suspend fun registerUser(
        email: String, 
        password: String, 
        fullName: String, 
        userType: UserType
    ): Result<FirebaseUser> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val userId = authResult.user?.uid ?: return Result.failure(Exception("Failed to get user ID"))
            
            // Create User object
            val user = User(
                id = userId,
                email = email,
                displayName = fullName,
                userType = userType,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            
            // Convert User object to Map using companion function
            val userData = User.toMap(user)
            
            // Save to Firestore
            firestore.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .set(userData)
                .await()
            
            Result.success(authResult.user!!)
        } catch (e: Exception) {
            Timber.e(e, "Registration failed")
            Result.failure(e)
        }
    }
    
    override suspend fun registerServiceProvider(
        email: String, 
        password: String, 
        fullName: String, 
        userType: UserType
    ): Result<FirebaseUser> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val userId = authResult.user?.uid ?: return Result.failure(Exception("Failed to get user ID"))
            
            // Create User object for customers collection
            val user = User(
                id = userId,
                email = email,
                displayName = fullName,
                userType = userType,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            
            // Create ServiceProvider object for service providers collection
            val serviceProvider = ServiceProvider(
                id = userId,
                userId = userId,
                businessName = fullName, // Initially set to full name, can be updated later
                description = "",
                status = ProviderStatus.PENDING,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            
            // Convert objects to Maps using companion functions
            val userData = User.toMap(user)
            val serviceProviderData = ServiceProvider.toMap(serviceProvider)
            
            // Save User to users collection
            firestore.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .set(userData)
                .await()
            
            // Save ServiceProvider to service_providers collection
            firestore.collection(Constants.COLLECTION_SERVICE_PROVIDERS)
                .document(userId)
                .set(serviceProviderData)
                .await()
            
            Result.success(authResult.user!!)
        } catch (e: Exception) {
            Timber.e(e, "Service provider registration failed")
            Result.failure(e)
        }
    }
    
    override suspend fun createGoogleUser(
        email: String, 
        fullName: String, 
        userType: UserType
    ): Result<FirebaseUser> {
        return try {
            val currentUser = auth.currentUser ?: return Result.failure(Exception("No authenticated user"))
            val userId = currentUser.uid
            
            // Create User object
            val user = User(
                id = userId,
                email = email,
                displayName = fullName,
                userType = userType,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            
            // Convert User object to Map using companion function
            val userData = User.toMap(user)
            
            // Save to Firestore
            firestore.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .set(userData)
                .await()
            
            Result.success(currentUser)
        } catch (e: Exception) {
            Timber.e(e, "Google user creation failed")
            Result.failure(e)
        }
    }
    
    override suspend fun createGoogleServiceProvider(
        email: String, 
        fullName: String, 
        userType: UserType
    ): Result<FirebaseUser> {
        return try {
            val currentUser = auth.currentUser ?: return Result.failure(Exception("No authenticated user"))
            val userId = currentUser.uid
            
            // Create User object for customers collection
            val user = User(
                id = userId,
                email = email,
                displayName = fullName,
                userType = userType,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            
            // Create ServiceProvider object for service providers collection
            val serviceProvider = ServiceProvider(
                id = userId,
                userId = userId,
                businessName = fullName, // Initially set to full name, can be updated later
                description = "",
                status = ProviderStatus.PENDING,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            
            // Convert objects to Maps using companion functions
            val userData = User.toMap(user)
            val serviceProviderData = ServiceProvider.toMap(serviceProvider)
            
            // Save User to users collection
            firestore.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .set(userData)
                .await()
            
            // Save ServiceProvider to service_providers collection
            firestore.collection(Constants.COLLECTION_SERVICE_PROVIDERS)
                .document(userId)
                .set(serviceProviderData)
                .await()
            
            Result.success(currentUser)
        } catch (e: Exception) {
            Timber.e(e, "Google service provider creation failed")
            Result.failure(e)
        }
    }
    
    override suspend fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    override suspend fun getCustomerName(): String? {
        val userId = getCurrentUserId()
        val document = userId?.let {
            firestore.collection(Constants.COLLECTION_USERS)
                .document(it)
                .get()
                .await()
        }
        return if (document != null && document.exists()) {
            val userData = document.data
            userData?.get("displayName") as? String
        } else {
            null
        }
    }

    override suspend fun getUserData(userId: String): Result<User> {
        return try {
            val document = firestore.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .get()
                .await()
            
            if (document.exists()) {
                val userData = document.data
                if (userData != null) {
                    // Convert Map to User object using companion function
                    val user = User.fromMap(userData)
                    if (user != null) {
                        Result.success(user)
                    } else {
                        Result.failure(Exception("Failed to parse user data"))
                    }
                } else {
                    Result.failure(Exception("User data is null"))
                }
            } else {
                Result.failure(Exception("User document not found"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to get user data")
            Result.failure(e)
        }
    }
    
    override suspend fun updateUserData(user: User): Result<Unit> {
        return try {
            val updatedUser = user.copy(updatedAt = System.currentTimeMillis())
            
            // Convert User object to Map using companion function
            val userData = User.toMap(updatedUser)
            
            firestore.collection(Constants.COLLECTION_USERS)
                .document(user.id)
                .set(userData)
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to update user data")
            Result.failure(e)
        }
    }
    
    override suspend fun logout() {
        auth.signOut()
    }
    
    override suspend fun updateServiceProviderServices(
        providerId: String, 
        services: List<Service>
    ): Result<Unit> {
        return try {
            val userRef = firestore.collection(Constants.COLLECTION_USERS)
                .document(providerId)

            val document = userRef.get().await()

            if (document.exists()) {
                val updates = mapOf(
                    "providerProfile.services" to services.map { Service.toMap(it) },
                    "providerProfile.updatedAt" to System.currentTimeMillis(),
                    "updatedAt" to System.currentTimeMillis()
                )

                userRef.update(updates).await()
                Timber.d("Provider services updated successfully for provider: $providerId")
                Result.success(Unit)
            } else {
                Result.failure(Exception("Provider profile not found"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to update service provider services")
            Result.failure(e)
        }
    }
    
    override suspend fun updateServiceProviderLocation(
        providerId: String, 
        serviceLocation: ServiceLocation,
        serviceRadius: Double
    ): Result<Unit> {
        return try {
            val userRef = firestore.collection(Constants.COLLECTION_USERS)
                .document(providerId)
            
            val document = userRef.get().await()

            val intRadius = serviceRadius.toInt()
            
            if (document.exists()) {
                val updates = mapOf(
                    "providerProfile.serviceLocation" to ServiceLocation.toMap(serviceLocation),
                    "providerProfile.serviceRadius" to intRadius,
                    "providerProfile.updatedAt" to System.currentTimeMillis(),
                    "updatedAt" to System.currentTimeMillis()
                )
                
                userRef.update(updates).await()
                Timber.d("Provider location updated successfully for provider: $providerId")
                Result.success(Unit)
            } else {
                Result.failure(Exception("Provider profile not found"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to update service provider location")
            Result.failure(e)
        }
    }
    
    override suspend fun getServiceProviderServices(providerId: String): Result<List<Service>> {
        return try {
            val document = firestore.collection(Constants.COLLECTION_USERS)
                .document(providerId)
                .get()
                .await()
            
            if (document.exists()) {
                val data = document.data
                val providerProfile = data?.get("providerProfile") as? Map<String, Any?>
                val servicesData = providerProfile?.get("services") as? List<Map<String, Any?>> ?: emptyList()
                
                val services = servicesData.mapNotNull { serviceData ->
                    Service.fromMap(serviceData)
                }
                
                Timber.d("Retrieved ${services.size} services for provider: $providerId")
                Result.success(services)
            } else {
                Timber.w("Provider profile not found: $providerId")
                Result.success(emptyList())
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to get service provider services")
            Result.failure(e)
        }
    }
    
    override suspend fun checkServiceProviderExists(userId: String): Result<Boolean> {
        return try {
            val document = firestore.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .get()
                .await()
            
            val exists = document.exists() &&
                isProviderUserType(document.getString("userType")) &&
                document.data?.containsKey("providerProfile") == true
            Timber.d("Provider profile exists for user $userId: $exists")
            Result.success(exists)
        } catch (e: Exception) {
            Timber.e(e, "Failed to check if service provider exists")
            Result.failure(e)
        }
    }
    
    override suspend fun createServiceProviderFromCustomer(userId: String): Result<Unit> {
        return try {
            // Get user details from users collection
            val userDocument = firestore.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .get()
                .await()
            
            if (!userDocument.exists()) {
                return Result.failure(Exception("User not found"))
            }
            
            val userData = userDocument.data!!
            val userName = userData["displayName"] as? String ?: "Unknown User"
            val userEmail = userData["email"] as? String ?: ""
            
            // Create ServiceProvider object for service providers collection
            val serviceProvider = ServiceProvider(
                id = userId,
                userId = userId,
                businessName = userName, // Initially set to user's display name
                description = "",
                status = ProviderStatus.PENDING,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            
            // Convert ServiceProvider object to Map using companion function
            val serviceProviderData = ServiceProvider.toMap(serviceProvider)

            val userRef = firestore.collection(Constants.COLLECTION_USERS)
                .document(userId)

            userRef
                .set(
                    mapOf(
                        "providerProfile" to serviceProviderData,
                        "userType" to Constants.USER_TYPE_PROVIDER,
                        "updatedAt" to System.currentTimeMillis()
                    ),
                    SetOptions.merge()
                )
                .await()
            
            Timber.d("Provider profile created for user: $userId")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to create service provider from customer")
            Result.failure(e)
        }
    }
    
    private fun isProviderUserType(userType: String?): Boolean {
        val normalized = userType?.lowercase(Locale.US)
        return normalized == Constants.USER_TYPE_PROVIDER ||
            normalized == UserType.SERVICE_PROVIDER.name.lowercase(Locale.US)
    }

    private fun generateVerificationCode(): String {
        return Random.nextInt(100000, 999999).toString()
    }
    
    private suspend fun sendVerificationEmail(email: String, name: String, code: String) {
        return suspendCoroutine { continuation ->
            Thread {
                try {
                    // Log the code for debugging purposes
                    Timber.d("Sending verification code to $email: $code")
                    
                    val props = Properties()
                    props.put("mail.smtp.auth", "true")
                    props.put("mail.smtp.starttls.enable", "true")
                    props.put("mail.smtp.host", "smtp.gmail.com")
                    props.put("mail.smtp.port", "587")
                    
                    // Email configuration
                    val emailSender = "abeysundarahettigesandalu@gmail.com"
                    val emailPassword = "lyyqoswpzjgzwvew"  // App password without spaces
                    
                    val session = Session.getInstance(props, object : Authenticator() {
                        override fun getPasswordAuthentication(): PasswordAuthentication {
                            return PasswordAuthentication(emailSender, emailPassword)
                        }
                    })
                    
                    val message = MimeMessage(session)
                    message.setFrom(InternetAddress(emailSender))
                    message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email))
                    message.subject = "FixMate - Email Verification"
                        
                        val emailContent = """
                            <html>
                            <body style="font-family: Arial, sans-serif; padding: 20px;">
                                <h2>Welcome to FixMate!</h2>
                                <p>Hello $name,</p>
                                <p>Thank you for registering with FixMate. Please use the verification code below to complete your registration:</p>
                                <h3 style="background-color: #f2f2f2; padding: 10px; text-align: center; font-size: 24px;">$code</h3>
                                <p>This code will expire in 10 minutes.</p>
                                <p>If you didn't request this code, please ignore this email.</p>
                                <p>Best regards,<br>The FixMate Team</p>
                            </body>
                            </html>
                        """.trimIndent()
                        
                        message.setContent(emailContent, "text/html; charset=utf-8")
                        
                        Transport.send(message)
                        continuation.resume(Unit)
                    } catch (e: Exception) {
                        Timber.e(e, "Failed to send email")
                        continuation.resumeWithException(e)
                    }
                }.start()
            }
        }

    override suspend fun updateFCMToken(userId: String, token: String): Result<Unit> {
        return try {
            // Update user document with new FCM token
            firestore.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .update(
                    mapOf(
                        "fcmToken" to token,
                        "updatedAt" to System.currentTimeMillis()
                    )
                )
                .await()

            Timber.d("FCM token updated for user: $userId")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to update FCM token for user: $userId")
            Result.failure(e)
        }
    }
}






