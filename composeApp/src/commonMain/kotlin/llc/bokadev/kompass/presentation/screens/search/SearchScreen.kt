package llc.bokadev.kompass.presentation.screens.search

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import llc.bokadev.kompass.core.util.buildPhotoUrl
import llc.bokadev.kompass.core.util.currentAppLanguage
import llc.bokadev.kompass.domain.repository.FavoritesRepository
import llc.bokadev.kompass.domain.model.FavoriteKey
import llc.bokadev.kompass.domain.model.SearchEmptyStateKind
import llc.bokadev.kompass.domain.model.SearchFilterType
import llc.bokadev.kompass.domain.model.SearchResult
import llc.bokadev.kompass.domain.model.SearchResultType
import llc.bokadev.kompass.presentation.shared.FavoriteToggleButton
import llc.bokadev.kompass.presentation.shared.KompassSharedTopBar
import llc.bokadev.kompass.presentation.theme.KompassTheme
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SearchScreen(
    onBack: () -> Unit = {},
    onPlaceClick: (String) -> Unit = {},
    onActivityClick: (String) -> Unit = {},
    onEventClick: (String) -> Unit = {}
) {
    val vm: SearchViewModel = koinViewModel()
    val state by vm.state.collectAsState()
    val lang = currentAppLanguage()
    val favoritesRepository = koinInject<FavoritesRepository>()
    val favoriteEntries by favoritesRepository.favoritesFlow.collectAsState()
    val favoriteKeySet = favoritesRepository.getFavoriteKeySet()
    val strings = rememberSearchStrings(lang)
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(lang) {
        vm.load(lang)
    }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KompassTheme.colors.colorHomeCanvas)
    ) {
        KompassSharedTopBar(
            slug = "",
            title = strings.title,
            subtitle = strings.subtitle,
            showBack = true,
            onBackClick = onBack
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            TextField(
                value = state.query,
                onValueChange = { vm.onIntent(SearchEvent.QueryChanged(it)) },
                placeholder = {
                    Text(strings.placeholder, color = KompassTheme.colors.colorSlate)
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .clip(RoundedCornerShape(18.dp))
                    .border(
                        1.dp,
                        KompassTheme.colors.colorSignal.copy(alpha = 0.10f),
                        RoundedCornerShape(18.dp)
                    ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = KompassTheme.colors.colorWhite,
                    unfocusedContainerColor = KompassTheme.colors.colorWhite,
                    focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                    unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                    cursorColor = KompassTheme.colors.colorSignalStrong
                )
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SearchFilterChip(
                    label = strings.all,
                    isSelected = state.selectedType == SearchFilterType.ALL,
                    onClick = { vm.onIntent(SearchEvent.TypeChanged(SearchFilterType.ALL)) }
                )
                SearchFilterChip(
                    label = strings.places,
                    isSelected = state.selectedType == SearchFilterType.PLACE,
                    onClick = { vm.onIntent(SearchEvent.TypeChanged(SearchFilterType.PLACE)) }
                )
                SearchFilterChip(
                    label = strings.activities,
                    isSelected = state.selectedType == SearchFilterType.ACTIVITY,
                    onClick = { vm.onIntent(SearchEvent.TypeChanged(SearchFilterType.ACTIVITY)) }
                )
                SearchFilterChip(
                    label = strings.events,
                    isSelected = state.selectedType == SearchFilterType.EVENT,
                    onClick = { vm.onIntent(SearchEvent.TypeChanged(SearchFilterType.EVENT)) }
                )
            }
        }

        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = KompassTheme.colors.colorSignalStrong)
                }
            }
            state.results.isNotEmpty() -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(state.results, key = { "${it.result.type}-${it.result.id}" }) { item ->
                        val favoriteKey = item.result.favoriteKeyOrNull()
                        SearchResultCard(
                            item = item,
                            lang = lang,
                            onFavoriteClick = favoriteKey?.let { key ->
                                { vm.toggleFavorite(key) }
                            },
                            isFavorited = favoriteKey?.let { it in favoriteKeySet } ?: false,
                            onClick = {
                                when (item.result.type) {
                                    SearchResultType.PLACE -> onPlaceClick(item.result.id)
                                    SearchResultType.ACTIVITY -> onActivityClick(item.result.id)
                                    SearchResultType.EVENT -> onEventClick(item.result.id)
                                }
                            }
                        )
                    }
                    item { Spacer(Modifier.height(90.dp)) }
                }
            }
            else -> {
                SearchEmptyState(
                    kind = state.emptyStateKind,
                    error = state.error,
                    strings = strings
                )
            }
        }
    }
}

