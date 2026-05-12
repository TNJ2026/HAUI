package ai.tnj.haui.feature.home.ui.chat

import ai.tnj.haui.core.designsystem.HAUIRectProgressIndicator
import ai.tnj.haui.core.utils.PendingAttachment
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage

@Composable
fun ChatComposer(
    healthOk: Boolean,
    hasPendingRun: Boolean,
    attachments: List<PendingAttachment>,
    contextProgress: Float,
    showAttachmentButton: Boolean,
    onPickImages: () -> Unit,
    onPickDocuments: () -> Unit,
    onRemoveAttachment: (id: String) -> Unit,
    onSend: (text: String) -> Unit,
) {
    var input by rememberSaveable { mutableStateOf("") }
    var showAttachMenu by remember { mutableStateOf(false) }
    val canSend = healthOk && !hasPendingRun && (input.trim().isNotEmpty() || attachments.isNotEmpty())

    Column(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background)) {
        if (attachments.isNotEmpty()) {
            AttachmentsStrip(attachments, onRemoveAttachment)
        }
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .border(1.dp, MaterialTheme.colorScheme.inversePrimary),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Attachment Button
            if (showAttachmentButton && attachments.isEmpty() && input.isEmpty()) {
                Box {
                    Button(
                        onClick = { showAttachMenu = true },
                        modifier = Modifier.height(56.dp).width(48.dp),
                        shape = RectangleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.inversePrimary,
                        ),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AttachFile,
                            contentDescription = "Attach",
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    
                    AttachmentDropdown(
                        expanded = showAttachMenu,
                        onDismiss = { showAttachMenu = false },
                        onPickImages = onPickImages,
                        onPickDocuments = onPickDocuments
                    )
                }
            }

            // Input Field
            TextField(
                value = input,
                modifier = Modifier.weight(1f),
                onValueChange = { input = it },
                placeholder = { 
                    Text(
                        text = "Type a message…", 
                        style = mobileBodyStyle(), 
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) 
                    ) 
                },
                minLines = 1,
                maxLines = 5,
                textStyle = mobileBodyStyle(),
                colors = chatTextFieldColors(),
            )

            // Send Button with Progress
            SendButton(
                canSend = canSend,
                isPending = hasPendingRun,
                progress = contextProgress,
                onSend = {
                    val text = input
                    input = ""
                    onSend(text)
                }
            )
        }
    }
}

@Composable
private fun AttachmentDropdown(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onPickImages: () -> Unit,
    onPickDocuments: () -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = Modifier
            .border(1.dp, MaterialTheme.colorScheme.inversePrimary)
            .background(MaterialTheme.colorScheme.background)
            .width(180.dp),
    ) {
        DropdownMenuItem(
            leadingIcon = { Icon(Icons.Outlined.Image, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            text = { Text("IMAGES", style = MaterialTheme.typography.labelMedium) },
            onClick = {
                onDismiss()
                onPickImages()
            },
        )
        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.inversePrimary.copy(alpha = 0.2f))
        DropdownMenuItem(
            leadingIcon = { Icon(Icons.Outlined.Description, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            text = { Text("DOCUMENTS", style = MaterialTheme.typography.labelMedium) },
            onClick = {
                onDismiss()
                onPickDocuments()
            },
        )
    }
}

@Composable
private fun SendButton(
    canSend: Boolean,
    isPending: Boolean,
    progress: Float,
    onSend: () -> Unit
) {
    Box(modifier = Modifier.size(56.dp).padding(6.dp), contentAlignment = Alignment.Center) {
        Button(
            onClick = onSend,
            enabled = canSend,
            modifier = Modifier.size(40.dp),
            shape = MaterialTheme.shapes.small,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                disabledContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                disabledContentColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f),
            ),
            contentPadding = PaddingValues(0.dp)
        ) {
            if (isPending) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    modifier = Modifier.size(18.dp),
                )
            }
        }
        
        HAUIRectProgressIndicator(
            progress = progress.coerceIn(0f, 1f),
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 2.dp,
            modifier = Modifier.matchParentSize()
        )
    }
}

@Composable
private fun AttachmentsStrip(
    attachments: List<PendingAttachment>,
    onRemoveAttachment: (id: String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.8f))
            .border(width = 1.dp, color = MaterialTheme.colorScheme.inversePrimary)
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        attachments.forEach { att ->
            AttachmentChip(
                attachment = att,
                onRemove = { onRemoveAttachment(att.id) }
            )
        }
    }
}

@Composable
private fun AttachmentChip(
    attachment: PendingAttachment,
    onRemove: () -> Unit
) {
    Box(modifier = Modifier.size(width = if (attachment is PendingAttachment.Image) 80.dp else 160.dp, height = 80.dp)) {
        Surface(
            modifier = Modifier.align(Alignment.Center),
            shape = MaterialTheme.shapes.small,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            color = MaterialTheme.colorScheme.surfaceContainer
        ) {
            when (attachment) {
                is PendingAttachment.Image -> {
                    AsyncImage(
                        model = attachment.uri,
                        contentScale = ContentScale.Crop,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp)
                    )
                }
                is PendingAttachment.Document -> {
                    Column(modifier = Modifier.size(140.dp, 64.dp).padding(8.dp)) {
                        Text(
                            text = attachment.fileName,
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.weight(1f))
                        Icon(
                            imageVector = Icons.Outlined.Description,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
        
        // Remove Button
        Surface(
            onClick = onRemove,
            modifier = Modifier.size(20.dp).align(Alignment.TopEnd),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove",
                modifier = Modifier.padding(2.dp)
            )
        }
    }
}

private val RectangleShape = androidx.compose.ui.graphics.RectangleShape

@Composable
private fun chatTextFieldColors() = TextFieldDefaults.colors(
    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent,
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent,
    cursorColor = MaterialTheme.colorScheme.primary,
)

@Composable
private fun mobileBodyStyle() = MaterialTheme.typography.bodyMedium.copy(
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp,
    lineHeight = 22.sp,
)
