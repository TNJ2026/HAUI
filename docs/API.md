# Hermes 服务端接口契约

> HAUI 当前依赖的 Hermes 后端接口清单。**任何一处契约变化都需要同时更新这份文档与对应的数据模型**（`core/model/HermesResponseModels.kt`）。

---

## 1. 通信约定

- **Base URL**：运行时由用户配置 `http://<host>:<port>/`（Retrofit 占位 + 拦截器重写，见 `HermesEndpointInterceptor`）。
- **认证**：`Authorization: Bearer <apiKey>` —— apiKey 非空时由拦截器自动附加。
- **Content-Type**：POST JSON 时由 Retrofit 自动加 `application/json; charset=utf-8`。
- **会话头**：`X-Hermes-Session-Id` 仅在非空时携带（用于 SSE 接口）。
- **序列化**：`kotlinx-serialization-json`，未知字段忽略；snake_case 通过 `@SerialName` 映射。

---

## 2. 非流式 REST 端点

下表列出 `HermesService`（Retrofit）声明的所有接口。

| 方法 | 路径 | 入参 | 返回模型 | 用途 |
| --- | --- | --- | --- | --- |
| GET | `health/detailed` | — | `HermesHealth` | 健康检查 |
| GET | `v1/models` | — | `HermesModels` | 模型列表 |
| POST | `v1/runs` | `RunRequestBody` | `HermesRun` | 创建 Run |
| GET | `v1/runs/{run_id}` | — | `HermesRunState` | 查询 Run 状态 |
| POST | `v1/runs/{run_id}/stop` | — | `HermesRunStopModel` | 停止 Run |
| POST | `v1/responses` | `OpenAIResponsesRequestBody` | `HermesRun` | OpenAI Responses 格式（备用） |
| POST | `v1/chat/completions` | `ChatCompletionsRequestBody` | `HermesRun` | 非流式 Chat（流式走 SSE） |
| GET | `api/jobs` | — | `HermesJobs` | Jobs 列表 |
| POST | `api/jobs/{job_id}/pause` | — | — | 暂停 Job |
| POST | `api/jobs/{job_id}/resume` | — | — | 恢复 Job |
| POST | `api/jobs/{job_id}/run` | — | — | 立即运行 Job |
| DELETE | `api/jobs/{job_id}` | — | — | 删除 Job |

---

## 3. SSE 流式端点

SSE 接口 **不通过 Retrofit**，而是通过 `NetworkManager.connectSse(request)` 直接构造 OkHttp `Request`。

### 3.1 Chat Completions（流式）

```http
POST /v1/chat/completions HTTP/1.1
Authorization: Bearer <apiKey>
Content-Type: application/json
X-Hermes-Session-Id: api_xxx   ← 仅在非空时

{
  "model": "...",
  "messages": [...],
  "stream": true
}
```

**事件序列**：

```
data: {"choices":[{"delta":{"content":"hi"}}], ...}
data: {"choices":[{"delta":{"content":" there"}}], ...}
data: {"usage":{"prompt_tokens":12,"completion_tokens":3,"total_tokens":15}}
data: [DONE]
```

- 客户端处理：`ChatCompletionsHandler`
- 结束哨兵：**严格** `trim() == "[DONE]"`
- sessionId：来自响应头或服务端首条事件（持久化在 `LocalDataStore.chatSessionId`）

### 3.2 Runs Events（流式）

```http
GET /v1/runs/{run_id}/events HTTP/1.1
Authorization: Bearer <apiKey>
Accept: text/event-stream
```

**事件序列**：

```
data: {"id":"...","type":"...","role":"assistant","data":{...}}
data: {"id":"...","type":"...","role":"tool","data":{...}}
...
data: [DONE]
```

事件的 `data.event` 字段决定子类型，定义于 `HermesChatMsgEvent`：

| 枚举 | 字符串值 | 数据字段 | 含义 |
| --- | --- | --- | --- |
| `ToolsStart` | `tool.started` | `tool`, `preview` | 工具调用开始 |
| `ToolsCompleted` | `tool.completed` | `duration` | 工具调用结束（携带耗时） |
| `Delta` | `message.delta` | `delta` | 文本增量 |
| `Reasoning` | `reasoning.available` | `text` | 推理过程文本 |
| `Completed` | `run.completed` | `output`, `usage` | Run 完成（含 token 用量） |

- 客户端处理：`ChatRunsHandler`
- runId：从 POST `v1/runs` 返回值读取（持久化在 `LocalDataStore.chatRunId`）

---

## 4. 数据模型详解

### 4.1 `HermesHealth`

