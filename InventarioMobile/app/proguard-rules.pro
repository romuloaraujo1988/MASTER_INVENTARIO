# ==============================================
# SIHCP Mobile — Regras de ProGuard/R8
# correcoes-seguranca Req 4.3
# ==============================================

# -------- Kotlin --------
-keep class kotlin.Metadata { *; }
-keepclassmembers class kotlin.Metadata { *; }
-dontwarn kotlin.**

# -------- Coroutines --------
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** { volatile <fields>; }

# -------- Hilt / Dagger --------
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }
-keepclasseswithmembers class * {
    @dagger.hilt.InstallIn <methods>;
}
-keep @dagger.hilt.android.AndroidEntryPoint class *
-keep @dagger.hilt.android.HiltAndroidApp class *

# -------- Room --------
-keep class androidx.room.** { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }
-keep @androidx.room.Database class * { *; }
-keep class * extends androidx.room.RoomDatabase { *; }

# -------- Retrofit / OkHttp --------
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

# -------- Gson (se usado) --------
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapter
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# -------- SQLCipher --------
-keep class net.sqlcipher.** { *; }
-keep class net.sqlcipher.database.** { *; }
-dontwarn net.sqlcipher.**

# -------- Security Crypto (EncryptedSharedPreferences) --------
-keep class androidx.security.crypto.** { *; }

# -------- SLF4J (usado transitoriamente por bibliotecas como iText) --------
# StaticLoggerBinder pertence ao SLF4J 1.x e não existe em Android — é opcional.
-dontwarn org.slf4j.impl.StaticLoggerBinder
-dontwarn org.slf4j.**

# -------- DTOs e modelos de dados (preservar nomes para JSON) --------
-keep class com.inventario.mobile.data.remote.dto.** { *; }
-keep class com.inventario.mobile.data.local.entity.** { *; }
-keep class com.inventario.mobile.domain.model.** { *; }
-keepclassmembers class com.inventario.mobile.data.remote.dto.** { <fields>; }

# -------- WorkManager --------
-keep class androidx.work.** { *; }
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.CoroutineWorker

# -------- Paging 3 --------
-keep class androidx.paging.** { *; }

# -------- Parcelize --------
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator CREATOR;
}

# -------- ZXing (QR Code) --------
-keep class com.google.zxing.** { *; }
-dontwarn com.google.zxing.**

# -------- iText 7 (PDF) --------
-keep class com.itextpdf.** { *; }
-dontwarn com.itextpdf.**

# -------- Glide --------
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule

# -------- Depuração (remover em builds finais) --------
# Remover logs Log.d e Log.v em release
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
}
