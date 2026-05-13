# `:feature:home`

> 首页特性模块：底部三 Tab（AGENT / CHAT / SETTINGS）+ 各自的 ViewModel 与 UI。

> ⚠️ `build.gradle.kts:9` 中 namespace 为 `ai.tnj.huai.feature.home`（**huai** 拼写问题，待修复，影响范围较小所以暂未处理）。

---

## 职责

- 主屏幕骨架：`HomeScreen` + 自定义 `HAUIBottomBar`。
- AGENT Tab：连接配置、健康检查、模型列表。
- CHAT Tab：双协议对话、Markdown 渲染、历史会话、附件。
- SETTINGS Tab：主题、协议、工具气泡、Hermes Jobs 管理。
- 导航入口：`homeScreen()`（NavGraphBuilder 扩展）。

## 依赖

- `:core:data`（Repository、DataStore、Chat Handlers）
- `:core:model`、`:core:network`（仅 Coil 图片加载与少量 OkHttp Request 构造）
- `:core:ui`、`:core:designsystem`、`:core:utils`
- `markdown-renderer-m3` + `markdown-renderer-coil3`
- `coil-compose` + `coil-network-okhttp`
- `compose-shimmer`

## 目录结构

```
feature/home/src/main/kotlin/.../home/
├── navigation/HomeNavigation.kt        # NavGraphBuilder.homeScreen()
├── ui/
│   ├── HomeScreen.kt                   # Scaffold + 底部 Tab
│   ├── components/PanelHeader.kt       # 通用面板标题（带操作按钮）
│   ├── agent/
│   │   ├── AgentTab.kt
│   │   ├── AgentViewModel.kt
│   │   └── ConnectionSheet.kt          # CONNECT 弹出底栏
│   ├── chat/
│   │   ├── ChatTab.kt
│   │   ├── ChatViewModel.kt            # InternalChatState 单 StateFlow
│   │   ├── ChatComposer.kt             # 输入框 + 附件按钮 + 协议感知
│   │   ├── ChatMessageListCard.kt
│   │   ├── ChatMessageBubble.kt        # User / Assistant / Tool 气泡
│   │   ├── ChatMarkdown.kt             # mikepenz + 自定义 GFM 表格
│   │   └── ChatHistorySheet.kt
│   └── settings/
│       ├── SettingsTab.kt
│       ├── SettingsViewModel.kt
│       └── SettingsModels.kt           # SettingsUiState、ChatProtocol 等
```

## 关键约定

- ViewModel 用 `@HiltViewModel` + `@Inject constructor`。
- UI ↔ ViewModel：单一 `StateFlow<UiState>` + 事件回调（lambda）。
- ChatTab：
  - 状态合并到 `InternalChatState`，UI 只看派生的 `ChatUiState.ChatUIData`。
  - `streamJob` / `healthJob` 显式管理生命周期。
  - 协议切换由 `SettingsViewModel` 写 DataStore，`ChatViewModel` 订阅后 swap presenter。
- SettingsTab：
  - 用 `combine(isDarkTheme, chatProtocol, showToolBubble)` 派生 UI 状态。
- Markdown：表格走自定义实现，其它块走 mikepenz；**不要** 改回 mikepenz 默认表格。

## Tab 简介

### AGENT
- `ConnectionSheet` 输入 host/port/apiKey → `checkHealth` → 持久化。
- `ON_RESUME` 触发 health 刷新（30s 节流）。
- 模型列表展示 `HermesModels.data`。

### CHAT
- 双协议运行时切换（`ChatPresenter` 接口 + 两个 Handler）。
- Markdown 渲染、附件（图片单选、文档单选）、Token 用量（默认上下文 204.8k）。
- ChatHistorySheet 按 sessionId 分组列出历史会话，恢复时按前缀自动切协议。

### SETTINGS
- 主题 / 协议 / 工具气泡开关。
- Hermes Jobs：列表 / 暂停 / 恢复 / 立即运行 / 删除（乐观更新）。

## 测试

```bash
./gradlew :feature:home:test
```