@Composable
private fun SearchResultCard(
    item: SearchListItem,
    lang: String,
    isFavorited: Boolean,
    onFavoriteClick: (() -> Unit)?,
    onClick: () -> Unit
) {
    val colors = KompassTheme.colors
    val result = item.result
    val metaLine = buildList {
        item.distanceKm
            ?.takeIf { it in 0.0..150.0 }
            ?.let { add(it.toDistanceLabel()) }
        result.categoryLabel?.takeIf { it.isNotBlank() }?.let { add(it) }
        result.zoneOrLocation?.takeIf { it.isNotBlank() }?.let { add(it.prettySearchLabel()) }
    }.joinToString(" · ")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(colors.colorWhite)
            .border(1.dp, colors.colorSignal.copy(alpha = 0.06f), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = result.imageUrl?.let { buildPhotoUrl(it) },
            contentDescription = result.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(width = 96.dp, height = 82.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(colors.colorSurfaceMid)
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = result.type.toUiLabel(lang),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    letterSpacing = 0.2.sp
                ),
                color = colors.colorSignalStrong.copy(alpha = 0.72f)
            )
            Text(
                text = result.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 24.sp
                ),
                color = colors.colorNavy,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (metaLine.isNotBlank()) {
                Text(
                    text = metaLine,
                    style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                    color = colors.colorSlateLight,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        onFavoriteClick?.let {
            FavoriteToggleButton(
                isFavorited = isFavorited,
                onClick = it,
                size = 36.dp
            )
        }
    }
}

@Composable
private fun SearchFilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = KompassTheme.colors
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(
                if (isSelected) colors.colorSignal.copy(alpha = 0.10f) else colors.colorWhite
            )
            .border(
                1.dp,
                if (isSelected) colors.colorSignal.copy(alpha = 0.22f) else colors.colorSignal.copy(alpha = 0.08f),
                RoundedCornerShape(999.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            color = if (isSelected) colors.colorSignalStrong else colors.colorSlate
        )
    }
}

@Composable
private fun SearchEmptyState(
    kind: SearchEmptyStateKind,
    error: String?,
    strings: SearchStrings
) {
    val colors = KompassTheme.colors
    val message = error ?: when (kind) {
        SearchEmptyStateKind.NONE -> strings.startTyping
        SearchEmptyStateKind.NO_RESULTS -> strings.noResults
        SearchEmptyStateKind.OFFLINE_REQUIRES_INTERNET -> strings.offlineRequiresInternet
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 26.sp),
            color = colors.colorSlate,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

private data class SearchStrings(
    val title: String,
    val subtitle: String,
    val placeholder: String,
    val all: String,
    val places: String,
    val activities: String,
    val events: String,
    val startTyping: String,
    val noResults: String,
    val offlineRequiresInternet: String
)

@Composable
private fun rememberSearchStrings(lang: String): SearchStrings = remember(lang) {
    when (lang) {
        "fr" -> SearchStrings(
            title = "Recherche",
            subtitle = "Lieux, activités et événements",
            placeholder = "Rechercher dans Kompass…",
            all = "Tout",
            places = "Lieux",
            activities = "Activités",
            events = "Événements",
            startTyping = "Commencez à taper pour explorer Kotor.",
            noResults = "Aucun résultat pour cette recherche.",
            offlineRequiresInternet = "Connectez-vous à Internet pour lancer une nouvelle recherche."
        )
        "tr" -> SearchStrings(
            title = "Ara",
            subtitle = "Yerler, aktiviteler ve etkinlikler",
            placeholder = "Kompass içinde ara…",
            all = "Tümü",
            places = "Yerler",
            activities = "Aktiviteler",
            events = "Etkinlikler",
            startTyping = "Kotor'u keşfetmek için yazmaya başlayın.",
            noResults = "Bu arama için sonuç bulunamadı.",
            offlineRequiresInternet = "Yeni bir arama yapmak için internete bağlanın."
        )
        "es" -> SearchStrings(
            title = "Buscar",
            subtitle = "Lugares, actividades y eventos",
            placeholder = "Buscar en Kompass…",
            all = "Todo",
            places = "Lugares",
            activities = "Actividades",
            events = "Eventos",
            startTyping = "Empieza a escribir para explorar Kotor.",
            noResults = "No hay resultados para esta búsqueda.",
            offlineRequiresInternet = "Conéctate a Internet para realizar una nueva búsqueda."
        )
        "de" -> SearchStrings(
            title = "Suche",
            subtitle = "Orte, Aktivitäten und Events",
            placeholder = "In Kompass suchen…",
            all = "Alle",
            places = "Orte",
            activities = "Aktivitäten",
            events = "Events",
            startTyping = "Beginne zu tippen, um Kotor zu entdecken.",
            noResults = "Keine Ergebnisse für diese Suche.",
            offlineRequiresInternet = "Verbinde dich mit dem Internet, um eine neue Suche zu starten."
        )
        else -> SearchStrings(
            title = "Search",
            subtitle = "Places, activities, and events",
            placeholder = "Search inside Kompass…",
            all = "All",
            places = "Places",
            activities = "Activities",
            events = "Events",
            startTyping = "Start typing to explore Kotor.",
            noResults = "No results matched this search.",
            offlineRequiresInternet = "Connect to the internet to run a new search."
        )
    }
}

private fun SearchResultType.toUiLabel(lang: String): String = when (this) {
    SearchResultType.PLACE -> when (lang) {
        "fr" -> "Lieu"
        "tr" -> "Yer"
        "es" -> "Lugar"
        "de" -> "Ort"
        else -> "Place"
    }
    SearchResultType.ACTIVITY -> when (lang) {
        "fr" -> "Activité"
        "tr" -> "Aktivite"
        "es" -> "Actividad"
        "de" -> "Aktivität"
        else -> "Activity"
    }
    SearchResultType.EVENT -> when (lang) {
        "fr" -> "Événement"
        "tr" -> "Etkinlik"
        "es" -> "Evento"
        "de" -> "Event"
        else -> "Event"
    }
}

private fun Double.toDistanceLabel(): String = when {
    this < 1.0 -> "${(this * 1000).toInt()} m away"
    else -> "${((this * 10).toInt() / 10.0)} km away"
}

private fun String.prettySearchLabel(): String =
    split('_', '-', ' ')
        .filter { it.isNotBlank() }
        .joinToString(" ") { token ->
            token.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
