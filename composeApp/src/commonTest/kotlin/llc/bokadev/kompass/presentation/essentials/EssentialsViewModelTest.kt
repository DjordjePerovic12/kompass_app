package llc.bokadev.kompass.presentation.essentials

import llc.bokadev.kompass.presentation.shared.linkTitle
import llc.bokadev.kompass.core.utils.networking.NetworkError
import llc.bokadev.kompass.core.utils.networking.Resource
import llc.bokadev.kompass.presentation.shared.LanguageSheetHost
import llc.bokadev.kompass.testutil.NoOpAnalytics
import llc.bokadev.kompass.testutil.FakeNavigator
import llc.bokadev.kompass.core.navigation.ALL_CATEGORIES
import llc.bokadev.kompass.core.navigation.Screen
import llc.bokadev.kompass.core.navigation.utils.NavType
import llc.bokadev.kompass.presentation.shared.ExternalLaunch
import kotlinx.coroutines.flow.toList
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlin.test.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import llc.bokadev.kompass.core.utils.AppPreferences
import llc.bokadev.kompass.domain.location.UserLocationProvider
import llc.bokadev.kompass.domain.model.*
import llc.bokadev.kompass.domain.repository.*
import llc.bokadev.kompass.domain.usecase.*
import llc.bokadev.kompass.presentation.shared.EssentialGroupKind
import llc.bokadev.kompass.presentation.shared.formatDistance
import llc.bokadev.kompass.presentation.essentials.utils.BUNDLED_EMERGENCY_NUMBERS
import llc.bokadev.kompass.presentation.essentials.utils.EmergencyService
import llc.bokadev.kompass.presentation.essentials.utils.firstParagraph
import llc.bokadev.kompass.presentation.essentials.utils.parseEmergencyNumbers

@OptIn(ExperimentalCoroutinesApi::class)
class EssentialsViewModelTest {

    @Test fun emergencyNumbersParseFromTheCmsLinesAndSkip112() {
        val parsed = parseEmergencyNumbers("Police - 122\nFire department - 123\nAmbulance - 124\nGeneral: 112\nCall us anytime")
        assertEquals(listOf("122", "123", "124"), parsed.map { it.number })
        assertEquals(listOf(EmergencyService.POLICE, EmergencyService.FIRE, EmergencyService.AMBULANCE), parsed.map { it.service })
    }

    @Test fun formattingHelpers() {
        assertEquals("86 m", 86.formatDistance())
        assertEquals("130 m", 133.formatDistance())
        assertEquals("1.2 km", 1_234.formatDistance())
        assertEquals("redtaxikotor.com", "https://www.redtaxikotor.com/prices".linkTitle())
        assertEquals("First paragraph.", "First paragraph.\n\nSecond one.".firstParagraph())
    }

    @Test fun cmsContentBuildsPriorityEmergencyAndOrderedGroups() = run(
        essentials = listOf(
            essential("tips1", EssentialCategory.TIPS, "Traffic is heavy at noon."),
            essential("taxi", EssentialCategory.TRANSPORT, "Use Red Taxi.", links = listOf("https://redtaxi.me")),
            essential("emergency", EssentialCategory.EMERGENCY, "Police - 122\nFire department - 123"),
            essential("registration", EssentialCategory.CUSTOMS, "Register within 24 hours.\n\nBring your passport."),
            essential("empty", EssentialCategory.PRACTICAL, "")
        )
    ) { state ->
        assertEquals("registration", state.priority?.id)
        assertEquals(listOf("122", "123"), state.emergencyNumbers.map { it.number })
        // Registration, Getting around, (Practical has nothing to show, so it's hidden), Local tips.
        assertEquals(
            listOf(EssentialGroupKind.REGISTRATION, EssentialGroupKind.GETTING_AROUND, EssentialGroupKind.LOCAL_TIPS),
            state.groups.map { it.kind }
        )
        assertFalse(state.essentialsFailed)
    }

    @Test fun offlineFreshInstallStillHasBundledEmergencyNumbers() = run(fail = true) { state ->
        assertEquals(BUNDLED_EMERGENCY_NUMBERS, state.emergencyNumbers)
        assertTrue(state.essentialsFailed)
        assertTrue(state.utilitiesFailed)
        assertNull(state.priority)
    }

