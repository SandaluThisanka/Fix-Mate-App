package com.fixmate.data.models

import com.google.gson.annotations.SerializedName

// Request models for backend API
data class CreatePaymentIntentRequest(
    @SerializedName("bookingId")
    val bookingId: String,
    @SerializedName("amount")
    val amount: Double, // Amount in LKR (backend converts to paisa)
    @SerializedName("currency")
    val currency: String = "lkr",
    @SerializedName("customerId")
    val customerId: String,
    @SerializedName("providerId")
    val providerId: String,
    @SerializedName("paymentMethodTypes")
    val paymentMethodTypes: List<String> = listOf("card")
)

data class CreatePaymentIntentResponse(
    @SerializedName("clientSecret")
    val clientSecret: String,
    @SerializedName("paymentIntentId")
    val paymentIntentId: String,
    @SerializedName("publishableKey")
    val publishableKey: String
)

data class ConfirmPaymentRequest(
    @SerializedName("paymentIntentId")
    val paymentIntentId: String,
    @SerializedName("bookingId")
    val bookingId: String
)

data class ConfirmPaymentResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("paymentStatus")
    val paymentStatus: String,
    @SerializedName("bookingStatus")
    val bookingStatus: String,
    @SerializedName("message")
    val message: String
)

data class CashPaymentRequest(
    @SerializedName("bookingId")
    val bookingId: String,
    @SerializedName("amount")
    val amount: Double,
    @SerializedName("customerId")
    val customerId: String,
    @SerializedName("providerId")
    val providerId: String
)

data class CashPaymentResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("paymentId")
    val paymentId: String,
    @SerializedName("bookingStatus")
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
