# Clex — Android Frontend

## Standalone GitHub Folder

`mobile-frontend-new` is a standalone Android Studio project.

You can push this folder by itself to GitHub and open this folder directly in Android Studio without importing code from the rest of `clex-main`.

What is inside this folder:

- Android app source
- Gradle wrapper
- app resources
- local persistence and settings logic
- mobile transfer, vault, chain, and deep-link handling

What is not copied into this folder:

- deployed backend worker source
- website source
- old mobile project source

Those remain external runtime services, documented in [INTEGRATION.md](/Users/abhinav/Downloads/clex-main/mobile-frontend-new/INTEGRATION.md).

**Neo-Brutalist file workspace for Android.**
Drop files, prepare them with built-in tools, share through the fastest route. Privacy-first.

---

## Architecture

```
mobile-frontend-new/
├── app/
│   ├── build.gradle.kts           # App-level Gradle config
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── res/
│       │   └── values/
│       │       ├── strings.xml
│       │       └── themes.xml
│       └── java/com/clex/android/
│           ├── MainActivity.kt                    # Entry point + nav shell
│           ├── navigation/
│           │   ├── NavRoutes.kt                   # Screen routes + bottom nav config
│           │   ├── BottomNavBar.kt                # Neo-Brutalist bottom nav
│           │   └── AppNavHost.kt                  # Navigation graph + transitions
│           └── ui/
│               ├── theme/
│               │   ├── DesignTokens.kt            # Colors, typography, spacing, shadows, animation tokens
│               │   └── Theme.kt                   # Compose theme provider (dark/light)
│               ├── anim/
│               │   ├── AnimationUtils.kt          # Spring specs, entrance anims, idle effects, border-draw
│               │   └── HapticUtils.kt             # Tactile feedback patterns
│               ├── components/
│               │   ├── BrutalistButton.kt         # Primary interaction component
│               │   ├── BrutalistCard.kt           # Card, Badge, TapeStrip, Divider
│               │   ├── TextComponents.kt          # HeroTitle, SectionTitle, MonoText, BodyText, etc.
│               │   ├── MicroAppPanel.kt           # Windowed tool panel with border-draw animation
│               │   ├── ProgressAndStatus.kt       # Progress bar, StatusDot, Radar, ScanLine
│               │   ├── ChainStep.kt               # Tool chain step + pipeline
│               │   └── Accordion.kt               # Collapsible FAQ component
│               └── screens/
│                   ├── splash/SplashScreen.kt     # Cinematic stamp entrance
│                   ├── onboarding/OnboardingScreen.kt  # 3-beat pager: Drop/Prepare/Share
│                   ├── home/HomeScreen.kt         # Full landing: hero, marquee, story, vault, routing, features, trust, CTA
│                   ├── workspace/WorkspaceScreen.kt    # Core app: Send/Receive/Tools with all states
│                   ├── vault/VaultScreen.kt       # Notes/Secret Share/Cloud Share/Settings
│                   ├── chain/ChainScreen.kt       # Pipeline builder + public ledger
│                   └── help/HelpFaqScreen.kt      # FAQ accordion + getting started + tips
├── build.gradle.kts               # Root Gradle
├── settings.gradle.kts
├── gradle.properties
└── README.md
```

---

## Design System: Neo-Brutalism

### Non-Negotiable Rules

| Rule | Implementation |
|------|---------------|
| **Hard borders** | `CxBorders.thin` (2dp) to `CxBorders.heavy` (5dp), always solid, never rounded beyond 4dp |
| **Directional shadows** | `HardShadow(x, y)` — hard offset rectangles drawn in `drawBehind`, never blurred |
| **Physical press** | Buttons: shadow collapses + element shifts on press via spring animation |
| **Monospace headings** | `CxTypography.fontDisplay` = Monospace, always UPPERCASE, tight tracking |
| **No Material ripple** | All `clickable` uses `indication = null`. Press feedback is shadow collapse + haptics |
| **No gradients** | Zero gradient usage. Flat fills only. Accent color muted at 15% alpha for subtle tints |
| **No blur** | No `backdrop-filter` equivalents. Hard borders and opaque backgrounds only |

### Color Palette

