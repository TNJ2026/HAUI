# `:core:utils`

> 跨模块共享的纯工具类：Release 安全的日志、聊天附件编解码（图片 / 文档）、JPEG 尺寸—质量约束压缩算法。

---

## 职责

- `LogUtil`：`inline` + lambda，Release 模式零开销。
- 聊天附件载体 `PendingAttachment`（`Image` / `Document`）。
- 图片附件加载、缩放、Base64 编码与解码缓存。
- 文档附件加载，按 MIME 自动选择 UTF-8 文本或 Base64 输出。
- JPEG "先缩尺寸 → 二分搜索质量" 的体积约束压缩器。

## 依赖

- `androidx.core.ktx`、`androidx.appcompat`、`google-material`
- `androidx.security.crypto`
- `kotlinx-serialization-json`、`kotlinx-coroutines-android`
- Compose BOM + `androidx.compose.ui`（用于 Compose 友好的 API，本模块 `buildFeatures.compose = true`）
- `:core:model`
- `buildFeatures.buildConfig = true`：`LogUtil.isDebug` 依赖 `BuildConfig.DEBUG`

## 关键文件

- **`LogUtil.kt`** — `object LogUtil`：`inline` + lambda 形式的 `v/d/i/w/e`，外加 `String` 便捷重载；Debug 才输出。
- **`ChatImageCodec.kt`** — 同时承载 sealed class `PendingAttachment.Image` / `Document`，以及：
  - `loadSizedImageAttachment(resolver, uri)`：URI → 缩放并 JPEG 压缩后的 `PendingAttachment.Image`。
  - `decodeBase64Bitmap`：带 16MB `LruCache` 的解码缓存。
  - `decodeScaledBitmap` / `computeInSampleSize` / `normalizeAttachmentFileName` 等辅助函数。
- **`ChatDocCodec.kt`** — `loadDocumentAttachment(resolver, uri)`：≤ 5MB 限制；`text/*`、`application/json`、`application/xml` → UTF-8 文本；其余 → Base64。
- **`JpegSizeLimiter.kt`** — `object JpegSizeLimiter.compressToLimit(...)`：Step1 按比例缩小尺寸，Step2 在 `[minQuality, startQuality]` 区间内对 JPEG 质量二分搜索；返回 `JpegSizeLimiterResult(bytes, width, height, quality)`。

### 关键常量（在 `ChatImageCodec.kt`）

- `CHAT_ATTACHMENT_MAX_WIDTH = 800`：上传前最大边长。
- `CHAT_ATTACHMENT_MAX_BASE64_CHARS = 300 * 1024`：Base64 字符上限（≈ 225KB 原始字节）。
- `CHAT_ATTACHMENT_START_QUALITY = 75`：JPEG 初始质量。
- `CHAT_DECODE_MAX_DIMENSION = 1600`：解码展示侧最大边长。
- `CHAT_IMAGE_CACHE_BYTES = 16 * 1024 * 1024`：解码 Bitmap 的 LruCache 上限。

## 使用示例

```kotlin
// 1. Release 安全日志：lambda 仅在 BuildConfig.DEBUG 时执行
LogUtil.d("MyTag") { "computed result: ${expensiveCalc()}" }
```

```kotlin
// 2. 图片附件加载（已内部走 JpegSizeLimiter）
val attachment = loadSizedImageAttachment(contentResolver, uri)
// attachment is PendingAttachment.Image(uri, fileName, mimeType, base64=...)
```

```kotlin
// 3. 直接调用 JpegSizeLimiter（调用方负责 encode lambda 与 Bitmap.compress）
val result = JpegSizeLimiter.compressToLimit(
    initialWidth = bitmap.width,
    initialHeight = bitmap.height,
    startQuality = 75,
    maxBytes = 225 * 1024,
) { width, height, quality ->
    val scaled = bitmap.scale(width, height)
    val out = ByteArrayOutputStream()
    scaled.compress(Bitmap.CompressFormat.JPEG, quality, out)
    out.toByteArray()
}
// result.bytes / result.width / result.height / result.quality
```

## 使用约定

- 日志 **永远** 用 `LogUtil`，不用 `android.util.Log`；保持 lambda 形式以确保 Release 零开销。
- 编解码失败抛出 `IllegalStateException`（语义清晰、调用方易区分），不要静默吞掉。
- `loadDocumentAttachment` 超过 5MB 直接抛异常，UI 层需自己做提示。
- 新增工具类时：单一职责、可单测、尽量不依赖 Android Framework。

## 测试

```bash
./gradlew :core:utils:test
```

单测覆盖：

- `ChatImageCodecTest`
- `ChatDocCodecTest`
- `JpegSizeLimiterTest`（包含 "已小于阈值直接返回"、"先缩尺寸再降质量"、"极端最低参数仍超限抛异常" 等场景）
