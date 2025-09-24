package com.fixmate.data.models

import com.google.gson.annotations.SerializedName

// Request models for backend API
data class CreatePaymentIntentRequest(
    @SerializedName("booking_id")
    val bookingId: String,
    @SerializedName("amount")
    val amount: Long, // Amount in cents
    @SerializedName("currency")
    val currency: String = "lkr",
    @SerializedName("customer_id")
    val customerId: String,
    @SerializedName("provider_id")
    val providerId: String,
    @SerializedName("payment_method_types")
    val paymentMethodTypes: List<String> = listOf("card")
)

data class CreatePaymentIntentResponse(
    @SerializedName("client_secret")
    val clientSecret: String,
    @SerializedName("payment_intent_id")
    val paymentIntentId: String,
    @SerializedName("publishable_key")
    val publishableKey: String
)

data class ConfirmPaymentRequest(
    @SerializedName("payment_intent_id")
    val paymentIntentId: String,
    @SerializedName("booking_id")
    val bookingId: String
)

data class ConfirmPaymentResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("payment_status")
    val paymentStatus: String,
    @SerializedName("booking_status")
    val bookingStatus: String,
    @SerializedName("message")
    val message: String
)

data class CashPaymentRequest(
    @SerializedName("booking_id")
    val bookingId: String,
    @SerializedName("amount")
    val amount: Double,
    @SerializedName("customer_id")
    val customerId: String,
    @SerializedName("provider_id")
    val providerId: String
)

data class CashPaymentResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("payment_id")
    val paymentId: String,
    @SerializedName("booking_status")
    val bookingStatus: String,
    @SerializedName("message")
    val message: String
)

// Enhanced payment models for Stripe integration
data class StripePaymentDetails(
    val paymentIntentId: String = "",
    val clientSecret: String = "",
    val paymentMethodId: String = "",
    val setupIntentId: String = ""
)

enum class StripePaymentMethod {
    CARD, CASH
}

data class PaymentIntentData(
    val id: String,
    val clientSecret: String,
    val amount: Long,
    val currency: String,
    val status: String
)