| Token | Dark Mode | Light Mode |
|-------|-----------|------------|
| `accent` | `#C8FF00` | `#C8FF00` |
| `accentSecondary` | `#FF3D00` | `#FF3D00` |
| `accentTertiary` | `#00D4FF` | `#00D4FF` |
| `bgPrimary` | `#0A0A0A` | `#F5F0E8` |
| `bgCard` | `#141414` | `#FFFFFF` |
| `textPrimary` | `#F0F0E8` | `#0A0A0A` |
| `borderBold` | `#F0F0E8` | `#0A0A0A` |

### Typography

- **Display** (headings): System Monospace, Bold/Black, UPPERCASE, tight letter-spacing (−0.05em)
- **Body**: System Sans-Serif (Inter-like), Regular, normal casing, relaxed line-height (1.6×)
- **Mono** (labels, data, code): System Monospace, Bold, UPPERCASE, wide letter-spacing (+0.1em)

### Animation System

| Config | Stiffness | Damping | Use Case |
|--------|-----------|---------|----------|
| `snap` | 800 | 0.7 | Buttons, chips, small UI |
| `panel` | 400 | 0.72 | MicroApp panels, screen enters |
| `bounce` | 300 | 0.55 | Onboarding, success states |
| `gentle` | 150 | 0.85 | Idle floats, background |
| `slam` | 1200 | 0.65 | Hero elements, stamps |
| `press` | 2000 | 0.9 | Button depression |

### Haptic Patterns

- **Press**: `KEYBOARD_TAP` — sharp, immediate
- **Connect**: Double-tap vibration
- **Success**: Triumphant 3-beat pattern
- **Error**: Hard single buzz
- **Snap**: `CLOCK_TICK` — tiny tick
- **Scanning**: Rhythmic pulse

---

## Screen Map

### 1. Splash Screen
Cinematic 2.5s entrance: black → CLEX stamps in at 2× scale → snaps to 1× → accent underline draws → tagline fades → auto-navigate.

### 2. Onboarding
3-beat horizontal pager: **Drop** → **Prepare** → **Share**. Each beat: giant outlined number, floating visual symbol, title, subtitle, tags. Skip button always visible. Last page shows CTA.

### 3. Home
Full landing page matching web architecture:
- Hero with "DROP PREPARE SHARE" title + CTAs
- Scrolling marquee strip (accent background, diamond separators)
- Product story: 3 numbered step cards
- Vault section with feature chips
- Routing panel with 3 route cards (P2P/Local/Drive) + status dots
- Features grid: 6 tool cards in 2-column layout
- Trust strip: 5 checkmark items
- Final CTA with accent background

### 4. Workspace
Core app screen with 3 tabs:
- **Send**: Empty (dashed drop zone + floating icon) → Files Ready (file list + stats + actions) → Processing (MicroAppPanel + progress) → Sharing (radar scan + route probing) → Connected (route confirmed) → Transferring (segmented progress + speed) → Success (stamp animation + stats) → Error (retry/reset) → Offline
- **Receive**: Share code input + QR scanner with scan line overlay
- **Tools**: Scrollable list of 8 tools with icon, title, description

### 5. Vault
4 tabs:
- **Notes**: Folder list + note cards (title, preview, time, encryption icon). Empty state with CTA.
- **Secret Share**: Text input → protection toggles → expiry selector → generate → encrypted animation → link ready with copy/QR
- **Cloud Share**: Google Drive connect CTA + limits info + upload area
- **Settings**: Encryption, sync, recovery, expiry, theme rows + export/clear actions

### 6. Chain
- Hero with trust items and badge pills
- How Chain Works: 4-step horizontal scrollable cards
- Pipeline Builder: interactive chain visualization + popular preset chains
- Public Ledger: simulated live entries with hash, route, duration, status
- Vault Boundary: 3 explanation cards

### 7. Help / FAQ
- Hero with title
- 6 FAQ categories with accordion items (all content from web FAQ)
- Getting Started: 5 numbered steps
- Tips & Tricks: 4 cards
- Contact CTA with accent background

---

## Component Index

