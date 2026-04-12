# Android Distribution

Standalone Android project:

- `mobile-frontend-new`

Recommended outputs:

- Direct sharing: universal APK
- Google Play: Android App Bundle (`.aab`)

Signing:

- Create `key.properties` from `key.properties.example`
- Point `storeFile` to your upload keystore
- Release builds fall back to debug signing only when `key.properties` is missing
- Debug-signed release builds are usable for local testing and direct install, but not for Play Store publishing

Expected artifacts after release build:

- `app/build/outputs/apk/release/`
- `app/build/outputs/bundle/release/`

## v1.9.2

Public artifact names:

- `Clex-1.9.2-universal.apk`
- `Clex-1.9.2.aab`

Release notes:

- Applied a glass-style shell pass to Android with softer segmented tabs, chips, labels, and bottom navigation surfaces.
- Brightened the light-theme accent system so selected states read more clearly outside dark mode.
- Aligned the Android release metadata and packaging with the cleaned iOS 1.9.2 content update.
