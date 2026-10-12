package com.example.ui.components

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AiBubbleColor
import com.example.ui.theme.AiPrimary
import com.example.ui.theme.AiTertiary
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.UserBubbleColor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatMessageItem(
    sender: String,
    text: String,
    imageBase64: String? = null,
    timestamp: Long = System.currentTimeMillis(),
    isError: Boolean = false,
    onSpeak: (String) -> Unit = {},
    onCopy: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isUser = sender == "user"
    val clipboardManager = LocalClipboardManager.current
    val timeFormatted = remember(timestamp) {
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        sdf.format(Date(timestamp))
    }

    val userBitmap = remember(imageBase64) {
        if (!imageBase64.isNullOrEmpty()) {
            try {
                val decoded = Base64.decode(imageBase64, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(decoded, 0, decoded.size)?.asImageBitmap()
            } catch (e: Exception) {
                null
            }
        } else null
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(AiPrimary.copy(alpha = 0.2f))
                    .border(1.dp, AiPrimary.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "OmniAI Icon",
                    tint = AiTertiary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            modifier = Modifier.widthIn(max = 320.dp),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 18.dp,
                            topEnd = 18.dp,
                            bottomStart = if (isUser) 18.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 18.dp
                        )
                    )
                    .background(
                        when {
                            isError -> MaterialTheme.colorScheme.errorContainer
                            isUser -> UserBubbleColor
                            else -> AiBubbleColor
                        }
                    )
                    .border(
                        width = 1.dp,
                        color = if (isUser) Color.Transparent else DarkBorder,
                        shape = RoundedCornerShape(
                            topStart = 18.dp,
                            topEnd = 18.dp,
                            bottomStart = if (isUser) 18.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 18.dp
                        )
                    )
                    .padding(14.dp)
                    .testTag(if (isUser) "user_message_bubble" else "model_message_bubble")
            ) {
                Column {
                    if (userBitmap != null) {
                        Image(
                            bitmap = userBitmap,
                            contentDescription = "Uploaded image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    if (isError) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = "Error",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Something went wrong",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    RenderFormattedAiText(
                        rawText = text,
                        textColor = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Message footer
            Row(
                modifier = Modifier.padding(top = 4.dp, start = 4.dp, end = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = timeFormatted,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontSize = 11.sp
                    )
                )

                if (!isUser && !isError) {
                    IconButton(
                        onClick = { onSpeak(text) },
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("speak_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Read aloud",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(text))
                            onCopy(text)
                        },
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("copy_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy text",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }

        if (isUser) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "User Icon",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun RenderFormattedAiText(
    rawText: String,
    textColor: Color
) {
    // Segment text by code blocks ```code```
    val parts = remember(rawText) {
        splitByCodeBlocks(rawText)
    }

    Column {
        parts.forEach { part ->
            if (part.isCode) {
                CodeBlockView(code = part.text, language = part.language)
            } else {
                Text(
                    text = parseSimpleMarkdown(part.text),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = textColor,
                        lineHeight = 22.sp
                    )
                )
            }
        }
    }
}

@Composable
fun CodeBlockView(code: String, language: String) {
    val clipboardManager = LocalClipboardManager.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF090D16))
            .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF141A29))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = language.ifBlank { "code" },
                style = MaterialTheme.typography.labelSmall.copy(
                    color = AiTertiary,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            )
            IconButton(
                onClick = { clipboardManager.setText(AnnotatedString(code)) },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy code",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(10.dp)
        ) {
            Text(
                text = code,
                style = TextStyleForCode(Color(0xFFE2E8F0))
            )
        }
    }
}

private fun TextStyleForCode(color: Color) = androidx.compose.ui.text.TextStyle(
    fontFamily = FontFamily.Monospace,
    fontSize = 13.sp,
    color = color,
    lineHeight = 18.sp
)

data class TextChunk(
    val text: String,
    val isCode: Boolean,
    val language: String = ""
)

private fun splitByCodeBlocks(text: String): List<TextChunk> {
    val result = mutableListOf<TextChunk>()
    val codeRegex = Regex("```([a-zA-Z0-9]*)\\n?([\\s\\S]*?)```")
    var lastIndex = 0

    codeRegex.findAll(text).forEach { match ->
        val startIndex = match.range.first
        if (startIndex > lastIndex) {
            result.add(TextChunk(text.substring(lastIndex, startIndex), isCode = false))
        }
        val lang = match.groupValues[1].trim()
        val codeContent = match.groupValues[2].trimEnd()
        result.add(TextChunk(codeContent, isCode = true, language = lang))
        lastIndex = match.range.last + 1
    }

    if (lastIndex < text.length) {
        result.add(TextChunk(text.substring(lastIndex), isCode = false))
    }
    return if (result.isEmpty()) listOf(TextChunk(text, isCode = false)) else result
}

private fun parseSimpleMarkdown(markdown: String): AnnotatedString {
    return buildAnnotatedString {
        val lines = markdown.split("\n")
        lines.forEachIndexed { lineIdx, line ->
            // Bullet points
            val trimmed = line.trimStart()
            val isBullet = trimmed.startsWith("* ") || trimmed.startsWith("- ")
            val lineContent = if (isBullet) "•  ${trimmed.substring(2)}" else line

            // Parse bold **text**
            var cursor = 0
            val boldRegex = Regex("\\*\\*(.*?)\\*\\*")
            boldRegex.findAll(lineContent).forEach { match ->
                if (match.range.first > cursor) {
                    append(lineContent.substring(cursor, match.range.first))
                }
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(match.groupValues[1])
                }
                cursor = match.range.last + 1
            }
            if (cursor < lineContent.length) {
                append(lineContent.substring(cursor))
            }

            if (lineIdx < lines.size - 1) {
                append("\n")
            }
        }
    }
}
