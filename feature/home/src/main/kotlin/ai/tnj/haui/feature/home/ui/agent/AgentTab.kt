package ai.tnj.haui.feature.home.ui.agent

import ai.tnj.haui.core.ui.BlinkingCursor
import ai.tnj.haui.core.ui.PulseDot
import ai.tnj.haui.core.ui.terminalCornerBorders
import ai.tnj.haui.feature.home.ui.components.PanelHeader
import ai.tnj.huai.feature.home.R
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

@Composable
fun AgentTab(
    modifier: Modifier = Modifier,
    viewModel: AgentViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showConnectSheet by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Re-check server health every time the tab becomes resumed
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.refreshHealth()
    }

    // Close sheet on successful connection
    DisposableEffect(uiState.isConnected) {
        if (uiState.isConnected) showConnectSheet = false
        onDispose {}
    }

    // Show error in snackbar
    DisposableEffect(uiState.connectError) {
        val error = uiState.connectError
        val scope = CoroutineScope(SupervisorJob())
        if (error != null) {
            scope.launch {
                snackbarHostState.showSnackbar(error)
            }
        }
        onDispose { scope.cancel() }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(Modifier.height(24.dp))

                // ── Header Section (Agent Profile) ───────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.2f))
                        .terminalCornerBorders(color = MaterialTheme.colorScheme.primary, length = 8.dp)
                        .padding(12.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "AGENT_PROFILE",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.headlineLarge.copy(
                                shadow = Shadow(
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                    blurRadius = 8f
                                )
                            )
                        )

                        Box(
                            modifier = Modifier
                                .size(160.dp)
                                .border(2.dp, MaterialTheme.colorScheme.primary)
                                .background(MaterialTheme.colorScheme.surfaceContainerLow.copy(0.2f))
                                .padding(4.dp)
                        ) {
                            val backgroundColor = if (uiState.isDarkTheme) {
                                Color.White
                            } else {
                                Color.Transparent
                            }
                            Image(
                                painter = painterResource(R.drawable.ic_hermes_agent),
                                contentDescription = "Hermes Avatar",
                                modifier = Modifier.fillMaxSize().background(backgroundColor),
                                contentScale = ContentScale.Crop
                            )
                            // Status Indicator
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(8.dp)
                            ) {
                                PulseDot(color = MaterialTheme.colorScheme.primary)
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "HERMES",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    letterSpacing = 0.3.em,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            BlinkingCursor(
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }

                Spacer(Modifier.height(4.dp))

                // Panel 1: Server Status
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        .background(MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.9f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .drawBehind {
                                drawLine(
                                    color = Color.Gray.copy(alpha = 0.3f),
                                    start = Offset(0f, size.height),
                                    end = Offset(size.width, size.height),
                                    strokeWidth = 1f,
                                )
                            }
                            .padding(horizontal = 16.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = "[ SERVER_STATUS ]",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelMedium,
                        )
                        if (uiState.isConnected) {
                            Surface(
                                onClick = { showConnectSheet = true },
                                shape = MaterialTheme.shapes.large,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                                border = BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                ),
                            ) {
                                Text(
                                    text = "MODIFY_CONFIG",
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Medium,
                                        letterSpacing = 0.1.em,
                                    ),
                                    modifier = Modifier.padding(
                                        horizontal = 8.dp,
                                        vertical = 4.dp
                                    ),
                                )
                            }
                        }
                    }
                    Column(Modifier.padding(12.dp)) {
                        PanelRow(
                            "ADDRESS:",
                            if (uiState.host.isNotEmpty()) "${uiState.host}:${uiState.port}" else "---",
                            MaterialTheme.colorScheme.secondary
                        )
                        PanelRow(
                            "HEALTH:",
                            uiState.health.toUpperCase(Locale.current),
                            when (uiState.health) {
                                "ok" -> MaterialTheme.colorScheme.primary
                                "Unknown" -> MaterialTheme.colorScheme.secondary
                                else -> MaterialTheme.colorScheme.error
                            }
                        )
                        PanelRow(
                            "UPDATE_AT:",
                            uiState.uptime.ifBlank { "---" },
                            MaterialTheme.colorScheme.secondary,
                            isLast = true,
                        )
                    }
                }

                // Panel 2: Active Sub-Routines
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        .background(MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.9f))
                ) {
                    PanelHeader("[ ACTIVE_SUB-ROUTINES ]")
                    Column(Modifier.padding(12.dp)) {
                        val gatewayColor = when (uiState.gatewayState) {
                            "running" -> MaterialTheme.colorScheme.primary
                            "Unknown" -> MaterialTheme.colorScheme.secondary
                            else -> MaterialTheme.colorScheme.error
                        }
                        PanelRow(
                            "GATEWAY:",
                            uiState.gatewayState.toUpperCase(Locale.current),
                            gatewayColor
                        )
                        PanelRow(
                            "CONNECTION:",
                            uiState.connection.toUpperCase(Locale.current),
                            if (uiState.connection == "connected") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            isLast = true
                        )
                    }
                }


                Spacer(Modifier.weight(1f))

                // ── CONNECT ──────────────────────────────────────────────────────────
                if (!uiState.isConnected) {
                    OutlinedButton(
                        onClick = { showConnectSheet = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.primaryContainer,
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.inversePrimary),
                        shape = MaterialTheme.shapes.large,
                    ) {
                        Text(
                            text = "CONNECT",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                }

                Spacer(Modifier.height(32.dp))
            }
        }

        // ── Connection Bottom Sheet ──────────────────────────────────────────
        if (showConnectSheet) {
            ConnectionSheet(
                isConnecting = uiState.isConnecting,
                connectError = uiState.connectError,
                initialHost = uiState.host,
                initialPort = uiState.port,
                initialApiKey = uiState.apiKey,
                onDismiss = { if (!uiState.isConnecting) showConnectSheet = false },
                onConnect = { host, port, apiKey -> viewModel.connect(host, port, apiKey) },
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
            snackbar = { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        )
    }
}

@Composable
private fun PanelRow(name: String, value: String, valueColor: Color, isLast: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .then(
                if (!isLast) Modifier.drawBehind {
                    drawLine(
                        color = Color.Gray.copy(alpha = 0.3f),
                        start = Offset(0f, size.height + 8.dp.toPx()),
                        end = Offset(size.width, size.height + 8.dp.toPx()),
                        strokeWidth = 1f
                    )
                } else Modifier
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            name,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelSmall
        )
        Text(value, color = valueColor, style = MaterialTheme.typography.labelSmall)
    }
}
