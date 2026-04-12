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

## v1.9.5

Public artifact names:

- `Clex-1.9.5-universal.apk`
- `Clex-1.9.5.aab`

Release notes:

- Fixed Clex Link readiness so choosing the nearby route now prompts BLE permissions and starts discovery on the device that should become visible.
- Added Android chain session write instrumentation with receiver chain-ID handoff to the public ledger flow.
- Expanded mobile ledger detail to show file category, MIME type, size, and hash metadata.

## v1.9.4

Public artifact names:

- `Clex-1.9.4-universal.apk`
- `Clex-1.9.4.aab`

Release notes:

- Fixed the Share to Android Apps crash by exposing the staged `clex-shares` cache path through FileProvider.
- Added safer chooser error handling so Android app sharing failures no longer crash the app.
- Kept Quick Share and other Android app sharing as an additive export path without changing Direct, Local, or Clex Link flows.
