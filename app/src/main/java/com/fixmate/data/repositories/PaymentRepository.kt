package com.fixmate.data.repositories

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
                amount = (amount * 100).toLong(), // Convert to cents
                customerId = customerId,
                providerId = providerId
            )
            System.out.println("Inside createPaymentIntent:")
            val response = paymentApiService.createPaymentIntent(request)
            
            if (response.isSuccessful && response.body() != null) {
                Timber.d("Payment intent created successfully")
                Result.success(response.body()!!)
            } else {
                val errorMsg = "Failed to create payment intent: ${response.message()}"
                Timber.e(errorMsg)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            System.out.println("SevaLK EXCEPTION: ${e.javaClass.simpleName} - ${e.message}")
            Timber.e(e, "Error creating payment intent")
            Result.failure(e)
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