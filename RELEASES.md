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

## v1.9.3

Public artifact names:

- `Clex-1.9.3-universal.apk`
- `Clex-1.9.3.aab`

Release notes:

- Added real Clex Link nearby handoff over BLE GATT, then resolved accepted sessions into Local or Direct transfer routes.
- Fixed Android Sharesheet add-on behavior so files can be shared out to Android apps and shared into Clex without disrupting existing flows.
- Includes corrected room-code handoff, receiver-side Clex Link readiness, and aligned Android release metadata for v1.9.3.
