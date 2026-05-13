# ──────────────────────────────────────────────────────────────────────────
# HAUI · ProGuard / R8 规则
#
# 规则按"库分组"组织。每组都说明：
#   - 这是什么库 / 为什么需要规则
#   - 不加规则的具体后果
#
# 调试技巧：
#   - 删除 R8 规则 → ./gradlew :app:assembleRelease → 看崩溃栈
#   - 反混淆栈：${BUILD_TOOLS}/retrace.jar mapping.txt stacktrace.txt
# ──────────────────────────────────────────────────────────────────────────

# ─── Global / System ──────────────────────────────────────────────────────
# 反射、泛型、内部类、行号信息。kotlinx-serialization 与 Retrofit 都依赖这些。
# 不保留 SourceFile/LineNumberTable，崩溃栈会丢失文件名 + 行号，定位困难。

-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod
-keepattributes SourceFile,LineNumberTable

# ─── Project Models（JSON 序列化） ─────────────────────────────────────────
# 所有 @Serializable 模型 + 它们的 Companion 与 $serializer。
# 不加：kotlinx-serialization 反射查找 $serializer 失败，抛
# `SerializationException: Serializer for class 'XXX' is not found`。

-keep @kotlinx.serialization.Serializable class ai.tnj.haui.core.model.** { *; }
-keepclassmembers class ai.tnj.haui.core.model.** {
    *** Companion;
    *** $serializer;
}

# ─── Kotlin Serialization 运行时 ───────────────────────────────────────────
# Json 子类与 @SerialName 注解字段。
# 不加：自定义字段名（snake_case）无法映射，反序列化得到空对象。

-dontnote kotlinx.serialization.AnnotationsKt
-keep,allowobfuscation,allowshrinking class kotlinx.serialization.json.** { *; }
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}

# ─── Retrofit ─────────────────────────────────────────────────────────────
# 注解信息 + 接口方法签名。
# 不加：Retrofit 在运行时通过反射解析 @GET / @POST / @Path 等注解失败，
# `IllegalArgumentException: Method must be a Retrofit annotation`。

-keepattributes RuntimeVisibleAlphaAnnotations, RuntimeVisibleParameterAnnotations
-keepclassmembers,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn retrofit2.Platform$Java8

# ─── OkHttp ───────────────────────────────────────────────────────────────
# 平台特定类的警告（Android 上不存在 desktop JDK 的 TLS 后端）。
# 不加：编译期 warning 噪音，运行不影响。

-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# ─── Hilt / Dagger ────────────────────────────────────────────────────────
# Hilt 在运行时通过反射构造 ViewModel 与生成的 Module。
# 不加：`MissingBindingException` 或 ViewModel 注入失败崩溃。

-keep class dagger.hilt.android.internal.** { *; }
-keep class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# ─── Room ─────────────────────────────────────────────────────────────────
# RoomDatabase 子类不能混淆（Room 编译期生成 _Impl 类需要原名）。
# Paging 集成相关警告（项目未使用但依赖里有）。

-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# ─── Coil ─────────────────────────────────────────────────────────────────
# 图片加载器扩展通过反射注册。
# 不加：自定义 Decoder / Fetcher 可能无法工作。

-keep class coil.** { *; }
-dontwarn coil.util.CoilUtils

# ─── Markdown Renderer (mikepenz) ─────────────────────────────────────────
# Composable 函数 + 内部 AST 节点类型。
# 不加：表格 / 代码块的特定节点类型被混淆，渲染异常。

-keep class com.mikepenz.markdown.** { *; }
-dontwarn com.mikepenz.markdown.**

# ─── Bouncy Castle ────────────────────────────────────────────────────────
# OkHttp / DataStore-EncryptedSharedPreferences 的传递依赖。
# 项目本身不直接用，但需要消除警告且不引入运行时漏失。

-keep class org.bouncycastle.** { *; }
-dontwarn org.bouncycastle.**

# ─── DNS Java ─────────────────────────────────────────────────────────────
# OkHttp 的 DNS 解析在某些环境下会引入此依赖。
# 不加：edge case 下 DNS 解析失败崩溃。

-keep class org.xbill.DNS.** { *; }
-dontwarn org.xbill.DNS.**

# ─── ML Kit / Play Services ───────────────────────────────────────────────
# 项目目前不直接使用 ML Kit，但 Coil / Camera 的部分传递依赖可能引入。
# 保守起见保留警告抑制。如果未来彻底无依赖可移除。

-keep class com.google.android.gms.internal.** { *; }
-dontwarn com.google.android.gms.**
