package llc.bokadev.kompass.presentation.favorites

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kompass.composeapp.generated.resources.Res
import kompass.composeapp.generated.resources.redesign_remove_title_from_saved
import kompass.composeapp.generated.resources.redesign_saved
import kompass.composeapp.generated.resources.redesign_saved_subtitle
import kompass.composeapp.generated.resources.retry
import kompass.composeapp.generated.resources.saved_activity
import kompass.composeapp.generated.resources.saved_audio
import kompass.composeapp.generated.resources.saved_couldnt_refresh
import kompass.composeapp.generated.resources.saved_distances_in_bay
import kompass.composeapp.generated.resources.saved_empty_body
import kompass.composeapp.generated.resources.saved_empty_title
import kompass.composeapp.generated.resources.saved_explore_kotor
import kompass.composeapp.generated.resources.saved_group_heading
import kompass.composeapp.generated.resources.saved_group_other
import kompass.composeapp.generated.resources.saved_remove
import kompass.composeapp.generated.resources.saved_saved_count
import kompass.composeapp.generated.resources.saved_unavailable
import kompass.composeapp.generated.resources.saved_unavailable_name
import llc.bokadev.kompass.core.utils.AppStrings
import llc.bokadev.kompass.core.utils.LocalAppLocale
import llc.bokadev.kompass.core.utils.appStringsFor
import llc.bokadev.kompass.core.utils.buildPhotoUrl
import llc.bokadev.kompass.core.utils.localizedTypeLabel
import llc.bokadev.kompass.core.utils.toTitleCaseLabel
import llc.bokadev.kompass.core.utils.uppercaseFor
import llc.bokadev.kompass.domain.model.FavoriteEntry
import llc.bokadev.kompass.domain.model.FavoriteItemType
import llc.bokadev.kompass.domain.model.SavedSnapshot
import llc.bokadev.kompass.presentation.favorites.components.SavedCard
import llc.bokadev.kompass.presentation.favorites.components.SavedEmptyState
import llc.bokadev.kompass.presentation.favorites.components.UnavailableSavedCard
import llc.bokadev.kompass.presentation.screens.essentials.formatDistance
import llc.bokadev.kompass.presentation.screens.home.HomeArea
import llc.bokadev.kompass.presentation.screens.placedetail.toVisitLength
import llc.bokadev.kompass.presentation.shared.GROUP_OTHER
import llc.bokadev.kompass.presentation.shared.LamplightHeader
import llc.bokadev.kompass.ui.theme.KompassShapes
import llc.bokadev.kompass.ui.theme.KompassTheme
import llc.bokadev.kompass.ui.theme.pageGradientBackground
import org.jetbrains.compose.resources.stringResource

/**
 * Lamplight Saved (Saved spec): favorites grouped by area, most recently saved first, with
 * distances only once the user is in the bay. Presentation only; favorites storage is unchanged.
 * The shared type/visit-length/area labels still take the AppStrings tables until they are migrated.
 */
@Composable
fun FavoritesScreenContent(
    modifier: Modifier = Modifier,
    state: FavoritesState,
    onLanguageClick: () -> Unit,
    onRetryClick: () -> Unit,
    onExploreKotorClick: () -> Unit,
    onItemClick: (FavoriteItemType, String) -> Unit,
    onRemoveClick: (FavoriteEntry, String) -> Unit
) {
    // Language from the composition (AppEnvironment), not Koin, so the content stays previewable.
    val lang = LocalAppLocale.current.substringBefore('-')
    val strings = appStringsFor(lang)
    val colors = KompassTheme.colors
    val otherLabel = stringResource(Res.string.saved_group_other)

    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        LamplightHeader(searchPlaceholder = null, onLanguageClick = onLanguageClick, onSearchClick = null) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = stringResource(Res.string.redesign_saved), style = KompassTheme.typography.gloockRegular40, color = colors.nightBlack, modifier = Modifier.semantics { heading() })
                Text(
                    text = stringResource(Res.string.redesign_saved_subtitle),
                    style = KompassTheme.typography.geologicaMedium16.copy(fontSize = 15.sp, fontWeight = FontWeight.Medium),
                    color = colors.nightBlack
                )
            }
        }

        Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 18.dp)) {
            if (!state.isLoading && state.groups.isEmpty()) {
                SavedEmptyState(
                    title = stringResource(Res.string.saved_empty_title),
                    body = stringResource(Res.string.saved_empty_body),
                    actionLabel = stringResource(Res.string.saved_explore_kotor),
                    onExplore = onExploreKotorClick
                )
                return@Column
            }
            if (state.groups.isNotEmpty()) {
                Text(
                    text = stringResource(Res.string.saved_saved_count, state.savedCount.toString()),
                    style = KompassTheme.typography.geologicaRegular14.copy(fontWeight = FontWeight.SemiBold),
                    color = colors.boneWhite
                )
                if (!state.inRegion) {
                    Text(text = stringResource(Res.string.saved_distances_in_bay), style = KompassTheme.typography.geologicaRegular14, color = KompassTheme.colors.white85)
                }
                if (state.refreshFailed) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                        Text(
                            text = stringResource(Res.string.saved_couldnt_refresh),
                            style = KompassTheme.typography.geologicaRegular14,
                            color = KompassTheme.colors.white85,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = stringResource(Res.string.retry),
                            style = KompassTheme.typography.geologicaRegular14.copy(fontWeight = FontWeight.SemiBold),
                            color = colors.boneWhite,
                            modifier = Modifier
                                .heightIn(min = 44.dp)
                                .clip(KompassShapes.pill)
                                .clickable(role = Role.Button, onClick = onRetryClick)
                                .padding(horizontal = 12.dp, vertical = 12.dp)
                        )
                    }
                }
            }
            state.groups.forEach { group ->
                val area = group.key.groupLabel(strings, otherLabel)
                val groupHeading = stringResource(Res.string.saved_group_heading, area, group.items.size.toString())
                Row(
                    modifier = Modifier
                        .padding(top = 26.dp, bottom = 10.dp)
                        .clearAndSetSemantics {
                            heading()
                            contentDescription = groupHeading
                        },
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = area, style = KompassTheme.typography.gloockRegular26, color = colors.boneWhite)
                    Text(text = group.items.size.toString(), style = KompassTheme.typography.geologicaRegular13, color = KompassTheme.colors.white85, modifier = Modifier.padding(bottom = 4.dp))
                }
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    group.items.forEach { item -> SavedItemRow(item, area, lang, strings, onItemClick, onRemoveClick) }
                }
            }
        }
        // The shell already insets content above the floating bar; this is the spec's breathing room.
        Spacer(Modifier.height(26.dp))
    }
}

