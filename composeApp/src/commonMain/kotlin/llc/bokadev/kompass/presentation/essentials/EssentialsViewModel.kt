package llc.bokadev.kompass.presentation.essentials

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import llc.bokadev.kompass.core.navigation.ALL_CATEGORIES
import llc.bokadev.kompass.core.navigation.Screen
import llc.bokadev.kompass.core.navigation.utils.Navigator
import llc.bokadev.kompass.core.utils.AppPreferences
import llc.bokadev.kompass.core.utils.networking.Resource
import llc.bokadev.kompass.core.utils.networking.getOrNull
import llc.bokadev.kompass.domain.location.UserLocationProvider
import llc.bokadev.kompass.domain.model.CityEssential
import llc.bokadev.kompass.domain.model.EssentialCategory
import llc.bokadev.kompass.domain.model.GeoPoint
import llc.bokadev.kompass.domain.model.Utility
import llc.bokadev.kompass.domain.repository.AnalyticsRepository
import llc.bokadev.kompass.domain.usecase.GetEssentialsUseCase
import llc.bokadev.kompass.domain.usecase.GetUtilitiesUseCase
import llc.bokadev.kompass.presentation.base.BaseViewModel
import llc.bokadev.kompass.presentation.essentials.utils.BUNDLED_EMERGENCY_NUMBERS
import llc.bokadev.kompass.presentation.essentials.utils.EmergencyNumber
import llc.bokadev.kompass.presentation.essentials.utils.EssentialGroup
import llc.bokadev.kompass.presentation.essentials.utils.KOTOR_OLD_TOWN_CENTER
import llc.bokadev.kompass.presentation.essentials.utils.UtilityCategorySection
import llc.bokadev.kompass.presentation.essentials.utils.buildUtilitySection
import llc.bokadev.kompass.presentation.essentials.utils.hasSubstance
import llc.bokadev.kompass.presentation.essentials.utils.orderIndex
import llc.bokadev.kompass.presentation.essentials.utils.parseEmergencyNumbers
import llc.bokadev.kompass.presentation.shared.EssentialGroupKind
import llc.bokadev.kompass.presentation.shared.ExternalLaunch
import llc.bokadev.kompass.presentation.shared.LanguageSheetHost

/**
 * Serves both the Essentials tab and Screen.EssentialModuleScreen(id): the route's "id" arrives
 * through the [SavedStateHandle] and opens the screen focused on that module.
 */
