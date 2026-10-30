# KOmpass — Local Tourist Guide for Kotor

Pocket tourist guide app for Kotor, Montenegro. All recommendations curated by locals — no pay-to-rank, no tourist traps. Single app, all data in one place.

The code follows the **amplitudo-kmp-compose** skill (source of truth: `~/StudioProjects/amplitudo-claude-tools/plugins/amplitudo-kmp-compose`). When this file and the skill disagree on a convention, the skill wins; this file records what is specific to kompass.

## Tech Stack

- **Platform:** Kotlin Multiplatform + Compose Multiplatform 1.12.2 (shared UI)
- **Kotlin:** 2.4.21 (AGP 9.3.1, Gradle 9.7.0 — Kotlin 2.4.21 supports AGP ≤ 9.3.1)
- **Package:** `llc.bokadev.kompass`
- **Backend:** Supabase (Postgres, Storage, REST) via the Supabase Kotlin SDK
- **DI:** Koin 4.x (`viewModelOf`, `singleOf`)
- **Networking:** Ktor Client 3.x, wrapped in `Resource`
- **Logging:** Kermit
- **Images:** Coil 3 (KMP)
- **Navigation:** Compose Navigation Multiplatform with typed `Screen` routes
- **Serialization:** kotlinx.serialization
- **Targets:** Android minSdk 26, target/compile SDK 37, JVM 17; iOS 18.2+

## Modules

```
androidApp/   com.android.application: KompassApplication (initKoin), MainActivity, app manifest, launcher icons
composeApp/   com.android.kotlin.multiplatform.library (namespace llc.bokadev.kompass.shared): all shared code
iosApp/       Xcode project; iOSApp.swift calls InitKoinKt.doInitKoin and hosts MainViewController
```

## Architecture (`composeApp/src/commonMain/kotlin/llc/bokadev/kompass/`)

```
App.kt                  AppEnvironment(language) { KompassTheme { KompassAppNavigation(isFirstLaunch) } }
core/
  navigation/           Screen.kt (typed routes), KompassAppNavigation.kt, MainShell.kt (tabs),
                        destinations/ (one file per screen), utils/ (Navigator, NavigatorImpl,
                        NavType, NavigatorBridge, ObserveAsEvents)
  utils/                AppLocale (LocalAppLocale), AppPreferences, StringProvider, networking/
                        (Resource, NetworkError, safeSupabaseCall, retrieveAPIResponse), formatters
  audio/, components/, presentation/SnackbarBus
data/                   remote/ (Supabase client, DTOs), mapper/ (DTO → domain extensions), repository/ (impls)
domain/                 model/, repository/ (interfaces), usecase/ (operator fun invoke)
di/                     initKoin, AppModule, one Koin module per feature
presentation/
  base/                 BaseViewModel, BaseViewState, BaseEvent, BaseError, BaseContentView
  <feature>/            one folder per screen (see below); sub-features as snake_case sub-packages
                        (infocenter/detail, essentials/utility_map, excursions/related_activities)
  shared/               composables and helpers used by several features: icons/, detail/ (detail
                        screen building blocks), LamplightHeader, AreaLabel, PlaceCategoryLabels, ...
ui/theme/               KompassTheme: Color, Type, Shape, Gradient, Theme
```

## Screen structure (MANDATORY)

Every screen package holds exactly five files, plus optional `components/` and `utils/`:

```
presentation/<feature>/
├── <F>Screen.kt          orchestrator: (modifier, viewModel, showSnackBar); collectAsStateWithLifecycle;
│                         observes snackBarChannel and eventFlow; wraps content in BaseContentView;
│                         owns screen-lifetime platform wiring (launcher, audio owner key, permissions)
├── <F>ScreenContent.kt   stateless UI taking state + callbacks; no Koin; private @Preview in KompassTheme
├── <F>ViewModel.kt       extends BaseViewModel<State, Event>; navigates through the injected Navigator
├── <F>State.kt           data class extending BaseViewState(isLoading, error: BaseError?)
├── <F>Event.kt           sealed class : BaseEvent — every user intent (NavigateBack, On<Thing>Click,
│                         On<Thing>Change) plus one-off hand-offs the ViewModel emits (OpenExternal, ...)
├── components/           reusable composables, one public composable per file, each with a @Preview
└── utils/                non-UI helpers used only by this feature
```

