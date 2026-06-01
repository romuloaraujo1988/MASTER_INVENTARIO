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
# MD-02 (correcoes-seguranca — maio/2026): regras atualizadas para a
# biblioteca `net.zetetic:sqlcipher-android:4.15.0`, que usa o pacote
# `net.zetetic.database.sqlcipher.*` em vez do legado `net.sqlcipher.*`.
-keep class net.zetetic.database.** { *; }
-keep class net.zetetic.database.sqlcipher.** { *; }
-dontwarn net.zetetic.database.**
# Mantidas temporariamente para compatibilidade com dependências antigas
# que ainda podem referenciar o pacote legado (nada mais no código vivo
# o usa após a migração).
-dontwarn net.sqlcipher.**

# -------- Security Crypto (EncryptedSharedPreferences) --------
-keep class androidx.security.crypto.** { *; }

# -------- Google Tink (dependência transitiva de security-crypto) --------
# MD-01 (correcoes-seguranca — maio/2026): security-crypto usa Tink via
# reflection para inicializar o MasterKey. Sem estas regras, o R8 remove
# classes essenciais e a inicialização de EncryptedSharedPreferences quebra
# em release com ClassNotFoundException.
-keep class com.google.crypto.tink.** { *; }
-keep interface com.google.crypto.tink.** { *; }
-keepclassmembers class com.google.crypto.tink.** { *; }
-dontwarn com.google.crypto.tink.**
# Dependências de Tink (protobuf e errorprone annotations)
-dontwarn com.google.api.client.**
-dontwarn com.google.errorprone.annotations.**
-dontwarn com.google.j2objc.annotations.**
-dontwarn org.checkerframework.**
-dontwarn javax.annotation.**

# -------- SLF4J (usado transitoriamente por bibliotecas como iText) --------
# StaticLoggerBinder pertence ao SLF4J 1.x e não existe em Android — é opcional.
-dontwarn org.slf4j.impl.StaticLoggerBinder
-dontwarn org.slf4j.**

# -------- DTOs e modelos de dados (preservar nomes para JSON) --------
# Regra geral: todas as classes em pacotes que contêm DTOs/modelos usados
# pelo Gson/Retrofit devem ser preservadas. Sem isso, o R8 ofusca os nomes
# dos campos e o Gson falha ao mapear chaves JSON como `salaNome`,
# `responsavelNome`, `content`, `totalElements`, etc.
#
# A regra `-keepclassmembers class * { @SerializedName <fields>; }`
# preserva APENAS campos anotados — classes sem @SerializedName (muitas
# como ApiResponse, PagedResponse, MobileColetaBatchRequest, InventarioDTO)
# ficariam quebradas. Por isso mantemos esses pacotes inteiros.
-keep class com.inventario.mobile.data.remote.dto.** { *; }
-keep class com.inventario.mobile.data.remote.api.** { *; }
-keep class com.inventario.mobile.data.model.** { *; }
-keep class com.inventario.mobile.data.local.entity.** { *; }
-keep class com.inventario.mobile.data.realtime.** { *; }
-keep class com.inventario.mobile.domain.model.** { *; }
-keep class com.inventario.mobile.api.** { *; }
-keepclassmembers class com.inventario.mobile.data.remote.dto.** { <fields>; }
-keepclassmembers class com.inventario.mobile.data.remote.api.** { <fields>; }
-keepclassmembers class com.inventario.mobile.data.model.** { <fields>; }
-keepclassmembers class com.inventario.mobile.data.realtime.** { <fields>; }
-keepclassmembers class com.inventario.mobile.api.** { <fields>; }

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
