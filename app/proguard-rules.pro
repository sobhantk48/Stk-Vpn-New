# Keep model classes
-keep class com.v2ray.app.data.** { *; }

# Keep ViewModel
-keep class com.v2ray.app.viewmodel.** { *; }

# Keep Hilt-generated classes
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# Keep serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class kotlinx.serialization.** { *; }

# Keep libv2ray (Xray-core)
-keep class libv2ray.** { *; }
-keep class go.** { *; }

# Keep Room
-keep class androidx.room.** { *; }
-keep @androidx.room.Entity class *

# Keep Compose
-keep class androidx.compose.** { *; }

# Keep DataStore
-keep class androidx.datastore.** { *; }

# Keep Kotlin reflection (for serialization)
-keep class kotlin.reflect.** { *; }
-keep class kotlinx.serialization.** { *; }

# Remove debug logs (optional)
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
}
