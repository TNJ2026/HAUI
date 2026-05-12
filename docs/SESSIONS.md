# HAUI 历史会话整理

> 数据来源：`/Users/cxd/.claude/projects/-Users-cxd-Documents-Dev-Gemini-HAUI/*.jsonl`
> 时间范围：**2026-05-01 → 2026-05-12**，共 **9 个会话**、约 **100+ 条用户指令**、改动 **70+ 个文件**。
> 本文按时间线串联各次会话，标注每次会话的主旨、关键指令与产物文件，便于回溯设计决策。

---

## 0. 总览

| # | 会话 ID（前 8 位） | 时间 | 用户指令数 | 影响文件 | 主线 |
| - | --- | --- | --- | --- | --- |
| 1 | `39bc73b5` | 2026-05-01 10:23 → 2026-05-02 01:40 | 1 | 1 | Hermes Completions 数据类生成 |
| 2 | `5c4540b2` | 2026-05-02 02:24 → 02:34 | 4 | 0 | DESIGN.md 主题改造（未落盘） |
| 3 | `9e54daf5` | 2026-05-02 02:36 → 2026-05-03 02:55 | 32 | 13 | 复古终端主题 + AgentTab 重设计 + ConnectionSheet + LocalDataStore |
| 4 | `32448a2c` | 2026-05-04 11:57 → 14:15 | 4 | 5 | Tool/Delta 消息排序 + 打字指示器 + ChatBubble 样式 |
| 5 | `79b498f5` | 2026-05-06 06:36 | 1 | 0 | `ls` 探活 |
| 6 | `474263e9` | 2026-05-06 06:37 → 09:55 | 6 | 2 | 图片/文档单选 + Attach 下拉菜单 |
| 7 | `a9e33f7a` | 2026-05-06 14:38 → 2026-05-07 06:47 | 8 | 12 | 协议感知附件按钮 + sessionId/runId 持久化 + ToolBubble 开关 + Switch 方形化 |
| 8 | `b022a2f9` | 2026-05-07 07:13 → 2026-05-09 07:29 | 28 | 27 | Room 聊天历史 + ChatHistorySheet + 应用图标 + ConnectionSheet 重设计 |
| 9 | `3f291e03` | 2026-05-10 14:41 → 2026-05-12 06:55 | 18 | 32 | mikepenz Markdown + Network/App/Data/ChatViewModel 全面优化 + FEATURES.md |

---

## 1. 会话 #1 — `39bc73b5`（2026-05-01）：Hermes Completions 数据类

**单一指令**：用户粘贴 `chat.completion.chunk` JSON 片段，要求据此生成 Kotlin data class，忽略 `object` 字段，非驼峰字段用 `@SerialName`。

**产物**：
- `core/model/.../HermesCompletionsContent.kt`

**意义**：建立流式聊天事件数据模型的雏形，为后续 SSE 解析铺路。

---

## 2. 会话 #2 — `5c4540b2`（2026-05-02 02:24 ~ 02:34）：主题改造尝试（未落盘）

**指令（重复 4 次）**：
> 根据 design 文件夹下的 DESIGN.md 更改 theme

**结果**：无文件修改（推测为对话探索阶段，未确认落盘）。紧接着用户开启新会话继续推进。

---

## 3. 会话 #3 — `9e54daf5`（2026-05-02 02:36 ~ 2026-05-03 02:55）：复古终端主题 + AgentTab 重构

会话内 32 条指令，是早期最密集的一段开发。可分为四组：

### 3.1 主题与底部导航
- 根据 `design/DESIGN.md` 调整主题（终端复古风）。
- 参考 `design/code.html` 与 `screen.png` 重做 AgentTab 卡片样式。
- CONNECT/DISCONNECT 按钮改为青色；底部导航栏也按 `code.html` 重做。
- 后续移除 AgentTab 中自声明的颜色常量，改为直接使用 `MaterialTheme.colorScheme` 对应色。

### 3.2 ConnectionSheet
- 参考 `design/sheet.html`，AgentTab 点击 CONNECT 弹出 BottomSheet。
- 移除独立的 DISCONNECT 按钮，将连接状态统一到 sheet。
- `serverAddress` 拆为 `host` / `port` 两个独立输入字段，通过 sheet 配置。
- CONNECT 按钮加输入校验：
  - host 用正则校验为合法 IPv4
  - port 限制为数字
  - host 输入也限制为合法字符
