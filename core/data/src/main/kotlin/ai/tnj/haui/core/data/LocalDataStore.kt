package ai.tnj.haui.core.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "HAUI_Data_Store")

@Singleton
class LocalDataStore @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val dataStore = context.dataStore

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        private val HOST_KEY = stringPreferencesKey("server.host")
        private val PORT_KEY = stringPreferencesKey("server.port")
        private val API_KEY_KEY = stringPreferencesKey("server.api_key")
        private val IS_DARK_THEME_KEY = booleanPreferencesKey("ui.is_dark_theme")
        private val SHOW_TOOL_BUBBLE_KEY = booleanPreferencesKey("ui.show_tool_bubble")
        private val CHAT_PROTOCOL_KEY = stringPreferencesKey("chat.protocol")
        private const val DEFAULT_IS_DARK_THEME = true
        private const val DEFAULT_SHOW_TOOL_BUBBLE = false
        const val DEFAULT_CHAT_PROTOCOL = "RUN"
    }

    val isDarkTheme: StateFlow<Boolean> = dataStore.data
        .map { it[IS_DARK_THEME_KEY] ?: DEFAULT_IS_DARK_THEME }
        .stateIn(scope, SharingStarted.Eagerly, DEFAULT_IS_DARK_THEME)

    val showToolBubble: StateFlow<Boolean> = dataStore.data
        .map { it[SHOW_TOOL_BUBBLE_KEY] ?: DEFAULT_SHOW_TOOL_BUBBLE }
        .stateIn(scope, SharingStarted.Eagerly, DEFAULT_SHOW_TOOL_BUBBLE)

    val chatProtocol: StateFlow<String> = dataStore.data
        .map { it[CHAT_PROTOCOL_KEY] ?: DEFAULT_CHAT_PROTOCOL }
        .stateIn(scope, SharingStarted.Eagerly, DEFAULT_CHAT_PROTOCOL)


    /**
     * Reads the persisted dark-theme flag directly from disk, bypassing the
     * [isDarkTheme] StateFlow's `Eagerly` default. Used at app start to seed
     * `ThemeController` before the first frame, avoiding a theme flicker.
     */
    suspend fun initialIsDarkTheme(): Boolean =
        dataStore.data.first()[IS_DARK_THEME_KEY] ?: DEFAULT_IS_DARK_THEME

    suspend fun getServerConfig(): Triple<String, String, String> {
        val prefs = dataStore.data.first()
        return Triple(prefs[HOST_KEY] ?: "", prefs[PORT_KEY] ?: "", prefs[API_KEY_KEY] ?: "")
    }

    suspend fun saveServerConfig(host: String, port: String, apiKey: String) {
        dataStore.edit { prefs ->
            prefs[HOST_KEY] = host.trim()
            prefs[PORT_KEY] = port.trim()
            prefs[API_KEY_KEY] = apiKey.trim()
        }
    }

    suspend fun setDarkTheme(isDark: Boolean) {
        dataStore.edit { prefs ->
            prefs[IS_DARK_THEME_KEY] = isDark
        }
    }

    suspend fun setShowToolBubble(show: Boolean) {
        dataStore.edit { prefs ->
            prefs[SHOW_TOOL_BUBBLE_KEY] = show
        }
    }

    suspend fun setChatProtocol(protocol: String) {
        dataStore.edit { prefs ->
            prefs[CHAT_PROTOCOL_KEY] = protocol
        }
    }

}
