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

## v1.9.9

Public artifact names:

- `Clex-1.9.9-universal.apk`
- `Clex-1.9.9.aab`

Release notes:

- Release builds now run through R8 (`isMinifyEnabled = true`, `isShrinkResources = true`) with proguard rules covering WebRTC JNI, OkHttp/Okio, PDFBox-Android, Apache POI / XMLBeans / OpenXML schemas, ZXing, and the Clex `Application` subclass. Build-tooling-only references (OSGi, FindBugs, aQute) are silenced via `-dontwarn` since they aren't on the Android runtime classpath. Smaller install footprint and faster cold start.
- Cold start now installs the SplashScreen API (`androidx.core:core-splashscreen`) so the very first frame is the Clex brand mark on a black background instead of the OEM splash. The post-splash transition into the workspace is handled by the platform.
- Added a `ClexApplication` subclass (registered in the manifest) that pre-warms the WebRTC `PeerConnectionFactory` on a background dispatcher and initialises the theme manager early, removing the ~150–300ms hitch on first transfer and the one-frame light/dark theme flash on cold start.

## v1.9.8

Public artifact names:

- `Clex-1.9.8-universal.apk`
- `Clex-1.9.8.aab`

Release notes:

- Fixed a follow-up edge case in the Clex Link `SCAN_ONLY` state introduced in v1.9.7. `peripheralSupported()` was being captured into instance state once at `startDiscovery` time, but `advertiseCallback.onStartFailure` only updated `_sessionState` to `SCAN_ONLY` without also clearing the cached `canAdvertise` flag. So a phone whose chip looked capable but whose `startAdvertise` failed at runtime (system resource exhaustion, too many active advertisers) would correctly enter `SCAN_ONLY` immediately, but the next invite/decline/timeout would route through `scanningBaseState()` and silently revert to `DISCOVERING` because `canAdvertise` was still `true`. The runtime advertise failure now also flips `canAdvertise = false` so the SCAN_ONLY banner stays sticky for the rest of the session.

## v1.9.7

Public artifact names:

- `Clex-1.9.7-universal.apk`
- `Clex-1.9.7.aab`

Release notes:

- Clex Link "scan-only" devices (phones whose Bluetooth chip can't broadcast as a peripheral) now keep their `SCAN_ONLY` state — and the "INVISIBLE TO OTHER PHONES" banner — across invite, decline, timeout, and peer-disconnect cycles, instead of silently flipping back to `DISCOVERING` and misleading the user into thinking their phone had become visible to peers.
- Accepting a Clex Link invite from the SEND or TOOLS tab no longer race-fires the sender-side `startTransfer` against the receiver's own connection during the workspace tab Crossfade. The SEND/RECEIVE auto-handoff effects are now gated on the device's role in the current handshake, so only the controller that matches the local role fires.

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