- port 默认值 `8642`。
- CONNECT 按钮下方加错误提示文本（绑定 `AgentUiState.connectError`）。

### 3.3 数据层与网络层调整
- `SecurePrefs` 重命名为 `LocalDataStore`，删除未使用方法。
- `HermesRequests.BASE_URL` 改为运行时由 host:port 拼装。
- `Authorization` 头改为传入 API key。
- 修复 KSP 因 `SecurePrefs` 找不到导致的 DataModule 处理失败。
- 移除 `LocalDataStore` 中保存 `GatewayToken` 的旧逻辑。
- 点击 CONNECT 先用输入的 host/port/apiKey 调 `checkHealth`，成功后再写入 `LocalDataStore`。
- `AgentViewModel.init` 启动时读取 LocalDataStore，若存在则自动 `checkHealth`。
- 连接期显示 LoadingOverlay，成功关闭 sheet，失败 Snackbar 提示；后续将 `LoadingOverlay` 直接覆盖在 ConnectionSheet 上方而非整页。
- `LaunchedEffect` 改 `DisposableEffect` 以正确管理生命周期。
- 调整调用顺序：先 `fetchModels` 再 `connect`。

### 3.4 状态整合与模型补全
- 将 health / isConnected / isHealthy 等分散字段整合到统一 `AgentUiState`。
- 一度移除再恢复 `getModels` 逻辑。
- 根据真实 health JSON（包含 `platforms.api_server.state`、`gateway_state`、`updated_at` 等）补全 `HermesHealth` 字段。
- 加入依赖：`com.valentinilk.shimmer:compose-shimmer`。

**主要产物**：
- `core/data/.../HermesRequests.kt`、`LocalDataStore.kt`、`di/DataModule.kt`、`repository/HermesRepository.kt`
- `core/designsystem/.../HAUIColors.kt`、`HAUITheme.kt`、`HAUITypography.kt`
- `core/model/.../HermesResponseModels.kt`
- `feature/home/.../HomeScreen.kt`、`agent/AgentTab.kt`、`agent/AgentViewModel.kt`
- `feature/home/build.gradle.kts`、`gradle/libs.versions.toml`

---

## 4. 会话 #4 — `32448a2c`（2026-05-04 11:57 ~ 14:15）：聊天流序与气泡样式

**指令**：

1. 修复 `OpenChatViewModel` 中 ChatCompletions 流的事件顺序：
   - 事件只有 `tool` 与 `Delta` 两种，到达顺序乱序。
   - 写入 `_messages` 时始终保持 `tool` 类型在前、`Delta` 类型在后。
2. 用户发消息后立即插入 `ChatTypingIndicatorBubble`，收到 tool 或 Assistant 消息后移除。
3. 按 `design/chat.html` 调整 ChatBubble 样式。
4. 询问：能否实现一个方形的进度指示器？（设计基调延续复古终端方块美学）

**产物**：
- `feature/home/.../chat/ChatMessageListCard.kt`
- `feature/home/.../chat/ChatMessageViews.kt`
- `feature/home/.../chat/ChatTab.kt`
- `feature/home/.../chat/ChatViewModel.kt`
- `feature/home/.../chat/OpenChatViewModel.kt`（彼时尚未合并为 ChatViewModel）

---

## 5. 会话 #5 — `79b498f5`（2026-05-06 06:36）：探活会话

仅一条指令 `ls`，无实际修改。

---

## 6. 会话 #6 — `474263e9`（2026-05-06 06:37 ~ 09:55）：图片/文档附件

**指令链路**：
1. `ChatTab.pickImages` 改为只能选图片，且单选（连续 2 次确认）。
2. ChatComposer 的 Attach 按钮点击后弹出选择列表，包含「图片」与「文档」。
3. DropdownMenu 加边框。
4. DropdownMenuItem 之间加分隔线。
5. 实现文档选择逻辑，同样单选。

**产物**：
- `feature/home/.../chat/ChatComposer.kt`
- `feature/home/.../chat/ChatTab.kt`

**设计取舍**：附件来源用下拉而非全屏选择器，与底部 Composer 紧贴，操作链路最短。

---

## 7. 会话 #7 — `a9e33f7a`（2026-05-06 14:38 ~ 2026-05-07 06:47）：协议感知与持久化

**指令**：

