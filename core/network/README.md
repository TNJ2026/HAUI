# `:core:network`

> Retrofit / OkHttp / SSE 网络层。对外暴露 `HermesService`（REST）与 `NetworkManager`（SSE）。

---

## 职责

- 维护 OkHttp + Retrofit 客户端，注入认证头与动态 BaseURL。
- SSE 长连接（独立 client，`readTimeout(0)`）。
- 统一错误包装：`SafeHermesCall.hermesFlow { }` → `HermesResponse<T>`。

## 依赖

- `okhttp` + `okhttp-sse` + `logging-interceptor`
- `retrofit` + `retrofit-converter-kotlinx-serialization`
- `kotlinx-coroutines-android`
- 上游：`:core:model`、`:core:utils`

## 关键文件

| 文件 | 用途 |
| --- | --- |
| `HermesService.kt` | Retrofit 接口声明（health / models / runs / chat completions / jobs） |
| `HermesEndpoint.kt` | `HermesEndpoint`（运行时 host/port/apiKey）+ `HermesEndpointInterceptor` |
| `NetworkManager.kt` | `connectSse(request): Flow<SseEvent>` + 单独的 OkHttp client |
| `SafeHermesCall.kt` | `hermesFlow { }` 统一 try/catch 与 Loading/Success/Error 映射 |
| `di/NetworkModule.kt` | Hilt 提供 OkHttp、Retrofit、HermesService 等单例 |

## 使用方式

### REST 调用

```kotlin
@Inject lateinit var service: HermesService

flow {
    emit(HermesResponse.Loading)
    emit(HermesResponse.Success(service.health()))
}.catch { emit(HermesResponse.Error(it.toHttpError())) }
```

或直接用 `SafeHermesCall.hermesFlow { service.health() }`。

### SSE 调用

```kotlin
val req = Request.Builder()
    .url("http://$host:$port/v1/runs/$runId/events")
    .header("Authorization", "Bearer $apiKey")
    .build()

networkManager.connectSse(req).collect { event ->
    when (event) {
        is SseEvent.Open -> ...
        is SseEvent.Message -> ...
        is SseEvent.Closed -> ...
        is SseEvent.Failure -> ...
    }
}
```

## 错误码

| 常量 | 值 | 含义 |
| --- | --- | --- |
| `HttpError.ERROR_CODE_PARSE_JSON` | 10001 | 解析失败 |
| `HttpError.ERROR_CODE_NETWORK` | 10002 | IO / DNS / Timeout |
| `HttpError.ERROR_CODE_UNKNOWN` | 10003 | 其它未归类 |

## 使用约定

- **SSE 必须** 用 `NetworkManager.connectSse`，**不要** 复用 REST 的 OkHttpClient。
- 新接口先在 `HermesService` 加方法签名，再写 Repository 包装。
- 端点路径 **不要** 硬编码在 ViewModel；放在 `HermesService`。
- 拦截器只允许加一个，原因见 `NetworkModule.kt` 上方注释。

## 测试

```bash
./gradlew :core:network:test
```

依赖：Kotest + MockWebServer + Turbine。
