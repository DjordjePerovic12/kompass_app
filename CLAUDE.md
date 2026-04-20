# KOmpass — Local Tourist Guide for Kotor

Pocket tourist guide app for Kotor, Montenegro. All recommendations curated by locals — no pay-to-rank, no tourist traps. Single app, all data in one place.

## Tech Stack

- **Platform:** Kotlin Multiplatform + Compose Multiplatform 1.10.3 (shared UI)
- **Kotlin:** 2.2.20
- **Package:** `llc.bokadev.kompass`
- **Backend:** Supabase (Postgres, Auth, Storage, REST)
- **DI:** Koin 4.x (KMP)
- **Networking:** Ktor Client 3.x
- **Images:** Coil 3 (KMP)
- **Navigation:** Official Compose Navigation Multiplatform
- **Serialization:** kotlinx.serialization
- **Architecture:** Clean Architecture + MVI
- **Target SDK:** Android 35, iOS 17+

## Architecture

```
composeApp/
  src/
    commonMain/
      kotlin/llc/bokadev/kompass/
        core/           # Design system tokens, common utils, extensions
        data/
          remote/       # Supabase client, DTOs
          mapper/       # DTO → Domain mappers (extension functions)
          repository/   # Repository implementations
        domain/
          model/        # Domain entities (Place, Event, Experience, etc.)
          repository/   # Repository interfaces
          usecase/      # Single-purpose use cases with operator fun invoke()
        presentation/
          navigation/   # Nav graphs, route sealed class
          screens/      # Feature packages: home/, places/, events/, etc.
            home/
              HomeScreen.kt           # orchestrator only
              HomeScreenContent.kt    # all UI, private helpers as private funs
              HomeViewModel.kt
              HomeState.kt
              components/
                FeaturedPlaceCard.kt  # reusable public composables, one per file
                CompactPlaceCard.kt
                EventCard.kt
                SectionHeader.kt
          theme/        # KOmpassTheme, Colors, Typography, Shapes
        di/             # Koin modules per feature + root appModule
    androidMain/        # Android Application class, expect/actual implementations
    iosMain/            # iOS entry point, expect/actual implementations
androidApp/             # Android manifest, launcher
iosApp/                 # Xcode project, SwiftUI entry
```

## Screen Structure (MANDATORY for every screen)

Every screen package must contain exactly:

```
screens/[name]/
├── [Name]Screen.kt         # orchestrator: collect state, call VM, delegate to content
├── [Name]ScreenContent.kt  # pure UI: public composable taking state + lambdas
├── [Name]ViewModel.kt
└── components/             # public composables reusable beyond this screen
    └── SomeCard.kt         # one file per component
```

Rules:
- `[Name]Screen.kt` — only: `koinViewModel()`, `collectAsState()`, call `[Name]ScreenContent()`
- `[Name]ScreenContent.kt` — public top-level composable + private helper funs (sections, headers, etc.)
- `components/` — reusable composables that could be used by other screens or will grow complex. One public `@Composable` per file. No private helpers here — those stay in ScreenContent.
- Never put UI logic in Screen.kt. Never put VM wiring in ScreenContent.kt.

## Color System

All colors defined in `presentation/theme/Color.kt`. Access ONLY via `KompassTheme.colors.X` — never import raw color vals directly in UI files.

```kotlin
// CORRECT
Text(color = KompassTheme.colors.colorNavy)

// WRONG — never do this in UI files
import llc.bokadev.kompass.presentation.theme.colorNavy
Text(color = colorNavy)
```

### Color Token Reference

