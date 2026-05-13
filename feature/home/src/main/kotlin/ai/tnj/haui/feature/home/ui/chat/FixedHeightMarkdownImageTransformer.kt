package ai.tnj.haui.feature.home.ui.chat

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import coil3.compose.LocalPlatformContext
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import com.mikepenz.markdown.model.ImageData
import com.mikepenz.markdown.model.ImageTransformer

// 流式渲染下，AsyncImagePainter 初始 intrinsicSize = Unspecified，
// 解码完才回填真实尺寸；用默认 transformer 会导致气泡先 0 高度、加载后突然撑开。
// 这里给所有 Markdown 图片固定 240.dp 高度做提前占位，避免页面抖动。
private val MarkdownImageHeight = 240.dp

object FixedHeightMarkdownImageTransformer : ImageTransformer {

    @Composable
    override fun transform(link: String): ImageData {
        val painter = rememberAsyncImagePainter(
            model = ImageRequest.Builder(LocalPlatformContext.current)
                .data(link)
                .size(coil3.size.Size.ORIGINAL)
                .build()
        )
        return ImageData(
            painter = painter,
            modifier = Modifier
                .fillMaxWidth()
                .height(MarkdownImageHeight),
            contentScale = ContentScale.Fit,
            alignment = Alignment.CenterStart,
        )
    }

    @Composable
    override fun intrinsicSize(painter: Painter): Size {
        val px = with(LocalDensity.current) { MarkdownImageHeight.toPx() }
        return Size(px, px)
    }
}
