# 架构文档

> 本文档说明 HAUI 的整体架构、模块依赖、数据流与关键状态机。
> 配合 [`FEATURES.md`](FEATURES.md)（功能视角）与 [`API.md`](API.md)（接口契约）一起阅读。

---

## 1. 总览

```
┌─────────────────────────────────────────────────────────────────┐
│                          UI Layer                                │
│  Jetpack Compose · Navigation · Material 3                       │
│  feature:home（AgentTab / ChatTab / SettingsTab）                │
└──────────────────────────┬──────────────────────────────────────┘
                           │ StateFlow / 事件
┌──────────────────────────┴──────────────────────────────────────┐
│                         ViewModel Layer                          │
│  AgentViewModel · ChatViewModel · SettingsViewModel              │
│  ChatPresenter（接口） · InternalChatState（数据类）             │
└──────────────────────────┬──────────────────────────────────────┘
                           │ Flow<HermesResponse<T>>
┌──────────────────────────┴──────────────────────────────────────┐
│                       Data Layer (core:data)                     │
│  HermesRepository · ChatHistoryRepository                        │
│  ChatCompletionsHandler / ChatRunsHandler                        │
│  LocalDataStore（Preferences） · HauiDatabase（Room）            │
└──────────────────────────┬──────────────────────────────────────┘
                           │ Retrofit / OkHttp / SSE
┌──────────────────────────┴──────────────────────────────────────┐
│                    Network Layer (core:network)                  │
│  HermesService · NetworkManager · SafeHermesCall                 │
│  HermesRequestInterceptor（动态 BaseURL）                        │
└─────────────────────────────────────────────────────────────────┘
                           │
                           ▼
                  Hermes Backend (HTTP / SSE)
```

---

## 2. 模块依赖图

```
       ┌──────────────────────┐
       │        :app          │   入口、Navigation、Splash
       └──────────┬───────────┘
                  │
                  ▼
       ┌──────────────────────┐
       │   :feature:home      │   AgentTab / ChatTab / SettingsTab
       └─┬──┬──┬──┬──┬────────┘
         │  │  │  │  │
         │  │  │  │  └─▶ :core:utils         （日志 / 附件）
         │  │  │  └────▶ :core:designsystem  （主题）
         │  │  └───────▶ :core:ui            （CommonUi / CRT 背景）
         │  └──────────▶ :core:model         （数据模型）
         └─────────────▶ :core:data          ─┐
                                              │
                          ┌───────────────────┘
                          ▼
                  ┌──────────────────────┐
                  │     :core:data       │
                  └─┬────┬───────────────┘
                    │    │
                    │    └─▶ :core:network ─┐
                    │                       │
                    │                       ▼
                    │                ┌────────────────┐
                    │                │  :core:model   │
                    │                │  :core:utils   │
                    │                └────────────────┘
                    └─▶ :core:model
                    └─▶ :core:utils
```

**关键原则**：

- `feature:*` 只依赖 `core:*`，不依赖其它 `feature:*`。
- `core:data` 是 Repository 边界，`feature:*` **不直接** 依赖 `core:network`（保留例外：`feature:home` 当前对 OkHttp 有轻量直引，用于 Coil 图片加载与附件构造）。
- `core:network` 只对外暴露 `HermesService`（Retrofit 接口）与 `NetworkManager`。
- `core:model` 是叶子模块，纯数据类，不依赖任何业务模块。
- `core:designsystem` 与 `core:ui` 分离：前者管"色板字体"，后者管"组件"。

---

## 3. 应用启动流程

```
HAUIApplication.onCreate()
    │
    ├─ runBlocking { localDataStore.initialIsDarkTheme() }   ← 同步预读主题
    │     │
    │     └─▶ ThemeController.setDark(...)                    ← 提前写入
    │
    └─ Hilt 容器初始化（@HiltAndroidApp）

MainActivity.onCreate()
    │
    └─ setContent {
           HAUITheme {                                        ← 订阅 ThemeController
               AppNavigation()
                   ├─ SPLASH → SplashScreen (3s)
                   └─ HOME   → HomeScreen
                                  ├─ AgentTab
                                  ├─ ChatTab
                                  └─ SettingsTab
           }
       }
```

**关键点**：`initialIsDarkTheme()` 同步读取 DataStore 一次，避免 Compose 首帧时主题异步还没就绪导致的颜色闪烁。

