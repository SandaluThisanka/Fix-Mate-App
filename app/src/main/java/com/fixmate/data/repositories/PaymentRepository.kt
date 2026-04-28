package com.fixmate.data.repositories

import android.util.Log
import com.fixmate.BuildConfig
import com.google.firebase.firestore.FirebaseFirestore
import com.fixmate.data.api.PaymentApiService
import com.fixmate.data.models.*
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

interface PaymentRepository {
    suspend fun createPaymentIntent(
        bookingId: String,
        amount: Double,
        customerId: String,
        providerId: String
    ): Result<CreatePaymentIntentResponse>
    
    suspend fun confirmStripePayment(
        paymentIntentId: String,
        bookingId: String
    ): Result<ConfirmPaymentResponse>
    
    suspend fun processCashPayment(
        bookingId: String,
        amount: Double,
        customerId: String,
        providerId: String
    ): Result<CashPaymentResponse>
    
    suspend fun createPayment(payment: Payment): Result<Payment>
    suspend fun updatePaymentStatus(paymentId: String, status: PaymentStatus): Result<Unit>
    suspend fun getPaymentsByBookingId(bookingId: String): Result<List<Payment>>
}

@Singleton
class PaymentRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val paymentApiService: PaymentApiService
) : PaymentRepository {
    
    override suspend fun createPaymentIntent(
        bookingId: String,
        amount: Double,
        customerId: String,
        providerId: String
    ): Result<CreatePaymentIntentResponse> {
        return try {
            val request = CreatePaymentIntentRequest(
                bookingId = bookingId,
                amount = amount, // Send as Double, backend converts to paisa
                currency = "lkr",
                customerId = customerId,
                providerId = providerId,
                metadata = mapOf(
                    "bookingId" to bookingId,
                    "customerId" to customerId,
                    "providerId" to providerId
                )
            )
            
            Log.d("FIXMATE_PAYMENT", "========== BACKEND REQUEST ==========")
            Log.d("FIXMATE_PAYMENT", "Amount (LKR): $amount")
            Log.d("FIXMATE_PAYMENT", "Amount sent to backend: $amount")
            Log.d("FIXMATE_PAYMENT", "BookingId: $bookingId")
            Log.d("FIXMATE_PAYMENT", "Customer: $customerId")
            Log.d("FIXMATE_PAYMENT", "Provider: $providerId")
            
            Timber.d("========== PAYMENT INTENT REQUEST ==========")
            Timber.d("BookingId: $bookingId")
            Timber.d("Amount: LKR $amount (backend will convert to ${(amount * 100).toLong()} paisa)")
            Timber.d("Customer ID: $customerId")
            Timber.d("Provider ID: $providerId")
            Timber.d("Backend URL: ${BuildConfig.API_BASE_URL}")
            Timber.d("=============================================")
            
            val response = paymentApiService.createPaymentIntent(request)
            
            Log.d("FIXMATE_PAYMENT", "Backend Response Code: ${response.code()}")
            Log.d("FIXMATE_PAYMENT", "Response Success: ${response.isSuccessful}")
            
            Timber.d("Payment API Response Code: ${response.code()}")
            Timber.d("Response Successful: ${response.isSuccessful}")
            
            if (response.isSuccessful && response.body() != null) {
                val responseBody = response.body()!!
                Log.d("FIXMATE_PAYMENT", "✅ SUCCESS - Payment intent created")
                Timber.d("✅ Payment intent created successfully")
                Timber.d("Payment Intent ID: ${responseBody.paymentIntentId}")
                Timber.d("Client Secret: ${responseBody.clientSecret.take(20)}...")
                Result.success(responseBody)
            } else {
                val errorBody = response.errorBody()?.string() ?: "No error details"
                Log.e("FIXMATE_PAYMENT", "❌ BACKEND ERROR - Code: ${response.code()}")
                Log.e("FIXMATE_PAYMENT", "Error Message: ${response.message()}")
                Log.e("FIXMATE_PAYMENT", "Error Body: $errorBody")
                val errorMsg = when (response.code()) {
                    400 -> "Invalid payment request. Please check booking details and try again."
                    401 -> "Payment authentication failed. Please contact support."
                    404 -> "Payment service not found. The backend server may be unavailable."
                    500 -> "Payment server error. Please try again later or contact support."
                    503 -> "Payment service temporarily unavailable. The backend may be starting up (this can take 1-2 minutes on first request)."
                    else -> "Payment failed with code ${response.code()}: ${response.message()}"
                }
                Timber.e("❌ Payment Error - Code: ${response.code()}")
                Timber.e("Error Message: ${response.message()}")
                Timber.e("Error Body: $errorBody")
                Timber.e("User-friendly message: $errorMsg")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: java.net.UnknownHostException) {
            val errorMsg = "Cannot reach payment server. Please check:\n1. Internet connection is active\n2. Backend server is running at ${BuildConfig.API_BASE_URL}\n3. No firewall blocking the connection"
            Log.e("FIXMATE_PAYMENT", "❌ UnknownHostException: ${e.message}")
            Timber.e(e, "❌ UnknownHostException: $errorMsg")
            Result.failure(Exception(errorMsg))
        } catch (e: java.net.SocketTimeoutException) {
            val errorMsg = "Payment server timeout. The backend may be sleeping (free tier).\nPlease wait 1-2 minutes and try again as the server starts up."
            Log.e("FIXMATE_PAYMENT", "❌ SocketTimeoutException: ${e.message}")
            Timber.e(e, "❌ SocketTimeoutException: $errorMsg")
            Result.failure(Exception(errorMsg))
        } catch (e: java.net.ConnectException) {
            val errorMsg = "Cannot connect to payment server.\nThe backend at ${BuildConfig.API_BASE_URL} may be offline or not deployed."
            Log.e("FIXMATE_PAYMENT", "❌ ConnectException: ${e.message}")
            Timber.e(e, "❌ ConnectException: $errorMsg")
            Result.failure(Exception(errorMsg))
        } catch (e: Exception) {
            val errorMsg = "Unexpected payment error: ${e.message ?: e.javaClass.simpleName}\nPlease try again or contact support."
            Timber.e(e, "❌ Exception in createPaymentIntent: ${e.javaClass.name}")
            Timber.e("Stack trace: ${e.stackTraceToString()}")
            Result.failure(Exception(errorMsg))
        }
    }
    
    override suspend fun confirmStripePayment(
        paymentIntentId: String,
        bookingId: String
    ): Result<ConfirmPaymentResponse> {
        return try {
            val request = ConfirmPaymentRequest(
                paymentIntentId = paymentIntentId,
                bookingId = bookingId
            )
            
            val response = paymentApiService.confirmPayment(request)
            
            if (response.isSuccessful && response.body() != null) {
                Timber.d("Payment confirmed successfully")
                Result.success(response.body()!!)
            } else {
                val errorMsg = "Failed to confirm payment: ${response.message()}"
                Timber.e(errorMsg)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error confirming payment")
            Result.failure(e)
        }
    }
    
    override suspend fun processCashPayment(
        bookingId: String,
        amount: Double,
        customerId: String,
        providerId: String
    ): Result<CashPaymentResponse> {
        return try {
            val request = CashPaymentRequest(
                bookingId = bookingId,
                amount = amount,
                customerId = customerId,
                providerId = providerId
            )
            
            val response = paymentApiService.processCashPayment(request)
            
            if (response.isSuccessful && response.body() != null) {
                Timber.d("Cash payment processed successfully")
                Result.success(response.body()!!)
            } else {
                val errorMsg = "Failed to process cash payment: ${response.message()}"
                Timber.e(errorMsg)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error processing cash payment")
            Result.failure(e)
        }
    }
    
    override suspend fun createPayment(payment: Payment): Result<Payment> {
        return try {
            val paymentRef = firestore.collection("payments").document()
            val paymentWithId = payment.copy(id = paymentRef.id)

            paymentRef.set(paymentWithId).await()
            Result.success(paymentWithId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updatePaymentStatus(paymentId: String, status: PaymentStatus): Result<Unit> {
        return try {
            firestore.collection("payments")
                .document(paymentId)
                .update("status", status)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPaymentsByBookingId(bookingId: String): Result<List<Payment>> {
        return try {
            val snapshot = firestore.collection("payments")
                .whereEqualTo("bookingId", bookingId)
                .get()
                .await()

            val payments = snapshot.toObjects(Payment::class.java)
            Result.success(payments)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPayment(paymentId: String): Result<Payment> {
        return try {
            val snapshot = firestore.collection("payments")
                .document(paymentId)
                .get()
                .await()

            val payment = snapshot.toObject(Payment::class.java)
            if (payment != null) {
                Result.success(payment)
            } else {
                Result.failure(Exception("Payment not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}