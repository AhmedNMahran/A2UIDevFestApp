package com.example.a2uidevfest

import androidx.a2ui.compose.runtime.A2uiMessageParser
import androidx.a2ui.compose.ui.A2uiMessageProcessor
import androidx.a2ui.model.processor.processInput
import androidx.a2ui.model.protocol.A2uiClientErrorMessage
import androidx.a2ui.model.protocol.A2uiClientEventMessage
import androidx.compose.material3.a2ui.A2uiSurface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.a2uidevfest.catalog.DevFestCatalogs
import com.example.a2uidevfest.protocol.DevFestAgentBlueprints
import com.example.a2uidevfest.ui.theme.A2UIDevFestTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Contextual Compose UI Instrumentation Tests for Takeaway 4 of the talk:
 * "Testing: Using contextual testing patterns to mock agent payloads and safely validate UX behavior."
 */
@RunWith(AndroidJUnit4::class)
class A2uiContextualComposeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun streamingSurface_rendersBeniSuefPlaceCardsAfterDataModelArrives() {
        val parser = A2uiMessageParser()
        val catalogs = DevFestCatalogs.createCatalogs(onUrlOpened = {})
        val processor = A2uiMessageProcessor(catalogs = catalogs)
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        scope.launch { processor.collectMessages() }

        // Stream Chunks 1..3 (createSurface + updateComponents + updateDataModel(/places))
        DevFestAgentBlueprints.streamingScenarioChunks.take(3).forEach { (_, json) ->
            processor.processInput(parser, json)
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            processor.activeSurfaces.value.any { it.id == DevFestAgentBlueprints.SURFACE_PLACES }
        }

        composeTestRule.setContent {
            A2UIDevFestTheme {
                val surfaces by processor.activeSurfaces.collectAsState()
                val surface = surfaces.firstOrNull { it.id == DevFestAgentBlueprints.SURFACE_PLACES }
                if (surface != null) {
                    A2uiSurface(surfaceModel = surface)
                }
            }
        }

        // Verify native Material 3 text and custom BeniSuefPlaceCard render the bound data
        composeTestRule
            .onNodeWithText("Top Touristic Places in Beni Suef")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText("Meidum Pyramid")
            .assertIsDisplayed()
    }

    @Test
    fun twoWayBinding_dispatchesActionWithMutatedBeniSuefTripContext() = runBlocking {
        val parser = A2uiMessageParser()
        val catalogs = DevFestCatalogs.createCatalogs(onUrlOpened = {})
        val processor = A2uiMessageProcessor(catalogs = catalogs)
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        scope.launch { processor.collectMessages() }

        DevFestAgentBlueprints.twoWayBindingBlueprint.forEach { json ->
            processor.processInput(parser, json)
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            processor.activeSurfaces.value.any { it.id == DevFestAgentBlueprints.SURFACE_ITINERARY }
        }

        composeTestRule.setContent {
            A2UIDevFestTheme {
                val surfaces by processor.activeSurfaces.collectAsState()
                val surface = surfaces.firstOrNull { it.id == DevFestAgentBlueprints.SURFACE_ITINERARY }
                if (surface != null) {
                    A2uiSurface(surfaceModel = surface)
                }
            }
        }

        // Tap the primary Button to dispatch 'confirm_beni_suef_trip'
        composeTestRule
            .onNodeWithText("Confirm Beni Suef Day-Trip with Agent")
            .performClick()

        val outbound = processor.outboundEvents.first()
        assertTrue(outbound is A2uiClientEventMessage)
        val eventMessage = outbound as A2uiClientEventMessage
        assertEquals("confirm_beni_suef_trip", eventMessage.type)
        assertEquals("Ahmed Nabil", eventMessage.context["travelerName"])
        assertNotNull(eventMessage.clientDataModel)
    }

    @Test
    fun hallucinatedComponent_emitsOutboundClientErrorWithoutCrashing() = runBlocking {
        val parser = A2uiMessageParser()
        val catalogs = DevFestCatalogs.createCatalogs(onUrlOpened = {})
        val processor = A2uiMessageProcessor(catalogs = catalogs)
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        scope.launch { processor.collectMessages() }

        DevFestAgentBlueprints.securityBaselineBlueprint.forEach { json ->
            processor.processInput(parser, json)
        }
        processor.processInput(parser, DevFestAgentBlueprints.ATTACK_HALLUCINATED_COMPONENT)

        val outbound = processor.outboundEvents.first()
        assertTrue(outbound is A2uiClientErrorMessage)
    }
}