---

## 4. CHAT 数据流（核心）

### 4.1 用户发送消息

```
User 输入 → ChatComposer.onSend
    │
    ▼
ChatViewModel.sendMessage(text, attachments)
    │
    ├─ streamJob?.cancel()                       ← 显式取消上一次流
    │
    ├─ _state.update { messages += UserMsg }
    │
    ├─ swap ChatPresenter（如果协议变化）
    │     ┌──────────────────────────────────────┐
    │     │ CHAT_COMPLETIONS → ChatCompletionsHandler │
    │     │ RUN              → ChatRunsHandler        │
    │     └──────────────────────────────────────┘
    │
    └─ streamJob = viewModelScope.launch {
           presenter.send(...)
               .collect { event ->
                   when (event) {
                       Open    → 添加打字指示器
                       Message → onDelta / onTool / onUsage 回调
                       Closed  → onMessageEnd（持久化 + 关闭流）
                       Failure → onError
                   }
               }
       }
```

### 4.2 SSE 事件序列（CHAT_COMPLETIONS）

```
Client ──POST v1/chat/completions──▶ Hermes
       ◀──SseEvent.Open──            （建立连接）
       ◀──data: {"choices":[{"delta":{"content":"..."}}]}──  （delta 增量）
       ◀──data: {"choices":[{"delta":{"content":"..."}}]}──
       ◀──data: {"choices":[{"delta":{"content":"..."}}]}──
       ◀──data: {"usage":{...}}──     （usage 块）
       ◀──data: [DONE]──              （严格匹配，结束哨兵）
       ◀──SseEvent.Closed──           （服务端断连）
```

### 4.3 SSE 事件序列（RUN）

```
Client ──POST v1/runs──▶ Hermes
       ◀──200 {"id": "run_xxx"}──
       │
       ▼
Client ──GET v1/runs/run_xxx/events──▶ Hermes
       ◀──SseEvent.Open──
       ◀──data: {"type":"message.delta","content":"..."}──
       ◀──data: {"type":"tool.call","name":"...","args":{...}}──
       ◀──data: {"type":"message.delta","content":"..."}──
       ◀──data: {"type":"run.completed"}──
       ◀──data: [DONE]──
       ◀──SseEvent.Closed──
```

### 4.4 状态合并：`InternalChatState`

```kotlin
data class InternalChatState(
    val messages: List<ChatMessage>,
    val hasPendingRun: Boolean,
    val healthOk: Boolean,
    val showTypingIndicator: Boolean,
    val tokenUsage: Int,
)

private val _state = MutableStateFlow(InternalChatState.Initial)
val uiState: StateFlow<ChatUiState> = _state.map { it.toUiData() }.stateIn(...)
```

**为什么不拆分**：5 个独立 `MutableStateFlow` 会让 UI 在一次业务动作里被触发多次重组（消息 + 打字 + 健康），合并后是单次原子更新，UI 抖动消失。

---

## 5. 双协议状态机

```
                  ┌───────────────────┐
                  │ Settings 切换协议  │
                  └─────────┬─────────┘
                            │
                            ▼
            ┌──────────────────────────────┐
   ┌────────│  ChatViewModel.swapPresenter  │────────┐
   │        └──────────────────────────────┘        │
   │                                                 │
   ▼                                                 ▼
┌──────────────────────┐                ┌──────────────────────┐
│ ChatCompletionsHandler│                │   ChatRunsHandler    │
│   sessionId: api_xxx  │                │      runId: run_xxx  │
└──────────────────────┘                └──────────────────────┘

恢复历史会话时：
    sessionId.startsWith("run_") → RUN
    otherwise                    → CHAT_COMPLETIONS
```

**关键约束**：

1. 协议切换会 `reset()` 当前消息列表（除非是恢复历史触发的首次切换）。
2. `sessionId` / `runId` 各自独立持久化到 DataStore。
3. `newChat()` 会清空 sessionId 但 **不会** 清空 runId（runId 由服务端在新 RUN 时分配）。

---

## 6. Room 数据流

