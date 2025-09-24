# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Enhanced signature preservation - CRITICAL for generic types and DI
-keepattributes Signature,InnerClasses,EnclosingMethod
-keepattributes *Annotation*
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeInvisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations
-keepattributes RuntimeInvisibleParameterAnnotations

# Kotlin metadata annotations (important for generic type preservation)
-keep class kotlin.Metadata { *; }
-keepclassmembers class kotlin.Metadata { *; }

# Kotlin Parcelize - Fix for Stripe SDK
-keep class kotlinx.parcelize.** { *; }
-dontwarn kotlinx.parcelize.**
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# Stripe SDK
-keep class com.stripe.** { *; }
-dontwarn com.stripe.**
-keepclassmembers class com.stripe.** { *; }

# Firebase Messaging
-keep class com.google.firebase.messaging.** { *; }
-keep class com.google.firebase.iid.** { *; }
-dontwarn com.google.firebase.messaging.**

# Firebase Core
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# Firebase Firestore
-keepclassmembers class com.google.firebase.firestore.** { *; }
-keep class com.google.firebase.firestore.** { *; }

# Firebase Auth
-keep class com.google.firebase.auth.** { *; }

# Firebase Database
-keep class com.google.firebase.database.** { *; }

# Firebase Tasks
-keep class com.google.android.gms.tasks.** { *; }
-keepclassmembers class com.google.android.gms.tasks.** { *; }

# Gson (used for JSON serialization)
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

# SevaLK Custom Services - Enhanced
-keep class com.fixmate.services.** { *; }

# FCM Service
-keep class com.fixmate.services.SevaLKFirebaseMessagingService { *; }
-keep class com.fixmate.services.FCMNotificationService { *; }
-keep class com.fixmate.services.FCMTokenManager { *; }

# Data Models - Keep all with signatures
-keep class com.fixmate.data.models.** { *; }
-keepclassmembers class com.fixmate.data.models.** { *; }

# Payment specific models
-keep class com.fixmate.data.models.CreatePaymentIntentRequest { *; }
-keep class com.fixmate.data.models.CreatePaymentIntentResponse { *; }
-keep class com.fixmate.data.models.ConfirmPaymentRequest { *; }
-keep class com.fixmate.data.models.ConfirmPaymentResponse { *; }
-keep class com.fixmate.data.models.CashPaymentRequest { *; }
-keep class com.fixmate.data.models.CashPaymentResponse { *; }
-keep class com.fixmate.data.models.Payment { *; }
-keep class com.fixmate.data.models.PaymentStatus { *; }

# Repositories - Critical for DI
-keep interface com.fixmate.data.repositories.** { *; }
-keep class com.fixmate.data.repositories.** { *; }
-keepclassmembers class com.fixmate.data.repositories.** { *; }

# Payment Repository specifically
-keep interface com.fixmate.data.repositories.PaymentRepository { *; }
-keep class com.fixmate.data.repositories.PaymentRepositoryImpl { *; }

# Notification Repository
-keep class com.fixmate.data.repositories.NotificationRepository { *; }
-keep class com.fixmate.data.repositories.NotificationRepositoryImpl { *; }

# API Services
-keep interface com.fixmate.data.api.** { *; }
-keepclassmembers interface com.fixmate.data.api.** { *; }

# Preserve generic type information for Result class
-keep class kotlin.Result { *; }
-keepclassmembers class kotlin.Result { *; }

# AGGRESSIVE Hilt/Dagger rules - Fix for ClassCastException
-keep class dagger.** { *; }
-keep class javax.inject.** { *; }
-dontwarn dagger.**

# Keep all Hilt classes and generated code
-keep class dagger.hilt.** { *; }
-keep class **_HiltComponents { *; }
-keep class **_HiltComponents$* { *; }
-keep class **Hilt** { *; }
-keep class **_Factory { *; }
-keep class **_MembersInjector { *; }
-keep class dagger.hilt.android.** { *; }
-keep class dagger.hilt.internal.** { *; }

