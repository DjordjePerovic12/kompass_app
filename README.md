# KOmpass

A pocket guide to Kotor and the Bay, curated by locals. Places, events, local updates, stories and audio guides, City Essentials and a utility map, in English, German, French, Spanish and Turkish.

Kotlin Multiplatform with a shared Compose Multiplatform UI for Android and iOS, backed by Supabase.

## Project layout

- `androidApp/` — the Android application (entry point, manifest, launcher icons).
- `composeApp/` — all shared code: UI, ViewModels, domain and data layers, string resources.
- `iosApp/` — the Xcode project that hosts the shared UI.

Architecture, conventions and gotchas are in [CLAUDE.md](CLAUDE.md).

## Setup

1. Android Studio (its bundled JBR works for Gradle) and Xcode for iOS.
2. Create `local.properties` next to `settings.gradle.kts`:

   ```properties
   sdk.dir=/path/to/Android/sdk
   SUPABASE_URL=https://<project>.supabase.co
   SUPABASE_ANON_KEY=<anon key>
   GOOGLE_MAPS_API_KEY=<Android Maps SDK key>
   ANALYTICS_BACKEND_BASE_URL=<optional>
   ```

   For a local backend, see "Local Supabase Development" in CLAUDE.md.

## Build and run

```bash
./gradlew :androidApp:installDebug
```

```bash
./gradlew :composeApp:testAndroidHostTest
```

For iOS, open `iosApp/iosApp.xcodeproj` in Xcode and run the `iosApp` scheme on a simulator, or build the shared framework with:

```bash
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64
```
