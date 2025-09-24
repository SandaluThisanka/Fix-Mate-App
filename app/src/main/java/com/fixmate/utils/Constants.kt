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
        const val HOME_SERVICES = "Home Services"
        const val EDUCATION_TUTORING = "Education & Tutoring"
        const val PERSONAL_CARE_WELLNESS = "Personal Care & Wellness"
        const val AUTOMOTIVE_SERVICES = "Automotive Services"
        const val EVENTS_ENTERTAINMENT = "Events & Entertainment"
        const val TECHNOLOGY_DIGITAL = "Technology & Digital"
        const val BUSINESS_PROFESSIONAL = "Business & Professional"
        const val TRANSPORTATION_DELIVERY = "Transportation & Delivery"
    }

    // Service Display Names
    object ServiceNames {

        // Home Services
        const val PLUMBING = "Plumbing"
        const val CLEANING_RESIDENTIAL = "Cleaning (Residential)"
        const val PAINTING_DECORATING = "Painting & Decorating"
        const val APPLIANCE_REPAIR = "Appliance Repair"

        // Education & Tutoring
        const val MATH_TUTORING = "Math Tutoring"
        const val MUSIC_LESSONS = "Music Lessons"
        const val TEST_PREP = "Test Prep (SAT, ACT, etc.)"
        const val ACADEMIC_WRITING_HELP = "Academic Writing Help"

        // Personal Care & Wellness
        const val HAIR_STYLING_CUTTING = "Hair Styling & Cutting"
        const val MASSAGE_THERAPY = "Massage Therapy"
        const val PERSONAL_TRAINING = "Personal Training"
        const val CHILD_CARE_BABYSITTING = "Child Care/Babysitting"

        // Automotive Services
        const val CAR_REPAIR_MAINTENANCE = "Car Repair & Maintenance"
        const val OIL_CHANGE = "Oil Change"
        const val CAR_DETAILING_WASHING = "Car Detailing & Washing"
        const val TIRE_SERVICES = "Tire Services"

        // Events & Entertainment
        const val EVENT_PHOTOGRAPHY = "Event Photography"
        const val CATERING_SERVICES = "Catering Services"
        const val DJ_SERVICES = "DJ Services"
        const val EQUIPMENT_RENTAL = "Equipment Rental"

        // Technology & Digital
        const val COMPUTER_REPAIR = "Computer Repair"
        const val PHONE_TABLET_REPAIR = "Phone/Tablet Repair"
        const val SMART_HOME_SETUP = "Smart Home Setup"
        const val SECURITY_SYSTEM_INSTALLATION = "Security System Installation"

        // Business & Professional
        const val ACCOUNTING_BOOKKEEPING = "Accounting & Bookkeeping"
        const val LEGAL_CONSULTATION = "Legal Consultation"
        const val GRAPHIC_DESIGN = "Graphic Design"
        const val CONTENT_WRITING = "Content Writing"

        // Transportation & Delivery
        const val MOVING_SERVICES = "Moving Services"
        const val DELIVERY_SERVICES = "Delivery Services"
        const val RIDE_SERVICES = "Ride Services"
        const val FURNITURE_ASSEMBLY = "Furniture Assembly"
    }

    object ServiceDescriptions {

        // Home Services
        const val PLUMBING = "Fixing and installing water systems like pipes and faucets."
        const val CLEANING_RESIDENTIAL = "General home cleaning services."
        const val PAINTING_DECORATING = "Interior and exterior painting, wall design."
        const val APPLIANCE_REPAIR = "Repairing home appliances like refrigerators, washing machines."

        // Education & Tutoring
        const val MATH_TUTORING = "Helping students understand and solve math problems."
        const val MUSIC_LESSONS = "Teaching instruments or vocals."
        const val TEST_PREP = "Guiding students in preparation for exams."
        const val ACADEMIC_WRITING_HELP = "Assisting with essays and academic papers."

        // Personal Care & Wellness
        const val HAIR_STYLING_CUTTING = "Haircuts and styling for men, women, and children."
        const val MASSAGE_THERAPY = "Relaxation and therapeutic massages."
        const val PERSONAL_TRAINING = "Fitness training and workout guidance."
        const val CHILD_CARE_BABYSITTING = "Looking after children at home."

        // Automotive Services
        const val CAR_REPAIR_MAINTENANCE = "General vehicle repair and upkeep."
        const val OIL_CHANGE = "Replacing engine oil and filter."
        const val CAR_DETAILING_WASHING = "Cleaning and polishing vehicles."
        const val TIRE_SERVICES = "Tire fitting, rotation, or repairs."

        // Events & Entertainment
        const val EVENT_PHOTOGRAPHY = "Capturing photos at events."
        const val CATERING_SERVICES = "Providing food and drinks for events."
        const val DJ_SERVICES = "Playing music at parties and events."
        const val EQUIPMENT_RENTAL = "Providing rental equipment for events."

        // Technology & Digital
        const val COMPUTER_REPAIR = "Fixing computer hardware and software issues."
        const val PHONE_TABLET_REPAIR = "Repairing smartphones and tablets."
        const val SMART_HOME_SETUP = "Installing smart home devices."
        const val SECURITY_SYSTEM_INSTALLATION = "Setting up home and office security systems."

        // Business & Professional
        const val ACCOUNTING_BOOKKEEPING = "Managing financial records and taxes."
        const val LEGAL_CONSULTATION = "Providing legal advice and services."
        const val GRAPHIC_DESIGN = "Creating visual designs for branding or marketing."
        const val CONTENT_WRITING = "Writing articles, blogs, or web content."

        // Transportation & Delivery
        const val MOVING_SERVICES = "Helping with home or office relocation."
        const val DELIVERY_SERVICES = "Delivering goods to customers."
        const val RIDE_SERVICES = "Providing transportation for passengers."
        const val FURNITURE_ASSEMBLY = "Putting together furniture at customer location."
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
