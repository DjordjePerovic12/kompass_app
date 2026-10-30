package llc.bokadev.kompass.core.navigation

import androidx.compose.runtime.*
import androidx.navigation.compose.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import kotlinx.coroutines.withTimeoutOrNull
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.navigation
import llc.bokadev.kompass.core.audio.AudioController
import llc.bokadev.kompass.core.audio.AudioState
import llc.bokadev.kompass.core.utils.AppStrings
import llc.bokadev.kompass.core.audio.NavAudioHost
import llc.bokadev.kompass.core.presentation.SnackbarBus
import llc.bokadev.kompass.core.navigation.utils.Navigator
import llc.bokadev.kompass.core.utils.ConnectivityMonitor
import llc.bokadev.kompass.core.utils.currentAppLanguage
import llc.bokadev.kompass.core.utils.rememberAppStrings
import llc.bokadev.kompass.core.utils.supportedDeviceLanguage
import llc.bokadev.kompass.presentation.shared.LanguageSheet
import llc.bokadev.kompass.presentation.shared.LanguageSheetHost
import llc.bokadev.kompass.core.utils.AppPreferences
import llc.bokadev.kompass.core.utils.AudioGuidePlayer
import llc.bokadev.kompass.core.utils.PlaybackRequestGuard
import llc.bokadev.kompass.presentation.onboarding.OnboardingScreen
import llc.bokadev.kompass.presentation.onboarding.OnboardingViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.koin.compose.koinInject
import llc.bokadev.kompass.core.components.BottomTab
import llc.bokadev.kompass.core.components.KompassBottomNavBar
import llc.bokadev.kompass.core.components.toListenButtonState
import llc.bokadev.kompass.core.components.NavAudioButton
import llc.bokadev.kompass.core.navigation.utils.HomeRootResetSequencer
import llc.bokadev.kompass.core.navigation.utils.NavigatorBridge
import llc.bokadev.kompass.core.navigation.utils.RootTabBackHandler
import llc.bokadev.kompass.ui.theme.KompassTheme

