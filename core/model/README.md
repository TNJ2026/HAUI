# `:core:model`

> 纯数据模型模块。`@Serializable` data class + 枚举。**叶子模块**，不依赖任何业务模块。

---

## 职责

- 序列化模型：服务端请求 / 响应、聊天消息内容、Token 用量等。
- 共享枚举：`MessageRole`、`HermesChatMsgEvent`。
- 序列化基础设施：`HauiJson`（全局 `Json` 配置，宽松解析）。

## 依赖

- `kotlinx-serialization-json`
- 无 Android 依赖（理论上可拆为 JVM 模块）

## 关键文件

| 文件 | 内容 |
| --- | --- |
| `HermesRequestModels.kt` | `RunRequestBody`、`ChatCompletionsRequestBody`、`OpenAIResponsesRequestBody` 等 |
| `HermesResponseModels.kt` | `HermesHealth`、`HermesRun`、`HermesJob`、`HermesChatMessage`、`HttpError`、`HermesResponse` |
| `HermesCompletionsModels.kt` | OpenAI Chat Completions 流式增量结构 |
| `ChatMessage.kt` | UI 层使用的聊天消息数据类（domain model） |
| `MessageRole.kt` | `USER` / `ASSISTANT` / `TOOL` / `SYSTEM` 枚举 |
| `HauiJson.kt` | `val HauiJson = Json { ignoreUnknownKeys = true; ... }` |

## 使用约定

- **新增字段**：加 `@SerialName` + 默认值，便于服务端先行。
- **枚举**：解析时用 `runCatching { enumValueOf<T>() }.getOrDefault(...)` 容错。
- **不要** 在此模块写业务逻辑（除 `HermesMessageData.displayText()` 这类纯文本格式化）。
- **不要** 引入 Android 依赖（Context、Resources 等）。
