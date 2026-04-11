# Mobile Project Scope

`mobile-frontend-new` is a standalone Android project.

You can push this folder by itself to GitHub and open it directly in Android Studio.

## Included In This Folder

- full Android source under `app/src/main`
- Gradle wrapper and Gradle build files
- app resources, manifest, navigation, UI, local persistence, transfer logic, vault logic, chain wiring, and settings persistence

## Not Imported From Outside This Folder

The Android app does **not** import source files from:

- `../apps`
- `../packages`
- `../frontend-2`
- `../clex-mobile`
- any absolute local filesystem source path

Build-time source dependency check was done against the current codebase before this file was added.

## Runtime Service Dependencies

This app still talks to deployed services at runtime. Those services are not copied into this folder because the mobile app consumes them over network APIs.

Current external runtime dependencies:

- `https://clex.in`
- `https://clex.in/vault/api`
- `https://clex.in/vault/secret`
- `wss://signal.clex.in`
- Google Drive API endpoints for cloud share

Main code references:

- `app/src/main/java/com/clex/android/data/ClexBackend.kt`
- `app/src/main/java/com/clex/android/data/transfer/TransferCoordinator.kt`
- `app/src/main/java/com/clex/android/MainActivity.kt`
- `app/src/main/AndroidManifest.xml`

## Practical Meaning

- This folder is self-contained for Android development and GitHub push.
- It is **not** a full monorepo copy of the deployed backend.
- If you want backend source vendored into this folder too, that is a separate duplication step and should be done intentionally because it creates drift risk.
