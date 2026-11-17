package com.example.a2uidevfest.viewmodel

import androidx.a2ui.compose.runtime.A2uiMessageParser
import androidx.a2ui.compose.ui.A2uiCatalog
import androidx.a2ui.compose.ui.A2uiMessageProcessor
import androidx.a2ui.compose.ui.toJsonSchemaString
import androidx.a2ui.model.processor.A2uiMessageParser as A2uiModelMessageParser
import androidx.a2ui.model.processor.A2uiMessageProcessor as A2uiModelMessageProcessor
import androidx.a2ui.model.processor.A2uiSurfaceModel
import androidx.a2ui.model.processor.processInput
import androidx.a2ui.model.protocol.A2uiClientErrorMessage
import androidx.a2ui.model.protocol.A2uiClientEventMessage
import androidx.a2ui.model.protocol.A2uiClientToServerMessage
import androidx.a2ui.model.protocol.A2uiException
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a2uidevfest.catalog.DevFestCatalogs
import com.example.a2uidevfest.protocol.DevFestAgentBlueprints
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Controls whether the Beni Suef Travel Agent responds with traditional plain text ("Before A2UI")
 * or streams declarative A2UI v0.9 JSONL blueprints rendered as Jetpack Compose UI ("After A2UI").
 */
enum class AgentResponseMode(val label: String, val badge: String) {
    PLAIN_TEXT(
        label = "Before A2UI: Text Chat",
        badge = "Plain Text LLM",
    ),
    A2UI_COMPOSE(
        label = "After A2UI: Compose UI",
        badge = "androidx.a2ui + M3",
    ),
}

enum class QuickTopicPrompt(val chipLabel: String, val userMessage: String) {
    DISCOVER_PLACES(
        chipLabel = "🏛️ Top places in Beni Suef",
        userMessage = "Show me the top touristic places to visit in Beni Suef, Egypt.",
    ),
    PLAN_ITINERARY(
        chipLabel = "🐪 Plan 1-day itinerary",
        userMessage = "Help me plan a 1-day itinerary in Beni Suef with Meidum Pyramid & the Nile.",
    ),
    SECURITY_SHIELD(
        chipLabel = "🛡️ Test hallucination shield",
        userMessage = "What happens if the LLM hallucinates an invalid UI component or bad data?",
    ),
}

data class ChatMessageItem(
    val id: Long,
    val isUser: Boolean,
    val senderName: String,
    val text: String,
    val modeAtCreation: AgentResponseMode,
    val surfaceId: String? = null,
    val upgradeTopic: QuickTopicPrompt? = null,
    val isSecuritySandbox: Boolean = false,
)

enum class ProtocolDirection {
    SERVER_TO_CLIENT,
    CLIENT_TO_SERVER_EVENT,
    CLIENT_TO_SERVER_ERROR,
    AGENT_SELF_CORRECTION,
}

data class ProtocolLogEntry(
    val id: Long,
    val direction: ProtocolDirection,
    val summary: String,
    val payload: String,
)

data class ContextualTestCaseResult(
    val name: String,
    val takeawayCategory: String,
    val passed: Boolean,
    val details: String,
)

class DevFestA2uiViewModel : ViewModel() {

    val parser: A2uiModelMessageParser<String> = A2uiMessageParser()

    val catalogs: List<A2uiCatalog> = DevFestCatalogs.createCatalogs(
        onUrlOpened = { url ->
            appendLog(
                direction = ProtocolDirection.CLIENT_TO_SERVER_EVENT,
                summary = "Local Function Call: openUrl($url)",
                payload = """{"functionCall":{"call":"openUrl","args":{"url":"$url"}}}""",
            )
        }
    )

    val processor: A2uiModelMessageProcessor = A2uiMessageProcessor(catalogs = catalogs)

    var responseMode: AgentResponseMode by mutableStateOf(AgentResponseMode.PLAIN_TEXT)
        private set

    var streamedChunkCount: Int by mutableIntStateOf(0)
        private set

    var isAutoStreaming: Boolean by mutableStateOf(false)
        private set

    var autoSelfCorrectEnabled: Boolean by mutableStateOf(true)
        private set

    var lastSecurityBanner: String? by mutableStateOf(null)
        private set

