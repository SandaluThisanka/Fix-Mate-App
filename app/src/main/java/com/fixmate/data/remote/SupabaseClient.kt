package com.fixmate.data.remote

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.storage.Storage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseClient @Inject constructor() {
    
    val client = createSupabaseClient(
        supabaseUrl = "https://rhrhemmogrknrmopzffs.supabase.co", // Replace with your actual Supabase URL
        supabaseKey = "sb_publishable_0tjPA8qSgnHhgW6GfTSIXg_8STblLcX" // Replace with your actual Supabase anon key
    ) {
        install(Storage)
    }
    
    companion object {
        const val PROFILE_IMAGES_BUCKET = "profile-images"
        const val CHAT_IMAGES_BUCKET = "chat-images"
    }
}
