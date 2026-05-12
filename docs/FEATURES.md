# HAUI 功能说明

> HAUI（Hermes Android UI）是 Hermes AI 服务端的 Android 客户端，采用 Jetpack Compose + 多模块 MVVM 架构，提供对 Hermes 服务的对话、运行编排、任务管理与连接状态可视化。

---

## 1. 项目概览

| 项 | 说明 |
| --- | --- |
| applicationId | `ai.tnj.haui`（dev 风味追加 `.dev` 后缀） |
| 命名空间 | `ai.tnj.haui` |
| 构建变体 | `dev` / `prod` × `debug` / `release` |
| 最低/编译/目标 SDK | 由 `libs.versions.toml` 集中管理 |
| UI 框架 | Jetpack Compose + Material 3 |
| DI | Hilt |
| 本地存储 | DataStore Preferences + Room |
| 网络 | Retrofit + OkHttp + okhttp-sse + kotlinx.serialization |
| 异步 | Kotlin Coroutines + Flow / StateFlow |
| Markdown | mikepenz/multiplatform-markdown-renderer + intellij-markdown AST |

---

## 2. 模块划分

```
HAUI
├── app                      // 应用入口、导航、闪屏
├── core
│   ├── model                // 网络与 DB 共用数据模型
│   ├── network              // Retrofit/OkHttp/SSE 客户端封装
│   ├── data                 // 仓库层、Room、DataStore、Chat 流处理器
│   ├── ui                   // 可复用 Compose 组件、复古终端栅格背景
│   ├── designsystem         // 主题、色板、字体、ThemeController
│   └── utils                // 日志、文件附件辅助
└── feature
    └── home                 // 首页三 Tab：Agent / Chat / Settings
```

各模块单一职责，`feature` 仅依赖 `core`，业务边界与编译期解耦清晰。

---

## 3. 应用启动流程

1. **`HAUIApplication`**（`@HiltAndroidApp`）
   - `onCreate` 通过 `runBlocking { localDataStore.initialIsDarkTheme() }` 同步读取持久化主题，提前写入 `ThemeController`，避免 Compose 首帧主题闪烁。
2. **`SplashScreen`**
   - 80dp Hermes 标识 + 复古终端栅格背景（`retroTerminalBackground`）。
   - 展示 3 秒后自动跳转 `HomeScreen`。
3. **`HomeScreen`**
   - `Scaffold + 底部 Tab`：AGENT / CHAT / SETTINGS。
   - 状态通过 `rememberSaveable` 保留，可在配置变更下保持选中项。

---

## 4. AGENT Tab（服务连接与模型）

文件：`feature/home/.../agent/AgentTab.kt`、`AgentViewModel.kt`、`ConnectionSheet.kt`

### 功能
- **连接配置**：通过底部弹出 `ConnectionSheet` 输入 `host` / `port` / `apiKey`。
- **自动重连**：启动时读取上次成功的服务器配置并自动连接（不存在或为空时跳过）。
- **健康检查**：调用 `GET health/detailed` 获取整体状态：
  - `status`、`gatewayState`、`platforms.apiServer.state`、`updatedAt`（自动格式化为 `YYYY/M/D HH:mm:ss`）。
  - `ON_RESUME` 时刷新，30 秒节流避免频繁请求（`HEALTH_CHECK_THROTTLE_MS = 30_000`）。
- **模型列表**：`GET v1/models` 返回的模型信息以列表展示。
- **错误反馈**：401 时根据是否提供 API Key 显示精准提示（“Unauthorized, Please input API key” / “incorrect API key”），其它错误展示 code + msg。

---

## 5. CHAT Tab（双协议对话）

文件：`feature/home/.../chat/*.kt`，核心 ViewModel 为 `ChatViewModel`。

### 5.1 双协议切换
通过设置项可选两种对话协议，由 `ChatViewModel` 在运行时动态切换处理器：

| 协议 | 端点 | 处理器 |
| --- | --- | --- |
| `CHAT_COMPLETIONS` | `POST v1/chat/completions`（SSE 流） | `ChatCompletionsHandler` |
| `RUN` | `POST v1/runs` + `GET v1/runs/{id}/events`（SSE 流） | `ChatRunsHandler` |

