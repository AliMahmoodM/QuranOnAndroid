# Implementation Plan - Update to API 36 (Android 16)

This plan covers updating the `targetSdkVersion` to 36 (Android 16) and addressing the major behavior changes required for this API level, specifically the migration to Predictive Back.

## User Review Required

> [!IMPORTANT]
> Targeting API 36 (Android 16) requires migrating from `onBackPressed()` and `onKeyDown(KEYCODE_BACK)` to the AndroidX `OnBackPressedDispatcher`. This ensures compatibility with Predictive Back system animations.

## Proposed Changes

### Build Configuration

#### [MODIFY] [app/build.gradle](file:///D:/Apps/1 Quran MP3/QuranOnAndroid/app/build.gradle)
- Update `compileSdkVersion` to `36`.
- Update `targetSdkVersion` to `36`.

### Back Navigation Migration

Android 16 disables `onBackPressed()` and `onKeyDown(KEYCODE_BACK)` for apps targeting API 36. We must use `OnBackPressedCallback`.

#### [MODIFY] [RecitesName.java](file:///D:/Apps/1 Quran MP3/QuranOnAndroid/app/src/main/java/com/live/holyquranmp3/quran/RecitesName.java)
- Remove `onBackPressed()`.
- Register an `OnBackPressedCallback` in `onCreate()` to handle the "double tap to exit" logic.

#### [MODIFY] [managerdb.java](file:///D:/Apps/1 Quran MP3/QuranOnAndroid/app/src/main/java/com/live/holyquranmp3/quran/managerdb.java)
- Remove `onKeyDown()`.
- Register an `OnBackPressedCallback` in `onCreate()` to pause playback (if playing) and finish the activity.

### Edge-to-Edge Support

#### [MODIFY] [AndroidManifest.xml](file:///D:/Apps/1 Quran MP3/QuranOnAndroid/app/src/main/AndroidManifest.xml)
- Ensure `android:enableOnBackInvokedCallback="true"` is set in the `<application>` tag (optional but recommended for modern back handling).

## Verification Plan

### Automated Tests
- Run `gradle_build app:assembleDebug` to ensure the project compiles with SDK 36.

### Manual Verification
- Test the back button in `RecitesName` to ensure the "double tap to exit" still works.
- Test the back button in `managerdb` to ensure playback pauses and the activity closes.
