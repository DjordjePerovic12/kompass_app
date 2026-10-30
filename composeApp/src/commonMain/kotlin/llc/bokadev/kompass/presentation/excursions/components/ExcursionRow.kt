package llc.bokadev.kompass.presentation.excursions.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import kompass.composeapp.generated.resources.Res
import kompass.composeapp.generated.resources.excursions_activity
import kompass.composeapp.generated.resources.excursions_arrange_ahead
import kompass.composeapp.generated.resources.excursions_culture
import kompass.composeapp.generated.resources.excursions_full_day
import kompass.composeapp.generated.resources.excursions_half_day
import kompass.composeapp.generated.resources.excursions_hiking
import kompass.composeapp.generated.resources.excursions_independent
import kompass.composeapp.generated.resources.excursions_nature
import kompass.composeapp.generated.resources.excursions_place
import kompass.composeapp.generated.resources.excursions_short
import kompass.composeapp.generated.resources.excursions_water
import kompass.composeapp.generated.resources.redesign_remove_title_from_saved
import kompass.composeapp.generated.resources.redesign_save_title
import llc.bokadev.kompass.core.utils.LocalAppLocale
import llc.bokadev.kompass.core.utils.buildPhotoUrl
import llc.bokadev.kompass.domain.model.BestTime
import llc.bokadev.kompass.domain.model.Excursion
import llc.bokadev.kompass.domain.model.Place
import llc.bokadev.kompass.domain.model.PlaceCategory
import llc.bokadev.kompass.presentation.shared.icons.BookmarkIcon
import llc.bokadev.kompass.ui.theme.KompassTheme
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** One Worth the Drive (or related-activity) row: photo, type, name, outing metadata and a save button. */
@Composable
fun ExcursionRow(entry: Excursion, saved: Boolean, onFavorite: () -> Unit, onClick: () -> Unit) {
    val colors = KompassTheme.colors
    val language = LocalAppLocale.current.substringBefore('-')
    val name = entry.place?.localizedName(language) ?: entry.activity!!.localizedName(language)
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.weight(1f).clickable(onClick = onClick).padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                entry.photo?.let { photo ->
                    AsyncImage(buildPhotoUrl(photo), contentDescription = null, contentScale = ContentScale.Crop,
                        modifier = Modifier.size(width = 96.dp, height = 112.dp).background(colors.slateDark))
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        stringResource(if (entry.place != null) Res.string.excursions_place else Res.string.excursions_activity),
                        color = colors.lampYellow,
                        style = KompassTheme.typography.geologicaMedium16
                    )
                    Text(name, color = colors.boneWhite, style = KompassTheme.typography.gloockRegular26)
                    val metadata = listOfNotNull(entry.timeBand, entry.planningLevel)
                        .mapNotNull(::excursionLabelRes)
                        .map { stringResource(it) }
                        .filter(String::isNotBlank)
                    if (metadata.isNotEmpty()) Text(metadata.joinToString(" · "), color = colors.stoneGray, style = KompassTheme.typography.geologicaMedium16)
                }
            }
            val saveLabel = stringResource(if (saved) Res.string.redesign_remove_title_from_saved else Res.string.redesign_save_title, name)
            IconButton(onClick = onFavorite, modifier = Modifier.size(48.dp).semantics { contentDescription = saveLabel }) {
                BookmarkIcon(filled = saved, color = if (saved) colors.lampYellow else colors.stoneGray, size = 20.dp)
            }
        }
        HorizontalDivider(color = colors.slateDivider, modifier = Modifier.padding(top = 14.dp))
    }
}

/** Label for an interest, time band or planning level key; `null` for keys the app doesn't know. */
internal fun excursionLabelRes(value: String): StringResource? = when (value) {
    "culture" -> Res.string.excursions_culture
    "nature" -> Res.string.excursions_nature
    "hiking" -> Res.string.excursions_hiking
    "water" -> Res.string.excursions_water
    "short" -> Res.string.excursions_short
    "half_day" -> Res.string.excursions_half_day
    "full_day" -> Res.string.excursions_full_day
    "independent" -> Res.string.excursions_independent
    "arrange_ahead" -> Res.string.excursions_arrange_ahead
    else -> null
}

@Preview
@Composable
private fun ExcursionRowPreview() {
    val lovcen = Place(
        id = "lovcen", cityId = "kotor", name = mapOf("en" to "Lovćen Mausoleum"), description = emptyMap(),
        longDescription = emptyMap(), localsTip = emptyMap(), category = PlaceCategory.SEE_AND_VISIT, subCategory = null,
        priceIndicator = null, latitude = 42.3995, longitude = 18.8378, zone = "lovcen", tags = emptyList(),
        bestTime = BestTime.MORNING, estimatedDuration = null, openingHours = null, menuUrl = null, bookingPhone = null,
        photos = emptyList(), audioFile = null, audioAccessTier = "free", detailAccessTier = "free",
        isMustSee = false, isActive = true, sortOrder = 0
    )
    KompassTheme {
        ExcursionRow(Excursion("lovcen", place = lovcen, timeBand = "half_day"), saved = false, onFavorite = {}, onClick = {})
    }
}
