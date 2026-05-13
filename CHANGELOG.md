# Changelog

本文档记录 HAUI 的所有重要变更。
格式遵循 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.1.0/)，版本规则遵循 [Semantic Versioning](https://semver.org/lang/zh-CN/)。

---

## [Unreleased]

### Added
- 项目级文档体系：`docs/ARCHITECTURE.md`、`docs/API.md`、`docs/BUILD.md`、`docs/RELEASE.md`
- 治理文件：`LICENSE`（MIT）、`CONTRIBUTING.md`、`SECURITY.md`、`CLAUDE.md`
- 各模块 README（`core/*`、`feature/home`）
- `.github` PR / Issue 模板与 `CODEOWNERS`
- `local.properties.example`

### Changed
- 重写 README，与项目当前结构对齐（之前严重过时）
- 把 `docs/` 从 `.gitignore` 移除，文档正式入库
- 为 `app/proguard-rules.pro` 补完整分组注释
- **Room v3 引入正式 Migration 框架**：
  - 开启 `room.schemaLocation` ksp 参数，导出 `schemas/.../3.json` 为基线。
  - 新增 `db/migrations/HauiMigrations.kt`（含模板 + `recreateChatMessages` 帮手）。
  - `DatabaseModule` 移除 `fallbackToDestructiveMigration`：
    升级路径强制走 `HauiMigrations.ALL`，缺失 Migration 会启动崩溃；
    仅对历史 v1/v2 与降级保留 destructive fallback。

---

## [1.0.0] - 2026-05-12

首个里程碑版本，对应 commit `ef73423`。

### Added
- 三 Tab 主界面：AGENT / CHAT / SETTINGS
- 双协议对话支持（`CHAT_COMPLETIONS` / `RUN`）
- Room 聊天历史持久化（TOOL 角色不入库）
- ChatHistorySheet 历史会话恢复
- Hermes Jobs 计划任务管理（列表 / 暂停 / 恢复 / 立即运行 / 删除）
- Markdown 渲染（mikepenz + 自定义 GFM 表格）
- 复古 CRT 终端主题、应用图标、SplashScreen
- DataStore 持久化（主题、协议、工具气泡、sessionId/runId、服务器配置）

### Optimized
- SSE 独立 OkHttpClient（`readTimeout(0)`），消除 60s 空闲断开
- 严格 `[DONE]` 哨兵、显式 `SseEvent.Closed` 处理
- `streamJob` / `healthJob` 显式取消，防止并发流冲突
- 状态合并到 `InternalChatState` 单 `MutableStateFlow`
- Room 批量 `upsertAllWithTimestampCheck`（单事务 + IN 子句，保留 `createdAt`）
- `LogUtil` 改为 inline + lambda，Release 模式零开销
- 启动期主题预读，消除首帧闪烁
- ProGuard 完整规则覆盖 Serialization / Retrofit / Hilt / Room / Coil / Markdown

### Removed
- 未使用的 CameraX / ML Kit / BouncyCastle 等依赖

---

[Unreleased]: https://github.com/TNJ2026/HAUI/compare/v1.0.0...HEAD
[1.0.0]: https://github.com/TNJ2026/HAUI/releases/tag/v1.0.0