| 序号 | 指令摘要 |
| --- | --- |
| 1 | 协议为 CHAT_COMPLETIONS 时显示 ChatTab 输入框左侧的 Attach 按钮，其它情况隐藏 |
| 2 | `ChatCompletionsHandler.currentSessionId` 持久化 |
| 3 | `ChatRunsHandler.currentRunId` 持久化 |
| 4 | Clear 操作不清空 chat run ID |
| 5 | Settings 新增开关：是否显示 ChatToolBubble |
| 6 | `build and run`（编译验证） |
| 7 | SettingsTab 的 Switch 改为自定义方形（贴合终端美学） |
| 8 | 移除 PanelHeader 的 badge |

**产物**：
- `core/data/.../LocalDataStore.kt`（新增 sessionId/runId/showToolBubble 键）
- `feature/home/.../chat/ChatCompletionsHandler.kt`、`ChatRunsHandler.kt`、`ChatPresenter.kt`
- `feature/home/.../chat/ChatComposer.kt`、`ChatTab.kt`、`ChatViewModel.kt`、`ChatMessageListCard.kt`
- `feature/home/.../components/PanelHeader.kt`
- `feature/home/.../settings/SettingsTab.kt`、`SettingsViewModel.kt`
- `feature/home/.../agent/AgentTab.kt`（关联修复）

**设计意图**：让聊天体验在两种协议间无缝切换，并把工具调用气泡设为可选 UX。

---

## 8. 会话 #8 — `b022a2f9`（2026-05-07 07:13 ~ 2026-05-09 07:29）：聊天历史 · 协议自动切换 · 应用图标

本次会话耗时近 48 小时，是项目的"功能跃迁"段，可分七组：

### 8.1 健康检查节流
- AgentTab 的 `checkHealth` 加 30 秒节流，避免回到该页时反复触发。

### 8.2 ToolBubble 隐藏与打字指示器联动 Bug
- Bug：关闭 Tool bubble 显示后，收到 Tool 事件会让 `ChatTypingIndicatorBubble` 消失，但 AI 回复并未出现，造成"卡住"假象。
- 修复：Tool 事件在被隐藏时不影响打字指示器存活。

### 8.3 Room 聊天历史
- 用户：聊天记录保存到 Room，**不保存 TOOL 类型消息**。
- 进一步细化：一个 `sessionId` 一条会话记录，实时增量保存。
- 后又改为：只在 `onMessageEnd` 时一次性快照保存。
- 默认值：`SettingsUiState.showToolBubble = false`，`LocalDataStore.showToolBubble` 默认 false。
- New Chat：消息非空时清空列表、重置状态、重置 sessionId/runId。
- 保存的 sessionId 来自 `ChatRunsHandler.currentRunId` 或 `ChatCompletionsHandler.currentSessionId`（而非随机 UUID）。

### 8.4 ChatHistorySheet
- 按 `design/chat_history.html` 实现 BottomSheet，按 sessionId 分组显示所有历史。
- ChatTab AppBar 增加 History 按钮触发。
- 点击历史记录恢复时：
  - 若 sessionId 以 `run_` 开头 → 切换到 Run 协议
  - 若以 `api_`/其它开头 → 切换到 ChatCompletions
- 当 chat history 与 chatTab messages 都为空时，隐藏右上角 New Chat 按钮。
- 每次打开 Sheet 都刷新历史。
- 打开 Sheet 前主动收起输入键盘。
- 消息为空时，EmptyChatHint 增加一个"打开聊天历史"按钮。

### 8.5 协议切换的副作用
- 用户在 Settings 切换 ChatProtocol 后，清空 ChatTab 消息、重置状态。

### 8.6 ChatCompletions 图片消息
- 检查带图片的 ChatCompletions 请求是否符合 OpenAI 接口协议。
- 用户确认 → 落实修改。
- 影响 `HermesRequestModels.kt`、`HermesRequests.kt`。

### 8.7 应用图标与 ConnectionSheet 重设计
- 将 `design/icon.png` 设为 App 图标。
- 按 `design/connect_config.html` 重做 AgentTab 的 ConnectionSheet 样式。
- 已连接状态下 CONNECT 按钮改为"修改连接配置"，点击仍打开 sheet，但预填已保存的 host/port/apiKey。
- 使用 `design/app_icon.png` 生成 adaptive 图标，风格贴近 `design/icon.png`。
- `ic_launcher_foreground` 尺寸缩小（之前显示不全）。
- 最终选定 `design/hermes_App_icon.html` 中第一个样式作为正式图标方案。
- PanelHeader：MODIFY_CONFIG 按钮位置调整 — 先放到 Data Panels header 右上角，最终落定到 PanelHeader 内部。

