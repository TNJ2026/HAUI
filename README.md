# HAUI · Hermes Android UI

> Android client for the Hermes AI server. Built with Jetpack Compose and a multi-module MVVM architecture, it provides chat, workflow orchestration (Runs), health monitoring, and scheduled task (Jobs) management for Hermes.

[![Android CI](https://github.com/TNJ2026/HAUI/actions/workflows/android.yml/badge.svg)](.github/workflows/android.yml)

---

## ✨ Features at a Glance

- **AGENT Tab** — Connection configuration, health checks (30s throttling), and model list.
- **CHAT Tab** — Dual-protocol chat:
  - `CHAT_COMPLETIONS`: `POST v1/chat/completions` (SSE stream).
  - `RUN`: `POST v1/runs` + `GET v1/runs/{id}/events` (SSE stream).
- **SETTINGS Tab** — Theme switching, protocol switching, tool bubble toggle, and Hermes Jobs management.
- Retro terminal aesthetics (CRT raster background, Monospace fonts, square progress indicators).
- Room-based chat history + Markdown rendering (with custom GFM table implementation).

For a detailed feature list, see [`docs/FEATURES.md`](docs/FEATURES.md).

---

## 🧱 Tech Stack

| Category | Technology |
| --- | --- |
| Language / JDK | Kotlin 2.3 / JDK 17 |
| UI | Jetpack Compose + Material 3 (Managed via Compose BOM) |
| Architecture | Multi-module MVVM, StateFlow as Single Source of Truth |
| DI | Hilt 2.59 |
| Networking | Retrofit 3 + OkHttp 5 + okhttp-sse + kotlinx.serialization |
| Concurrency | Kotlin Coroutines + Flow |
| Storage | DataStore Preferences + Room 2.8 |
| Markdown | mikepenz/multiplatform-markdown-renderer + Custom GFM Table |
| Image Loading | Coil 3 |
| Testing | Kotest 6 + Turbine + MockWebServer |

Full version table: [`gradle/libs.versions.toml`](gradle/libs.versions.toml).

---

## 📦 Module Structure

```
HAUI
├── app                       # Application entry, Navigation, SplashScreen
├── core
│   ├── model                 # Shared data models for Network / DB (kotlinx-serialization)
│   ├── network               # Retrofit, OkHttp, SSE Client
│   ├── data                  # Repository, Room, DataStore, Chat Stream Processors
│   ├── ui                    # Reusable Compose components (CommonUi, CRT backgrounds)
│   ├── designsystem          # Themes, Palettes, Typography, ThemeController
│   └── utils                 # Utils for Logs, Attachments, JpegSizeLimiter, etc.
└── feature
    └── home                  # Home screen tabs: Agent / Chat / Settings
```

Module Dependency Graph:

```
app  ──▶  feature:home  ──▶  core:designsystem
                          ──▶  core:ui          ──▶  core:designsystem
                          ──▶  core:data        ──▶  core:network ──▶ core:model
                                                                  ──▶ core:utils
                          ──▶  core:model
                          ──▶  core:utils
```

For module-specific details, refer to the `README.md` in each directory.

---

## 🚀 Getting Started

### Prerequisites

- JDK 17
- Android SDK: `compileSdk = 37`, `minSdk = 31`, `targetSdk = 36`
- Android Studio Ladybug+ (Recommended) or IntelliJ IDEA 2025.1+
- Hermes backend service (for live debugging)

### Clone and Build

```bash
git clone https://github.com/TNJ2026/HAUI.git
cd HAUI

# Configure SDK path (First time only)
cp local.properties.example local.properties
# Then edit local.properties and set sdk.dir

# Build Debug APK
./gradlew :app:assembleDebug

# Build Release APK (Requires local signing config, see docs/RELEASE.md)
./gradlew :app:assembleRelease
```

### Install to Device

```bash
./gradlew :app:installDebug
```

### Running Tests

```bash
./gradlew test            # All unit tests (JUnit Platform + Kotest)
./gradlew :core:network:test
```

Detailed build instructions: [`docs/BUILD.md`](docs/BUILD.md).

---

## 🔌 Interfacing with Hermes Server

Launch the app and go to **AGENT Tab → Click CONNECT**, then enter:

| Field | Default | Description |
| --- | --- | --- |
| host | (None) | Hermes service address (must be a valid IPv4) |
| port | `8642` | Service port |
| apiKey | (Optional) | Bearer token |

Connections are automatically persisted to DataStore and restored on the next launch.

> ⚠️ `usesCleartextTraffic="true"` is set in `AndroidManifest.xml` to support local plaintext connections. Switch to HTTPS or manage via a user toggle before production release.

API Contract: [`docs/API.md`](docs/API.md).

---

## 📚 Documentation Index

| Document | Purpose |
| --- | --- |
| [`docs/FEATURES.md`](docs/FEATURES.md) | Full feature list and implementation notes |
| [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) | Dependency graph, SSE data flows, state machines |
| [`docs/API.md`](docs/API.md) | Hermes server API contract |
| [`docs/BUILD.md`](docs/BUILD.md) | Build commands and environment matrix |
| [`docs/RELEASE.md`](docs/RELEASE.md) | Signing and CI release workflow |
| [`CONTRIBUTING.md`](CONTRIBUTING.md) | Contributions, coding style, PR process |
| [`CHANGELOG.md`](CHANGELOG.md) | Version history |
| [`CLAUDE.md`](CLAUDE.md) | Project guidelines for AI collaborators/new engineers |

---

## 🗺️ Known Limitations / Roadmap

- [ ] Add build flavors (`dev` / `prod`) for environment isolation.
- [ ] HTTPS support and user-level toggle for `usesCleartextTraffic`.
- [ ] Expand Unit / UI test coverage.
- [ ] Improved fault tolerance and fallback displays for server fields (Token usage, Tool progress, Job status).

---

## 📝 License

This project is licensed under the [MIT License](LICENSE).
