# `:core:designsystem`

> Material 3 主题、色板、字体、形状、动态主题切换。**不包含组件**（组件在 `:core:ui`）。

---

## 职责

- 定义 `HAUITheme { content }` 包装器：色板 + 字体 + 系统栏对比度。
- 维护全局主题状态：`ThemeController`（`object` 单例 + `MutableStateFlow<Boolean>`）。
- 字体：Inter 主字体 + Courier New 终端字体。
- 形状：方形为主（贴合终端美学）。

## 依赖

- `androidx.compose.material3`
- 字体资源在 `src/main/res/font/`

## 关键文件

| 文件 | 内容 |
| --- | --- |
| `HAUITheme.kt` | `@Composable HAUITheme { ... }` 入口 |
| `HAUIColors.kt` | 浅色 / 深色色板（终端青 + 高对比黑底） |
| `HAUITypography.kt` | Inter + Courier New 的 Material 3 `Typography` |
| `HAUIShapes.kt` | 圆角 ≈ 0 的方形 `Shapes` |
| `HAUIProgressIndicators.kt` | 方形进度指示器（替代默认圆形） |
| `ThemeController.kt` | `object` + `MutableStateFlow<Boolean>` 的全局暗色开关 |

## 使用方式

```kotlin
setContent {
    HAUITheme {
        AppNavigation()
    }
}

// 切换主题（在 SettingsViewModel 中）
ThemeController.setDark(isDark = true)
```

## 设计基调

- **复古 CRT 终端**：青色 primary、黑底 background、橘色 error。
- **方形**：所有形状圆角接近 0；圆形进度条用 `HAUIProgressIndicators` 替代。
- **Monospace 强调**：标签、错误码、命令行用 Courier New；正文用 Inter。
- **首帧无闪烁**：`HAUIApplication.onCreate()` 用 `runBlocking` 同步预读暗色偏好。

## 使用约定

- **不要** 把 Compose 组件放在此模块（除主题相关的小工具如 `HAUIProgressIndicators`）。
- **不要** 在业务模块硬编码颜色（`Color(0xFF...)`），永远走 `MaterialTheme.colorScheme`。
- 新增字体：放 `res/font/` + 在 `HAUITypography` 注册 + 在此 README 加一行说明。
- 主题热切换通过 `ThemeController`，**不要** 自己写 `mutableStateOf`。
