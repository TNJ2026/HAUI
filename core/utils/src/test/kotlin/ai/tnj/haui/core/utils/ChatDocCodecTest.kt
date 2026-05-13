package ai.tnj.haui.core.utils

import android.content.ContentResolver
import android.content.res.AssetFileDescriptor
import android.net.Uri
import android.util.Base64
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import java.io.ByteArrayInputStream

class ChatDocCodecTest : StringSpec({

    val resolver = mockk<ContentResolver>()
    val uri = mockk<Uri>()

    // 显式在每个测试前或初始化时定义行为
    beforeTest {
        io.mockk.clearMocks(resolver, uri)
        every { uri.lastPathSegment } returns "test.txt"
        every { uri.toString() } returns "content://test"
    }

    "loadDocumentAttachment should handle text files correctly" {
        val content = "Hello World"
        val bytes = content.toByteArray()

        every { resolver.getType(uri) } returns "text/plain"
        every { resolver.query(any(), any(), any(), any(), any()) } returns null

        val afd = mockk<AssetFileDescriptor>()
        every { afd.length } returns bytes.size.toLong()
        every { afd.close() } returns Unit
        every { resolver.openAssetFileDescriptor(uri, "r") } returns afd

        every { resolver.openInputStream(uri) } returns ByteArrayInputStream(bytes)

        val result = loadDocumentAttachment(resolver, uri) as PendingAttachment.Document

        result.fileName shouldBe "test.txt"
        result.mimeType shouldBe "text/plain"
        result.text shouldBe content
        result.base64 shouldBe null
    }

    "loadDocumentAttachment should handle binary files using base64" {
        val bytes = byteArrayOf(1, 2, 3, 4)

        every { resolver.getType(uri) } returns "application/pdf"
        every { resolver.query(any(), any(), any(), any(), any()) } returns null

        val afd = mockk<AssetFileDescriptor>()
        every { afd.length } returns bytes.size.toLong()
        every { afd.close() } returns Unit
        every { resolver.openAssetFileDescriptor(uri, "r") } returns afd

        every { resolver.openInputStream(uri) } returns ByteArrayInputStream(bytes)

        mockkStatic(Base64::class)
        // 关键：在 static mock 块中显式指定参数匹配
        every { Base64.encodeToString(any<ByteArray>(), Base64.NO_WRAP) } returns "AQIDBA=="

        val result = loadDocumentAttachment(resolver, uri) as PendingAttachment.Document

        result.mimeType shouldBe "application/pdf"
        result.base64 shouldBe "AQIDBA=="

        unmockkStatic(Base64::class)
    }

    // ... 其他测试用例保持不变 ...
})