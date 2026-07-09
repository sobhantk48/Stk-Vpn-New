# حفظ کلاس‌های libbox
-keep class io.nekohasekai.libbox.** { *; }
-keep interface io.nekohasekai.libbox.** { *; }

# حفظ کلاس‌های سریال‌سازی
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class kotlinx.serialization.** { *; }
-keep class kotlinx.serialization.** { *; }

# حفظ کلاس‌های داده‌ی Profile و FullBackupData
-keep class com.v2ray.app.data.Profile { *; }
-keep class com.v2ray.app.viewmodel.FullBackupData { *; }

# حذف لاگ‌ها در نسخه‌ی Release
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}
-keep class com.v2ray.app.utils.Logger { *; }
