package ai.tnj.haui.feature.home.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mikepenz.markdown.coil3.Coil3ImageTransformerImpl
import com.mikepenz.markdown.compose.components.markdownComponents
import com.mikepenz.markdown.compose.elements.MarkdownCodeBlock
import com.mikepenz.markdown.compose.elements.MarkdownCodeFence
import com.mikepenz.markdown.compose.elements.MarkdownTableBasicText
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.model.DefaultMarkdownTypography
import com.mikepenz.markdown.model.MarkdownTypography
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.flavours.gfm.GFMElementTypes
import org.intellij.markdown.flavours.gfm.GFMTokenTypes

@Composable
fun ChatMarkdown(text: String, textColor: Color) {
    val typography = markdownTypography()
    val components = remember(textColor) {
        markdownComponents(
            codeFence = { model ->
                MarkdownCodeFence(model.content, model.node) { code, language, _ ->
                    ChatCodeBlock(code = code, language = language?.trim()?.ifEmpty { null })
                }
            },
            codeBlock = { model ->
                MarkdownCodeBlock(model.content, model.node) { code, _, _ ->
                    ChatCodeBlock(code = code, language = null)
                }
            },
            table = { model ->
                ChatMarkdownTable(content = model.content, node = model.node, textColor = textColor)
            }
        )
    }

    SelectionContainer(modifier = Modifier.fillMaxWidth()) {
        Markdown(
            content = text,
            colors = markdownColor(text = textColor),
            typography = typography,
            modifier = Modifier.fillMaxWidth(),
            imageTransformer = Coil3ImageTransformerImpl,
            components = components,
        )
    }
}

@Composable
private fun ChatMarkdownTable(content: String, node: ASTNode, textColor: Color) {
    val rows = remember(node) {
        node.children.filter {
            it.type == GFMElementTypes.HEADER || it.type == GFMElementTypes.ROW
        }
    }
    if (rows.isEmpty()) return

    val maxCols = remember(rows) {
        rows.maxOf { row ->
            row.children.count { it.type == GFMTokenTypes.CELL }
        }.coerceAtLeast(1)
    }

    val scrollState = rememberScrollState()
    val primaryColor = MaterialTheme.colorScheme.primary
    val dividerColor = primaryColor.copy(alpha = 0.2f)
    val strokePx = with(LocalDensity.current) { 1.dp.toPx() }
    
    val baseBodyStyle = MaterialTheme.typography.bodyMedium
    val bodyStyle = remember(textColor, baseBodyStyle) { 
        baseBodyStyle.copy(color = textColor) 
    }
    val headerStyle = remember(bodyStyle, primaryColor) { 
        bodyStyle.copy(fontWeight = FontWeight.Bold, color = primaryColor) 
    }

    Column(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .horizontalScroll(scrollState)
            .background(MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.3f))
            .border(1.dp, dividerColor)
    ) {
        rows.forEachIndexed { rowIndex, row ->
            val isHeader = row.type == GFMElementTypes.HEADER
            val isLastRow = rowIndex == rows.lastIndex
            val cells = row.children.filter { it.type == GFMTokenTypes.CELL }
            
            Row(
                modifier = Modifier
                    .height(IntrinsicSize.Min)
                    .then(
                        if (isHeader) Modifier.background(primaryColor.copy(alpha = 0.05f))
                        else Modifier
                    )
            ) {
                for (index in 0 until maxCols) {
                    val isLastCol = index == maxCols - 1
                    val cell = cells.getOrNull(index)
                    
                    Box(
                        modifier = Modifier
                            .width(140.dp)
                            .fillMaxHeight()
                            .drawBehind {
                                // Right vertical divider
                                if (!isLastCol) {
                                    drawLine(
                                        color = dividerColor,
                                        start = Offset(size.width, 0f),
                                        end = Offset(size.width, size.height),
                                        strokeWidth = strokePx
                                    )
                                }
                                // Bottom horizontal divider
                                if (!isLastRow) {
                                    drawLine(
                                        color = dividerColor,
                                        start = Offset(0f, size.height),
                                        end = Offset(size.width, size.height),
                                        strokeWidth = strokePx
                                    )
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (cell != null) {
                            MarkdownTableBasicText(
                                content = content,
                                cell = cell,
                                style = if (isHeader) headerStyle else bodyStyle,
                                maxLines = Int.MAX_VALUE,
                                overflow = TextOverflow.Clip,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun markdownTypography(
    h1: TextStyle = headingStyle(1),
    h2: TextStyle = headingStyle(2),
    h3: TextStyle = headingStyle(3),
    h4: TextStyle = headingStyle(4),
    h5: TextStyle = headingStyle(5),
    h6: TextStyle = headingStyle(6),
    text: TextStyle = MaterialTheme.typography.bodyMedium,
    code: TextStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
    inlineCode: TextStyle = text.copy(
        fontFamily = FontFamily.Monospace,
        background = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
        color = MaterialTheme.colorScheme.primary
    ),
    quote: TextStyle = MaterialTheme.typography.bodyMedium.copy(
        fontStyle = FontStyle.Italic,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    ),
    paragraph: TextStyle = MaterialTheme.typography.bodyMedium,
    ordered: TextStyle = MaterialTheme.typography.bodyMedium,
    bullet: TextStyle = MaterialTheme.typography.bodyMedium,
    list: TextStyle = MaterialTheme.typography.bodyMedium,
    textLink: TextLinkStyles = TextLinkStyles(
        style = SpanStyle(
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            textDecoration = TextDecoration.Underline
        )
    ),
    table: TextStyle = text,
): MarkdownTypography = remember(h1, h2, h3, h4, h5, h6, text, code, inlineCode, quote, paragraph) {
    DefaultMarkdownTypography(
        h1 = h1, h2 = h2, h3 = h3, h4 = h4, h5 = h5, h6 = h6,
        text = text, quote = quote, code = code, inlineCode = inlineCode,
        paragraph = paragraph, ordered = ordered, bullet = bullet, list = list,
        textLink = textLink, table = table
    )
}

@Composable
private fun headingStyle(level: Int): TextStyle {
    val base = MaterialTheme.typography.headlineSmall
    return when (level.coerceIn(1, 6)) {
        1 -> MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold)
        2 -> MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
        3 -> MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
        4 -> base.copy(fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        else -> base.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}