Rules:
- There are no Action files: user intents are Event members dispatched with `viewModel.onEvent(...)`.
- ViewModels load on first subscription: `state.onStart { if (!hasLoadedInitialData) … }.stateIn(viewModelScope, WhileSubscribed(5_000), …)`.
- Navigation happens in the ViewModel via `Navigator` (`navigateTo`, `navigateUp`, `switchTab`), never through callbacks. `NavigatorBridge` applies it to the selected tab's NavController.
- One-off UI hand-offs (open maps/URL/dialer, permission requests, snackbars that need an action such as Undo) are Event members emitted on `eventFlow`; plain-text snackbars go through `snackBarChannel`.
- `isLoading` means nothing can be shown yet; `error` means nothing usable could be loaded (`BaseError.InlineError(Res.string.x)`). A failed refresh with content on screen is a separate flag (e.g. `refreshFailed`).
- Screens that show loading/errors inline under their header pass the same content to `BaseContentView`'s `loading`, `error` and main slots (with a one-line comment).
- ViewModels are registered in the feature's Koin module with `viewModelOf(::X)`; route arguments arrive via `parametersOf` (or `SavedStateHandle` when optional).
- Every ViewModel has a test in `commonTest/.../presentation/<feature>/` using `FakeNavigator` (testutil). Tear down with `vm.viewModelScope.cancel(); advanceUntilIdle(); Dispatchers.resetMain()`.

## Theme (`ui/theme/`)

One theme, `KompassTheme`, following the skill's `theme.md` pattern:
- `Color.kt`: raw color vals named by how they look (`lampYellow`, `nightBlack`, `charcoal`, `boneWhite`, `stoneGray`, ...), `KompassColors`, `LocalKompassColors`.
- `Type.kt`: `KompassTypography` styles named `<font><Weight><size>` (`gloockRegular40`, `geologicaMedium16`, `helveticaBold24`, ...). Font families hold every weight file, so `.copy(fontWeight = …)` uses the real font.
- `Shape.kt` (`KompassShapes`, `KompassDimens`), `Gradient.kt` (`Modifier.pageGradientBackground()`), `Theme.kt` (`KompassTheme { }` + `object KompassTheme { colors; typography }`; it also supplies the Material scheme only so Material components keep their look).

In UI code read `KompassTheme.colors.X` / `KompassTheme.typography.X`. Never `Color(0x…)` in `presentation/`; add a token to `Color.kt` instead. `.claude/skills/design-system/SKILL.md` holds the design principles; the tokens themselves live in `ui/theme/`.

## Strings and language

- All UI text lives in `composeResources/values{,-fr,-de,-es,-tr}/strings.xml` (keys `<group>_<field>`, args `%1$s`, documented in an `<!-- args: -->` comment). Read them with `stringResource(Res.string.x)` in composables, the suspend `getString(Res.string.x)` in suspend callbacks, and `StringProvider` in ViewModels. Shared helpers return a `StringResource` rather than a string.
- Never hardcode visitor-facing English. When adding a key, add it to all five files.
- The current language comes from `LocalAppLocale.current` (fed by `AppPreferences.selectedLanguageFlow` in `App.kt`). Changing it recomposes in place, so the visitor keeps their tab and back stack.
- Translatable CMS fields are JSONB `{"en":"...","fr":"...","tr":"...","es":"...","de":"..."}`; always fall back to "en".
- `core/utils/SearchVocabularyTable.kt` deliberately keeps its own five-language word lists so a search in any language matches; Compose Resources cannot load another locale's string.
- Long translations: keep single-line controls single-line (`maxLines = 1`, `TextAutoSize`), let titles take `Modifier.weight(1f)`, and check German and French on device.

## Local Supabase Development

Toggle `USE_LOCAL_SUPABASE=true` in `local.properties` to point builds at a local Supabase instance (`supabase start`). The platform-appropriate host is selected automatically: Android emulator uses `10.0.2.2`, iOS simulator uses `localhost`, both on port `54321`. Also set `SUPABASE_ANON_KEY_LOCAL` to the anon key printed by `supabase start`.

```properties
# local.properties
USE_LOCAL_SUPABASE=true
SUPABASE_ANON_KEY_LOCAL=<key from supabase start>
```

Set `USE_LOCAL_SUPABASE=false` before any production builds.

## Data Models (maps to Supabase)

**Place** — name (i18n jsonb), description, locals_tip, category (eat_and_drink/see_and_visit/activities/hidden_gems/practical), sub_category, price_indicator (1-3), lat/lng, zone, tags, best_time, estimated_duration, opening_hours, photos

