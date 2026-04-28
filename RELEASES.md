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

## v1.9.6

Public artifact names:

- `Clex-1.9.6-universal.apk`
- `Clex-1.9.6.aab`

Release notes:

- Fixed Clex Link invites and accept replies being silently dropped on every connection. The GATT link now negotiates a larger ATT MTU before service discovery, so the invite and accept JSON travel in a single ATT PDU instead of being truncated to ~20 bytes by the default 23-byte MTU. Tap-a-device → invite arrives → accept → transfer hand-off now works end-to-end on devices that support BLE peripheral mode.
- Inbound Clex Link invites now appear as a top-level overlay regardless of which workspace tab is selected, so an invite arriving while the user is on SEND or TOOLS no longer silently dismisses itself.
- Devices whose Bluetooth chip can't broadcast as a peripheral (no `bluetoothLeAdvertiser`, or `isMultipleAdvertisementSupported() == false`) now report a dedicated "scan-only" state and surface a banner explaining they're invisible to peers, instead of failing silently. Such phones can still scan for and invite peers that do advertise.
- The GATT server now defers `startAdvertising` until `onServiceAdded` confirms the Clex service is queryable, removing the race where peers connect before the receiver's service is ready.
- Self-discovery is suppressed via a per-install instance ID broadcast in manufacturer-specific data, replacing the broken Android 6+ MAC-based filter that always returned `02:00:00:00:00:00`.
- Refreshed the launcher icon. The mipmap PNGs (mdpi → xxxhdpi, square + round) and the adaptive-icon foreground are regenerated from a single tightened master so the icon body fills more of the launcher tile and respects the 66/108 adaptive-icon safe zone on circle, squircle, and tear-drop masks across OEM launchers.
- Dropped the `ACCESS_FINE_LOCATION` permission on SDK 31+ (it was only needed for legacy BLE scans pre-Android 12).

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
