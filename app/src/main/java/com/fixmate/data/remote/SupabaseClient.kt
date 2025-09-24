package com.fixmate.data.remote

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.storage.Storage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseClient @Inject constructor() {
    
    val client = createSupabaseClient(
        supabaseUrl = "https://qirqsvnqewqmhtgiftcj.supabase.co", // Replace with your actual Supabase URL
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InFpcnFzdm5xZXdxbWh0Z2lmdGNqIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTMxOTYyMjQsImV4cCI6MjA2ODc3MjIyNH0.TvTAa-2iDf4G91RgTNQNlVAAItZJN7lZiAlgVmUUJKc" // Replace with your actual Supabase anon key
    ) {
        install(Storage)
    }
    
    companion object {
        const val PROFILE_IMAGES_BUCKET = "profile-images"
        const val CHAT_IMAGES_BUCKET = "chat-images"
    }
}
