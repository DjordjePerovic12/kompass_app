package llc.bokadev.kompass.presentation.screens.myguides

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import llc.bokadev.kompass.core.presentation.base.BaseContentView
import llc.bokadev.kompass.core.util.buildPhotoUrl
import llc.bokadev.kompass.core.util.currentAppLanguage
import llc.bokadev.kompass.domain.model.Experience
import llc.bokadev.kompass.domain.model.FavoriteItemType
import llc.bokadev.kompass.domain.model.Place
import llc.bokadev.kompass.domain.repository.AnalyticsRepository
import llc.bokadev.kompass.domain.repository.FavoritesRepository
import llc.bokadev.kompass.presentation.shared.FavoriteToggleButton
import llc.bokadev.kompass.presentation.shared.KompassSharedTopBar
import llc.bokadev.kompass.presentation.theme.KompassTheme
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MyGuidesScreen(
    onBack: () -> Unit = {},
    onOpenPlaceGuide: (String) -> Unit = {},
    onOpenActivityGuide: (String) -> Unit = {},
    onOpenPlace: (String) -> Unit = {},
    onOpenActivity: (String) -> Unit = {}
) {
    val vm: MyGuidesViewModel = koinViewModel()
    val state by vm.state.collectAsState()
    val lang = currentAppLanguage()
    val analytics = koinInject<AnalyticsRepository>()
    val favoritesRepository = koinInject<FavoritesRepository>()
    val favorites by favoritesRepository.favoritesFlow.collectAsState()
    val favoriteKeys = favoritesRepository.getFavoriteKeySet()

    LaunchedEffect(Unit) {
        analytics.trackScreenView("audio_library")
    }

    val guideItems = buildList {
        addAll(state.placeGuides.map { AudioLibraryItem.PlaceItem(it) })
        addAll(state.activityGuides.map { AudioLibraryItem.ActivityItem(it) })
    }.sortedBy { it.title(lang) }
        .let { items ->
            val (favoriteItems, regularItems) = items.partition { item ->
                when (item) {
                    is AudioLibraryItem.PlaceItem ->
                        favoriteKeys.any { it.type == FavoriteItemType.PLACE && it.id == item.place.id }
                    is AudioLibraryItem.ActivityItem ->
                        favoriteKeys.any { it.type == FavoriteItemType.ACTIVITY && it.id == item.activity.id }
                }
            }
            favoriteItems + regularItems
        }

    BaseContentView(
        state = state,
        topBar = {
            KompassSharedTopBar(
                slug = "",
                title = "Audio Guides",
                subtitle = "Places and activities ready to play",
                showBack = true,
                onBackClick = onBack
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(KompassTheme.colors.colorHomeCanvas),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (guideItems.isEmpty()) {
                item {
                    AudioLibraryPlaceholder()
                }
            } else {
                items(guideItems, key = { item -> item.stableKey }) { item ->
                    AudioLibraryCard(
                        item = item,
                        lang = lang,
                        isFavorited = when (item) {
                            is AudioLibraryItem.PlaceItem -> favoritesRepository.isFavorited(FavoriteItemType.PLACE, item.place.id)
                            is AudioLibraryItem.ActivityItem -> favoritesRepository.isFavorited(FavoriteItemType.ACTIVITY, item.activity.id)
                        },
                        onFavoriteClick = {
                            when (item) {
                                is AudioLibraryItem.PlaceItem -> favoritesRepository.toggleFavorite(FavoriteItemType.PLACE, item.place.id)
                                is AudioLibraryItem.ActivityItem -> favoritesRepository.toggleFavorite(FavoriteItemType.ACTIVITY, item.activity.id)
                            }
                        },
                        onCardClick = {
                            when (item) {
                                is AudioLibraryItem.PlaceItem -> {
                                    analytics.trackPlaceView(
                                        placeId = item.place.id,
                                        cityId = item.place.cityId,
                                        zone = item.place.zone,
                                        placeCategory = item.place.category.name.lowercase(),
                                        contentOrigin = "audio_library"
                                    )
                                    onOpenPlace(item.place.id)
                                }
                                is AudioLibraryItem.ActivityItem -> {
                                    analytics.trackActivityView(
                                        activityId = item.activity.id,
                                        cityId = item.activity.cityId,
                                        zone = item.activity.category,
                                        contentOrigin = "audio_library"
                                    )
                                    onOpenActivity(item.activity.id)
                                }
                            }
                        },
                        onPlayClick = {
                            when (item) {
                                is AudioLibraryItem.PlaceItem -> {
                                    analytics.trackGuideOpen(item.place.id, item.place.cityId)
                                    analytics.trackAudioPlay(item.place.id, item.place.cityId)
                                    onOpenPlaceGuide(item.place.id)
                                }
                                is AudioLibraryItem.ActivityItem -> {
                                    analytics.trackGuideOpen(item.activity.id, item.activity.cityId)
                                    analytics.trackAudioPlay(item.activity.id, item.activity.cityId)
                                    onOpenActivityGuide(item.activity.id)
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

private sealed interface AudioLibraryItem {
    val stableKey: String
    fun title(lang: String): String
    fun meta(lang: String): String
    fun imageUrl(): String?

    data class PlaceItem(val place: Place) : AudioLibraryItem {
        override val stableKey: String = "place-${place.id}"
        override fun title(lang: String): String = place.localizedName(lang)
        override fun meta(lang: String): String {
            val zone = place.zone?.replace('_', ' ')?.replace('-', ' ')?.takeIf { it.isNotBlank() }
            return listOfNotNull(
                place.category.name.lowercase().replace('_', ' ').replaceFirstChar { it.titlecase() },
                zone
            ).joinToString(" · ")
        }
        override fun imageUrl(): String? = place.photos.firstOrNull()?.let(::buildPhotoUrl)
    }

    data class ActivityItem(val activity: Experience) : AudioLibraryItem {
        override val stableKey: String = "activity-${activity.id}"
        override fun title(lang: String): String = activity.localizedName(lang)
        override fun meta(lang: String): String {
            val category = activity.category?.replace('_', ' ')?.replace('-', ' ')
                ?.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            val location = activity.localizedLocation(lang).takeIf { it.isNotBlank() }
            return listOfNotNull(category, location).joinToString(" · ")
        }
        override fun imageUrl(): String? = activity.photos.firstOrNull()?.let(::buildPhotoUrl)
    }
}

@Composable
private fun AudioLibraryCard(
    item: AudioLibraryItem,
    lang: String,
    isFavorited: Boolean,
    onFavoriteClick: () -> Unit,
    onCardClick: () -> Unit,
    onPlayClick: () -> Unit
) {
    val colors = KompassTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(colors.colorWhite)
            .border(1.dp, colors.colorSurfaceMid.copy(alpha = 0.6f), RoundedCornerShape(24.dp))
            .clickable(onClick = onCardClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        AsyncImage(
            model = item.imageUrl(),
            contentDescription = item.title(lang),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(width = 112.dp, height = 86.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(colors.colorSurfaceMid)
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = item.title(lang),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 25.sp
                ),
                color = colors.colorNavy,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = item.meta(lang),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                ),
                color = colors.colorSlate.copy(alpha = 0.78f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FavoriteToggleButton(
                isFavorited = isFavorited,
                onClick = onFavoriteClick,
                size = 36.dp
            )
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(colors.colorOrangeMain)
                    .clickable(onClick = onPlayClick),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "▶",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = colors.colorWhite
                )
            }
        }
    }
}

@Composable
private fun AudioLibraryPlaceholder() {
    val colors = KompassTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(colors.colorWhite)
            .border(1.dp, colors.colorSurfaceMid.copy(alpha = 0.6f), RoundedCornerShape(24.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "No audio guides unlocked yet",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = colors.colorNavy
        )
        Text(
            text = "As soon as a premium place or activity includes narration, it will appear here for quick access and offline playback.",
            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
            color = colors.colorSlate.copy(alpha = 0.78f)
        )
    }
}