# Keep all classes with Hilt annotations
-keep @dagger.hilt.android.HiltAndroidApp class * { *; }
-keep @dagger.hilt.android.AndroidEntryPoint class * { *; }
-keep @dagger.Module class * { *; }
-keep @dagger.hilt.InstallIn class * { *; }
-keep @javax.inject.Singleton class * { *; }
-keep @javax.inject.Inject class * { *; }

# Preserve ALL constructors, methods, and fields with inject annotations
-keepclassmembers class * {
    @javax.inject.Inject <init>(...);
    @javax.inject.Inject <fields>;
    @javax.inject.Inject <methods>;
    @dagger.Provides <methods>;
    @dagger.Binds <methods>;
}

# Keep classes that extend HiltAndroidApp
-keep class * extends dagger.hilt.android.HiltAndroidApp { *; }

# Preserve generic type information for all injected classes
-keepclassmembers class * {
    @javax.inject.Inject *;
}

# Additional rules for parameterized types
-keep class java.lang.reflect.ParameterizedType { *; }
-keep class java.lang.reflect.Type { *; }
-keep class java.lang.reflect.WildcardType { *; }
-keep class java.lang.reflect.TypeVariable { *; }
-keep class java.lang.reflect.GenericArrayType { *; }

# Keep all reflection classes
-keep class java.lang.reflect.** { *; }

# AndroidX and Support Libraries
-keep class androidx.** { *; }
-dontwarn androidx.**

# Coroutines - Enhanced
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**
-keepclassmembernames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepclassmembernames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.coroutines.android.AndroidExceptionPreHandler {}
-keepclassmembernames class kotlinx.coroutines.android.AndroidDispatcherFactory {}
-keepclassmembernames class kotlinx.coroutines.** { *; }

# OkHttp and networking - Enhanced for Retrofit
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# OkHttp platform used only on JVM and when Conscrypt and other security providers are available.
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# Animal Sniffer compileOnly dependency to ensure APIs are compatible with older versions of Java.
-dontwarn org.codehaus.mojo.animal_sniffer.*

# RETROFIT - Enhanced rules to fix ClassCastException
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations

# Keep all Retrofit classes
-keep class retrofit2.** { *; }
-keepclassmembers class retrofit2.** { *; }
-dontwarn retrofit2.**

# Keep Response class with generic information
-keep class retrofit2.Response { *; }
-keepclassmembers class retrofit2.Response { *; }

# Keep all HTTP annotation methods
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Keep your specific API service interface
-keep interface com.fixmate.data.api.PaymentApiService { *; }
-keepclassmembers interface com.fixmate.data.api.PaymentApiService {
    @retrofit2.http.POST *** createPaymentIntent(...);
    @retrofit2.http.POST *** confirmPayment(...);
    @retrofit2.http.POST *** processCashPayment(...);
}

# Keep all request/response model classes used by Retrofit
-keep class com.fixmate.data.models.CreatePaymentIntentRequest { *; }
-keep class com.fixmate.data.models.CreatePaymentIntentResponse { *; }
-keep class com.fixmate.data.models.ConfirmPaymentRequest { *; }
-keep class com.fixmate.data.models.ConfirmPaymentResponse { *; }
-keep class com.fixmate.data.models.CashPaymentRequest { *; }
-keep class com.fixmate.data.models.CashPaymentResponse { *; }
-keepclassmembers class com.fixmate.data.models.** { *; }

# Keep generic signatures for Retrofit calls
-keepattributes Signature,InnerClasses,EnclosingMethod

# Keep Call interface
-keep interface retrofit2.Call
-keep class retrofit2.Call { *; }

# Platform calls Class.forName on types which do not exist on Android to determine platform.
-dontnote retrofit2.Platform
# Platform used when running on Java 8 VMs. Will not be used at runtime.
-dontwarn retrofit2.Platform$Java8

# Retain generic type information for use by reflection by converters and adapters.
-keepattributes Signature
# Retain declared checked exceptions for use by a Proxy instance.
-keepattributes Exceptions

# Parceable
-keep interface android.os.Parcelable
-keepclassmembers class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator CREATOR;
}

# Serializable
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Timber
-keep class timber.log.** { *; }
-dontwarn timber.log.**

