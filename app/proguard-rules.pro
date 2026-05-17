# =====================================================
# HALALYTICS - COMPREHENSIVE PROGUARD / R8 RULES
# =====================================================
# Protects APK from reverse engineering while keeping
# all libraries functional.
# =====================================================

# ── Stack trace preservation (for Crashlytics) ───────
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ── General keep rules ───────────────────────────────
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes AnnotationDefault
-keepattributes EnclosingMethod
-keepattributes InnerClasses
-keepattributes Exceptions

# =====================================================
# 1. GSON / JSON Serialization
# =====================================================
-keepattributes Signature
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# =====================================================
# 2. APP DATA MODELS (keep all models for JSON parsing)
# =====================================================
-keep class com.example.halalyticscompose.data.model.** { *; }
-keep class com.example.halalyticscompose.data.api.** { *; }
-keep class com.example.halalyticscompose.data.network.** { *; }
-keep class com.example.halalyticscompose.data.local.entities.** { *; }

# =====================================================
# 3. RETROFIT / OKHTTP
# =====================================================
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-keep class okio.** { *; }

# =====================================================
# 4. HILT / DAGGER (Dependency Injection)
# =====================================================
-dontwarn dagger.**
-keep class dagger.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }
-keep class **_HiltModules* { *; }
-keep class **_Factory { *; }
-keep class **_MembersInjector { *; }

# =====================================================
# 5. ROOM DATABASE
# =====================================================
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-dontwarn androidx.room.paging.**

# =====================================================
# 6. FIREBASE
# =====================================================
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# Firebase Crashlytics
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception

# Firebase Messaging
-keep class com.example.halalyticscompose.messaging.** { *; }

# =====================================================
# 7. ML KIT (Barcode & Text Recognition)
# =====================================================
-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**
-keep class com.google.android.gms.internal.mlkit_vision_** { *; }

# =====================================================
# 8. CAMERAX
# =====================================================
-keep class androidx.camera.** { *; }
-dontwarn androidx.camera.**

# =====================================================
# 9. COIL (Image Loading)
# =====================================================
-keep class io.coil.** { *; }
-dontwarn io.coil.**

# =====================================================
# 10. ZXING (QR Code Generation)
# =====================================================
-keep class com.google.zxing.** { *; }
-dontwarn com.google.zxing.**

# =====================================================
# 11. SQLCIPHER (Database Encryption)
# =====================================================
-keep class net.sqlcipher.** { *; }
-dontwarn net.sqlcipher.**

# =====================================================
# 12. JETPACK COMPOSE
# =====================================================
-dontwarn androidx.compose.**
-keep class androidx.compose.** { *; }

# Keep Compose default parameter wrapper functions
-keepclassmembers class * {
    public static ** access$*(...);
}

# =====================================================
# 13. WORKMANAGER / HILT WORKER
# =====================================================
-keep class * extends androidx.work.Worker { *; }
-keep class * extends androidx.work.ListenableWorker { *; }
-keep class * extends androidx.hilt.work.HiltWorker { *; }
-keep class com.example.halalyticscompose.data.worker.** { *; }

# =====================================================
# 14. NAVIGATION COMPOSE
# =====================================================
-keep class androidx.navigation.** { *; }

# =====================================================
# 15. KOTLIN SPECIFIC
# =====================================================
-dontwarn kotlin.**
-keep class kotlin.Metadata { *; }
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class * {
    @kotlin.jvm.JvmStatic <methods>;
}
# Keep Parcelize
-keep class * implements android.os.Parcelable {
    public static final ** CREATOR;
}
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# =====================================================
# 16. YCHARTS (Charting Library)
# =====================================================
-keep class co.yml.charts.** { *; }
-dontwarn co.yml.charts.**

# =====================================================
# 17. GOOGLE MAPS
# =====================================================
-keep class com.google.android.gms.maps.** { *; }
-keep class com.google.maps.android.** { *; }

# =====================================================
# 18. DATASTORE
# =====================================================
-keepclassmembers class * extends com.google.protobuf.GeneratedMessageLite { *; }

# =====================================================
# 19. GENERAL ANDROID
# =====================================================
-keep class * extends android.app.Application { *; }
-keep class * extends android.app.Activity { *; }
-keep class * extends android.app.Service { *; }
-keep class * extends android.content.BroadcastReceiver { *; }
-keep class * extends android.content.ContentProvider { *; }

# Keep enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep R classes
-keep class **.R$* { *; }

# =====================================================
# 20. SUPPRESS WARNINGS
# =====================================================
-dontwarn java.lang.invoke.StringConcatFactory
-dontwarn org.bouncycastle.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**

# =====================================================
# 21. BIOMETRIC
# =====================================================
-keep class androidx.biometric.** { *; }
-dontwarn androidx.biometric.**

# =====================================================
# 22. RELEASE SAFETY GUARDS (R8 FALSE-CRASH PREVENTION)
# =====================================================
# Keep app startup/security entry points
-keep class com.example.halalyticscompose.HalalyticsApplication { *; }
-keep class com.example.halalyticscompose.MainActivity { *; }
-keep class com.example.halalyticscompose.utils.CrashReporter { *; }

# Keep Retrofit API interfaces and generic signatures
-keep interface com.example.halalyticscompose.**ApiService { *; }
-keepattributes Signature, RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations

# Keep Room schema details used by generated code
-keep class * extends androidx.room.RoomDatabase { *; }
-keep class androidx.room.RoomDatabase_Impl { *; }

# Keep WorkManager workers and receivers referenced by manifest/runtime
-keep class com.example.halalyticscompose.data.worker.** { *; }
-keep class com.example.halalyticscompose.data.notification.** { *; }
-keep class com.example.halalyticscompose.messaging.** { *; }

# =====================================================
# 22. APPLICATION / STARTUP SAFETY
# =====================================================
-keep class com.example.halalyticscompose.HalalyticsApplication { *; }
-keep class com.example.halalyticscompose.MainActivity { *; }
-keep class com.example.halalyticscompose.data.worker.** { *; }

# =====================================================
# 23. GEMINI / VERTEX AI SAFETY
# =====================================================
-keep class com.google.ai.client.generativeai.** { *; }
-dontwarn com.google.ai.client.generativeai.**
-keep class com.google.firebase.vertexai.** { *; }
-dontwarn com.google.firebase.vertexai.**
