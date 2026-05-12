package ai.tnj.haui.core.utils

import kotlin.math.max
import kotlin.math.roundToInt

data class JpegSizeLimiterResult(
  val bytes: ByteArray,
  val width: Int,
  val height: Int,
  val quality: Int,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as JpegSizeLimiterResult

        if (width != other.width) return false
        if (height != other.height) return false
        if (quality != other.quality) return false
        if (!bytes.contentEquals(other.bytes)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = width
        result = 31 * result + height
        result = 31 * result + quality
        result = 31 * result + bytes.contentHashCode()
        return result
    }
}

object JpegSizeLimiter {
  fun compressToLimit(
    initialWidth: Int,
    initialHeight: Int,
    startQuality: Int,
    maxBytes: Int,
    minQuality: Int = 20,
    minSize: Int = 256,
    scaleStep: Double = 0.8,
    maxScaleAttempts: Int = 4,
    encode: (width: Int, height: Int, quality: Int) -> ByteArray,
  ): JpegSizeLimiterResult {
    require(initialWidth > 0 && initialHeight > 0) { "Invalid image size" }
    require(maxBytes > 0) { "Invalid maxBytes" }

    var width = initialWidth
    var height = initialHeight
    
    var currentBytes = encode(width, height, startQuality)
    var best = JpegSizeLimiterResult(bytes = currentBytes, width = width, height = height, quality = startQuality)
    
    if (currentBytes.size <= maxBytes) return best

    // Step 1: Scale down if way too large (saves more bytes and CPU than quality reduction)
    repeat(maxScaleAttempts) {
        val scale = if (currentBytes.size > maxBytes * 2) scaleStep else 0.9
        val nextWidth = max(minSize, (width * scale).roundToInt())
        val nextHeight = max(minSize, (height * scale).roundToInt())
        
        if (nextWidth == width && nextHeight == height) return@repeat
        
        width = nextWidth
        height = nextHeight
        currentBytes = encode(width, height, startQuality)
        best = JpegSizeLimiterResult(bytes = currentBytes, width = width, height = height, quality = startQuality)
        
        if (currentBytes.size <= maxBytes) return best
    }

    // Step 2: Binary Search for quality (much faster than linear repeat)
    var low = minQuality
    var high = startQuality
    while (low <= high) {
        val mid = (low + high) / 2
        val bytes = encode(width, height, mid)
        val result = JpegSizeLimiterResult(bytes = bytes, width = width, height = height, quality = mid)
        
        if (bytes.size <= maxBytes) {
            best = result
            low = mid + 1 // Try to get better quality
        } else {
            high = mid - 1
        }
    }

    if (best.bytes.size > maxBytes) {
        // Last resort: Minimum quality at minimum size
        val minBytes = encode(minSize, minSize, minQuality)
        if (minBytes.size > maxBytes) {
            throw IllegalStateException("IMAGE_TOO_LARGE: ${minBytes.size} bytes > $maxBytes bytes even at min settings")
        }
        return JpegSizeLimiterResult(minBytes, minSize, minSize, minQuality)
    }

    return best
  }
}
