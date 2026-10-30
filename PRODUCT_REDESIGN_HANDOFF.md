# Product Redesign Handoff

Paused at the user's request on 2026-10-01 until tokens reset.

## Workspace

- Work only in /Users/djordjeperovic/AndroidStudioProjects/kompass.
- Branch: custom/product_redesign. No commits made.
- Approved slice: persistent Home / Explore / Saved / Essentials navigation,
  Home area preferences, independent Home loading, localization and restrained styling.
- User explicitly approved recovering 64 missing baseline files from local main and
  deleting ten newly generated, incompatible Navigation* scaffold files in androidMain
  and iosMain. Recovery used git restore --source=main --worktree on added-only paths.
- Recovered files are untracked on this branch. They are required app source/fonts,
  NOT disposable generated files. Preserve them and include them in any eventual commit.

## Implemented

- Replaced Route.Main replacement/backTab/version counters with a stable MainShell,
  four remembered NavControllers, typed nested tab graphs and SaveableStateHolder.
- Shared details are registered in each controller, preserving their originating tab.
- Standard details keep bottom tabs; UtilityMap hides them.
- Active tab taps pop to the root. Android Back at other tab roots returns Home root.
- ViewModels remain entry-scoped; added SavedStateHandle for activity/place/event
  filters and Essentials subtab/expanded cards. Nearby UI filter is rememberSaveable.
- Removed redundant reload when returning to an already initialized place list/detail.
- Moved existing onboarding outside Home into the shell, preserving preference keys.
  Location education follows onboarding; removed repeated automatic permission requests.
- Explore has Places, Eat & Drink, Activities and Worth the Drive, retaining other links.
  Worth the Drive reuses the existing Nearby screen's >3.5km filter (not a new CMS model).
- Favorites promoted to Saved, retaining existing storage/identifiers; empty CTA to Explore.
- Home: compact header/search/audio, persisted Device/Old Town/Dobrota/Perast area picker,
  nearby first then highlights/notices/events. Manual areas use normalized stored zones.
- Device suggestions remain sightseeing-only, 3.5km/max four, distance ordered without
  favorite promotion. Fixed previous UI omission of the closest item.
- Manual/fallback suggestions do not display invented distances. Utilities still use GPS.
- Independent section jobs and localized loading/error/retry/empty states.
- Central destination-change and tab-switch audio stopping; removed old disposal hooks.
  Backgrounding itself does not trigger these navigation hooks.
- New labels localized in English/French/German/Spanish/Turkish.

## Verified

- Android and iOS Kotlin compilation passed after baseline recovery.
- Android APK assembly and unit tests passed: six HomeRedesignTest tests plus one existing.
- Xcode simulator build passed; installed and launched on iPhone 16 Pro / iOS 18.5.
- Android emulator-5554 (Medium_Phone) has app installed with existing data retained.
- Android runtime checks passed: Home detail -> Explore -> Home restores detail;
  Back returns Home root; Explore -> Activities (Hike filter) -> Vrmac detail -> Saved
  -> Explore restores Vrmac; Back retains Hike filter; Back returns Explore; Back returns Home.
- Existing Saved place/activity entries survived installation.
- iOS onboarding completed independently, then location education; chose Not Now.
- iOS Explore visually reviewed. Root tab labels still English in installed build;
  translations have since been added in source but not rebuilt yet.

## Important Runtime Blocker

- iOS had an existing Spanish language preference. Spanish is supported; it is NOT why
  there is no data. Runtime logs show NSURLErrorDomain -1003, DNS NoSuchRecord for the
  configured Supabase host. All requests fail before content can load.
- Host observed: msfyhxaengmbwcgsgysf.supabase.co. Do not expose API keys in logs/output.
- Android's populated screens can be supplied by the existing offline repository cache;
  this does not establish that the backend is currently reachable.
- Do not clear app data or seed/modify CMS to hide this issue. Diagnose configured endpoint
  and reachability next. Supabase paused/deleted/misconfigured is possible, not confirmed.

## Remaining Work

- Final source changes after successful builds: CompactPlaceCard minimum/wrap height fix,
  translated bottom Home/Essentials labels, scoped RedesignPalette in Color.kt and uses.
  Rebuild both platforms and rerun tests before claiming current HEAD fully verified.
- Last tool call failed JavaScript parsing before executing. It applied NONE of its intended
  changes: BaseContentView accessibility fix, shorter navigation transitions,
  Save/Remove-from-Saved translations and FavoriteToggleButton semantics.
- iOS AX currently merges Explore into one large button because BaseContentView wraps
  the entire screen in noRippleClickable for clearing focus. Consider removing this
  whole-screen clickable or replacing it without merged accessibility semantics.
- Finish active-tab repeated taps, Saved detail/back, process recreation, scroll restoration,
  manual-area persistence, denied-location retries, fullscreen maps and audio runtime tests.
- Audio background/navigation behavior is implemented but not yet runtime-verified.
  Check late async autoplay after navigating away as a potential remaining race.
- iOS happy-path details/back gestures remain unverified due to backend DNS failure.
- Final clean-up: unused imports/arguments, accessibility, diff review, verification notes.
- Keep Astra Medium for remaining navigation correctness; styling-only Terra cue later.

## Stage 1 — Reliability Gate (2026-10-05)

Addressed the three P1 findings from the follow-up review, before any Home visual work.

- Root Back from a non-Home tab previously popped `home`'s back stack before switching
  `selected` to Home — if Home's NavController had never been mounted this process
  (e.g. restored into another tab), its graph was never installed and the pop could crash.
  Fixed by switching to Home first and deferring the pop via a new `HomeRootResetSequencer`
  (`presentation/navigation/HomeRootResetSequencer.kt`), consumed in a `LaunchedEffect(selected)`
  in `MainShell.kt` so the pop only runs once Home's NavHost has actually composed.
- Guide autoplay suppression relied solely on each guide screen's own `ON_PAUSE` lifecycle
  observer, which removing a tab's NavHost does not reliably trigger. Replaced with a shared
  `PlaybackRequestGuard` (`core/util/PlaybackRequestGuard.kt`): a token captured before each
  async autoplay/prepare flow, invalidated centrally in `MainShell` wherever playback was
  already being stopped (tab switch, in-app destination change, explicit root reset) — not on
  backgrounding. A stale completion is rejected right before `prepare()`.
- Applied the same guard to `PlaceGuideViewModel`, `ExperienceGuideViewModel` and
  `MyGuidesViewModel` (place/activity list playback) — a newer selection also supersedes an
  older pending one via the same token bump, so rapid list taps can't race each other either.
- New deterministic tests (commonTest, no new dependencies — kotlinx-coroutines-test's
  `StandardTestDispatcher` + manual `runCurrent()`/`invalidate()` interleaving to force the
  race): `PlaybackRequestGuardTest`, `HomeRootResetSequencerTest` (includes a recreation test
  starting in a non-Home tab), `PlaceGuideViewModelTest`, `MyGuidesViewModelTest`. Confirmed
  each guard-rejection test actually fails if the guard check is removed (sanity-checked, then
  reverted). `ExperienceGuideViewModel` got the identical code fix as `PlaceGuideViewModel`
  but was not separately duplicated in tests — same bug shape, same fix, same proof pattern.
- Verified: `./gradlew :composeApp:testDebugUnitTest` — all 21 tests pass (15 new + existing
  6 HomeRedesignTest + 1 ComposeAppCommonTest). `:composeApp:assembleDebug` succeeds.
  `:composeApp:compileKotlinIosSimulatorArm64` succeeds.
- Also removed 5 stray, already-staged Dagger/Hilt scaffold files under
  `commonTest/.../presentation/screens/Screens*.kt` that didn't belong to this Koin/KMP
  codebase and were blocking all of commonTest from compiling. Unrelated to this feature;
  pre-existing in the working tree before this stage started.
- Not yet done: no on-device/simulator runtime verification of the back-crash fix or the
  playback race (the new tests are deterministic unit tests of the extracted logic, not
  instrumented Compose/Navigation tests — the project has no Compose UI test infra and the
  plan asked for no new dependencies). Stage 2 (Home visual prototype) has not started.

## Stage 2 — Home Visual Prototype (2026-10-05)

Home-only editorial redesign ("Ink and Citron"), built on top of Stage 1. Ranking, radius,
area persistence and API behavior are untouched — this was a visual-layer pass only.

- Added `HomePalette` (`presentation/theme/Color.kt`): canvas #F4F5EF, surface #FFFFFF,
  textPrimary #191C19, textSecondary #596158, divider #D9DED4, citron #D6EB45, error #A33832.
  Pure addition — the existing `RedesignPalette` (also used by Explore/Categories) and the
  global `KompassTheme.colors` tokens (used app-wide) are untouched, so this cannot leak
  outside Home.
- Replaced the large gold `HeroHeader` with a compact masthead ("Kotor" at 32/36sp + EN
  language pill + Audio pill, both ≥48dp targets), search field directly below, then the
  area control ("Device location ▾" + the existing Old Town fallback explanation).
- Nearby: `CompactPlaceCard` rewritten as a flat 88dp-square photo-led row with a thin
  divider between rows, replacing the old bordered/shadowed card.
- Highlights: `FeaturedPlaceCard`/`MustSeeCarouselCard` rewritten — one 16:10 photo per
  paged item, title+meta below the image (not overlaid), no scale/alpha/offset pager
  effects. Removed `MustSeeMiniCard` (dead code, confirmed unused elsewhere first).
  Citron is used only for the page-position dots' active state.
- Notices: rewritten from large gradient photo cards to restrained text-first rows with a
  thin accent bar (error-red for `priority == urgent`, citron for `priority == important`,
  none for general) — no photography, so they don't compete with Highlights.
- Events: unchanged `EventCard` component, but added optional `accentColor`/
  `accentContentColor` params (default unchanged, so the standalone Events screen via
  `EventItem.kt` is unaffected) and Home now passes citron/textPrimary for the date block —
  exactly the "small accent, dark text" usage the palette calls for.
- `SectionHeader` (Home-only, confirmed no outside usage) restyled to the new type scale;
  "See all" keeps dark text with just the arrow chip in citron, never citron body text.
- Area sheet (`HomeScreen.kt`) restyled to Home tokens; selected row gets a citron
  highlight + checkmark instead of a plain "✓ " text prefix.
- 20dp horizontal margins, 28dp between major sections, 12–16dp internal spacing, 48dp
  minimum interactive targets applied throughout the above.
- Verified live on `emulator-5554` (installDebug + screenshots): masthead, search, area
  control with fallback text, Nearby rows, Highlights pager (incl. a missing-photo
  placeholder), Notices, and the area sheet's selection state all render as designed.
  Confirmed by navigating into it that the standalone Place Detail screen is visually
  unaffected (old styling intact), consistent with "Explore/Saved/Essentials/details
  unchanged." `testDebugUnitTest`, `assembleDebug` and `compileKotlinIosSimulatorArm64`
  all pass after these changes.
- Not verified: iOS Home visually (compiles, not screenshotted this pass — no iOS
  simulator run this session); upcoming-events visual state (this environment's CMS/device
  had zero upcoming events, so the citron date-block treatment is implemented but
  unobserved live); enlarged system font scale; screen-reader labels; the other four
  languages rendered live (strings were reused from the already-localized `redesign`/base
  string tables, not retranslated, so no new i18n gaps were introduced, but not visually
  spot-checked per language this pass).

## Stage 3 — Ronas-Reference Redesign: Home + Shared Navigation (2026-10-05)

Superseded Stage 2's "Ink and Citron" direction with a new brief: a lilac-and-blue system
adapted from Ronas's Tour Operator / Travel Mobile App Dribbble references. First slice is
Home + shared bottom navigation only — place/activity details are an explicit next slice,
pending review of this one.

- **Correctness, done first per the brief's priority order:**
  - `StopPlayback` now invalidates `PlaybackRequestGuard` in `PlaceGuideViewModel`,
    `ExperienceGuideViewModel` and `MyGuidesViewModel` (previously only nav-level changes did).
  - Toggling play/pause on an already-active track also invalidates the guard, so an unrelated
    stale fetch can't sneak a `prepare()` in right after.
  - `MyGuidesViewModel` now tracks in-flight request keys: a repeat tap on a place/activity
    whose signed-URL fetch hasn't resolved yet cancels it instead of racing a second fetch for
    the same key. New regression test
    `playA_requestB_pauseABeforeBResolves_neitherEverPlays` (plus
    `explicitStopInvalidatesAnyPendingRequest`) — confirmed the first one fails without the
    in-flight-key fix, then restored it.
- **Palette** (`HomePalette` in `Color.kt`, replacing the Stage 2 citron tokens — same scoping
  discipline, only touches Home + shared nav now): canvas #EEEBF4, surface #FCFCFD,
  ink #211F29, secondaryText #625E70, action #38476B (deep blue — CTAs, active accents),
  selection #DCD5EA (muted lavender — selected states). All text/background pairings are
  dark-on-light or light-on-dark; no low-contrast pairing introduced.
- **Shape roles**: 24dp major surfaces (Nearby cards, area sheet), 16dp photographic tiles
  (Highlights, Nearby's inset photo), 12dp controls (search field, masthead pills, buttons).
- **Masthead**: unchanged structurally from Stage 2 (Kotor + language + Audio, search below,
  area control below that, fallback text retained) — just retokenized and typography moved to
  the new scale (30/36 title, 22/28 headings, 16/24 body, 14/20 metadata).
- **Nearby rebuilt**: replaced Stage 2's flat divided rows with a horizontally paged stack
  (`NearbyPlacesStack`/`NearbyPlaceCard`, new files; old `CompactPlaceCard` removed, confirmed
  unused elsewhere first) — up to 4 places, existing order preserved, up to two subtle white
  backing layers peek below the front card only when further pages exist, a page-position
  dot indicator, and accessible previous/next buttons (properly sized, localized
  `contentDescription`s) alongside swipe. No auto-rotation.
- **Missing/failed photos**: new `HomeImageFallback` — a lavender panel showing the place's
  name, never a blank gray box. Used by both the new Nearby cards and Highlights
  (`FeaturedPlaceCard`). Nearby order itself is untouched regardless of photo availability.
- **Highlights**: kept Stage 2's one-photo-per-page pager (no scale/alpha effects, title+meta
  below the image), retokenized; active page dot is now `action` blue.
- **Notices**: unchanged text-first row structure; priority accent bar now reuses the shared
  app-wide error color for `urgent` and `action` blue for `important` (no Home-only error
  token introduced this round, since the brief's palette didn't specify one).
  **Known pre-existing confusion** (not introduced by this change, left as-is): the sample
  "Water outage" notice shows `noticeType: "important"` in its meta line but its separate
  `priority` field is apparently not "important", so no accent bar shows for it — this is a
  CMS data quality question, not a code bug (`priorityRank()` correctly reads `priority`, not
  `noticeType`).
- **Events**: `EventCard` itself untouched (shared with the standalone Events screen via
  `EventItem.kt`); Home now passes its existing optional `accentColor`/`accentContentColor`
  params as `action`/`surface` instead of Stage 2's citron/ink.
- **"See all"**: dark ink text, with only the arrow in a small `action`-blue chip — never
  colored body text, per the "small accent" principle carried over from Stage 2.
