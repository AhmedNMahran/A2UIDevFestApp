# A2UIDevFestApp — Beni Suef Explorer AI 🇪🇬

Companion Android application and DevFest / Android Dev Summit presentation for the session:
**"Beyond the WebView: Shipping Agent-Driven Native UIs with Jetpack Compose and A2UI"**

## Overview

**Beni Suef Explorer AI** is a conversational Android app demonstrating how to evolve an AI travel assistant for **Beni Suef, Egypt** from a plain-text chatbot into a rich, native Jetpack Compose experience powered by `androidx.a2ui` (`1.0.0-alpha01`):

- **Before A2UI (`Plain Text Mode`):** Traditional LLM text responses listing *Meidum Pyramid (هرم ميدوم)*, *Wadi Sannur Cave (كهف وادي سنور)*, *Beni Suef Nile Corniche (كورنيش النيل)*, and *Ehnasya & Beni Suef National Museum*.
- **After A2UI (`Compose UI Mode`):** Declarative A2UI v0.9 JSONL blueprints streamed into `A2uiMessageProcessor` and rendered natively inside chat bubbles via `A2uiSurface`, Material 3 catalog components, and custom `A2uiComponent` implementations (`BeniSuefPlaceCard` & `InteractiveRatingGauge`).

## Key Project Files

- [`DevFestCustomCatalog.kt`](app/src/main/java/com/example/a2uidevfest/catalog/DevFestCustomCatalog.kt) — Custom `BeniSuefPlaceCardComponent` and `InteractiveRatingGaugeComponent` with `isReady()` progressive rendering, `bind()` / `bindUpdater()` two-way data binding, `dispatchAction()`, and `reportError()`.
- [`DevFestAgentBlueprints.kt`](app/src/main/java/com/example/a2uidevfest/protocol/DevFestAgentBlueprints.kt) — Plain-text responses ("Before A2UI") and A2UI v0.9 JSONL blueprints ("After A2UI") for Beni Suef landmark discovery, 1-day itinerary building, and adversarial security testing.
- [`DevFestA2uiViewModel.kt`](app/src/main/java/com/example/a2uidevfest/viewmodel/DevFestA2uiViewModel.kt) — Chat state machine, `A2uiMessageParser` + `A2uiMessageProcessor` pipeline, outbound `A2uiClientEventMessage` / `A2uiClientErrorMessage` collector, and contextual test runner.
- [`DevFestMainScreen.kt`](app/src/main/java/com/example/a2uidevfest/ui/DevFestMainScreen.kt) — Conversational Chat UI with a top **Before A2UI (Text) vs. After A2UI (Compose)** mode switcher, inline `A2uiSurface` chat bubbles, progressive stream controls, and a `{ } Wire` BottomSheet inspector.
- [`A2uiBlueprintVerificationTest.kt`](app/src/test/java/com/example/a2uidevfest/A2uiBlueprintVerificationTest.kt) & [`A2uiContextualComposeTest.kt`](app/src/androidTest/java/com/example/a2uidevfest/A2uiContextualComposeTest.kt) — Unit and Compose UI contextual tests.

## Building & Running Tests

```bash
./gradlew assembleDebug testDebugUnitTest
```