class EssentialsViewModel(
    private val getEssentials: GetEssentialsUseCase,
    private val getUtilities: GetUtilitiesUseCase,
    private val userLocationProvider: UserLocationProvider,
    private val appPreferences: AppPreferences,
    private val savedState: SavedStateHandle,
    private val navigator: Navigator,
    private val languageSheet: LanguageSheetHost,
    private val analytics: AnalyticsRepository
) : BaseViewModel<EssentialsState, EssentialsEvent>() {

    override val initialViewState = EssentialsState(
        selectedTab = savedState.get<String>("selectedTab")?.let {
            runCatching { EssentialsTab.valueOf(it) }.getOrNull()
        } ?: EssentialsTab.INFO,
        expandedIds = savedState.get<List<String>>("expandedIds")?.toSet() ?: emptySet(),
        reminderHidden = appPreferences.getString(REMINDER_HIDDEN_KEY) == "true",
        // Screen.EssentialModuleScreen(id) — Search opened one module.
        focusModuleId = savedState.get<String>("id")
    )
    private var hasLoadedInitialData = false

    private var cachedUtilities = emptyList<Utility>()
    private var cachedEssentials: List<CityEssential>? = null
    private var loadJob: Job? = null

    init {
        // The language is changed from this tab's own header: re-localize what was built from it.
        viewModelScope.launch {
            appPreferences.selectedLanguageFlow.drop(1).collect { lang ->
                cachedEssentials?.let { loaded -> state.update { it.withEssentials(loaded, lang) } }
            }
        }
    }

    override fun dismissError() {
        state.update { it.copy(error = null) }
    }

    /**
     * Re-reads the location every 5 s. Merged into [essentialsState], so it polls only while the
     * screen collects the state (and stops with the WhileSubscribed timeout).
     */
    private val locationRefresh: Flow<Nothing> = flow {
        while (true) {
            delay(5_000L)
            val location = currentLocationOrNull()
            state.update { it.copy(currentLocation = location, sections = sectionsFor(location)) }
        }
    }

    val essentialsState = merge(state, locationRefresh)
        .onStart {
            if (!hasLoadedInitialData) {
                hasLoadedInitialData = true
                analytics.trackScreenView("essentials")
                load()
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = state.value
        )

    fun onEvent(event: EssentialsEvent) {
        when (event) {
            is EssentialsEvent.OnTabSelect -> {
                savedState["selectedTab"] = event.tab.name
                state.update { it.copy(selectedTab = event.tab) }
            }

            is EssentialsEvent.OnItemToggle -> setExpanded(event.id, event.id !in state.value.expandedIds)
            is EssentialsEvent.OnItemExpand -> setExpanded(event.id, true)
            EssentialsEvent.OnHideReminderClick -> {
                appPreferences.setString(REMINDER_HIDDEN_KEY, "true")
                state.update { it.copy(reminderHidden = true) }
            }

            EssentialsEvent.OnRetryClick -> load()
            EssentialsEvent.OnLanguageClick -> languageSheet.open()
            is EssentialsEvent.OnUtilityMapClick -> viewModelScope.launch {
                navigator.navigateTo(Screen.UtilityMapScreen(event.category?.name ?: ALL_CATEGORIES, event.selectedId))
            }

            is EssentialsEvent.OnDialClick -> openExternal(ExternalLaunch.Dial(event.number))
            is EssentialsEvent.OnLocationClick -> openExternal(ExternalLaunch.Maps(event.latitude, event.longitude, event.title))
            is EssentialsEvent.OnLinkClick -> openExternal(ExternalLaunch.Url(event.url))
            EssentialsEvent.OnTurnOnLocationClick -> openExternal(ExternalLaunch.AppSettings)
            EssentialsEvent.NavigateBack -> viewModelScope.launch {
                navigator.navigateUp()
            }

            is EssentialsEvent.OpenExternal -> Unit
        }
    }

    private fun openExternal(launch: ExternalLaunch) {
        viewModelScope.launch {
            event.emit(EssentialsEvent.OpenExternal(launch))
        }
    }

    private fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            state.update { it.copy(isLoading = it.groups.isEmpty() && it.sections.isEmpty()) }
            val location = async { currentLocationOrNull() }
            val essentials = async { getEssentials() }
            val utilities = async { getUtilities() }

            val currentLocation = location.await()
            val essentialsResult = essentials.await()
            val utilitiesResult = utilities.await()
            cachedUtilities = utilitiesResult.getOrNull() ?: cachedUtilities
            val lang = appPreferences.getSelectedLanguage()

            val loaded = essentialsResult.getOrNull()
            if (loaded != null) cachedEssentials = loaded

            state.update { current ->
                (loaded?.let { current.withEssentials(it, lang) } ?: current).copy(
                    isLoading = false,
                    essentialsFailed = loaded == null,
                    sections = sectionsFor(currentLocation),
                    utilitiesFailed = utilitiesResult is Resource.Error && cachedUtilities.isEmpty(),
                    currentLocation = currentLocation
                )
            }
        }
    }

    private fun EssentialsState.withEssentials(loaded: List<CityEssential>, lang: String) = copy(
        priority = priorityOf(loaded, lang),
        emergencyNumbers = emergencyNumbersOf(loaded, lang),
        groups = groupsOf(loaded, lang)
    )

    private suspend fun currentLocationOrNull(): GeoPoint? =
        if (!userLocationProvider.hasPermission()) null
        else runCatching { userLocationProvider.getCurrentLocation() }.getOrNull()

    /** Ranked from the device when known, else from Old Town (distances are hidden in that case). */
    private fun sectionsFor(location: GeoPoint?): List<UtilityCategorySection> =
        cachedUtilities
            .filter { it.latitude != null && it.longitude != null }
            .groupBy { it.category }
            .mapNotNull { (category, items) ->
                buildUtilitySection(category, items, location ?: KOTOR_OLD_TOWN_CENTER, location)
            }
            .sortedBy { it.category.orderIndex() }

    private fun setExpanded(id: String, expanded: Boolean) {
        state.update { it.copy(expandedIds = if (expanded) it.expandedIds + id else it.expandedIds - id) }
        savedState["expandedIds"] = state.value.expandedIds.toList()
    }

    private fun priorityOf(all: List<CityEssential>, lang: String): CityEssential? =
        all.filter { it.category == EssentialCategory.CUSTOMS && it.hasSubstance(lang) }.minByOrNull { it.sortOrder }

    private fun emergencyNumbersOf(all: List<CityEssential>, lang: String): List<EmergencyNumber> =
        all.filter { it.category == EssentialCategory.EMERGENCY }
            .flatMap { parseEmergencyNumbers(it.localizedContent(lang)) }
            .ifEmpty { BUNDLED_EMERGENCY_NUMBERS }

    private fun groupsOf(all: List<CityEssential>, lang: String): List<EssentialGroup> =
        EssentialGroupKind.entries.mapNotNull { kind ->
            all.filter { it.category == kind.category && it.hasSubstance(lang) }
                .sortedBy { it.sortOrder }
                .takeIf { it.isNotEmpty() }
                ?.let { EssentialGroup(kind, it) }
        }
}

/** Local, per-device preference: the arrival reminder card is hidden. Not a registration status. */
internal const val REMINDER_HIDDEN_KEY = "essentials_arrival_reminder_hidden"