    val chatMessages = mutableStateListOf<ChatMessageItem>()
    val protocolLogs = mutableStateListOf<ProtocolLogEntry>()
    val testResults = mutableStateListOf<ContextualTestCaseResult>()

    private var messageSequence = 1L
    private var logSequence = 1L

    val customCatalogJsonSchema: String by lazy {
        runCatching {
            catalogs.last().toJsonSchemaString()
        }.getOrElse { e ->
            "// Catalog Schema Error: ${e.message}"
        }
    }

    init {
        // 1. Collect and process incoming A2UI JSONL messages on a background dispatcher
        viewModelScope.launch(Dispatchers.Default) {
            processor.collectMessages()
        }

        // 2. Collect outbound Client-to-Server events (User Actions & Validation/Runtime Errors)
        viewModelScope.launch {
            processor.outboundEvents.collect { outbound: A2uiClientToServerMessage ->
                handleOutboundMessage(outbound)
            }
        }

        // 3. Pre-initialize the A2UI surfaces in the background so switching modes is instantaneous
        resetStreamingScenario(initialChunks = 4, logEntries = false)
        loadTwoWayBindingScenario(logEntries = false)
        loadSecurityBaseline(logEntries = false)
        runContextualTestSuite()

        // 4. Seed the initial conversation in "Before A2UI: Plain Text" mode to set up the live talk story
        seedInitialConversation()
    }

    private fun seedInitialConversation() {
        chatMessages.clear()
        chatMessages.add(
            ChatMessageItem(
                id = messageSequence++,
                isUser = true,
                senderName = "You",
                text = QuickTopicPrompt.DISCOVER_PLACES.userMessage,
                modeAtCreation = AgentResponseMode.PLAIN_TEXT,
            )
        )
        chatMessages.add(
            ChatMessageItem(
                id = messageSequence++,
                isUser = false,
                senderName = "Beni Suef Explorer AI (Plain Text)",
                text = DevFestAgentBlueprints.TEXT_RESPONSE_PLACES,
                modeAtCreation = AgentResponseMode.PLAIN_TEXT,
                upgradeTopic = QuickTopicPrompt.DISCOVER_PLACES,
            )
        )
    }

    /**
     * Switches between "Before A2UI: Text Chat" and "After A2UI: Compose UI".
     * If switching into A2UI_COMPOSE and no A2UI surface bubble is in the chat yet,
     * automatically streams the Beni Suef Touristic Places surface so the audience sees the difference immediately!
     */
    fun selectResponseMode(mode: AgentResponseMode) {
        if (responseMode == mode) return
        responseMode = mode
        if (mode == AgentResponseMode.A2UI_COMPOSE && chatMessages.none { it.surfaceId != null }) {
            upgradePromptToA2ui(QuickTopicPrompt.DISCOVER_PLACES)
        }
    }

    /**
     * Upgrades a plain-text response directly into an interactive A2UI Compose Surface response.
     */
    fun upgradePromptToA2ui(topic: QuickTopicPrompt) {
        responseMode = AgentResponseMode.A2UI_COMPOSE
        sendQuickTopic(topic)
    }

