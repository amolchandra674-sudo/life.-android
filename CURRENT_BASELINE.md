# Current Baseline — Life Android

## Authority and verification

- **Verified baseline:** v27, Android version name `1.0.27` (CI run number 27).
- **Authoritative app-code baseline:** commit `51df53cffb289aeda09203395ea67a221a87b2ff` (`Fix occasional sorting and make reorder deterministic without duplicates`). This is the latest commit that changed application source; subsequent commit(s) add baseline documentation only.
- **Verification evidence:** GitHub Actions run 27 built successfully from that app-code commit. The recovered APK matches the APK inside the run-27 Actions artifact (SHA-256 `d6e0639e608e8ba2a44e5b9e267ea6b3eff3e2d99e22e998ea1fb10bd26bcb3e`). The user then confirmed on-device: “Works well / Nothing breaking” after testing the drag interactions.
- Do not use older APKs or earlier source iterations as the starting point unless investigating a specific regression.

## Source and entry point

- Android project root: repository root.
- UI and application logic: `app/src/main/assets/index.html` (loaded by the WebView).
- Android bridge / WebView wrapper: `app/src/main/java/com/life/app/MainActivity.java`.
- App/build configuration: `app/build.gradle`, root `build.gradle`, `settings.gradle`, and `gradle.properties`.
- CI build: `.github/workflows/build-apk.yml`.

## Build

CI uses JDK 17, Gradle 9.7.1, Android SDK platform 36, and build-tools 36.0.0. From the repository root with those tools installed:

```sh
gradle --no-daemon -PversionCode=27 :app:assembleDebug
```

The APK is written to `app/build/outputs/apk/debug/app-debug.apk`. CI sets `versionCode` from the GitHub Actions run number and derives `versionName` as `1.0.<versionCode>`; future builds should therefore use their actual run number rather than reusing 27.

Debug signing is configured to use `app/life-debug.keystore`. **Preserve this exact signing key and signing configuration** so updates can install over the existing app. Do not replace, regenerate, or remove it during cleanup.

## Behavior that must not regress

- Routine plans: **Regularly Plan** and **Fall Back Plan**; Study plan remains separate and stable.
- Long-press drag-and-drop reordering for Routine, Study, and Occasional Tasks; one dragged task must not create duplicates.
- Occasional task add/edit/delete/complete behavior and persisted ordering.
- Task start, pause, resume, complete, stop, skip/unskip, edit, and delete flows.
- Local-first persistence, existing stored task data and history, and JSON backup import/export.
- Weight tracking, subject revision counts and correction, activity history, CSV/text export, and print/save as PDF.

Do not redesign working interactions or change the data schema as part of unrelated maintenance. Protect backward compatibility with existing local data.

## Persistence and dependencies

- Android application ID: `com.life.app`; min SDK 29; compile/target SDK 36.
- The UI is a local HTML/CSS/JavaScript app inside Android WebView. Data is stored locally using browser `localStorage` (key `life-v4-local`); there is no application server/backend.
- Android native bridges provide file saving, JSON file selection, and printing.
- Keep the existing Android Gradle Plugin, Gradle, SDK, and signing configuration aligned with the CI workflow. This repository currently has no automated UI test suite.

## Known status and next work

- **Known status:** v27 builds successfully and the user has confirmed the drag interactions work without visible breakage. Automated device-level regression tests have not been run.
- **Next development task:** none queued. Keep this as the stable baseline; make only requested, incremental changes and check the affected behavior before calling a new build verified.
