package com.fixmate.data.repositories

import com.fixmate.data.models.User
import javax.inject.Inject

interface UserRepository {
    suspend fun getUserById(userId: String): Result<User>
    suspend fun getUserProfileImageUrl(userId: String): Result<String?>
}

class UserRepositoryImpl @Inject constructor(
    private val authRepository: AuthRepository
) : UserRepository {
    
    override suspend fun getUserById(userId: String): Result<User> {
        return authRepository.getUserData(userId)
    }
    
    override suspend fun getUserProfileImageUrl(userId: String): Result<String?> {
        return try {
            val userResult = authRepository.getUserData(userId)
            userResult.fold(
                onSuccess = { user ->
                    Result.success(user.profileImageUrl.takeIf { it.isNotEmpty() })
                },
                onFailure = { exception ->
                    Result.failure(exception)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
