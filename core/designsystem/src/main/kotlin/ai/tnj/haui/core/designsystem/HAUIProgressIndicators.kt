package ai.tnj.haui.core.designsystem

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A rectangular progress indicator that draws along the perimeter and shows percentage.
 */
@Composable
fun HAUIRectProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    strokeWidth: Dp = 2.dp,
    trackColor: Color = color.copy(alpha = 0.2f)
) {
    val coercedProgress = progress.coerceIn(0f, 1f)
    
    Box(
        modifier = modifier.size(42.dp),
        contentAlignment = Alignment.Center
    ) {
        val pathMeasure = remember { PathMeasure() }
        val path = remember { Path() }
        val segmentPath = remember { Path() }

        Canvas(modifier = Modifier.matchParentSize()) {
            val strokeWidthPx = strokeWidth.toPx()
            val size = this.size
            
            // Draw track rectangle
            drawRect(
                color = trackColor,
                style = Stroke(width = strokeWidthPx)
            )

            // Update path for the full rectangle (starting from top center)
            path.reset()
            path.moveTo(size.width / 2f, 0f)
            path.lineTo(size.width, 0f)
            path.lineTo(size.width, size.height)
            path.lineTo(0f, size.height)
            path.lineTo(0f, 0f)
            path.lineTo(size.width / 2f, 0f)

            pathMeasure.setPath(path, false)
            
            segmentPath.reset()
            pathMeasure.getSegment(0f, pathMeasure.length * coercedProgress, segmentPath, true)
            
            drawPath(
                path = segmentPath,
                color = color,
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Square)
            )
        }
    }
}

/**
 * An indeterminate rectangular progress indicator.
 */
@Composable
fun HAUIRectProgressIndicator(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    strokeWidth: Dp = 2.dp,
    trackColor: Color = color.copy(alpha = 0.1f)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rect_progress")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress"
    )

    HAUIRectProgressIndicator(
        progress = progress,
        modifier = modifier,
        color = color,
        strokeWidth = strokeWidth,
        trackColor = trackColor
    )
}
