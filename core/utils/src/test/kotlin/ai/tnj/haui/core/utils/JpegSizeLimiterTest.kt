package ai.tnj.haui.core.utils

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.ints.shouldBeLessThanOrEqual

class JpegSizeLimiterTest : StringSpec({

    "should return original if size is within limit" {
        val result = JpegSizeLimiter.compressToLimit(
            initialWidth = 1000,
            initialHeight = 1000,
            startQuality = 80,
            maxBytes = 5000,
        ) { w, h, q ->
            ByteArray(1000) // Always 1000 bytes
        }

        result.bytes.size shouldBe 1000
        result.width shouldBe 1000
        result.height shouldBe 1000
        result.quality shouldBe 80
    }

    "should scale down if size is too large" {
        val result = JpegSizeLimiter.compressToLimit(
            initialWidth = 2000,
            initialHeight = 2000,
            startQuality = 80,
            maxBytes = 1000,
            maxScaleAttempts = 10, // 增加尝试次数，确保能缩放到 1000 以下
        ) { w, h, q ->
            // 模拟体积随面积线性变化
            ByteArray((w * h / 1000))
        }

        result.bytes.size shouldBeLessThanOrEqual 1000
        // 验证确实进行了缩放（宽度小于初始的 2000）
        result.width shouldBeLessThanOrEqual 1000
    }

    "should reduce quality if scaling is not enough" {
        val result = JpegSizeLimiter.compressToLimit(
            initialWidth = 500,
            initialHeight = 500,
            startQuality = 100,
            maxBytes = 500,
            minSize = 500, // 禁止缩放
        ) { w, h, q ->
            // 模拟体积随质量线性变化：q=100 -> 1000字节, q=50 -> 500字节
            ByteArray(q * 10)
        }

        result.bytes.size shouldBeLessThanOrEqual 500
        result.quality shouldBe 50
    }

    "should use binary search for quality" {
        var callCount = 0
        JpegSizeLimiter.compressToLimit(
            initialWidth = 500,
            initialHeight = 500,
            startQuality = 100,
            maxBytes = 500,
            minSize = 500, // Disable scaling
        ) { w, h, q ->
            callCount++
            if (q > 50) ByteArray(1000) else ByteArray(100)
        }

        // Binary search log2(100-20) approx 6-7 calls
        callCount shouldBeLessThanOrEqual 10 
    }

    "should throw exception if even min settings are too large" {
        shouldThrow<IllegalStateException> {
            JpegSizeLimiter.compressToLimit(
                initialWidth = 1000,
                initialHeight = 1000,
                startQuality = 80,
                maxBytes = 50,
            ) { w, h, q ->
                ByteArray(100) // Always 100 bytes, which is > 50
            }
        }
    }
    
    "should handle invalid inputs" {
        shouldThrow<IllegalArgumentException> {
            JpegSizeLimiter.compressToLimit(0, 1000, 80, 1000) { _, _, _ -> ByteArray(0) }
        }
        shouldThrow<IllegalArgumentException> {
            JpegSizeLimiter.compressToLimit(1000, 1000, 80, 0) { _, _, _ -> ByteArray(0) }
        }
    }
})