    @Test fun deniedLocationHidesTheOriginButStillRanksFromOldTown() = run(
        utilities = listOf(utility("a", UtilityCategory.ATM, 42.4250, 18.7710), utility("b", UtilityCategory.ATM, 42.4300, 18.7700)),
        permission = false
    ) { state ->
        assertNull(state.currentLocation)
        val atm = state.sections.single()
        assertEquals("a", atm.nearest.first().utility.id)
    }

    @Test fun howItWorksExpandsWithoutTogglingClosed() = run(
        essentials = listOf(essential("registration", EssentialCategory.CUSTOMS, "Register.")),
        utilities = emptyList(),
        fail = false,
        permission = true
    ) { _: EssentialsState, vm: EssentialsViewModel ->
        vm.onEvent(EssentialsEvent.OnItemExpand("registration"))
        vm.onEvent(EssentialsEvent.OnItemExpand("registration"))
        assertEquals(setOf("registration"), vm.viewStateFlow.value.expandedIds)
    }

    @Test fun utilityMapAndBackGoThroughTheNavigator() = run(
        essentials = emptyList(),
        utilities = emptyList(),
        fail = false,
        permission = true
    ) { _: EssentialsState, vm: EssentialsViewModel, navigator: FakeNavigator ->
        vm.onEvent(EssentialsEvent.OnUtilityMapClick(UtilityCategory.ATM, "a"))
        vm.onEvent(EssentialsEvent.OnUtilityMapClick(null, null))
        vm.onEvent(EssentialsEvent.NavigateBack)
        runCurrent()
        assertEquals(
            listOf<Screen>(Screen.UtilityMapScreen(UtilityCategory.ATM.name, "a"), Screen.UtilityMapScreen(ALL_CATEGORIES, null)),
            navigator.destinations
        )
        assertEquals(NavType.NavigateUp, navigator.recorded.last())
    }

