package com.fixmate.utils

object Constants {

    // Firebase Collections
    const val COLLECTION_USERS = "users"
    const val COLLECTION_SERVICE_PROVIDERS = "service_providers"
    const val COLLECTION_SERVICES = "services"
    const val COLLECTION_BOOKINGS = "bookings"
    const val COLLECTION_CHATS = "chats"
    const val COLLECTION_MESSAGES = "messages"
    const val COLLECTION_REVIEWS = "reviews"
    const val COLLECTION_CATEGORIES = "categories"

    // User Types
    const val USER_TYPE_CUSTOMER = "customer"
    const val USER_TYPE_PROVIDER = "provider"

    // Notification Channels
    const val NOTIFICATION_CHANNEL_BOOKING = "booking_notifications"
    const val NOTIFICATION_CHANNEL_CHAT = "chat_notifications"
    const val NOTIFICATION_CHANNEL_PROMO = "promo_notifications"
    const val NOTIFICATION_CHANNEL_GENERAL = "general_notifications"

    // Shared Preferences Keys
    const val PREF_USER_ID = "user_id"
    const val PREF_USER_TYPE = "user_type"
    const val PREF_IS_FIRST_LAUNCH = "is_first_launch"
    const val PREF_FCM_TOKEN = "fcm_token"
    const val PREF_LOCATION_PERMISSION = "location_permission"
    const val PREF_NOTIFICATION_ENABLED = "notification_enabled"
    
    // Verification
    const val VERIFICATION_CODE_EXPIRY = 600000L // 10 minutes in milliseconds

    // Booking Status
    const val BOOKING_STATUS_PENDING = "pending"
    const val BOOKING_STATUS_CONFIRMED = "confirmed"
    const val BOOKING_STATUS_IN_PROGRESS = "in_progress"
    const val BOOKING_STATUS_COMPLETED = "completed"
    const val BOOKING_STATUS_CANCELLED = "cancelled"
    
    // Booking Fields
    const val FIELD_PROVIDER_NAME = "providerName"
    const val FIELD_CUSTOMER_NAME = "customerName"
    const val FIELD_SERVICE_NAME = "serviceName"

    // Message Types
    const val MESSAGE_TYPE_TEXT = "text"
    const val MESSAGE_TYPE_IMAGE = "image"
    const val MESSAGE_TYPE_LOCATION = "location"
    const val MESSAGE_TYPE_BOOKING_REQUEST = "booking_request"

    // Service Categories
    object CategoryNames {
        const val MECHANICAL = "Mechanical"
        const val ELECTRICAL = "Electrical"
        const val SUSPENSION = "Suspension"
        const val BRAKES = "Brakes"
        const val BODY_CARE = "Body & Care"
        const val TIRES = "Tires"
        const val EMERGENCY = "Emergency"
    }

    // Service Display Names
    object ServiceNames {

        // Mechanical
        const val GENERAL_SERVICE = "General Service"
        const val ENGINE_REPAIR = "Engine Repair"
        const val GEARBOX_CLUTCH = "Gearbox & Clutch"
        const val FUEL_SYSTEM_REPAIR = "Fuel System Repair"

        // Electrical
        const val AUTO_ELECTRICAL_REPAIR = "Auto Electrical Repair"
        const val BATTERY_REPLACEMENT = "Battery Replacement"
        const val ECU_SCANNING = "ECU Scanning"
        const val LIGHTING_WIRING = "Lighting & Wiring"

        // Suspension
        const val SUSPENSION_REPAIR = "Suspension Repair"
        const val SHOCK_ABSORBERS = "Shock Absorbers"
        const val STEERING_REPAIR = "Steering Repair"
        const val WHEEL_ALIGNMENT = "Wheel Alignment"

        // Brakes
        const val BRAKE_REPAIR = "Brake Repair"
        const val ABS_REPAIR = "ABS Repair"
        const val BRAKE_PADS_DISCS = "Brake Pads & Discs"

        // Body & Care
        const val BODY_PAINTING = "Body Painting"
        const val DENTING_WELDING = "Denting & Welding"
        const val INTERIOR_CLEANING = "Interior Cleaning"
        const val FULL_DETAILING = "Full Detailing"

        // Tires
        const val TIRE_REPLACEMENT = "Tire Replacement"
        const val PUNCTURE_REPAIR = "Puncture Repair"
        const val WHEEL_BALANCING = "Wheel Balancing"

        // Emergency
        const val TOWING_SERVICE = "Towing Service"
        const val JUMP_START = "Jump Start"
        const val ROADSIDE_ASSISTANCE = "Roadside Assistance"
        const val MOBILE_MECHANIC = "Mobile Mechanic"
    }

    object ServiceDescriptions {

        // Mechanical
        const val GENERAL_SERVICE = "Routine vehicle servicing and maintenance."
        const val ENGINE_REPAIR = "Engine diagnosis and repair."
        const val GEARBOX_CLUTCH = "Transmission and clutch repairs."
        const val FUEL_SYSTEM_REPAIR = "Fuel pump and injector servicing."

        // Electrical
        const val AUTO_ELECTRICAL_REPAIR = "Electrical system fault repairs."
        const val BATTERY_REPLACEMENT = "Battery testing and replacement."
        const val ECU_SCANNING = "Computer-based diagnostics for vehicle faults."
        const val LIGHTING_WIRING = "Lighting and wiring issue repairs."

        // Suspension
        const val SUSPENSION_REPAIR = "Suspension system inspections and repairs."
        const val SHOCK_ABSORBERS = "Shock absorber replacement services."
        const val STEERING_REPAIR = "Power steering maintenance and repairs."
        const val WHEEL_ALIGNMENT = "Wheel alignment and balancing services."

