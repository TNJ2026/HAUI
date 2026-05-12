# HAUI

## Project Overview
HAUI is a mobile application built with modern Android development technologies. The project follows a modular architecture, using Jetpack Compose, Hilt dependency injection, Kotlin Coroutines and Flow, and the MVVM architecture pattern.

## Architecture and Tech Stack
*   **Architecture:** Modular design (`app`, `core:data`, `core:designsystem`, `core:model`, `core:network`, `core:ui`, `core:utils`, `feature:home`), MVVM design pattern.
*   **UI:** Jetpack Compose, Material Design 3.
*   **Reactive Programming:** Kotlin Coroutines, Kotlin Flow.
*   **Dependency Injection:** Dagger Hilt.
*   **Networking:** OkHttp (supports standard HTTP requests and Server-Sent Events / SSE).
*   **Local Storage:** DataStore (Preferences DataStore).

## Module Overview
*   `:app` - Application entry point, contains MainActivity, Navigation, etc.
*   `:core:network` - Network request management module. Provides `NetworkManager`, supports standard REST API and `EventSource`-based SSE real-time streaming.
*   `:core:data` - Data layer, such as `SecurePrefs` for local data or repository encapsulation.
*   `:core:model` - Core application data models.
*   `:core:designsystem` - App theme and shared Compose UI styles.
*   `:core:ui` - Reusable UI components (e.g., `LoadingOverlay`).
*   `:core:utils` - Utility classes.
*   `:feature:home` - Home feature module.

## Features
### NetworkManager
Located in `ai.tnj.haui.core.network.NetworkManager` under the `core:network` module. Encapsulates SSE long-lived connections. For standard HTTP requests, use `HermesService` (Retrofit).

*   **SSE Requests (`connectSse`)**
    *   **Purpose:** Establish a Server-Sent Events connection with the server to continuously receive server-pushed events as a `Flow`.
    *   **Usage:** Pass a constructed `okhttp3.Request`.
    *   **Return:** `Flow<SseEvent>`. By collecting, you can receive network event states such as `Open`, `Message`, `Closed`, `Failure`. SSE uses a separate `OkHttpClient` (no read timeout), so it won't be killed by the 60-second idle limit.

### LogUtil
Singleton located in `ai.tnj.haui.core.utils.LogUtil` under the `core:utils` module.
*   **Purpose:** Safely print logs, only outputting in Debug mode to prevent sensitive information leaks in production.
*   **Usage:** `LogUtil.d("TAG", "Message")`, `LogUtil.e("TAG", "Error message", exception)`, etc.
*   **Return:** None.

## TODO and Optimizations
- [ ] Add Room database support for complex relational local data caching.
- [ ] Add common interceptors to `NetworkManager` (e.g., header injection, log printing).
- [ ] Add more unit tests and UI tests.
