package com.fixmate.presentation.customer.payment

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fixmate.R
import com.fixmate.data.models.StripePaymentMethod
import com.fixmate.ui.theme.*
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import com.stripe.android.paymentsheet.rememberPaymentSheet
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StripePaymentScreen(
    bookingId: String,
    onBackClick: () -> Unit = {},
    onPaymentSuccess: (Double) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: StripePaymentViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    
    var selectedPaymentMethod by remember { mutableStateOf(StripePaymentMethod.CARD) }

    // Fetch booking details when screen loads
    LaunchedEffect(bookingId) {
        viewModel.fetchBookingDetails(bookingId)
    }

    // Stripe PaymentSheet setup
    val paymentSheet = rememberPaymentSheet { paymentSheetResult ->
        when (paymentSheetResult) {
            is PaymentSheetResult.Canceled -> {
                Timber.d("Payment canceled by user")
            }
            is PaymentSheetResult.Failed -> {
                Timber.e("Payment failed: ${paymentSheetResult.error}")
                viewModel.resetPaymentState()
            }
            is PaymentSheetResult.Completed -> {
                Timber.d("Payment completed successfully")
                viewModel.confirmStripePayment(bookingId)
            }
        }
    }

    // Initialize Stripe with publishable key when payment intent is created
    LaunchedEffect(state.publishableKey) {
        if (state.publishableKey.isNotEmpty()) {
            PaymentConfiguration.init(context, state.publishableKey)
        }
    }

    // Handle payment success
    LaunchedEffect(state.paymentSuccess) {
        if (state.paymentSuccess) {
            onPaymentSuccess(state.amount)
        }
    }

    // Handle errors
    LaunchedEffect(state.error) {
        state.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Payment",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = {
                        when (selectedPaymentMethod) {
                            StripePaymentMethod.CARD -> {
                                if (state.isPaymentIntentCreated && state.clientSecret.isNotEmpty()) {
                                    // Present PaymentSheet
                                    paymentSheet.presentWithPaymentIntent(
                                        paymentIntentClientSecret = state.clientSecret,
                                        configuration = PaymentSheet.Configuration(
                                            merchantDisplayName = "SevaLK",
                                            allowsDelayedPaymentMethods = true
                                        )
                                    )
                                } else {
                                    // Create payment intent first
                                    viewModel.createPaymentIntent(bookingId)
                                }
                            }
                            StripePaymentMethod.CASH -> {
                                viewModel.processCashPayment(bookingId)
                            }
                        }
                    },
                    enabled = !state.isLoading && !state.isLoadingBooking && state.booking != null,
                    colors = ButtonDefaults.buttonColors(containerColor = S_YELLOW),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (state.isLoading || state.isLoadingBooking) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.dollar_sign),
                                contentDescription = "Pay",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when {
                                    state.isLoadingBooking -> "Loading..."
                                    state.booking == null -> "Load Booking Details"
                                    selectedPaymentMethod == StripePaymentMethod.CARD -> {
                                        if (state.isPaymentIntentCreated) "Pay LKR ${String.format("%.2f", state.amount)}"
                                        else "Prepare Payment"
                                    }
                                    else -> "Confirm Cash Payment"
                                },
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "By proceeding, you agree to our Terms of Service and\nacknowledge our Privacy Policy",
                    style = MaterialTheme.typography.labelSmall.copy(color = S_LIGHT_TEXT),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(S_INPUT_BACKGROUND)
                    .padding(16.dp)
            ) {
                // Total Amount Section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(S_YELLOW, RoundedCornerShape(8.dp))
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Total Amount",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                        Text(
                            text = if (state.booking != null) {
                                "LKR ${String.format("%.2f", state.amount)}"
                            } else {
                                "Loading..."
                            },
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Choose Payment Method Section
                Text(
                    text = "Choose Payment Method",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Payment Method Options
                PaymentMethodOption(
                    iconRes = R.drawable.credit_card,
                    title = "Credit/Debit Card",
                    description = "Secure payment via Stripe",
                    feeInfo = "Protected by Stripe",
                    isSelected = selectedPaymentMethod == StripePaymentMethod.CARD,
                    onClick = { 
                        selectedPaymentMethod = StripePaymentMethod.CARD
                        viewModel.setSelectedPaymentMethod(StripePaymentMethod.CARD)
                    }
                )
                
                Spacer(modifier = Modifier.height(8.dp))

                PaymentMethodOption(
                    iconRes = R.drawable.dollar_sign,
                    title = "Cash Payment",
                    description = "Pay directly to service provider",
                    feeInfo = "No processing fees",
                    isSelected = selectedPaymentMethod == StripePaymentMethod.CASH,
                    onClick = { 
                        selectedPaymentMethod = StripePaymentMethod.CASH
                        viewModel.setSelectedPaymentMethod(StripePaymentMethod.CASH)
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Security Information
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.check_circle1),
                        contentDescription = "Secure",
                        tint = S_GREEN,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (selectedPaymentMethod == StripePaymentMethod.CARD) {
                            "Secure 256-bit SSL encryption powered by Stripe"
                        } else {
                            "Direct payment to service provider upon completion"
                        },
                        style = MaterialTheme.typography.bodySmall.copy(color = S_LIGHT_TEXT)
                    )
                }
            }
        }
    }
}

@Composable
private fun PaymentMethodOption(
    iconRes: Int,
    title: String,
    description: String,
    feeInfo: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) {
        when (title) {
            "Cash Payment" -> S_GREEN
            else -> S_BLUE
        }
    } else {
        null
    }

    val iconTint = if (title == "Cash Payment") S_GREEN else S_BLUE
    val selectedCheckmarkTint = if (title == "Cash Payment") S_GREEN else S_BLUE

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = borderColor?.let { BorderStroke(2.dp, it) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall.copy(color = S_LIGHT_TEXT)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Check",
                        tint = S_GREEN,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = feeInfo,
                        style = MaterialTheme.typography.bodySmall.copy(color = S_GREEN)
                    )
                }
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = selectedCheckmarkTint,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Preview
@Composable
fun StripePaymentScreenPreview() {
    SevaLKTheme {
        StripePaymentScreen(
            bookingId = "test_booking"
        )
    }
}
