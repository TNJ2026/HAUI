# 发布流程

> 当前状态：**`app/build.gradle.kts` 未配置 `signingConfigs`**，Release APK 默认仍由 Android Gradle 用 debug keystore 签名。本文档说明如何切换到正式签名以及通过 GitHub Actions 自动发布。

---

## 1. 准备正式 Keystore

```bash
keytool -genkey -v \
  -keystore haui-release.keystore \
  -alias haui \
  -keyalg RSA -keysize 4096 \
  -validity 25000
```

按提示填写 CN/OU/O 等信息。最终产物：

| 文件 / 凭据 | 用途 |
| --- | --- |
| `haui-release.keystore` | 密钥库（**勿入库**） |
| Keystore 密码 | 解锁 keystore |
| Key alias（`haui`） | 别名 |
| Key 密码 | 别名密码 |

> **强烈建议**：把 keystore 同时备份在 1Password / Bitwarden / 团队保险柜，**丢失即无法更新应用**（包名 + 签名是 Android 应用身份）。

---

## 2. 本地签名（手动）

### 2.1 在 `~/.gradle/gradle.properties`（**不要** 在仓库内）写入：

```properties
HAUI_KEYSTORE_FILE=/Users/<you>/keys/haui-release.keystore
HAUI_KEYSTORE_PASSWORD=<store-password>
HAUI_KEY_ALIAS=haui
HAUI_KEY_PASSWORD=<key-password>
```

### 2.2 在 `app/build.gradle.kts` 增加（**需要单独提交**）：

```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file(providers.gradleProperty("HAUI_KEYSTORE_FILE").get())
            storePassword = providers.gradleProperty("HAUI_KEYSTORE_PASSWORD").get()
            keyAlias = providers.gradleProperty("HAUI_KEY_ALIAS").get()
            keyPassword = providers.gradleProperty("HAUI_KEY_PASSWORD").get()
        }
    }
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            // ... 现有 minify / proguard 配置保持不变
        }
    }
}
```

### 2.3 构建

```bash
./gradlew :app:assembleRelease
# 产物：app/build/outputs/apk/release/app-release.apk
```

验证签名：

```bash
~/Library/Android/sdk/build-tools/35.0.0/apksigner verify --verbose app/build/outputs/apk/release/app-release.apk
```

---

## 3. GitHub Actions 自动发布

工作流：[`.github/workflows/android.yml`](../.github/workflows/android.yml)
触发条件：**push tag 匹配 `v*`** 或手动 `workflow_dispatch`。

### 3.1 配置 Secrets

在 Repository → Settings → Secrets and variables → Actions → New repository secret：

| Secret | 内容 |
| --- | --- |
| `SIGNING_KEYSTORE` | `base64` 编码后的 keystore 文件 |
| `SIGNING_KEY_ALIAS` | 别名（如 `haui`） |
| `SIGNING_STORE_PASSWORD` | keystore 密码 |
| `SIGNING_KEY_PASSWORD` | 别名密码 |

把 keystore 转 base64：

```bash
base64 -i haui-release.keystore | pbcopy        # macOS（已复制到剪贴板）
base64 -w 0 haui-release.keystore                # Linux（一行输出）
```

### 3.2 发布步骤

```bash
# 1. 更新 versionCode / versionName
# 编辑 app/build.gradle.kts：
#   versionCode = <n>
#   versionName = "<x.y.z>"

# 2. 同步 CHANGELOG.md
# 把 [Unreleased] 改名为 [x.y.z] - 2026-mm-dd，新增空的 [Unreleased]

# 3. 提交 & 打 tag
git add app/build.gradle.kts CHANGELOG.md
git commit -m "release: vX.Y.Z"
git tag vX.Y.Z
git push origin main --tags

# 4. CI 自动执行：
#    - assembleRelease
#    - 签名
#    - 重命名为 HAUI_vX.Y.Z.apk
#    - 上传到 GitHub Release（自动生成 release notes）
```

### 3.3 验证 CI 产物

1. 进入 Actions → 找到对应 run → 查看 Release Notes。
2. 下载 APK → `apksigner verify --print-certs HAUI_vX.Y.Z.apk`。
3. 安装到设备：`adb install -r HAUI_vX.Y.Z.apk`。

---

## 4. 版本号规则（SemVer）

| 类型 | 规则 | 举例 |
| --- | --- | --- |
| Patch | bugfix、小幅优化 | 1.0.0 → 1.0.1 |
| Minor | 新功能、向后兼容 | 1.0.1 → 1.1.0 |
| Major | 破坏性变化（Hermes 服务端契约不兼容等） | 1.x → 2.0.0 |

`versionCode` 每次发布单调递增（推荐用 `MMmmpp` 风格，例如 1.2.3 → `010203`）。

---

## 5. 回滚

GitHub Release 不能"撤销"已发布的 APK，但可以：

1. 标记 release 为 pre-release / draft
2. 删除 release（tag 仍保留）
3. 删除 tag：`git push --delete origin vX.Y.Z`（**慎用**，可能让在途的 CI / 已通知用户混乱）
4. 推一个 hotfix 版本 `vX.Y.(Z+1)`，CHANGELOG 显式说明回滚

---

## 6. 当前未完成的发布工作（路线图）

- [ ] `app/build.gradle.kts` 引入 `signingConfigs.release`
- [ ] 重新引入 `productFlavors`（`dev` / `prod`）以便发布单独走 `assembleProdRelease`
- [ ] 把 `versionCode` / `versionName` 改为通过 `versionCatalogs` 或 Gradle property 注入
- [ ] CI 增加 `./gradlew test` 作为 release 前置门槛
- [ ] CI 增加 ProGuard mapping.txt 上传到 Release（便于线上栈跟踪回溯）
- [ ] AAB（Play Store 上架）打包流程

---

## 7. 安全提示

- **不要** 把 keystore 文件或密码以任何形式提交到 Git。
- **不要** 在 PR / Issue / Slack 截图中暴露 keystore 的 SHA-1 fingerprint（同样是身份信息）。
- 团队成员离职时 **不需要** 重新生成 keystore，但应轮换访问 secrets 的权限。
- 若怀疑 keystore 已泄露：立即生成新 keystore + 提升 versionCode + 发新 major 版本，用户需重新安装。
