package ai.tnj.haui.feature.home.ui.agent

import ai.tnj.haui.core.ui.LoadingOverlay
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionSheet(
    isConnecting: Boolean,
    connectError: String?,
    initialHost: String,
    initialPort: String,
    initialApiKey: String,
    onDismiss: () -> Unit,
    onConnect: (host: String, port: String, apiKey: String) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var host by remember { mutableStateOf(initialHost) }
    var port by remember { mutableStateOf(initialPort.ifBlank { "8642" }) }
    var apiKey by remember { mutableStateOf(initialApiKey) }
    val ipv4Regex =
        remember { Regex("""^((25[0-5]|2[0-4]\d|[01]?\d\d?)\.){3}(25[0-5]|2[0-4]\d|[01]?\d\d?)$""") }
    val canConnect = ipv4Regex.matches(host) && port.toIntOrNull()?.let { it in 1..65535 } == true

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        scrimColor = Color.Black.copy(alpha = 0.8f),
        shape = MaterialTheme.shapes.large,
        tonalElevation = 0.dp,
        dragHandle = {
            val primaryColor = MaterialTheme.colorScheme.primary
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top accent line
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(primaryColor.copy(alpha = 0.3f))
                )

                // Handle
                Box(
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .width(48.dp)
                        .height(6.dp)
                        .background(primaryColor.copy(alpha = 0.2f), MaterialTheme.shapes.large)
                )

                // Header — "[ CONNECTION_SETTINGS ]" with blueprint glow
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 48.dp)
                        .padding(top = 24.dp, bottom = 16.dp)
                ) {
                    Text(
                        text = "[ CONNECTION_SETTINGS ]",
                        color = primaryColor,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.1.em,
                            shadow = Shadow(
                                color = primaryColor.copy(alpha = 0.2f),
                                blurRadius = 12f
                            )
                        )
                    )
                }
                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.primary.copy(0.1f)
                )
            }
        },
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding(),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Scrollable Form Fields
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 48.dp)
                        .padding(top = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    // Host (full width)
                    ConnectField(
                        label = "Host",
                        value = host,
                        placeholder = "127.0.0.1",
                        onValueChange = { host = it.filter { c -> c.isDigit() || c == '.' } },
                    )

                    // Port
                    ConnectField(
                        label = "Port",
                        value = port,
                        placeholder = "8642",
                        keyboardType = KeyboardType.Number,
                        onValueChange = { port = it.filter(Char::isDigit) },
                    )

                    // API Key
                    ConnectField(
                        label = "API Key",
                        value = apiKey,
                        placeholder = "••••••••",
                        isOptional = true,
                        isPassword = false,
                        onValueChange = { apiKey = it }
                    )
                }

                // Fixed Bottom Action Area (Always above keyboard)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 48.dp)
                        .padding(top = 24.dp, bottom = 48.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = { onConnect(host, port, apiKey) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.primary,
                            disabledContainerColor = Color.Transparent,
                            disabledContentColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.inversePrimary),
                        shape = RectangleShape,
                        enabled = canConnect
                    ) {
                        Text(
                            text = "CONNECT",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.2.em
                            )
                        )
                    }

                    if (connectError != null) {
                        Text(
                            text = "> ERROR: $connectError",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace
                            )
                        )
                    }
                }
            }
            if (isConnecting) LoadingOverlay()
        }
    }
}

@Composable
private fun ConnectField(
    label: String,
    value: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    isOptional: Boolean = false,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    onValueChange: (String) -> Unit,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label.toUpperCase(Locale.current),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.1.em
                ),
            )
            if (isOptional) {
                Text(
                    text = " (OPTIONAL)",
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                )
            }
        }

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            leadingIcon = {
                Text(
                    text = ">",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp)
                )
            },
            placeholder = {
                Text(
                    text = placeholder,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                )
            },
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.primary,
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                cursorColor = MaterialTheme.colorScheme.primary,
            ),
            shape = RectangleShape,
        )
    }
}
