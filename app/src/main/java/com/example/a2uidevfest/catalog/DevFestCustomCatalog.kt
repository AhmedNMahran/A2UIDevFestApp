package com.example.a2uidevfest.catalog

import androidx.a2ui.compose.runtime.A2uiComponentProperties
import androidx.a2ui.compose.runtime.A2uiComponentScope
import androidx.a2ui.compose.runtime.A2uiProperty
import androidx.a2ui.compose.runtime.DynamicA2uiProperty
import androidx.a2ui.compose.runtime.StaticA2uiProperty
import androidx.a2ui.compose.ui.A2uiCatalog
import androidx.a2ui.compose.ui.A2uiComponent
import androidx.a2ui.compose.ui.catalog.A2uiBasicCatalogV1
import androidx.a2ui.model.catalog.functions.A2uiLocaleProvider
import androidx.a2ui.model.protocol.A2uiException
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.a2ui.catalog.MaterialA2uiBasicCatalogV1Defaults
import androidx.compose.material3.a2ui.catalog.materialA2uiBasicCatalogV1
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.text.MessageFormat

/**
 * Factory and definitions for both the standard A2UI Basic Catalog V1 and our extended
 * Beni Suef Tourism Custom Catalog (`BeniSuefPlaceCard` + `InteractiveRatingGauge`).
 */
object DevFestCatalogs {

    const val BASIC_CATALOG_ID: String = A2uiBasicCatalogV1.CatalogId
    const val DEVFEST_CATALOG_ID: String =
        "https://devfest.a2ui.org/specification/v0_9/catalogs/beni_suef_tourism/catalog.json"