**主要产物**：
- 数据层：`db/HauiDatabase.kt`、`db/dao/ChatMessageDao.kt`、`db/entity/ChatMessageEntity.kt`、`db/ChatMessageMapper.kt`、`di/DatabaseModule.kt`、`repository/ChatHistoryRepository.kt`
- 资源：`mipmap-anydpi-v26/ic_launcher.xml`、`ic_launcher_round.xml`、`drawable/ic_launcher_background.xml`、`ic_launcher_foreground.xml`
- 聊天：`ChatHistorySheet.kt`、`ChatViewModel.kt`、`ChatTab.kt`、`ChatCompletionsHandler.kt`、`ChatRunsHandler.kt`、`ChatPresenter.kt`、`ChatMessageListCard.kt`
- Agent：`AgentTab.kt`、`AgentViewModel.kt`
- 设计：`PanelHeader.kt`、`SettingsViewModel.kt`、`LocalDataStore.kt`、`HermesRequests.kt`、`HermesRequestModels.kt`
- 还产生了一个 Plan 文件：`~/.claude/plans/room-tool-message-frolicking-shamir.md`

---

## 9. 会话 #9 — `3f291e03`（2026-05-10 14:41 ~ 2026-05-12 06:55）：Markdown 渲染重写 + 多模块全面优化

本次会话同样耗时 ~48 小时，主导项目从"完成功能"走向"生产级质量"。

### 9.1 Markdown 渲染重写（5/10）
- 用户：能否把 markdown 渲染换成 `mikepenz/multiplatform-markdown-renderer`？
- 「使用这个方案继续」→ 全面切换实现。
- 移除 `ChatMarkdown` 中关于 base64 的旧逻辑。
- 5/11：能否把 markdown 表格改回之前的实现？→ 表格部分回退为自定义实现（横向滚动 + 160dp 单元格 + drawBehind 网格线），其它块仍用 mikepenz。

### 9.2 Network 模块优化（5/12 上午）
- 用户：分析 network 模块是否有可优化处 → 列出 7 项。
- 然后分阶段落地：
  - 「1. SSE 单独设置 timeout」 → 给 SSE 一个独立 OkHttpClient，`readTimeout(0)`，避免 60s 空闲断开。
  - 「修复 2、3，忽略 4」 → 处理 2 项指定优化，跳过第 4 项。
  - 「继续修复 5、6、7」 → 完成剩余三项。
  - 「优化其它小优化」 → 收尾杂项。

**影响**：`NetworkManager.kt`、`SafeHermesCall.kt`、`HermesEndpoint.kt`、`HermesService.kt`、`di/NetworkModule.kt`、`HermesRequests.kt`。

### 9.3 App 模块优化
- 用户：检查 app 模块可优化处。
- 「修改全部」→ 一次性应用：
  - 删除空的 `AppViewModel`（dead code）。
  - 修正 `homeScree()` 拼写为 `homeScreen()`。
  - 删除重复 `implementation(project(":core:data"))`。
  - 主题预读：`HAUIApplication.onCreate()` 中通过 `runBlocking { localDataStore.initialIsDarkTheme() }` 提前同步主题，消除首帧闪烁。
  - `MainActivity` 改用 Compose `LocalActivity.current?.window`。
  - SplashScreen 移除冗余 120dp Box 包裹。

### 9.4 Data 模块优化
- 用户：分析 data 模块可优化处。
- 「全部优化」→ 一次性应用：
  - `LocalDataStore` 改为 `@Singleton class @Inject constructor(@ApplicationContext context)`，删除未用键。
  - 删除 `ApplicationScope` 与未消费的 `DefaultDispatcher`/`MainDispatcher`。
  - Room：版本 2→3，删除冗余 `Index("sessionId")`，保留复合索引 `(sessionId, createdAt)`。
  - 枚举解析容错：`runCatching { enumValueOf<MessageRole>() }.getOrDefault(USER)`。
  - DAO 新增 `upsertAllWithTimestampCheck`：单事务 + `IN` 子句查 createdAt + 批量 upsert。
  - `ChatHistoryRepository.upsertAll(sessionId, messages)`：在仓库层过滤 TOOL 角色后批量写入。
  - `HermesRequests`：`Content-Type` 在 POST 时附加；`X-Hermes-Session-Id` 只在非空时携带。
  - ChatCompletionsHandler/ChatRunsHandler：日志改为 lambda 形式延迟拼接、严格 DONE 哨兵 `trim()=="[DONE]"`、显式处理 `SseEvent.Closed`、修正错误的 TAG。

