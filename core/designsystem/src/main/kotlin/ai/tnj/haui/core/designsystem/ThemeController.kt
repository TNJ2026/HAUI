package ai.tnj.haui.core.designsystem

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Process-level theme switch. Wired by `MainActivity` to drive [HAUITheme] and
 * mutated by settings UI. For persistence, back this with DataStore later.
 */
object ThemeController {

    private val _isDark = MutableStateFlow(true)
    val isDark: StateFlow<Boolean> = _isDark.asStateFlow()

    fun setDark(dark: Boolean) {
        _isDark.value = dark
    }
}
