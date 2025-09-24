package com.fixmate.data.api

import com.fixmate.data.models.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface PaymentApiService {
    
    @POST("api/payments/create-intent")
    suspend fun createPaymentIntent(
        @Body request: CreatePaymentIntentRequest
    ): Response<CreatePaymentIntentResponse>
    
    @POST("api/payments/confirm")
    suspend fun confirmPayment(
        @Body request: ConfirmPaymentRequest
    ): Response<ConfirmPaymentResponse>
    
    @POST("api/payments/cash")
    suspend fun processCashPayment(
        @Body request: CashPaymentRequest
    ): Response<CashPaymentResponse>
}
