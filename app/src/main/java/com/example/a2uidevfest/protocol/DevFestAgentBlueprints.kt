package com.example.a2uidevfest.protocol

import com.example.a2uidevfest.catalog.DevFestCatalogs

/**
 * Curated Plain-Text responses ("Before A2UI") and A2UI v0.9 JSONL protocol blueprints ("After A2UI")
 * for the Beni Suef, Egypt Tourism AI Assistant demo app.
 */
object DevFestAgentBlueprints {

    const val SURFACE_PLACES = "beni_suef_places"
    const val SURFACE_ITINERARY = "beni_suef_itinerary"
    const val SURFACE_SECURITY = "beni_suef_security"

    // Compatibility aliases used by tests and presentation references
    const val SURFACE_STREAMING = SURFACE_PLACES
    const val SURFACE_BINDING = SURFACE_ITINERARY

    /**
     * BEFORE A2UI (Plain Text Mode):
     * What a traditional text-only LLM chatbot returns when asked for touristic places in Beni Suef.
     */
    val TEXT_RESPONSE_PLACES: String = """
        Here are the top touristic places to visit in **Beni Suef, Egypt (محافظة بني سويف)**:

        1. **Meidum Pyramid (هرم ميدوم)**
           • Category: Pharaonic & Pyramids (★ 4.9)
           • Distance: ~42 km north of Beni Suef city
           • Info: Built for Pharaoh Sneferu (~2600 BC), marking humanity's transition from step pyramids to the first true smooth-sided pyramid. Famous for the Meidum Geese fresco and Mastaba of Nefermaat.

        2. **Wadi Sannur Cave Protectorate (كهف وادي سنور)**
           • Category: Natural Cave & Desert (★ 4.8)
           • Distance: ~70 km southeast in the Eastern Desert
           • Info: A rare 40-million-year-old Eocene alabaster cavern filled with glittering stalactites and stalagmites.

        3. **Beni Suef Nile Corniche (كورنيش النيل ببني سويف)**
           • Category: Nile Riverfront (★ 4.7)
           • Distance: City Center (1.5 km)
           • Info: A newly landscaped 5 km riverfront promenade ideal for sunset felucca boat rides, cafés, and views of the Nile islands.

        4. **Beni Suef National Museum & Ehnasya (متحف بني سويف وإهناسيا)**
           • Category: Museum & Heritage (★ 4.6)
           • Distance: ~15 km west of Beni Suef city
           • Info: Showcases artifacts from Heracleopolis Magna (capital of Egypt during the 9th & 10th Dynasties), Roman ruins, and Coptic heritage.

        ⚠️ *Notice the limitation of plain text: you cannot filter categories with tabs, bookmark places into live state, or tap a card to configure a trip without typing another long message.*
    """.trimIndent()

    /**
     * BEFORE A2UI (Plain Text Mode):
     * What a traditional text-only LLM chatbot returns when asked to plan a 1-day trip in Beni Suef.
     */
    val TEXT_RESPONSE_ITINERARY: String = """
        I can help you plan a **1-Day Trip in Beni Suef**, but since this is plain-text chat, please reply by typing answers to all 5 questions below:

        1. Which stops do you want? (Meidum Pyramid, Wadi Sannur Cave, Nile Corniche, Ehnasya Ruins)
        2. How many travelers are in your group? (1 to 10)
        3. What adventure pace do you prefer? (1 = Relaxed to 5 = Full Desert Expedition)
        4. Do you need a licensed Egyptologist guide & 4x4 transfer? (Yes / No)
        5. Where should we pick you up? (e.g., Beni Suef Train Station at 8:00 AM)

        ⚠️ *With A2UI Mode enabled, the agent streams an interactive Material 3 Trip Builder surface with two-way data bindings so you can adjust sliders, chips, and checkboxes directly!*
    """.trimIndent()