| Token | Description | Hex |
|---|---|---|
| `colorNavy` | Primary brand navy — text, backgrounds, icons | `#102A43` |
| `colorNavyMedium` | Secondary navy shade | `#243B53` |
| `colorNavySubtle` | Tertiary navy shade | `#334E68` |
| `colorNavyMuted` | Muted navy — surface variants | `#486581` |
| `colorSlate` | Secondary text, subdued labels | `#627D98` |
| `colorSlateLight` | Inactive icons, placeholders | `#829AB1` |
| `colorSlateSoft` | Outlines, dividers | `#9FB3C8` |
| `colorSlatePale` | Subtle outlines | `#BCCCDC` |
| `colorSlateGhost` | Borders, thumbnail backgrounds | `#D9E2EC` |
| `colorSlateFaint` | Hover states, faint backgrounds | `#F0F4F8` |
| `colorAmberDark` | Accent dark — "See all" links | `#D97706` |
| `colorAmber` | Primary accent — badges, highlights | `#F59E0B` |
| `colorAmberLight` | Amber mid-tone | `#FBBF24` |
| `colorAmberSubtle` | Amber tints — date blocks, indicators | `#FEF3C7` |
| `colorWhite` | Cards, nav bar background | `#FFFFFF` |
| `colorSurface` | Page background | `#F8F9FB` |
| `colorSurfaceMid` | Input backgrounds, surface variants | `#F1F3F7` |
| `colorSurfaceStrong` | Dividers, strong surfaces | `#E4E7ED` |
| `colorSuccess` | Success states | `#059669` |
| `colorError` | Error states | `#DC2626` |
| `colorSponsored` | Sponsored badge | `#F59E0B` |

## MVI Pattern

Every screen follows this structure strictly:
- `sealed interface [Screen]Intent` — all user actions
- `data class [Screen]State(...)` — immutable, single source of truth
- `sealed interface [Screen]SideEffect` — one-shot events (navigation, toasts)
- ViewModel exposes `StateFlow<State>`, processes intents via `fun onIntent(intent)`
- No mutable state outside ViewModels
- UI observes state via `collectAsState()`, dispatches intents via `onIntent()`

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

**Itinerary** — name, description, days_count, items (ordered place/event references per day with time_of_day and notes)

All translatable fields: JSONB `{"en":"...","fr":"...","tr":"...","es":"...","de":"..."}`. Always fall back to "en" if locale key missing.

## Categories & Filters

Top-level: Eat & Drink, See & Visit, Activities, Hidden Gems, Practical. Sub-filters are tag-based (dinner, nightlife, music, seafood, casual, fine dining, etc.)

## Key Features (MVP)

- GPS-based nearby attractions with category browsing
- City Essentials (transport, customs, tipping, emergency, local tips)
- Day-based curated itineraries ("2 days in Kotor")
- Upcoming Events on home screen
- Experiences section (bookable, clearly labeled as sponsored)
- Multi-language (English, French, Turkish, Spanish, German)
- Bottom nav: Home, Categories, Events, Experiences, Essentials

## Premium (post-MVP)

- Interactive map with all landmarks pinned
- AI audio guides per landmark (2-3 min, ElevenLabs)

## Commands

```bash
./gradlew build
./gradlew :composeApp:installDebug          # Android
./gradlew :composeApp:testDebugUnitTest     # Tests
./gradlew :composeApp:dependencies          # Deps
```

## Code Rules

- Unit tests required for every ViewModel and repository
- All DTOs in data/remote/, mapped to domain via extension functions in data/mapper/
- Never expose Supabase types above data layer
- All user-visible strings from translations jsonb, never hardcoded English
- Repository interfaces in domain/, implementations in data/
- Use cases: single-purpose, `operator fun invoke()`
- Koin modules per feature, registered in root appModule
- Compose previews required for all reusable components in components/ subfolders
- Use Supabase Kotlin SDK (`io.github.jan-tennert.supabase`), not raw Ktor
- GPS permissions: expect/actual, graceful degradation if denied
- Read `.claude/skills/design-system/SKILL.md` before creating ANY UI composable

## Gotchas

- Compose Navigation multiplatform: check compat before deeplinks or nested graphs
- Coil 3 KMP: use AsyncImage, configure ImageLoader per platform via expect/actual
- Supabase Kotlin SDK has KMP support — use it, don't write raw REST calls
- translations jsonb: always fall back to "en" if current locale missing
- CMP 1.10.3 requires Kotlin 2.2.20 — don't downgrade
- Use @Preview (org.jetbrains.compose.ui.tooling.preview.Preview) — unified annotation
- Never import raw color vals (colorNavy, colorAmber, etc.) in UI files — always use KompassTheme.colors.X
