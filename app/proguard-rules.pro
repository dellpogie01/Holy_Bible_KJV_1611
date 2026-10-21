# Room Database entities and DAOs
-keep class com.example.data.entity.** { *; }
-keep class com.example.data.dao.** { *; }
-keep class com.example.data.database.** { *; }
-keep class androidx.room.** { *; }

# Moshi / JSON Serialization
-keep class com.squareup.moshi.** { *; }
-dontwarn com.squareup.moshi.**
-keepclassmembers class * {
    @com.squareup.moshi.Json <fields>;
}

# Kotlin Coroutines
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-keep class kotlinx.coroutines.** { *; }

# Jetpack Compose & Navigation
-keep class androidx.compose.** { *; }
-keep class androidx.navigation.** { *; }
