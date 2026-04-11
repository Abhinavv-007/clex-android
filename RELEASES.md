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

Suggested public names:

- `Clex-1.8.2-universal.apk`
- `Clex-1.8.2.aab`
