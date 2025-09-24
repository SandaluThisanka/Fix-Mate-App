package com.fixmate.presentation.navigation

object NavigationDestinations {
    const val HOME = "home"
    const val SEARCH = "search"
    const val BOOKINGS = "bookings"
    const val MESSAGES = "messages"
    const val PROFILE = "profile"
    const val JOB_DETAILS = "job_details/{jobId}"
    
    fun jobDetails(jobId: String) = "job_details/$jobId"
}