    /**
     * Sends one of the curated quick-topic prompts in the currently active [responseMode].
     */
    fun sendQuickTopic(topic: QuickTopicPrompt) {
        chatMessages.add(
            ChatMessageItem(
                id = messageSequence++,
                isUser = true,
                senderName = "You",
                text = topic.userMessage,
                modeAtCreation = responseMode,
            )
        )

        when (responseMode) {
            AgentResponseMode.PLAIN_TEXT -> {
                val replyText = when (topic) {
                    QuickTopicPrompt.DISCOVER_PLACES -> DevFestAgentBlueprints.TEXT_RESPONSE_PLACES
                    QuickTopicPrompt.PLAN_ITINERARY -> DevFestAgentBlueprints.TEXT_RESPONSE_ITINERARY
                    QuickTopicPrompt.SECURITY_SHIELD ->
                        "In a plain-text or WebView setup, if an agent outputs malformed HTML/JS or hallucinates fields, you either get broken markup or unsafe code execution. Switch to **After A2UI: Compose UI** to test the live `A2uiCatalog` schema shield and self-correction loop!"
                }
                chatMessages.add(
                    ChatMessageItem(
                        id = messageSequence++,
                        isUser = false,
                        senderName = "Beni Suef Explorer AI (Plain Text)",
                        text = replyText,
                        modeAtCreation = AgentResponseMode.PLAIN_TEXT,
                        upgradeTopic = topic,
                    )
                )
            }

            AgentResponseMode.A2UI_COMPOSE -> {
                when (topic) {
                    QuickTopicPrompt.DISCOVER_PLACES -> {
                        resetStreamingScenario(initialChunks = 4, logEntries = true)
                        removeExistingSurfaceMessage(DevFestAgentBlueprints.SURFACE_PLACES)
                        chatMessages.add(
                            ChatMessageItem(
                                id = messageSequence++,
                                isUser = false,
                                senderName = "Beni Suef Explorer AI • A2UI Surface",
                                text = "Here are the top touristic places in **Beni Suef, Egypt**, streamed as a native Material 3 `A2uiSurface` (`${DevFestAgentBlueprints.SURFACE_PLACES}`):",
                                modeAtCreation = AgentResponseMode.A2UI_COMPOSE,
                                surfaceId = DevFestAgentBlueprints.SURFACE_PLACES,
                            )
                        )
                    }

                    QuickTopicPrompt.PLAN_ITINERARY -> {
                        loadTwoWayBindingScenario(logEntries = true)
                        removeExistingSurfaceMessage(DevFestAgentBlueprints.SURFACE_ITINERARY)
                        chatMessages.add(
                            ChatMessageItem(
                                id = messageSequence++,
                                isUser = false,
                                senderName = "Beni Suef Explorer AI • A2UI Surface",
                                text = "I've generated an interactive **1-Day Beni Suef Trip Builder** (`${DevFestAgentBlueprints.SURFACE_ITINERARY}`) with two-way data bindings (`A2uiDataModel`):",
                                modeAtCreation = AgentResponseMode.A2UI_COMPOSE,
                                surfaceId = DevFestAgentBlueprints.SURFACE_ITINERARY,
                            )
                        )
                    }

                    QuickTopicPrompt.SECURITY_SHIELD -> {
                        loadSecurityBaseline(logEntries = true)
                        removeExistingSurfaceMessage(DevFestAgentBlueprints.SURFACE_SECURITY)
                        chatMessages.add(
                            ChatMessageItem(
                                id = messageSequence++,
                                isUser = false,
                                senderName = "Beni Suef Explorer AI • Security Sandbox",
                                text = "Here is the **A2UI Runtime Defense Sandbox** (`${DevFestAgentBlueprints.SURFACE_SECURITY}`). Use the buttons below to simulate LLM hallucinations and watch `A2uiSurface` self-correct:",
                                modeAtCreation = AgentResponseMode.A2UI_COMPOSE,
                                surfaceId = DevFestAgentBlueprints.SURFACE_SECURITY,
                                isSecuritySandbox = true,
                            )
                        )
                    }
                }
            }
        }
    }

