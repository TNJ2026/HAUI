# HAUI · Hermes Android UI

> Hermes AI 服务端的 Android 客户端。Jetpack Compose + 多模块 MVVM，提供对 Hermes 的对话、运行编排（Runs）、健康监控与计划任务（Jobs）管理。

[![Android CI](https://github.com/TNJ2026/HAUI/actions/workflows/android.yml/badge.svg)](.github/workflows/android.yml)

---

## ✨ 功能速览

- **AGENT Tab** — 连接配置、健康检查（30s 节流）、模型列表
- **CHAT Tab** — 双协议对话：
  - `CHAT_COMPLETIONS`：`POST v1/chat/completions`（SSE 流）
  - `RUN`：`POST v1/runs` + `GET v1/runs/{id}/events`（SSE 流）
- **SETTINGS Tab** — 主题切换、协议切换、工具气泡开关、Hermes Jobs 管理
- 复古终端美学（CRT 栅格背景、Monospace 字体、方形进度指示器）
- Room 聊天历史 + Markdown 渲染（GFM 表格自定义实现）

详细功能清单见 [`docs/FEATURES.md`](docs/FEATURES.md)。

---

## 🧱 技术栈

| 维度 | 选型 |
| --- | --- |
| 语言 / JDK | Kotlin 2.3 / JDK 17 |
| UI | Jetpack Compose + Material 3（Compose BOM 管理版本） |
| 架构 | 多模块 MVVM、StateFlow 单一可信源 |
| DI | Hilt 2.59 |
| 网络 | Retrofit 3 + OkHttp 5 + okhttp-sse + kotlinx.serialization |
| 异步 | Kotlin Coroutines + Flow |
| 本地存储 | DataStore Preferences + Room 2.8 |
| Markdown | mikepenz/multiplatform-markdown-renderer + 自定义 GFM 表格 |
| 图片加载 | Coil 3 |
| 测试 | Kotest 6 + Turbine + MockWebServer |

完整版本表见 [`gradle/libs.versions.toml`](gradle/libs.versions.toml)。

---

## 📦 模块结构

```
HAUI
├── app                       # 应用入口、Navigation、SplashScreen
├── core
│   ├── model                 # 网络 / DB 共用数据模型（kotlinx-serialization）
│   ├── network               # Retrofit、OkHttp、SSE 客户端
│   ├── data                  # Repository、Room、DataStore、Chat 流处理器
│   ├── ui                    # 可复用 Compose 组件（CommonUi、CRT 背景）
│   ├── designsystem          # 主题、色板、字体、ThemeController
│   └── utils                 # 日志、附件、JpegSizeLimiter 等工具
└── feature
    └── home                  # 首页三 Tab：Agent / Chat / Settings
```

模块依赖图：

```
app  ──▶  feature:home  ──▶  core:designsystem
                          ──▶  core:ui          ──▶  core:designsystem
                          ──▶  core:data        ──▶  core:network ──▶ core:model
                                                                  ──▶ core:utils
                          ──▶  core:model
                          ──▶  core:utils
```

各模块的 README 见对应目录下 `README.md`。

---

## 🚀 快速开始

### 环境要求

- JDK 17
- Android SDK：`compileSdk = 37`、`minSdk = 31`、`targetSdk = 36`
- Android Studio Ladybug+（建议）或 IntelliJ IDEA 2025.1+
- Hermes 后端服务（用于联调）

### 克隆并构建

```bash
git clone https://github.com/TNJ2026/HAUI.git
cd HAUI

# 配置 SDK 路径（首次）
cp local.properties.example local.properties
# 然后编辑 local.properties 写入本机 sdk.dir

# Debug APK
./gradlew :app:assembleDebug

# Release APK（需要本地签名配置，见 docs/RELEASE.md）
./gradlew :app:assembleRelease
```

### 安装到设备

```bash
./gradlew :app:installDebug
```

### 运行测试

```bash
./gradlew test            # 全量单元测试（JUnit Platform + Kotest）
./gradlew :core:network:test
```

更详细的构建说明见 [`docs/BUILD.md`](docs/BUILD.md)。

---

## 🔌 与 Hermes 服务端联调

应用启动后在 **AGENT Tab → 点击 CONNECT** 输入：

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| host | （无） | Hermes 服务地址，需合法 IPv4 |
| port | `8642` | 服务端口 |
| apiKey | （可选） | Bearer token |

连接成功后自动持久化至 DataStore，下次冷启动自动尝试。

> ⚠️ `AndroidManifest.xml` 中 `usesCleartextTraffic="true"` 用于支持局域网明文连接；正式上线前应切换为 HTTPS 或通过用户级开关管理。

接口契约见 [`docs/API.md`](docs/API.md)。

---

## 📚 文档索引

| 文档 | 用途 |
| --- | --- |
| [`docs/FEATURES.md`](docs/FEATURES.md) | 全功能清单与实现要点 |
| [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) | 模块依赖图、SSE 数据流、状态机 |
| [`docs/API.md`](docs/API.md) | Hermes 服务端接口契约 |
| [`docs/BUILD.md`](docs/BUILD.md) | 构建命令与环境矩阵 |
| [`docs/RELEASE.md`](docs/RELEASE.md) | 签名、CI 发布流程 |
| [`CONTRIBUTING.md`](CONTRIBUTING.md) | 提交、代码风格、PR 流程 |
| [`CHANGELOG.md`](CHANGELOG.md) | 版本变更记录 |
| [`CLAUDE.md`](CLAUDE.md) | 给 AI 协作者/新工程师的项目守则 |

---

## 🗺️ 已知限制 / 路线图

- [ ] 增加 build flavors（`dev` / `prod`）以隔离环境
- [ ] HTTPS 支持与 `usesCleartextTraffic` 的用户级开关
- [ ] 扩充单元 / UI 测试覆盖
- [ ] 服务端字段容错与回退展示（Token 用量、Tool 进度、Job 状态）

---

## 📝 License

本项目采用 [MIT License](LICENSE)。
