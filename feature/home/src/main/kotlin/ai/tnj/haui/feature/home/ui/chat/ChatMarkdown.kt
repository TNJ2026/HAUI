package ai.tnj.haui.feature.home.ui.chat

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
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
import com.mikepenz.markdown.annotator.annotatorSettings
import com.mikepenz.markdown.annotator.buildMarkdownAnnotatedString
import com.mikepenz.markdown.compose.components.markdownComponents
import com.mikepenz.markdown.compose.elements.MarkdownCodeBlock
import com.mikepenz.markdown.compose.elements.MarkdownCodeFence
import com.mikepenz.markdown.compose.elements.material.MarkdownBasicText
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.model.DefaultMarkdownTypography
import com.mikepenz.markdown.model.MarkdownTypography
import com.mikepenz.markdown.model.NoOpImageTransformerImpl
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.flavours.gfm.GFMElementTypes
import org.intellij.markdown.flavours.gfm.GFMTokenTypes

@Composable
fun ChatMarkdown(text: String, textColor: Color, isStreaming: Boolean = false) {
    val typography = markdownTypography()
    val colors = markdownColor(text = textColor)

    // 流式期间 targetState 稳定为 true，AnimatedContent 不会触发动画，
    // 只在 isStreaming 翻转到 false 的瞬间做一次过渡：
    // 旧的极简 monospace 视图淡出 + 新的完整渲染淡入 (250ms)，
    // 同时容器高度通过 SizeTransform 平滑变形 (300ms)，
    // 避免最终渲染(表格/代码块)尺寸不一致导致的硬切+顶撑下方气泡。
    AnimatedContent(
        targetState = isStreaming,
        transitionSpec = {
            (fadeIn(animationSpec = tween(durationMillis = 250)) togetherWith
                fadeOut(animationSpec = tween(durationMillis = 250)))
                .using(SizeTransform(clip = false) { _, _ -> tween(durationMillis = 300) })
        },
        label = "ChatMarkdownMode",
        modifier = Modifier.fillMaxWidth(),
    ) { streaming ->
        if (streaming) {
            // 极简组件：表格/代码块/图片全部跳过昂贵渲染，原样输出 markdown 源到
            // monospace 文本。视觉上像终端打字机回显，契合 retro CRT 美学。
            val streamingComponents = remember(textColor) {
                markdownComponents(
                    codeFence = { model ->
                        StreamingRawBlock(model.content, model.node, textColor)
                    },
                    codeBlock = { model ->
                        StreamingRawBlock(model.content, model.node, textColor)
                    },
                    table = { model ->
                        StreamingRawBlock(model.content, model.node, textColor)
                    },
                )
            }
            Markdown(
                content = text,
                colors = colors,
                typography = typography,
                modifier = Modifier.fillMaxWidth(),
                imageTransformer = StreamingNoOpImageTransformer,
                components = streamingComponents,
            )
        } else {
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
                    colors = colors,
                    typography = typography,
                    modifier = Modifier.fillMaxWidth(),
                    imageTransformer = FixedHeightMarkdownImageTransformer,
                    components = components,
                )
            }
        }
    }
}

private val StreamingNoOpImageTransformer = NoOpImageTransformerImpl()

