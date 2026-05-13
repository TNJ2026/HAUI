# 构建与运行

> 本文档面向首次拉取本仓库的开发者。配合 [`RELEASE.md`](RELEASE.md) 了解发布签名流程。

---

## 1. 环境矩阵

| 项 | 版本 / 说明 |
| --- | --- |
| JDK | 17（项目 `compileOptions` 锁定） |
| Kotlin | 2.3.21（见 `libs.versions.toml`） |
| Android Gradle Plugin | 9.2.1 |
| Android Studio | Ladybug Feature Drop 或更新 |
| Android SDK | compileSdk **37** / minSdk **31** / targetSdk **36** |
| Gradle | Wrapper 内置（无需手动安装） |

> macOS / Linux / Windows 均支持；Windows 用 `gradlew.bat`，其余命令一致。

---

## 2. 首次设置

```bash
git clone https://github.com/TNJ2026/HAUI.git
cd HAUI

cp local.properties.example local.properties
# 编辑 local.properties，写入：
# sdk.dir=/Users/<you>/Library/Android/sdk    （macOS）
# sdk.dir=/home/<you>/Android/Sdk             （Linux）
# sdk.dir=C\:\\Users\\<you>\\AppData\\Local\\Android\\Sdk  （Windows）
```

如果有 Android Studio，打开项目时会自动生成 `local.properties`，可跳过这一步。

---

## 3. 常用 Gradle 命令

### 3.1 构建

```bash
./gradlew :app:assembleDebug      # Debug APK → app/build/outputs/apk/debug/
./gradlew :app:assembleRelease    # Release APK（需签名配置，见 RELEASE.md）
./gradlew build                   # 全模块构建 + 测试
./gradlew clean                   # 清理所有 build 目录
```

### 3.2 安装到设备

```bash
./gradlew :app:installDebug       # 安装 Debug 到当前 adb 设备
adb shell am start -n ai.tnj.haui/.MainActivity
```

### 3.3 测试

```bash
./gradlew test                    # 所有模块的单元测试
./gradlew :core:network:test      # 单模块
./gradlew :core:data:test
./gradlew :feature:home:test
```

测试框架：**Kotest 6** + **Turbine** + **MockWebServer**。
`testOptions { suites { } }` 已配置 JUnit Platform。

### 3.4 代码检查

```bash
./gradlew lint                    # Android Lint（每个模块）
./gradlew :app:lintDebug          # 仅 app 模块 Debug 配置
```

### 3.5 依赖图

```bash
./gradlew :app:dependencies                            # app 模块依赖树
./gradlew :feature:home:dependencies --configuration releaseRuntimeClasspath
```

---

## 4. 构建产物

| 类型 | 位置 |
| --- | --- |
| Debug APK | `app/build/outputs/apk/debug/app-debug.apk` |
| Release APK | `app/build/outputs/apk/release/app-release.apk` |
| Lint 报告 | `app/build/reports/lint-results-*.html` |
| 测试报告 | `<module>/build/reports/tests/` |
| ProGuard 映射 | `app/build/outputs/mapping/release/mapping.txt` |

> ⚠️ Release 构建当前仍使用 debug 签名 keystore（见 [`RELEASE.md`](RELEASE.md)）。

---

## 5. 模块构建顺序

Gradle 会自动按依赖顺序构建：

```
:core:model
:core:utils
:core:network          ← 依赖 model, utils
:core:designsystem
:core:ui               ← 依赖 designsystem
:core:data             ← 依赖 model, utils, network
:feature:home         ← 依赖 core:*
:app                   ← 依赖 feature:home + core:*
```

修改 `:core:model` 会触发 `:core:data` / `:core:network` / `:feature:home` / `:app` 全部重建。

---

## 6. Compose Preview / 实机调试

### Compose Preview

各 `@Composable` 标 `@Preview` 即可在 Android Studio 右侧渲染。需要 `core:designsystem` 的 `HAUITheme { }` 包裹才能拿到正确色板。

### 实机日志

```bash
adb logcat -s "HAUI:*" "ChatViewModel:*" "ChatRunsHandler:*"
```

`LogUtil` 默认 TAG 见各调用点；Release 构建不输出。

---

## 7. 常见构建问题

### 7.1 `Could not find ...` 依赖解析失败

- 确认网络可访问 `mavenCentral()` 与 `google()`。
- 如果在公司网络，配置 `~/.gradle/gradle.properties` 加 `systemProp.https.proxyHost=...`。

### 7.2 KSP / Hilt 错误

```
error: [Dagger/MissingBinding] ... cannot be provided ...
```

- 检查目标模块的 `build.gradle.kts` 是否 `ksp(libs.hilt.compiler)`。
- `@Inject constructor` 是否漏写。
- DataModule / DatabaseModule 是否声明了相应 `@Provides`。

### 7.3 Room schema 错误

```
Schema export directory is not provided to the annotation processor
```

- 当前 `exportSchema` 开启但未指定输出目录；可在 `core/data/build.gradle.kts` `defaultConfig` 中加：
  ```kotlin
  ksp { arg("room.schemaLocation", "$projectDir/schemas") }
  ```

### 7.4 `Unresolved reference: huai`

`feature/home/build.gradle.kts:9` 的 namespace 是 `ai.tnj.huai.feature.home`（**huai 拼写**）。修复需同步包路径，暂未处理。

### 7.5 SDK 找不到

```
SDK location not found. Define location with sdk.dir in the local.properties
```

参见 [§2 首次设置](#2-首次设置)。

---

## 8. CI 构建

`.github/workflows/android.yml` 在 push tag `v*` 时触发：

1. checkout
2. setup-java 17（temurin）
3. `./gradlew assembleRelease`
4. `r0adkll/sign-android-release@v1` 用 GitHub Secrets 签名
5. 上传 APK 到 GitHub Release

CI secrets 配置见 [`RELEASE.md`](RELEASE.md)。

---

## 9. 性能调优（可选）

`gradle.properties` 已配置：

```properties
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
org.gradle.parallel=true
org.gradle.caching=true
```

如果 IDE 同步慢：
- 关闭未编辑模块的 sync（File → Project Structure）
- `--offline` 模式跳过网络（前提：依赖已缓存）

```bash
./gradlew :app:assembleDebug --offline
```