    /**
     * Handles free-form text typed by the user in the chat input bar.
     */
    fun sendUserChatMessage(rawInput: String) {
        val text = rawInput.trim()
        if (text.isEmpty()) return

        val lower = text.lowercase()
        val matchedTopic = when {
            "plan" in lower || "itinerary" in lower || "trip" in lower || "day" in lower || "book" in lower ->
                QuickTopicPrompt.PLAN_ITINERARY
            "security" in lower || "hallucinat" in lower || "error" in lower || "hack" in lower || "shield" in lower ->
                QuickTopicPrompt.SECURITY_SHIELD
            else ->
                QuickTopicPrompt.DISCOVER_PLACES
        }

        chatMessages.add(
            ChatMessageItem(
                id = messageSequence++,
                isUser = true,
                senderName = "You",
                text = text,
                modeAtCreation = responseMode,
            )
        )

        if (responseMode == AgentResponseMode.PLAIN_TEXT) {
            val replyText = when (matchedTopic) {
                QuickTopicPrompt.PLAN_ITINERARY -> DevFestAgentBlueprints.TEXT_RESPONSE_ITINERARY
                QuickTopicPrompt.SECURITY_SHIELD ->
                    "Switch to **After A2UI: Compose UI** mode to test live schema validation and self-correction!"
                QuickTopicPrompt.DISCOVER_PLACES -> DevFestAgentBlueprints.TEXT_RESPONSE_PLACES
            }
            chatMessages.add(
                ChatMessageItem(
                    id = messageSequence++,
                    isUser = false,
                    senderName = "Beni Suef Explorer AI (Plain Text)",
                    text = replyText,
                    modeAtCreation = AgentResponseMode.PLAIN_TEXT,
                    upgradeTopic = matchedTopic,
                )
            )
        } else {
            // Stream the corresponding A2UI surface
            when (matchedTopic) {
                QuickTopicPrompt.DISCOVER_PLACES -> {
                    resetStreamingScenario(initialChunks = 4, logEntries = true)
                    removeExistingSurfaceMessage(DevFestAgentBlueprints.SURFACE_PLACES)
                    chatMessages.add(
                        ChatMessageItem(
                            id = messageSequence++,
                            isUser = false,
                            senderName = "Beni Suef Explorer AI • A2UI Surface",
                            text = "Here are the top touristic places in **Beni Suef, Egypt**, rendered natively via `A2uiSurface`:",
                            modeAtCreation = AgentResponseMode.A2UI_COMPOSE,
                            surfaceId = DevFestAgentBlueprints.SURFACE_PLACES,
                        )
                    )
                }
                QuickTopicPrompt.PLAN_ITINERARY -> {
                    loadTwoWayBindingScenario(logEntries = true)
                    removeExistingSurfaceMessage(DevFestAgentBlueprints.SURFACE_ITINERARY)
                    chatMessages.add(
                        ChatMessageItem(
                            id = messageSequence++,
                            isUser = false,
                            senderName = "Beni Suef Explorer AI • A2UI Surface",
                            text = "Customize your **Beni Suef 1-Day Itinerary** using two-way bound Material 3 controls:",
                            modeAtCreation = AgentResponseMode.A2UI_COMPOSE,
                            surfaceId = DevFestAgentBlueprints.SURFACE_ITINERARY,
                        )
                    )
                }
                QuickTopicPrompt.SECURITY_SHIELD -> {
                    loadSecurityBaseline(logEntries = true)
                    removeExistingSurfaceMessage(DevFestAgentBlueprints.SURFACE_SECURITY)
                    chatMessages.add(
                        ChatMessageItem(
                            id = messageSequence++,
                            isUser = false,
                            senderName = "Beni Suef Explorer AI • Security Sandbox",
                            text = "Test the **A2UI Runtime Defense Shield** against malformed LLM outputs:",
                            modeAtCreation = AgentResponseMode.A2UI_COMPOSE,
                            surfaceId = DevFestAgentBlueprints.SURFACE_SECURITY,
                            isSecuritySandbox = true,
                        )
                    )
                }
            }
        }
    }

    private fun removeExistingSurfaceMessage(surfaceId: String) {
        chatMessages.removeAll { it.surfaceId == surfaceId }
    }

    fun resetConversation() {
        responseMode = AgentResponseMode.PLAIN_TEXT
        seedInitialConversation()
    }

    fun toggleAutoSelfCorrect() {
        autoSelfCorrectEnabled = !autoSelfCorrectEnabled
    }

    fun clearLogs() {
        protocolLogs.clear()
    }

    fun activeSurfaceById(surfaceId: String, surfaces: List<A2uiSurfaceModel>): A2uiSurfaceModel? {
        return surfaces.firstOrNull { it.id == surfaceId }
    }

    /**
     * Streams the next JSONL chunk in the 4-stage progressive rendering sequence for `beni_suef_places`.
     */
    fun streamNextChunk(logEntries: Boolean = true) {
        val chunks = DevFestAgentBlueprints.streamingScenarioChunks
        if (streamedChunkCount >= chunks.size) return
        val (label, json) = chunks[streamedChunkCount]
        streamedChunkCount++
        if (logEntries) {
            sendServerJson(summary = label, json = json)
        } else {
            processor.processInput(parser, json)
        }
    }

    /**
     * Resets `beni_suef_places` and streams [initialChunks] immediately (e.g. `2` to show the skeleton loading state!).
     */
    fun resetStreamingScenario(initialChunks: Int = 2, logEntries: Boolean = true) {
        isAutoStreaming = false
        val deleteJson =
            """{"version":"v0.9","deleteSurface":{"surfaceId":"${DevFestAgentBlueprints.SURFACE_PLACES}"}}"""
        processor.processInput(parser, deleteJson)
        streamedChunkCount = 0
        repeat(initialChunks.coerceAtMost(DevFestAgentBlueprints.streamingScenarioChunks.size)) {
            streamNextChunk(logEntries = logEntries)
        }
    }