# Email libraries
-keep class com.sun.mail.** { *; }
-keep class javax.mail.** { *; }
-keep class javax.activation.** { *; }
-dontwarn com.sun.mail.**
-dontwarn javax.mail.**
-dontwarn javax.activation.**

# Google Play Services
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# Maps
-keep class com.google.android.gms.maps.** { *; }
-keep interface com.google.android.gms.maps.** { *; }

# HTTP URL Connection (for FCM)
-keep class java.net.** { *; }
-keep class java.io.** { *; }

# Compose (if any issues)
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# SLF4J (fixes R8 missing class error)
-dontwarn org.slf4j.impl.StaticLoggerBinder
-dontwarn org.slf4j.**
-keep class org.slf4j.** { *; }

# Reflection preservation
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeInvisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations
-keepattributes RuntimeInvisibleParameterAnnotations

# Keep enum values
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Generic signatures for collections
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# View system
-keepclassmembers public class * extends android.view.View {
   void set*(***);
   *** get*();
}

# Keep custom views
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# Activity and Fragment rules
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends androidx.fragment.app.Fragment

# Keep onClick methods
-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}

# Keep R class
-keep class **.R
-keep class **.R$* {
    <fields>;
}

# Specific rules for your Hilt module and repositories
-keep class com.fixmate.di.RepositoryModule { *; }
-keepclassmembers class com.fixmate.di.RepositoryModule { *; }

# Keep all your repository interfaces and implementations with exact names
-keep interface com.fixmate.data.repositories.PaymentRepository { *; }
-keep class com.fixmate.data.repositories.PaymentRepositoryImpl { *; }
-keep interface com.fixmate.data.repositories.BookingRepository { *; }
-keep class com.fixmate.data.repositories.BookingRepositoryImpl { *; }
-keep interface com.fixmate.data.repositories.UserRepository { *; }
-keep class com.fixmate.data.repositories.UserRepositoryImpl { *; }
-keep interface com.fixmate.data.repositories.AuthRepository { *; }
-keep interface com.fixmate.data.repositories.NotificationRepository { *; }
-keep class com.fixmate.data.repositories.NotificationRepositoryImpl { *; }

# Keep your PaymentApiService interface
-keep interface com.fixmate.data.api.PaymentApiService { *; }
-keepclassmembers interface com.fixmate.data.api.PaymentApiService { *; }

# Keep all Dagger/Hilt binding methods with exact signatures
-keepclassmembers class com.fixmate.di.RepositoryModule {
    abstract com.fixmate.data.repositories.BookingRepository bindBookingRepository(com.fixmate.data.repositories.BookingRepositoryImpl);
    abstract com.fixmate.data.repositories.UserRepository bindUserRepository(com.fixmate.data.repositories.UserRepositoryImpl);
    abstract com.fixmate.data.repositories.NotificationRepository bindNotificationRepository(com.fixmate.data.repositories.NotificationRepositoryImpl);
    abstract com.fixmate.data.repositories.PaymentRepository bindPaymentRepository(com.fixmate.data.repositories.PaymentRepositoryImpl);
}

# NUCLEAR OPTION - Keep everything for debugging
-keep class com.fixmate.** { *; }
-keepclassmembers class com.fixmate.** { *; }

# NUCLEAR RETROFIT - Keep everything related to Retrofit
-keep class retrofit2.** { *; }
-keep interface retrofit2.** { *; }
-keepclassmembers class retrofit2.** { *; }
-keepclassmembers interface retrofit2.** { *; }

# NUCLEAR GSON - Keep everything related to Gson
-keep class com.google.gson.** { *; }
-keepclassmembers class com.google.gson.** { *; }

# NUCLEAR REFLECTION - Keep all reflection classes
-keep class java.lang.reflect.** { *; }
-keepclassmembers class java.lang.reflect.** { *; }

-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

 # R8 full mode strips generic signatures from return types if not kept.
 -if interface * { @retrofit2.http.* public *** *(...); }
 -keep,allowoptimization,allowshrinking,allowobfuscation class <3>

 # With R8 full mode generic signatures are stripped for classes that are not kept.
 -keep,allowobfuscation,allowshrinking class retrofit2.Response

# Disable optimizations that might cause issues
-dontoptimize
-dontobfuscate
-dontpreverify