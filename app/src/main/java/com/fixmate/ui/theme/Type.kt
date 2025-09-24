package com.fixmate.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.fixmate.R


val poppinsFamily = FontFamily(
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_semibold, FontWeight.SemiBold),
    Font(R.font.poppins_bold, FontWeight.Bold),
)


val AppTypography = Typography(
    displayLarge = TextStyle(fontFamily = poppinsFamily),
    displayMedium = TextStyle(fontFamily = poppinsFamily),
    displaySmall = TextStyle(fontFamily = poppinsFamily),
    headlineLarge = TextStyle(fontFamily = poppinsFamily),
    headlineMedium = TextStyle(fontFamily = poppinsFamily),
    headlineSmall = TextStyle(fontFamily = poppinsFamily),
    titleLarge = TextStyle(fontFamily = poppinsFamily),
    titleMedium = TextStyle(fontFamily = poppinsFamily),
    titleSmall = TextStyle(fontFamily = poppinsFamily),
    bodyLarge = TextStyle(fontFamily = poppinsFamily),
    bodyMedium = TextStyle(fontFamily = poppinsFamily),
    bodySmall = TextStyle(fontFamily = poppinsFamily),
    labelLarge = TextStyle(fontFamily = poppinsFamily),
    labelMedium = TextStyle(fontFamily = poppinsFamily),
    labelSmall = TextStyle(fontFamily = poppinsFamily),
)


