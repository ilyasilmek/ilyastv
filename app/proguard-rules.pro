# Project specific ProGuard / R8 rules for İlyasTV

# Keep data models used for Room, Gson, Retrofit, Serialization, UI models
-keep class com.example.data.model.** { *; }
-keep class com.example.data.local.** { *; }

# Keep Room generated classes & annotations
-keep class * extends androidx.room.RoomDatabase
-keepclassmembers class * {
    @androidx.room.* <fields>;
    @androidx.room.* <methods>;
}
-dontwarn androidx.room.**

# Retrofit & OkHttp
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepclassmembers enum * { *; }
-dontwarn okhttp3.**
-dontwarn retrofit2.**

# Firebase & Annotations
-keepattributes *Annotation*

# Preserve line numbers for stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
