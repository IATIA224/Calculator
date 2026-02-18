# Retrofit
-keepattributes Signature
-keepattributes *Annotation*
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# Gson
-keep class com.calmahahh.app.api.** { *; }
-keep class com.calmahahh.app.model.** { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