- **Bottom navigation** (`BottomNavBar.kt` — explicitly in scope this round as "shared
  navigation", unlike Stage 2 which left it alone): bar background is now `selection`
  lavender (replacing the cream/off-white capsule), selected tab gets a `surface`
  (near-white) circular backing with `action`-blue icon/label, unselected items use
  `ink` at reduced alpha. Shape, floating position, safe-area padding, icons, and content
  inset-via-Scaffold behavior are all unchanged — this affects every tab, not just Home.
- **Area sheet**: restyled to the new tokens; selected row gets a `selection` lavender
  highlight with an `action`-blue checkmark.
- Verified live on `emulator-5554` (installDebug + screenshots, including iterating on two
  real bugs found this way — see below): masthead, search, area control + fallback text,
  Nearby stack (including a missing-photo fallback card and the backing-layer peek), the
  prev/next buttons, Highlights pager, Notices, Events, the area sheet, and — by navigating
  into a Place Detail screen and back — confirmed details are still completely unstyled from
  this change, as intended for this slice. `testDebugUnitTest`, `assembleDebug` and
  `compileKotlinIosSimulatorArm64` all pass.
  - Bug found and fixed during this verification: the Nearby stack's backing layers were
    sized/positioned so the front card fully occluded them (zero visible peek) — fixed by
    giving the container extra height and letting the backing layers show below the front
    card instead of shrinking them to fit inside it.
  - Bug found and fixed: Nearby's zone text wasn't title-cased ("old town" instead of
    "Old Town") — added the same title-casing the rest of Home already used.
- **Not verified**: iOS Home visually (compiles only); enlarged system font scale;
  screen-reader label behavior beyond the contentDescriptions being present in code; the
  other four languages rendered live; actual on-device tap-accuracy of the prev/next buttons
  (confirmed via code review and that the page did advance during testing, but a mis-aimed
  manual tap also hit the card underneath it once — worth a deliberate touch-target check
  before wider sign-off).
- **Explicit checkpoint**: per the brief, this is where we stop. Place/activity detail
  redesign (photo carousel sizing, shared presentation shell, content reordering) has not
  started and should not start until this Home/navigation slice is reviewed against the
  Ronas reference.

## Stage 3b — Differentiate Nearby vs. Must See (2026-10-05)

User feedback: the two sections looked the same and the palette had no contrast color.
Fixed with an explicit per-section spec, not my own first instinct (which had been a coral
badge — scrapped in favor of this).

- **Nearby** ("Around Old Town"): unchanged mechanically (stack, backing-layer peek, area
  selection, paging, favorites, distance behavior). Only change: title bumped 16sp → 20sp
  (`NearbyPlaceCard`). Metadata stays 14sp.
- **Must See**: `FeaturedPlaceCard` rewritten from a 16:10 pager card to a portrait (3:4) tile
  — title moved inside the photo at the bottom behind a dark gradient scrim, save control
  still top-end, no separate white caption area. A missing/failed photo skips the scrim
  entirely and falls back to `HomeImageFallback` (solid lavender, dark readable text) instead
  of white-on-nothing. `MustSeeSection` replaced `HorizontalPager` with a `LazyRow`
  (`fillParentMaxWidth(0.62f)` per tile, 14dp spacing) so the next tile visibly peeks — no
  pagination dots anymore (not needed for a scrollable row). The whole section sits on a new
  `HomePalette.aubergine` (#292433) full-bleed background with a white `SectionHeader`
  (added optional `titleColor`/`labelColor`/`chipColor`/`chipContentColor` params, defaulted
  to the existing look so Nearby/Notices/Events are unaffected).
- **New accent**: `HomePalette.apricot` (#F2A17E), used only for the Must See "See all" chip
  (dark ink arrow on apricot) and the Home masthead's Audio pill (dark ink text on apricot) —
  `HomeMastheadControl` gained optional background/border/content-color params for this,
  defaulting to the prior white-with-lavender-border look so the language (EN) pill is
  unchanged. Not used anywhere else (no rainbow cards, no badges).
- Verified live on `emulator-5554`: Nearby's bigger title, the apricot Audio pill, and the
  aubergine Must See band with peeking portrait tiles (confirmed with a long title —
  "Cathedral of Saint Tryphon" — wrapping correctly inside the scrim) all render as specified.
  `testDebugUnitTest`, `assembleDebug` and `compileKotlinIosSimulatorArm64` pass.
- **Not verified this pass**: iOS visually (compiles only); enlarged system font scale;
  independent save-tap isolation between adjacent peeking tiles (visually plausible since
  each `FavoriteToggleButton` click is scoped to its own card, not separately tap-tested);
  content clearance above the nav bar for the Must See band specifically (Events below it
  showed clear margin in the screenshot, so likely fine, but not a dedicated check).

## Stage 3c — Actual Reference Comparison (2026-10-05)

WebFetch couldn't render the Dribbble shot (JS-rendered gallery, returned empty — confirmed
via Chrome automation too: page loaded, cookie dialog dismissed, repeated waits, but the
shot image never loaded and network logs showed zero image/CDN requests, only ad trackers —
something in this browser profile is blocking Dribbble's asset domain). User attached a
screenshot of the actual shot directly instead.

Comparing it against what was built:
- **Bottom nav bar was wrong** — the reference uses a consistently dark (near-black) pill
  nav across all three of its screens, with a white rounded-square chip behind the selected
  icon. What was built per the earlier text brief was a light lavender bar. Fixed:
  `BottomNavBar.kt` background changed from `HomePalette.selection` to
  `HomePalette.aubergine`; unselected icon/label color flipped from dark-on-light
  (`ink.copy(alpha=0.55f)`, would've been nearly invisible on dark) to
  `HomePalette.surface.copy(alpha=0.6f)`. This also ties together visually with the
  aubergine Must See band already in place. Selected icon/label stayed `action` blue on a
  white chip, unchanged.
- **Deliberately not copied**, per the user's own original ground rule ("adapt the design
  principles, not the artwork, branding, distinctive image masks"): the organic "blob"-shaped
  photo mask on the reference's tour card, and the home screen's lilac-to-purple gradient
  background (current Home canvas stays the flat `HomePalette.canvas` specified earlier;
  revisit only if asked). The reference's dark "Tenerife" destination-listing screen is a
  different screen than Home, not actionable here.
- Verified live on `emulator-5554`: dark nav bar renders correctly on both light (canvas)
  and aubergine (Must See) backgrounds, selected-tab contrast holds on both.
  `testDebugUnitTest` and `compileKotlinIosSimulatorArm64` pass.
- **Not verified**: iOS visually (compiles only).

## Stage 3d — Full Home Pass Against the Actual Reference (2026-10-05)

User feedback: fixing only the nav bar wasn't a real pass at matching the reference — asked
for the whole Home screen to be planned and redone against it. Used `EnterPlanMode`, wrote a
plan covering every section, asked one clarifying question (collapse the full-width search
field into a header icon button, or keep it — user chose the icon button), got the plan
approved, then implemented.

- **Gradient background**: added `HomePalette.violet` (#4A3F6B). The Masthead-through-Nearby
  block (`HomeScreenContent.kt`) is now wrapped in its own `Column` with
  `Brush.verticalGradient(canvas → violet)` sized to that block's natural height — not a
  gradient across the whole unbounded scroll content, which would barely show any change.
  The Must See aubergine band sits right after it, so the gradient now reads as flowing into
  the dark band rather than a disconnected flat-color block. After Must See, Notices/Events
  fall back to the plain `canvas` (no reference equivalent for a mid-page dark→light
  transition, so kept simple rather than inventing one).
- **Header**: removed the full-width `HomeSearchField` entirely (now dead code, deleted) and
  added a circular search icon button (`HomeSearchIconButton`, hand-drawn Canvas magnifying
  glass — consistent with the app's existing icon style in `BottomNavBar.kt`, no new icon
  dependency) between the EN and Audio pills. Verified live that it navigates to Search
  correctly. "Kotor" title, labeled EN/Audio pills, and the area control below are otherwise
  unchanged — this was a decided trade (closer reference match, less discoverable than a
  visible field), not a default guess.
- **Nearby card**: added a list-header row (`NearbyPlaceCard.kt`) — category label + "›" —
  above the photo, echoing the reference's "Portugal · Tours (28) ›" framing. Moved category
  out of the subtitle line to avoid repeating it twice on the same card; subtitle is now
  zone (+ distance when using current location). `NearbyPlacesStack.kt` passes the category
  as `headerLabel`. Stack peek, paging, favorites, area selection, 20sp/14sp type — all
  unchanged, per the standing Nearby spec.
- **Deliberately not copied**, per the user's own ground rule: the organic blob photo mask,
  and inventing a "category tiles row" or "destination list" section not backed by existing
  Home data (the reference's structure doesn't map 1:1 onto Nearby/Must See/Notices/Events).
- Verified live on `emulator-5554`: gradient visibly flows lilac → violet → aubergine behind
  Masthead/Nearby/Must See in one continuous read; Nearby's new header row renders correctly;
  search icon navigates; Notices/Events remain readable on the plain canvas after the dark
  band; bottom nav still reads correctly against both the gradient and canvas regions.
  `testDebugUnitTest`, `assembleDebug` and `compileKotlinIosSimulatorArm64` all pass.
- **Not verified**: iOS visually (compiles only); the area sheet and Must See tiles
  specifically against the new gradient (checked structurally in Stage 3c, not re-screenshotted
  this round — worth a final look before calling this slice done); enlarged font scale;
  screen-reader behavior for the new search icon button beyond the contentDescription being
  present in code.

## Stage 3e — Exact Reference Palette + Clean Icons (2026-10-05)

User feedback: lose the hand-drawn "AI default" arrow/chevron glyphs, lose the orange,
use clean icons — and shared the reference's own branding slide, giving the actual palette
and font (Outfit; #777DB6, #AD9DBE, #1E1B20, #FFFFFF) instead of my earlier approximated hex
values.

- **Palette replaced with the exact reference values** (`HomePalette` in `Color.kt`):
  `action` = #777DB6 (was #38476B), `selection` = #AD9DBE (was #DCD5EA), `ink` = #1E1B20 (was
  #211F29), `surface` = pure #FFFFFF (was #FCFCFD). Dropped `aubergine` and `apricot`
  entirely — `aubergine`'s usages now point straight at `ink` (the reference's dark sections
  and the real brand color are the same near-black, no need for a second near-duplicate dark
  token); `apricot`'s usages (Audio pill, Must See's "See all" chip) now use `action` with
  `surface` content color — orange is gone from the app. `secondaryText` is now derived
  (`ink.copy(alpha = 0.6f)`) instead of a separately invented gray hex, and `canvas` is a
  light tint of the brand's mauve rather than an arbitrary lilac.
- **New `components/HomeIcons.kt`**: `ChevronRightIcon`, `ChevronLeftIcon`, `ChevronDownIcon`,
  `ArrowRightIcon`, `CheckIcon`, `SearchIcon` — small clean vector Canvas icons (simple
  geometric Path strokes, Material-shape-equivalent) replacing every text-glyph icon that had
  crept in: the "›"/"‹" prev-next glyphs (`NearbyPlacesStack.kt`), the "›" on Nearby's
  list-header row (`NearbyPlaceCard.kt`), the "→" in the "See all" chip (`SectionHeader.kt`),
  the "▾" on the area control and the hand-drawn circle+line search icon
  (`HomeScreenContent.kt`), and the "✓" in the area sheet (`HomeScreen.kt`). Still no new
  icon-library dependency — `material-icons-core` is documented elsewhere in this project as
  unavailable for this CMP version, hence hand-drawn Canvas icons in the first place; the fix
  here is icon *quality* (proper stroked vector paths instead of Unicode text glyphs, which
  render inconsistently and look crude at small sizes), not a new dependency.
- Verified live on `emulator-5554`: Home top, Must See band, and the area sheet all re-checked
  — no orange anywhere, every arrow/chevron/check is now a clean vector shape, palette matches
  the reference's own swatches exactly. `testDebugUnitTest`, `assembleDebug` and
  `compileKotlinIosSimulatorArm64` all pass.
- **Not done this round**: switching the type family to Outfit (the reference's branding slide
  names it explicitly) — that requires sourcing/bundling the actual font files as a KMP
  resource, a separate, larger piece of work not implied by "lose the arrows/orange." Flagged
  for the user rather than assumed. Bottom-nav and other pre-existing hand-drawn icons
  (Home/Explore/Saved tab glyphs) were left as-is — not flagged this round, same drawing
  technique as the new icons, revisit only if asked. iOS not re-screenshotted (compiles only).

## Stage 3f — Whole-Screen Gradient, Not Per-Section Patches (2026-10-05)

User feedback: Must See looked awful next to the rest, the gradient should go light purple →
dark purple rather than reverting to white after Must See, padding was inconsistent, the
Nearby prev/next arrows were unnecessary, and the whole screen needed treating as one design
rather than patched section-by-section.

- **One continuous gradient** (`HomePalette.pageGradientStops` = canvas → violet → ink) now
  backs the entire root scrollable `Column` in `HomeScreenContent.kt`, not just the
  Masthead-through-Nearby block. Removed the nested gradient-only sub-`Column` from before.
  `canvas` itself was also redefined from a washed-out near-white (#F2F0F6) to a visibly light
  purple (#C9BFD5), since "light purple to white" was exactly the complaint.
- **Must See no longer has its own background block.** Removed `.background(HomePalette.ink)`
  and its extra `.padding(vertical = 24.dp)` — it now simply sits wherever the page gradient
  has reached by that scroll position, so there's no hard seam between Nearby and Must See.
- **Notices and Events now live in the gradient's dark half too** (previously they fell back
  to flat `canvas`, which was the "goes back to white" problem) — both sections' `SectionHeader`
  calls now use the same light-text styling as Must See, and `InfoCenterPreviewSection`'s row
  text switched from `ink`/`secondaryText` to `surface`/`surface.copy(alpha=0.7f)`. Added a
  `darkZone: Boolean` parameter to `HomeSectionFrame` and `EmptyHomeSection` so their
  loading/error/empty states also switch to light text for these three sections while Nearby
  keeps dark-on-light. `HomeDivider` took an optional `color` param so Notices' divider reads
  on dark instead of using the `selection` tone tuned for light backgrounds.
- **Standardized header-to-content spacing** to 16dp across all four sections (was an
  inconsistent 4/16/4/12dp mix) — part of what read as "padding is off."
- **Removed the Nearby prev/next arrow buttons** per explicit feedback ("not necessary") —
  `NearbyPlacesStack.kt` now shows only the centered page-dot indicator; swiping still works.
  This knowingly relaxes the earlier "accessible previous/next actions" requirement from an
  even earlier brief — explicit new instruction from the user takes precedence, not raised as
  a question since they were asking for direct action, not more back-and-forth.
- Verified live on `emulator-5554` end-to-end, top to bottom: masthead/area/Nearby on the
  light end, a seamless flow into Must See, Recent News and Upcoming Events fully readable in
  white on the dark end, no more hard section boundaries. `testDebugUnitTest`, `assembleDebug`
  and `compileKotlinIosSimulatorArm64` all pass.
- **Not done this round**: iOS visually (compiles only); enlarged font scale against the new
  dark-zone text; whether removing the prev/next buttons needs a different accessibility
  affordance for non-touch navigation (flagging, not assuming, since it was explicitly asked
  to be removed).

## Stage 3g — Consistent Card Borders, Photo-Led Notices, Bottom White-Space Fix (2026-10-05)

User feedback: add the white border to all Home cards for consistency, turn Notices into
photo-led compact cards, and fix a visible white/cream strip showing below the floating nav
bar at the very bottom of the screen.

- **`FeaturedPlaceCard.kt`** (Must See tiles): added the same `.border(1.dp, HomePalette.surface, shape)`
  treatment `NearbyPlaceCard` already had, so Nearby and Must See now read as the same card
  family.
- **`EventCard.kt`**: deliberately left unchanged — it already has its own white fill + shadow
  for card definition, and a white border on a white card would be invisible. Only
  NearbyPlaceCard/FeaturedPlaceCard/the new NoticeCard (all full-bleed photo cards on the dark
  gradient) needed the border to read as distinct cards.
- **Notices redesigned into photo cards** (`InfoCenterPreviewSection` → new `NoticeCard` in
  `HomeScreenContent.kt`): each notice is now a bordered, rounded row card with a small 64dp
  square photo thumbnail (`notice.imageUrl`, falling back to `HomeImageFallback` when absent)
  next to the title/description/meta text — replacing the old plain text-only rows separated
  by `HomeDivider` (which is now unused and was removed). The priority accent bar (urgent/
  important) moved to sit between the photo and the text instead of at the row's far left.
- **Bottom white-space bug fixed**: the Scaffold in `MainShell.kt` had
  `containerColor = KompassTheme.colors.colorSurface` (a light, global token), which showed
  through as a cream strip behind the floating bottom-nav pill's safe-area padding — visible
  on every tab, most obviously against Home's dark gradient. Since the bottom nav pill is
  already a shared, intentionally-dark element (ink `#1E1B20`) across all four tabs, changed
  `containerColor` to `HomePalette.ink` to match — the backdrop directly behind the nav chrome
  is now consistently dark everywhere, not just a Home-specific patch.
- Verified live on `emulator-5554`: Nearby, Must See, and the new Notices cards all show the
  matching white border; Notices now shows the water-outage notice's actual photo in a small
  square thumbnail; the dark background now extends cleanly to the bottom edge of the screen
  with no visible light strip beneath the nav bar. `testDebugUnitTest`, `assembleDebug` and
  `compileKotlinIosSimulatorArm64` all pass.
- **Not verified this round**: iOS visually (compiles only); whether the Scaffold
  `containerColor` change looks correct on Explore/Saved/Essentials specifically (their own
  content wasn't touched this session, so this is just the thin backdrop sliver behind the
  nav bar on those tabs too — plausible but not screenshotted).

## Stage 33 — Place Detail redesign (2026-10-05)

User request: "revisit the details page of a place, and give it a redesign to match our new
pattern" (with a screenshot of the old amber/gold detail screen).

- **Scoping decision**: grepped all call sites first. `PlacePhotoHeader`, `InfoChip`, and
  `DetailNativeMapCard` are each shared by 5-6 other detail-type screens (InfoCenter, Event,
  Service, Experience, ExperienceGuide, LocalFind) beyond Place. Left their defaults
  untouched rather than risk rippling the redesign into unrelated screens — the one exception
  is `InfoChip`, which gained an optional `accentColor: Color? = null` param (falls back to
  the existing orange when null) so Place can pass `HomePalette.action` without affecting any
  other caller's default orange accent chip.
- Redesigned everything exclusive to `PlaceDetailScreenContent.kt`: converted all
  `KompassTheme.colors.*` (old navy/amber palette) to `HomePalette.*` tokens throughout the
  title, chips, audio guide, description, fact cards, and action cards.
- Reading-surface card: removed the old heavily-shadowed, inset, all-corners-rounded floating
  card (`.padding(horizontal = 14.dp)` + `.shadow(18.dp, RoundedCornerShape(28.dp))`). It's
  now a full-width sheet that overlaps the photo with a small negative offset, rounded only on
  its top corners (`RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)`), no shadow — matches
  the "one full-width reading surface... round its upper corners only" brief from earlier in
  the session.
- Back button: replaced the `"‹"` text-glyph `OverlayCircleButton` with a proper hand-drawn
  `ChevronLeftIcon` (new, added to `HomeIcons.kt` alongside `PlayIcon`/`PauseIcon`), on a
  translucent dark-ink circle instead of a white drop-shadowed one — now visually consistent
  with `PlacePhotoHeader`'s own dark page-counter pill in the same header.
- Audio guide play/pause button: replaced the `"▶"` / `"❚❚"` text glyphs with the new
  `PlayIcon`/`PauseIcon` vector composables.
- **Fixed a genuine duplication bug**: `place.localizedLocalsTip(lang)` was being rendered
  twice — once as a generic `DetailFactCard(label = "Local perspective", ...)` before the audio
  section, and again via the dedicated `LocalPerspectiveCard` after it. Removed the redundant
  `DetailFactCard` call; the single `LocalPerspectiveCard` (richer treatment — accent bar +
  colored label) now carries that content once.
- **Reordered content** to match the earlier Ronas-reference brief ("title and practical
  metadata, available inline audio, description/context, local advice, and practical
  actions/map"): Audio Guide now sits right after title/meta/chips instead of after
  Description; the map card moved from right after the chips to the very end, grouped with the
  other practical actions (hours, menu, booking).
- Removed the `HorizontalDivider` between the map and Description in favor of the reading
  surface's own `spacedBy` rhythm (consistent with Home's divider-free, spacing-based layout).
- Introduced one consistent "info card" treatment (lavender tint `HomePalette.canvas.copy(alpha
  = 0.22f)` + `HomePalette.selection` border at 0.35 alpha) applied to the Audio Guide card,
  `LocalPerspectiveCard`, `DetailFactCard`, and `PlaceActionCard` — gives these secondary-info
  blocks visual consistency against the plain white reading surface, since a plain white
  border (Home's pattern) would be invisible on a white background.
- Removed two confirmed-dead private composables: `PremiumGuideCard` and
  `DeepCompanionSection` — neither was called anywhere in `PlaceDetailBody` (leftover from the
  "Removes deep for prod" commit).
- Deliberately did NOT extend Home's full-page purple gradient to Place Detail — kept the
  reading surface on plain white. Reasoning: Home is a colorful discovery/browse surface,
  Detail is a photo-forward read/consume surface; the brief's own language ("reading surface,"
  legibility) and the shared `PlacePhotoHeader`'s photo-first treatment fit a clean white sheet
  better than wrapping body copy in a colored backdrop.
- **Not done this round**: dynamic status-bar icon contrast (making the system clock/battery
  icons switch light/dark depending on what's behind them) — would require platform-level
  expect/actual window-insets-controller code, out of scope for a Compose-only visual pass.
  `DetailNativeMapCard`'s internal "Open in Maps app →" link still shows the old orange accent
  since it's a shared component left untouched.
- Verified live on `emulator-5554` on the Cathedral of Saint Tryphon detail page (has an audio
  file): back chevron, white-bordered-free full-bleed reading surface, periwinkle practical
  line and first-chip accent, lavender audio/fact/local-perspective cards, vector play icon,
  single (deduped) Local perspective card, and the reordered Description → More Context → Local
  perspective → Practical → Restaurant Info → Map flow all render correctly.
  `testDebugUnitTest`, `assembleDebug`, and `compileKotlinIosSimulatorArm64` all pass.
- **Not verified this round**: iOS visually (compiles only); a place with no `audioFile` (the
  conditional branch is untouched existing logic, just not click-tested on-device this round);
  the `EAT_AND_DRINK`-only Restaurant Info section with menu/booking cards (no on-device place
  with those fields was opened this round, but the code path and styling are unchanged in
  shape from the file's prior working version, just recolored).

## Stage 34 — Map card touch-up on Place Detail (2026-10-05)

User flagged (via annotated screenshots) that `DetailNativeMapCard` still looked like the old
theme after Stage 33 — orange "Open in Maps app →" text link, neutral border.

- Added four optional override params to the shared `DetailNativeMapCard`
  (`titleColor`, `bodyColor`, `actionColor`, `borderColor`, all `Color? = null`, falling back
  to the existing `KompassTheme.colors.*` values) — same safe-extension pattern as `InfoChip`'s
  `accentColor`. The 5-6 other callers (InfoCenter, Event, Service, Experience, LocalFind) are
  unaffected.
- Place Detail now passes `HomePalette.ink` / `HomePalette.secondaryText` / `HomePalette.action`
  / `HomePalette.selection.copy(alpha = 0.35f)` — matching the lavender-tinted-border family
  used by the other info cards on this screen.
- Replaced the plain `"Open in Maps app →"` text glyph with real text + the existing
  `ArrowRightIcon` vector (reused from `HomeIcons.kt`, same icon already used in Home's
  "See all" links).
- **Deliberately left the native map tile styling alone** (cream/tan land, default Google
  pins) — that's the platform map SDK's own default style, not a Compose color token; reskinning
  it would need a custom map-style JSON threaded through the `NativeDetailMapView` expect/actual
  on both Android and iOS, which is out of scope for a color/icon touch-up and would affect
  every other screen using this shared map view too.
- Verified live on `emulator-5554`: the map card's title, summary, and "Open in Maps app"
  link + arrow icon now read in ink/periwinkle instead of navy/orange, with a lavender card
  border consistent with the rest of the screen. `testDebugUnitTest`, `assembleDebug`, and
  `compileKotlinIosSimulatorArm64` all pass.

## Stage 35 — Pilot Reliability Batch 1: UI defects (2026-10-05)

User supplied a larger "Pilot Reliability and Release Readiness" spec with four sections
(UI defects, data/offline, navigation/audio/location, release gates), explicitly to be done in
small reviewable batches. Confirmed with the user before starting: no apricot accent — stay on
the periwinkle/`HomePalette` system already built. This covers **Section 1 only** (UI defects).

- **Fixed a real category-mislabeling bug**: `toEditorialPlaceCategory()` (Home's Must See
  meta) and `toStackCategoryLabel()` (Nearby's stack meta) both hardcoded `SEE_AND_VISIT ->
  "Historic Site"` for every sightseeing place regardless of its actual type, ignoring
  `place.subCategory` entirely — this was visibly wrong on-device (e.g. "Historic Site" shown
  under a cathedral whose own `subCategory` is literally "Cathedral"). Replaced both with a new
  shared `Place.localizedTypeLabel(strings)` (`core/util/PlaceCategoryLabels.kt`): prefers the
  place's own `subCategory` (title-cased), falls back to a localized category label only when
  `subCategory` is blank. `PlaceDetailScreenContent.kt`'s chip-fallback path and the old
  per-file `PlaceCategory.prettyLabel()` duplicate were folded into the same shared helper.
- **Localized the category fallback labels**: added `categoryEatAndDrink` /
  `categorySeeAndVisit` / `categoryActivities` / `categoryHiddenGems` / `categoryPractical` to
  `RedesignStrings` (all 5 languages) — these were previously hardcoded English literals in
  Compose code (not CMS content), so this was a code-level fix, in scope. **Not fixed, and not
  in scope**: `subCategory`/`zone`/`tags` are raw, non-localized free text from the CMS
  (`Place.subCategory: String?`, no JSONB translation map) — genuinely untranslatable without a
  CMS schema/content change. Recording as a content gap for separate follow-up, per the spec's
  own instruction not to modify CMS records from here.
- **Fixed the "infer text color from gradient position" contrast bug**: Must
  See/Notices/Events section headers and their loading/empty/failed placeholder text hardcode
  white text assuming they land on the gradient's dark half (`darkZone = true`). Since the page
  gradient's brush stops are fractions of total scrollable content height, a short section above
  (e.g. an empty/loading Nearby) shifts everything below it, and white text can end up on the
  gradient's light half with no contrast. Added a local translucent ink scrim
  (`HomePalette.ink.copy(alpha = 0.34f)`, `localSectionScrim()` in `HomeScreenContent.kt`) behind
  every `darkZone` heading/metadata block and `SectionHeader` (new optional
  `localSurfaceColor` param) — contrast is now guaranteed by the panel itself, independent of
  gradient position. Nearby's header was left alone (always first, provably always on the light
  half, no bug there).
- **Explicit image loading/success/missing/failure handling** in `NearbyPlaceCard` and
  `FeaturedPlaceCard` (previously only checked for a blank/missing URL — a URL that 404'd or
  failed to decode just rendered nothing under the scrim, an unstyled gap). Both now track
  Coil's `onError` callback; a failed load gets the exact same compact fallback treatment as a
  missing URL. A neutral tinted placeholder sits under the `AsyncImage` while a real photo is
  loading, so there's never a frame where the scrim draws over blank content.
- **`HomeImageFallback` gained an optional `secondaryLabel`** so the compact fallback (missing
  or failed photo) shows title *and* metadata, not just the title — matches the spec's "title,
  metadata, and save action" for a compact Nearby card. (Save action was already present outside
  the hasPhoto/fallback branch in both cards — unaffected.)
- **`NearbyPlacesStack` now sizes to the current page's presentation**: each page's compactness
  (no URL, known upfront; load failure, reported back via a new `onCompactChange` callback once
  Coil resolves) is tracked per-index. The pager's height animates between `fullCardHeight`
  (380dp) and `compactCardHeight` (150dp) based on the *current* page, and the decorative backing
  peek layers are suppressed entirely while the current page is compact (and skipped individually
  for any peeked page that is itself compact) — a stack-of-photos effect doesn't make sense
  without a photo. Result order (`visible = places.take(4)`) is unchanged.
- **Found and fixed an unrelated but real theming bug while diagnosing a nav-bar visual
  artifact** (see below): `MainShell.kt`'s `Scaffold` lost its explicit `containerColor`
  mid-session (edited down to nothing) — Material3's Scaffold default `containerColor` is
  `MaterialTheme.colorScheme.background`, which is still `KOmpassTheme.kt`'s old warm off-white
  token (`colorSurface`/`colorWarmOffWhite`, `#F8F3EA`) since that theme hasn't been touched by
  the redesign. This leaked the old cream color into any gap around the new dark floating nav
  bar. Restored `containerColor = Color.Transparent` explicitly (with a comment explaining why
  it must stay explicit) so each screen's own content/background shows through naturally instead
  of a stale theme color.
- **Nav bar floating-pill fix** (user-reported, with screenshots, mid-session): a light/white
  rounded-rect haze was visible behind/around the floating bottom nav bar on both Android and
  iOS. Root-caused to each platform's native root background (`Theme.Material.Light`'s
  `windowBackground` on Android — confirmed by pixel-sampling the haze and matching it exactly
  to the theme's default; SwiftUI's default view background on iOS) showing through any pixel
  Compose doesn't explicitly paint. Fixed at the root: added `composeApp/src/androidMain/res/
  values/themes.xml` (`AppTheme` extending `Theme.Material.Light.NoActionBar`, `windowBackground`
  set to `HomePalette.ink`) + `AndroidManifest.xml` now references `@style/AppTheme`; iOS
  `ContentView.swift` gained a matching `.background(Color(ink))`. Also moved
  `navigationBarsPadding()` on `BottomNavBar.kt`'s pill to apply *inside* the
  shadow/clip/background chain (not as outer Box padding) so the pill's own ink shape physically
  extends through the system inset instead of stopping short of it. Also tried (and kept, though
  it turned out not to be the actual cause) `window.isNavigationBarContrastEnforced = false` +
  explicit `window.navigationBarColor = TRANSPARENT` in `MainActivity.kt` — harmless, standard
  edge-to-edge hygiene, left in place.
- Verified live on `emulator-5554` with pixel-level sampling (not just visual inspection) at
  each step: confirmed the haze's exact RGB before each fix attempt, confirmed `(30,27,32)` —
  exactly `HomePalette.ink` — uniformly behind/around the nav bar after the final fix, both at
  the top of Home (light gradient zone) and scrolled to the dark zone. Also checked the
  not-yet-redesigned Explore tab to confirm the now-transparent Scaffold doesn't regress screens
  outside this redesign's scope. `testDebugUnitTest`, `assembleDebug`, and
  `compileKotlinIosSimulatorArm64` all pass.
- **Not verified this round**: iOS visually (ContentView.swift change compiles but wasn't
  run on a simulator/device this round — the user's own screenshot showing the same haze on what
  looked like an iPhone was the evidence it affected iOS too, pre-fix); Must See's compact
  fallback path and Nearby's load-failure path specifically (no on-device place with a broken
  photo URL was available to click-test this round — verified by code review and the shared
  code path with the missing-URL case, which was click-tested).

## Stage 36 — Lamplight rewrite, Step 1: Design tokens (2026-10-05)

First step of the approved "Lamplight" Home rewrite plan (see
`~/.claude/plans/humble-wishing-ritchie.md` for the full plan — Amplitudo-style architecture,
Home-scoped; full new visual spec superseding the earlier periwinkle `HomePalette` redesign).

- **Fonts sourced and bundled**: Gloock (400 only) downloaded directly from google/fonts.
  Geologica is variable-only upstream (no static weight files in the repo) — instanced it into 5
  static TTFs (300/400/500/600/700) with `fonttools varLib.instancer`, pinning the non-weight axes
  (CRSV=0, SHRP=0, slnt=0) to their defaults. All 6 files added to
  `composeApp/src/commonMain/composeResources/font/` (`gloock_regular.ttf`,
  `geologica_{light,regular,medium,semibold,bold}.ttf`) — bundled resources, not downloadable
  fonts, per CLAUDE.md.
- **`presentation/theme/LamplightColors.kt`** (new) — `LamplightColors` data class (not a plain
  `object`, per the spec's explicit ask for a swappable token set) with all 15 tokens from spec
  §1.1, a `CompositionLocal`, and `ProvideLamplightTheme { }` + `LamplightTheme.colors` accessor
  mirroring the existing `KompassTheme.colors` pattern. The spec's alternative dark-partner
  swatches are documented in a comment but not wired up ([NEEDS APPROVAL], staying on `night`).
- **`presentation/theme/LamplightTypography.kt`** (new) — the 11 named styles from spec §1.2
  (`areaTitle` through `navLabel`), each a fixed size+lineHeight+weight `TextStyle`, all in `.sp`
  so they scale with system font size. `bodyS`'s spec'd 350-400 weight range simplified to 400
  (no 350 static instance was generated); `body`'s 500-600 range defaults to 500, callers can
  `.copy(fontWeight = ...)` for the 600 case.
- **`presentation/theme/LamplightShapes.kt`** (new) — `LamplightDimens`/`LamplightShapes` objects
  for spec §1.3's radii/spacing, including Notices' deliberately-square `0.dp` shape.
- Verified with a throwaway preview screen (temporarily swapped into `App.kt` in place of
  `KompassNavHost`, screenshotted on `emulator-5554`, then fully reverted and deleted): Gloock's
  serif display and Geologica's sans both render correctly at the right weights/sizes; color
  swatches match the spec's hex values exactly.
- `testDebugUnitTest`, `assembleDebug`, and `compileKotlinIosSimulatorArm64` all pass after the
  revert (confirmed the throwaway file left no trace).
- **Not done this round**: nothing from this step is wired into any real screen yet — these are
  pure token definitions, unused until Step 2 onward.

## Stage 37 — Lamplight rewrite, Step 2: Architecture skeleton (2026-10-05)

Second step of the "Lamplight" rewrite plan: the Amplitudo-shaped MVI pattern and command-bus
`Navigator`, scoped to Home only, wired to the pre-existing (pre-visual-rewrite) UI so the only
expected change this round was internal — confirmed pixel-identical on-device.

- **New `core/presentation/mvi/` package**: `BaseAction`/`BaseEvent`/`BaseViewState` (marker
  interfaces), `BaseViewModel<State, Action, Event>` (replaces the single-`Event`,
  `Channel<String>`-side-effect `core.presentation.base.BaseViewModel<S,E>` *for Home only* — the
  other 26 ViewModels on the old base class are untouched), and `Navigator`/`NavigatorImpl` (a
  `Channel<NavigationCommand>`-backed command bus, registered as a Koin `single`).
- **`NavigatorBridge.kt`** (new, `presentation/navigation/`) — one composable, invoked once from
  `MainShell()`, collects `navigator.commands` and applies them to whichever per-tab
  `NavHostController` is currently selected. The only place the new Navigator touches the
  existing nav host; every other screen still navigates via callback-threading, untouched.
- **Home rewritten end to end**: `HomeAction.kt` (new, takes over user-intent duties from
  `HomeEvent`), `HomeEvent.kt` (repurposed to one genuine one-off effect,
  `TriggerLocationPermissionRequest`, replacing the old synchronous-looking "show dialog, read
  prefs inline" flow), `HomeState.kt` (implements the new `BaseViewState`, gained
  `showLocationPrompt: Boolean` — business-state, not UI-state, since its initial value depends
  on permission + preference reads), `HomeViewModel.kt` (extends the new `BaseViewModel`, takes
  `Navigator` + `AnalyticsRepository` + `FavoritesRepository` as constructor params — all three
  were previously injected directly into `HomeScreen.kt` and used inline there).
- **Fixed two real bugs found during the architecture review** (not just moved code):
  `GetNearbyPlacesUseCase` gained optional `category`/`maxDistanceKm` params (safe defaults,
  `NearbyPlacesViewModel`'s existing call sites unaffected) so Home's category/radius filtering
  and sort happen once, in the use case, instead of being redundantly re-applied client-side in
  the ViewModel on top of the use case's own sort. New `GetNearbyFallbackPlacesUseCase` replaces
  a direct `PlaceRepository.getActivePlaces()` call that previously sat inline in
  `HomeViewModel`, bypassing the use-case layer entirely for the zone-matched (no-GPS) path.
- **`HomeScreen.kt` is now genuinely orchestrator-only and callback-free**: no more
  `onNavigateToX` params threaded from `MainShell.kt` (it's just `HomeScreen()` now), no inline
  analytics-tracking lambdas, no full `AlertDialog`/`ModalBottomSheet` UI sitting in the
  "orchestrator." Those moved into `HomeScreenContent.kt` (area-sheet open/close as local
  `remember` state — genuinely presentational; the location-prompt dialog driven by
  `state.showLocationPrompt` since that's business state) and into `HomeViewModel.onAction`
  (analytics calls, navigation, favorite toggling) respectively — same rendered UI, relocated to
  the right layer.
- **Caught and fixed a real constructor-ordering bug while verifying on-device** (not caught by
  compilation or unit tests — only surfaced as a runtime crash): `BaseViewModel`'s `init` block
  launched `onFirstStart()` via `viewModelScope.launch { }`, whose default dispatcher
  (`Dispatchers.Main.immediate`) runs synchronously inline when already on the main thread —
  meaning it executed *during* the superclass constructor, before `HomeViewModel`'s own
  `private val jobs = mutableMapOf(...)` property initializer (part of the *subclass*
  constructor body, which Kotlin/JVM runs *after* the superclass's `init` block) had run,
  producing a `NullPointerException` on `jobs[section]` at every cold start. Fixed by explicitly
  launching with `Dispatchers.Main` (a real post) instead of the inherited `.immediate`, so
  `onFirstStart()` only runs once the full object graph — concrete subclass included — has
  finished constructing.
- **Found and removed unrelated stray files**: 5 Dagger/Hilt-based files
  (`MviEvent.kt`/`MviScreen.kt`/`MviScreenContent.kt`/`MviState.kt`/`MviViewModel.kt`, some
  already `git add`-staged) had landed in the same new `core/presentation/mvi/` package —
  leftover boilerplate from the wrong (native-Android Hilt) skill variant, confirmed with the
  user before deleting since they were staged, not something I created.
- **`HomeRedesignTest.kt` updated** for the new API (`onIntent`→`onAction`,
  `HomeEvent.RetrySection/SelectArea`→`HomeAction.*`, new constructor params with lightweight
  `TestAnalytics`/`TestFavorites` fakes + a real `NavigatorImpl()`) — all 5 original tests still
  pass unmodified in substance. Added 2 new direct tests for the two use-case fixes
  (`nearbyPlacesUseCaseAppliesCategoryAndRadiusFilterInOneSortedPass`,
  `nearbyFallbackUseCaseZoneMatchesWithoutFabricatingDistance`).
- Verified live on `emulator-5554`: Home renders pixel-identical to before this step (same
  gradient, same cards, same copy) — confirmed by direct screenshot comparison, not just visual
  impression. Exercised the full new chain end-to-end: area sheet opens/selects correctly
  (local UI state), tapping a Nearby card navigates to Place Detail through
  `HomeViewModel.onAction → Navigator.navigateTo → NavigatorBridge → NavController` (not the old
  callback-threading path), and Android Back from the detail screen returns cleanly to Home in
  its prior scroll state — the navigation contract (per-tab stacks, back behavior) holds.
  `testDebugUnitTest`, `assembleDebug`, `compileKotlinIosSimulatorArm64` all pass.
- **Not verified this round**: iOS visually (compiles only); the location-permission-prompt
  dialog and permission-grant retry path specifically (no on-device test of denying/granting
  location this round — covered by the existing/updated unit tests, but not click-tested live);
  `ToggleFavorite`'s new ViewModel-routed path (favorite icon not tapped live this round, though
  its wiring is a direct, low-risk port of the prior one-line callback).

## Stage 38 — Lamplight rewrite, Step 3: FloatingNavBar (2026-10-05)

Third step of the Lamplight plan: the first thing that actually looks visually different —
the global bottom nav bar, restyled to Lamplight tokens with the new center audio action.

- **`presentation/navigation/BottomNavBar.kt` restyled in place** (spec §3.6): `navBar`
  (`#262B2F`) fill, `chalk` selected chip + bold label, `textTertiary` unselected, `radiusNav`
  (28dp) shape, drop shadow. Global chrome shared by all 4 tabs — Explore/Saved/Essentials
  inherit the new look automatically; their own screen content is untouched.
- **New 5th center slot**: a 62dp `lamp`-filled circle with a hand-drawn headphones glyph,
  `contentDescription = "Audio"` — not a tab (`BottomTab` enum unchanged, still 4 entries), does
  not participate in `selectedTab`/back-stack logic, per spec. Wired in `MainShell.kt` to
  `controller.navigate(Route.MyGuides)` on whichever tab is currently selected — a plain
  callback, not the new `Navigator` (this is shared global chrome, not Home-exclusive, so it
  stays consistent with how the other 3 tabs already navigate).
- **Fixed a real bug found while touching this file**: `Essentials` silently reused `Explore`'s
  icon (`drawTabIcon`'s `when` had no `Essentials` branch, falling through to
  `drawExplore(color)`). Added a proper `drawLifebuoy` (two concentric rings + 4 tick marks).
  Also replaced Explore's old icon (four small outlined squares — a dashboard/grid glyph) with
  `drawCompass` (ring + kite-shaped needle), matching the spec's explicit icon list (§1.4:
  "compass (Explore)... lifebuoy (Essentials)").
- **`LamplightColors`/`LamplightTheme` wired into the real app for the first time**:
  `MainShell()` now wraps its `Scaffold` in `ProvideLamplightTheme { }` — Steps 1-2 only defined
  the tokens, nothing consumed them yet. Scoped tightly around the Scaffold (not the whole
  composition), consistent with "Home + shared nav chrome only," not a global theme replacement.
- **Simplification noted, not a spec deviation worth blocking on**: kept the existing
  chip-behind-selected-icon treatment (matches the reference mockup) rather than building true
  filled/outline icon pairs for every glyph — selected/unselected is conveyed by the chip +
  color + label weight, not by swapping icon rendering style. Exact height (spec says 78dp) is
  content-driven (icon + label + padding) rather than a hardcoded value, to preserve the
  edge-to-edge-safe-area-extension fix from Stage 36 — lands within a few dp of spec, prioritized
  over risking that regression.
- Verified live on `emulator-5554`, methodically this time: an initial coordinate-reading
  mistake (eyeballing tap positions off the scaled-down screenshot preview instead of computing
  from the stated 1.21× display-to-raw ratio) briefly looked like every nav bar tap was dead —
  resolved by pulling exact element bounds via `adb shell uiautomator dump` rather than
  continuing to guess, which confirmed the wiring was correct all along. With accurate
  coordinates: all 4 tabs switch correctly and show the right selected state (including Explore's
  new compass and Essentials' new lifebuoy), the center Audio button opens My Guides without
  affecting tab selection, per-tab back stacks persist independently across tab switches (Home
  retained its earlier Place Detail position after visiting 3 other tabs), and repeated-tap on
  the already-selected tab pops to that tab's root. `testDebugUnitTest`, `assembleDebug`,
  `compileKotlinIosSimulatorArm64` all pass.
- **Not verified this round**: iOS visually (compiles only); full-screen map/photo screens
  hiding the bar (`fullScreen` check is unchanged code, not touched this round, so low risk but
  not re-confirmed live); onboarding hides the bar (same — unchanged code path).

## Stage 39 — Lamplight rewrite, Step 4: LamplightTopBar (2026-10-05)

Fourth step of the Lamplight plan: the top bar, replacing both `HomeMasthead` and
`HomeAreaControl` with one cohesive component (deleted both, plus their now-dead
`HomeSearchIconButton`/`HomeMastheadControl` helpers).

- **New `components/LamplightTopBar.kt`** (spec §3.1): `lamp`-filled bar, `KOMPASS` wordmark
  (0.3em letter spacing, distinct from the general `label` style's 0.12em — needed its own
  override), a globe-icon language pill, a big Gloock "Around / {area}" title that opens the
  area picker, a `night`-filled search field (placeholder "Search Kotor and the bay" — updated
  `AppStrings.searchPlaceholder`, confirmed unused by the actual Search screen itself so this
  was safe to repoint), a status line, and a faint decorative zigzag (`contentDescription =
  null`, droppable without loss per spec).
- **Per owner decision (spec §7), Audio is no longer in the top bar** — it was already added to
  the nav bar in Step 3; the old masthead's amber "Audio" pill is simply gone now, not replaced.
- **Status line logic implemented per spec**, covering both cases the old code only partially
  handled (old code only explained a *failed* device-location fallback; manual-area selection
  got no explanation at all): manual area → "Chosen area · use my location"; device location
  denied/unavailable (and not already loading) → the existing fallback copy + the same link.
  Both states share one "use my location" link that dispatches the existing
  `HomeAction.SelectArea(HomeArea.DEVICE)` — no new action needed, and it only ever fires on an
  explicit tap, never automatically.
- **New strings** (`RedesignStrings`: `aroundLabel`, `oldTownShort`, `chosenAreaStatus`,
  `useMyLocationLink`, all 5 languages) plus a new `HomeArea.shortLabel()` so the big title shows
  just "Old Town" rather than the existing `label()`'s full "Kotor / Old Town" breadcrumb (kept
  `label()` itself unchanged — still used by the area-picker sheet's own rows).
- **New `GlobeIcon`** added to `HomeIcons.kt` (circle + meridian ellipse + latitude line),
  following the file's existing hand-drawn-Canvas convention.
- **Found and fixed a real text-clipping bug while verifying live**: the area title Row had no
  `fillMaxWidth()`/width constraint, so a long label ("Device location") measured as if infinite
  width was available and got clipped at the physical screen edge instead of wrapping. Confirmed
  by screenshot, fixed by constraining the Column to `fillMaxWidth()` and giving the title
  `Modifier.weight(1f, fill = false)` + `maxLines = 2` so it wraps properly while the chevron
  stays put — reverified live afterward (clean two-line wrap, no clipping).
- Verified live on `emulator-5554`: wordmark/language pill/zigzag render correctly; "Old Town"
  (short label, fits on one line) and "Device location" (long label, now wraps to two lines)
  both confirmed; search field renders and is tappable; status line correctly shows "Chosen
  area · use my location" when a manual area is selected and correctly shows *nothing* when
  device location is active with a successful (if empty) result — did not incorrectly fire the
  "unavailable" fallback message for a legitimate empty-results case. Area picker sheet opens
  and selects correctly through the new top bar. `testDebugUnitTest`, `assembleDebug`,
  `compileKotlinIosSimulatorArm64` all pass.
- **Observed, not investigated (unrelated to this round's changes)**: the Cathedral of Saint
  Tryphon's photo intermittently rendered as a line-art building illustration instead of the
  real photo in a couple of screenshots this round — looked like a Coil
  error/fallback drawable, not the app's own `HomeImageFallback` (lavender panel) — but was not
  reproducible on demand and `NearbyPlaceCard`/`FeaturedPlaceCard` weren't touched this step.
  Worth a look in a future round if it recurs.
- **Not verified this round**: iOS visually (compiles only); dark status-bar-icon contrast
  against the bright `lamp` background specifically (spec §3.1 calls this out explicitly) — the
  status bar time/icons were legible in every screenshot taken, so no fix was needed, but this
  wasn't stress-tested against every background transition.

## Stage 40 — Lamplight rewrite, Step 5: NoticeItem, HomeEventCard, MustSeeCard (2026-10-05)

Fifth step: the three remaining list/row card types, each built with image-missing/empty-field
handling from the start, per spec §3.3–§3.5.

- **New `components/MustSeeCard.kt`** — `limestone`-filled card, 196dp crop-to-fill image
  (`surfaceMuted` block on missing/failed load, via the same `onError`-tracked pattern as the
  rest of this session's cards), centered area label / title / description, and a "Save for
  later +" outline pill that becomes "Saved" + filled bookmark. Description hides its own line
  when the CMS field is blank — never generated.
- **New `components/NoticeItem.kt`** — square corners (deliberately, per spec — "reads as
  operational, not discovery"), `alert`-bordered for Important, solid `alert` left rule added
  for Urgent, `surfaceRaised` fill for General. New `NoticeLevel` enum.
- **New `components/HomeEventCard.kt`** — Home-exclusive, confirmed *not* a restyle of the
  shared `components/EventCard.kt` (which the standalone Events screen keeps using untouched).
  124dp image with a `lamp`-filled date-tag pill, `surfaceMuted` fallback block (tag stays
  visible) on missing/failed image, no save/favorite affordance per spec.
- **Events section changed from a vertical list to a horizontal `LazyRow`** (168dp cards, 12dp
  gap) — the spec is explicit about this ("Horizontal row, cards 168dp wide") and the prior
  Column-based layout predates the Lamplight spec entirely.
- **New icons** (`HomeIcons.kt`): `BookmarkIcon(filled: Boolean, ...)`, `PlusIcon`,
  `WarningTriangleIcon`, `InfoCircleIcon`.
- **New shared `core/util/DateTimeFormatters.kt`**, finally doing the formatter consolidation
  planned (but deferred) in Step 2: replaces 4 near-identical `Instant.parse(...)` helpers that
  were duplicated across `HomeScreenContent.kt`. Fixed a real latent bug in the process — the
  old code's month formatting called itself part of a "localized" meta line but actually used
  `Month.name` (always English) regardless of the app's language; the new `toMonthAbbrev(lang)`
  has a real 5-language abbreviation table. (Also hit and resolved a kotlinx-datetime 0.7.1 API
  wrinkle: `Month.number` needed an explicit `import kotlinx.datetime.number` to resolve as a
  top-level extension property — the deprecated `LocalDate.monthNumber` shortcut was avoided.)
- **New Home-scoped `HomeCardFormatting.kt`**: `Event.toHomeEventMeta()`/`toRecurrenceSuffix()`,
  `Place.toMustSeeAreaLabel()` (the matched area's short name, or "Worth the Drive" when the
  place's `zone` doesn't match any of the three curated areas — derived only from that static
  CMS field, since Must See items carry no live-GPS distance the way `NearbyPlace` does),
  `InfoNotice.toNoticeLevel()`/`toNoticeLabel()` ("Important · until 12 Oct", hiding the "until"
  segment when `endsAt` is absent — never fabricated). Deleted a genuinely dead
  `Place.toMustSeeMeta()` duplicate found during the cleanup (same logic as `NearbyPlacesStack`'s
  own `toStackMeta`, never actually called from anywhere else).
- **New strings**: `RedesignStrings` gained `noticeImportant`/`noticeGeneral`/`noticeUrgent`/
  `noticeUntil`/`eventRecurringWeekly` (all 5 languages); reused the already-existing
  `worthDrive` string for the Must See area-label fallback instead of adding a duplicate.
- Verified live on `emulator-5554`: Must See cards render with correct area labels, centered
  Gloock titles, descriptions, and working save-toggle pills; the Important notice renders with
  its alert border and warning icon; Events now scroll horizontally with date tags, time ranges,
  and arrow buttons all correct. `testDebugUnitTest`, `assembleDebug`,
  `compileKotlinIosSimulatorArm64` all pass.
- **Not verified this round**: iOS visually (compiles only); a General (non-alert) notice's
  rendering (only an Important notice was present in the seed data visited this round — code
  path reviewed, not click-tested); a "Worth the Drive" Must See item (no such item was present
  in the visible data this round); the saved/bookmarked state of `MustSeeCard`'s pill button
  (not tapped live this round).

## Stage 41 — Lamplight rewrite, Step 6a: Nearby front card + pager, arch mode (2026-10-06)

Built from the "Must See card + cut-out pipeline" spec, Part B (card geometry B2, rendering rules B8).

- **New `components/NearbyCard.kt`**: 338×410 `lamp` card, 26dp radius. Gloock title (`cardTitleXL`,
  max 4 lines) plus meta inside the title safe zone (x 22→218). Bottom row (18dp margin): chalk pill
  showing **Listen** + headphones when the place has audio in the current language, else **Open** +
  arrow. Next to it, a 56dp chalk save circle with a bookmark. The button-row layout is inferred from
  `render_preview()` in the cut-out script because the spec text doesn't describe it: check it
  against the mockup. B8 rule 7 is implemented: 4+ title lines or font scale ≥1.3 narrows the slot to
  50%, and a text block reaching past y=160dp drops the image.
- **New `components/CutoutSlot.kt`**: `SlotMode.Arch(url)` / `SlotMode.None`. The photo is
  crop-to-fill in a semicircle-topped arch, with two `night` shadow stamps at (−10,−6) and (−10,0).
  Nothing is drawn until the image loads, and a failed load leaves the slot empty (rules 5/6). A
  chalk ground under the photo keeps transparent PNGs, such as the cathedral line drawing in the
  seed data, from showing as a solid black arch. `SlotMode.Cutout` is deliberately absent until
  the B7 fields are approved.
- **New `components/NearbyStackCarousel.kt`**: `HorizontalPager` with a 26dp content-padding peek
  and 12dp spacing. Neighbours scale to 0.94 and fade via `getOffsetDistanceInPages`. The old static
  backing layers are gone. Dots use `lamp`/`indicatorInactive` and are windowed to 5 by a pure
  `dotWindow()`. A polite live region announces "Item n of total". No auto-advance.
- **`HomeAction.ListenClicked`** → `Route.PlaceGuide(id, autoplay = true)` plus `trackPlaceView`
  with origin `nearby_listen`. Place-view tracking was factored into one private helper.
- `NearbyPlace.toNearbyMeta()` moved to `HomeCardFormatting.kt`, reusing `toTitleCaseLabel()`.
  The private `toStackZoneLabel` duplicate is gone.
- **Deleted**: `NearbyPlacesStack.kt`, `NearbyPlaceCard.kt`. `HomeImageFallback.kt` stays because
  `FeaturedPlaceCard` still uses it (Step 7).
- **`tools/cutout/`**: `make_cutout.py` (as supplied) plus a README. It runs offline and is not
  ported to Kotlin: it needs rembg/BiRefNet, and spec B6 rules out on-device use. Its `CardSpec`
  mirrors `NearbyCard`'s constants.
- New icons: `HeadphonesIcon`, `LandmarkIcon`. New strings in all 5 languages: listen, open,
  carouselItemOf, saveTitle, removeTitleFromSaved, notSaved, saveAction, removeFromSavedAction.
- Verified on `emulator-5554` (Medium_Phone): paging and peek, arch photo and shadow, a no-photo card
  (Kotor) with an empty slot, live distances in Device mode, zone meta in Old Town mode, **Listen**
  on the cathedral (the only place with `en` audio) opening the Audio Guide with autoplay, and dots.
  Vertical drags that start on the carousel don't change the page.
- **Not verified this round**: iOS visually (compiles only); TalkBack announcements (semantics are
  in code but no screen reader was run); font scale 1.3 and a long DE title (slot-narrowing path
  reviewed, not run); a >5-item list live (`HomeViewModel.NEARBY_LIMIT` is still 4, so windowing
  is unit-tested only).

## Stage 42 — Lamplight rewrite, Step 6b: Must See aligned to spec Part A (2026-10-06)

- **Fixed hardcoded English** "Saved"/"Save for later" → `RedesignStrings.mustSeeSaved`/`mustSeeSave`.
- Saved state: the button is filled `night` with chalk text and a filled bookmark. Unsaved: a 1.5dp
  border and a plus icon. Text is 15sp/600 and icons 16dp. Haptic tick on toggle. Card-level custom
  accessibility action (Save / Remove from saved). The button is a separate focus target with
  "Save {title}" and a Saved/Not saved state description.
- Images load on `limestoneShade` (#C9CAC3, new token). A missing or failed image becomes a 96dp
  block with a 24dp `LandmarkIcon` in `landmarkGlyph` (#7A7F78, new token) instead of 196dp of
  empty colour.
- The row is now `Row(height(IntrinsicSize.Max)).horizontalScroll`, so cards share the tallest
  height and the buttons line up. Known trade-off: no snap-fling, since Compose's snap providers
  are lazy-only. Cards widen to 264dp at font scale ≥1.3.
- Area label: `toMustSeeAreaLabel` returns null for a place with no zone (hidden, not guessed). New
  `String.uppercaseFor(lang)` handles Turkish İ.
- Section: hidden entirely when loaded and empty. **"See all" removed** per spec A5. Note:
  `Route.MustSeePlaces` does exist, and `HomeAction.SeeAllMustSee` is kept, so restoring the link is
  one line if product confirms it. New header: Gloock 32 "Must See" plus a right-aligned
  `THE WHOLE BAY` label.
- Verified live: equal-height cards with aligned buttons; saved/unsaved toggle round-trips to
  `favorites_v1` and the scroll position is kept; the 96dp fallback isn't present in the seed data
  (every Must See item has a photo).
- **Deferred** (Home has no snackbar host yet): undo snackbar on unsave, revert-on-failure, and the
  "no longer available" snackbar. Pressed-state overlays use the default clip-bounded indication.
- **Open visual issue (pre-existing)**: the page still uses the old `HomePalette` purple gradient
  rather than Lamplight `night`, so the chalk "Must See" title and the `textSecondary` label land on
  a mid-light purple with weak contrast. That belongs to the page-background pass, not this step.

## Stage 43 — Owner tweaks on Landmarks / News / Events (2026-10-06)

- **Must See → "The Landmarks"**: new `RedesignStrings.landmarks` in all 5 languages (Les monuments /
  Die Wahrzeichen / Los monumentos / Simge yapılar). The shared `AppStrings.mustSee` is untouched
  because other screens use it.
- **Smaller Landmarks cards**: 196dp wide (224dp at font scale ≥1.3), 140dp photo, 72dp
  missing-photo block, 20sp title, tighter padding. The save button stays 48dp tall to keep the
  touch target. This deliberately departs from spec A3 (232dp / 196dp image) at the owner's request.
- **"See all" without the arrow chip** on every Home section header. `SectionHeader` lost its
  `chipColor`/`chipContentColor` params because Home is its only caller.
- **Recent News header has no dark backing panel** now. Upcoming Events keeps its panel: not asked for.
- **Equal card heights**: Landmarks and Events rows use a new private `EqualHeightRow`
  (`SubcomposeLayout`) in `HomeScreenContent.kt`. It measures the cards at their real height,
  then gives them all the tallest card's height. This replaces `IntrinsicSize.Max`, which
  over-reserved space because text intrinsics ignore `maxLines`. Cards pin their bottom row with a
  weight spacer. A card's body must not be `weight()`-ed, or it measures to 0 in the probe pass
  (that bug was hit and fixed in `MustSeeCard`). Event titles ellipsize after 3 lines (was 2);
  Landmarks titles after 3 lines; descriptions stay at 2.
- Verified on `emulator-5554`. `testDebugUnitTest`, `installDebug` and
  `compileKotlinIosSimulatorArm64` all pass.

## Stage 44 — Place detail redesign + context-aware audio button (2026-10-06)

Built from the "Place detail screen + audio button" spec (Parts 1 and 2). Owner decisions: all of
Part 2 this round; **no full-screen photo button** (the carousel is enough); the map stays the
native map, locked, **keeping the live-location dot**.

**Part 1, the screen** (`placedetail/`): rewritten as one Lamplight template on `night`.
- `components/DetailHero.kt`: 480dp photo pager (400dp under 700dp tall, 200dp + landmark glyph
  with no photos), top and bottom scrims, Back and Save overlay buttons, shared dots, "Photo n of
  total", 0.5× parallax (off with reduced motion), and a per-photo failure fallback.
- Title block (`CATHEDRAL · OLD TOWN`, Gloock 40, short description), then the practical rows:
  opening hours as entered; price level (€–€€€ with "compared with…") **for Eat & Drink only**,
  so the old "Free entry" for sights is gone; visit length and best time side by side, stacking at
  font scale ≥1.3; best time hidden when "any time".
- Eat & Drink: a View menu pill (browser) and a Call to book outline pill (dialer, never calls),
  with the number as entered.
- About collapses after 6 lines with Read more / Show less, and is hidden when it only repeats the
  short description. The local tip shows only when it adds something beyond the descriptions.
- Where it is: `NativeDetailMapView(interactive = false)` with a tap layer → Open in Maps at the
  exact coordinate (Android `geo:` with a Google Maps URL fallback; iOS Apple Maps https link).
- The compact top bar fades in after the hero. It's **opaque**, not the spec's 96%, because the
  title ghosted through at 96%. Light status bar icons on Android.
- Loading skeleton, plus an error state with Retry and Back. "Couldn't open Maps/browser/dialer"
  messages go to the new app-wide `SnackbarBus` (snackbar host in `MainShell`).
- The old inline audio guide section is gone. `prettyLabel()`/`formatDuration()` moved to
  `PlaceDetailFormatting.kt` (same package, still used by other screens). `InfoChip` and
  `PlacePhotoHeader` stay because other screens use them.
- New shared `presentation/shared/PagerDots.kt` (28×8 `lamp` pill and 8dp dots, windowed to 5),
  now also used by Home's Nearby carousel.

**Part 2, audio:**
- `core/audio/AudioController`: an app-wide owner on top of the existing `AudioGuidePlayer`
  (Android foreground service + media session, iOS AVPlayer + now-playing info). Every intent
  takes a token from the **shared** `PlaybackRequestGuard`, so the slow URL resolve is dropped if
  the user left, changed tracks, or `MainShell` invalidated on navigation. Track end → Ready;
  player error → Failed; an external stop (notification Stop) → Idle.
- `NavAudioHost`: screens push and pop a `NavAudioContext`. It also keeps the pulse counter (3
  full visits per session, then 1 cycle) and the scroll signal.
- `NavAudioButton` (in the floating bar): NoAudio headphones → library; Ready ▶ with pulse rings;
  Loading (dimmed ▶ + spinning arc; tap cancels); Playing (pause + equalizer bars + progress
  ring); Paused (▶ + static ring); Failed (retry + snackbar); Unavailable (headphones with a
  slash at 45%; tap explains). Long-press and an a11y custom action open the library. The
  first-time hint bubble shows once per install (`audio_hint_seen`). Reduced motion gives static
  ring and bars and no bob. The bar's pill moved behind an unclipped row so rings and hint can
  draw past it.
- Track selection: the app language, else English (flagged "English audio" in the a11y label).
  "Downloaded" comes from `OfflineAudioStore` via the new `AudioGuideRepository.isAvailableOffline`.
  New `ConnectivityMonitor` (Android `ConnectivityManager` + `ACCESS_NETWORK_STATE`; iOS
  `NWPathMonitor`).
- Platform hardening. Android service: audio focus (pause on transient loss, resume only if
  focus returns within 30s, stop on permanent loss), `ACTION_AUDIO_BECOMING_NOISY` → pause,
  completion flag, 500ms progress. iOS player: real buffering state, end-of-item, failure,
  interruption pause/resume (30s window), route-change (old device unavailable) → pause.
- Verified on `emulator-5554`: cathedral detail; hint and rings on the first visit; ▶ → playing
  (bars + progress ring) → paused → resumed; Back while playing stops audio (service gone) and
  Home shows headphones; restaurant page (price row, menu/call buttons, number); Call to book
  opens the dialer prefilled; Open in Maps opens Google Maps at the coordinate with the name
  label; airplane mode with a downloaded track → ▶ and it plays.
- Tests: `AudioControllerTest` (11) and `PlaceDetailViewModelTest` (7). The whole suite (46) passes.
- **Not verified this round**: iOS visually (compiles only; status bar icon color is a no-op on
  iOS); the lock-screen controls, call interruption, and headphone unplug paths (code in place;
  the emulator can't exercise them meaningfully); the Unavailable look (the only place with
  audio is now cached); TalkBack. Home's "Listen" still opens the separate PlaceGuide screen.
  There's no "Showing saved info" note: the repository exposes no staleness signal.

## Stage 45 — Owner tweaks: pannable detail map, longer audio pulse (2026-10-06)

- **Detail map is interactive again** (owner: "navigate the map for basic orientation, open Maps
  for directions"). The tap-to-Maps overlay is gone; Open in Maps stays for handoff. Both
  `NativeDetailMapView` actuals now frame the camera once, plus once more when the user's location
  first arrives. The 5s live-location refresh then moves only the pins, so a pan isn't snapped back
  (verified on the emulator). Trade-off: a drag that starts on the 180dp map pans the map rather
  than scrolling the page.
- **Audio pulse runs longer**: 5 cycles (~11s) on every visit. The spec's 3 cycles, dropping to 1
  after three visits per session, was easy to miss. Scrolling now dismisses only the hint
  bubble; the rings keep going until the button is tapped or the screen is left. Verified still
  pulsing ~8s after an immediate scroll.

## Stage 46 — Explore redesign (2026-10-06)

Built from the Explore spec. `screens/categories/` (Explore's screen) was rewritten as a grouped
discovery hub on `night`. Every group has a deliberately different card shape.
- New shared `presentation/shared/LamplightHeader.kt` (wordmark, language, headline slot, search,
  footer slot, wall-line decoration). Home's `LamplightTopBar` now uses it too. Explore's headline
  is "Explore" + subtitle, with no area picker.
- Components (`categories/components/`, one file each + preview): `ExploreHeroCard` (Places,
  230dp photo + scrim + lamp go-circle; raised card when there's no photo), `ExploreTileCard`
  (Eat & Drink on limestone, Activities on raised; stacked at font scale ≥1.3),
  `ExploreWideArchCard` (Worth the Drive, arch photo + night shadow; no photo → text full
  width), `ExploreStoryCard` (Legends & Localisms, lamp + big quote mark, READ or READ · LISTEN),
  `ExploreGroupedList` (Events with the real next-event date tile or a generic icon; Info Center
  with the `alert` dot only while an urgent/important notice is current; Services).
  `ExploreIcons.kt` holds the stroke glyphs. Every card is one a11y button: "title, subtitle"
  (+ next event / important notice).
- `CategoriesViewModel` now loads only the dynamic bits, each independently: hero photo, Worth the
  Drive emptiness + photo, stories emptiness + audio, next event, important notice. A failed
  fetch keeps the default, so **no entry point is ever hidden because a fetch failed**. The old
  `GetCategoriesUseCase` dependency and the "Practical" row (Essentials is its own tab) are gone.
- **Worth the Drive rule:** the card uses the list's own threshold (> 3.5 km), measured from Kotor
  Old Town (`domain/model/GeoDistance.kt`) because the card shouldn't need GPS. A zone-based
  first try hid the card even though its list had Perast in it; caught on the emulator.
- Defaults taken on the spec's [CONFIRM]s: hero = first Must See place's first photo; Services
  stays in Explore; story subtitle uses the spec's copy (new `ExploreStrings`, 5 languages). The
  row subtitles reuse the existing translated strings (Services keeps "Rent-a-car, boat rentals &
  more").
- Verified on `emulator-5554`: layout, the real Events date (OCT 5), the Info Center alert dot,
  READ (no story audio), the Perast arch; Worth the Drive / Legends open inside the Explore stack;
  Back keeps the scroll; the Explore tab returns to the root. Tests: `CategoriesViewModelTest`
  (3); the suite (49) passes.
- **Not verified / follow-ups**: iOS visually; German at max font scale; TalkBack. The hero's
  first Must See photo is the transparent cathedral line drawing, which reads oddly under the
  scrim; a fixed editorial photo (the spec's other option) would look better. The destination
  lists (Places, Worth the Drive, Legends, Events, Info Center, Services) are still the old
  light design. The Worth the Drive *list* also includes restaurants (it measures from the user's
  GPS across all categories); the card's photo comes from sights only.

## Stage 47 — Essentials redesign: Essential info + Utilities (2026-10-06)

Built from the Essentials spec. Owner decisions: **map the existing CMS categories** (no
taxonomy change); **"Do this early" = the customs/registration entry** (no priority flag); **112
added as the primary emergency button** plus a bundled offline fallback.

- Shared: `LamplightHeader` (search now optional; Essentials has none) with the "Essentials"
  title, subtitle, and the new reusable **`presentation/shared/LamplightSegmentedControl.kt`**
  (night track, chalk selected segment, announced as tabs). The selected view and the expanded
  modules persist via `SavedStateHandle`, and each view keeps its own scroll position.
- **Essential info**:
  - `DoThisEarlyCard`: the first `customs` entry's title, its *first paragraph* (verified CMS
    copy, max 3 lines, no rewritten deadlines), and "How it works", which expands that module and
    scrolls its group heading to the top of the screen.
  - `EmergencyCard`: 112 "General emergency" (1.5dp alert border, Gloock) + tiles parsed from the
    CMS `emergency` entry ("Police - 122" lines, `parseEmergencyNumbers`). `BUNDLED_EMERGENCY_NUMBERS`
    (122/123/124) shows until the CMS loads and on an offline fresh install. Every tap opens the
    dialer prefilled.
  - `TopicIndexGrid` (2 columns, 1 at font scale ≥1.3) jumps to the groups in UI order:
    Registration & tax (customs), Getting around (transport), Practical info (practical), Local
    tips (tips). A group with no renderable module is hidden, along with its tile. Purpose lines
    are static UI copy.
  - `EssentialModuleGroup` accordions: no subtitle (there's no CMS summary field), **never a
    link count**; the body collapses to 6 lines + Read more past ~8; one Map action per CMS
    location (exact coordinate, labelled with its title) and one Open action per link (titled by
    host). Several modules can be open at once.
- **Utilities**:
  - Location status line: "Distances from your current location", or "Location is off · showing
    places around Kotor Old Town" + **Turn on** (opens app settings; never re-prompts).
    Distances are hidden without a fix, and lists are then ranked from Old Town.
  - `UtilityCategoryGrid`: 4 columns (3 / 2 at larger fonts). **Only categories that have
    records** get a tile (the data has no Shop or Emergency POIs, so those tiles are absent
    rather than dead ends), plus All on map.
  - `UtilitySummaryCard` for ATMs and Pharmacies (`SUMMARY_UTILITY_CATEGORIES`): "{n} nearby"
    (the existing 1.2 km rule), a big Closest distance, a locked preview map framing the nearest
    pins + the user, the numbered nearest-3 list (a tap opens the map with that pin selected), and
    Open map (filled on the first card, outline after). `UtilityCompactRow` for the rest.
  - Distances: "86 m", 10 m steps above 100 m, "1.2 km" (`formatDistance`).
- **Utility map route**: `Route.UtilityMap(category, selectedId)`; `ALL_CATEGORIES` shows every
  category. The VM key includes the selection. The map screen itself isn't restyled in this pass,
  per spec.
- New `EssentialsStrings` (5 languages), `ExternalLauncher.openAppSettings()`, and
  `EssentialsFormatting.kt`. The old 1,066-line content file was replaced.
- Verified on `emulator-5554`: Info layout; How it works → expanded registration at the top with
  2 Map actions; Utilities status line, 6-tile grid, ATM card (Closest 56 m = row 1), Pharmacies
  (outline Open map), compact rows; tapping row #2 opened the map with Prva Banka selected; Back
  kept the Utilities scroll. Tests: `EssentialsViewModelTest` (6); the suite (55) passes.
- **Not verified / follow-ups**: iOS visually; TalkBack focus moving to the heading on a topic
  jump (only the scroll is implemented); German at max font scale; "last known location" (>2 min)
  isn't distinguished because the location provider exposes no fix age; there's no "Offline map"
  label. **CMS gaps to close later**: a per-module summary field, link titles, a priority flag,
  and 112 in the emergency entry.

## Stage 48 — Home gradient everywhere, white Home lettering (2026-10-06)

- New `theme/PageBackground.kt`: `Modifier.pageGradientBackground()` = Home's
  `HomePalette.pageGradientStops` (#C9BFD5 → #55517A → #1E1B20), pinned to the viewport (applied
  before `verticalScroll`). Used by Home, Explore, Essentials and Place detail in place of the
  flat `night`. The detail hero's bottom scrim now fades to a 45% shade rather than solid night.
- Home lettering on the gradient is white: `SectionHeader` defaults to white; section frames,
  empty/loading messages, the Nearby empty text, and the Landmarks title + "THE WHOLE BAY"
  label. Not changed: text inside cards, the yellow header (night on lamp), and the white
  area-picker sheet.
- The Upcoming Events header has no dark backing panel now (Recent News lost its panel in Stage 43).
- **Known readability issue (flagged to the owner)**: white text on the gradient's light top stop
  is ~1.8:1. Home and Explore/Essentials hide most of it behind the yellow header, but on Place
  detail, scrolled content passes over the light top (the description under the compact bar is
  hard to read). Suggested fix: darken the top stop for non-Home pages, or anchor the gradient to
  the content so the light part stays behind the hero.
- Old-design screens (lists, Saved, Search, utility map, guides) still have their own light
  backgrounds; switching them needs a text-colour pass alongside.

## Stage 49 — Language bottom sheet replaces the Change Language screen (2026-10-06)

- New `presentation/shared/LanguageSheet.kt` (`ModalBottomSheet`) on the **purple page
  gradient** (owner's call over the mockup's ink). Each language is shown by its own name, with
  its name in the UI language underneath ("Current" for the selected one). The selected accent is
  **purple, not lamp yellow** (owner's call): periwinkle `HomePalette.action` code tile/check/row
  tint and lilac `HomePalette.canvas` name. "Use my phone's language" maps the device locale to a
  supported code (else English) via the new `deviceLanguageCode()` expect/actual.
- `LanguageSheetHost` (Koin single) opens it from anywhere: Home (`HomeAction.OpenLanguagePicker`
  → `languageSheet.open()`), Explore, Essentials and Saved. `MainShell` renders it once.
  `Route.ChangeLanguage` and its destination were removed; the first-launch `LanguagePickerScreen`
  is untouched.
- New `LanguageStrings` (5 languages). Verified on `emulator-5554`: the sheet renders, and
  choosing Deutsch re-localized the app at once (left in German on that AVD; switch it back with
  the EN chip).
- **Known cosmetic issue**: on Android the sheet's dialog window draws a light system scrim behind
  the navigation buttons. Material3 `1.10.0-alpha05` exposes no switch for it; fixing it means
  replacing `ModalBottomSheet` with an in-window overlay.

## Stage 50 — Saved redesign (2026-10-06)

Built from the Saved spec. Favorites storage is unchanged (`favorites_v1`, same ids).
- `FavoritesRepository.restore(entry)` restores the entry with its original timestamp, so Undo
  puts it back in exactly the same place.
- New `SavedSnapshotRepository` (`saved_snapshots_v1` in preferences): each item's name, group
  and first photo, remembered whenever it resolves. An unpublished item then still shows its
  name, and offline items keep a name and photo.
- `FavoritesViewModel` rewritten: groups via the pure `groupKeyFor`/`groupSaved` (curated Home
  areas → far-out places "Worth the Drive" (> 3.5 km from Old Town) → their own zone → Other).
  Order: the user's current area first (the group with their nearest item, only in the bay), then
  Old Town, Dobrota, Perast, other areas A–Z, Worth the Drive, Other; newest first within a group.
  **Distances only within 30 km of Old Town** (`REGION_RADIUS_KM`); the "10283.0 km away"
  problem is gone. An item is "No longer available" only when the content list loaded and the id
  is missing; a failed/offline fetch never marks it unavailable.
- UI on the page gradient: the Lamplight header (title + subtitle, no search), "{n} saved" plus
  "Distances appear once you're in the bay." (outside the bay). Components: `SavedCard` (photo,
  type label, Gloock title, Audio · distance · one CMS duration hint, a separate remove button
  with a filled lamp bookmark), `UnavailableSavedCard` (dashed, cached name, Remove, not
  tappable), `SavedEmptyState` (lamp arch, Explore Kotor → Explore tab).
- `SnackbarBus` now carries an optional action. Undo snackbars stay 5s (chalk, radius 16) and the
  action restores the entry.
- Tests: `FavoritesViewModelTest` (6): grouping/order, no distances abroad, nearest-area-first in
  the bay, unavailable with cached name, offline ≠ unavailable, Undo restores position. Suite: 68 pass.
- **Not verified on a device**: my emulator was shut down and the only one running was
  `Kompass_Pitch_Pixel_9a`, which I left alone. Activity area grouping uses the CMS location text
  or the coordinates (activities have no zone). There's no 10s snackbar extension for screen readers.

## Stage 51 — Shared category list + save-to-top animation (2026-10-06)

Built from the category list spec. New `screens/categorylist/` serves the Explore entry points
**Places** (See & Visit), **Eat & Drink** and **Activities** via `Route.CategoryList(kind)`.
**Worth the Drive is left on `Route.WorthTheDrive` → `ExcursionsScreen`**, which was added
outside this session (12:20 today); the `WORTH_THE_DRIVE` kind exists and is tested, but nothing
routes to it. The old `CategoryItemsList`, `Activities` and `NearbyPlaces` destinations are kept
for their other entry points.
- `CategoryListViewModel`: one data path per kind. Pure `placesFor` / `splitSavedAndRest` /
  `areasOf`: "Your saved" = favorites in the current filter, **`favoritedAtEpochMs` descending
  (the same order as the Saved tab)**; the rest by proximity when the device location is real,
  else CMS `sortOrder`. Area filter options reuse Saved's `groupKeyFor`, and the filter persists
  in `SavedStateHandle`. Favorites are written immediately (optimistic); effects `Saved`
  (visible in filter?) and `Removed` (entry, for Undo → `restore`, original timestamp).
- UI: `LamplightCompactHeader` (new shared: lamp, night back circle, outline search circle →
  global Search, Gloock 44 title, Explore subtitle) on the page gradient; count + Area filter pill
  (menu; an active filter shows "Area: X ✕"). `CategoryListCard` (96dp photo, `TYPE · AREA`
  label, Gloock 20, walk time only from real location (~80 m/min, distance past 60 min), Audio
  marker, bookmark off/on states).
- **Save animation**: the bookmark pops (spring 0.45 / 600 to 1.28×) with an expanding lamp ring
  and a haptic tick; the card dims (0.97, 25%, 240ms); at 300ms the favorite is written and the
  item moves to "Your saved" (`animateItem` with group-specific keys, so the old slot collapses
  and the card fades/springs in); a scroll to the saved header only if it's above the viewport
  (max once per 600ms); the landed card glows (lamp border + lamp shadow, fading over 1.5s, only
  the latest); a "Saved · {name}" toast for 2.2s. A second tap on the same bookmark is ignored
  during the 300ms. Unsave from the saved group: immediate, no scroll, "Removed from saved" + Undo
  (5s). Reduced motion: instant reorder/scroll, a static outline for 1.5s.
- `SnackbarMessage.durationMs` added. New `CategoryListStrings` (5 languages).
- Tests: `CategoryListViewModelTest` (6). Suite: 74 pass.
- **Not verified on a device** (only `Kompass_Pitch_Pixel_9a` was running). Not built: the
  search scoped to the category (it opens global search), the "write failed → reverse" path (the
  favorites store is local and can't fail today), and focus staying on the moved bookmark for
  screen readers.

## Stage 52 — Search (Lamplight)

Spec: "KOMPASS · Search (Lamplight)". Destination-only search over cached content; no server call per keystroke.

- **Local index**: `domain/model/SearchIndex.kt` — `SearchKind`, `SearchIndexItem`, `SearchHit`, `searchIndex()`, `foldForSearch()` (lowercase, Latin diacritics map incl. đ→d, ß→ss, collapsed whitespace, origin-index map so highlights land on the original characters). Scores: 0 name prefix, 1 word prefix (any word, not just the first occurrence), 2 substring, 3 alias (English name when the app language differs), 4 area. Ties: type order, then name. Min 2 chars.
- `domain/usecase/BuildSearchIndexUseCase.kt` — places, activities, stories (local finds), Essentials, upcoming events, loaded in parallel; fails only if every source fails (→ "Search isn't ready yet" + Retry, never "no results").
- Recent searches: `RecentSearchesRepository` / `data/repository/RecentSearchesStore.kt` (AppPreferences JSON, 5 max, most recent first, case-insensitive de-dupe). Added when a result is opened.
- `SearchViewModel` rewritten: 150ms debounce, chip resets to All on a new query, "Show more" after 50, query + chip in `SavedStateHandle`, index rebuilt on language change. Takes the index builder as a lambda (testable).
- UI: `SearchScreen` (orchestrator, autofocus once, effects → navigation), `SearchScreenContent` (Recent + Try, chips with counts for types with results only, polite status line after 600ms, result rows, no-results, not-ready; drag closes the keyboard), `components/` `SearchHeader`, `SearchTypeChip`, `SearchResultRow` (photo, else type-icon tile; `lamp` 700 highlight), `SearchIcons`.
- Strings: `core/util/SearchUiStrings.kt` (`strings.search`, 5 languages; editorial Try suggestions per language).
- Navigation: results open inside the current tab's stack. New `Route.EssentialModule(id)` → `EssentialsScreen(focusModuleId)` expands the module and scrolls its group heading into view. Stories → `LocalFindDetail`, events → `EventDetail`, Browse Explore → Explore tab.
- `MainShell`: floating bar hidden while the IME inset is > 0 (`WindowInsets.isImeVisible` is not available on iOS common).
- Old server `SearchRepository` remains registered but unused by the screen.
- Tests: `SearchViewModelTest` (folding/highlight mapping, ranking, debounce, chip reset, recent + navigation effect, not-ready). 83 tests passing.

Known gaps / follow-ups:
- Essentials focus scrolls to the module's group heading, not the module row itself (fine for short groups).
- The index is built when Search opens (from cached repositories), not proactively after content sync.
- No aliases field in the CMS yet; only the English-name fallback is used as an alias.
- Not yet verified on a device: autofocus/keyboard, bar hiding, iOS keyboard behavior.

## Stage 53 — Audio library ("Listen")

Spec: "KOMPASS · Audio library (Listen)". Reuses the shared `AudioController` (no second player).

- **Playback is now stream-first app-wide.** Previously `getSignedAudioUrl` downloaded the whole file before playing and silently cached it, so "Downloaded" could not be honest. `AudioController` now resolves via `AudioGuideRepository.streamUrl` (local file if downloaded, else signed/public URL, never downloads). Downloads are explicit: `download(path, onProgress)` (Ktor `onDownload`, size checked against Content-Length) and `removeDownload(path)`. `getSignedAudioUrl` is unchanged for the legacy guide screens.
- `OfflineAudioStore.persist` writes `*.part` then renames (Android + iOS), so a half-written file never counts; added `remove`.
- `AudioController`: `toggle(track, owner)` (pause / resume / cancel-load / replace), `seekTo`, `skipBy` (clamped). `AudioState.trackOrNull`.
- `screens/listen/`: `ListenViewModel` (places with audio in app language, English fallback flagged; ordered current area first — Home's area, else the device's nearest — then Old Town, Dobrota, Perast, others, Worth the Drive; editorial order inside; per-track download state with cancel; Downloaded view default when opened offline), `ListenScreen` (owner key per instance; leaving stops via `stopIfOwner`; card lingers 1s after a track ends; no auto-play; snackbars for unavailable / failed / download failed; remove confirm), `ListenScreenContent`, components `ListenTrackRow`, `NowPlayingCard` (seekable bar = slider with ±15s actions), `PlayDisc`, `DownloadButton`.
- `Route.MyGuides` now renders `ListenScreen` (old `MyGuidesScreen`/VM left in place, unused by navigation).
- Shell: on the library no tab is selected, the center button shows a `lamp` "you're here" ring and its tap is a no-op; tapping any tab pops the library first; opening the library is `launchSingleTop`. `NavAudioContext.isLibrary`.
- New color token `surfaceActive` (#2A2516) for the active row. Strings `core/util/ListenStrings.kt` (5 languages).
- Tests: `ListenViewModelTest` (6), `AudioControllerTest` +3 (toggle, cancel while loading, skip clamping). 92 passing.

Known gaps:
- No duration in the CMS: rows show a duration only after the track has been played this session.
- Only places are listed (activities with audio are not; rows open the place detail).
- Slider value reads "1:05 of 4:12", not spelled-out minutes/seconds.
- Download progress is unknown (spinner) when the server sends no Content-Length.
- Not verified on a device: lock-screen controls, background playback from the library, download on iOS.

## Stage 54 — Activity detail (Lamplight)

The activity detail now uses the place detail template (no separate spec; "match the design").

- Shared detail pieces extracted from `PlaceDetailScreenContent` into `placedetail/components/` (public, with previews): `DetailPillButton`, `DetailPracticalRows` (+ `DetailPracticalRow`), `DetailTextSection` (Read more / Show less, optional Gloock heading), `DetailWhereItIs`, `DetailCompactBar`, `DetailLoadState` (skeleton / error + Retry). Place detail uses them; no visual change intended there.
- `experiencedetail/` rewritten to the mandatory structure: `ExperienceDetailScreen` (orchestrator: favorites, center-button audio context with owner key + stop on leave, live location, maps / website hand-off with snackbars), `ExperienceDetailScreenContent`, `ExperienceDetailViewModel` (activity + `audioTrack`, same English-fallback rule as places; premium-access check removed).
- Sections, each only when its data exists: hero carousel + back/save, `{CATEGORY} · {LOCATION}` label, Gloock title, short description, Duration + Best time rows, **Visit website** (filled pill), About (long description, de-duplicated), **How to get there** (headed, collapsible), Where it is (pannable map + Open in Maps), compact bar after the hero.
- Removed from this screen: the inline audio player (audio now plays from the floating bar's center button, as on places) and the old light card layout. `ExperienceGuideScreen` / `Route.ExperienceGuide` are untouched.
- Strings: `DetailStrings` + `activityLoadError`, `duration`, `howToGetThere`, `visitWebsite`, `activity` (5 languages).
- Tests: `ExperienceDetailViewModelTest` (3). 95 passing.

## Stage 55 — Device pass fixes + Legends & Localisms (list + story detail)

Device pass (Kompass_Pitch_Pixel_9a, emulator location = California):
- Listen library verified: play in place, Now playing card, equalizer row, "you're here" ring, no tab selected; tapping Explore leaves the library and stops audio.
- Fixed: category list showed "10283 km" when the device is outside the bay → distances only within 30 km of Old Town (same rule as Saved).
- Fixed: detail map zoomed out to a continent when the user is far away → `DetailWhereItIs` includes the user only within 30 km of the destination (places + activities).
- Fixed: activity label used the CMS location sentence → only short (≤24 chars, no comma) locations in the label.
- Still open: activity cards whose CMS location is a sentence show it as their area label in the category list; one activity card with a photo URL rendered an empty tile.

Legends & Localisms (spec "KOMPASS · Legends & Localisms"):
- CMS `local_finds` has **no** kind (legend/localism), short description, area, featured flag, `locations[]` or pull-quote field. Built against what exists: every story is a legend (`LocalFind.storyKind`, one place to change) so the All/Legends/Localisms control hides itself (spec rule); excerpt = first sentence of the body; area = the location text's first short segment; one Map row when the story has coordinates; a paragraph starting with "> " renders as a pull quote.
- List (`LocalFindsScreen/Content/ViewModel/State/Intent`): lamp top bar with faint “, Gloock 42 title; Feature (first) / Compact (legend with photo) / Quote (no photo or localism) cards; meta = read time (words ÷ 200, ≥1, hidden without body) + "Listen" when audio exists (no duration in CMS); skeleton, empty-per-filter and error + Retry states.
- Detail (`LocalFindDetailScreen/Content/ViewModel`): 380dp hero with scrims (or compact lamp header), title block overlapping the hero by 24dp, meta (read time · short place name), Listen pill mirroring the center button (`resolveNavAudioMode` + new `AudioController.onNavAudioTap`), 24dp reading column 18/30.6 with a measured drop cap (off at font scale ≥ 1.3; paragraph stays one accessible text), pull quotes, Where it happened (cleaned location text + Map row), Next story (replaces the current story: pop + push, so Back returns to the list), compact bar + 2dp reading-progress line.
- Paragraphs: blank lines break; a single line break only after a sentence end (the CMS hard-wraps mid-sentence).
- `Route.LocalFindDetail(id, filter)` carries the list filter for Next story.
- Components: `StoryFeatureCard`, `StoryCompactCard`, `StoryQuoteCard`, `StoryMetaRow`, `DropCapParagraph`, `StoryListenPill`, `StoryLocationRow`, `NextStoryCard`. Strings `core/util/StoriesStrings.kt` (5 languages).
- Center-button rings stay at 5 cycles (owner's earlier request) rather than the spec's 3.
- Tests: `StoriesTest` (8). 102 passing.
- Known: status-bar icons stay light on the list's yellow header after returning from a story (the exiting screen's effect wins); CMS should add `kind`, `short_description`, `zone`, `locations`.

## Local Verification Artifacts

- /tmp/kompass-redesign-build.log
- /tmp/kompass-redesign-tests.log
- /tmp/kompass-redesign-xcode.log
- /tmp/kompass-redesign-ios/Build/Products/Debug-iphonesimulator/Kompass.app
- /tmp/kompass-home-redesign.png (actually captured Essentials before switching to Home)
- iOS simulator UUID: DCBF5DFB-5941-4239-8B82-2A0387BF1CE6
- iOS bundle ID: llc.bokadev.kompass.Kompass
- Android bundle ID: llc.bokadev.kompass

## Pilot Refinement — Decisions recorded (2026-10-07)

Owner answers to the plan's §5 product review (do not reopen without a concrete new conflict):
- **Saving while browsing:** save in place. Persist on tap, row keeps its position, viewport doesn't move,
  restrained "Saved · {name}" confirmation, Undo on remove. "Your saved" group removed from category lists.
- **Central audio button:** stable, labeled Listen library access; explicit play controls on content (Step 10).
- **Destination overviews:** editorial introduction, excluded from nearby suggestions (Step 7). Note: CMS
  already has `sub_category = "destination_overview"` (seen on Kotor, Perast) — reuse it, no new field.
- **Arrival reminder:** locally dismissible/collapsible; dismissing never implies "registered" (Step 8).
- **Saving stories:** deferred.

## Pilot Refinement — Batch A, Steps 1–5 (2026-10-07)

### Step 1 baseline
- Before changes: `assembleDebug`, `testDebugUnitTest` (102/102), `compileKotlinIosSimulatorArm64` all passed.
- Backend reachable again from Android emulator (live place JSON in logcat); the earlier iOS DNS blocker
  was not re-tested on iOS.
- Reachability (callers in `MainShell`, `HomeViewModel`, `KompassNavHost`):
  - Reachable: CategoryList (Explore Places/Eat & Drink/Activities), WorthTheDrive→ExcursionsScreen,
    Events (Home + Explore), Services, InfoCenter(+Detail), LocalFinds(+Detail), Search, EssentialModule,
    UtilityMap, MyGuides→ListenScreen, Favorites (Saved tab), PlaceDetail, ExperienceDetail, EventDetail.
  - **Legacy but reachable:** `PlaceGuide` (Home `HomeAction` → `Route.PlaceGuide(autoplay)`),
    `NearbyPlaces` (Home "See all"), `MustSeePlaces` (Home "See all"). → Step 6 targets.
  - **Registered, no caller (unreferenced):** `ExperienceGuide`, `ItineraryDetail`, `CategoryItemsList`,
    `Activities` (ExperiencesScreen), `PaymentCheckout`, `PremiumBundles` (no composable). Keep until Step 6
    confirms no restored/stored routes depend on them.
- Screenshots: scratchpad only (not in repo).

### Step 2 utility map identity
- `UtilityMapPin` now has `id`; `NativeUtilityDetailMapView(focusedPinId, onPinSelected(id))`. Android tags
  markers with the id; **iOS previously had no selection callback at all** — now an `MKMapViewDelegate`
  reports the annotation's id, annotations are diffed (not rebuilt every update), and the focused one is selected.
- Shared `UtilityMapCameraTracker`: camera frames only when the pin set changes, pans only when the selection
  changes; location updates never move it; a far-away user is left out of the frame.
- VM: the selected/requested point is always kept in the list even if it leaves the nearest set; a
  selection that vanishes after reload is cleared and explained ("no longer listed"), never swapped.
  Retry keeps the shown points; one load at a time. No raw exception text.
- Navigate uses `ExternalLauncher.openMaps` with the "No maps app found" snackbar (was unguarded `openUri`).
- All screen strings localized (5 languages). Rows are `selectable`; Navigate has 44dp+ height and a
  "Navigate to {name}" description.
- **Verified on Android emulator (Medium_Phone):** real data has three ATMs named "NLB Banka"; tapping a
  non-nearest one selected it (100 m, its own address, red marker moved), and the selection held across
  two location refreshes. **Not verified:** iOS runtime (compiles only), external Maps launch, physical device gestures.

### Step 3 distance and event time
- Removed the 80 m/min walking-time estimate (category list). Removed "walk" from CMS visit durations
  (`PlaceListItem`, `PlaceGuideScreen`). Distances are straight-line, one formatter (`formatDistance`:
  "86 m", "130 m", "1.2 km") on Home, category list, utility map, Saved, Essentials.
- New `domain/model/EventSchedule.kt`: `Destination.timeZone = Europe/Podgorica` (single config point),
  `EventWindow`, `isCurrentOrUpcoming`, `EventDateFilter.period`, overlap matching. Documented rules:
  invalid start → excluded; invalid/backwards end → treated as missing; missing end → listed until the end
  of its Kotor start day (visibility only; no end time is displayed). Weeks start Monday.
- Repository, Home preview, Search index and Categories all use it via `getEvents`/`getUpcomingEvents`.
  Event list/detail, Home dates and notice dates are formatted in Kotor time.
- Tests: `EventScheduleTest` (8) incl. multi-day overlap, device-vs-destination day, DST (25 Oct 2026 = 25 h).

### Step 4 saving
- `FavoritesRepository.save/remove` (idempotent; storage written before the flow publishes).
  `toggleFavorite` kept as an interface default for legacy callers. Category list, Saved Remove, place/
  activity detail and Home now send the state the visitor saw (no blind toggle).
- Category list: one list in stable order; no delay, pop, ring, dim, glow or scroll. A snapshot (title,
  photo, area) is stored at save time (category list + detail screens).
- Saved: `SavedItemStatus` (RESOLVED / LOADING / NOT_REFRESHED / UNAVAILABLE); `isLoading` stays true until
  the load finishes (when anything is saved); "Couldn't refresh · Retry" notice; one load at a time.
- Storage key `favorites_v1` and entry shape unchanged.
- **Verified on Android:** existing favorite survived the upgrade install; Save then immediate Back
  persisted; saving two items kept their positions; test saves removed afterwards (favorites restored to the
  original single activity).

### Step 5 recovery states
- New shared `StateMessage` component and `StateStrings` (5 languages).
- Events: filter failure no longer blocks events; filter change clears and loads inline (no full-page
  spinner); older requests are cancelled; Reset filters on filtered empty; Retry keeps content with
  "Couldn't refresh". "When"/"Type" localized.
- Local updates (InfoCenter): previously blank on error/empty — now error + Retry, empty, refresh-failed.
- Services: localized error/empty, refresh-failed keeps content. Category list: stale-load cancel, no raw error.
- Tests: `RecoveryStatesTest` (7), `UtilityMapViewModelTest` (8), updated `CategoryListViewModelTest`,
  `FavoritesViewModelTest` (+2). **127/127 pass**; Android assemble and iOS sim compile pass.

### Known limitations / not done in Batch A
- Repositories still return cached data as plain success, so screens can't say "showing saved data" vs
  "fresh" (Step 5.6). Needs an explicit result/status contract (Excursions' `ExcursionResult.isCached` is a model).
- Event detail still says "Until HH:MM" in English and shows only times for multi-day events (Step 9).
- Not runtime-tested: Events/Notices/Services states, iOS anything, offline mode, TalkBack/VoiceOver.
- Utility map layout (300dp map in a scrolling list) unchanged — Step 9.

## Pilot Refinement — Batch B, Steps 6–9 (2026-10-07)

### Step 6 consolidation
- `Route.CategoryList(kind, area?)`; new `CategoryListKind.MUST_SEE`. Kind passed via Koin params so legacy
  routes (which carry no "kind") render the right list. `area` seeds the existing area filter (SavedStateHandle).
- Home: Nearby "See all" → shared list with the manual area (or none for device location) — now offered for
  manual areas too; Must See "See all" → MUST_SEE list; "Listen" → `PlaceDetail(id, autoplay = true)`
  (plays once when the track loads; survives rotation without replaying).
- Compatibility redirects (restored back stacks): PlaceGuide → PlaceDetail, ExperienceGuide → ExperienceDetail,
  NearbyPlaces/MustSeePlaces/CategoryItemsList/Activities → shared list. ItineraryDetail and PaymentCheckout pop
  themselves immediately. DEEP stays gated (`PilotFeatures.DEEP_ENABLED = false`; no route sets `deep = true`).
- **Now unreferenced UI, not deleted (needs owner OK):** `PlaceGuideScreen`, `ExperienceGuideScreen`,
  `NearbyPlacesScreen*`, `MustSeePlacesScreen*`, `CategoryItemsListScreen*`, `ExperiencesScreen*`,
  `ItineraryDetailScreen`, `PaymentCheckoutScreen`, `MyGuidesScreen`, `events/EventItem.kt`, `home/components/EventCard`.

### Step 7 Home
- Headline "Near you" (no "Around") when the device location is used; "Device location" stays a picker option.
  Area accessibility label localized.
- Destination overviews: `Place.isDestinationOverview` (CMS `sub_category = destination_overview`, no title
  matching) excluded from nearby/fallback suggestions; new `GetDestinationOverviewUseCase` shows "About {area}"
  as an editorial link above the cards (manual area by zone; device location: nearest overview ≤ 3.5 km;
  none in CMS → nothing). Verified: Home shows "About Kotor", first card Pima Palace (50 m).
- Photo-free Nearby card is compact (text + buttons) instead of a 410dp empty panel.
- Repeat-use headline `areaTitle` 52→40sp (decorative uses override their own size).
- Successfully loaded but empty Local updates/Events sections are hidden; loading/failed still shown.
- **Not done:** measured contrast on the gradient (7.7) and bottom-bar reduction (7.9). Grey body text on the
  light top of the gradient (e.g. detail intro) looks low-contrast — needs measurement.

### Step 8 details and Essentials
- Place/activity hero: 40% of screen height, clamped 260–420dp (was 400/480 fixed). No-photo/failed-photo
  handling unchanged (already compact/placeholder). Content order already intro → facts → long text.
- Essentials: order is reminder → "What do you need?" → emergency; emergency numbers in one row (112 first,
  outlined). Arrival reminder has "Hide reminder" (local pref `essentials_arrival_reminder_hidden`; hides the card
  only; the registration module stays in its group). Search-opened module shows a visible Back.
- **Proposals only (no schema change made):** activity preparation fields (effort, advance booking, meeting
  point, access), map-point role, and operational source/owner/review-date metadata.

### Step 9 secondary screens (Lamplight)
- Shared: `LamplightChip`, `StateMessage` on Lamplight colors. List screens use the solid night surface under
  the lamp header (the light gradient top fails AA for light body text).
- Events: day groups (Today / Tomorrow / "8 Oct", Kotor days), running multi-day events under Today as
  "Ongoing", localized filters, Reset filters. Event detail rebuilt on shared detail components: when (Kotor
  time; multi-day "Until 8 Oct"), venue, price, Tickets button (handled launcher), description, map.
- "Info Center"/"Recent News" → "Local updates" in all 5 languages. List reuses Home's `NoticeItem`
  (level · validity · location); detail: level, title, where, when, body, "Source and details".
- Services: compact rows (area · website host, no "Website available"); detail with area/website/map.
- Utility map: full-screen map, overlay back/title, Recenter, bottom sheet (selected point + Navigate, list toggle).
  Android frames after layout and pads the camera by the sheet/top-bar size. **iOS ignores the padding**
  (needs `setVisibleMapRect(edgePadding:)`).
- First launch reuses the Lamplight language chooser (no close button). Onboarding is one localized screen
  ("Kotor, from the people who live here", three truthful points, Start). Stored keys and the onboarding context
  value are preserved; "Entry framing"/"Orientation" and the non-interactive "selected" cards are gone.
- Explore "next event {date}" now uses the next event that hasn't started (an ongoing festival showed "5 Oct").

### Verification
- 130/130 unit tests; Android `assembleDebug`; iOS `compileKotlinIosSimulatorArm64`.
- Android emulator (Medium_Phone): Home, See all, Events, Local updates, Services, Essentials, utility map
  (framing, list selection of a duplicate-named ATM), About Kotor detail. **Not tested:** iOS runtime, onboarding
  and first-launch language screen (would need a data reset — not done on this emulator), TalkBack, large text.

### CMS content observations (not edited)
- Event "New event" (test record, venue "New event") is live. Event type filter label "Theather".
- Kotor overview has no photo and `estimated_duration = 20` ("Visit takes about 20 min" for a whole town).
- Notice "Water outage": short description repeats the title (UI now hides the duplicate).

## Architecture migration — whole app on the Amplitudo-style MVI (2026-10-07)

Owner asked for every screen to follow the KMP-CMP-Amplitudo-style skill. Done app-wide:
- **One base:** `core/presentation/mvi` (`BaseViewModel<State, Action, Event>`, `onAction`, `onFirstStart`,
  `BaseViewState`, `BaseAction`, `BaseEvent`, `Navigator`, `BaseContentView`, new `UiText`).
  The old `core/presentation/base` package is deleted, as are `KompassSharedTopBar` and the old `FilterChip`.
- **Navigation:** every ViewModel navigates through the injected `Navigator` (`navigateTo`, `navigateUp`, new
  `switchTab`). No screen or nav-host destination takes navigation callbacks. `NavigatorBridge` in MainShell
  applies commands to the selected tab's controller (and handles `SwitchTab`); first launch has its own
  bridge on the root controller while LanguagePicker is shown. Hidden routes (itinerary, payment) pop via Navigator.
- **Per screen:** `State` (implements `BaseViewState`, `error: UiText?`), `Action`, `Event` (UI-only; `Nothing`
  when none), `ViewModel`, `Screen` (orchestrator), `ScreenContent` (`state` + `onAction`).
  External hand-offs are `Event`s carrying `ExternalLaunch` (Maps/Url/Dial/AppSettings), performed by the
  orchestrator with `ExternalLauncher.perform` (snackbar on failure). Audio owner keys, NavAudio push/pop and the
  Listen/Stories playback taps stay in orchestrators (screen-lifetime), documented in each file.
- Detail screens (place, activity, event, local update, service) use the mvi `BaseContentView` with the shared
  `DetailLoadState` for loading/error. Lists with independent parts (Events filters, Essentials, Saved) keep their
  own inline states, as the skill allows. `refreshFailed` = content kept after a failed refresh; `error` = nothing to show.
- Onboarding now has `OnboardingViewModel` (same pref keys); LanguagePicker navigates via Navigator.
- Saves on place/activity detail, Worth the Drive and related activities moved into ViewModels (explicit target state,
  snapshot on save).
- **Deleted (owner approved):** PlaceGuide/ExperienceGuide screens+VMs, nearby/, mustsee/, category_items_list/,
  experiences/, myguides/, itinerarydetail/, payment/, premium/ screens, EventItem/EventCard, their DI entries and
  tests (PlaceGuideViewModelTest, MyGuidesViewModelTest). Their `Route`s remain as redirects for restored back stacks.
  Data-layer premium/payment classes remain (DEEP gate still references `DeepPurchaseRepository`).
- CLAUDE.md "MVI Pattern" section rewritten to describe this structure.
- Tests: shared `testutil/TestDoubles.kt` (`RecordingNavigator`, `NoOpAnalytics`); new navigation tests for Events,
  category list, Search (incl. Explore tab switch), place detail save/back. **126/126 pass** (fresh run);
  Android assemble and iOS sim compile pass.
- **Emulator (Medium_Phone) smoke test:** Explore → Places → detail → Back ×2; Events; Local updates → detail → back;
  Services → detail → back; Essentials → Utilities → map → back; Search "registration" → exact Essentials module →
  back to results; Home See all → list → back. No crashes in logcat. Not run: iOS, onboarding/first launch
  (needs data reset), Saved empty-state "Explore Kotor" tab switch on device (unit-tested only).

## Pilot Refinement — Batch C, Step 10 part 1: Search (2026-10-07)

- **Category/subtype matching:** index items carry localized `keywords` from typed `SearchTag`s (place category,
  CMS subtype / activity category, Essentials category, utility category). Words come from one reviewed table,
  `core/util/SearchVocabularyTable.kt` (`SearchVocabulary` interface in domain): existing UI labels from AppStrings
  + a few plain synonyms per language; English words always included. Unknown CMS subtypes match their own value;
  `destination_overview` and `worth_the_drive` are deliberately not search words. Tags are not used (free-form).
- **Related words** (directional, whole query): airport → taxi/bus/transfer, payment → cash/card/ATM/exchange,
  toilet, luggage, doctor/hospital, police — same table, 5 languages.
- **Ranking (deterministic):** name prefix 0 > word prefix 1 > substring 2 > alias 3 > category word 4 >
  related word 5 > area 6; ties by type, then name.
- **New types:** Map points (utilities), Services, Local updates. A utility *category* is one result
  ("Cash & ATMs · 53 on the map" → `UtilityMap(category)`); single points match only by own name/address and open
  `UtilityMap(category, id)`. Services → `ServiceDetail`, notices → `InfoCenterDetail` (current notices only).
- **Partial availability:** `SearchIndex.failedSources`; screen shows "Some content couldn't load, so results may
  be incomplete · Retry" above results or above "Nothing for …". Not ready (nothing at all) stays separate.
  A failed retry keeps the index already built.
- **Language change:** Search rebuilds (was already). Also fixed stale localized state after a language switch in
  Essentials (groups/priority/emergency — the language button is on that screen), place/activity detail audio
  track, stories list/detail, Listen.
- Tests: `SearchCoverageTest` (8, real builder over fake repos + real vocabulary), `SearchViewModelTest` +3,
  Essentials +1. **138/138 pass.**
- **Emulator (Medium_Phone, live CMS):** "restaurant" → 7 eat & drink places (none had the word except Galion);
  "airport" → 4 (taxi/bus); "payment" → Cash and car paying, Money Exchange, Cash & ATMs (53) → ATM map;
  "prva" → Prva Banka → map with that exact point selected (73 m). Fixed a dangling "Eat & Drink ·" (empty zone).
- **Not done/tested:** iOS; other languages on device (unit-tested fr); the grey status line ("3 results…") is
  low-contrast on the gradient (pre-existing, Step 7.7 contrast item).

## Pilot Refinement — Batch C, Step 10 part 2: Audio (2026-10-07)

- **One free-audio catalog:** `GetAudioCatalogUseCase` → `AudioCatalog` of places, activities and stories with a
  regular `audio_file` (DEEP audio excluded; tier fields are not read in the pilot). Sources load independently;
  a failed one shows "may be incomplete" in Listen. Listen rows: place subtype ("Cathedral") or Place/Activity/Story;
  a row / Now playing opens the item's own detail (`PlaceDetail` / `ExperienceDetail` / `LocalFindDetail`).
- **Identity:** `AudioTrack(source, sourceId, …)`, `id = "{source}:{id}:{lang}"`. One language rule
  (`chooseAudio`): app language, else English flagged "English audio", else no track (never claims a language).
- **Downloads keyed by full storage path** (`audioCacheKey`, `v2_…`), so equal file names in different folders /
  languages no longer share a file. Files saved under the old leaf-name key are still found and removed
  (verified: Saint Tryphon downloaded in an earlier session still shows Downloaded and plays offline).
- **Center button (owner decision):** always "Listen" (labelled, 5 languages via `listen.title`), opens the library;
  never plays/pauses; no long-press, pulse rings or hint bubble. Shows equalizer + progress ring while anything
  plays; "you're here" ring in the library. Bar items bottom-aligned so labels share a baseline; nothing draws
  outside the bar (the shell clips above it).
- **Explicit controls:** shared `ListenPill` (was StoryListenPill) + `DetailListenControl` "Listen to the audio
  guide (· m:ss) (· English audio)" directly under the title on place and activity detail; stories keep theirs.
  Playback failure snackbar moved to an app-wide watcher in the shell (silent in the library, which has its own).
- Unchanged rules: leaving a screen or switching tabs stops its playback; backgrounding doesn't.
- Tests: Listen +5 (activity/story in library + routes, identity, no false language, French-only in French,
  partial failure), `AudioCacheKeyTest` (4). **147/147 pass**; Android assemble + iOS sim compile pass.
- **Emulator (Medium_Phone, live CMS):** Tryphon detail → Listen plays (pause state, ring on center button);
  tab switch stopped it; library play → Now playing card; backgrounded app kept playing (0:20 on return);
  airplane mode → downloaded track plays.
- **Not tested / limits:** no activity or story in the CMS has audio yet (unit-tested only); iOS runtime;
  a not-downloaded track offline on device; opening the library *stops* a detail's playback (screen-ownership
  rule) — if the owner wants the library to adopt it, that's a deliberate rule change.

## Pilot Refinement — Batch C, Step 10 part 3: Geographic grouping (2026-10-30)

- **One area rule:** `areaKeyFor(area)` (favorites/FavoritesViewModel.kt) replaces `groupKeyFor(zone, point)`:
  curated Home area (Old Town/Dobrota/Perast via `HomeArea.matchesZone`), else the item's own area normalized
  (lower-case, non-letters → `_`, so "Kotor Bay" = "kotor_bay"), else Other. **No distance branch.** Used by
  Saved, Listen, the shared category list, and the place/activity save snapshots.
- **Worth the Drive is editorial only:** the `> 3.5 km from Old Town` bucket is gone from Saved and Listen, and the
  distance-based `CategoryListKind.WORTH_THE_DRIVE` list plus `WORTH_THE_DRIVE_KM` are deleted (no caller: Explore
  already opens `Route.WorthTheDrive` → `ExcursionsScreen`, backed by `worth_the_drive_items`, which links places
  *and* standalone activities). A restored `Route.CategoryList("WORTH_THE_DRIVE")` opens the editorial collection.
  Related activities (`place_activities`) are untouched.
- **Upgrade:** snapshots stored with the old `worth_the_drive` group key show under Other; stored keys pass through
  `areaKeyFor` again (idempotent). Favorite IDs/timestamps untouched.
- Tests: Favorites +1 (`farPlacesGroupByTheirOwnAreaNeverAsWorthTheDrive`), CategoryList +1, two updated.
  Also fixed a time-bomb fixture (`CategoriesViewModelTest` used 2026-10-12 as "upcoming"). **149/149 pass.**

## Pilot Refinement — Step 7 leftovers: gradient contrast (7.7/7.8) and bottom bar (7.9) (2026-10-30)

Measured on Medium_Phone (screenshot pixels, WCAG ratio):
- Language screen on the old gradient: white title 2.4:1, subtitle 2.3:1. The light top stop `#C9BFD5` cannot carry
  white text, and content scrolls across the whole pinned gradient.
- Home section failure state: error red on the gradient 1.5–3.0:1 (computed; looked near-black on device);
  Retry white-on-periwinkle 3.9:1, ~38dp tall.

Changes:
- `HomePalette.gradientTop = #6A6489` (canvas lavender, deepened) replaces `canvas` as the top stop. Worst case over
  the gradient: white 5.5:1, chalk 5.0:1, new `HomePalette.onGradientSecondary` (white 85%) 4.5:1.
  `canvas` is kept as the light accent text on dark rows.
- Muted text sitting directly on the gradient (Lamplight textSecondary/Tertiary 2.4–2.8:1, white 70–80%) →
  `onGradientSecondary`: place/activity intro + phone, Search incomplete/no-results/not-ready, Saved header lines,
  list count/empty/error, Listen and Stories empty text, story meta + location, Essentials offline/status lines,
  language sheet subtitle, Home nearby empty text. Text inside cards unchanged.
- Home section failure: white message; Retry is the night-on-lamp pill used elsewhere, ≥ 48dp.
  **Re-measured on device: message 7.4–12.4:1, Retry 11.3:1.**
- Home status line: "· use my location" was squeezed into a one-word column ("locati / on") — now a `FlowRow`
  (link moves whole to the next line) with a 44dp touch target. Not re-seen on device (location was granted).
- Bottom bar (7.9): shadow 10dp → 4dp, bottom margin 10 → 6dp, Listen button 50 → 46dp (ring 64 → 58dp, ring radius
  now derived from the button), tab icon circle 38 → 34dp, item min height 56 → 48dp. Labels/touch targets kept.
  Seen on device: lighter, labels on one baseline.

## Pilot Refinement — Step 11 release check status (2026-10-30)

Done (evidence):
- Unit tests 149/149; `assembleDebug`; `compileKotlinIosSimulatorArm64` — all pass.
- Gates: `PilotFeatures.DEEP_ENABLED = false`; `PaymentCheckout`/`ItineraryDetail` close themselves and have no
  callers. `Route.PremiumBundles` had **no destination** (a restored stack would crash) — now closes the same way.
- `USE_LOCAL_SUPABASE=false`. First launch (language → onboarding → location prompt → Home) ran on Android.

**Blockers / not done — release is NOT verified:**
- **Production Supabase does not resolve** (`msfyhxaengmbwcgsgysf.supabase.co` → NXDOMAIN from the Mac and the
  emulator, 2026-10-30). The project is probably paused or deleted. No live-content check was possible this
  round; the app shows failure states everywhere. Owner: restore it in the Supabase dashboard.
- **Launcher icon is still the Android template robot** (since the initial commit). Needs the brand asset.
- Emulator `emulator-5554` was shared with another project's app (repwars) during this session; device testing
  stopped early to avoid interfering.
- Not run: the Step 11 functional matrix on live content, iOS runtime, physical-device map/audio/dialer/browser,
  upgrade-over-old-install check, TalkBack/large text, other languages on device, owner approval of journeys.
- Open owner decision: opening the Listen library stops a detail screen's playback (screen-ownership rule).
