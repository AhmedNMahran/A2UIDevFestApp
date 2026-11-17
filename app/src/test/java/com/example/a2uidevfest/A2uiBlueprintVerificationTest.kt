package com.example.a2uidevfest

import com.example.a2uidevfest.catalog.BeniSuefPlaceCardComponent
import com.example.a2uidevfest.catalog.DevFestCatalogs
import com.example.a2uidevfest.catalog.InteractiveRatingGaugeComponent
import com.example.a2uidevfest.protocol.DevFestAgentBlueprints
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * JVM Unit Tests verifying custom [BeniSuefPlaceCardComponent] and [InteractiveRatingGaugeComponent]
 * property schema definitions and protocol blueprint invariants for the Beni Suef Tourism A2UI demo.
 */
class A2uiBlueprintVerificationTest {

    @Test
    fun beniSuefPlaceCard_declaresExpectedSchemaProperties() {
        val keys = BeniSuefPlaceCardComponent.properties.map { it.key }
        assertEquals(
            listOf(
                "placeName",
                "arabicName",
                "category",
                "summary",
                "distance",
                "rating",
                "saved",
                "onPlanVisitAction",
            ),
            keys,
        )
        assertTrue(BeniSuefPlaceCardComponent.PlaceNameProperty.isRequired)
        assertTrue(BeniSuefPlaceCardComponent.SummaryProperty.isRequired)
        assertTrue(BeniSuefPlaceCardComponent.RatingProperty.isRequired)
        assertTrue(BeniSuefPlaceCardComponent.SavedProperty.isRequired)
        assertFalse(BeniSuefPlaceCardComponent.ArabicNameProperty.isRequired)
    }

    @Test
    fun interactiveRatingGauge_declaresExpectedSchemaProperties() {
        val keys = InteractiveRatingGaugeComponent.properties.map { it.key }
        assertEquals(listOf("title", "score", "maxScore"), keys)
        assertTrue(InteractiveRatingGaugeComponent.TitleProperty.isRequired)
        assertTrue(InteractiveRatingGaugeComponent.ScoreProperty.isRequired)
        assertFalse(InteractiveRatingGaugeComponent.MaxScoreProperty.isRequired)
    }

    @Test
    fun streamingScenarioBlueprints_containAllFourLifecycleStagesForBeniSuefPlaces() {
        val chunks = DevFestAgentBlueprints.streamingScenarioChunks
        assertEquals(4, chunks.size)
        assertTrue(chunks[0].second.contains("\"createSurface\""))
        assertTrue(chunks[0].second.contains(DevFestCatalogs.DEVFEST_CATALOG_ID))
        assertTrue(chunks[1].second.contains("\"updateComponents\""))
        assertTrue(chunks[1].second.contains("\"BeniSuefPlaceCard\""))
        assertTrue(chunks[2].second.contains("\"updateDataModel\""))
        assertTrue(chunks[2].second.contains("\"/places\""))
        assertTrue(chunks[2].second.contains("Meidum Pyramid"))
        assertTrue(chunks[3].second.contains("\"/tips\""))
    }

    @Test
    fun tripConfirmationResponseGenerator_incorporatesMutatedClientContext() {
        val context = mapOf(
            "travelerName" to "Ahmed Nabil",
            "adventureLevel" to 5,
            "travelers" to 4,
            "stops" to listOf("meidum", "sannur"),
            "pickupNotes" to "Beni Suef Station 8:00 AM",
            "withEgyptologist" to true,
        )
        val json = DevFestAgentBlueprints.buildAgentResponseForFeedback(context)
        assertTrue(json.contains("\"updateDataModel\""))
        assertTrue(json.contains("Ahmed Nabil"))
        assertTrue(json.contains("Meidum Pyramid"))
        assertTrue(json.contains("SnapshotStateSparseList"))
    }
}
