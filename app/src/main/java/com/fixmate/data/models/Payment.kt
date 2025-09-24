package com.fixmate.data.models

data class Payment(
    val id: String = "",
    val bookingId: String,  // Remove default empty string
    val customerId: String, // Remove default empty string
    val providerId: String, // Remove default empty string
    val amount: Double,     // Remove default 0.0
    val currency: String = "LKR",
    val paymentMethod: PaymentMethodType,
    val paymentDetails: PaymentDetails? = null,
    val status: PaymentStatus = PaymentStatus.PENDING,
    val transactionId: String = "",
    val gatewayResponse: String = "",
    val fees: PaymentFees = PaymentFees(),
    val refunds: List<Refund> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val processedAt: Long? = null,
    val failureReason: String = ""
)

data class PaymentDetails(
    val cardDetails: CardDetails? = null,
    val bankDetails: BankDetails? = null,
    val digitalWalletDetails: DigitalWalletDetails? = null,
    val stripePaymentDetails: StripePaymentDetails? = null
)

data class CardDetails(
    val last4Digits: String = "",
    val cardType: String = "", // Visa, Mastercard, etc.
    val expiryMonth: Int = 0,
    val expiryYear: Int = 0
)

data class BankDetails(
    val bankName: String = "",
    val accountNumber: String = "", // Masked
    val routingNumber: String = ""
)

data class DigitalWalletDetails(
    val walletType: String = "", // PayPal, GooglePay, etc.
    val walletId: String = ""
)

data class PaymentFees(
    val platformFee: Double = 0.0,
    val paymentGatewayFee: Double = 0.0,
    val taxes: Double = 0.0
)

data class Refund(
    val id: String = "",
    val amount: Double = 0.0,
    val reason: String = "",
    val status: RefundStatus = RefundStatus.PENDING,
    val processedAt: Long? = null,
    val refundTransactionId: String = ""
)

data class Wallet(
    val userId: String = "",
    val balance: Double = 0.0,
    val currency: String = "LKR",
    val transactions: List<WalletTransaction> = emptyList(),
    val lastUpdated: Long = System.currentTimeMillis()
)

data class WalletTransaction(
    val id: String = "",
    val type: WalletTransactionType = WalletTransactionType.CREDIT,
    val amount: Double = 0.0,
    val description: String = "",
    val referenceId: String = "", // Booking ID or Payment ID
    val timestamp: Long = System.currentTimeMillis()
)

enum class PaymentMethodType {
    CASH, CREDIT_CARD, DEBIT_CARD, BANK_TRANSFER, DIGITAL_WALLET, SEVALKA_WALLET, STRIPE_CARD
}

enum class RefundStatus {
    PENDING, PROCESSED, FAILED, CANCELLED
}

enum class WalletTransactionType {
    CREDIT, DEBIT, REFUND, WITHDRAWAL
}

enum class PaymentStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED,
    REFUNDED,
    CANCELLED
}
