package llc.bokadev.kompass.presentation.screens.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.util.lerp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import llc.bokadev.kompass.core.util.rememberAppStrings
import llc.bokadev.kompass.core.util.buildPhotoUrl
import llc.bokadev.kompass.core.util.currentAppLanguage
import llc.bokadev.kompass.domain.model.Event
import llc.bokadev.kompass.domain.model.FavoriteItemType
import llc.bokadev.kompass.domain.model.FavoriteKey
import llc.bokadev.kompass.domain.model.InfoNotice
import llc.bokadev.kompass.domain.model.NearbyPlace
import llc.bokadev.kompass.domain.model.Place
import llc.bokadev.kompass.domain.model.PlaceCategory
import llc.bokadev.kompass.domain.model.favoriteNearbyFirst
import llc.bokadev.kompass.domain.model.favoritePlacesFirst
import llc.bokadev.kompass.presentation.screens.home.components.CompactPlaceCard
import llc.bokadev.kompass.presentation.screens.home.components.EventCard
import llc.bokadev.kompass.presentation.screens.home.components.FeaturedPlaceCard
import llc.bokadev.kompass.presentation.screens.home.components.NearbyDiscoveryCard
import llc.bokadev.kompass.presentation.screens.home.components.SectionHeader
import llc.bokadev.kompass.presentation.theme.KompassTheme
import llc.bokadev.kompass.presentation.theme.colorDustySage
import llc.bokadev.kompass.presentation.theme.colorRoseClay
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import llc.bokadev.kompass.presentation.theme.helveticaBold48
import llc.bokadev.kompass.presentation.theme.helveticaMedium11
import llc.bokadev.kompass.presentation.theme.helveticaMedium16
import llc.bokadev.kompass.presentation.theme.helveticaRegular11
import llc.bokadev.kompass.presentation.theme.stomic48
import kotlin.math.roundToInt
import kotlin.math.absoluteValue
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Composable
fun HomeScreenContent(
    state: HomeState,
    favoriteKeySet: Set<FavoriteKey>,
    showOrientationLayer: Boolean,
    onboardingContext: String,
    onPlaceClick: (String) -> Unit,
    onEventClick: (String) -> Unit,
    onNoticeClick: (String) -> Unit,
    onEventsSeeAll: () -> Unit,
    onMustSeeSeeAll: () -> Unit,
    onNearbySeeAll: () -> Unit,
    onInfoCenterSeeAll: () -> Unit,
    onMyGuidesClick: () -> Unit,
    onSearchClick: () -> Unit,
    onChangeLanguageClick: () -> Unit,
    onPlaceFavoriteToggle: (String) -> Unit,
    onIntent: (HomeEvent) -> Unit
) {
    val colors = KompassTheme.colors
    val lang = currentAppLanguage()
    val strings = rememberAppStrings()

    if (state.isLoading &&
        state.mustSeePlaces.isEmpty() &&
        state.upcomingEvents.isEmpty() &&
        state.infoCenterNotices.isEmpty() &&
        state.nearbyPlaces.isEmpty()
    ) {
        HomeSplashLoader(strings.loadingKotor)
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.colorWhite)
            .verticalScroll(rememberScrollState())
    ) {
        HeroHeader(
            showMyGuides = true,
            onMyGuidesClick = onMyGuidesClick,
            onSearchClick = onSearchClick,
            onChangeLanguageClick = onChangeLanguageClick,
            strings = strings
        )
//        if (showOrientationLayer) {
//            OrientationLayer(
//                context = onboardingContext
//            )
//        }
        MustSeeSection(
            places = state.mustSeePlaces.favoritePlacesFirst(favoriteKeySet),
            isLoading = state.isLoading,
            lang = lang,
            onPlaceClick = onPlaceClick,
            favoriteKeySet = favoriteKeySet,
            onPlaceFavoriteToggle = onPlaceFavoriteToggle,
            onSeeAll = onMustSeeSeeAll,
            title = strings.mustSee,
            loadingLabel = strings.loadingLabel
        )
        if (state.nearbyPlaces.isNotEmpty()) {
            NearbyYouSection(
                nearbyPlaces = state.nearbyPlaces.favoriteNearbyFirst(favoriteKeySet),
                isLoading = state.isLoading,
                lang = lang,
                onPlaceClick = onPlaceClick,
                favoriteKeySet = favoriteKeySet,
                onPlaceFavoriteToggle = onPlaceFavoriteToggle,
                onSeeAll = onNearbySeeAll,
                title = if (state.isUsingCurrentLocation) strings.nearbyYou else "Around Old Town",
                emptyMessage = strings.nearbyEmpty,
                loadingLabel = strings.loadingLabel
            )
        }
        if (state.infoCenterNotices.isNotEmpty()) {
            InfoCenterPreviewSection(
                notices = state.infoCenterNotices,
                lang = lang,
                onNoticeClick = onNoticeClick,
                onSeeAll = onInfoCenterSeeAll,
                title = strings.recentNews
            )
        }
        UpcomingEventsSection(
            events = state.upcomingEvents,
            isLoading = state.isLoading,
            lang = lang,
            onEventClick = onEventClick,
            onSeeAll = onEventsSeeAll,
            title = strings.upcomingEvents,
            loadingLabel = strings.loadingLabel
        )
        Spacer(
            Modifier
                .fillMaxWidth()
                .height(90.dp)
                .background(colors.colorWhite)
        )
    }
}

