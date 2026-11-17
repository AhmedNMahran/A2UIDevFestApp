package com.example.a2uidevfest.ui

import androidx.a2ui.model.protocol.A2uiException
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.a2ui.A2uiSurface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.a2uidevfest.protocol.DevFestAgentBlueprints
import com.example.a2uidevfest.viewmodel.AgentResponseMode
import com.example.a2uidevfest.viewmodel.ChatMessageItem
import com.example.a2uidevfest.viewmodel.DevFestA2uiViewModel
import com.example.a2uidevfest.viewmodel.ProtocolDirection
import com.example.a2uidevfest.viewmodel.QuickTopicPrompt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevFestMainScreen(
    viewModel: DevFestA2uiViewModel,
    modifier: Modifier = Modifier,
) {
    var showInspectorSheet by remember { mutableStateOf(false) }
    var userInputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Automatically scroll to the latest message when a new bubble is added
    LaunchedEffect(viewModel.chatMessages.size) {
        if (viewModel.chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(viewModel.chatMessages.lastIndex)
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .imePadding(),
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(text = "🔺", style = MaterialTheme.typography.titleMedium)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Beni Suef Explorer AI",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    text = "Egypt Tourism Agent • Text vs. A2UI Compose",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    },
                    actions = {
                        TextButton(
                            onClick = { viewModel.resetConversation() },
                            contentPadding = PaddingValues(horizontal = 8.dp),
                        ) {
                            Text("Reset", style = MaterialTheme.typography.labelMedium)
                        }
                        FilledTonalButton(
                            onClick = { showInspectorSheet = true },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.padding(end = 8.dp),
                        ) {
                            Text(
                                text = "{ } Wire (${viewModel.protocolLogs.size})",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                )

                // Prominent Mode Switcher: "Before A2UI: Plain Text" vs "After A2UI: Compose UI"
                ModeComparisonSwitcherBar(
                    currentMode = viewModel.responseMode,
                    onModeSelected = { viewModel.selectResponseMode(it) },
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
            }
        },
        bottomBar = {
            ChatInputComposerBar(
                inputText = userInputText,
                onInputChanged = { userInputText = it },
                onSendClicked = {
                    if (userInputText.isNotBlank()) {
                        viewModel.sendUserChatMessage(userInputText)
                        userInputText = ""
                    }
                },
                onQuickPromptClicked = { prompt ->
                    viewModel.sendQuickTopic(prompt)
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            items(viewModel.chatMessages, key = { it.id }) { message ->
                if (message.isUser) {
                    UserChatBubble(message = message)
                } else {
                    AgentChatBubble(
                        message = message,
                        viewModel = viewModel,
                    )
                }
            }
        }
    }

    if (showInspectorSheet) {
        ModalBottomSheet(
            onDismissRequest = { showInspectorSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        ) {
            A2uiDeveloperInspectorSheet(
                viewModel = viewModel,
                onClose = { showInspectorSheet = false },
            )
        }
    }
}

@Composable
private fun ModeComparisonSwitcherBar(
    currentMode: AgentResponseMode,
    onModeSelected: (AgentResponseMode) -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AgentResponseMode.entries.forEach { mode ->
                val isSelected = currentMode == mode
                val containerColor = when {
                    isSelected && mode == AgentResponseMode.A2UI_COMPOSE ->
                        MaterialTheme.colorScheme.primary
                    isSelected && mode == AgentResponseMode.PLAIN_TEXT ->
                        MaterialTheme.colorScheme.secondary
                    else -> MaterialTheme.colorScheme.surface
                }
                val contentColor = when {
                    isSelected && mode == AgentResponseMode.A2UI_COMPOSE ->
                        MaterialTheme.colorScheme.onPrimary
                    isSelected && mode == AgentResponseMode.PLAIN_TEXT ->
                        MaterialTheme.colorScheme.onSecondary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onModeSelected(mode) },
                    shape = RoundedCornerShape(12.dp),
                    color = containerColor,
                    border = if (!isSelected) {
                        BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    } else {
                        null
                    },
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = mode.label,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = contentColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = mode.badge,
                            style = MaterialTheme.typography.labelSmall,
                            color = contentColor.copy(alpha = 0.85f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UserChatBubble(message: ChatMessageItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
    ) {
        Surface(
            modifier = Modifier.widthIn(max = 320.dp),
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = 18.dp,
                bottomEnd = 4.dp,
            ),
            color = MaterialTheme.colorScheme.primary,
            shadowElevation = 1.dp,
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AgentChatBubble(
    message: ChatMessageItem,
    viewModel: DevFestA2uiViewModel,
) {
    val isA2uiSurface = message.surfaceId != null

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(
            topStart = 6.dp,
            topEnd = 18.dp,
            bottomStart = 18.dp,
            bottomEnd = 18.dp,
        ),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        border = BorderStroke(
            width = if (isA2uiSurface) 1.5.dp else 1.dp,
            color = if (isA2uiSurface) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
            } else {
                MaterialTheme.colorScheme.outlineVariant
            },
        ),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Agent Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(
                                if (isA2uiSurface) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.secondary
                                }
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = if (isA2uiSurface) "✨" else "💬",
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = message.senderName,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isA2uiSurface) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.secondaryContainer
                    },
                ) {
                    Text(
                        text = if (isA2uiSurface) "A2UI Compose" else "Plain Text",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isA2uiSurface) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSecondaryContainer
                        },
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Agent Intro / Plain Text Content
            Text(
                text = message.text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            // If this is a Plain Text bubble, offer a 1-tap button to upgrade it to A2UI Compose UI!
            if (message.upgradeTopic != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Want interactive native Compose UI instead?",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                            Text(
                                text = "Stream A2UI v0.9 JSONL -> A2uiSurface (Material 3 + BeniSuefPlaceCard)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { viewModel.upgradePromptToA2ui(message.upgradeTopic) },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        ) {
                            Text(
                                text = "✨ Render with A2UI",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }

            // If this message carries an A2UI Surface, render it natively via A2uiSurface!
            if (message.surfaceId != null) {
                Spacer(modifier = Modifier.height(10.dp))

                // Optional Progressive Stream Controls for the Beni Suef Places surface
                if (message.surfaceId == DevFestAgentBlueprints.SURFACE_PLACES) {
                    ProgressiveStreamControlStrip(viewModel = viewModel)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Optional Security Sandbox Attack Controls for the Security surface
                if (message.isSecuritySandbox) {
                    SecuritySandboxControlStrip(viewModel = viewModel)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                LiveChatA2uiSurfaceHost(
                    surfaceId = message.surfaceId,
                    viewModel = viewModel,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProgressiveStreamControlStrip(viewModel: DevFestA2uiViewModel) {
    val totalChunks = DevFestAgentBlueprints.streamingScenarioChunks.size
    val currentChunk = viewModel.streamedChunkCount

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Progressive JSONL Stream ($currentChunk / $totalChunks chunks)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = if (currentChunk < 3) "⏳ Skeleton (isReady=false)" else "✓ Data Bound",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                FilledTonalButton(
                    onClick = { viewModel.autoStreamAllChunks() },
                    enabled = !viewModel.isAutoStreaming,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = if (viewModel.isAutoStreaming) "Streaming JSONL..." else "▶ Replay Live Stream",
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
                OutlinedButton(
                    onClick = { viewModel.resetStreamingScenario(initialChunks = 2, logEntries = true) },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Text("Show Skeleton (Chunk 2)", style = MaterialTheme.typography.labelSmall)
                }
                if (currentChunk < totalChunks) {
                    Button(
                        onClick = { viewModel.streamNextChunk(logEntries = true) },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    ) {
                        Text("Next Chunk (${currentChunk + 1}/$totalChunks)", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SecuritySandboxControlStrip(viewModel: DevFestA2uiViewModel) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Simulate Malformed Agent Payloads",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error,
                )
                FilterChip(
                    selected = viewModel.autoSelfCorrectEnabled,
                    onClick = { viewModel.toggleAutoSelfCorrect() },
                    label = {
                        Text(
                            text = if (viewModel.autoSelfCorrectEnabled) "Self-Heal: ON" else "Self-Heal: OFF",
                            style = MaterialTheme.typography.labelSmall,
                        )
                    },
                )
            }

            viewModel.lastSecurityBanner?.let { banner ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = banner,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                FilledTonalButton(
                    onClick = {
                        viewModel.injectSecurityPayload(
                            label = "Unknown Component ('PyramidHologram3D')",
                            json = DevFestAgentBlueprints.ATTACK_HALLUCINATED_COMPONENT,
                        )
                    },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Text("1. Unknown Component", style = MaterialTheme.typography.labelSmall)
                }
                FilledTonalButton(
                    onClick = {
                        viewModel.injectSecurityPayload(
                            label = "Schema Type Error",
                            json = DevFestAgentBlueprints.ATTACK_SCHEMA_TYPE_MISMATCH,
                        )
                    },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Text("2. Schema Violation", style = MaterialTheme.typography.labelSmall)
                }
                FilledTonalButton(
                    onClick = {
                        viewModel.injectSecurityPayload(
                            label = "Sparse Index [50,000] (Safe)",
                            json = DevFestAgentBlueprints.DEFENSE_SPARSE_ARRAY_SAFE,
                            companionJson = DevFestAgentBlueprints.DEFENSE_SPARSE_ARRAY_SAFE_STATUS,
                        )
                    },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Text("3. Sparse [50,000]", style = MaterialTheme.typography.labelSmall)
                }
                FilledTonalButton(
                    onClick = {
                        viewModel.injectSecurityPayload(
                            label = "Out-of-Bounds Score=999",
                            json = DevFestAgentBlueprints.ATTACK_SEMANTIC_OUT_OF_BOUNDS,
                        )
                    },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Text("4. Score=999 (Runtime Error)", style = MaterialTheme.typography.labelSmall)
                }
                OutlinedButton(
                    onClick = { viewModel.loadSecurityBaseline(logEntries = true) },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Text("Restore Valid Surface", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
private fun LiveChatA2uiSurfaceHost(
    surfaceId: String,
    viewModel: DevFestA2uiViewModel,
) {
    val activeSurfaces by viewModel.processor.activeSurfaces.collectAsState()
    val surfaceState = viewModel.activeSurfaceById(surfaceId, activeSurfaces)

    if (surfaceState == null) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "Initializing A2UI surface '$surfaceId'...",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    } else {
        A2uiSurface(
            surfaceModel = surfaceState,
            modifier = Modifier.fillMaxWidth(),
            loadingContent = {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.5.dp,
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Progressive Rendering: Awaiting /places Data...",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = "BeniSuefPlaceCard.isReady() == false until Chunk 3 arrives.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            },
            errorContent = { exception: A2uiException ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "🛡️ A2UI Runtime Shield Caught ${exception::class.simpleName}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = exception.message ?: "Component failed validation.",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { viewModel.loadSecurityBaseline(logEntries = true) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                            ),
                        ) {
                            Text("Trigger Agent Self-Correction Patch", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            },
        )
    }
}

@Composable
private fun ChatInputComposerBar(
    inputText: String,
    onInputChanged: (String) -> Unit,
    onSendClicked: () -> Unit,
    onQuickPromptClicked: (QuickTopicPrompt) -> Unit,
) {
    Surface(
        tonalElevation = 3.dp,
        shadowElevation = 6.dp,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            // Quick Prompt Suggestion Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                QuickTopicPrompt.entries.forEach { prompt ->
                    AssistChip(
                        onClick = { onQuickPromptClicked(prompt) },
                        label = {
                            Text(
                                text = prompt.chipLabel,
                                style = MaterialTheme.typography.labelMedium,
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                        ),
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Text Input + Send Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = onInputChanged,
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text(
                            text = "Ask about Beni Suef places, Meidum Pyramid, trip plan...",
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onSendClicked,
                    enabled = inputText.isNotBlank(),
                    shape = RoundedCornerShape(24.dp),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
                ) {
                    Text(
                        text = "Send",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

/**
 * Clean BottomSheet for inspecting the live A2UI v0.9 JSONL wire protocol, contextual test results,
 * and auto-generated custom catalog JSON Schema during the DevFest presentation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun A2uiDeveloperInspectorSheet(
    viewModel: DevFestA2uiViewModel,
    onClose: () -> Unit,
) {
    var selectedSheetTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Wire Log (${viewModel.protocolLogs.size})", "Contextual Tests", "Catalog Schema")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 420.dp, max = 640.dp)
            .padding(bottom = 16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = "A2UI Protocol & Testing Inspector",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Server JSONL ⇄ Client Outbound Events • Custom Catalog Schema",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            TextButton(onClick = onClose) {
                Text("Done")
            }
        }

        PrimaryTabRow(selectedTabIndex = selectedSheetTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedSheetTab == index,
                    onClick = { selectedSheetTab = index },
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (selectedSheetTab == index) FontWeight.Bold else FontWeight.Normal,
                        )
                    },
                )
            }
        }

        when (selectedSheetTab) {
            0 -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Live JSONL & Outbound Event Stream",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    TextButton(onClick = { viewModel.clearLogs() }) {
                        Text("Clear Log", style = MaterialTheme.typography.labelSmall)
                    }
                }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(viewModel.protocolLogs, key = { it.id }) { entry ->
                        val badgeColor = when (entry.direction) {
                            ProtocolDirection.SERVER_TO_CLIENT -> Color(0xFF1A73E8)
                            ProtocolDirection.CLIENT_TO_SERVER_EVENT -> Color(0xFF00897B)
                            ProtocolDirection.CLIENT_TO_SERVER_ERROR -> Color(0xFFD93025)
                            ProtocolDirection.AGENT_SELF_CORRECTION -> Color(0xFF8E24AA)
                        }
                        val badgeLabel = when (entry.direction) {
                            ProtocolDirection.SERVER_TO_CLIENT -> "S→C JSONL"
                            ProtocolDirection.CLIENT_TO_SERVER_EVENT -> "C→S EVENT"
                            ProtocolDirection.CLIENT_TO_SERVER_ERROR -> "C→S ERROR"
                            ProtocolDirection.AGENT_SELF_CORRECTION -> "SELF-HEAL"
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = badgeColor.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(4.dp),
                                    ) {
                                        Text(
                                            text = badgeLabel,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = badgeColor,
                                            fontWeight = FontWeight.Bold,
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = entry.summary,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = entry.payload,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = FontFamily.Monospace,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }

            1 -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    viewModel.testResults.forEach { test ->
                        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = if (test.passed) "✓ ${test.name}" else "✗ ${test.name}",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (test.passed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                )
                                Text(
                                    text = test.details,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }

            2 -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(14.dp),
                ) {
                    Text(
                        text = "Auto-Synthesized JSON Schema (beniSuefCatalog.toJsonSchemaString()):",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(10.dp)
                                .verticalScroll(rememberScrollState())
                                .horizontalScroll(rememberScrollState()),
                        ) {
                            Text(
                                text = viewModel.customCatalogJsonSchema.take(5000),
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                            )
                        }
                    }
                }
            }
        }
    }
}
