# Clex Android

Standalone Android app project for Clex.

This repo is the Android build only. It can be opened directly in Android Studio and does not depend on source code from the rest of the original monorepo at build time.

## Current Release

- Version: `1.8.2`
- Package: `com.clex.android`
- Min SDK: `26`
- Target SDK: `34`

Distribution artifacts built locally from this project:

- Universal APK: `dist/Clex-1.8.2-universal.apk`
- Play bundle: `dist/Clex-1.8.2.aab`

## What The App Includes

- Home, Workspace, Vault, Chain, and Settings screens
- transfer flows with QR / room-code handling
- Vault notes, secret-share, cloud-share, and settings persistence
- Chain feed and detail loading
- in-app Help, Privacy, and Changelog screens
- deep-link handling for receive and vault secret flows
- Android launcher, splash, and release packaging config

## Project Layout

```text
mobile-frontend-new/
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
mobile-frontend-new
```

Do not open only `app/`.

## Build

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

For Play Store upload:

- use the `.aab` bundle

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
