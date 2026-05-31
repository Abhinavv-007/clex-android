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

## v1.9.13

Public artifact names:

- `Clex-1.9.13-universal.apk`
- `Clex-1.9.13.aab`

Release notes:

- Cinematic motion pass synced with the web design language. Vault note list (`VaultScreen.NoteCard`) staggers in via `RevealFromBottom` at `350ms + index * 80ms` with an 18.dp slide; Chain hero stat cards now wrap in `parallaxFloat` (4.dp amplitude, 4600ms, staggered delay) and the public-ledger rows in `ChainScreen.PublicLedger` stagger at `index * 80ms` with an 18.dp slide for parity with the web table-row entry.
- Chain hero headline parallaxes at `0.55 * scrollState.value` (capped at 56.dp lift) so the title floats slightly faster than the rest of the hero card as the page scrolls upward.
- Workspace state headlines morph through `CxPremium.accentGradient` over 800ms EaseInOut with a `Reverse` repeat — `ConnectingPanel` and `WorkspaceCompletePanel` use the new `lerpAccentGradient` helper, and `WorkspaceErrorPanel` cross-fades the failure glyph between `CxColors.error` and `CxColors.accentSecondary`.
- New `MorphingShareFab` in the Send tab: 48.dp accent circle while files are queued, animates to a 120.dp pill via `animateDpAsState` + spring with the `SHARE` label fading in via `animateFloatAsState` once at least one file is ready.
- `BrutalistButton` now emits a 16dp `CxPremium.neonLime` radial press-glow at 0.30 alpha (pulses for 200ms, fades over 320ms) using `animateFloatAsState` on the press interaction.
- `BrutalistCard` carries a subtle premium shimmer (`premiumShimmer`, 2200ms, 18°, white at 0.08 alpha) so the brand layer reads consistently across cards in Vault, Chain, Workspace, and Settings.
- `BrutalistAccordion` content now slides in via `slideInVertically + expandVertically` on a panel spring; the chevron rotates with `CxSpringSpecs.bounce()` instead of a flat tween.
- Settings hero (`SettingsScreen`) slides up from 24.dp via a 400ms panel spring on first entry while the gear glyph scales 0.8 → 1.0 with a bouncy spring.
- Bottom navigation (`CxBottomNavBar`) now draws a soft accent glow ring under the active tab (radial gradient, 22%/4%/transparent stops). Tab swaps in `AppNavHost` use the new `CxTransitions.tabEnter` (scaleIn 0.95 + fadeIn 200ms) and `CxTransitions.tabExit` (scaleOut 1.05 + fadeOut 160ms) instead of pure cross-fade.
- `SplashScreen` logo carries a 1.0 ↔ 1.02 breathe cycle (2400ms `infiniteRepeatable` sine) once `phase2Logo` settles, layered on top of the existing stamp scale animation.
- Vault note long-press now emits `CxHaptics.dragTick` and pulses the card from 0.98 → 1.0 via a press spring before opening the actions sheet.
- (no behavioural changes — transfer state machine, BLE/WebRTC signaling, and the Vault crypto pipeline are untouched)

## v1.9.12

Public artifact names:

- `Clex-1.9.12-universal.apk`
- `Clex-1.9.12.aab`

Release notes:

- Build infrastructure pass — no user-visible behaviour changes.
- New GitHub Actions CI workflow (`.github/workflows/ci.yml`) runs on every PR and `main` push: compiles debug Kotlin, runs unit tests, runs lint (against a checked-in baseline), and builds both debug and R8-minified release APKs. Artifacts (debug APK, release APKs, lint report, unit-test report) are uploaded with 14-day retention.
- Lint baseline (`app/lint-baseline.xml`) snapshots pre-existing `MissingPermission` warnings on the Bluetooth call sites in `NearbySession.kt`; CI now flags only **new** lint regressions instead of the whole pre-existing surface.
- Unit-test scaffolding added — `junit:4.13.2` + `org.json` for JVM tests, `androidx.test.ext:junit` + `androidx.test:runner` for instrumented tests. First concrete tests pin `ClexChainApi.hashBytes` (SHA-256 of empty + `"abc"`) and `ClexChainApi.fileCategory` across all branches (image, video, audio, pdf, archive, document, other).
- BuildConfig surfaces the WebRTC signaling URL (`wss://signal.clex.in`) and the BLE manufacturer ID (`0xCE48`) via `buildConfigField` entries in `app/build.gradle.kts`. `TransferCoordinator` and `NearbySession` now read from `BuildConfig` so a future debug/staging variant can swap endpoints without touching source. The existing values are unchanged so production behaviour is identical.

## v1.9.11

Public artifact names:

- `Clex-1.9.11-universal.apk`
- `Clex-1.9.11.aab`

Release notes:

- Vault tab transitions ported from `Crossfade(220ms)` to `AnimatedContent` with directional `slideIntoContainer` / `slideOutOfContainer` (260ms slide, 200ms fade, `FastOutSlowInEasing`). NOTES → SECRET → CLOUD → SETTINGS slides left in forward direction, right in reverse.
- `VaultTabSelector` now produces `CxHaptics.snap` on tab change (suppressed when re-tapping the active tab) — matches the workspace tab selector behaviour.
- Vault success/failure haptics:
    - Secret link creation → `CxHaptics.success` on confirmation, `CxHaptics.error` on failure.
    - Drive upload → `CxHaptics.success` on completion, `CxHaptics.error` on failure.
- No behavioural changes to the Vault state machine, encryption, or signaling.

## v1.9.10

Public artifact names:

- `Clex-1.9.10-universal.apk`
- `Clex-1.9.10.aab`

Release notes:

- Workspace tab transitions ported from `Crossfade(220ms)` to `AnimatedContent` with directional `slideIntoContainer` / `slideOutOfContainer` (260ms slide, 200ms fade, `FastOutSlowInEasing`). Tabs swap in the direction of travel instead of soft-fading; the two destinations are no longer composed simultaneously, removing the original Crossfade race that v1.9.7 had to role-gate around.
- Haptic feedback pass on the workspace surface:
    - Tab selector: `CxHaptics.snap` on tab change.
    - Clex Link invite overlay: `CxHaptics.connect` on accept, `CxHaptics.press` on decline.
    - Transfer state transitions (sender + receiver): `CxHaptics.connect` on `CONNECTING`, `CxHaptics.success` on `COMPLETE`, `CxHaptics.error` on `FAILED`. Idle and intermediate spinner states are intentionally silent.
- No behavioural changes to the transfer state machine, BLE flow, or signaling.

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
