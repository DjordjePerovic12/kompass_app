package llc.bokadev.kompass.presentation.screens.essentials

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import llc.bokadev.kompass.core.presentation.mvi.BaseViewModel
import llc.bokadev.kompass.core.presentation.mvi.Navigator
import llc.bokadev.kompass.core.utils.AppPreferences
import llc.bokadev.kompass.core.utils.networking.Resource
import llc.bokadev.kompass.core.utils.networking.getOrNull
import llc.bokadev.kompass.domain.repository.AnalyticsRepository
import llc.bokadev.kompass.core.navigation.ALL_CATEGORIES
import llc.bokadev.kompass.core.navigation.Screen
import llc.bokadev.kompass.presentation.shared.ExternalLaunch
import llc.bokadev.kompass.presentation.shared.LanguageSheetHost
import llc.bokadev.kompass.domain.location.UserLocationProvider
import llc.bokadev.kompass.domain.model.CityEssential
import llc.bokadev.kompass.domain.model.EssentialCategory
import llc.bokadev.kompass.domain.model.GeoPoint
import llc.bokadev.kompass.domain.model.Utility
import llc.bokadev.kompass.domain.usecase.GetEssentialsUseCase
import llc.bokadev.kompass.domain.usecase.GetUtilitiesUseCase
import llc.bokadev.kompass.presentation.shared.EssentialGroupKind
import llc.bokadev.kompass.presentation.shared.title

class EssentialsViewModel(
    private val getEssentials: GetEssentialsUseCase,
    private val getUtilities: GetUtilitiesUseCase,
    private val userLocationProvider: UserLocationProvider,
    private val appPreferences: AppPreferences,
    private val savedState: SavedStateHandle,
    private val navigator: Navigator,
    private val languageSheet: LanguageSheetHost,
    private val analytics: AnalyticsRepository
) : BaseViewModel<EssentialsState, EssentialsAction, EssentialsEvent>(
    EssentialsState(
        selectedTab = savedState.get<String>("selectedTab")?.let {
            runCatching { EssentialsTab.valueOf(it) }.getOrNull()
        } ?: EssentialsTab.INFO,
        expandedIds = savedState.get<List<String>>("expandedIds")?.toSet() ?: emptySet(),
        reminderHidden = appPreferences.getString(REMINDER_HIDDEN_KEY) == "true",
        // Screen.EssentialModuleScreen(id) — Search opened one module.
        focusModuleId = savedState.get<String>("id")
    )
) {

    private var cachedUtilities = emptyList<Utility>()
    private var cachedEssentials: List<CityEssential>? = null
    private var loadJob: Job? = null

    init {
        startLocationRefresh()
        // The language is changed from this tab's own header: re-localize what was built from it.
        viewModelScope.launch {
            appPreferences.selectedLanguageFlow.drop(1).collect { lang ->
                cachedEssentials?.let { loaded -> updateState { it.withEssentials(loaded, lang) } }
            }
        }
    }

    override suspend fun onFirstStart() {
        analytics.trackScreenView("essentials")
        load()
    }

    override fun onAction(action: EssentialsAction) {
        when (action) {
            is EssentialsAction.SelectTab -> {
                savedState["selectedTab"] = action.tab.name
                updateState { it.copy(selectedTab = action.tab) }
            }
            is EssentialsAction.ToggleItem -> setExpanded(action.id, action.id !in state.value.expandedIds)
            is EssentialsAction.ExpandItem -> setExpanded(action.id, true)
            EssentialsAction.HideReminder -> {
                appPreferences.setString(REMINDER_HIDDEN_KEY, "true")
                updateState { it.copy(reminderHidden = true) }
            }
            EssentialsAction.Retry -> load()
            EssentialsAction.OpenLanguage -> languageSheet.open()
            is EssentialsAction.OpenUtilityMap ->
                navigator.navigateTo(Screen.UtilityMapScreen(action.category?.name ?: ALL_CATEGORIES, action.selectedId))
            is EssentialsAction.Dial -> sendEvent(EssentialsEvent.Launch(ExternalLaunch.Dial(action.number)))
            is EssentialsAction.OpenLocation ->
                sendEvent(EssentialsEvent.Launch(ExternalLaunch.Maps(action.latitude, action.longitude, action.title)))
            is EssentialsAction.OpenLink -> sendEvent(EssentialsEvent.Launch(ExternalLaunch.Url(action.url)))
            EssentialsAction.TurnOnLocation -> sendEvent(EssentialsEvent.Launch(ExternalLaunch.AppSettings))
            EssentialsAction.Back -> navigator.navigateUp()
        }
    }

    private fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            updateState { it.copy(isLoading = it.groups.isEmpty() && it.sections.isEmpty()) }
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

            updateState { current ->
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

    private fun startLocationRefresh() {
        viewModelScope.launch {
            while (true) {
                delay(5_000L)
                val location = currentLocationOrNull()
                updateState { it.copy(currentLocation = location, sections = sectionsFor(location)) }
            }
        }
    }

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
        updateState { it.copy(expandedIds = if (expanded) it.expandedIds + id else it.expandedIds - id) }
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