### 9.5 ChatViewModel 优化
- 用户：分析 ChatViewModel 可优化处。
- 「全部优化」→ 一次性应用：
  - `streamJob`、`healthJob` 显式取消旧任务，防止并发流冲突。
  - 用 `.update {}` 替换 `_messages.value += ...`。
  - 用 `toMutableList().apply { set(idx, message) }` 优化 `onMessageUpdate`。
  - 字段命名、`!!` 消除、初始 protocol 推断、`sessionId.startsWith("run_")` 严格匹配。
  - `persistCurrentConversation` 单次 `upsertAll`。
- 用户随后**手动二次重构**：将 5 个 MutableStateFlow 合并为 `InternalChatState` 数据类 + 单一 `MutableStateFlow`；`uiState` 通过 `.map` 派生；`refreshSessions` 加 `isConfigured` 守卫。
- 用户随后**手动**为 `app/build.gradle.kts` 加入 release 配置（minify + shrinkResources + proguard）与 dev/prod 风味。

### 9.6 文档产出
- 用户：分析这个项目的功能，整理输出为 markdown 文档 → 生成 `docs/FEATURES.md`。
- 用户：你有这个项目所有的对话记录吗？ → 列出 9 份 JSONL。
- 用户：分析整理所有的会话记录，输出为详细的 Markdown 文档 → 本文件。

**产物**：
- 文档：`docs/FEATURES.md`、`docs/SESSIONS.md`（本文件）
- 网络：`NetworkManager.kt`、`SafeHermesCall.kt`、`HermesEndpoint.kt`、`HermesService.kt`、`di/NetworkModule.kt`
- 数据：`LocalDataStore.kt`、`db/HauiDatabase.kt`、`db/HauiTypeConverters.kt`、`db/dao/ChatMessageDao.kt`、`db/entity/ChatMessageEntity.kt`、`repository/HermesRepository.kt`、`repository/ChatHistoryRepository.kt`、`chat/ChatCompletionsHandler.kt`、`chat/ChatRunsHandler.kt`、`HermesRequests.kt`、`di/DataModule.kt`、`di/DispatchersModule.kt`
- App：`HAUIApplication.kt`、`MainActivity.kt`、`SplashScreen.kt`、`navigation/AppNavigation.kt`、`build.gradle.kts`
- Feature：`feature/home/.../chat/ChatViewModel.kt`、`chat/ChatMarkdown.kt`、`navigation/HomeNavigation.kt`、`feature/home/build.gradle.kts`
- 版本：`gradle/libs.versions.toml`、`core/ui/build.gradle.kts`

---

## 10. 跨会话主题脉络

把会话扁平化后，可以看到几条贯穿始终的主线：

### 10.1 设计基调：复古终端美学
- 起源：会话 #3 引入 `design/code.html`、`screen.png`，确立青色 + 方块 + 网格背景的风格。
- 强化：会话 #4 询问"方形进度指示器"；会话 #7 把 Settings 的 Switch 改为方形；会话 #8 完成应用图标的同款风格。
- 延伸：会话 #9 在退路上把 GFM 表格按风格回退为自定义实现，避免 mikepenz 默认表格破坏氛围。

### 10.2 数据持久化的演进
1. 会话 #3：建立 `LocalDataStore`，仅存 host/port/apiKey。
2. 会话 #7：扩展存 sessionId/runId/showToolBubble。
3. 会话 #8：引入 Room，新增 `ChatMessageEntity`、`ChatMessageDao`、`ChatHistoryRepository`，**TOOL 消息不入库**。
4. 会话 #9：DAO 增加批量 + 单事务版本；版本号升到 3；Repository 增加 `upsertAll`。

### 10.3 双协议的演进
1. 会话 #4：在 OpenChatViewModel 里处理 Tool/Delta 排序。
2. 会话 #7：根据协议显示/隐藏 Attach 按钮；持久化各自的 sessionId/runId。
3. 会话 #8：从 sessionId 前缀自动推断协议；切换协议清空状态。
4. 会话 #9：抽出 `ChatPresenter` 接口 + 两个 Handler；ViewModel 内 `swapChatHandler` + `protocol.drop(1).collect`；移除`var first = true`等代码味道。