```
ChatViewModel.persistCurrentConversation()
    │
    └─▶ ChatHistoryRepository.upsertAll(sessionId, messages)
            │
            ├─ filter { it.role != TOOL }      ← 过滤 TOOL 消息
            │
            └─▶ ChatMessageDao.upsertAllWithTimestampCheck(entities)
                    │
                    └─▶ @Transaction
                            ├─ existingTimestamps = SELECT createdAt
                            │                       FROM messages
                            │                       WHERE id IN (?, ?, ...)
                            │
                            ├─ for each entity: createdAt =
                            │       existing[entity.id] ?: entity.createdAt
                            │
                            └─ @Upsert (batch)
```

**为什么是这套设计**：

- **TOOL 过滤** 在 repository 层而不是 ViewModel 层 → ViewModel 只关心"持久化整个会话"，不关心过滤规则。
- **单事务 + IN 子句** → N 条消息 1 次往返；逐条 upsert 是 N 次。
- **`createdAt` 保留** → 同一条消息多次写入（因为流式增量），不能覆盖第一次的时间戳。

---

## 7. 网络层关键设计

### 7.1 动态 BaseURL

```
HermesEndpoint.BASE_URL = "http://placeholder.local/"   ← Retrofit 注解占位

请求经过 HermesRequestInterceptor：
    if (host != null && port != null) {
        request.url = request.url.newBuilder()
            .host(host)
            .port(port)
            .scheme("http")
            .build()
    }
```

**为什么不直接 `Retrofit.Builder().baseUrl(...)`**：服务器地址在运行时由用户输入，Retrofit 一旦构建无法修改 baseUrl。通过 Interceptor 重写让 `HermesService` 实例可以全局复用。

### 7.2 双 OkHttpClient

| 用途 | 配置 |
| --- | --- |
| 普通 REST | `readTimeout(60s)` + 拦截器 |
| SSE | `readTimeout(0)`（无超时）+ 拦截器 |

**为什么分两个**：SSE 是长连接，复用普通 client 会被 60s 空闲断开。

### 7.3 错误码

```
ERROR_CODE_PARSE_JSON = 10001   ← kotlinx.serialization 解析失败
ERROR_CODE_NETWORK    = 10002   ← IOException / SocketTimeout
ERROR_CODE_UNKNOWN    = 10003   ← 其它
```

业务错误用服务端返回的 HTTP code（401 / 5xx 等）。

---

## 8. 关键设计决策（FAQ）

### Q: 为什么把 `streamJob` 设为 `var` 而不是用 `MutableStateFlow<Job?>`？

A: Job 不需要被 UI 订阅，且生命周期完全由 ViewModel 内部控制。用 `var` 显式 cancel 旧任务比订阅模型更直观。

### Q: 为什么 Markdown 表格不用 mikepenz 默认实现？

A: 默认实现在窄屏上会撑破布局或字号缩到不可读。自定义实现固定 160dp 列宽 + 横向滚动 + `drawBehind` 网格线，更适配终端风格。其它块（标题、代码、列表、引用）继续用 mikepenz。

### Q: 为什么 TOOL 消息不入库但要保留 in-memory？

A: TOOL 消息是 UX 节奏的一部分（"正在调用工具…"），但对回放历史无意义。运行时保留可让用户看到工具调用的过程，历史只看最终对话即可。

### Q: 为什么用 `runBlocking` 读主题？

A: 主题必须在 `setContent` 之前确定，否则 Compose 首帧会用默认值渲染再切换，产生肉眼可见的闪烁。DataStore 是异步的，启动期同步读取一次是可接受的权衡（< 10ms）。

### Q: 为什么 ChatPresenter 用接口而不是密封类？

A: 两个 Handler 内部状态差异较大（一个跟 sessionId，一个跟 runId + currentToolMsgId），用接口让它们各自维护自己的状态；密封类强制共享字段反而别扭。

---

## 9. 演进路线

| 维度 | 当前 | 下一步 |
| --- | --- | --- |
| Room Migration | v3 基线已导出 schema，`HauiMigrations.ALL` 框架就绪 | 随版本升级填入真实 Migration + 添加 `MigrationTest` |
| Build Flavors | 无 | 重新引入 `dev` / `prod` 隔离环境 |
| 签名 | Release 用 debug 签名 | 配置正式 keystore（详见 `RELEASE.md`） |
| HTTPS | `usesCleartextTraffic="true"` | 用户级开关 |
| 测试覆盖 | 仅依赖配置 | 为 Repository / Handler 补单测 |