    /**
     * AFTER A2UI — Scenario 1: Discovering Touristic Places in Beni Suef (`beni_suef_places`).
     * Divided into 4 progressive JSONL chunks to demonstrate `createSurface` -> `updateComponents`
     * (skeleton loading via `isReady()`) -> `updateDataModel` (/places) -> `updateDataModel` (/tips).
     */
    val streamingScenarioChunks: List<Pair<String, String>> = listOf(
        "Chunk 1: createSurface (Beni Suef Tourism Catalog Negotiation)" to
            """{"version":"v0.9","createSurface":{"surfaceId":"$SURFACE_PLACES","catalogId":"${DevFestCatalogs.DEVFEST_CATALOG_ID}","sendDataModel":true}}""",

        "Chunk 2: updateComponents (Beni Suef Places UI Tree — Awaiting /places Data)" to
            """{"version":"v0.9","updateComponents":{"surfaceId":"$SURFACE_PLACES","components":[""" +
            """{"id":"root","component":"Column","children":["hero_banner","header_title","header_sub","category_tabs","saved_summary_btn"]},""" +
            """{"id":"hero_banner","component":"Image","url":"https://egypt.travel/beni-suef/meidum-pyramid-hero.jpg","description":"Beni Suef, Egypt • Pharaonic & Nile Gems"},""" +
            """{"id":"header_title","component":"Text","text":"Top Touristic Places in Beni Suef","variant":"h4"},""" +
            """{"id":"header_sub","component":"Text","text":"Tap ♥ Save to Trip (two-way bound to A2uiDataModel) or Plan Visit on any landmark:","variant":"caption"},""" +
            """{"id":"category_tabs","component":"Tabs","tabs":[{"title":"All Gems (4)","child":"all_places_col"},{"title":"Pharaonic","child":"pharaonic_col"},{"title":"Nature & Nile","child":"nature_col"}]},""" +
            """{"id":"all_places_col","component":"Column","children":["card_meidum","card_sannur","card_corniche","card_museum"]},""" +
            """{"id":"pharaonic_col","component":"Column","children":["card_meidum","card_museum"]},""" +
            """{"id":"nature_col","component":"Column","children":["card_sannur","card_corniche","tips_header","tips_list"]},""" +
            """{"id":"card_meidum","component":"BeniSuefPlaceCard","placeName":{"path":"/places/meidum/name"},"arabicName":{"path":"/places/meidum/arabicName"},"category":"Pharaonic & Pyramids","summary":{"path":"/places/meidum/summary"},"distance":{"path":"/places/meidum/distance"},"rating":{"path":"/places/meidum/rating"},"saved":{"path":"/places/meidum/saved"},"onPlanVisitAction":{"event":{"name":"plan_place_visit","context":{"placeName":{"path":"/places/meidum/name"},"saved":{"path":"/places/meidum/saved"}}}}},""" +
            """{"id":"card_sannur","component":"BeniSuefPlaceCard","placeName":{"path":"/places/sannur/name"},"arabicName":{"path":"/places/sannur/arabicName"},"category":"Natural Cave & Desert","summary":{"path":"/places/sannur/summary"},"distance":{"path":"/places/sannur/distance"},"rating":{"path":"/places/sannur/rating"},"saved":{"path":"/places/sannur/saved"},"onPlanVisitAction":{"event":{"name":"plan_place_visit","context":{"placeName":{"path":"/places/sannur/name"},"saved":{"path":"/places/sannur/saved"}}}}},""" +
            """{"id":"card_corniche","component":"BeniSuefPlaceCard","placeName":{"path":"/places/corniche/name"},"arabicName":{"path":"/places/corniche/arabicName"},"category":"Nile Riverfront","summary":{"path":"/places/corniche/summary"},"distance":{"path":"/places/corniche/distance"},"rating":{"path":"/places/corniche/rating"},"saved":{"path":"/places/corniche/saved"},"onPlanVisitAction":{"event":{"name":"plan_place_visit","context":{"placeName":{"path":"/places/corniche/name"},"saved":{"path":"/places/corniche/saved"}}}}},""" +
            """{"id":"card_museum","component":"BeniSuefPlaceCard","placeName":{"path":"/places/museum/name"},"arabicName":{"path":"/places/museum/arabicName"},"category":"Museum & Heritage","summary":{"path":"/places/museum/summary"},"distance":{"path":"/places/museum/distance"},"rating":{"path":"/places/museum/rating"},"saved":{"path":"/places/museum/saved"},"onPlanVisitAction":{"event":{"name":"plan_place_visit","context":{"placeName":{"path":"/places/museum/name"},"saved":{"path":"/places/museum/saved"}}}}},""" +
            """{"id":"tips_header","component":"Text","text":"Local Beni Suef Travel Tips (Template Bound to /tips):","variant":"h5"},""" +
            """{"id":"tips_list","component":"Column","children":{"path":"/tips","componentId":"tip_card"}},""" +
            """{"id":"tip_card","component":"Card","child":"tip_col"},""" +
            """{"id":"tip_col","component":"Column","children":["tip_title","tip_detail"]},""" +
            """{"id":"tip_title","component":"Text","text":{"path":"title"},"variant":"h5"},""" +
            """{"id":"tip_detail","component":"Text","text":{"path":"detail"},"variant":"body"},""" +
            """{"id":"saved_summary_btn","component":"Button","variant":"primary","child":"saved_summary_btn_text","action":{"event":{"name":"build_itinerary_from_saved","context":{"meidumSaved":{"path":"/places/meidum/saved"},"sannurSaved":{"path":"/places/sannur/saved"},"cornicheSaved":{"path":"/places/corniche/saved"},"museumSaved":{"path":"/places/museum/saved"}}}}},""" +
            """{"id":"saved_summary_btn_text","component":"Text","text":"Build 1-Day Itinerary from ♥ Saved Places"}""" +
            """]}}""",

        "Chunk 3: updateDataModel (Populates /places -> Unlocks BeniSuefPlaceCards!)" to
            """{"version":"v0.9","updateDataModel":{"surfaceId":"$SURFACE_PLACES","path":"/places","value":{""" +
            """"meidum":{"name":"Meidum Pyramid","arabicName":"هرم ميدوم","summary":"Built for Pharaoh Sneferu (~2600 BC), the world's first straight-sided pyramid attempt & home of the famous Meidum Geese fresco.","distance":"42 km north of Beni Suef • 45 min drive","rating":4.9,"saved":true},""" +
            """"sannur":{"name":"Wadi Sannur Cave Protectorate","arabicName":"محمية كهف وادي سنور","summary":"Rare 40-million-year-old Eocene alabaster cavern in the Eastern Desert featuring glowing stalactite & stalagmite formations.","distance":"70 km southeast • 4x4 Desert Track","rating":4.8,"saved":true},""" +
            """"corniche":{"name":"Beni Suef Nile Corniche","arabicName":"كورنيش النيل ببني سويف","summary":"5 km landscaped Nile promenade offering sunset felucca boat rides, island views, and waterfront cafés.","distance":"Beni Suef City Center • 1.5 km","rating":4.7,"saved":false},""" +
            """"museum":{"name":"Beni Suef Museum & Ehnasya","arabicName":"متحف بني سويف وإهناسيا","summary":"Treasures from Heracleopolis Magna (ancient 9th & 10th Dynasty capital), Roman temples, and Coptic monasteries.","distance":"15 km west of Beni Suef city","rating":4.6,"saved":false}""" +
            """}}}""",

        "Chunk 4: updateDataModel (Populates /tips Template List)" to
            """{"version":"v0.9","updateDataModel":{"surfaceId":"$SURFACE_PLACES","path":"/tips","value":[""" +
            """{"title":"Best Time to Visit","detail":"Start at 8:00 AM at Meidum Pyramid before desert heat, then head to Beni Suef Nile Corniche for sunset."},""" +
            """{"title":"Getting There from Cairo","detail":"Only 115 km (90 mins) south of Cairo via the Cairo–Assiut Desert Road or Egyptian National Railways."}""" +
            """]}}""",
    )

