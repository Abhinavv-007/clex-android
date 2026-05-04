<!-- =====================================================================
     Clex Android — clex.in on Android
     ===================================================================== -->

<div align="center">

<img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.png" alt="Clex Android icon" width="120" />

# 🤖 Clex — Android

### Standalone, **glassy** Android client for [clex.in](https://clex.in) — Workspace, Vault, Chain, and **Clex Link** nearby device discovery (BLE).

<a href="https://clex.in"><img src="https://img.shields.io/badge/Live-clex.in-FFD83D?style=for-the-badge&labelColor=111111" alt="Live" /></a>
<img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white&labelColor=111111" alt="Android" />
<img src="https://img.shields.io/badge/Min%20SDK-26%20%28Android%208%29-3DDC84?style=for-the-badge&logo=android&logoColor=white&labelColor=111111" alt="Min SDK" />
<img src="https://img.shields.io/badge/Target%20SDK-34-3DDC84?style=for-the-badge&logo=android&logoColor=white&labelColor=111111" alt="Target SDK" />
<a href="https://github.com/Abhinavv-007/clex-android/actions/workflows/ci.yml"><img src="https://img.shields.io/github/actions/workflow/status/Abhinavv-007/clex-android/ci.yml?style=for-the-badge&logo=githubactions&label=CI&labelColor=111111&color=3DDC84" alt="CI" /></a>

<br />

<a href="https://github.com/Abhinavv-007/clex-android/stargazers"><img src="https://img.shields.io/github/stars/Abhinavv-007/clex-android?style=flat-square&logo=github&color=FFD83D&labelColor=111111" alt="Stars" /></a>
<a href="https://github.com/Abhinavv-007/clex-android/commits/main"><img src="https://img.shields.io/github/last-commit/Abhinavv-007/clex-android?style=flat-square&logo=git&color=FFD83D&labelColor=111111" alt="Last commit" /></a>
<img src="https://img.shields.io/github/commit-activity/m/Abhinavv-007/clex-android?style=flat-square&logo=github&color=FFD83D&labelColor=111111" alt="Commit activity" />
<img src="https://img.shields.io/github/repo-size/Abhinavv-007/clex-android?style=flat-square&logo=files&color=FFD83D&labelColor=111111" alt="Repo size" />
<img src="https://img.shields.io/github/languages/top/Abhinavv-007/clex-android?style=flat-square&logo=kotlin&color=FFD83D&labelColor=111111" alt="Top language" />
<img src="https://img.shields.io/github/v/tag/Abhinavv-007/clex-android?style=flat-square&logo=semver&color=FFD83D&labelColor=111111&label=version" alt="Version" />

<br />

<sub><b>Jetpack Compose · BLE Clex Link · WebRTC transfer · Apple-style glass UI</b></sub>

</div>

<br />

---

## ✦ Current Release — `1.9.12`

| | |
|---|---|
| Package | `com.clex.android` |
| Min SDK | `26` (Android 8.0) |
| Target SDK | `34` |
| Universal APK | `dist/Clex-1.9.12-universal.apk` |
| Play bundle | `dist/Clex-1.9.12.aab` |

> The repo is the Android build only. It opens directly in Android Studio and does not depend on source code from the rest of the original monorepo at build time.

---

## ✦ What's Inside

<table>
  <tr>
    <td width="50%" valign="top">
      <h3>🏠 Five-tab Shell</h3>
      <p><b>Home</b> · <b>Workspace</b> · <b>Vault</b> · <b>Chain</b> · <b>Settings</b> — Compose throughout, with directional <code>AnimatedContent</code> slides and a full haptic feedback pass.</p>
    </td>
    <td width="50%" valign="top">
      <h3>📡 Clex Link (BLE)</h3>
      <p>Nearby device discovery with BLE invite / accept handoff. Permission-aware, MTU-fixed in v1.9.6, scan-only state persistence in v1.9.7, runtime-failure tolerance in v1.9.8.</p>
    </td>
  </tr>
  <tr>
    <td width="50%" valign="top">
      <h3>🪂 Transfer flows</h3>
      <p>QR / room-code handling, Sharesheet send-out, share-into-Clex support. WebRTC under the hood, signaling at <code>wss://signal.clex.in</code>.</p>
    </td>
    <td width="50%" valign="top">
      <h3>🗝 Vault</h3>
      <p>Notes, secret-share, cloud-share, settings persistence — feature parity with the web Vault, with Drive flows handled native-side.</p>
    </td>
  </tr>
  <tr>
    <td width="50%" valign="top">
      <h3>🔗 Chain feed</h3>
      <p>Chain feed and detail loading powered by the public transfer-chain worker.</p>
    </td>
    <td width="50%" valign="top">
      <h3>📲 In-app extras</h3>
      <p>Help, Privacy, Changelog screens; deep-link handling for receive and Vault secret flows; launcher, splash, and release packaging.</p>
    </td>
  </tr>
</table>

---

## ✦ Version Timeline