@Composable
fun MainShell() {
    val preferences = koinInject<AppPreferences>()
    // First-run intro gate: its ViewModel owns the stored "seen" flag.
    val onboardingVm: OnboardingViewModel = koinViewModel()
    val onboarding by onboardingVm.onboardingState.collectAsStateWithLifecycle()
    if (!onboarding.completed) {
        OnboardingScreen(viewModel = onboardingVm, showSnackBar = koinInject<SnackbarBus>()::show)
        return
    }
    var selected by rememberSaveable { mutableStateOf(BottomTab.Home) }
    // Controllers outlive the visible host, retaining each tab's entries and ViewModels.
    val home = rememberNavController()
    val explore = rememberNavController()
    val saved = rememberNavController()
    val essentials = rememberNavController()
    val controller = when (selected) {
        BottomTab.Home -> home
        BottomTab.Explore -> explore
        BottomTab.Saved -> saved
        BottomTab.Essentials -> essentials
    }
    val holder = rememberSaveableStateHolder()
    val player = koinInject<AudioGuidePlayer>()
    val playbackGuard = koinInject<PlaybackRequestGuard>()
    val navigator = koinInject<Navigator>()
    val audio = koinInject<AudioController>()
    val navAudio = koinInject<NavAudioHost>()
    val connectivity = koinInject<ConnectivityMonitor>()
    val snackbarBus = koinInject<SnackbarBus>()
    val languageSheet = koinInject<LanguageSheetHost>()
    val languageSheetOpen by languageSheet.isOpen.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val strings = rememberAppStrings()
    LaunchedEffect(snackbarBus) {
        snackbarBus.messages.collect { message ->
            // Actions (Undo) stay up for 5s, plain messages for the short default.
            val result = if (message.actionLabel != null || message.durationMs != null) {
                withTimeoutOrNull(message.durationMs ?: 5_000L) {
                    snackbarHostState.showSnackbar(message.text, message.actionLabel, duration = SnackbarDuration.Indefinite)
                }.also { if (it == null) snackbarHostState.currentSnackbarData?.dismiss() }
            } else {
                snackbarHostState.showSnackbar(message.text)
            }
            if (result == SnackbarResult.ActionPerformed) message.onAction?.invoke()
        }
    }
    val homeRootReset = remember { HomeRootResetSequencer() }
    val entry by controller.currentBackStackEntryAsState()
    val fullScreen = entry?.destination?.hasRoute<Screen.UtilityMapScreen>() == true
    val inLibrary = entry?.destination?.hasRoute<Screen.ListenScreen>() == true
    // Search spec §2.6: the floating bar never sits on top of the keyboard.
    val imeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    fun select(tab: BottomTab) {
        // Listen spec §4.1: tapping any tab leaves the audio library (which stops its playback).
        if (inLibrary) controller.popBackStack()
        if (tab != selected) {
            player.stop()
            playbackGuard.invalidate()
            selected = tab
        } else {
            when (tab) {
                BottomTab.Home -> controller.popBackStack<Screen.HomeScreen>(false)
                BottomTab.Explore -> controller.popBackStack<Screen.ExploreScreen>(false)
                BottomTab.Saved -> controller.popBackStack<Screen.FavoritesScreen>(false)
                BottomTab.Essentials -> controller.popBackStack<Screen.EssentialsScreen>(false)
            }
        }
    }
    NavigatorBridge(navigator, controller, onSwitchTab = ::select)
    Scaffold(
        // Explicit, not incidental: Scaffold's own containerColor defaults to
        // MaterialTheme.colorScheme.background, which is still the app's old warm off-white
        // token (Theme.kt's materialColorScheme). Leaving it unset leaks that color into
        // any gap instead of the current screen's own content showing through.
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0),
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    shape = RoundedCornerShape(16.dp),
                    containerColor = KompassTheme.colors.chalkWhite,
                    contentColor = KompassTheme.colors.nightBlack,
                    actionColor = KompassTheme.colors.nightBlack
                )
            }
        },
        bottomBar = {
            if (!fullScreen && !imeVisible) {
                KompassBottomNavBar(
                    selectedTab = selected.takeUnless { inLibrary },
                    onTabSelected = ::select,
                    audioButton = {
                        ShellAudioButton(
                            audio = audio,
                            navAudio = navAudio,
                            snackbarBus = snackbarBus,
                            strings = strings,
                            onOpenLibrary = { controller.navigate(Screen.ListenScreen) { launchSingleTop = true } }
                        )
                    }
                )
            }
        }
    ) { padding ->
        holder.SaveableStateProvider(selected.name) {
            NavHost(controller, startDestination = Screen.TabGraph(selected.name),
                modifier = Modifier.padding(padding).consumeWindowInsets(padding),
                enterTransition = kompassEnterTransition,
                exitTransition = kompassExitTransition,
                popEnterTransition = kompassPopEnterTransition,
                popExitTransition = kompassPopExitTransition) {
                navigation<Screen.TabGraph>(startDestination = when (selected) {
                    BottomTab.Home -> Screen.HomeScreen
                    BottomTab.Explore -> Screen.ExploreScreen
                    BottomTab.Saved -> Screen.FavoritesScreen
                    BottomTab.Essentials -> Screen.EssentialsScreen
                }) {
                    tabDestinations()
                }
            }
        }
    }
    if (languageSheetOpen) {
        LanguageSheet(
            currentLanguage = currentAppLanguage(),
            strings = strings.language,
            onSelect = { code ->
                preferences.setSelectedLanguage(code)
                languageSheet.close()
            },
            onUsePhoneLanguage = {
                preferences.setSelectedLanguage(supportedDeviceLanguage())
                languageSheet.close()
            },
            onDismiss = languageSheet::close
        )
    }
    // Backgrounding does not change destinations and must not stop audio.
    DisposableEffect(controller) {
        var previous = controller.currentBackStackEntry?.id
        val listener = androidx.navigation.NavController.OnDestinationChangedListener { nav, _, _ ->
            val next = nav.currentBackStackEntry?.id
            if (previous != null && previous != next) {
                player.stop()
                playbackGuard.invalidate()
            }
            previous = next
        }
        controller.addOnDestinationChangedListener(listener)
        onDispose { controller.removeOnDestinationChangedListener(listener) }
    }
    RootTabBackHandler(selected != BottomTab.Home && controller.previousBackStackEntry == null) {
        player.stop()
        playbackGuard.invalidate()
        // Switch to Home first; the pop below only runs once Home's graph is actually
        // installed (see HomeRootResetSequencer) — popping home's stack here directly can
        // crash if Home was never the mounted tab (e.g. restored into another tab).
        homeRootReset.arm()
        selected = BottomTab.Home
    }
    LaunchedEffect(selected) {
        if (homeRootReset.consume(selected)) {
            home.popBackStack<Screen.HomeScreen>(inclusive = false)
        }
    }
}

/**
 * The center button bound to app state: always the Listen library (never a play control), showing
 * whatever plays app-wide. Inside the library it is marked current and does nothing.
 */
@Composable
private fun ShellAudioButton(
    audio: AudioController,
    navAudio: NavAudioHost,
    snackbarBus: SnackbarBus,
    strings: AppStrings,
    onOpenLibrary: () -> Unit
) {
    val context by navAudio.current.collectAsState()
    val audioState by audio.state.collectAsState()
    val inLibrary = context?.isLibrary == true
    val playing = audioState.trackOrNull?.takeIf { audioState is AudioState.Playing || audioState is AudioState.Paused }
    // Detail screens play from their own controls: a failure is announced here, once, app-wide.
    // The library shows its own message, so not while it is on top.
    LaunchedEffect(audioState is AudioState.Failed) {
        if (audioState is AudioState.Failed && !inLibrary) snackbarBus.show(strings.detail.couldntPlayAudio)
    }
    NavAudioButton(
        state = audioState.toListenButtonState(),
        label = strings.listen.title,
        description = listOfNotNull(strings.detail.openAudioLibrary, playing?.let { "${strings.listen.nowPlaying.lowercase()}: ${it.title}" }).joinToString(". "),
        current = inLibrary,
        onClick = { if (!inLibrary) onOpenLibrary() }
    )
}
