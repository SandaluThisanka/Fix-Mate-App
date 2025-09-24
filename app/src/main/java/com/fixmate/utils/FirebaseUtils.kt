package com.fixmate.utils

import com.google.firebase.FirebaseException
import com.google.firebase.firestore.FirebaseFirestoreException

object FirebaseUtils {
    fun getReadableErrorMessage(error: Exception): String {
        return when (error) {
            is FirebaseFirestoreException -> {
                when (error.code) {
                    FirebaseFirestoreException.Code.PERMISSION_DENIED -> 
                        "Permission denied: Please check your account settings and try again."
                    FirebaseFirestoreException.Code.UNAUTHENTICATED -> 
                        "Authentication required. Please sign in again."
                    FirebaseFirestoreException.Code.UNAVAILABLE -> 
                        "Service is currently unavailable. Please check your internet connection."
                    FirebaseFirestoreException.Code.NOT_FOUND -> 
                        "Resource not found. Please try again."
                    else -> "An error occurred: ${error.message}"
                }
            }
            is FirebaseException -> 
                "Authentication error: ${error.message}"
            else -> error.message ?: "An unknown error occurred"
        }
    }
}