| | |
|---|---|
| `1.9.12` | GitHub Actions CI (compile + test + lint + assemble), JVM unit-test scaffolding, BuildConfig extraction for signaling URL + BLE manufacturer ID |
| `1.9.11` | Vault tab Crossfade → AnimatedContent · tab + success/failure haptics for Secret + Drive flows |
| `1.9.10` | Workspace tab Crossfade → AnimatedContent · full haptic feedback pass |
| `1.9.9` | R8 minification + resource shrinking · SplashScreen API on cold start · WebRTC + theme pre-warm in `Application.onCreate` |
| `1.9.8` | Clex Link scan-only follow-up: keep `canAdvertise=false` on runtime advertise failure |
| `1.9.7` | Clex Link scan-only state persistence + workspace tab Crossfade race fix |
| `1.9.6` | Clex Link invite / accept MTU fix — invites and accept replies no longer truncated |
| `1.9.5` | Clex Link permission + chain parity pass |

Full notes: [`RELEASES.md`](RELEASES.md) · Backend integration: [`INTEGRATION.md`](INTEGRATION.md).

---

## ✦ Tech Stack

<p>
  <img src="https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" />
  <img src="https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white" />
  <img src="https://img.shields.io/badge/Gradle%20Kotlin%20DSL-02303A?style=for-the-badge&logo=gradle&logoColor=white" />
  <img src="https://img.shields.io/badge/R8-3DDC84?style=for-the-badge&logo=android&logoColor=white" />
  <br/>
  <img src="https://img.shields.io/badge/WebRTC-333333?style=for-the-badge&logo=webrtc&logoColor=white" />
  <img src="https://img.shields.io/badge/BLE-000000?style=for-the-badge&logo=bluetooth&logoColor=white" />
  <img src="https://img.shields.io/badge/Google%20Drive-4285F4?style=for-the-badge&logo=googledrive&logoColor=white" />
  <img src="https://img.shields.io/badge/JUnit-25A162?style=for-the-badge&logo=junit5&logoColor=white" />
  <img src="https://img.shields.io/badge/GitHub%20Actions-2088FF?style=for-the-badge&logo=githubactions&logoColor=white" />
</p>

---

## ✦ Open in Android Studio

Open the **whole** folder:

```text
clex-android
```

Do **not** open just `app/`.

---

## ✦ Install Locally

Use the universal APK from the latest GitHub Release, or build it yourself:

```bash
./gradlew :app:assembleRelease

# install on a connected device
adb install -r app/build/outputs/apk/release/app-universal-release.apk
```

If Android blocks the install, enable installation from unknown sources for the app you used to open the APK.

---

## ✦ Build From Source

```bash
# Debug APK
./gradlew :app:assembleDebug

# Release outputs
./gradlew :app:assembleRelease :app:bundleRelease
```

---

## ✦ Distribution

| Use case | Artifact |
| --- | --- |
| Direct install / share | `Clex-1.9.12-universal.apk` |
| Play Store upload | `Clex-1.9.12.aab` |

See [RELEASES.md](RELEASES.md) for artifact names and signing notes.

### Signing

If `key.properties` is missing, release builds fall back to debug signing so the build still completes for local testing.

For Play Store publishing, create a real upload keystore and add:

- `key.properties`
- your `.jks` or `.keystore` file

Use [`key.properties.example`](key.properties.example) as the template.

---

## ✦ CI

<a href="https://github.com/Abhinavv-007/clex-android/actions/workflows/ci.yml">
  <img src="https://github.com/Abhinavv-007/clex-android/actions/workflows/ci.yml/badge.svg" alt="Android CI" />
</a>

The workflow at [`.github/workflows/ci.yml`](.github/workflows/ci.yml) runs on every PR and `main` push:

1. Compile debug Kotlin
2. Run JVM unit tests
3. Run `lintDebug` against a checked-in baseline
4. Assemble debug + R8-minified release APKs
5. Upload artifacts with 14-day retention

---

## ✦ Project Layout

```text
clex-android/
├── app/
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/clex/android/
│       └── res/
├── gradle/                 # wrapper
├── build.gradle.kts        # root build
├── settings.gradle.kts
├── gradle.properties
├── INTEGRATION.md          # backend service notes
├── RELEASES.md             # version log
├── key.properties.example  # signing template
└── .github/workflows/ci.yml
```

---

## ✦ Runtime Service Dependencies

This app talks to deployed backend services at runtime — they are **not** vendored into this repository.

| Service | URL |
| --- | --- |
| Clex web + APIs | `https://clex.in` |
| Vault API | `https://clex.in/vault/api` |
| Vault secret | `https://clex.in/vault/secret` |
| WebRTC signaling | `wss://signal.clex.in` |
| Cloud share | Google Drive API |

See [INTEGRATION.md](INTEGRATION.md).

---

## ✦ Star History

<a href="https://star-history.com/#Abhinavv-007/clex-android&Date">
  <img src="https://api.star-history.com/svg?repos=Abhinavv-007/clex-android&type=Date" alt="Star history" width="100%" />
</a>

---

<div align="center">
  <sub>🤖 Built by <a href="https://abhnv.in"><b>Abhinav Raj</b></a> · siblings: <a href="https://github.com/Abhinavv-007/clex">clex</a> · <a href="https://github.com/Abhinavv-007/clex-ios">clex-ios</a> · <a href="https://github.com/Abhinavv-007/clex-ai">clex-ai</a>.</sub>
  <br/>
  <a href="https://abhnv.in">Portfolio</a> · <a href="https://www.linkedin.com/in/abhnv07/">LinkedIn</a> · <a href="https://x.com/Abhnv007">X</a> · <a href="https://www.instagram.com/abhinavv.007/">Instagram</a>
</div>
