package ai.tnj.haui.core.utils

import android.util.Base64
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic

class ChatImageCodecTest : StringSpec({

    "normalizeAttachmentFileName should ensure .jpg extension" {
        normalizeAttachmentFileName("image.png") shouldBe "image.jpg"
        normalizeAttachmentFileName("photo") shouldBe "photo.jpg"
        normalizeAttachmentFileName("archive.tar.gz") shouldBe "archive.tar.jpg"
        normalizeAttachmentFileName("  spaces.txt  ") shouldBe "spaces.jpg"
        normalizeAttachmentFileName("") shouldBe "image.jpg"
        normalizeAttachmentFileName(" . ") shouldBe "image.jpg"
    }

    "computeInSampleSize should return correct power of 2" {
        computeInSampleSize(2000, 1000, 1600) shouldBe 2 // 2000/2 = 1000 <= 1600
        computeInSampleSize(4000, 1000, 1600) shouldBe 4 // 4000/4 = 1000 <= 1600
        computeInSampleSize(1000, 1000, 1600) shouldBe 1
        computeInSampleSize(1600, 1600, 1600) shouldBe 1
        computeInSampleSize(1601, 100, 1600) shouldBe 2
        computeInSampleSize(0, 0, 1600) shouldBe 1
        computeInSampleSize(1000, 1000, 0) shouldBe 1
    }

    "Base64 mocking example" {
        mockkStatic(Base64::class)
        every { Base64.encodeToString(any(), any()) } returns "mocked_base64"
        
        Base64.encodeToString(ByteArray(0), Base64.NO_WRAP) shouldBe "mocked_base64"
        
        unmockkStatic(Base64::class)
    }

    "normalizeAttachmentFileName should handle various extensions" {
        normalizeAttachmentFileName("test.PNG") shouldBe "test.jpg"
        normalizeAttachmentFileName("no-extension") shouldBe "no-extension.jpg"
        normalizeAttachmentFileName("  ") shouldBe "image.jpg"
    }

    "computeInSampleSize should handle edge cases" {
        computeInSampleSize(-10, 1000, 1600) shouldBe 1
        computeInSampleSize(1000, -10, 1600) shouldBe 1
        computeInSampleSize(1000, 1000, -1) shouldBe 1
        // Max sample size is 64 in the current implementation
        computeInSampleSize(200000, 100, 1600) shouldBe 64
    }
})
