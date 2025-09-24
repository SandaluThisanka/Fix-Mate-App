package com.fixmate.presentation.components.common

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fixmate.ui.theme.S_YELLOW

@Composable
fun PrimaryButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    backgroundColor: Color = S_YELLOW,
    foregroundColor: Color = Color.White,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    style: PrimaryButtonStyle = PrimaryButtonStyle.TEXT
){
    val buttonColors = if (style == PrimaryButtonStyle.OUTLINE) {
        ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = backgroundColor,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = backgroundColor
        )
    } else {
        ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = foregroundColor,
            disabledContainerColor = backgroundColor,
            disabledContentColor = foregroundColor
        )
    }

    val buttonModifier = if (style == PrimaryButtonStyle.OUTLINE) {
        Modifier
            .fillMaxWidth()
            .height(56.dp)
            .then(modifier)
            .border(
                width = 2.dp,
                color = backgroundColor,
                shape = RoundedCornerShape(16.dp)
            )
    } else {
        Modifier
            .fillMaxWidth()
            .height(56.dp)
            .then(modifier)
    }

    Button(
        onClick = onClick,
        modifier = buttonModifier,
        shape = RoundedCornerShape(16.dp),
        colors = buttonColors,
        enabled = enabled && !isLoading
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = if (style == PrimaryButtonStyle.OUTLINE) backgroundColor else foregroundColor,
                strokeWidth = 2.dp
            )
        } else {
            when (style) {
                PrimaryButtonStyle.TEXT -> {
                    Text(text = text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
                PrimaryButtonStyle.ICON_TEXT -> {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
                PrimaryButtonStyle.OUTLINE -> {
                    Text(text = text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PrimaryButtonPreview() {
    PrimaryButton(text = "Primary Button", onClick = {}, style = PrimaryButtonStyle.OUTLINE)
}

@Preview(showBackground = true)
@Composable
fun PrimaryButtonLoadingPreview() {
    PrimaryButton(
        text = "Loading Button",
        onClick = {},
        isLoading = true,
        style = PrimaryButtonStyle.TEXT
    )
}