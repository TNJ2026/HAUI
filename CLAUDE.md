# CLAUDE.md

> 给 AI 协作者（Claude Code、Cursor 等）以及新加入工程师的项目守则。
> 优先读这一份，再读 `docs/FEATURES.md` 与 `docs/ARCHITECTURE.md`。

---

## 项目一句话定位

HAUI 是 **Hermes AI 服务端的 Android 客户端**，Jetpack Compose + 多模块 MVVM，强调复古终端美学与流式对话稳定性。

---

## 必读约定（违反前请先问）

1. **TOOL 角色消息不入库**。Room 仓库层（`ChatHistoryRepository`）会过滤掉 `MessageRole.TOOL`，工具调用气泡仅作为 UX 提示。如果你在做"保存所有消息"的修改，先确认是否破坏这条约束。
2. **SSE 必须使用独立 OkHttpClient**。`NetworkManager.connectSse` 用 `readTimeout(0)` 的专用 client，**不能**复用默认 client（默认 60s 空闲会被断开）。
3. **`[DONE]` 哨兵必须严格匹配**。判断条件是 `trim() == "[DONE]"`，不要写成 `contains("[DONE]")`。
4. **聊天状态用单一 `MutableStateFlow<InternalChatState>`**。不要拆分回多个独立 StateFlow，避免重组放大。
5. **日志使用 `LogUtil` 的 lambda 形式**（`LogUtil.d(TAG) { "msg $expensive" }`），Release 模式下会被丢弃。**不要**使用 `Log.d` 或字符串拼接形式。
6. **`streamJob` / `healthJob` 必须显式 `cancel()` 旧任务**再启动新任务，否则并发流会互相覆写消息。
7. **协议切换从 sessionId 前缀推断**：`run_` 前缀 → `RUN`，其余 → `CHAT_COMPLETIONS`。从历史会话恢复时跳过一次 reset。
8. **批量写库走 `upsertAllWithTimestampCheck`**：单事务 + `IN` 子句，保留已有 `createdAt`。不要逐条 upsert。

---

## 代码风格

### Kotlin

- **数据模型**：`@Serializable` data class，非驼峰字段用 `@SerialName`。
- **DI**：`@Singleton class @Inject constructor(...)` 而非手写 Hilt Module（除非需要绑定接口实现）。
- **Coroutine**：`viewModelScope` + `Dispatchers.IO`（通过 `@IoDispatcher` 注入），不直接用全局 `GlobalScope`。
- **可空性**：避免 `!!`，用 `?.let { }` 或显式默认值。
- **枚举解析**：用 `runCatching { enumValueOf<T>() }.getOrDefault(...)` 容错，未知值回退到默认。

### Compose

- **状态来源单一**：`ViewModel` 暴露 `StateFlow<UiState>`，UI 层 `collectAsState()`。
- **可复用组件**进 `core/ui` 或 `core/designsystem`，不要在 feature 里就地复制。
- **颜色**：永远走 `MaterialTheme.colorScheme`，不要硬编码 `Color(0xFF...)`。
- **字体**：默认 Inter，需要终端感的地方用 Courier New（已注册在 `HAUITypography`）。

### 文件命名

- ViewModel：`<Feature>ViewModel.kt`
- Tab/屏幕：`<Feature>Tab.kt`、`<Feature>Screen.kt`
- 处理器：`<Name>Handler.kt`
- 接口：`<Name>Presenter.kt`、`<Name>Sink.kt`

---

## 设计美学（务必保持）

- **复古 CRT 终端**：青色（`MaterialTheme.colorScheme.primary`）+ 黑底 + 栅格背景（`retroTerminalBackground`）。
- **方形而非圆形**：进度指示器、Switch、按钮圆角接近 0。需要新增组件时遵循这条。
- **指令感**：以 `>` 开头的文本会被高亮，全大写 Label，Monospace 字体。
- **L 型装饰角**：Assistant 气泡的 `terminalCornerBorders` 修饰符。

视觉参考：`design/` 目录下的 HTML mockup（不入库，本地保留）。

---

## 提交规范

- Commit message 用祈使句，可中英混排。例：`fix: SSE Closed 事件未触发上游 onComplete`。
- 一个 commit 解决一个独立问题；不要把无关的格式化、命名重构与功能改动混在一起。
- 涉及行为变更必须更新对应 `docs/`（FEATURES、API、ARCHITECTURE 三者按需）与 `CHANGELOG.md`。
- 不要使用 `--no-verify` 跳过 hook；hook 失败先排查根因。

---

## 不要做的事

- **不要新增依赖** 而不先在 `gradle/libs.versions.toml` 集中登记版本。
- **不要在 feature 模块直接引用 `core:network`** 的 Retrofit Service —— feature 应该只依赖 `core:data` 提供的 Repository。
- **不要复活已删除的依赖**（CameraX / ML Kit / BouncyCastle 已剔除，除非有明确需求）。
- **不要写多行 docstring 或大段注释**。注释只解释 *为什么*，不解释 *是什么*。
- **不要为不可能发生的场景写防御代码**。在系统边界校验输入，内部信任契约。

---

## 高频任务的入口

| 想做 | 改哪 |
| --- | --- |
| 新增 Hermes 接口 | `core:network/HermesService.kt` → `core:data/repository/HermesRepository.kt` |
| 新增 SSE 事件类型 | `core:data/chat/ChatRunsHandler.kt` 或 `ChatCompletionsHandler.kt` |
| 新增聊天消息字段 | `core:model/HermesResponseModels.kt` + `core:data/db/entity/ChatMessageEntity.kt` + `ChatMessageMapper.kt`（注意升 Room 版本 + 写 `HauiMigrations` Migration + 提交新 schema JSON） |
| 新增设置项 | `core:data/LocalDataStore.kt` 新增键 → `feature/home/settings/SettingsViewModel.kt` 合并到 `combine` |
| 新增主题色 | `core:designsystem/HAUIColors.kt` + `HAUITheme.kt` |

---

## 联调与排查

- **服务端连不上**：先看 AGENT Tab 错误提示；401 区分"未填 apiKey"与"apiKey 错误"。
- **SSE 中途断开**：确认是否复用了默认 OkHttpClient（应使用 SSE 专用 client）。
- **Markdown 表格错位**：表格走自定义实现（横向滚动 + 160dp 列宽），不要回到 mikepenz 默认表格。
- **首帧主题闪烁**：`HAUIApplication.onCreate()` 必须保留 `runBlocking { localDataStore.initialIsDarkTheme() }`。

---

## 已踩过的坑（不要回退）

- **Markdown 表格** 曾尝试 mikepenz 默认实现，已回退到自渲染（横向滚动 + 固定列宽 + maxLines 限制）。
- **Room 升级路径** 曾用 `fallbackToDestructiveMigration` 全量擦库，现已替换为正式 `HauiMigrations` 框架 + 导出 schema 基线（v3）。
- **SSE** 曾复用默认 OkHttpClient，被 60s 空闲超时反复断连；现强制走 `readTimeout(0)` 的独立 client。
