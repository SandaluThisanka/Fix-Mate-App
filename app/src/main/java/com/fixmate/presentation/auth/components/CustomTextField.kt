package com.fixmate.presentation.auth.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fixmate.ui.theme.S_LIGHT_BLACK
import com.fixmate.ui.theme.S_RED

@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String = "",
    leadingIcon: Painter? = null,
    trailingIcon: Painter? = null,
    onTrailingIconClick: (() -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    isPasswordField: Boolean = false,
    isPasswordVisible: Boolean = false,
    isError: Boolean = false,
    errorMessage: String? = null,
    maxLines: Int = 1,
    enabled: Boolean = true,
    backgroundColor: Color = Color(0xFFF5F5F5),
    focusedBackgroundColor: Color = Color(0xFFF5F5F5),
    textColor: Color = Color.Black,
    placeholderColor: Color = Color.Gray,
    iconTint: Color = Color.Gray,
    cornerRadius: Int = 12
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp)
    ) {
        // Label
        label?.let {
            Text(
                text = it,
                fontSize = 16.sp,
                color = S_LIGHT_BLACK,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // TextField
        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = placeholder,
                    color = placeholderColor
                )
            },
            leadingIcon = leadingIcon?.let {
                {
                    Icon(
                        painter = it,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier
                            .size(24.dp, 24.dp)
                    )
                }
            },
            trailingIcon = trailingIcon?.let {
                {
                    if (onTrailingIconClick != null) {
                        IconButton(onClick = onTrailingIconClick) {
                            Icon(
                                painter = it,
                                contentDescription = null,
                                tint = iconTint
                            )
                        }
                    } else {
                        Icon(
                            painter = it,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier
                                .size(24.dp, 24.dp)
                        )
                    }
                }
            },
            visualTransformation = if (isPasswordField && !isPasswordVisible) {
                PasswordVisualTransformation()
            } else {
                visualTransformation
            },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = maxLines == 1,
            maxLines = maxLines,
            enabled = enabled,
            isError = isError,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(cornerRadius.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = focusedBackgroundColor,
                unfocusedContainerColor = backgroundColor,
                disabledContainerColor = backgroundColor.copy(alpha = 0.6f),
                errorContainerColor = backgroundColor,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent,
                focusedTextColor = textColor,
                unfocusedTextColor = textColor,
                disabledTextColor = textColor.copy(alpha = 0.6f),
                errorTextColor = textColor
            )
        )

        // Error message
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = S_RED,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )
        }
    }
}
