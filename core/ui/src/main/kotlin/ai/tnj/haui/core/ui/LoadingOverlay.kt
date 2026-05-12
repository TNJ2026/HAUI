package ai.tnj.haui.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput

@Composable
fun LoadingOverlay(
    modifier: Modifier = Modifier,
    background: Color = Color.Black.copy(alpha = 0.8f),
    content: @Composable () -> Unit = {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(background)
            .pointerInput(Unit) {
                // Consume all pointer events to prevent interaction with underlying UI
                // This is a more robust way than empty clickable
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        event.changes.forEach { it.consume() }
                    }
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}
