package ai.tnj.haui.core.model

import kotlinx.serialization.json.Json

/**
 * Global Json configuration for the application.
 */
val HauiJson = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
    encodeDefaults = true
    isLenient = true
    explicitNulls = false
}