@Composable
private fun OrientationLayer(
    context: String
) {
    val colors = KompassTheme.colors
    val nearbyLine = if (context == "already_here") {
        "Places around you shift as you move through the bay."
    } else {
        "Save your rhythm now, then let places around you rearrange themselves once you arrive."
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 18.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(colors.colorWhite)
            .border(1.dp, colors.colorSurfaceMid.copy(alpha = 0.72f), RoundedCornerShape(28.dp))
            .padding(horizontal = 18.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "A quiet orientation",
            style = helveticaMedium11(),
            color = colors.colorNavy.copy(alpha = 0.54f),
            letterSpacing = 1.8.sp
        )

        OrientationCallout(
            title = "Nearby exploration",
            body = nearbyLine,
            accent = colors.colorOrangeMain
        )
        OrientationCallout(
            title = "Saved places",
            body = "Save places quietly for later — before arriving or while wandering.",
            accent = colorDustySage
        )
        OrientationCallout(
            title = "Audio moments",
            body = "Some places include optional audio moments designed for slower exploration.",
            accent = colorRoseClay
        )
    }
}

@Composable
private fun OrientationCallout(
    title: String,
    body: String,
    accent: Color
) {
    val colors = KompassTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 6.dp)
                .size(10.dp)
                .clip(CircleShape)
                .background(accent)
        )

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    letterSpacing = (-0.2).sp
                ),
                color = colors.colorNavy
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                color = colors.colorNavy.copy(alpha = 0.72f)
            )
        }
    }
}

@Composable
private fun HomeSplashLoader(loadingText: String) {
    val colors = KompassTheme.colors
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.colorOrangeMain)
            .windowInsetsPadding(WindowInsets.statusBars),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(colors.colorHomePanel)
                    .border(1.dp, colors.colorHomePanelBorder, RoundedCornerShape(24.dp))
                    .padding(16.dp)
            ) {
                drawCircle(
                    color = colors.colorOrangeMain,
                    radius = size.minDimension * 0.12f,
                    center = center
                )
                drawCircle(
                    color = colors.colorHomeHeroSoft,
                    radius = size.minDimension * 0.28f,
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )
            }
            Text(
                text = "KOmpass",
                style = MaterialTheme.typography.headlineSmall,
                color = colors.colorHomePanel
            )
            Text(
                text = loadingText,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.colorHomeHeroSoft.copy(alpha = 0.84f)
            )
        }
    }
}