| Component | File | Description |
|-----------|------|-------------|
| `BrutalistButton` | `BrutalistButton.kt` | Primary/Secondary/Ghost variants, 3 sizes, physical press, hard shadow |
| `BrutalistCard` | `BrutalistCard.kt` | Container with hard border + shadow, optional accent border, press state |
| `BrutalistBadge` | `BrutalistCard.kt` | Small chip/tag with optional dot, filled/outline variants |
| `BrutalistDivider` | `BrutalistCard.kt` | Hard line separator |
| `TapeStrip` | `BrutalistCard.kt` | Decorative accent-color bar |
| `MicroAppPanel` | `MicroAppPanel.kt` | Windowed panel with title bar, border-draw entrance, close button |
| `SectionLabel` | `MicroAppPanel.kt` | Pill with dot indicator |
| `HeroTitle` | `TextComponents.kt` | Giant monospace heading |
| `SectionTitle` | `TextComponents.kt` | Section-level heading |
| `CardTitle` | `TextComponents.kt` | Card heading |
| `BodyText` | `TextComponents.kt` | Body paragraph text |
| `MonoText` | `TextComponents.kt` | Monospace utility text |
| `LabelText` | `TextComponents.kt` | Small accent label |
| `AccentNumber` | `TextComponents.kt` | Large accent-colored number |
| `BrutalistProgressBar` | `ProgressAndStatus.kt` | Smooth or segmented, hard borders, animated |
| `StatusDot` | `ProgressAndStatus.kt` | Colored dot with optional pulse |
| `RadarBackground` | `ProgressAndStatus.kt` | Animated radar sweep |
| `ScanLineOverlay` | `ProgressAndStatus.kt` | Horizontal scan line |
| `ChainStep` | `ChainStep.kt` | Single pipeline step with status |
| `ChainPipeline` | `ChainStep.kt` | Horizontal chain visualization |
| `BrutalistAccordion` | `Accordion.kt` | Collapsible FAQ item |

---

## Animation Library

| Animation | Utility | Effect |
|-----------|---------|--------|
| `RevealFromBottom` | `AnimationUtils.kt` | Slide + fade entrance with delay |
| `SlamIn` | `AnimationUtils.kt` | Scale 1.15× → 1× snap entrance |
| `StampIn` | `AnimationUtils.kt` | Scale 2× → 1× heavy stamp |
| `floatingIdle` | `AnimationUtils.kt` | Gentle Y-axis float loop |
| `pulseGlow` | `AnimationUtils.kt` | Pulsing accent glow behind element |
| `borderDrawIn` | `AnimationUtils.kt` | Perimeter-tracing border animation |
| `StaggeredColumn` | `AnimationUtils.kt` | Stagger-delay helper for children |
| `CxTransitions` | `AnimationUtils.kt` | Screen enter/exit/pop transition specs |

---

## Setup

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Android SDK 34

### Build
```bash
cd mobile-frontend-new
./gradlew assembleDebug
```

### Run
Open in Android Studio → Run on emulator or device (API 26+).

### Notes
- **Standalone Android project** — this folder contains everything required to build the app itself.
- **Backend integrated** — the app talks to live services at `clex.in`, `signal.clex.in`, and Google Drive APIs. See [INTEGRATION.md](/Users/abhinav/Downloads/clex-main/mobile-frontend-new/INTEGRATION.md).
- **Dark theme default** — light theme tokens are defined but dark is primary.
- **Custom fonts**: To use Space Mono and Inter, add font files to `res/font/` and update `CxTypography` font families. System monospace/sans-serif are used as defaults.
- **Icons**: The app uses typographic symbols (⊕, ◈, ⟐, etc.) instead of Material icons to maintain Neo-Brutalist authenticity.

---

## Design Decisions

1. **No Material Design** — Zero MD3 theming, no ripple effects, no rounded corners, no elevation shadows. Every visual element is custom-drawn to maintain Neo-Brutalist purity.

2. **Shadow as drawBehind** — Hard offset shadows are drawn manually in `drawBehind` blocks rather than using elevation, ensuring the directional hard-shadow look.

3. **Typographic icons** — Unicode symbols instead of icon libraries. Keeps the raw, technical aesthetic and avoids soft, friendly icon styles.

4. **Spring-first motion** — All transitions use spring physics rather than linear/eased tweens, giving every element a physical, weighted feel.

5. **Haptic integration** — Every press, connection, success, and error has a corresponding tactile pattern. The app should feel heavy and mechanical.

6. **State exhaustiveness** — Every screen covers: default, loading, active, success, error, empty, and offline states. No undesigned paths.

---

*Built for Clex by Abhinav. Privacy-first file movement.*
