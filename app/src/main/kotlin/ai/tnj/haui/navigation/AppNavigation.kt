package ai.tnj.haui.navigation

import ai.tnj.haui.core.ui.navigation.HAUIRoutes
import ai.tnj.haui.feature.home.navigation.homeScreen
import ai.tnj.haui.ui.SplashScreen
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = HAUIRoutes.SPLASH,
    ) {
        composable(HAUIRoutes.SPLASH) {
            SplashScreen {
                navController.navigate(HAUIRoutes.HOME) {
                    popUpTo(HAUIRoutes.SPLASH) { inclusive = true }
                }
            }
        }

        homeScreen()
    }
}