切换协议时自动重置会话状态；从历史会话恢复时根据 `sessionId` 前缀（`run_` → RUN，否则 CHAT_COMPLETIONS）自动推断协议，并跳过一次重置以保持加载内容。

### 5.2 流式消息处理
- **SSE 客户端**：`NetworkManager.connectSse` 使用独立 `OkHttpClient`（`readTimeout(0)`），不会被 60 秒空闲断开。
- **事件建模**：`SseEvent.Open / Message / Closed / Failure`。
- **DONE 哨兵**：严格匹配 `[DONE]`（`trim() == "[DONE]"`）作为结束标志。
- **消息分发**：通过 `ChatMessageSink` / `ChatMessageStore` 接口将流式增量回写到 UI 状态。

### 5.3 输入与附件
`ChatComposer` 支持：
- 纯文本消息
- 图片附件（`PendingAttachment` 携带 `uri` + `mimeType`）
- 文档附件（携带 `fileName`）

消息类型由 `ChatMessageType.determine(text, mimeType)` 推断（文本 / 图片 / 文件 / 错误）。

### 5.4 会话状态管理
`ChatViewModel` 将 5 个独立状态合并为 `InternalChatState` 数据类（一个 `MutableStateFlow`）：
- `messages` 消息列表
- `hasPendingRun` 是否有进行中的请求
- `healthOk` 健康状态（决定 UI 提示）
- `showTypingIndicator` 打字指示器
- `tokenUsage` Token 用量（默认上下文容量 204.8k）

`uiState` 由该状态映射成 UI 友好的 `ChatUiState.ChatUIData`。

### 5.5 任务生命周期
- `streamJob` / `healthJob` 显式管理：每次新请求 / 健康检查前 `cancel()` 旧任务，避免并发流冲突。
- `newChat()`：取消当前流并清空消息（消息为空时跳过，避免无意义的状态翻动）。

### 5.6 历史会话
文件：`ChatHistorySheet.kt` + `ChatHistoryRepository`。

- **持久化策略**：仅在流结束（`onMessageEnd`）或出错（`onError`）时整体快照写入 Room，单事务 + IN 子句一次性 upsert，保留已有 `createdAt`。
- **TOOL 消息排除**：工具调用气泡仅作为 UX 提示，不入库（`MessageRole.TOOL` 在 repository 层过滤）。
- **会话列表**：DAO 提供 `getSessionsFlow()`，按 `MAX(createdAt)` 倒序，附带：
  - `messageCount`
  - 首条 USER 消息文本（用于会话标题）
  - 首条 ASSISTANT 消息文本（用于副标题）
- **加载会话**：流式恢复消息，同步切换协议与服务端 sessionId。
- **删除会话**：按 `sessionId` 整体删除。

### 5.7 Markdown 渲染
文件：`ChatMarkdown.kt`

- 基于 mikepenz/multiplatform-markdown-renderer，按 Material 3 主题自适应颜色。
- 自定义 GFM 表格组件：横向滚动 + 160dp 固定单元格 + `drawBehind` 绘制网格。
- 代码块 / 引用 / 列表 / 链接遵循终端复古配色。

### 5.8 工具调用气泡（可选）
- 来自服务端的工具进度（TOOL 角色）默认隐藏，仅在 SETTINGS 中开启 “Show tool bubble” 时显示。
- 隐藏时不刷新打字指示器，保证 UI 节奏。

### 5.9 Token 用量
- 服务端返回的 `totalTokens` 通过 `onUsageUpdate` 回写。
- 进度条以默认上下文容量（204.8k tokens）为分母直观展示。

---

## 6. SETTINGS Tab（偏好、协议、计划任务）

文件：`feature/home/.../settings/SettingsTab.kt`、`SettingsViewModel.kt`、`SettingsModels.kt`

### 6.1 偏好同步
- 单一 `combine(isDarkTheme, chatProtocol, showToolBubble)` 流派生 UI 状态，避免多个独立订阅。
- 写入立即持久化到 DataStore，同时通过 `ThemeController.setDark(...)` 触发主题热切换。

