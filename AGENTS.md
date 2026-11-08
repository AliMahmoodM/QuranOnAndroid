# AGENTS.md

## Project overview

- This repository is a single-module Android app for Quran recitations without ads.
- The app package is `com.live.holyquranmp3.quran` and the build entry point is `app/`.
- The codebase is largely Java-based Android UI code; it does not use Jetpack Compose or Kotlin as the primary app language.
- The app relies on AndroidX, Material Components, and RecyclerView (`app/build.gradle`).

## Setup and build

- Use the Gradle wrapper from the repo root:
  - `./gradlew assembleDebug`
  - `./gradlew test`
  - `./gradlew installDebug` if you need to push to a connected device
- The project targets Android API 37 and uses:
  - Android Gradle Plugin `9.3.0`
  - Kotlin Gradle plugin `2.2.10`
- The app module is configured in `app/build.gradle`; do not move or rename the app module.

## Architecture and runtime flow

- Main app entry point is `app/src/main/AndroidManifest.xml`.
- The launcher activity is `com.live.holyquranmp3.quran.RecitesName`.
- The main navigation flow is:
  - `RecitesName` loads the reciter list from `LnaguageClass.getAuthorList(...)`, supports filtering and sorting, and launches `AyaList` when a reciter is selected.
  - `AyaList` shows the verses/chapters for the selected reciter and sends a selected verse index to `managerdb`.
  - `managerdb` is the media player/download screen. It uses `PlayerViewModel` to manage playback state and media loading.
  - `Sellings` is a secondary activity for links and marketing content.

## Key files and responsibilities

- `app/src/main/java/com/live/holyquranmp3/quran/RecitesName.java`
  - launcher page UI, search, sort, permissions, and reciter selection
- `app/src/main/java/com/live/holyquranmp3/quran/AyaList.java`
  - list of verses/chapters for a chosen reciter
- `app/src/main/java/com/live/holyquranmp3/quran/managerdb.java`
  - playback controls, seek bar, downloads, and media UI
- `app/src/main/java/com/live/holyquranmp3/quran/PlayerViewModel.java`
  - playback state, song queue, repeat/shuffle, and `MediaPlayer` lifecycle
- `app/src/main/java/com/live/holyquranmp3/quran/LnaguageClass.java`
  - central catalog of reciters and suras; large static `AuthorClass` lists are built here
- `app/src/main/java/com/live/holyquranmp3/quran/SaveSettings.java`
  - persisted app settings, especially language selection
- `app/src/main/java/com/live/holyquranmp3/quran/BaseActivity.java`
  - applies locale at runtime based on saved language preference
- `app/src/main/java/com/live/holyquranmp3/quran/SongsManager.java`
  - builds the per-reciter songs playlist from remote URL data
- `app/src/main/res/values*/`
  - localized resources for English, Arabic, French, German, Spanish, Italian, Romanian, and Russian variants

## Data and app conventions

- Keep new UI code in the existing package hierarchy under `com.live.holyquranmp3.quran`.
- Prefer Java `Activity` + `RecyclerView.Adapter` patterns already used in `RecitesName`, `AyaList`, and `Sellings` over introducing new frameworks.
- Use `ViewBinding` when adding new screens or updating existing layouts (`buildFeatures { viewBinding true }`).
- Use `BaseActivity` for screens that should respect the saved language selection; do not bypass `SaveSettings` or hardcode locale logic in individual screens.
- `SaveSettings.LanguageSelect` drives locale switching via `BaseActivity.attachBaseContext(...)` and the `getLangCode(...)` mapping.
- Playlists and reciter metadata are loaded asynchronously; follow the existing `Async` / callback pattern used by `SongsManager.getPlayListAsync(...)` and `PlayerViewModel.init(...)`.

## Android permissions and platform notes

- `AndroidManifest.xml` declares audio read permissions for Android 13+ and storage permissions for older Android versions.
- The app is explicitly built for modern Android with `READ_MEDIA_AUDIO` and legacy storage compatibility.
- When modifying permission logic, preserve the `Build.VERSION.SDK_INT` checks already present in `RecitesName.checkPermissions()`.

## Editing guidance for agents

- Do not repackage the app into feature modules or new namespaces unless the task explicitly requires it.
- Keep changes small and consistent with the existing naming style (`managerdb`, `RecitesName`, `LnaguageClass`, `PlayerViewModel`, etc.).
- If the task touches playback logic, inspect `PlayerViewModel` first; the screen logic in `managerdb` depends on its `LiveData` state.
- If the task touches reciter data, inspect `LnaguageClass` and `AuthorClass` before adding new entries or changing the catalog shape.
- If the task touches localization, update both the persisted setting and the locale mapping in `BaseActivity`.

## Current repo-specific notes

- The project has a small number of classes in the root `com.live.holyquranmp3.quran` package; this is the expected layout.
- `README.md` is intentionally minimal and mostly contains marketing screenshots; it is not an implementation guide.
- There is no existing root `AGENTS.md`; this file is the current project-specific guide for AI coding agents.