@Composable
private fun StreamingRawBlock(content: String, node: ASTNode, textColor: Color) {
    val raw = remember(content, node) {
        content.substring(node.startOffset, node.endOffset)
    }
    BasicText(
        text = raw,
        style = MaterialTheme.typography.bodySmall.copy(
            fontFamily = FontFamily.Monospace,
            color = textColor,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    )
}

@Composable
private fun ChatMarkdownTable(content: String, node: ASTNode, textColor: Color) {
    // 流式拼接时外层 Markdown 每个分片重新解析整段，ChatMarkdownTable 拿到的
    // ASTNode 都是新实例。但只要 markdown 源里表格区段不变（追加发生在表格之后），
    // 用表格子串当 signature，下游 remember 全部命中，跳过最贵的 AnnotatedString 构建。
    val tableSignature = remember(content, node) {
        content.substring(node.startOffset, node.endOffset)
    }

    val tableData = remember(tableSignature) {
        val parsedRows = node.children
            .filter { it.type == GFMElementTypes.HEADER || it.type == GFMElementTypes.ROW }
            .map { row ->
                TableRowData(
                    cells = row.children.filter { it.type == GFMTokenTypes.CELL },
                    isHeader = row.type == GFMElementTypes.HEADER,
                )
            }
        val cols = parsedRows.maxOfOrNull { it.cells.size }?.coerceAtLeast(1) ?: 1
        TableData(rows = parsedRows, maxCols = cols)
    }
    val rows = tableData.rows
    if (rows.isEmpty()) return
    val maxCols = tableData.maxCols

    val scrollState = rememberScrollState()
    val primaryColor = MaterialTheme.colorScheme.primary
    val dividerColor = remember(primaryColor) { primaryColor.copy(alpha = 0.2f) }
    val headerBg = remember(primaryColor) { primaryColor.copy(alpha = 0.05f) }
    val density = LocalDensity.current
    val strokePx = remember(density) { with(density) { 1.dp.toPx() } }
    val cellWidthPx = remember(density) { with(density) { CellWidth.toPx() } }
    val tableWidth = remember(maxCols) { CellWidth * maxCols }

    val baseBodyStyle = MaterialTheme.typography.bodyMedium
    val bodyStyle = remember(textColor, baseBodyStyle) {
        baseBodyStyle.copy(color = textColor)
    }
    val headerStyle = remember(bodyStyle, primaryColor) {
        bodyStyle.copy(fontWeight = FontWeight.Bold, color = primaryColor)
    }

    // annotatorSettings() 每次重组返回新实例（含临时 listener lambda），
    // 但它只在缓存 miss 时被消费；命中时复用旧的 AnnotatedString，不会触发重建。
    val annotator = annotatorSettings()
    val annotatedCells: List<List<AnnotatedString>> =
        remember(tableSignature, bodyStyle, headerStyle) {
            rows.map { row ->
                val style = if (row.isHeader) headerStyle else bodyStyle
                row.cells.map { cell ->
                    content.buildMarkdownAnnotatedString(
                        textNode = cell,
                        style = style,
                        annotatorSettings = annotator,
                    )
                }
            }
        }

    Column(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .horizontalScroll(scrollState)
            .width(tableWidth)
            .background(MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.3f))
            .border(1.dp, dividerColor)
    ) {
        rows.forEachIndexed { rowIndex, rowData ->
            val isLastRow = rowIndex == rows.lastIndex
            val rowAnnotated = annotatedCells[rowIndex]
            val rowModifier = Modifier
                .then(
                    if (rowData.isHeader) Modifier.background(headerBg) else Modifier
                )
                .drawBehind {
                    for (col in 1 until maxCols) {
                        val x = cellWidthPx * col
                        drawLine(
                            color = dividerColor,
                            start = Offset(x, 0f),
                            end = Offset(x, size.height),
                            strokeWidth = strokePx,
                        )
                    }
                    if (!isLastRow) {
                        drawLine(
                            color = dividerColor,
                            start = Offset(0f, size.height),
                            end = Offset(size.width, size.height),
                            strokeWidth = strokePx,
                        )
                    }
                }

            Row(modifier = rowModifier) {
                for (index in 0 until maxCols) {
                    val cellText = rowAnnotated.getOrNull(index)
                    Box(
                        modifier = Modifier
                            .width(CellWidth)
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        if (cellText != null) {
                            MarkdownBasicText(
                                text = cellText,
                                style = if (rowData.isHeader) headerStyle else bodyStyle,
                                maxLines = 10,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }
    }
}

private val CellWidth = 140.dp

private data class TableRowData(val cells: List<ASTNode>, val isHeader: Boolean)
private data class TableData(val rows: List<TableRowData>, val maxCols: Int)

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
