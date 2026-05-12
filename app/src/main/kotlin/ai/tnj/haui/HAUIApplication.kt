package ai.tnj.haui

import ai.tnj.haui.core.data.LocalDataStore
import ai.tnj.haui.core.designsystem.ThemeController
import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltAndroidApp
class HAUIApplication : Application() {

    @Inject lateinit var localDataStore: LocalDataStore

    override fun onCreate() {
        super.onCreate()
        // Seed the in-memory theme switch synchronously from DataStore before
        // any Activity renders. Avoids the brief light/dark flicker that would
        // otherwise occur while waiting for the async StateFlow to emit.
        ThemeController.setDark(runBlocking { localDataStore.initialIsDarkTheme() })
    }
}