@Composable
private fun HeroHeader(
    showMyGuides: Boolean,
    onMyGuidesClick: () -> Unit,
    onSearchClick: () -> Unit,
    onChangeLanguageClick: () -> Unit,
    strings: llc.bokadev.kompass.core.util.AppStrings
) {
    val colors = KompassTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                colors.colorOrangeMain,
                RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp)
            )
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = strings.heroLocation,
                    style = helveticaMedium11(),
                    color = colors.colorNavy
                )
                Text(
                    text = strings.heroTitle,
                    style = helveticaBold48(),
                    lineHeight = 56.sp,
                    color = colors.colorWhite,
                    letterSpacing = 0.8.sp
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onChangeLanguageClick,
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(15.dp),
                            ambientColor = colors.colorOrangeMain.copy(alpha = 0.20f),
                            spotColor = colors.colorOrangeMain.copy(alpha = 0.24f)
                        )
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.colorHomePanel.copy(alpha = 0.96f))
                        .border(
                            1.dp,
                            colors.colorHomeHeroSoft.copy(alpha = 0.38f),
                            RoundedCornerShape(15.dp)
                        )
                ) {
                    Canvas(modifier = Modifier.size(22.dp)) {
                        drawCircle(
                            color = colors.colorOrangeMain,
                            radius = size.minDimension * 0.38f,
                            center = center,
                            style = Stroke(width = 1.6.dp.toPx())
                        )
                        drawLine(
                            color = colors.colorSignalStrong,
                            start = Offset(center.x, size.height * 0.16f),
                            end = Offset(center.x, size.height * 0.84f),
                            strokeWidth = 1.4.dp.toPx()
                        )
                        drawPath(
                            Path().apply {
                                moveTo(size.width * 0.18f, center.y)
                                quadraticTo(center.x, size.height * 0.22f, size.width * 0.82f, center.y)
                            },
                            color = colors.colorOrangeMain,
                            style = Stroke(width = 1.4.dp.toPx())
                        )
                        drawPath(
                            Path().apply {
                                moveTo(size.width * 0.18f, center.y)
                                quadraticTo(center.x, size.height * 0.78f, size.width * 0.82f, center.y)
                            },
                            color = colors.colorOrangeMain,
                            style = Stroke(width = 1.4.dp.toPx())
                        )
                    }
                }

                if (showMyGuides) {
                    IconButton(
                        onClick = onMyGuidesClick,
                        modifier = Modifier
                            .size(44.dp)
                            .shadow(
                                elevation = 8.dp,
                                shape = RoundedCornerShape(15.dp),
                                ambientColor = colors.colorOrangeMain.copy(alpha = 0.20f),
                                spotColor = colors.colorOrangeMain.copy(alpha = 0.24f)
                            )
                            .clip(RoundedCornerShape(15.dp))
                            .background(colors.colorHomePanel.copy(alpha = 0.96f))
                            .border(
                                1.dp,
                                colors.colorHomeHeroSoft.copy(alpha = 0.38f),
                                RoundedCornerShape(15.dp)
                            )
                    ) {
                        Canvas(modifier = Modifier.size(22.dp)) {
                            drawCircle(
                                color = colors.colorOrangeMain,
                                radius = size.minDimension * 0.18f,
                                center = center
                            )
                            drawCircle(
                                color = colors.colorOrangeMain,
                                radius = size.minDimension * 0.36f,
                                center = center,
                                style = Stroke(width = 2.dp.toPx())
                            )
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(24.dp),
                    ambientColor = colors.colorOrangeMain.copy(alpha = 0.10f),
                    spotColor = colors.colorOrangeMain.copy(alpha = 0.14f)
                )
                .clip(RoundedCornerShape(24.dp))
                .background(colors.colorHomePanel)
                .border(
                    1.dp,
                    colors.colorHomePanelBorder,
                    RoundedCornerShape(24.dp)
                )
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = strings.searchPlaceholder,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.colorHomeHeroMuted
                )
                Text(
                    text = strings.heroLocation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.colorOrangeMain
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.colorWhite)
                    .border(
                        1.dp,
                        colors.colorSignal.copy(alpha = 0.18f),
                        RoundedCornerShape(16.dp)
                    )
                    .clickable(onClick = onSearchClick)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Canvas(modifier = Modifier.size(17.dp)) {
                    drawCircle(
                        color = colors.colorSignalStrong,
                        radius = 5.dp.toPx(),
                        style = Stroke(width = 1.6.dp.toPx())
                    )
                    drawLine(
                        color = colors.colorSignalStrong,
                        start = Offset(size.width * 0.68f, size.height * 0.68f),
                        end = Offset(size.width * 0.96f, size.height * 0.96f),
                        strokeWidth = 1.6.dp.toPx()
                    )
                }
                Text(
                    text = strings.searchPlaceholder,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.colorSlate
                )
            }
        }
    }
}