    /**
     * Creates the standard Material 3 Basic Catalog V1 together with the extended
     * Beni Suef Tourism Custom Catalog.
     */
    fun createCatalogs(onUrlOpened: (String) -> Unit): List<A2uiCatalog> {
        val basicCatalog = materialA2uiBasicCatalogV1(
            image = MaterialA2uiBasicCatalogV1Defaults.image { url, description, _, modifier, _ ->
                BeniSuefMediaBanner(
                    url = url,
                    description = description,
                    modifier = modifier,
                )
            },
            video = MaterialA2uiBasicCatalogV1Defaults.video { url, modifier, _ ->
                Surface(
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                ) {
                    Text(
                        text = "🎬 Beni Suef Tour Video: $url",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            },
            audioPlayer = MaterialA2uiBasicCatalogV1Defaults.audioPlayer { url, description, modifier, _ ->
                Surface(
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    Text(
                        text = "🔊 Audio Guide: ${description ?: url}",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            },
            urlOpener = { url -> onUrlOpened(url) },
            messageFormatter = { pattern, locale, args ->
                runCatching {
                    MessageFormat(pattern, locale).format(args)
                }.getOrDefault(pattern)
            },
            localeProvider = A2uiLocaleProvider.Default,
        )

        val beniSuefCatalog = A2uiCatalog(
            catalogId = DEVFEST_CATALOG_ID,
            components = basicCatalog.components + listOf(
                BeniSuefPlaceCardComponent,
                InteractiveRatingGaugeComponent,
            ),
            functions = basicCatalog.functions,
            themeSchema = basicCatalog.themeSchema,
            isInline = true,
        )

        return listOf(basicCatalog, beniSuefCatalog)
    }
}

@Composable
private fun BeniSuefMediaBanner(
    url: String,
    description: String?,
    modifier: Modifier = Modifier,
) {
    val lower = (url + " " + (description ?: "")).lowercase()
    val (emoji, gradient, subtitle) = when {
        "meidum" in lower || "pyramid" in lower -> Triple(
            "🔺",
            listOf(Color(0xFFB45309), Color(0xFFD97706)),
            "Old Kingdom • Pharaoh Sneferu (~2600 BC)"
        )
        "sannur" in lower || "cave" in lower -> Triple(
            "🦇",
            listOf(Color(0xFF6D28D9), Color(0xFF9333EA)),
            "Eocene Alabaster Cavern • Natural Protectorate"
        )
        "nile" in lower || "corniche" in lower -> Triple(
            "⛵",
            listOf(Color(0xFF0369A1), Color(0xFF0D9488)),
            "Beni Suef Nile Promenade & Sunset Feluccas"
        )
        else -> Triple(
            "🏛️",
            listOf(Color(0xFF1D4ED8), Color(0xFF0F766E)),
            "Beni Suef Heritage & Archaeological Gems"
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.horizontalGradient(gradient))
            .padding(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = emoji, style = MaterialTheme.typography.titleLarge)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = description ?: "Explore Beni Suef, Egypt",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.88f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/**
 * Custom A2UI Component #1: `"BeniSuefPlaceCard"`
 *
 * Renders an interactive tourist landmark card for Beni Suef, Egypt.
 * Demonstrates all 4 A2UI talk pillars:
 * 1. Schema declaration via [A2uiProperty] (`dynamicString`, `stringEnum`, `dynamicNumber`, `dynamicBoolean`, `action`).
 * 2. Progressive rendering readiness via [isReady]: shows skeleton loading until `/places/...`
 *    data model paths arrive, and calls [reportError] if `rating` is out of bounds (`0.0..5.0`).
 * 3. Reactive two-way data binding via [A2uiComponentScope.bind] and [A2uiComponentScope.bindUpdater]
 *    for saving/bookmarking places into the live `A2uiDataModel`.
 * 4. Action dispatch loop back to the remote Gemini agent via [A2uiComponentScope.dispatchAction].
 */
object BeniSuefPlaceCardComponent : A2uiComponent {

    override val name: String = "BeniSuefPlaceCard"

    override val description: String =
        "Displays a touristic landmark in Beni Suef, Egypt with two-way trip bookmarking and an agent itinerary action trigger."

    val PlaceNameProperty: DynamicA2uiProperty<String> = A2uiProperty.dynamicString(
        key = "placeName",
        required = true,
        description = "English name of the touristic landmark in Beni Suef.",
    )

    val ArabicNameProperty: DynamicA2uiProperty<String> = A2uiProperty.dynamicString(
        key = "arabicName",
        required = false,
        description = "Arabic name of the landmark (e.g. هرم ميدوم).",
    )

    val CategoryProperty: StaticA2uiProperty<String> = A2uiProperty.stringEnum(
        key = "category",
        enumValues = listOf(
            "Pharaonic & Pyramids",
            "Natural Cave & Desert",
            "Nile Riverfront",
            "Museum & Heritage",
        ),
        defaultValue = "Pharaonic & Pyramids",
        description = "Tourism category badge.",
    )

    val SummaryProperty: DynamicA2uiProperty<String> = A2uiProperty.dynamicString(
        key = "summary",
        required = true,
        description = "Visitor highlight description and historical significance.",
    )

    val DistanceProperty: DynamicA2uiProperty<String> = A2uiProperty.dynamicString(
        key = "distance",
        required = false,
        description = "Distance or travel time from Beni Suef city center.",
    )

    val RatingProperty: DynamicA2uiProperty<Number> = A2uiProperty.dynamicNumber(
        key = "rating",
        required = true,
        description = "Visitor rating between 0.0 and 5.0.",
    )

    val SavedProperty: DynamicA2uiProperty<Boolean> = A2uiProperty.dynamicBoolean(
        key = "saved",
        required = true,
        description = "Two-way bound boolean indicating whether the traveler saved this place to their Beni Suef itinerary.",
    )

    val ActionProperty: StaticA2uiProperty<Map<String, Any?>> = A2uiProperty.action(
        key = "onPlanVisitAction",
        required = false,
        description = "Optional A2UI action dispatched when the user taps 'Plan Visit with Agent'.",
    )

    override val properties: List<A2uiProperty<*>> = listOf(
        PlaceNameProperty,
        ArabicNameProperty,
        CategoryProperty,
        SummaryProperty,
        DistanceProperty,
        RatingProperty,
        SavedProperty,
        ActionProperty,
    )

    /**
     * Progressive Rendering & Runtime Invariant Gate:
     * - Returns `false` (keeping the card in `A2uiComponentState.Loading`) until all required
     *   dynamic properties resolve from the `A2uiDataModel`.
     * - Reports an [A2uiException.A2uiRuntimeException] if `rating` is outside `0.0..5.0`.
     */
    @Composable
    override fun A2uiComponentScope.isReady(properties: A2uiComponentProperties): Boolean {
        val placeName = properties.bind(PlaceNameProperty) ?: return false
        properties.bind(SummaryProperty) ?: return false
        val rating = properties.bind(RatingProperty)?.toDouble() ?: return false
        properties.bind(SavedProperty) ?: return false

        if (rating < 0.0 || rating > 5.0) {
            SideEffect {
                reportError(
                    A2uiException.A2uiRuntimeException(
                        "BeniSuefPlaceCard('$placeName') received invalid rating=$rating (must be in 0.0..5.0)"
                    )
                )
            }
            return false
        }
        return true
    }

    @Composable
    override fun A2uiComponentScope.Content(
        properties: A2uiComponentProperties,
        modifier: Modifier,
    ) {
        val placeName = properties.bind(PlaceNameProperty) ?: return
        val arabicName = properties.bind(ArabicNameProperty)
        val category = properties[CategoryProperty] ?: "Pharaonic & Pyramids"
        val summary = properties.bind(SummaryProperty) ?: return
        val distance = properties.bind(DistanceProperty) ?: "Beni Suef Governorate"
        val rating = properties.bind(RatingProperty)?.toDouble() ?: 4.8
        val isSaved = properties.bind(SavedProperty) ?: false

        // Two-way data binding: non-null only if bound to a writable JSON pointer path (e.g. {"path": "/places/meidum/saved"})
        val onSavedUpdater = properties.bindUpdater(SavedProperty)
        val isWritable = onSavedUpdater != null
        val planVisitAction = properties[ActionProperty]

        val (badgeEmoji, accentColors) = when (category) {
            "Pharaonic & Pyramids" -> "🔺" to listOf(Color(0xFFB45309), Color(0xFFD97706))
            "Natural Cave & Desert" -> "🦇" to listOf(Color(0xFF6D28D9), Color(0xFF8B5CF6))
            "Nile Riverfront" -> "⛵" to listOf(Color(0xFF0369A1), Color(0xFF0D9488))
            else -> "🏛️" to listOf(Color(0xFF1E40AF), Color(0xFF3730A3))
        }

        ElevatedCard(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp),
        ) {
            Column {
                // Scenic Landmark Header Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.horizontalGradient(accentColors))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.22f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = badgeEmoji,
                                    style = MaterialTheme.typography.titleMedium,
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = category.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Text(
                                    text = placeName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                )
                                if (!arabicName.isNullOrBlank()) {
                                    Text(
                                        text = arabicName,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.White.copy(alpha = 0.92f),
                                    )
                                }
                            }
                        }

                        Surface(
                            color = Color.Black.copy(alpha = 0.28f),
                            shape = RoundedCornerShape(20.dp),
                        ) {
                            Text(
                                text = "★ ${"%.1f".format(rating)}",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFDE68A),
                            )
                        }
                    }
                }

                // Body & Interactive Controls
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "📍 $distance",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = summary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        FilterChip(
                            selected = isSaved,
                            enabled = isWritable,
                            onClick = { onSavedUpdater?.invoke(!isSaved) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            ),
                            label = {
                                Text(
                                    text = when {
                                        !isWritable -> "🔒 Read-Only"
                                        isSaved -> "♥ Saved to Trip"
                                        else -> "♡ Save to Trip"
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isSaved) FontWeight.Bold else FontWeight.Normal,
                                )
                            },
                        )

                        if (planVisitAction != null) {
                            FilledTonalButton(
                                onClick = { dispatchAction(planVisitAction) },
                                contentPadding = ButtonDefaults.TextButtonContentPadding,
                            ) {
                                Text(
                                    text = "Plan Visit →",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Custom A2UI Component #2: `"InteractiveRatingGauge"`
 *
 * Used in the Beni Suef Trip Planner & Security Sandbox to let travelers rate their preferred
 * adventure intensity (`1..5`) with two-way data binding and runtime bounds checking.
 */
object InteractiveRatingGaugeComponent : A2uiComponent {

    override val name: String = "InteractiveRatingGauge"

    override val description: String =
        "An interactive multi-step rating/pace gauge with two-way data binding and runtime bounds checking."

    val TitleProperty: DynamicA2uiProperty<String> = A2uiProperty.dynamicString(
        key = "title",
        required = true,
        description = "Label displayed above the gauge.",
    )

    val ScoreProperty: DynamicA2uiProperty<Number> = A2uiProperty.dynamicNumber(
        key = "score",
        required = true,
        description = "Current numeric score bound to the surface data model.",
    )

    val MaxScoreProperty: StaticA2uiProperty<Number> = A2uiProperty.number(
        key = "maxScore",
        defaultValue = 5,
        description = "Maximum allowed score (defaults to 5).",
    )

    override val properties: List<A2uiProperty<*>> = listOf(
        TitleProperty,
        ScoreProperty,
        MaxScoreProperty,
    )

    @Composable
    override fun A2uiComponentScope.isReady(properties: A2uiComponentProperties): Boolean {
        val title = properties.bind(TitleProperty) ?: return false
        val score = properties.bind(ScoreProperty)?.toInt() ?: return false
        val maxScore = properties[MaxScoreProperty]?.toInt() ?: 5

        if (score < 0 || score > maxScore) {
            SideEffect {
                reportError(
                    A2uiException.A2uiRuntimeException(
                        "InteractiveRatingGauge('$title') received out-of-bounds score=$score (valid range: 0..$maxScore)"
                    )
                )
            }
            return false
        }
        return true
    }

    @Composable
    override fun A2uiComponentScope.Content(
        properties: A2uiComponentProperties,
        modifier: Modifier,
    ) {
        val title = properties.bind(TitleProperty) ?: return
        val score = properties.bind(ScoreProperty)?.toInt() ?: 0
        val maxScore = (properties[MaxScoreProperty]?.toInt() ?: 5).coerceIn(1, 10)
        val onScoreChange = properties.bindUpdater(ScoreProperty)
        val isWritable = onScoreChange != null

        OutlinedCard(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = if (isWritable) "$score / $maxScore ★" else "$score / $maxScore ★ (Read-Only)",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isWritable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    for (step in 1..maxScore) {
                        val isSelected = step <= score
                        val containerColor by animateColorAsState(
                            targetValue = when {
                                isSelected && isWritable -> MaterialTheme.colorScheme.primary
                                isSelected && !isWritable -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            },
                            label = "ratingStepColor",
                        )
                        val contentColor = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(containerColor)
                                .let { base ->
                                    if (onScoreChange != null) {
                                        base.clickable { onScoreChange(step) }
                                    } else {
                                        base
                                    }
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "$step ★",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = contentColor,
                            )
                        }
                    }
                }
            }
        }
    }
}