### 10.4 SSE 与流式消息的可靠性
- 会话 #4：建立 Tool/Delta 排序与打字指示器的协调。
- 会话 #8：修复 Tool 被隐藏时打字指示器误消失的 bug。
- 会话 #9：SSE 客户端 `readTimeout(0)`、显式 Closed 处理、严格 `[DONE]` 匹配、`streamJob`/`healthJob` 显式取消。

### 10.5 命名与组织的清理
- `SecurePrefs` → `LocalDataStore`（会话 #3）。
- `OpenChatViewModel` → `ChatViewModel`（会话 #4 ~ 会话 #7 期间合并）。
- `_chatCompletionsHandler` → `chatCompletionsHandler`（会话 #9）。
- `homeScree` → `homeScreen`（会话 #9）。

---

## 11. 命令风格观察

通览全部 100 余条用户指令，可以提炼出用户的协作偏好（已写入 memory）：

- **指令短、目标明确**：多数是单句中文祈使，如「移除 X」「按 Y 修改」「全部优化」。
- **设计文件驱动**：高频参考 `design/*.html` 与 `design/*.png`，少用文字描述视觉。
- **小步快跑**：发出指令 → 验证 → 再发下一个；偶尔一句「build and run」作为验证锚点。
- **批准范式**：分析建议 → 用户回复「全部优化」「修改全部」「使用这个方案继续」一次性放行。
- **手动二次重构**：在 AI 完成大方向重构后，用户常自己再压一层（如 `InternalChatState` 合并、build.gradle 增加风味）。

---

## 12. 文件改动汇总（去重）

按修改频次倒序（取自所有会话的 Edit/Write 调用）排前 20：

```
feature/home/.../ChatViewModel.kt                  (会话 #4/#7/#8/#9)
feature/home/.../ChatTab.kt                        (会话 #4/#6/#7/#8)
feature/home/.../ChatMessageListCard.kt            (会话 #4/#7/#8)
feature/home/.../ChatComposer.kt                   (会话 #6/#7)
feature/home/.../AgentTab.kt                       (会话 #3/#7/#8)
feature/home/.../AgentViewModel.kt                 (会话 #3/#8)
feature/home/.../SettingsViewModel.kt              (会话 #7/#8)
feature/home/.../ChatCompletionsHandler.kt         (会话 #7/#8/#9)
feature/home/.../ChatRunsHandler.kt                (会话 #7/#8/#9)
feature/home/.../ChatPresenter.kt                  (会话 #7/#8)
core/data/.../LocalDataStore.kt                    (会话 #3/#7/#8/#9)
core/data/.../HermesRequests.kt                    (会话 #3/#8/#9)
core/data/.../repository/HermesRepository.kt       (会话 #3/#9)
core/data/.../repository/ChatHistoryRepository.kt  (会话 #8/#9)
core/data/.../db/dao/ChatMessageDao.kt             (会话 #8/#9)
core/data/.../db/HauiDatabase.kt                   (会话 #8/#9)
core/network/.../NetworkManager.kt                 (会话 #9)
core/network/.../SafeHermesCall.kt                 (会话 #9)
core/model/.../HermesResponseModels.kt             (会话 #3/#9)
gradle/libs.versions.toml                          (会话 #3/#9)
```

---

## 13. 待办与遗留议题

从历史会话尾声归纳，下一步可能继续推进的方向：

1. **正式签名**：release 仍用 debug 签名（`app/build.gradle.kts` 注释明示 TODO）。
2. **HTTPS 选项**：当前依赖明文 LAN，未来可考虑用户级 HTTPS 开关。
3. **Room 正式 Migration**：暂用 `fallbackToDestructiveMigration`，稳定版本节点后引入 Migration。
4. **服务端字段容错**：Token 用量、Tool 进度、Job 状态仍强依赖服务端格式。
5. **测试覆盖**：项目早期 README 待办里提到"添加更多单元测试和 UI 测试"，至今未推进。
6. **`design/hermes_App_icon.html` 中其它备选图标方案**：当前只采用了第一个样式，其它备选可作为皮肤主题保留。

---

> 本文以历史会话 JSONL 为唯一信源，仅整理用户原始意图与可被工具调用证实的产物文件。具体代码段、错误信息等可通过 Read 工具按需打开对应 JSONL 行号回溯。