        // Brakes
        const val BRAKE_REPAIR = "Brake system inspection and repairs."
        const val ABS_REPAIR = "ABS system diagnostics and repairs."
        const val BRAKE_PADS_DISCS = "Brake pad and disc replacement."

        // Body & Care
        const val BODY_PAINTING = "Vehicle body painting and touch-ups."
        const val DENTING_WELDING = "Accident dent repair and welding."
        const val INTERIOR_CLEANING = "Interior detailing and cleaning."
        const val FULL_DETAILING = "Complete vehicle detailing service."

        // Tires
        const val TIRE_REPLACEMENT = "New tire fitting and rotation."
        const val PUNCTURE_REPAIR = "Tire puncture repairs."
        const val WHEEL_BALANCING = "Precision wheel balancing service."

        // Emergency
        const val TOWING_SERVICE = "Vehicle towing assistance."
        const val JUMP_START = "On-site battery jump start."
        const val ROADSIDE_ASSISTANCE = "Emergency roadside assistance."
        const val MOBILE_MECHANIC = "Mechanic dispatched to your location."
    }

    // Location & Search
    const val DEFAULT_SEARCH_RADIUS_KM = 15.0
    const val MAX_SEARCH_RADIUS_KM = 50.0
    const val NEARBY_PROVIDER_RADIUS_KM = 15.0
    const val LOCATION_UPDATE_INTERVAL = 10000L // 10 seconds
    const val LOCATION_FASTEST_INTERVAL = 5000L // 5 seconds

    // Image Upload
    const val MAX_IMAGE_SIZE_MB = 5
    const val IMAGE_COMPRESSION_QUALITY = 80
    const val MAX_IMAGES_PER_SERVICE = 5
    const val MAX_IMAGES_PER_CHAT = 3

    // Pagination
    const val PAGE_SIZE = 20
    const val INITIAL_LOAD_SIZE = 40

    // Rating System
    const val MIN_RATING = 1.0f
    const val MAX_RATING = 5.0f
    const val MIN_REVIEWS_FOR_VISIBILITY = 3

    // Payment
    const val COMMISSION_RATE = 0.05 // 5%
    const val MIN_SERVICE_PRICE = 10.0
    const val MAX_SERVICE_PRICE = 10000.0

    // Network & Caching
    const val NETWORK_TIMEOUT_SECONDS = 30L
    const val CACHE_SIZE_MB = 50L
    const val OFFLINE_CACHE_DAYS = 7

    // Animation Durations
    const val ANIMATION_DURATION_SHORT = 200
    const val ANIMATION_DURATION_MEDIUM = 300
    const val ANIMATION_DURATION_LONG = 500

    // Intent Keys
    const val EXTRA_USER_ID = "extra_user_id"
    const val EXTRA_SERVICE_ID = "extra_service_id"
    const val EXTRA_BOOKING_ID = "extra_booking_id"
    const val EXTRA_PROVIDER_ID = "extra_provider_id"
    const val EXTRA_CHAT_ID = "extra_chat_id"

    // Request Codes
    const val REQUEST_LOCATION_PERMISSION = 1001
    const val REQUEST_CAMERA_PERMISSION = 1002
    const val REQUEST_STORAGE_PERMISSION = 1003
    const val REQUEST_NOTIFICATION_PERMISSION = 1004

    // Firebase Storage Paths
    const val STORAGE_PROFILE_IMAGES = "profile_images"
    const val STORAGE_SERVICE_IMAGES = "service_images"
    const val STORAGE_CHAT_IMAGES = "chat_images"
    const val STORAGE_BEFORE_AFTER_IMAGES = "before_after_images"

    // Validation Rules
    const val MIN_PASSWORD_LENGTH = 6
    const val MAX_BIO_LENGTH = 500
    const val MAX_SERVICE_DESCRIPTION_LENGTH = 1000
    const val MIN_SERVICE_TITLE_LENGTH = 5
    const val MAX_SERVICE_TITLE_LENGTH = 100

    // Time Formats
    const val TIME_FORMAT_12H = "hh:mm a"
    const val TIME_FORMAT_24H = "HH:mm"
    const val DATE_FORMAT_DISPLAY = "MMM dd, yyyy"
    const val DATE_FORMAT_API = "yyyy-MM-dd"
    const val DATETIME_FORMAT_API = "yyyy-MM-dd'T'HH:mm:ss'Z'"

    // Deep Links
    const val DEEP_LINK_SCHEME = "fixmate"
    const val DEEP_LINK_HOST = "app"
    const val DEEP_LINK_SERVICE_DETAIL = "/service/{serviceId}"
    const val DEEP_LINK_PROVIDER_PROFILE = "/provider/{providerId}"
    const val DEEP_LINK_BOOKING_DETAIL = "/booking/{bookingId}"

    // Error Messages
    const val ERROR_NETWORK = "Please check your internet connection"
    const val ERROR_LOCATION_PERMISSION = "Location permission is required"
    const val ERROR_CAMERA_PERMISSION = "Camera permission is required"
    const val ERROR_STORAGE_PERMISSION = "Storage permission is required"
    const val ERROR_GENERIC = "Something went wrong. Please try again."
    const val ERROR_USER_NOT_FOUND = "User not found"
    const val ERROR_SERVICE_NOT_FOUND = "Service not found"
    const val ERROR_BOOKING_NOT_FOUND = "Booking not found"
}
