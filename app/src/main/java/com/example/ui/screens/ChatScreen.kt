package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.api.GeminiClient
import com.example.data.model.AiPersonas
import com.example.ui.components.ChatInputBar
import com.example.ui.components.ChatMessageItem
import com.example.ui.components.PersonaSelector
import com.example.ui.components.TypingIndicator
import com.example.ui.theme.AiBubbleColor
import com.example.ui.theme.AiPrimary
import com.example.ui.theme.AiTertiary
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.viewmodel.ChatViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    val messages by viewModel.messages.collectAsState()
    val streamingBuffer by viewModel.streamingBuffer.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val inputText by viewModel.inputText.collectAsState()
    val selectedImageBase64 by viewModel.selectedImageBase64.collectAsState()
    val selectedPersonaId by viewModel.selectedPersonaId.collectAsState()
    val selectedModel by viewModel.selectedModel.collectAsState()
    val notification by viewModel.statusNotification.collectAsState()

    val currentPersona = remember(selectedPersonaId) {
        AiPersonas.getById(selectedPersonaId)
    }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Scroll to bottom when new messages arrive or while streaming
    LaunchedEffect(messages.size, streamingBuffer) {
        val totalCount = messages.size + (if (streamingBuffer != null) 1 else 0)
        if (totalCount > 0) {
            listState.animateScrollToItem(totalCount - 1)
        }
    }

    LaunchedEffect(notification) {
        notification?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearNotification()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(AiPrimary.copy(alpha = 0.2f))
                                .border(1.dp, AiPrimary.copy(alpha = 0.6f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "OmniAI",
                                tint = AiTertiary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "OmniAI",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                // Active status pulse
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(AiTertiary)
                                )
                            }
                            Text(
                                text = currentPersona.name,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                },
                actions = {
                    // Quick model toggle badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkSurfaceVariant)
                            .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                            .clickable {
                                val nextModel = if (selectedModel == GeminiClient.DEFAULT_MODEL) {
                                    GeminiClient.PRO_MODEL
                                } else {
                                    GeminiClient.DEFAULT_MODEL
                                }
                                viewModel.setModel(nextModel)
                            }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                            .testTag("model_toggle_badge")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (selectedModel == GeminiClient.PRO_MODEL) Icons.Default.Psychology else Icons.Default.Bolt,
                                contentDescription = "Model",
                                tint = if (selectedModel == GeminiClient.PRO_MODEL) AiTertiary else AiPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (selectedModel == GeminiClient.PRO_MODEL) "3.1 Pro" else "3.5 Flash",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }

                    // New Chat button
                    IconButton(
                        onClick = { viewModel.createNewChat() },
                        modifier = Modifier.testTag("new_chat_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Start new chat",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            ChatInputBar(
                text = inputText,
                onTextChange = { viewModel.setInputText(it) },
                selectedImageBase64 = selectedImageBase64,
                onImageSelected = { viewModel.setSelectedImage(it) },
                isGenerating = isGenerating,
                onSend = { viewModel.sendMessage() },
                onStop = { viewModel.stopGeneration() },
                placeholder = "Message OmniAI, ask in Roman Urdu..."
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Persona chips carousel
            PersonaSelector(
                selectedPersonaId = selectedPersonaId,
                onPersonaSelected = { viewModel.setPersona(it.id) }
            )

            if (messages.isEmpty() && streamingBuffer == null) {
                // Empty state with Hero banner and prompt suggestions
                EmptyChatWelcome(
                    persona = currentPersona,
                    onPromptClick = { prompt ->
                        viewModel.sendMessage(explicitPrompt = prompt)
                    }
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .padding(vertical = 4.dp)
                ) {
                    items(messages, key = { it.id }) { msg ->
                        ChatMessageItem(
                            sender = msg.sender,
                            text = msg.text,
                            imageBase64 = msg.imageBase64,
                            timestamp = msg.timestamp,
                            isError = msg.isError,
                            onSpeak = { viewModel.speakText(it) },
                            onCopy = { viewModel.showNotification("Copied to clipboard") }
                        )
                    }

                    // Active streaming buffer card
                    if (streamingBuffer != null) {
                        item(key = "streaming_buffer") {
                            ChatMessageItem(
                                sender = "model",
                                text = streamingBuffer ?: "",
                                isError = false,
                                onSpeak = {},
                                onCopy = {}
                            )
                        }
                    }

                    // Typing dots when request is dispatched before first token
                    if (isGenerating && streamingBuffer.isNullOrEmpty()) {
                        item(key = "typing_indicator") {
                            Box(modifier = Modifier.padding(start = 16.dp, top = 6.dp, bottom = 6.dp)) {
                                TypingIndicator(statusText = "OmniAI is thinking...")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyChatWelcome(
    persona: com.example.data.model.AiPersona,
    onPromptClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            // Hero banner image
            Card(
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .border(1.dp, DarkBorder, RoundedCornerShape(18.dp)),
                colors = CardDefaults.cardColors(containerColor = AiBubbleColor)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ai_hero_banner),
                    contentDescription = "AI Neural Banner",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Welcome to OmniAI",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Multimodal intelligence • Roman Urdu & English • Code & Vision",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Persona description card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(DarkSurfaceVariant)
                    .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = persona.icon,
                        contentDescription = persona.name,
                        tint = AiTertiary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = persona.name,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = persona.subtitle,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Try asking:",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))
        }

        // Suggested prompts
        items(persona.suggestedPrompts) { prompt ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AiBubbleColor)
                    .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                    .clickable { onPromptClick(prompt) }
                    .padding(horizontal = 14.dp, vertical = 12.dp)
                    .testTag("suggested_prompt_item")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = prompt,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 14.sp
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = "Execute prompt",
                        tint = AiPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
