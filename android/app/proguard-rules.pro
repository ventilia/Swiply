# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** { kotlinx.serialization.KSerializer serializer(...); }
-keep,includedescriptorclasses class com.swiply.app.**$$serializer { *; }
-keepclassmembers class com.swiply.app.** { *** Companion; }
-keepclasseswithmembers class com.swiply.app.** { kotlinx.serialization.KSerializer serializer(...); }

# Retrofit + OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-keepattributes Signature, Exceptions
-keepclasseswithmembers class * { @retrofit2.http.* <methods>; }

# Coroutines
-dontwarn kotlinx.coroutines.**
