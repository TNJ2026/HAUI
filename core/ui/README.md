# `:core:ui`

> 可复用 Compose 组件、路由常量、复古终端栅格背景。

---

## 职责

- 跨 feature 复用的 Compose 组件（`BlinkingCursor`、`PulseDot`、`LoadingOverlay`）。
- 通用 Modifier：`terminalCornerBorders`、`retroTerminalBackground`。
- 路由常量定义：`HAUIRoutes`（让 `:app` 与 `:feature:*` 共享）。

## 依赖

- `:core:designsystem`
- `androidx.compose.material3`

## 关键文件

| 文件 | 内容 |
| --- | --- |
| `CommonUi.kt` | `BlinkingCursor`、`PulseDot`、`terminalCornerBorders` Modifier、`retroTerminalBackground` Modifier |
| `LoadingOverlay.kt` | 半透明加载浮层 |
| `navigation/HAUIRoutes.kt` | `SPLASH`、`HOME` 等路由名常量 |

## 使用方式

```kotlin
Box(
    modifier = Modifier
        .fillMaxSize()
        .retroTerminalBackground(),  // CRT 栅格背景
) {
    Card(
        modifier = Modifier.terminalCornerBorders(),  // L 型装饰角
    ) {
        Text("> READY")
        BlinkingCursor()
    }
}
```

## 使用约定

- **可复用** 优先放这里，不要在 feature 内重复实现。
- 不依赖任何业务数据 / ViewModel（保持纯组件）。
- 颜色走 `MaterialTheme.colorScheme`，从 `:core:designsystem` 来。
- 新增组件须可 `@Preview`。
- 不要直接 `import androidx.compose.material3.*` 后硬编码颜色 —— 用 `colorScheme` 派生。

## 路由

```kotlin
object HAUIRoutes {
    const val SPLASH = "splash"
    const val HOME = "home"
}
```

新增顶层路由时在此添加，避免 string 散落。
