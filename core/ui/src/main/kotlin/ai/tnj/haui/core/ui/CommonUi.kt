package ai.tnj.haui.core.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun BlinkingCursor(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.headlineMedium
) {
    val infiniteTransition = rememberInfiniteTransition(label = "blink")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1000
                1f at 500
                0f at 501
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha"
    )
    Text(
        text = "█",
        color = color.copy(alpha = alpha),
        modifier = modifier.padding(start = 4.dp),
        style = style
    )
}

@Composable
fun PulseDot(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    size: Dp = 6.dp,
    durationMillis: Int = 1000
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis), 
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha",
    )
    Box(
        modifier = modifier
            .size(size)
            .background(color.copy(alpha = alpha), CircleShape)
    )
}

/**
 * Draws a retro-style terminal grid background.
 */
fun Modifier.retroTerminalBackground(
    gridColor: Color,
    gridSize: Dp = 32.dp,
    gridAlpha: Float = 0.05f,
) = drawWithCache {
    val sizePx = gridSize.toPx()
    onDrawBehind {
        // Draw Grid
        var x = 0f
        while (x < size.width) {
            drawLine(
                color = gridColor.copy(alpha = gridAlpha),
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = 1f
            )
            x += sizePx
        }
        var y = 0f
        while (y < size.height) {
            drawLine(
                color = gridColor.copy(alpha = gridAlpha),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1f
            )
            y += sizePx
        }
    }
}

/**
 * Draws L-shaped corner borders around a component.
 */
fun Modifier.terminalCornerBorders(
    color: Color,
    length: Dp = 8.dp,
    thickness: Dp = 2.dp,
    drawTopLeft: Boolean = true,
    drawTopRight: Boolean = true,
    drawBottomLeft: Boolean = true,
    drawBottomRight: Boolean = true
) = drawWithContent {
    drawContent()
    val l = length.toPx()
    val t = thickness.toPx()
    
    if (drawTopLeft) {
        drawLine(color, Offset(0f, 0f), Offset(l, 0f), strokeWidth = t)
        drawLine(color, Offset(0f, 0f), Offset(0f, l), strokeWidth = t)
    }
    if (drawTopRight) {
        drawLine(color, Offset(size.width, 0f), Offset(size.width - l, 0f), strokeWidth = t)
        drawLine(color, Offset(size.width, 0f), Offset(size.width, l), strokeWidth = t)
    }
    if (drawBottomLeft) {
        drawLine(color, Offset(0f, size.height), Offset(l, size.height), strokeWidth = t)
        drawLine(color, Offset(0f, size.height), Offset(0f, size.height - l), strokeWidth = t)
    }
    if (drawBottomRight) {
        drawLine(color, Offset(size.width, size.height), Offset(size.width - l, size.height), strokeWidth = t)
        drawLine(color, Offset(size.width, size.height), Offset(size.width, size.height - l), strokeWidth = t)
    }
}
