package com.fixmate.di

import com.fixmate.data.repositories.PaymentRepository
import com.fixmate.data.repositories.PaymentRepositoryImpl
import com.fixmate.data.repositories.BookingRepository
import com.fixmate.data.repositories.BookingRepositoryImpl
import com.fixmate.data.repositories.UserRepository
import com.fixmate.data.repositories.UserRepositoryImpl
import com.fixmate.data.repositories.NotificationRepository
import com.fixmate.data.repositories.NotificationRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindBookingRepository(
        bookingRepositoryImpl: BookingRepositoryImpl
    ): BookingRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(
        notificationRepositoryImpl: NotificationRepositoryImpl
    ): NotificationRepository

    @Binds
    @Singleton
    abstract fun bindPaymentRepository(
        paymentRepositoryImpl: PaymentRepositoryImpl
    ): PaymentRepository
}

