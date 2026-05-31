package com.clex.android

data class ChangelogEntry(
    val version: String,
    val releasedOn: String,
    val notes: List<String>
)

object AppRelease {
    const val versionName = "1.9.13"
    const val versionCode = 203

    val changelog = listOf(
        ChangelogEntry(
            version = "1.9.13",
            releasedOn = "31 May 2026",
            notes = listOf(
                "Cinematic motion pass synced with the web design language: Vault note cards, Chain hero stats, and the Chain ledger now stagger in at 80ms-per-row with an 18.dp slide-from-below.",
                "Chain hero headline parallaxes at 0.55x scroll while the live-session and chains stat counters get an idle parallaxFloat so the page feels alive at rest.",
                "Workspace transfer headlines (CONNECTING / COMPLETE / FAILED) now sweep through the accent gradient over 800ms EaseInOut, and a morphing 48dp → 120dp Share FAB appears once files are loaded with the SHARE label fading in.",
                "BrutalistButton emits a 16dp neonLime radial press-glow that pulses 200ms then fades; BrutalistCard now carries a subtle 2200ms diagonal shimmer at 18° / 8% alpha so the brand layer reads consistently across cards.",
                "Accordion content slides in on a panel spring and the chevron rotates with a bounce spring instead of a flat tween.",
                "Settings hero now slides up from 24.dp on entry while the gear glyph scales 0.8 → 1.0; the bottom navigation gets a soft accent glow ring under the active tab and tab swaps use a scaleIn 0.95 + fadeIn 200ms / scaleOut 1.05 + fadeOut 160ms transition.",
                "Splash logo carries a gentle 1.0 ↔ 1.02 breathe cycle once it has stamped in, and Vault note long-press fires a dragTick haptic plus a 0.98 → 1.0 scale pulse before opening the actions sheet.",
                "(no behavioural changes — transfer state machine, BLE/WebRTC, and the Vault crypto pipeline are untouched)"
            )
        ),
        ChangelogEntry(
            version = "1.9.12",
            releasedOn = "28 Apr 2026",
            notes = listOf(
                "Build infrastructure pass — no user-visible behaviour changes.",
                "GitHub Actions CI now runs on every PR: compile, unit tests, lint (with baseline), and both debug + release (R8) APK builds.",
                "Unit-test scaffolding added with first JVM tests pinning SHA-256 hashing and MIME→category classification.",
                "WebRTC signaling URL and the BLE manufacturer ID are now read from BuildConfig so a future debug/staging variant can swap endpoints without source changes."
            )
        ),
        ChangelogEntry(
            version = "1.9.11",
            releasedOn = "28 Apr 2026",
            notes = listOf(
                "Vault tab transitions ported from a 220ms cross-fade to AnimatedContent with directional slides — moving NOTES → SECRET → CLOUD → SETTINGS slides left, going back slides right, with a 200ms fade overlap.",
                "Vault tab selector now produces a snap haptic on tab change.",
                "Vault success/failure haptics: creating a Secret link buzzes 'success' on confirmation and 'error' on failure; uploading to Drive buzzes the same ladder when the upload completes or fails."
            )
        ),
        ChangelogEntry(
            version = "1.9.10",
            releasedOn = "28 Apr 2026",
            notes = listOf(
                "Workspace tab transitions now use a directional slide instead of a 220ms cross-fade. Moving from SEND → RECEIVE → TOOLS slides left, going back slides right, with a 200ms fade and a fast-out-slow-in spring. Both tabs are no longer composed at once during the swap.",
                "Tactile feedback pass: tab switch produces a snap haptic, accepting a Clex Link invite produces a connect haptic, declining produces a press haptic, and the transfer headlines (CONNECTING, COMPLETE, FAILED) each fire their own haptic on entry on both sender and receiver. The workspace top-level tab selector also now haptics on switch.",
                "(no behavioural changes — the transfer state machine, BLE flow, and signaling are untouched)"
            )
        ),
        ChangelogEntry(
            version = "1.9.9",
            releasedOn = "28 Apr 2026",
            notes = listOf(
                "Release builds now ship through R8 minification and resource shrinking. Smaller download, smaller install footprint, and faster cold start because dead code from PDFBox, Apache POI, ZXing, and the WebRTC native bindings is stripped instead of bundled.",
                "Cold start now uses the Android 12+ SplashScreen API (backported to API 26+) so the very first frame is the Clex brand mark on a black background, not the OEM default. The post-splash transition into the workspace is handled by the platform.",
                "Native WebRTC libraries are now pre-warmed on a background thread during `Application.onCreate`, removing the ~150–300ms hitch the first time the workspace opens a transfer.",
                "Theme manager now initialises in `Application.onCreate` instead of `MainActivity.onCreate` so dark/light mode is applied to the very first composed frame rather than flashing the wrong theme for one frame on cold start."
            )
        ),
        ChangelogEntry(
            version = "1.9.8",
            releasedOn = "28 Apr 2026",
            notes = listOf(
                "Fixed a follow-up edge case in the Clex Link 'invisible to other phones' state: if BLE peripheral capability looked available at the start of a discovery session but the advertise actually failed at runtime (e.g. too many active advertisers), the next invite cycle could silently revert the state from SCAN_ONLY to DISCOVERING and hide the banner. The runtime failure path now also caches the unavailable advertise capability so the banner stays sticky."
            )
        ),
        ChangelogEntry(
            version = "1.9.7",
            releasedOn = "28 Apr 2026",
            notes = listOf(
                "Fixed Clex Link 'invisible to other phones' state flickering off after every invite, decline, timeout, or peer disconnect on phones whose Bluetooth chip can't broadcast as a peripheral. The scan-only state — and its banner — now stick across the whole discovery session.",
                "Fixed a workspace tab Crossfade race where accepting a Clex Link invite from the SEND or TOOLS tab could double-fire the sender-side controller against the receiver's room code. The auto-handoff effects on both sides are now gated on the device's role in the current handshake."
            )
        ),
        ChangelogEntry(
            version = "1.9.6",
            releasedOn = "28 Apr 2026",
            notes = listOf(
                "Fixed Clex Link invites and accept replies being silently dropped on every nearby connection. The BLE GATT link now negotiates a larger ATT MTU before service discovery so the invite and accept payloads travel intact instead of being truncated to ~20 bytes by the default 23-byte MTU.",
                "End-to-end nearby hand-off (tap a device → invite arrives on the other phone → accept → transfer) now works on devices that support BLE peripheral mode.",
                "Inbound Clex Link invites now show as a top-level overlay regardless of which workspace tab you're on, so invites arriving while you're on SEND or TOOLS no longer silently dismiss themselves.",
                "Phones whose Bluetooth chip can't broadcast as a peripheral now show a clear 'invisible to other phones' banner instead of failing silently — they can still tap nearby Clex devices to send.",
                "Removed the race where peers could connect to the GATT server before the Clex service was ready by deferring advertise start until the service is fully registered.",
                "Refreshed the launcher icon at every density (mdpi through xxxhdpi, square and round) and the adaptive-icon foreground so the icon body fills more of the launcher tile and stays inside the safe zone on circle, squircle, and tear-drop masks across OEM launchers."
            )
        ),
        ChangelogEntry(
            version = "1.9.5",
            releasedOn = "12 Apr 2026",
            notes = listOf(
                "Fixed Clex Link readiness so selecting the nearby route now prompts BLE permissions and starts discovery on the device that should become visible.",
                "Added Android chain write instrumentation with receiver chain-ID handoff and surfaced file type, file size, and hash detail in the mobile ledger view.",
                "Updated the workspace and vault page marks and aligned chain receiver-ID propagation across the mobile and web stacks."
            )
        ),
        ChangelogEntry(
            version = "1.9.4",
            releasedOn = "12 Apr 2026",
            notes = listOf(
                "Fixed the Share to Android Apps crash by exposing the staged clex-shares cache path through FileProvider.",
                "Added guarded chooser error handling so Android sharing failures surface as a message instead of killing the app.",
                "Kept the Quick Share / Android app share path as an additive export flow without changing Direct, Local, or Clex Link behavior."
            )
        ),
        ChangelogEntry(
            version = "1.9.3",
            releasedOn = "12 Apr 2026",
            notes = listOf(
                "Converted Clex Link from a simulated nearby state flow into a real BLE GATT invite / accept handoff that resolves into Local or Direct transfer routes.",
                "Fixed Android share-in and share-out plumbing so external apps can send files into Clex and workspace files can be exported through the Android chooser as an additive path.",
                "Corrected room-code handoff, BLE readiness handling, and receiver-side Clex Link startup without disturbing existing Direct, Local, QR, or code flows."
            )
        ),
        ChangelogEntry(
            version = "1.9.2",
            releasedOn = "12 Apr 2026",
            notes = listOf(
                "Applied a glass-style shell pass to the Android app with softer segmented tabs, chips, labels, and bottom navigation surfaces.",
                "Brightened the light-theme accent system so selected states read more clearly outside dark mode.",
                "Aligned mobile release metadata to v1.9.2 and synced the visual pass with the cleaned iOS content update."
            )
        ),
        ChangelogEntry(
            version = "1.9.1",
            releasedOn = "12 Apr 2026",
            notes = listOf(
                "Moved Android receive handling to temp-file storage so large transfers no longer keep full payloads in Compose state.",
                "Improved transfer connection messaging, portrait QR scanning, and local-route negotiation behavior.",
                "Added a filled developer profile, sharper settings branding, and aligned the mobile release to v1.9.1."
            )
        ),
        ChangelogEntry(
            version = "1.8.2",
            releasedOn = "11 Apr 2026",
            notes = listOf(
                "Restored the original page-level glyph marks while keeping the new app icon and launcher branding.",
                "Refined the Chain screen session expander and removed the harsh session-list border treatment.",
                "Added a full in-app changelog screen and aligned the visible app version to 1.8.2."
            )
        ),
        ChangelogEntry(
            version = "1.8.0",
            releasedOn = "10 Apr 2026",
            notes = listOf(
                "Stabilized the Chain page entry sequence so cards reveal in-place instead of reflowing downward.",
                "Cleaned the public ledger detail flow and improved live session loading behavior.",
                "Smoothed Chain section motion to match Vault and Settings more closely."
            )
        ),
        ChangelogEntry(
            version = "1.7.6",
            releasedOn = "10 Apr 2026",
            notes = listOf(
                "Reworked Vault Notes actions into a cleaner bottom-sheet style interaction.",
                "Made the My Notes and New Note header controls feel more balanced and structured.",
                "Improved Vault Plus organization so account, sync, expiry, backup, and device controls are easier to reach."
            )
        ),
        ChangelogEntry(
            version = "1.7.2",
            releasedOn = "09 Apr 2026",
            notes = listOf(
                "Enabled working workspace tools instead of leaving visible controls in a next-update state.",
                "Added QR-backed receive/share flows and boxed share-code presentation.",
                "Improved local receive timing so links and codes stay valid long enough to hand off cleanly."
            )
        ),
        ChangelogEntry(
            version = "1.7.0",
            releasedOn = "09 Apr 2026",
            notes = listOf(
                "Made Popular Chains interactive and tied chain steps to real selection state.",
                "Improved chain detail readability and card consistency throughout the public ledger flow.",
                "Brought chain section motion and state handling closer to the live backend feed."
            )
        ),
        ChangelogEntry(
            version = "1.6.8",
            releasedOn = "08 Apr 2026",
            notes = listOf(
                "Introduced reusable staggered section entry patterns across Vault, Chain, Workspace, and Settings.",
                "Rebuilt the Privacy view as native in-app content instead of showing the website inside the app shell.",
                "Reduced screen-to-screen animation mismatch so the product feels more coherent on mobile."
            )
        ),
        ChangelogEntry(
            version = "1.6.3",
            releasedOn = "08 Apr 2026",
            notes = listOf(
                "Unified app icon, splash branding, and shared brand asset handling inside the Android project.",
                "Improved page-top identity treatment and status chips across primary screens.",
                "Cleaned several remaining launch and resource mismatches."
            )
        ),
        ChangelogEntry(
            version = "1.6.0",
            releasedOn = "07 Apr 2026",
            notes = listOf(
                "Rebuilt onboarding so it renders correctly in both dark and light themes.",
                "Fixed first-run gating so onboarding does not reopen on every launch.",
                "Replaced weak placeholder graphics with cleaner mobile-first walkthrough panels."
            )
        ),
        ChangelogEntry(
            version = "1.5.6",
            releasedOn = "07 Apr 2026",
            notes = listOf(
                "Strengthened the light-theme accent system so selected states no longer look washed out.",
                "Improved top-bar chips, tags, and accent surfaces for better readability in light mode.",
                "Reduced mixed-theme artifacts across the app shell."
            )
        ),
        ChangelogEntry(
            version = "1.5.0",
            releasedOn = "06 Apr 2026",
            notes = listOf(
                "Added a dedicated Settings destination with persistent theme control and app-level options.",
                "Moved Help and Privacy into proper in-app flows and cleaned up settings navigation.",
                "Reduced shell confusion by tightening the bottom-nav structure."
            )
        ),
        ChangelogEntry(
            version = "1.4.4",
            releasedOn = "05 Apr 2026",
            notes = listOf(
                "Connected Vault Cloud state and backup metadata handling more cleanly to the mobile experience.",
                "Improved Drive auth pickup and persisted account-backed Vault state.",
                "Expanded Vault settings support for backup, sync, and account device visibility."
            )
        ),
        ChangelogEntry(
            version = "1.4.0",
            releasedOn = "04 Apr 2026",
            notes = listOf(
                "Added secret deep-link handling so supported Vault links can resolve inside the app.",
                "Improved secret expiry handling and mobile reveal flow protections.",
                "Tightened the handoff from incoming links into the correct Vault state."
            )
        ),
        ChangelogEntry(
            version = "1.3.6",
            releasedOn = "03 Apr 2026",
            notes = listOf(
                "Encrypted Vault notes at rest and improved migration from older local note storage.",
                "Surfaced sync and restore status more clearly for Vault note state.",
                "Reduced dead placeholder behavior around backup and sync controls."
            )
        ),
        ChangelogEntry(
            version = "1.3.0",
            releasedOn = "01 Apr 2026",
            notes = listOf(
                "Wired the workspace transfer flow to real Android-side file picking and transfer coordination.",
                "Improved receive routing, room-code behavior, and transfer-state handling.",
                "Stabilized the main send and receive shell for everyday device use."
            )
        ),
        ChangelogEntry(
            version = "1.2.5",
            releasedOn = "28 Mar 2026",
            notes = listOf(
                "Connected Chain to live session data and real session detail loading.",
                "Replaced dead summary-only rows with richer ledger-backed content.",
                "Started bringing mobile Chain behavior closer to the web product."
            )
        ),
        ChangelogEntry(
            version = "1.2.0",
            releasedOn = "24 Mar 2026",
            notes = listOf(
                "Simplified the main navigation structure into the current four-tab layout.",
                "Removed confusing extra nav behavior and improved top-level route stability.",
                "Started consolidating app-wide state into cleaner mobile-focused screens."
            )
        ),
        ChangelogEntry(
            version = "1.1.4",
            releasedOn = "18 Mar 2026",
            notes = listOf(
                "Improved home/workflow routing and reduced broken screen handoffs.",
                "Cleaned several rough layout and state issues across the early mobile build.",
                "Stabilized core paths before deeper backend work landed."
            )
        ),
        ChangelogEntry(
            version = "1.1.0",
            releasedOn = "10 Mar 2026",
            notes = listOf(
                "Fixed Gradle, launcher resource, and compile issues that were blocking Android Studio runs.",
                "Added missing app resources and repaired the initial Compose build path.",
                "Produced the first stable debug APK for the standalone mobile project."
            )
        ),
        ChangelogEntry(
            version = "1.0.0",
            releasedOn = "01 Mar 2026",
            notes = listOf(
                "Established the standalone Android project foundation for the new mobile frontend.",
                "Set up Compose navigation, the first screen set, and the initial project structure.",
                "Brought the new mobile app into a runnable state inside Android Studio."
            )
        )
    )
}
