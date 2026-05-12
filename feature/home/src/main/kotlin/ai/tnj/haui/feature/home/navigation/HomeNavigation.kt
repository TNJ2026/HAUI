package ai.tnj.haui.feature.home.navigation

import ai.tnj.haui.core.ui.navigation.HAUIRoutes
import ai.tnj.haui.feature.home.ui.HomeScreen
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

fun NavGraphBuilder.homeScreen() {
    composable(HAUIRoutes.HOME) {
        HomeScreen()
    }
}
