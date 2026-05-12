package ai.tnj.haui.core.designsystem

import androidx.compose.ui.graphics.Color

/**
 * HAUI Design System colors.
 * Contains both Dark (Retro-Tech) and Light (Technical Blueprint) palettes.
 */
object HAUIColors {

    // Dark Mode Palette (Retro-Tech)
    object Dark {
        val Primary = Color(0xFF6FEEE1)
        val OnPrimary = Color(0xFF003733)
        val PrimaryContainer = Color(0xFF4FD1C5)
        val OnPrimaryContainer = Color(0xFF005750)
        val InversePrimary = Color(0xFF006A63)

        val Secondary = Color(0xFFABCEC5)
        val OnSecondary = Color(0xFF153630)
        val SecondaryContainer = Color(0xFF2D4D46)
        val OnSecondaryContainer = Color(0xFF9ABCB4)

        val Tertiary = Color(0xFFFFCECA)
        val OnTertiary = Color(0xFF68000A)
        val TertiaryContainer = Color(0xFFFFA7A0)
        val OnTertiaryContainer = Color(0xFF9E0015)

        val Error = Color(0xFFE53E3E)
        val OnError = Color(0xFF690005)
        val ErrorContainer = Color(0xFF93000A)
        val OnErrorContainer = Color(0xFFFFDAD6)

        val Background = Color(0xFF071610)
        val OnBackground = Color(0xFFD4E7DD)
        val Surface = Color(0xFF071610)
        val OnSurface = Color(0xFFD4E7DD)
        val SurfaceVariant = Color(0xFF283831)
        val OnSurfaceVariant = Color(0xFFBBC9C7)
        val SurfaceTint = Color(0xFF5ADACE)

        val SurfaceContainerLowest = Color(0xFF03110B)
        val SurfaceContainerLow = Color(0xFF0F1F18)
        val SurfaceContainer = Color(0xFF13231C)
        val SurfaceContainerHigh = Color(0xFF1D2D27)
        val SurfaceContainerHighest = Color(0xFF283831)

        val Outline = Color(0xFF869491)
        val OutlineVariant = Color(0xFF3C4947)

        val InverseSurface = Color(0xFFD4E7DD)
        val InverseOnSurface = Color(0xFF24342D)
    }

    // Light Mode Palette (Technical Blueprint)
    object Light {
        val Primary = Color(0xFF004AC6)
        val OnPrimary = Color(0xFFFFFFFF)
        val PrimaryContainer = Color(0xFF2563EB)
        val OnPrimaryContainer = Color(0xFFEEEFFF)
        val InversePrimary = Color(0xFFB4C5FF)

        val Secondary = Color(0xFF3755C3)
        val OnSecondary = Color(0xFFFFFFFF)
        val SecondaryContainer = Color(0xFF708CFD)
        val OnSecondaryContainer = Color(0xFF00217A)

        val Tertiary = Color(0xFF4E565D)
        val OnTertiary = Color(0xFFFFFFFF)
        val TertiaryContainer = Color(0xFF676E76)
        val OnTertiaryContainer = Color(0xFFEAF1FA)

        val Error = Color(0xFFBA1A1A)
        val OnError = Color(0xFFFFFFFF)
        val ErrorContainer = Color(0xFFFFDAD6)
        val OnErrorContainer = Color(0xFF93000A)

        val Background = Color(0xFFF8F9FF)
        val OnBackground = Color(0xFF0B1C30)
        val Surface = Color(0xFFF8F9FF)
        val OnSurface = Color(0xFF0B1C30)
        val SurfaceVariant = Color(0xFFD3E4FE)
        val OnSurfaceVariant = Color(0xFF434655)
        val SurfaceTint = Color(0xFF0053DB)

        val SurfaceContainerLowest = Color(0xFFFFFFFF)
        val SurfaceContainerLow = Color(0xFFEFF4FF)
        val SurfaceContainer = Color(0xFFE5EEFF)
        val SurfaceContainerHigh = Color(0xFFDCE9FF)
        val SurfaceContainerHighest = Color(0xFFD3E4FE)

        val Outline = Color(0xFF737686)
        val OutlineVariant = Color(0xFFC3C6D7)

        val InverseSurface = Color(0xFF213145)
        val InverseOnSurface = Color(0xFFEAF1FF)
    }
}
