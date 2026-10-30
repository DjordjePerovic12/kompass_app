package llc.bokadev.kompass.presentation.screens.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import llc.bokadev.kompass.core.utils.AppStrings
import llc.bokadev.kompass.core.utils.buildPhotoUrl
import llc.bokadev.kompass.core.utils.currentAppLanguage
import llc.bokadev.kompass.core.utils.localizedTypeLabel
import llc.bokadev.kompass.core.utils.rememberAppStrings
import llc.bokadev.kompass.core.utils.toTitleCaseLabel
import llc.bokadev.kompass.core.utils.uppercaseFor
import llc.bokadev.kompass.domain.model.FavoriteEntry
import llc.bokadev.kompass.domain.model.FavoriteItemType
import llc.bokadev.kompass.presentation.screens.essentials.formatDistance
import llc.bokadev.kompass.presentation.screens.favorites.components.SavedCard
import llc.bokadev.kompass.presentation.screens.favorites.components.SavedEmptyState
import llc.bokadev.kompass.presentation.screens.favorites.components.UnavailableSavedCard
import llc.bokadev.kompass.presentation.screens.home.HomeArea
import llc.bokadev.kompass.presentation.screens.placedetail.toVisitLength
import llc.bokadev.kompass.presentation.shared.LamplightHeader
import llc.bokadev.kompass.ui.theme.KompassTheme
import llc.bokadev.kompass.ui.theme.KompassShapes
import llc.bokadev.kompass.ui.theme.pageGradientBackground

/**
 * Lamplight Saved (Saved spec): favorites grouped by area, most recently saved first, with
 * distances only once the user is in the bay. Presentation only; favorites storage is unchanged.
 */
@Composable
fun FavoritesScreenContent(state: FavoritesState, onAction: (FavoritesAction) -> Unit) {
    val strings = rememberAppStrings()
    val lang = currentAppLanguage()
    val colors = KompassTheme.colors

    Column(Modifier.fillMaxSize().pageGradientBackground().verticalScroll(rememberScrollState())) {
        LamplightHeader(searchPlaceholder = null, onLanguageClick = { onAction(FavoritesAction.OpenLanguage) }, onSearchClick = null) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = strings.redesign.saved, style = KompassTheme.typography.gloockRegular40, color = colors.nightBlack, modifier = Modifier.semantics { heading() })
                Text(
                    text = strings.redesign.savedSubtitle,
                    style = KompassTheme.typography.geologicaMedium16.copy(fontSize = 15.sp, fontWeight = FontWeight.Medium),
                    color = colors.nightBlack
                )
            }
        }

        Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 18.dp)) {
            if (!state.isLoading && state.groups.isEmpty()) {
                SavedEmptyState(
                    title = strings.saved.emptyTitle,
                    body = strings.saved.emptyBody,
                    actionLabel = strings.saved.exploreKotor,
                    onExplore = { onAction(FavoritesAction.ExploreKotor) }
                )
                return@Column
            }
            if (state.groups.isNotEmpty()) {
                Text(
                    text = strings.saved.savedCount.replace("{n}", state.savedCount.toString()),
                    style = KompassTheme.typography.geologicaRegular14.copy(fontWeight = FontWeight.SemiBold),
                    color = colors.boneWhite
                )
                if (!state.inRegion) {
                    Text(text = strings.saved.distancesInBay, style = KompassTheme.typography.geologicaRegular14, color = KompassTheme.colors.white85)
                }
                if (state.refreshFailed) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                        Text(
                            text = strings.saved.couldntRefresh,
                            style = KompassTheme.typography.geologicaRegular14,
                            color = KompassTheme.colors.white85,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = strings.retry,
                            style = KompassTheme.typography.geologicaRegular14.copy(fontWeight = FontWeight.SemiBold),
                            color = colors.boneWhite,
                            modifier = Modifier
                                .heightIn(min = 44.dp)
                                .clip(KompassShapes.pill)
                                .clickable(role = Role.Button) { onAction(FavoritesAction.Load) }
                                .padding(horizontal = 12.dp, vertical = 12.dp)
                        )
                    }
                }
            }
            state.groups.forEach { group ->
                val area = group.key.groupLabel(strings)
                Row(
                    modifier = Modifier
                        .padding(top = 26.dp, bottom = 10.dp)
                        .clearAndSetSemantics {
                            heading()
                            contentDescription = strings.saved.groupHeading.replace("{area}", area).replace("{n}", group.items.size.toString())
                        },
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = area, style = KompassTheme.typography.gloockRegular26, color = colors.boneWhite)
                    Text(text = group.items.size.toString(), style = KompassTheme.typography.geologicaRegular13, color = KompassTheme.colors.white85, modifier = Modifier.padding(bottom = 4.dp))
                }
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    group.items.forEach { item -> SavedItemRow(item, area, lang, strings, onAction) }
                }
            }
        }
        // The shell already insets content above the floating bar; this is the spec's breathing room.
        Spacer(Modifier.height(26.dp))
    }
}

@Composable
private fun SavedItemRow(item: SavedItem, area: String, lang: String, strings: AppStrings, onAction: (FavoritesAction) -> Unit) {
    val s = strings.saved
    val title = item.place?.localizedName(lang)
        ?: item.activity?.localizedName(lang)
        ?: item.snapshot?.name?.let { it[lang] ?: it["en"] }?.takeIf { it.isNotBlank() }

    if (item.unavailable) {
        UnavailableSavedCard(
            title = title ?: s.unavailableName,
            subtitle = s.unavailable,
            removeLabel = s.remove,
            onRemove = { onAction(FavoritesAction.Remove(item.entry, title ?: s.unavailableName)) }
        )
        return
    }

    val displayTitle = title ?: s.unavailableName
    val typeLabel = when {
        item.place != null -> item.place.localizedTypeLabel(strings).uppercaseFor(lang)
        item.activity != null -> listOfNotNull(s.activity, item.activity.category?.toTitleCaseLabel()).joinToString(" · ").uppercaseFor(lang)
        else -> null
    }
    val hasAudio = item.place?.localizedAudioFile(lang) != null || item.activity?.localizedAudioFile(lang) != null
    val distance = item.distanceKm?.let { (it * 1000).toInt().formatDistance() }
    // One practical hint, only when the CMS has it: the visit / activity length. Never invented.
    val hint = (item.activity?.durationMin ?: item.place?.estimatedDuration)?.toVisitLength(strings.detail)
    val photo = item.place?.photos?.firstOrNull() ?: item.activity?.photos?.firstOrNull() ?: item.snapshot?.photo

    SavedCard(
        title = displayTitle,
        typeLabel = typeLabel,
        imageUrl = photo?.let(::buildPhotoUrl),
        hasAudio = hasAudio,
        audioLabel = s.audio,
        distance = distance,
        hint = hint,
        accessibilityDescription = listOfNotNull(displayTitle, typeLabel, area, s.audio.takeIf { hasAudio }, distance).joinToString(", "),
        removeDescription = strings.redesign.removeTitleFromSaved.replace("{title}", displayTitle),
        onOpen = {
            onAction(FavoritesAction.Open(item.entry.type, item.entry.id))
        },
        onRemove = { onAction(FavoritesAction.Remove(item.entry, displayTitle)) }
    )
}

private fun String.groupLabel(strings: AppStrings): String = when (this) {
    GROUP_OTHER -> strings.saved.groupOther
    else -> HomeArea.entries.firstOrNull { it.preferenceValue == this }?.shortLabel(strings) ?: toTitleCaseLabel()
}
