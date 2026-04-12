# Clex Android

Standalone Android app project for Clex.

This repo is the Android build only. It can be opened directly in Android Studio and does not depend on source code from the rest of the original monorepo at build time.

## Current Release

- Version: `1.9.4`
- Package: `com.clex.android`
- Min SDK: `26`
- Target SDK: `34`

Distribution artifacts built locally from this project:

- Universal APK: `dist/Clex-1.9.4-universal.apk`
- Play bundle: `dist/Clex-1.9.4.aab`

## What The App Includes

- Home, Workspace, Vault, Chain, and Settings screens
- transfer flows with QR / room-code handling
- Clex Link nearby device discovery with BLE invite / accept handoff
- Android Sharesheet send-out and share-into-Clex support
- Vault notes, secret-share, cloud-share, and settings persistence
- Chain feed and detail loading
- in-app Help, Privacy, and Changelog screens
- deep-link handling for receive and vault secret flows
- Android launcher, splash, and release packaging config
- v1.9.4 Android sharesheet crash fix and add-on hardening

## Project Layout

```text
Clex Android/
├── app/
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/clex/android/
│       └── res/
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── INTEGRATION.md
├── RELEASES.md
└── key.properties.example
```

## Open In Android Studio

Open this folder directly:

```text
clex-android
```

Do not open only `app/`.

## Install Locally

Use the universal APK from the latest GitHub Release, or build it locally:

```bash
./gradlew :app:assembleRelease
```

Then install on a connected Android device:

```bash
adb install -r app/build/outputs/apk/release/app-universal-release.apk
```

If Android blocks the install, enable installation from unknown sources for the app you used to open the APK.

## Build From Source

Debug APK:

```bash
./gradlew :app:assembleDebug
```

Release outputs:

```bash
./gradlew :app:assembleRelease :app:bundleRelease
```

## Shareable APK And Play Store Upload

For direct installs:

- use the universal APK
- public file name: `Clex-1.9.4-universal.apk`

For Play Store upload:

- use the `.aab` bundle
- public file name: `Clex-1.9.4.aab`

See [RELEASES.md](RELEASES.md) for artifact names and signing notes.

## Signing

If `key.properties` is missing, release builds fall back to debug signing so the build still completes for local testing.

For Play Store publishing, create a real upload keystore and add:

- `key.properties`
- your `.jks` or `.keystore` file

Use [key.properties.example](key.properties.example) as the template.

## Runtime Service Dependencies

This app talks to deployed backend services at runtime. Those services are not vendored into this repository.

Main external dependencies:

- `https://clex.in`
- `https://clex.in/vault/api`
- `https://clex.in/vault/secret`
- `wss://signal.clex.in`
- Google Drive API endpoints for cloud-share flows

See [INTEGRATION.md](INTEGRATION.md) for the integration notes.

## Notes

- This repository is Android-only.
- The iOS project is separate.
- Build outputs under `dist/` are intentionally ignored from Git.