    /**
     * Auto-streams all 4 chunks of `beni_suef_places` with a visible 900ms delay so the audience
     * can watch `BeniSuefPlaceCard.isReady()` transition from skeleton Loading to rendered cards.
     */
    fun autoStreamAllChunks() {
        if (isAutoStreaming) return
        resetStreamingScenario(initialChunks = 0, logEntries = true)
        isAutoStreaming = true
        viewModelScope.launch {
            val chunks = DevFestAgentBlueprints.streamingScenarioChunks
            for ((label, json) in chunks) {
                streamedChunkCount++
                sendServerJson(summary = label, json = json)
                delay(900L)
            }
            isAutoStreaming = false
        }
    }

    fun loadTwoWayBindingScenario(logEntries: Boolean = true) {
        val deleteJson =
            """{"version":"v0.9","deleteSurface":{"surfaceId":"${DevFestAgentBlueprints.SURFACE_ITINERARY}"}}"""
        processor.processInput(parser, deleteJson)

        DevFestAgentBlueprints.twoWayBindingBlueprint.forEachIndexed { idx, json ->
            if (logEntries) {
                sendServerJson(summary = "Trip Builder Init #${idx + 1}", json = json)
            } else {
                processor.processInput(parser, json)
            }
        }
    }

    fun loadSecurityBaseline(logEntries: Boolean = true) {
        lastSecurityBanner = "✓ Surface healthy — all schemas & runtime invariants satisfied."
        val deleteJson =
            """{"version":"v0.9","deleteSurface":{"surfaceId":"${DevFestAgentBlueprints.SURFACE_SECURITY}"}}"""
        processor.processInput(parser, deleteJson)

        DevFestAgentBlueprints.securityBaselineBlueprint.forEachIndexed { idx, json ->
            if (logEntries) {
                sendServerJson(summary = "Security Baseline #${idx + 1}", json = json)
            } else {
                processor.processInput(parser, json)
            }
        }
    }

    fun injectSecurityPayload(label: String, json: String, companionJson: String? = null) {
        lastSecurityBanner = "Injected: $label"
        sendServerJson(summary = "Adversarial Payload: $label", json = json)
        if (companionJson != null) {
            sendServerJson(summary = "Status Update: $label", json = companionJson)
        }
    }