@Composable
private fun SavedItemRow(
    item: SavedItem,
    area: String,
    lang: String,
    strings: AppStrings,
    onItemClick: (FavoriteItemType, String) -> Unit,
    onRemoveClick: (FavoriteEntry, String) -> Unit
) {
    val unavailableName = stringResource(Res.string.saved_unavailable_name)
    val title = item.place?.localizedName(lang)
        ?: item.activity?.localizedName(lang)
        ?: item.snapshot?.name?.let { it[lang] ?: it["en"] }?.takeIf { it.isNotBlank() }

    if (item.unavailable) {
        UnavailableSavedCard(
            title = title ?: unavailableName,
            subtitle = stringResource(Res.string.saved_unavailable),
            removeLabel = stringResource(Res.string.saved_remove),
            onRemove = { onRemoveClick(item.entry, title ?: unavailableName) }
        )
        return
    }

    val displayTitle = title ?: unavailableName
    val activityLabel = stringResource(Res.string.saved_activity)
    val audioLabel = stringResource(Res.string.saved_audio)
    val typeLabel = when {
        item.place != null -> item.place.localizedTypeLabel(strings).uppercaseFor(lang)
        item.activity != null -> listOfNotNull(activityLabel, item.activity.category?.toTitleCaseLabel()).joinToString(" · ").uppercaseFor(lang)
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
        audioLabel = audioLabel,
        distance = distance,
        hint = hint,
        accessibilityDescription = listOfNotNull(displayTitle, typeLabel, area, audioLabel.takeIf { hasAudio }, distance).joinToString(", "),
        removeDescription = stringResource(Res.string.redesign_remove_title_from_saved, displayTitle),
        onOpen = { onItemClick(item.entry.type, item.entry.id) },
        onRemove = { onRemoveClick(item.entry, displayTitle) }
    )
}

private fun String.groupLabel(strings: AppStrings, otherLabel: String): String = when (this) {
    GROUP_OTHER -> otherLabel
    else -> HomeArea.entries.firstOrNull { it.preferenceValue == this }?.shortLabel(strings) ?: toTitleCaseLabel()
}

@Preview
@Composable
private fun FavoritesScreenContentPreview() {
    KompassTheme {
        FavoritesScreenContent(
            modifier = Modifier.pageGradientBackground(),
            state = FavoritesState(
                isLoading = false,
                groups = listOf(
                    SavedGroup(
                        key = "old_town",
                        items = listOf(
                            SavedItem(
                                entry = FavoriteEntry(FavoriteItemType.PLACE, "cathedral", 2),
                                snapshot = SavedSnapshot(mapOf("en" to "Cathedral of Saint Tryphon"), "old_town"),
                                status = SavedItemStatus.NOT_REFRESHED,
                                groupKey = "old_town"
                            ),
                            SavedItem(
                                entry = FavoriteEntry(FavoriteItemType.PLACE, "gone", 1),
                                snapshot = SavedSnapshot(mapOf("en" to "Church of Saint Luke"), "old_town"),
                                status = SavedItemStatus.UNAVAILABLE,
                                groupKey = "old_town"
                            )
                        )
                    )
                ),
                refreshFailed = true
            ),
            onLanguageClick = {},
            onRetryClick = {},
            onExploreKotorClick = {},
            onItemClick = { _, _ -> },
            onRemoveClick = { _, _ -> }
        )
    }
}
