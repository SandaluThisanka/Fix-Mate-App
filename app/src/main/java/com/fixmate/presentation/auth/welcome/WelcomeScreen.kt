package com.fixmate.presentation.auth.welcome

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fixmate.R
import com.fixmate.presentation.components.common.PrimaryButton
import com.fixmate.presentation.components.common.PrimaryButtonStyle
import com.fixmate.ui.theme.SevaLKTheme


@Composable
fun WelcomeScreen(
    onNavigateToLogin: () -> Unit = {},
    onNavigateToRegistration: () -> Unit = {}
) {
   Surface(
         modifier = Modifier.fillMaxSize(),
         color = Color.White
   ) {
       Column(
           modifier = Modifier.padding(horizontal = 25.dp)
       ) {
           Spacer(modifier = Modifier.height(50.dp))
           Image(
               painter = painterResource(id = R.drawable.garager),
               contentDescription = "Welcome Image",
               modifier = Modifier.size(430.dp)

           )
           Text(
               text = "FixMate",
               fontSize = 42.sp,
               fontWeight = FontWeight.Bold,
               color = Color.Black,
           )
           Text(
               text = "Your Local Services\nMarketplace.",
               fontSize = 24.sp,
               fontWeight = FontWeight.Normal,
               color = Color.Black,
               textAlign = TextAlign.Left,
               lineHeight = 32.sp
           )
           Spacer(modifier = Modifier.height(46.dp))
           PrimaryButton(text = "Sign UP", onClick = {
                onNavigateToRegistration()
           })
           Spacer(modifier = Modifier.height(22.dp))
           PrimaryButton(text = "Login", onClick = {
                onNavigateToLogin()
           }, style = PrimaryButtonStyle.OUTLINE)
       }
   }
}

@Preview
@Composable
fun WelcomeScreenPreview() {
    SevaLKTheme {
        WelcomeScreen()
    }
}