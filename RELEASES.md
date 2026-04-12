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

## v1.9.1

Public artifact names:

- `Clex-1.9.1-universal.apk`
- `Clex-1.9.1.aab`

Release notes:

- Fixed Android onboarding slide overflow by making setup/tutorial pages scrollable.
- Updated About the Developer with Abhinav Raj, Instagram, LinkedIn, current Clex emails, and experience websites.
- Includes the v1.9.1 transfer stability, portrait QR scanning, large-receive crash protection, and release metadata alignment work.