    /**
     * AFTER A2UI — Scenario 2: Interactive 1-Day Beni Suef Trip Planner (`beni_suef_itinerary`).
     * Showcases two-way data bindings (`InteractiveRatingGauge`, `ChoicePicker`, `Slider`,
     * `CheckBox`, `TextField`) and outbound `A2uiClientEventMessage` dispatch back to Gemini.
     */
    val twoWayBindingBlueprint: List<String> = listOf(
        """{"version":"v0.9","createSurface":{"surfaceId":"$SURFACE_ITINERARY","catalogId":"${DevFestCatalogs.DEVFEST_CATALOG_ID}","sendDataModel":true}}""",
        """{"version":"v0.9","updateDataModel":{"surfaceId":"$SURFACE_ITINERARY","path":"/trip","value":{"travelerName":"Ahmed Nabil","adventureLevel":4,"travelers":3,"stops":["meidum","sannur","corniche"],"pickupNotes":"Beni Suef Railway Station • 8:30 AM","withEgyptologist":true,"agentConfirmation":"Adjust stops, group size, or adventure level above, then tap 'Confirm Beni Suef Day-Trip' to send the bound state to the Gemini Agent!"}}}""",
        """{"version":"v0.9","updateComponents":{"surfaceId":"$SURFACE_ITINERARY","components":[""" +
            """{"id":"root","component":"Column","children":["itinerary_banner","itinerary_title","adventure_gauge","traveler_input","stops_picker","group_slider","guide_checkbox","pickup_input","confirm_btn","confirmation_card"]},""" +
            """{"id":"itinerary_banner","component":"Image","url":"https://egypt.travel/beni-suef/nile-corniche.jpg","description":"1-Day Beni Suef Interactive Trip Builder"},""" +
            """{"id":"itinerary_title","component":"Text","text":"Customize Your Beni Suef Day-Trip (Two-Way Bound)","variant":"h4"},""" +
            """{"id":"adventure_gauge","component":"InteractiveRatingGauge","title":"Adventure Intensity Level (/trip/adventureLevel)","score":{"path":"/trip/adventureLevel"},"maxScore":5},""" +
            """{"id":"traveler_input","component":"TextField","label":"Lead Traveler Name (/trip/travelerName)","value":{"path":"/trip/travelerName"},"variant":"shortText"},""" +
            """{"id":"stops_picker","component":"ChoicePicker","label":"Select Beni Suef Stops (/trip/stops)","variant":"multipleSelection","displayStyle":"chips","options":[{"label":"🔺 Meidum Pyramid","value":"meidum"},{"label":"🦇 Wadi Sannur Cave","value":"sannur"},{"label":"⛵ Nile Corniche Felucca","value":"corniche"},{"label":"🏛️ Ehnasya & Museum","value":"museum"}],"value":{"path":"/trip/stops"}},""" +
            """{"id":"group_slider","component":"Slider","label":"Group Size — Travelers (/trip/travelers)","min":1,"max":10,"value":{"path":"/trip/travelers"}},""" +
            """{"id":"pickup_input","component":"TextField","label":"Pickup Point & Notes (/trip/pickupNotes)","value":{"path":"/trip/pickupNotes"},"variant":"shortText"},""" +
            """{"id":"guide_checkbox","component":"CheckBox","label":"Include licensed Egyptologist & 4x4 Wadi Sannur transfer (/trip/withEgyptologist)","value":{"path":"/trip/withEgyptologist"}},""" +
            """{"id":"confirm_btn","component":"Button","variant":"primary","child":"confirm_btn_text","action":{"event":{"name":"confirm_beni_suef_trip","context":{"travelerName":{"path":"/trip/travelerName"},"adventureLevel":{"path":"/trip/adventureLevel"},"travelers":{"path":"/trip/travelers"},"stops":{"path":"/trip/stops"},"pickupNotes":{"path":"/trip/pickupNotes"},"withEgyptologist":{"path":"/trip/withEgyptologist"}}}}},""" +
            """{"id":"confirm_btn_text","component":"Text","text":"Confirm Beni Suef Day-Trip with Agent"},""" +
            """{"id":"confirmation_card","component":"Card","child":"confirmation_col"},""" +
            """{"id":"confirmation_col","component":"Column","children":["confirmation_title","confirmation_body"]},""" +
            """{"id":"confirmation_title","component":"Text","text":"Gemini Travel Agent Live Itinerary Status","variant":"h5"},""" +
            """{"id":"confirmation_body","component":"Text","text":{"path":"/trip/agentConfirmation"},"variant":"body"}""" +
            """]}}""",
    )

