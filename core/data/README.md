# `:core:data`

> 数据层：Repository、Room、DataStore、Chat 流处理器。**feature 模块应只依赖这层**，不直接依赖 `:core:network`。

---

## 职责

- 把 `HermesService`（Retrofit）包装为 `Flow<HermesResponse<T>>`。
- 维护本地持久化：DataStore Preferences + Room。
- 处理流式聊天事件（按协议分发到不同 Handler）。
- 提供 IO Dispatcher 等基础设施。

## 依赖

- `:core:network`、`:core:model`、`:core:utils`
- Room、DataStore、Hilt、kotlinx-serialization、kotlinx-coroutines

## 关键文件

```
core/data/
├── HermesRequests.kt              # 直接用 OkHttp 构造 SSE Request（chat completions / runs events）
├── LocalDataStore.kt              # DataStore Preferences 封装（主题、协议、服务器配置、sessionId/runId）
├── chat/
│   ├── ChatPresenter.kt           # 接口：两种协议的统一抽象
│   ├── ChatCompletionsHandler.kt  # CHAT_COMPLETIONS 协议处理器
│   └── ChatRunsHandler.kt         # RUN 协议处理器
├── db/
│   ├── HauiDatabase.kt            # Room 数据库（version 3）
│   ├── HauiTypeConverters.kt      # 枚举/时间戳转换（带容错）
│   ├── ChatMessageMapper.kt       # Entity ↔ Domain 映射
│   ├── dao/ChatMessageDao.kt      # DAO（含 upsertAllWithTimestampCheck）
│   └── entity/ChatMessageEntity.kt
├── di/
│   ├── DataModule.kt              # @Binds Repository / Sink 等
│   ├── DatabaseModule.kt          # 提供 Room 数据库与 DAO
│   └── DispatchersModule.kt       # @IoDispatcher
└── repository/
    ├── HermesRepository.kt        # 网络接口包装 + isConfigured 状态
    └── ChatHistoryRepository.kt   # 会话持久化（过滤 TOOL 消息）
```

## 关键约定

### Room

- 当前 **版本 3**（基线 schema 已导出至 `schemas/ai.tnj.haui.core.data.db.HauiDatabase/3.json`）。
- 升级版本必须配套写 Migration（见 `db/migrations/HauiMigrations.kt` 模板）；
  `DatabaseModule` 仅对历史 v1/v2 与降级做 destructive fallback，**升级缺 Migration 会启动崩溃**。
- `ChatMessageEntity` 复合索引 `(sessionId, createdAt)`。
- 批量写入走 `upsertAllWithTimestampCheck` —— 单事务 + IN 子句查 `createdAt`，保留首次创建时间。
- `MessageRole.TOOL` 在 `ChatHistoryRepository.upsertAll` 中过滤掉。

### Chat Handlers

- `ChatCompletionsHandler` 解析 `choices[].delta`；严格匹配 `[DONE]`；显式处理 `SseEvent.Closed`。
- `ChatRunsHandler` 解析 `HermesChatMessage`，按 `data.event` 分发到 `onDelta` / `onTool` / `onUsage` / `onMessageEnd`。
- 日志使用 `LogUtil` lambda 形式。

### Repository

- `HermesRepository.updateBaseUrl(host, port, apiKey)` 是 **唯一** 修改 endpoint 的入口；调用后 `isConfigured: StateFlow<Boolean>` 会反映新状态。
- `ChatHistoryRepository` 所有方法都在 `@IoDispatcher` 上执行。

## 使用约定

- **不要** 在此模块直接接触 Compose / Context（除 `LocalDataStore` 需要 `@ApplicationContext`）。
- 新加 Repository：用 `@Singleton class @Inject constructor(...)`。
- DataStore 新键：在 `LocalDataStore` 集中声明 `Preferences.Key<*>`，外部只看到方法签名。
- 流式接口：返回 `Flow<HermesResponse<T>>` 或 `Flow<SseEvent>`，由上层决定订阅生命周期。

## 测试

```bash
./gradlew :core:data:test
```
