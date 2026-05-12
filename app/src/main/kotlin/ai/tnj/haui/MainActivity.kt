package ai.tnj.haui

import ai.tnj.haui.core.designsystem.HAUITheme
import ai.tnj.haui.core.designsystem.ThemeController
import ai.tnj.haui.navigation.AppNavigation
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDark by ThemeController.isDark.collectAsState()

            // Drive system-bar icon contrast off the active theme:
            //  - dark theme → light (white) icons
            //  - light theme → dark icons
            val view = LocalView.current
            val activity = LocalActivity.current
            SideEffect {
                val window = activity?.window ?: return@SideEffect
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = !isDark
                controller.isAppearanceLightNavigationBars = !isDark
            }

            HAUITheme(darkTheme = isDark) {
                AppNavigation()
            }
        }
    }
}