@Composable
private fun MustSeeSection(
    places: List<Place>,
    isLoading: Boolean,
    lang: String,
    onPlaceClick: (String) -> Unit,
    favoriteKeySet: Set<FavoriteKey>,
    onPlaceFavoriteToggle: (String) -> Unit,
    onSeeAll: () -> Unit,
    title: String,
    loadingLabel: String
) {
    val colors = KompassTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.colorWhite)
            .padding(top = 20.dp, bottom = 26.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionHeader(title = title, onSeeAll = onSeeAll)
        if (isLoading) {
            LoadingText(loadingLabel)
        } else {
            val pagerState = rememberPagerState(pageCount = { places.size })
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                HorizontalPager(
                    state = pagerState,
                    contentPadding = PaddingValues(horizontal = 66.dp),
                    pageSpacing = (-124).dp,
                    modifier = Modifier.fillMaxWidth()
                ) { page ->
                    val place = places[page]
                    val signedOffset = (page - pagerState.currentPage) - pagerState.currentPageOffsetFraction
                    val pageOffset = signedOffset.absoluteValue
                    val normalizedOffset = pageOffset.coerceIn(0f, 1f)
                    val scale = lerp(0.92f, 1f, 1f - normalizedOffset)
                    val alpha = lerp(0.72f, 1f, 1f - normalizedOffset)
                    val horizontalPull = (signedOffset * -34f).dp
                    val verticalDrop = (normalizedOffset * 32f).dp

                    Box(
                        modifier = Modifier
                            .zIndex(1f - normalizedOffset)
                            .offset(x = horizontalPull, y = verticalDrop)
                            .scale(scale)
                            .alpha(alpha)
                    ) {
                        FeaturedPlaceCard(
                            name = place.localizedName(lang),
                            meta = place.toMustSeeMeta(short = true),
                            imageUrl = place.photos.firstOrNull()?.let { buildPhotoUrl(it) },
                            isFavorited = favoriteKeySet.contains(FavoriteKey(FavoriteItemType.PLACE, place.id)),
                            onFavoriteClick = { onPlaceFavoriteToggle(place.id) },
                            onClick = { onPlaceClick(place.id) }
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(places.size) { index ->
                            val selected = index == pagerState.currentPage
                            Box(
                                modifier = Modifier
                                    .height(8.dp)
                                    .width(if (selected) 28.dp else 8.dp)
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(
                                        if (selected) colors.colorOrangeMain else colors.colorNavy.copy(alpha = 0.18f)
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NearbyYouSection(
    nearbyPlaces: List<NearbyPlace>,
    isLoading: Boolean,
    lang: String,
    onPlaceClick: (String) -> Unit,
    favoriteKeySet: Set<FavoriteKey>,
    onPlaceFavoriteToggle: (String) -> Unit,
    onSeeAll: () -> Unit,
    title: String,
    emptyMessage: String,
    loadingLabel: String
) {
    val colors = KompassTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.colorWhite)
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionHeader(title = title, onSeeAll = onSeeAll)
        Column(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (isLoading && nearbyPlaces.isEmpty()) {
                LoadingText(loadingLabel)
            } else if (nearbyPlaces.isEmpty()) {
                Text(
                    text = emptyMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.colorSlate
                )
            } else {
                nearbyPlaces.firstOrNull()?.let { nearbyPlace ->
                    val place = nearbyPlace.place
                    NearbyDiscoveryCard(
                        name = place.localizedName(lang),
                        category = place.category.toEditorialPlaceCategory(),
                        zone = place.zone?.prettyUiLabel() ?: "Kotor",
                        distance = nearbyPlace.distanceKm.toNearbyDistanceLine(),
                        meta = nearbyPlace.toNearbyMeta(),
                        imageUrl = place.photos.firstOrNull()?.let { buildPhotoUrl(it) },
                        isFavorited = favoriteKeySet.contains(FavoriteKey(FavoriteItemType.PLACE, place.id)),
                        onFavoriteClick = { onPlaceFavoriteToggle(place.id) },
                        onClick = { onPlaceClick(place.id) }
                    )
                }

                nearbyPlaces.drop(1).take(3).forEach { nearbyPlace ->
                    val place = nearbyPlace.place
                    CompactPlaceCard(
                        name = place.localizedName(lang),
                        category = place.category.toEditorialPlaceCategory(),
                        zone = place.zone?.prettyUiLabel() ?: "Kotor",
                        distance = nearbyPlace.distanceKm.toNearbyDistanceLine(),
                        meta = nearbyPlace.toNearbyMeta(),
                        imageUrl = place.photos.firstOrNull()?.let { buildPhotoUrl(it) },
                        isFavorited = favoriteKeySet.contains(FavoriteKey(FavoriteItemType.PLACE, place.id)),
                        onFavoriteClick = { onPlaceFavoriteToggle(place.id) },
                        onClick = { onPlaceClick(place.id) }
                    )
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalTime::class)
private fun UpcomingEventsSection(
    events: List<Event>,
    isLoading: Boolean,
    lang: String,
    onEventClick: (String) -> Unit,
    onSeeAll: () -> Unit,
    title: String,
    loadingLabel: String
) {
    val colors = KompassTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.colorWhite)
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionHeader(title = title, onSeeAll = onSeeAll)
        Column(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (isLoading && events.isEmpty()) {
                LoadingText(loadingLabel)
            } else {
                events.forEach { event ->
                    EventCard(
                        name = event.localizedName(lang),
                        venue = event.localizedVenue(lang),
                        day = event.startTime.toHomeEventDay(),
                        month = event.startTime.toHomeEventMonth(),
                        meta = event.toHomeEventMeta(),
                        price = event.price,
                        onClick = { onEventClick(event.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoCenterPreviewSection(
    notices: List<InfoNotice>,
    lang: String,
    onNoticeClick: (String) -> Unit,
    onSeeAll: () -> Unit,
    title: String
) {
    val colors = KompassTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.colorWhite)
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionHeader(title = title, onSeeAll = onSeeAll)
        Column(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            notices.forEach { notice ->
                val metaLine = listOfNotNull(
                    notice.noticeType.prettyUiLabel(),
                    notice.startsAt?.toNoticeMetaTime(),
                    notice.localizedLocation(lang).ifBlank { null }
                ).joinToString(" · ")
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .shadow(10.dp, RoundedCornerShape(18.dp), ambientColor = Color.Black.copy(alpha = 0.12f), spotColor = Color.Black.copy(alpha = 0.16f))
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (notice.priorityRank() == 0) colors.colorOrangeMain else colors.colorNavy)
                        .clickable { onNoticeClick(notice.id) }
                ) {
                    if (!notice.imageUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = notice.imageUrl,
                            contentDescription = notice.localizedTitle(lang),
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            colorFilter = ColorFilter.colorMatrix(
                                ColorMatrix().apply { setToSaturation(0.7f) }
                            ),
                            alpha = 0.92f
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.22f),
                                        Color.Black.copy(alpha = 0.82f)
                                    ),
                                    startY = 36f
                                )
                            )
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        colors.colorOrangeMain.copy(alpha = 0.08f),
                                        colors.colorOrangeMain.copy(alpha = 0.20f)
                                    )
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(horizontal = 18.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = notice.noticeType.prettyUiLabel(),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                            color = colors.colorWhite.copy(alpha = 0.9f)
                        )
                        Text(
                            text = notice.localizedTitle(lang),
                            style = MaterialTheme.typography.titleLarge.copy(fontSize = 24.sp, lineHeight = 28.sp),
                            color = colors.colorWhite,
                            maxLines = 2
                        )
                        Text(
                            text = notice.localizedShortDescription(lang),
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp, lineHeight = 20.sp),
                            color = colors.colorWhite.copy(alpha = 0.86f),
                            maxLines = 2
                        )
                        if (metaLine.isNotBlank()) {
                            Text(
                                text = metaLine,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                                color = colors.colorWhite.copy(alpha = 0.72f),
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingText(label: String) {
    val colors = KompassTheme.colors
    Text(
        text = label,
        style = MaterialTheme.typography.bodyMedium,
        color = colors.colorSlate,
        modifier = Modifier.padding(horizontal = 20.dp)
    )
}

@OptIn(ExperimentalTime::class)
private fun String.toHomeEventDay(): String = runCatching {
    Instant.parse(this)
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .date
        .day
        .toString()
}.getOrDefault("--")

@OptIn(ExperimentalTime::class)
private fun String.toHomeEventMonth(): String = runCatching {
    Instant.parse(this)
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .date
        .month
        .name
        .take(3)
}.getOrDefault("---")

private fun Double.toDistanceLabel(): String {
    return if (this < 1.0) {
        "${(this * 1000).roundToInt()} m"
    } else {
        "${((this * 10).roundToInt() / 10.0)} km"
    }
}

private fun Double.toNearbyDistanceLine(): String {
    return if (this < 1.0) {
        "${(this * 1000).roundToInt()} m away"
    } else {
        "${((this * 10).roundToInt() / 10.0)} km away"
    }
}

private fun NearbyPlace.toNearbyMeta(): String {
    val distanceText = when {
        distanceKm < 1.0 -> "${(distanceKm * 12).roundToInt()} min walk"
        distanceKm <= 5.0 -> "${(distanceKm * 3).roundToInt()} min drive"
        else -> "${(distanceKm * 2.2).roundToInt()} min drive"
    }

    val bestTimeText = when (place.bestTime.name.lowercase()) {
        "morning" -> "Best in morning"
        "afternoon" -> "Best in afternoon"
        "evening" -> "Best in evening"
        "night" -> "Best at night"
        else -> null
    }

    return listOfNotNull(distanceText, bestTimeText).joinToString(" · ")
}

@OptIn(ExperimentalTime::class)
private fun String.toNoticeMetaTime(): String? = runCatching {
    val dateTime = Instant.parse(this).toLocalDateTime(TimeZone.currentSystemDefault())
    "${dateTime.date.day}. ${dateTime.date.month.name.take(3)}"
}.getOrNull()

private fun Event.toHomeEventMeta(): String {
    val start = startTime.toHomeEventTime()
    val end = endTime?.toHomeEventTime()
    return when {
        start != null && end != null -> "$start – $end"
        start != null -> start
        else -> ""
    }
}

private fun Place.toMustSeeMeta(short: Boolean = false): String {
    val categoryLabel = category.toEditorialPlaceCategory()
    val zoneLabel = zone?.takeIf { it.isNotBlank() }?.prettyUiLabel() ?: "Kotor"
    return if (short) {
        "$categoryLabel · $zoneLabel"
    } else {
        "$categoryLabel · $zoneLabel"
    }
}

private fun PlaceCategory.toEditorialPlaceCategory(): String = when (this) {
    PlaceCategory.EAT_AND_DRINK -> "Eat & Drink"
    PlaceCategory.SEE_AND_VISIT -> "Historic Site"
    PlaceCategory.ACTIVITIES -> "Activity"
    PlaceCategory.HIDDEN_GEMS -> "Hidden Gem"
    PlaceCategory.PRACTICAL -> "Practical"
}

private fun String.prettyUiLabel(): String =
    replace('_', ' ')
        .replace('-', ' ')
        .split(' ')
        .filter { it.isNotBlank() }
        .joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { char ->
                if (char.isLowerCase()) char.titlecase() else char.toString()
            }
        }

private fun String.toHomeEventCategory(): String =
    replace('_', ' ')
        .replace('-', ' ')
        .split(' ')
        .filter { it.isNotBlank() }
        .joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { char ->
                if (char.isLowerCase()) char.titlecase() else char.toString()
            }
        }

@OptIn(ExperimentalTime::class)
private fun String.toHomeEventTime(): String? = runCatching {
    Instant.parse(this)
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .time
        .let { time ->
            "${time.hour.toString().padStart(2, '0')}:${time.minute.toString().padStart(2, '0')}"
        }
}.getOrNull()
