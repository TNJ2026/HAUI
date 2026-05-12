package ai.tnj.haui.ui

import ai.tnj.haui.core.ui.BlinkingCursor
import ai.tnj.haui.core.ui.retroTerminalBackground
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(2_000)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .retroTerminalBackground(
                gridColor = MaterialTheme.colorScheme.primary,
                gridAlpha = 0.08f
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon
            Image(
                painter = painterResource(ai.tnj.haui.R.drawable.ic_launcher),
                contentDescription = "Logo",
                modifier = Modifier.size(80.dp),
                contentScale = ContentScale.Fit,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Title
            Text(
                text = "HERMES AI",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                    letterSpacing = 2.sp,
                    shadow = Shadow(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        blurRadius = 12f
                    )
                ),
                color = MaterialTheme.colorScheme.primary,
            )

            // Subtitle
            Text(
                text = "AUTONOMOUS AGENT CORE",
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 3.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
            )

            Spacer(modifier = Modifier.height(64.dp))

            // Loading Text
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "INITIALIZING_SYSTEM_CORE...",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.primary,
                )
                BlinkingCursor(
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