```json
{
  "status": "ok",
  "platform": "...",
  "gateway_state": "RUNNING",
  "platforms": {
    "api_server": {
      "state": "RUNNING",
      "error_code": null,
      "error_message": null,
      "updated_at": "2026-05-12T10:00:00Z"
    }
  },
  "active_agents": 2,
  "exit_reason": null,
  "updated_at": "2026-05-12T10:00:00Z",
  "pid": 12345
}
```

UI 展示字段：`status`、`gatewayState`、`platforms.apiServer.state`、`updatedAt`（格式化为 `YYYY/M/D HH:mm:ss`）。

### 4.2 `HermesModels`

```json
{
  "object": "list",
  "data": [
    { "id": "model-a", "root": "..." },
    { "id": "model-b", "root": "..." }
  ]
}
```

### 4.3 `HermesRun`

```json
{ "status": "queued", "run_id": "run_abc123" }
```

POST `v1/runs` 与 POST `v1/chat/completions` 都返回此结构。

### 4.4 `HermesRunState`

```json
{
  "object": "run",
  "run_id": "run_abc123",
  "status": "completed",
  "session_id": "...",
  "model": "...",
  "output": "...",
  "usage": {
    "input_tokens": 100,
    "output_tokens": 50,
    "total_tokens": 150
  }
}
```

### 4.5 `HermesJob`

完整字段见 `HermesResponseModels.kt:86-114`。关键字段：

| 字段 | 类型 | 用途 |
| --- | --- | --- |
| `id` / `name` / `prompt` | String | 标识与展示 |
| `schedule.expr` / `schedule_display` | String | cron 表达式与可读形式 |
| `state` / `last_status` | String | 状态来源（state 优先） |
| `enabled` | Boolean | 总开关 |
| `next_run_at` / `last_run_at` | String? (ISO) | 时间字段，前端格式化展示 |
| `repeat.times` / `repeat.completed` | Int? / Int | 重复次数 |

UI 端 `parseJobStatus()` 把它们映射为：

```
FAILED / RUNNING / SCHEDULED / QUEUED / COMPLETED / PAUSED
```

### 4.6 `HermesChatMessage`（SSE 事件）

```json
{
  "id": "...",
  "type": "message" | "tool" | "system",
  "role": "user" | "assistant" | "tool" | "system",
  "data": {
    "event": "message.delta" | "tool.started" | "tool.completed" | "reasoning.available" | "run.completed",
    "run_id": "run_xxx",
    "timestamp": 1715500800.123,
    "delta": "...",
    "text": "...",
    "output": "...",
    "usage": { "input_tokens": 0, "output_tokens": 0, "total_tokens": 0 },
    "tool": "...",
    "preview": "...",
    "duration": 1.23,
    "error": false
  }
}
```

`data.displayText()` 把 event 类型映射为 UI 文本（见 `HermesResponseModels.kt:152-165`）。

---

## 5. 错误处理

### 5.1 HTTP 错误

- **401**：UI 区分两种文案：
  - 未提供 apiKey → "Unauthorized, Please input API key"
  - 提供了但被拒 → "incorrect API key"
- **其它 4xx / 5xx**：展示 `code + msg`。

### 5.2 客户端错误码（`HttpError.Companion`）

| 常量 | 值 | 触发条件 |
| --- | --- | --- |
| `ERROR_CODE_PARSE_JSON` | 10001 | kotlinx.serialization 解析失败 |
| `ERROR_CODE_NETWORK` | 10002 | IOException / SocketTimeout / DNS / TLS |
| `ERROR_CODE_UNKNOWN` | 10003 | 其它未归类异常 |

通过 `SafeHermesCall.hermesFlow { ... }` 统一映射为 `HermesResponse.Error(HttpError)`。

### 5.3 SSE 错误

`SseEvent.Failure(throwable, response)` —— Handler 应停止当前流并通过 `onError` 上抛。

---

## 6. 版本与兼容性

- 服务端契约 **未** 标版本号。HAUI 与 Hermes 必须配套升级。
- 新增可选字段：直接加 `@SerialName` + 默认值（kotlinx-serialization 自动容错）。
- 删除字段：需同步 HAUI 改动，否则反序列化失败。
- 枚举值变化：`MessageRole` / `HermesChatMsgEvent` 端解析容错为默认值，但 UI 行为可能异常。

---

## 7. 调试技巧

- **OkHttp 日志**：`NetworkModule` 中 `HttpLoggingInterceptor.Level.BODY` 仅在 Debug 启用。
- **MockWebServer**：`core:network` 的测试用 `okhttp-mockwebserver`，可参考其用法编写新接口的单测。
- **SSE 抓包**：用 `curl -N --header "Authorization: Bearer xxx" http://host:port/v1/runs/run_xxx/events` 直接观察流。