    /**
     * Generates a dynamic Server-to-Client `updateDataModel` response after the user clicks
     * "Confirm Beni Suef Day-Trip with Agent" in `SURFACE_ITINERARY`.
     */
    fun buildAgentResponseForFeedback(context: Map<String, Any?>?): String {
        val travelerName = (context?.get("travelerName") ?: context?.get("attendee"))?.toString() ?: "Traveler"
        val adventureLevel = (context?.get("adventureLevel") ?: context?.get("rating"))?.toString() ?: "4"
        val travelersCount = ((context?.get("travelers") ?: context?.get("pace")) as? Number)?.toInt() ?: 3
        val rawStops = (context?.get("stops") ?: context?.get("topics")) as? List<*>
        val stopsFormatted = rawStops?.joinToString(", ") { code ->
            when (code.toString()) {
                "meidum" -> "Meidum Pyramid (09:00)"
                "sannur" -> "Wadi Sannur Cave (12:00)"
                "corniche" -> "Nile Corniche Sunset Felucca (16:30)"
                "museum" -> "Ehnasya & National Museum (14:30)"
                else -> code.toString()
            }
        } ?: "Meidum Pyramid, Wadi Sannur Cave, Nile Corniche"
        val pickupNotes = (context?.get("pickupNotes") ?: context?.get("question"))?.toString().orEmpty()
        val withGuide = (context?.get("withEgyptologist") ?: context?.get("requestCode")) == true

        val replyText = buildString {
            append("✅ Booking Confirmed for $travelerName ($travelersCount travelers, Adventure $adventureLevel/5★)! ")
            append("Route: $stopsFormatted. ")
            if (withGuide) {
                append("Egyptologist guide & 4x4 desert vehicle reserved. ")
            }
            if (pickupNotes.isNotBlank()) {
                append("Pickup: '$pickupNotes'. (Synced via SnapshotStateSparseList & A2uiDataModel).")
            }
        }.replace("\"", "\\\"")

        return """{"version":"v0.9","updateDataModel":{"surfaceId":"$SURFACE_ITINERARY","path":"/trip/agentConfirmation","value":"$replyText"}}"""
    }