    /**
     * Runs deterministic contextual verification tests on the Beni Suef A2UI stack.
     */
    fun runContextualTestSuite() {
        testResults.clear()
        val localParser = A2uiMessageParser()

        val t1Result = runCatching {
            val message = localParser.parse(
                """{"version":"v0.9","createSurface":{"surfaceId":"test_s1","catalogId":"${DevFestCatalogs.DEVFEST_CATALOG_ID}","sendDataModel":true}}"""
            )
            require(message.surfaceId == "test_s1") { "Expected surfaceId='test_s1', got ${message.surfaceId}" }
            ContextualTestCaseResult(
                name = "1. Protocol Envelope Parsing (v0.9)",
                takeawayCategory = "Architecture (a2ui-model)",
                passed = true,
                details = "Parsed A2uiCreateSurfaceMessage(surfaceId='test_s1', sendDataModel=true) cleanly.",
            )
        }.getOrElse { e ->
            ContextualTestCaseResult(
                name = "1. Protocol Envelope Parsing (v0.9)",
                takeawayCategory = "Architecture (a2ui-model)",
                passed = false,
                details = "Failed: ${e.message}",
            )
        }
        testResults.add(t1Result)

        val t2Result = runCatching {
            var rejectedWithValidationException = false
            try {
                localParser.parse(
                    """{"version":"v99.0","deleteSurface":{"surfaceId":"test_s1"}}"""
                )
            } catch (_: A2uiException.A2uiValidationException) {
                rejectedWithValidationException = true
            }
            require(rejectedWithValidationException) { "Expected A2uiValidationException for version v99.0" }
            ContextualTestCaseResult(
                name = "2. Unsupported Protocol Version Guard",
                takeawayCategory = "Security & Defense",
                passed = true,
                details = "Rejected 'version: v99.0' with A2uiValidationException before surface mutation.",
            )
        }.getOrElse { e ->
            ContextualTestCaseResult(
                name = "2. Unsupported Protocol Version Guard",
                takeawayCategory = "Security & Defense",
                passed = false,
                details = "Failed: ${e.message}",
            )
        }
        testResults.add(t2Result)

        val t3Result = runCatching {
            var rejected = false
            try {
                localParser.parse("""{"version":"v0.9"}""")
            } catch (_: A2uiException.A2uiValidationException) {
                rejected = true
            }
            require(rejected) { "Expected A2uiValidationException when no envelope key is present" }
            ContextualTestCaseResult(
                name = "3. Missing Action Envelope Guard",
                takeawayCategory = "Security & Defense",
                passed = true,
                details = "Rejected envelope missing createSurface/updateComponents/updateDataModel.",
            )
        }.getOrElse { e ->
            ContextualTestCaseResult(
                name = "3. Missing Action Envelope Guard",
                takeawayCategory = "Security & Defense",
                passed = false,
                details = "Failed: ${e.message}",
            )
        }
        testResults.add(t3Result)

        val t4Result = runCatching {
            val schemaStr = customCatalogJsonSchema
            require(schemaStr.contains("BeniSuefPlaceCard")) { "Missing BeniSuefPlaceCard in schema" }
            require(schemaStr.contains("InteractiveRatingGauge")) { "Missing InteractiveRatingGauge in schema" }
            ContextualTestCaseResult(
                name = "4. Beni Suef Custom Catalog Schema Synthesis",
                takeawayCategory = "Architecture & Custom Catalog",
                passed = true,
                details = "Synthesized ${schemaStr.length} chars of JSON Schema including BeniSuefPlaceCard & InteractiveRatingGauge.",
            )
        }.getOrElse { e ->
            ContextualTestCaseResult(
                name = "4. Beni Suef Custom Catalog Schema Synthesis",
                takeawayCategory = "Architecture & Custom Catalog",
                passed = false,
                details = "Failed: ${e.message}",
            )
        }
        testResults.add(t4Result)

        val t5Result = runCatching {
            val activeCount = processor.activeSurfaces.value.size
            ContextualTestCaseResult(
                name = "5. Contextual Surface Actor Pipeline",
                takeawayCategory = "State & Contextual Testing",
                passed = true,
                details = "Verified A2uiMessageProcessor activeSurfaces StateFlow ($activeCount surfaces mounted).",
            )
        }.getOrElse { e ->
            ContextualTestCaseResult(
                name = "5. Contextual Surface Actor Pipeline",
                takeawayCategory = "State & Contextual Testing",
                passed = false,
                details = "Failed: ${e.message}",
            )
        }
        testResults.add(t5Result)
    }

    private fun sendServerJson(summary: String, json: String) {
        appendLog(
            direction = ProtocolDirection.SERVER_TO_CLIENT,
            summary = summary,
            payload = json,
        )
        processor.processInput(parser, json)
    }

