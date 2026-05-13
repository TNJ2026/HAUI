# Contributing to HAUI

感谢你对 HAUI 的兴趣。本文档说明本仓库的协作流程与代码规范。

---

## 开始之前

- 阅读 [`README.md`](README.md) 了解项目结构。
- 阅读 [`CLAUDE.md`](CLAUDE.md) 了解必读约定（**TOOL 消息不入库、SSE 独立 client、`[DONE]` 严格匹配** 等）。
- 阅读 [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) 了解模块依赖与数据流。

---

## 开发环境

| 工具 | 版本 |
| --- | --- |
| JDK | 17 |
| Android SDK | compileSdk 37 / minSdk 31 / targetSdk 36 |
| Android Studio | Ladybug Feature Drop 或更新 |
| Gradle | 由 Wrapper 管理（`./gradlew`） |

首次拉取后：

```bash
cp local.properties.example local.properties
# 编辑 local.properties，写入 sdk.dir=/path/to/Android/sdk
./gradlew :app:assembleDebug
```

---

## 分支与提交

### 分支命名

- `feature/<short-desc>` — 新功能
- `fix/<short-desc>` — Bug 修复
- `refactor/<short-desc>` — 重构
- `docs/<short-desc>` — 文档

### Commit 规范

- 使用祈使语气，中英文均可（项目其它 commit 多为中文）。
- 一个 commit 解决一件事；不要混合无关改动。
- 涉及行为变更，同 commit 内更新 `docs/` 与 `CHANGELOG.md`。

示例：

```
fix(chat): SSE Closed 事件未上抛触发 onComplete

ChatCompletionsHandler 在收到 SseEvent.Closed 时只 break，
未调用 onMessageEnd，导致 ChatViewModel 的 hasPendingRun
保持 true。显式调用 onMessageEnd 修复。
```

### Pull Request

1. Fork & 新分支 → push 到自己的 fork。
2. 通过 `gh pr create` 或 Web 创建 PR，标题简洁（< 70 字符）。
3. PR 描述参考 [`.github/PULL_REQUEST_TEMPLATE.md`](.github/PULL_REQUEST_TEMPLATE.md)，至少包含：
   - **Summary**：1-3 个 bullet 说明做了什么、为什么。
   - **Test plan**：如何验证（手动 / 自动）。
4. 等待 CI 绿色 + 至少一位维护者 review。

---

## 代码风格

### 通用

- 删掉自动生成但未使用的导入。
- 不写"是什么"的注释，只写"为什么"的注释（解释非显然的约束、bug 历史、惊奇的行为）。
- 不写多行 docstring；如确实需要，一行 KDoc。
- 不为不可能的场景写防御代码。

### Kotlin

- 优先 `val`，不可变集合优先。
- 用 `data class` 表示纯数据，用 `sealed class` 建模有限状态。
- 异步：`Flow` / `StateFlow` 优先于 callback；Dispatcher 通过 `@IoDispatcher` 注入。
- 避免 `!!`；用 `?.let` / `requireNotNull(...)` / 显式默认值。

### Compose

- ViewModel 暴露单一 `StateFlow<UiState>`，UI 用 `collectAsState()`。
- 颜色走 `MaterialTheme.colorScheme`，不硬编码。
- 字体走 `HAUITypography`，需要终端感的用 `FontFamily(Font(R.font.courier_new_regular))`。
- Stateful 与 Stateless Composable 分离，便于 Preview。

### 命名约定

- 类：`PascalCase`
- 函数 / 变量：`camelCase`
- 常量：`UPPER_SNAKE_CASE`
- Composable 函数：`PascalCase`（同类名）
- ViewModel：`<Feature>ViewModel`
- UiState：`<Feature>UiState`（或 `InternalChatState` 这样的内部 wrapper）

---

## 测试

```bash
./gradlew test                # 所有模块
./gradlew :core:network:test  # 单模块
```

- 单元测试用 **Kotest** + **Turbine**（Flow 断言）+ **MockWebServer**（网络）。
- 测试类命名：`<Subject>Test` / `<Subject>Spec`。
- 不要 mock 自己写的 data class；用真实实例。

---

## 新增依赖

1. 先在 [`gradle/libs.versions.toml`](gradle/libs.versions.toml) `[versions]` 段集中登记版本号。
2. `[libraries]` 段添加别名。
3. 在目标模块的 `build.gradle.kts` 中以 `libs.xxx` 形式引用。
4. 如果是大型依赖，PR 中说明替代方案与选择理由。

---

## 修改 Room Schema

基线版本 **v3**，schema 已通过 ksp `room.schemaLocation` 导出到
`core/data/schemas/ai.tnj.haui.core.data.db.HauiDatabase/3.json`。
从 v3 起，每次升版本都 **必须** 配套写 Migration —— `DatabaseModule`
不再对升级做 destructive fallback，缺失 Migration 会在启动时崩溃。

升级步骤：

1. 调整 `ChatMessageEntity` 或新增表 → 提升 `HauiDatabase.version`。
2. 同时更新 `ChatMessageMapper` 与 `ChatMessageDao`。
3. 重新构建一次 → `schemas/.../<new>.json` 自动生成，**连同源码一起提交**。
4. 在 `core/data/db/migrations/HauiMigrations.kt` 中：
   - 新增 `MIGRATION_N_M`（按模板）。
   - 把它追加到 `HauiMigrations.ALL`。
5. 在 `core:data/src/androidTest/.../MigrationTest.kt` 增加 Migration 测试
   （使用 `MigrationTestHelper`，断言旧 schema → 新 schema 数据不丢失）。
6. PR 描述中说明 Migration 的语义（加列 / 重命名 / 数据搬移）。

> 历史 v1 / v2 设备没有 schema 历史，由 `fallbackToDestructiveMigrationFrom(1, 2)` 兜底销毁。降级（用户回装旧 APK）也会销毁本地数据。

---

## 文档同步

| 改动类型 | 必须更新 |
| --- | --- |
| 新功能 | `docs/FEATURES.md`、`CHANGELOG.md` |
| 接口契约变化 | `docs/API.md` |
| 模块依赖 / 数据流变化 | `docs/ARCHITECTURE.md` |
| 构建配置变化 | `docs/BUILD.md`、`docs/RELEASE.md` |
| 影响外部使用者的接口 | `README.md`（如有必要） |

---

## 沟通

- Bug：通过 [Issues](../../issues) 提交，模板见 [`.github/ISSUE_TEMPLATE/`](.github/ISSUE_TEMPLATE)。
- 安全漏洞：**不要走公开 Issue**，请使用 GitHub Security Advisories（`Security` → `Report a vulnerability`）或邮件至维护者。

---

## 行为准则

请在所有交流中保持专业与尊重。我们不容忍歧视、骚扰或人身攻击。
