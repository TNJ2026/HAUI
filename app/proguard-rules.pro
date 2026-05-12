# ─── Global / System ─────────────────────────────────────────────────────────

-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod
-keepattributes SourceFile,LineNumberTable

# ─── Project Models (JSON Serialization) ────────────────────────────────────

# We must keep all serializable models to prevent JSON mapping failures.
-keep @kotlinx.serialization.Serializable class ai.tnj.haui.core.model.** { *; }
-keepclassmembers class ai.tnj.haui.core.model.** {
    *** Companion;
    *** $serializer;
}

# ─── Kotlin Serialization ───────────────────────────────────────────────────

-dontnote kotlinx.serialization.AnnotationsKt
-keep,allowobfuscation,allowshrinking class kotlinx.serialization.json.** { *; }
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}

# ─── Retrofit ───────────────────────────────────────────────────────────────

-keepattributes RuntimeVisibleAlphaAnnotations, RuntimeVisibleParameterAnnotations
-keepclassmembers,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn retrofit2.Platform$Java8

# ─── OkHttp ────────────────────────────────────────────────────────────────

-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# ─── Hilt / Dagger ─────────────────────────────────────────────────────────

-keep class dagger.hilt.android.internal.** { *; }
-keep class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# ─── Room ──────────────────────────────────────────────────────────────────

-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# ─── Coil ──────────────────────────────────────────────────────────────────

-keep class coil.** { *; }
-dontwarn coil.util.CoilUtils

# ─── Markdown Renderer (mikepenz) ──────────────────────────────────────────

-keep class com.mikepenz.markdown.** { *; }
-dontwarn com.mikepenz.markdown.**

# ─── Bouncy Castle ─────────────────────────────────────────────────────────

-keep class org.bouncycastle.** { *; }
-dontwarn org.bouncycastle.**

# ─── DNS Java ──────────────────────────────────────────────────────────────

-keep class org.xbill.DNS.** { *; }
-dontwarn org.xbill.DNS.**

# ─── ML Kit / Play Services ────────────────────────────────────────────────

-keep class com.google.android.gms.internal.** { *; }
-dontwarn com.google.android.gms.**