**Event** — name, description, venue, lat/lng, category (music/festival/sport/theater/cultural/other), start_time, end_time, price, ticket_url, is_recurring, photos

**Experience** — name, description, operator_name, duration_min, price, booking_url, contact, category, lat/lng, photos, availability, is_sponsored (always true)

**CityEssential** — key, category (transport/customs/emergency/tips/practical), title, content (markdown)

**Itinerary** — in the database, not used by the app yet (no domain model).

Content gaps to fill in the CMS rather than the app: `sub_category` and `zone` are shown as stored (untranslated), and event filter labels need all five languages.

## Key Features

- GPS-based nearby attractions with category browsing
- City Essentials (transport, customs, tipping, emergency, local tips) and a utility map
- Upcoming events and local updates on Home
- Experiences and "Worth the drive" excursions
- Local stories (legends & localisms) and audio guides with offline downloads
- Saved places, local search across all content
- Multi-language (English, French, Turkish, Spanish, German)
- Bottom nav: Home, Explore, Listen, Saved, Essentials

There is no payment or premium code in the app; the earlier payment and DEEP purchase stacks were removed (recoverable from git history).

## Commands

```bash
./gradlew :androidApp:installDebug                       # Android
./gradlew :composeApp:testAndroidHostTest                # Tests (JVM)
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64 # iOS framework
./gradlew :composeApp:allTests                           # also runs iOS tests (needs Xcode 26)
./gradlew :composeApp:dependencies                       # Deps
```

## Code Rules

- Unit tests required for every ViewModel and repository
- All DTOs in data/remote/, mapped to domain via extension functions in data/mapper/
- Never expose Supabase types above the data layer; repositories return `Resource` (use `safeSupabaseCall`)
- Repository interfaces in domain/, implementations in data/; domain/ has no UI text or compose-resources imports
- Use cases: single-purpose, `operator fun invoke()`
- Koin modules per feature, included from `AppModule`
- Before creating new files or packages, list the paths and get approval
- Read `.claude/skills/design-system/SKILL.md` before creating ANY UI composable

## Gotchas

- An Android Studio plugin ("Amplitudo Presentation Generator") auto-creates Hilt-style Screen/State/Event/ViewModel stubs in every new `presentation/<feature>` package — including test folders — and stages them in git. They shadow real classes and break the build. After creating folders or merging, check `git status` and `git diff --cached`, and never commit a file containing `HiltViewModel` or `AppTheme`.
- iOS test executables don't link on Xcode 16.4 (`_LocationEssentials`); verify with `testAndroidHostTest` until Xcode 26 is installed. The iOS app itself builds and runs.
- iOS status bar style is app-wide (`UIViewControllerBasedStatusBarAppearance = false` in Info.plist); use `StatusBarIconsEffect` / `StatusBarScrim` rather than touching it directly.
- Android emulator DNS via some routers hangs; start the AVD with `-dns-server 8.8.8.8,1.1.1.1`.
- Kotlin 2.4.21 caps AGP at 9.3.1 — check the KMP compatibility table before bumping AGP.
- Coil 3 KMP: use AsyncImage, configure ImageLoader per platform via expect/actual.
- Use @Preview (org.jetbrains.compose.ui.tooling.preview.Preview) — unified annotation.

## Related Projects

### kompass-admin (CMS / Admin Panel)
**Path:** /Users/djordjeperovic/Downloads/kompass-admin
**Stack:** React 18 + TypeScript + Vite + Tailwind CSS + Supabase JS SDK
**Purpose:** Web-based admin panel for managing all KOmpass content in Supabase
**Key files:**
- `src/types/` — TypeScript types that mirror the Supabase schema (canonical source)
- `src/lib/supabase.ts` — Supabase JS client config
- `src/pages/` — Dashboard, Places, Events, Experiences, Essentials, Itineraries
- `src/hooks/useCrud.ts` — generic CRUD hook used by all pages
**Run:** `npm run dev` (port 3000)

### Adding a new feature / table
When a new Supabase table or column is added, update BOTH projects:
1. **kompass-admin:** `src/types/` (new TypeScript interface) + new page in `src/pages/`
2. **kompass:** `data/remote/dto/` (DTO) → `data/mapper/` (mapper extension) → `domain/model/` (domain class) → `domain/repository/` (interface) → `data/repository/` (impl returning `Resource`) → `domain/usecase/` (use case) → `presentation/<feature>/` (five-file screen) + `core/navigation/destinations/` + feature Koin module
