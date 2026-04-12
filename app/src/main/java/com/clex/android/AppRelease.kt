package com.clex.android

data class ChangelogEntry(
    val version: String,
    val releasedOn: String,
    val notes: List<String>
)

object AppRelease {
    const val versionName = "1.9.4"
    const val versionCode = 194

    val changelog = listOf(
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