### 6.2 主题切换
- 浅色 / 深色二选一（`ThemeMode.LIGHT / DARK`）。
- `HAUITheme` 中的 `MaterialTheme` 色板及系统栏对比度自动随之更新。
- 复古终端栅格背景颜色由 `MaterialTheme.colorScheme.primary` 决定，主题切换无缝。

### 6.3 对话协议
- 提供 `CHAT_COMPLETIONS` / `RUN` 两种模式（带文案说明）。
- 切换后由 `ChatViewModel` 监听并即时替换 Handler，不重启 ViewModel。

### 6.4 工具气泡开关
- 控制 TOOL 角色消息是否在聊天列表中显示。

### 6.5 Hermes Jobs 计划任务管理
对应 Hermes 服务的 `api/jobs` 系列接口：

| 操作 | 端点 | 处理 |
| --- | --- | --- |
| 列表 | `GET api/jobs` | 自动加载（连接配置成功后），UI 维护 loading / error |
| 暂停 / 恢复 | `POST api/jobs/{id}/pause` `resume` | 乐观更新本地状态，无需重新拉列表 |
| 立即运行 | `POST api/jobs/{id}/run` | 成功后自动刷新列表 |
| 删除 | `DELETE api/jobs/{id}` | 成功后自动刷新列表 |

任务状态映射（`parseJobStatus`）：
- `FAILED / RUNNING / SCHEDULED / QUEUED / COMPLETED / PAUSED`
- 优先取 `state`，回退到 `lastStatus`，结合 `enabled` 标志判定。

时间字段（`lastRunAt / nextRunAt / scheduleDisplay`）格式化为本地友好形式。

所有操作通过单一通道 `messages: Flow<String>` 推送 toast / snack 提示。

---

## 7. 主题与设计系统

文件：`core/designsystem/*`

- **`ThemeController`**：`object` 单例 + `MutableStateFlow<Boolean>`，全局可订阅暗黑模式。
- **`HAUITheme`**：Material 3 ColorScheme + 自定义字体（`HAUITypography`）+ 系统栏图标对比度。
- **`retroTerminalBackground`**（`core/ui`）：基于 `Modifier.drawBehind` 绘制扫描线/网格，渲染复古 CRT 终端氛围。

---

## 8. 数据层（`core/data`）

### 8.1 Room
- 数据库：`HauiDatabase`（版本 3），开启 `exportSchema`。
- 实体：`ChatMessageEntity`，复合索引 `(sessionId, createdAt)`。
- `HauiTypeConverters`：枚举解析容错，未知枚举回退到 `USER` 角色，保证版本前向兼容。
- `ChatMessageDao`
  - `@Upsert` 单条 / 批量。
  - `upsertWithTimestampCheck` / `upsertAllWithTimestampCheck`：在单事务内通过 `IN` 子句查找已存在 `createdAt`，避免覆盖原始创建时间。
  - `getSessionsFlow`：派生会话摘要。
  - `getBySessionFlow`：按时间升序流式返回会话内消息。

### 8.2 仓库
- **`HermesRepository`**：对 Retrofit 接口包装为 `Flow<HermesResponse<T>>`，使用 `hermesFlow { ... }` 折叠 `Loading / Success / Error` 三态。维护 `isConfigured: StateFlow<Boolean>` 与运行时 `updateBaseUrl(host, port, apiKey)`。
- **`ChatHistoryRepository`**：屏蔽 DAO 细节，提供 `getSessions / loadSession / upsert / upsertAll / deleteSession`，统一在 `IoDispatcher` 上执行。

### 8.3 DataStore
- **`LocalDataStore`**（`@Singleton`）：Preferences DataStore 封装。
- 维护键：
  - `IS_DARK_THEME_KEY`（暗黑模式）
  - `CHAT_PROTOCOL_KEY`（对话协议）
  - `SHOW_TOOL_BUBBLE_KEY`（工具气泡）
  - 服务器配置（host / port / apiKey）
- 提供 `initialIsDarkTheme()` 用于启动期同步读取。