    /**
     * Generates a dynamic update when the user taps "Plan Visit →" on a `BeniSuefPlaceCard`
     * or "Build 1-Day Itinerary from ♥ Saved Places".
     */
    fun buildSavedStopsList(context: Map<String, Any?>): List<String> {
        val selected = mutableListOf<String>()
        if (context["meidumSaved"] == true) selected.add("meidum")
        if (context["sannurSaved"] == true) selected.add("sannur")
        if (context["cornicheSaved"] == true) selected.add("corniche")
        if (context["museumSaved"] == true) selected.add("museum")
        if (selected.isEmpty()) {
            selected.add("meidum")
            selected.add("corniche")
        }
        return selected
    }

    /**
     * Scenario 3: Security & Hallucination Defense (`beni_suef_security`).
     */
    val securityBaselineBlueprint: List<String> = listOf(
        """{"version":"v0.9","createSurface":{"surfaceId":"$SURFACE_SECURITY","catalogId":"${DevFestCatalogs.DEVFEST_CATALOG_ID}","sendDataModel":true}}""",
        """{"version":"v0.9","updateDataModel":{"surfaceId":"$SURFACE_SECURITY","path":"/security","value":{"status":"SHIELD ACTIVE: Validating Beni Suef agent JSONL against A2uiCatalog schemas.","gaugeScore":5,"sparseInfo":"Memory shield ready (SnapshotStateSparseList)."}}}""",
        """{"version":"v0.9","updateComponents":{"surfaceId":"$SURFACE_SECURITY","components":[""" +
            """{"id":"root","component":"Column","children":["sec_header_card","readonly_demo_card","sec_gauge"]},""" +
            """{"id":"sec_header_card","component":"Card","child":"sec_header_col"},""" +
            """{"id":"sec_header_col","component":"Column","children":["sec_title","sec_status","sec_sparse_text"]},""" +
            """{"id":"sec_title","component":"Text","text":"A2UI Security Shield • Beni Suef Chat Guard","variant":"h4"},""" +
            """{"id":"sec_status","component":"Text","text":{"path":"/security/status"},"variant":"body"},""" +
            """{"id":"sec_sparse_text","component":"Text","text":{"path":"/security/sparseInfo"},"variant":"caption"},""" +
            """{"id":"readonly_demo_card","component":"Card","child":"readonly_col"},""" +
            """{"id":"readonly_col","component":"Column","children":["readonly_title","readonly_tf"]},""" +
            """{"id":"readonly_title","component":"Text","text":"Read-Only Literal Protection (bindUpdater == null)","variant":"h5"},""" +
            """{"id":"readonly_tf","component":"TextField","label":"Official Monument Permit Code (Literal — Auto-Locked)","value":"EGY-BSF-MEIDUM-2026","variant":"shortText"},""" +
            """{"id":"sec_gauge","component":"InteractiveRatingGauge","title":"Desert Safety Rating Bounds Check (0..5)","score":{"path":"/security/gaugeScore"},"maxScore":5}""" +
            """]}}""",
    )

