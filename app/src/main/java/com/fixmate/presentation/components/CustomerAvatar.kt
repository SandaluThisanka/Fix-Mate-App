package com.fixmate.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.firebase.firestore.FirebaseFirestore
import com.fixmate.ui.theme.S_YELLOW

@Composable
fun CustomerAvatar(
    customerId: String,
    isProvider: Boolean = false,
    size: Dp = 40.dp,
    modifier: Modifier = Modifier
) {
    var avatarImageUrl by remember { mutableStateOf<String?>(null) }
    var userProfileImageUrl by remember { mutableStateOf<String?>(null) }
    var providerProfileImageUrl by remember { mutableStateOf<String?>(null) }
    var isLoadingImage by remember { mutableStateOf(false) }

    DisposableEffect(customerId, isProvider) {
        val firestore = FirebaseFirestore.getInstance()
        isLoadingImage = true

        if (customerId.isNotEmpty()) {
            val userListener = firestore.collection("users")
                .document(customerId)
                .addSnapshotListener { document, error ->
                    if (error != null) {
                        avatarImageUrl = null
                        userProfileImageUrl = null
                        isLoadingImage = false
                        return@addSnapshotListener
                    }

                    if (document != null && document.exists()) {
                        userProfileImageUrl = document.getString("profileImageUrl")
                        avatarImageUrl = userProfileImageUrl ?: providerProfileImageUrl
                    } else {
                        userProfileImageUrl = null
                        avatarImageUrl = providerProfileImageUrl
                    }

                    isLoadingImage = false
                }

            val providerListener = if (isProvider) {
                firestore.collection("service_providers")
                    .document(customerId)
                    .addSnapshotListener { document, error ->
                        if (error != null) {
                            providerProfileImageUrl = null
                            avatarImageUrl = userProfileImageUrl
                            isLoadingImage = false
                            return@addSnapshotListener
                        }

                        if (document != null && document.exists()) {
                            providerProfileImageUrl = document.getString("profileImageUrl")
                            avatarImageUrl = userProfileImageUrl ?: providerProfileImageUrl
                        } else {
                            providerProfileImageUrl = null
                            avatarImageUrl = userProfileImageUrl
                        }

                        isLoadingImage = false
                    }
            } else {
                null
            }

            onDispose {
                userListener.remove()
                providerListener?.remove()
            }
        } else {
            isLoadingImage = false
            onDispose { }
        }
    }
    
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(Color.Gray.copy(alpha = 0.3f))
    ) {
        when {
            isLoadingImage -> {
                // Show loading indicator
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(size * 0.5f)
                        .align(Alignment.Center),
                    strokeWidth = 2.dp,
                    color = S_YELLOW
                )
            }
            !avatarImageUrl.isNullOrEmpty() -> {
                // Show the uploaded profile image from the user profile, with provider fallback
                AsyncImage(
                    model = avatarImageUrl,
                    contentDescription = if (isProvider) "Provider Profile Picture" else "Customer Profile Picture",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
             else -> {
                // Show default avatar icon when no profile image
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Default Avatar",
                    modifier = Modifier
                        .size(size * 0.6f)
                        .align(Alignment.Center),
                    tint = Color.Gray
                )
            }
        }
    }
}