### 8.4 Chat 流处理器
- **`ChatPresenter`** 接口：屏蔽不同协议差异。
- **`ChatCompletionsHandler`**：解析 `delta` 增量、严格匹配 `[DONE]`；显式处理 `SseEvent.Closed`。
- **`ChatRunsHandler`**：处理 Run 的多种事件（消息、工具调用、状态变更），维护 `currentMsgId` / `currentToolMsgId`。
- 所有日志通过 `LogUtil` 的 lambda 形式输出，发布版下被丢弃，避免不必要的字符串拼接。

---

## 9. 网络层（`core/network`）

- **`HermesEndpoint`**：使用占位符 BaseURL，运行时由 `HermesRequestInterceptor` 重写为真实 host:port。
- **`HermesService`**：Retrofit 接口，覆盖 health / models / runs / chat completions / jobs。
- **`NetworkManager.connectSse`**：基于 `okhttp-sse` 的 `EventSources`，使用 SSE 专用 `OkHttpClient`（`readTimeout(0)`）。
- **`SafeHermesCall`**：
  - `hermesFlow { service.X() }` 统一封装 try/catch + Loading/Success/Error 转换。
  - 错误码：`ERROR_CODE_PARSE_JSON=10001`、`ERROR_CODE_NETWORK=10002`、`ERROR_CODE_UNKNOWN=10003`。
- **请求头**：`Authorization: Bearer <apiKey>`；POST JSON 自动附加 `Content-Type: application/json; charset=utf-8`；`X-Hermes-Session-Id` 仅在非空时携带。

---

## 10. 工具与基础设施（`core/utils`）

- **`LogUtil`**：Debug 模式下生效的 lazy 日志方法（lambda 入参延迟拼接）。
- **`PendingAttachment`**：图片 / 文件附件的数据载体，跨模块共用。
- **文件类型工具**：根据 MIME 类型推断 `ChatMessageType`（图片 / 文件）。

---

## 11. 构建配置

`app/build.gradle.kts`：

- **JDK 17** 源 / 目标兼容。
- **Compose** 启用（Compose BOM 管理版本）。
- **风味维度** `version`：
  - `dev`：`applicationIdSuffix = ".dev"`、`versionNameSuffix = "-dev"`，可与正式版共存安装。
  - `prod`：发布版风味，无后缀。
- **构建类型**：
  - `release`：开启 `isMinifyEnabled` 与 `isShrinkResources`，使用 `proguard-android-optimize.txt` + 项目级 `proguard-rules.pro`；当前仍使用 `debug` 签名（待替换正式发布签名）。
  - `debug`：关闭混淆，便于排查。

---

## 12. 关键技术亮点

1. **SSE 流稳定性**：独立 `OkHttpClient` + `readTimeout(0)` + 严格 DONE 哨兵 + 显式 Closed 处理。
2. **多协议运行时切换**：单 ViewModel 内 `ChatPresenter` 替换，保留 Handler 实例避免重新分配。
3. **批量写库**：`upsertAllWithTimestampCheck` 单事务 + IN 查询，N 条消息 1 次往返，保留 `createdAt`。
4. **启动期主题预读**：`runBlocking + initialIsDarkTheme()` 消除首帧主题闪烁。
5. **状态合并**：`InternalChatState` 单一 MutableStateFlow，避免多源订阅造成的重组放大。
6. **任务生命周期**：`streamJob` / `healthJob` 显式 `cancel()`，防止并发流互相覆写消息。
7. **Markdown 表格**：GFM 表格自渲染（横向滚动、统一 160dp 列宽、`drawBehind` 网格）。
8. **复古 CRT 终端美学**：栅格背景 + Hermes 标识，统一品牌氛围。

---

## 13. 已知限制 / 后续可演进方向

- Release 仍使用 debug 签名，正式发布前需配置正式签名与版本号策略。
- `usesCleartextTraffic="true"`：为支持局域网 Hermes 服务的明文连接，未来可考虑通过用户级配置切换 HTTPS。
- Room 暂启用 `fallbackToDestructiveMigration`，跨版本升级会清空对话历史；可在稳定版本节点引入正式 Migration。
- 工具气泡 / Token 用量 / 任务状态等仍依赖服务端字段；可考虑前端做更强的容错与回退展示。