    /**
     * Attack 1: LLM hallucinates a non-existent `"PyramidHologram3D"` component not in the catalog.
     */
    const val ATTACK_HALLUCINATED_COMPONENT: String =
        """{"version":"v0.9","updateComponents":{"surfaceId":"$SURFACE_SECURITY","components":[{"id":"root","component":"PyramidHologram3D","monument":"Meidum","laserIntensity":9000}]}}"""

    /**
     * Attack 2: LLM sends a schema violation (`Slider` missing required `max` and passing a String for `min`).
     */
    const val ATTACK_SCHEMA_TYPE_MISMATCH: String =
        """{"version":"v0.9","updateComponents":{"surfaceId":"$SURFACE_SECURITY","components":[{"id":"bad_slider","component":"Slider","min":"invalid_number","value":5}]}}"""

    /**
     * Defense 3a: LLM writes to a sparse index `/sparseItems/50000`.
     */
    const val DEFENSE_SPARSE_ARRAY_SAFE: String =
        """{"version":"v0.9","updateDataModel":{"surfaceId":"$SURFACE_SECURITY","path":"/sparseItems/50000","value":"Beni Suef waypoint #50,000 stored in ~24 bytes!"}}"""

    const val DEFENSE_SPARSE_ARRAY_SAFE_STATUS: String =
        """{"version":"v0.9","updateDataModel":{"surfaceId":"$SURFACE_SECURITY","path":"/security/sparseInfo","value":"✓ Wrote /sparseItems/50000 -> SnapshotStateSparseList upgraded to MutableIntObjectMap (~24 bytes, 0 nulls allocated)!"}}"""

    /**
     * Attack 3b: LLM attempts an OOM index attack at `/sparseItems/250000` (above MAX_LIST_SIZE = 100,001).
     */
    const val ATTACK_SPARSE_ARRAY_OOM: String =
        """{"version":"v0.9","updateDataModel":{"surfaceId":"$SURFACE_SECURITY","path":"/sparseItems/250000","value":"Malicious OOM index > 100,000"}}"""

    /**
     * Attack 4: LLM updates `/security/gaugeScore` to `999` (violating `maxScore = 5`).
     */
    const val ATTACK_SEMANTIC_OUT_OF_BOUNDS: String =
        """{"version":"v0.9","updateDataModel":{"surfaceId":"$SURFACE_SECURITY","path":"/security/gaugeScore","value":999}}"""

    /**
     * Self-correction patch sent when the simulated Gemini Agent receives an `A2uiClientErrorMessage`.
     */
    const val SELF_CORRECTION_PATCH: String =
        """{"version":"v0.9","updateDataModel":{"surfaceId":"$SURFACE_SECURITY","path":"/security","value":{"status":"✓ SELF-CORRECTED: Agent received A2uiClientErrorMessage and restored valid Beni Suef surface state.","gaugeScore":5,"sparseInfo":"Memory shield verified (MAX_LIST_SIZE = 100,001 enforced)."}}}"""
}