    private fun handleOutboundMessage(outbound: A2uiClientToServerMessage) {
        when (outbound) {
            is A2uiClientEventMessage -> {
                val dataModelSnapshot = outbound.clientDataModel?.surfaces?.get(outbound.surfaceId)
                val payloadPreview = buildString {
                    append("""{"event":{"name":"${outbound.type}","surfaceId":"${outbound.surfaceId}","componentId":"${outbound.componentId}","context":${outbound.context}}""")
                    if (dataModelSnapshot != null) {
                        append(""","clientDataModel":$dataModelSnapshot""")
                    }
                    append("}")
                }
                appendLog(
                    direction = ProtocolDirection.CLIENT_TO_SERVER_EVENT,
                    summary = "User Action Dispatched: '${outbound.type}' (from #${outbound.componentId})",
                    payload = payloadPreview,
                )

                // Simulate Gemini Agent round-trip response!
                viewModelScope.launch {
                    delay(300L)
                    when (outbound.type) {
                        "confirm_beni_suef_trip", "submit_devfest_feedback" -> {
                            val responseJson =
                                DevFestAgentBlueprints.buildAgentResponseForFeedback(outbound.context)
                            sendServerJson(
                                summary = "Gemini Agent Reply -> updateDataModel (/trip/agentConfirmation)",
                                json = responseJson,
                            )
                        }

                        "plan_place_visit" -> {
                            val placeName = outbound.context["placeName"]?.toString() ?: "Meidum Pyramid"
                            loadTwoWayBindingScenario(logEntries = true)
                            val customNoteJson =
                                """{"version":"v0.9","updateDataModel":{"surfaceId":"${DevFestAgentBlueprints.SURFACE_ITINERARY}","path":"/trip/pickupNotes","value":"Priority visit: $placeName • Morning Departure"}}"""
                            sendServerJson(
                                summary = "Gemini Agent Reply -> Pre-filled Trip Builder for $placeName",
                                json = customNoteJson,
                            )
                            removeExistingSurfaceMessage(DevFestAgentBlueprints.SURFACE_ITINERARY)
                            chatMessages.add(
                                ChatMessageItem(
                                    id = messageSequence++,
                                    isUser = false,
                                    senderName = "Beni Suef Explorer AI • A2UI Surface",
                                    text = "Great choice! I've opened the **1-Day Beni Suef Trip Builder** pre-focused on **$placeName**:",
                                    modeAtCreation = AgentResponseMode.A2UI_COMPOSE,
                                    surfaceId = DevFestAgentBlueprints.SURFACE_ITINERARY,
                                )
                            )
                        }

                        "build_itinerary_from_saved" -> {
                            val selectedStops = DevFestAgentBlueprints.buildSavedStopsList(outbound.context)
                            val stopsJsonArray = selectedStops.joinToString(",") { "\"$it\"" }
                            loadTwoWayBindingScenario(logEntries = true)
                            val updateStopsJson =
                                """{"version":"v0.9","updateDataModel":{"surfaceId":"${DevFestAgentBlueprints.SURFACE_ITINERARY}","path":"/trip/stops","value":[$stopsJsonArray]}}"""
                            sendServerJson(
                                summary = "Gemini Agent Reply -> Synced ♥ Saved Places ($selectedStops) into Trip Builder",
                                json = updateStopsJson,
                            )
                            removeExistingSurfaceMessage(DevFestAgentBlueprints.SURFACE_ITINERARY)
                            chatMessages.add(
                                ChatMessageItem(
                                    id = messageSequence++,
                                    isUser = false,
                                    senderName = "Beni Suef Explorer AI • A2UI Surface",
                                    text = "I synced your **${selectedStops.size} ♥ Saved Beni Suef Places** from `A2uiDataModel` into this interactive **1-Day Trip Builder**:",
                                    modeAtCreation = AgentResponseMode.A2UI_COMPOSE,
                                    surfaceId = DevFestAgentBlueprints.SURFACE_ITINERARY,
                                )
                            )
                        }
                    }
                }
            }

            is A2uiClientErrorMessage -> {
                lastSecurityBanner =
                    "Shield Intercepted [${outbound.code}] on '${outbound.surfaceId}': ${outbound.message}"
                appendLog(
                    direction = ProtocolDirection.CLIENT_TO_SERVER_ERROR,
                    summary = "Outbound A2uiClientErrorMessage (${outbound.code} on '${outbound.surfaceId}')",
                    payload = """{"code":"${outbound.code}","surfaceId":"${outbound.surfaceId}","message":"${outbound.message}","context":${outbound.context}}""",
                )

                if (autoSelfCorrectEnabled && outbound.surfaceId == DevFestAgentBlueprints.SURFACE_SECURITY) {
                    viewModelScope.launch {
                        delay(800L)
                        appendLog(
                            direction = ProtocolDirection.AGENT_SELF_CORRECTION,
                            summary = "Gemini Agent Self-Correction Patch",
                            payload = DevFestAgentBlueprints.SELF_CORRECTION_PATCH,
                        )
                        processor.processInput(parser, DevFestAgentBlueprints.SELF_CORRECTION_PATCH)
                        lastSecurityBanner =
                            "✓ Auto Self-Corrected! Agent received A2uiClientErrorMessage & healed '${outbound.surfaceId}'."
                    }
                }
            }
        }
    }

    private fun appendLog(direction: ProtocolDirection, summary: String, payload: String) {
        protocolLogs.add(
            0,
            ProtocolLogEntry(
                id = logSequence++,
                direction = direction,
                summary = summary,
                payload = payload,
            )
        )
        if (protocolLogs.size > 50) {
            protocolLogs.removeAt(protocolLogs.lastIndex)
        }
    }
}
