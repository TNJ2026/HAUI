package ai.tnj.haui.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val HAUIDarkColorScheme = darkColorScheme(
    primary = HAUIColors.Dark.Primary,
    onPrimary = HAUIColors.Dark.OnPrimary,
    primaryContainer = HAUIColors.Dark.PrimaryContainer,
    onPrimaryContainer = HAUIColors.Dark.OnPrimaryContainer,
    secondary = HAUIColors.Dark.Secondary,
    onSecondary = HAUIColors.Dark.OnSecondary,
    secondaryContainer = HAUIColors.Dark.SecondaryContainer,
    onSecondaryContainer = HAUIColors.Dark.OnSecondaryContainer,
    tertiary = HAUIColors.Dark.Tertiary,
    onTertiary = HAUIColors.Dark.OnTertiary,
    tertiaryContainer = HAUIColors.Dark.TertiaryContainer,
    onTertiaryContainer = HAUIColors.Dark.OnTertiaryContainer,
    error = HAUIColors.Dark.Error,
    onError = HAUIColors.Dark.OnError,
    errorContainer = HAUIColors.Dark.ErrorContainer,
    onErrorContainer = HAUIColors.Dark.OnErrorContainer,
    background = HAUIColors.Dark.Background,
    onBackground = HAUIColors.Dark.OnBackground,
    surface = HAUIColors.Dark.Surface,
    onSurface = HAUIColors.Dark.OnSurface,
    surfaceVariant = HAUIColors.Dark.SurfaceVariant,
    onSurfaceVariant = HAUIColors.Dark.OnSurfaceVariant,
    surfaceContainer = HAUIColors.Dark.SurfaceContainer,
    surfaceContainerHigh = HAUIColors.Dark.SurfaceContainerHigh,
    surfaceContainerHighest = HAUIColors.Dark.SurfaceContainerHighest,
    surfaceContainerLow = HAUIColors.Dark.SurfaceContainerLow,
    surfaceContainerLowest = HAUIColors.Dark.SurfaceContainerLowest,
    surfaceTint = HAUIColors.Dark.SurfaceTint,
    outline = HAUIColors.Dark.Outline,
    outlineVariant = HAUIColors.Dark.OutlineVariant,
    inverseSurface = HAUIColors.Dark.InverseSurface,
    inverseOnSurface = HAUIColors.Dark.InverseOnSurface,
    inversePrimary = HAUIColors.Dark.InversePrimary,
)

private val HAUILightColorScheme = lightColorScheme(
    primary = HAUIColors.Light.Primary,
    onPrimary = HAUIColors.Light.OnPrimary,
    primaryContainer = HAUIColors.Light.PrimaryContainer,
    onPrimaryContainer = HAUIColors.Light.OnPrimaryContainer,
    secondary = HAUIColors.Light.Secondary,
    onSecondary = HAUIColors.Light.OnSecondary,
    secondaryContainer = HAUIColors.Light.SecondaryContainer,
    onSecondaryContainer = HAUIColors.Light.OnSecondaryContainer,
    tertiary = HAUIColors.Light.Tertiary,
    onTertiary = HAUIColors.Light.OnTertiary,
    tertiaryContainer = HAUIColors.Light.TertiaryContainer,
    onTertiaryContainer = HAUIColors.Light.OnTertiaryContainer,
    error = HAUIColors.Light.Error,
    onError = HAUIColors.Light.OnError,
    errorContainer = HAUIColors.Light.ErrorContainer,
    onErrorContainer = HAUIColors.Light.OnErrorContainer,
    background = HAUIColors.Light.Background,
    onBackground = HAUIColors.Light.OnBackground,
    surface = HAUIColors.Light.Surface,
    onSurface = HAUIColors.Light.OnSurface,
    surfaceVariant = HAUIColors.Light.SurfaceVariant,
    onSurfaceVariant = HAUIColors.Light.OnSurfaceVariant,
    surfaceContainer = HAUIColors.Light.SurfaceContainer,
    surfaceContainerHigh = HAUIColors.Light.SurfaceContainerHigh,
    surfaceContainerHighest = HAUIColors.Light.SurfaceContainerHighest,
    surfaceContainerLow = HAUIColors.Light.SurfaceContainerLow,
    surfaceContainerLowest = HAUIColors.Light.SurfaceContainerLowest,
    surfaceTint = HAUIColors.Light.SurfaceTint,
    outline = HAUIColors.Light.Outline,
    outlineVariant = HAUIColors.Light.OutlineVariant,
    inverseSurface = HAUIColors.Light.InverseSurface,
    inverseOnSurface = HAUIColors.Light.InverseOnSurface,
    inversePrimary = HAUIColors.Light.InversePrimary,
)

@Composable
fun HAUITheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) HAUIDarkColorScheme else HAUILightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = HAUITypography,
        shapes = HAUIShapes,
        content = content,
    )
}
