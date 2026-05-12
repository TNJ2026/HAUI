# HAUI

## 项目简介
HAUI 是一款使用现代 Android 开发技术构建的移动应用。项目采用模块化架构、Jetpack Compose、Hilt 依赖注入、Kotlin 协程与 Flow 以及 MVVM 架构进行开发。

## 架构和技术栈
*   **架构:** 模块化设计 (`app`, `core:data`, `core:designsystem`, `core:model`, `core:network`, `core:ui`, `core:utils`, `feature:home`)，MVVM 设计模式。
*   **UI:** Jetpack Compose, Material Design 3 规范。
*   **响应式编程:** Kotlin Coroutines (协程), Kotlin Flow。
*   **依赖注入:** Dagger Hilt。
*   **网络通信:** OkHttp (支持常规 HTTP 请求与 Server-Sent Events / SSE)。
*   **本地存储:** DataStore (Preferences DataStore)。

## 模块介绍
*   `:app` - 应用入口，包含 MainActivity, Navigation 等。
*   `:core:network` - 网络请求管理模块。提供 `NetworkManager`，支持标准的 REST API 和基于 `EventSource` 的 SSE 实时流。
*   `:core:data` - 数据层，如 `SecurePrefs` 等负责本地数据或仓库类的封装。
*   `:core:model` - 应用核心数据模型。
*   `:core:designsystem` - 应用的主题和共享的 Compose UI 样式。
*   `:core:ui` - 可复用的 UI 组件（如 `LoadingOverlay` 等）。
*   `:core:utils` - 通用工具类。
*   `:feature:home` - 首页功能模块。

## 功能说明
### 网络管理器 (NetworkManager)
位于 `core:network` 模块下的 `ai.tnj.haui.core.network.NetworkManager` 类，封装 SSE 长连接能力。常规 HTTP 请求请通过 `HermesService` (Retrofit) 调用。

*   **SSE 请求 (`connectSse`)**
    *   **用途:** 与服务器建立 Server-Sent Events 连接，以流(Flow)的形式持续接收服务端推送的事件。
    *   **使用方法:** 传入构建好的 `okhttp3.Request`。
    *   **返回值:** `Flow<SseEvent>`，通过收集 (collect) 可以接收 `Open`, `Message`, `Closed`, `Failure` 等网络事件状态。SSE 内部使用单独的 `OkHttpClient`（无读超时），不会被 60 秒空闲限制砍掉。

### 日志工具 (LogUtil)
位于 `core:utils` 模块下的 `ai.tnj.haui.core.utils.LogUtil` 单例。
*   **用途:** 安全地打印日志，且只在应用的 Debug 模式下输出，防止生产环境泄漏敏感信息。
*   **使用方法:** `LogUtil.d("TAG", "Message")`、`LogUtil.e("TAG", "Error message", exception)` 等等。
*   **返回值:** 无。

## 待办与优化
- [ ] 扩展 Room 数据库支持以应对复杂关系型本地数据缓存。
- [ ] 针对 `NetworkManager` 添加常见的拦截器（如 Header 注入、日志打印等）。
- [ ] 添加更多单元测试和 UI 测试。