    @Test fun dialHandsOffThroughTheEventFlow() = run(
        essentials = emptyList(),
        utilities = emptyList(),
        fail = false,
        permission = true
    ) { _: EssentialsState, vm: EssentialsViewModel, _: FakeNavigator ->
        val events = mutableListOf<EssentialsEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.eventFlow.toList(events) }
        vm.onEvent(EssentialsEvent.OnDialClick("112"))
        vm.onEvent(EssentialsEvent.OnTurnOnLocationClick)
        runCurrent()
        assertEquals(
            listOf<EssentialsEvent>(
                EssentialsEvent.OpenExternal(ExternalLaunch.Dial("112")),
                EssentialsEvent.OpenExternal(ExternalLaunch.AppSettings)
            ),
            events
        )
    }

    @Test fun aLanguageChangeReLocalizesTheGroupsWithoutReloading() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val prefs = FakePreferences()
        var loads = 0
        // French-only content: hidden in English (nothing to show), shown once the app is French.
        val frenchOnly = CityEssential(id = "taxi", key = "taxi", category = EssentialCategory.TRANSPORT, title = mapOf("fr" to "Taxi"), content = mapOf("fr" to "Prenez un taxi."))
        val vm = EssentialsViewModel(
            GetEssentialsUseCase(object : EssentialRepository {
                override suspend fun getEssentials(): Resource<List<CityEssential>, NetworkError, String?> { loads++; return Resource.Success(listOf(frenchOnly)) }
            }),
            GetUtilitiesUseCase(object : UtilityRepository { override suspend fun getUtilities() = Resource.Success(emptyList<Utility>()) }),
            object : UserLocationProvider {
                override fun hasPermission() = false
                override suspend fun getCurrentLocation(): GeoPoint? = null
            },
            prefs, SavedStateHandle(), FakeNavigator(), LanguageSheetHost(), NoOpAnalytics()
        )
        try {
            backgroundScope.launch { vm.essentialsState.collect {} }
            runCurrent()
            assertTrue(vm.essentialsState.value.groups.isEmpty())
            prefs.setSelectedLanguage("fr")
            runCurrent()
            assertEquals(listOf(EssentialGroupKind.GETTING_AROUND), vm.essentialsState.value.groups.map { it.kind })
            assertEquals(1, loads)
        } finally {
            vm.viewModelScope.cancel()
            advanceUntilIdle()
            Dispatchers.resetMain()
        }
    }

    private fun run(
        essentials: List<CityEssential> = emptyList(),
        utilities: List<Utility> = emptyList(),
        fail: Boolean = false,
        permission: Boolean = true,
        assertions: (EssentialsState) -> Unit
    ) = run(essentials, utilities, fail, permission) { state, _ -> assertions(state) }

    private fun run(
        essentials: List<CityEssential>,
        utilities: List<Utility>,
        fail: Boolean,
        permission: Boolean,
        assertions: (EssentialsState, EssentialsViewModel) -> Unit
    ) = run(essentials, utilities, fail, permission) { state: EssentialsState, vm: EssentialsViewModel, _: FakeNavigator -> assertions(state, vm) }

    private fun run(
        essentials: List<CityEssential>,
        utilities: List<Utility>,
        fail: Boolean,
        permission: Boolean,
        assertions: suspend TestScope.(EssentialsState, EssentialsViewModel, FakeNavigator) -> Unit
    ) = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val navigator = FakeNavigator()
            val vm = EssentialsViewModel(
                GetEssentialsUseCase(object : EssentialRepository {
                    override suspend fun getEssentials() =
                        if (fail) Resource.Error(NetworkError.UNKNOWN, "offline") else Resource.Success(essentials)
                }),
                GetUtilitiesUseCase(object : UtilityRepository {
                    override suspend fun getUtilities() =
                        if (fail) Resource.Error(NetworkError.UNKNOWN, "offline") else Resource.Success(utilities)
                }),
                object : UserLocationProvider {
                    override fun hasPermission() = permission
                    override suspend fun getCurrentLocation(): GeoPoint? = if (permission) GeoPoint(42.4247, 18.7712) else null
                },
                FakePreferences(),
                SavedStateHandle(),
                navigator,
                LanguageSheetHost(),
                NoOpAnalytics()
            )
            // Subscribing starts the first load.
            backgroundScope.launch { vm.essentialsState.collect {} }
            runCurrent()
            advanceTimeBy(100)
            runCurrent()
            try {
                assertions(vm.essentialsState.value, vm, navigator)
            } finally {
                // The ViewModel refreshes location every 5s forever; stop it before Main is reset.
                vm.viewModelScope.cancel()
                advanceUntilIdle()
            }
        } finally {
            Dispatchers.resetMain()
        }
    }

    private fun essential(id: String, category: EssentialCategory, content: String, links: List<String> = emptyList()) = CityEssential(
        id = id, key = id, category = category, title = mapOf("en" to id), content = mapOf("en" to content), links = links
    )

    private fun utility(id: String, category: UtilityCategory, lat: Double, lng: Double) =
        Utility(id = id, name = id, category = category, latitude = lat, longitude = lng, address = null, googlePlaceId = null)

    private class FakePreferences : AppPreferences {
        private val values = mutableMapOf<String, String>()
        override val selectedLanguageFlow = MutableStateFlow("en")
        override fun getString(key: String) = values[key]
        override fun setString(key: String, value: String?) { if (value == null) values.remove(key) else values[key] = value }
        override fun getSelectedLanguage() = "en"
        override fun setSelectedLanguage(language: String) { selectedLanguageFlow.value = language }
        override fun getAnonymousUserId(): String? = null
        override fun setAnonymousUserId(value: String) = Unit
        override fun isFirstLaunch() = false
        override fun setFirstLaunch(value: Boolean) = Unit
        override fun hasSeenLocationEducationPrompt() = true
        override fun setSeenLocationEducationPrompt(value: Boolean) = Unit
        override fun hasAudioPass() = false
        override fun setAudioPass(value: Boolean) = Unit
        override fun hasExplorerPass() = false
        override fun setExplorerPass(value: Boolean) = Unit
        override fun hasPerksPass() = false
        override fun setPerksPass(value: Boolean) = Unit
    }
}
