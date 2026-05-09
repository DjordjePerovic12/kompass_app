package llc.bokadev.kompass.presentation.screens.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import llc.bokadev.kompass.core.presentation.base.BaseContentView
import llc.bokadev.kompass.core.util.currentAppLanguage
import llc.bokadev.kompass.domain.model.Experience
import llc.bokadev.kompass.domain.model.FavoriteItemType
import llc.bokadev.kompass.domain.model.Place
import llc.bokadev.kompass.presentation.screens.category_items_list.components.PlaceListItem
import llc.bokadev.kompass.presentation.screens.experiences.components.ActivityListItem
import llc.bokadev.kompass.presentation.shared.KompassSharedTopBar
import llc.bokadev.kompass.presentation.theme.KompassTheme
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun FavoritesScreen(
    onBack: () -> Unit,
    onPlaceClick: (String) -> Unit,
    onActivityClick: (String) -> Unit
) {
    val vm: FavoritesViewModel = koinViewModel()
    val state by vm.state.collectAsState()
    val lang = currentAppLanguage()

    BaseContentView(
        state = state,
        topBar = {
            KompassSharedTopBar(
                slug = "",
                title = "My Favorites",
                subtitle = "Saved places and activities",
                showBack = true,
                onBackClick = onBack
            )
        }
    ) {
        when {
            state.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(KompassTheme.colors.colorWhite),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.error ?: "Could not load favorites",
                        style = MaterialTheme.typography.bodyMedium,
                        color = KompassTheme.colors.colorSlate
                    )
                }
            }

            state.items.isEmpty() && !state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(KompassTheme.colors.colorWhite),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nothing saved yet",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = KompassTheme.colors.colorNavy
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(KompassTheme.colors.colorWhite),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(state.items, key = { it.stableKey }) { item ->
                        when (item) {
                            is FavoriteDisplayItem.PlaceItem -> {
                                PlaceListItem(
                                    place = item.place,
                                    lang = lang,
                                    bottomMetaOverride = item.distanceLabel ?: "Saved",
                                    isFavorited = true,
                                    onFavoriteClick = { vm.toggleFavorite(FavoriteItemType.PLACE, item.place.id) },
                                    onClick = { onPlaceClick(item.place.id) }
                                )
                            }

                            is FavoriteDisplayItem.ActivityItem -> {
                                ActivityListItem(
                                    activity = item.activity,
                                    lang = lang,
                                    bottomMetaOverride = item.distanceLabel ?: "Saved",
                                    isFavorited = true,
                                    onFavoriteClick = { vm.toggleFavorite(FavoriteItemType.ACTIVITY, item.activity.id) },
                                    onClick = { onActivityClick(item.activity.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

sealed interface FavoriteDisplayItem {
    val stableKey: String

    data class PlaceItem(
        val place: Place,
        val favoritedAtEpochMs: Long,
        val distanceKm: Double? = null
    ) : FavoriteDisplayItem {
        override val stableKey: String = "place-${place.id}"
        val distanceLabel: String?
            get() = distanceKm?.toDistanceLabel()
    }

    data class ActivityItem(
        val activity: Experience,
        val favoritedAtEpochMs: Long,
        val distanceKm: Double? = null
    ) : FavoriteDisplayItem {
        override val stableKey: String = "activity-${activity.id}"
        val distanceLabel: String?
            get() = distanceKm?.toDistanceLabel()
    }
}

private fun Double.toDistanceLabel(): String {
    return if (this < 1.0) {
        "${(this * 1000).toInt()} m away"
    } else {
        "${((this * 10).toInt() / 10.0)} km away"
    }
}
