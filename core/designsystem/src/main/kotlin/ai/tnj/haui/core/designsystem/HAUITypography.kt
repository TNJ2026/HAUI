package ai.tnj.haui.core.designsystem

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import ai.tnj.haui.core.designsystem.R
import androidx.compose.ui.text.font.FontStyle

val SpaceGroteskFontFamily = FontFamily(
    Font(R.font.spacegrotesk_light, weight = FontWeight.Light),
    Font(R.font.spacegrotesk_medium, weight = FontWeight.Medium),
    Font(R.font.spacegrotesk_semibold, weight = FontWeight.SemiBold),
    Font(R.font.spacegrotesk_bold, weight = FontWeight.Bold),
)

val InterFontFamily = FontFamily(
    Font(R.font.inter_light, weight = FontWeight.Light, style = FontStyle.Normal),
    Font(R.font.inter_regular, weight = FontWeight.Normal, style = FontStyle.Normal),
    Font(R.font.inter_medium, weight = FontWeight.Medium, style = FontStyle.Normal),
    Font(R.font.inter_semibold, weight = FontWeight.SemiBold, style = FontStyle.Normal),
)

val JetBrainsMonoFontFamily = FontFamily(
    Font(R.font.jetbrains_mono_bold, weight = FontWeight.Bold, style = FontStyle.Normal),
    Font(R.font.jetbrains_mono_semibold, weight = FontWeight.SemiBold, style = FontStyle.Normal),
    Font(R.font.jetbrains_mono, weight = FontWeight.Normal, style = FontStyle.Normal),
    Font(R.font.jetbrains_mono_italic, weight = FontWeight.Normal, style = FontStyle.Italic),
)

val HAUITypography = Typography(
    displayLarge = TextStyle(
        fontFamily = JetBrainsMonoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 28.sp,
    ),
    displayMedium = TextStyle(
        fontFamily = JetBrainsMonoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 24.sp,
    ),
    displaySmall = TextStyle(
        fontFamily = JetBrainsMonoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 22.sp,
    ),
    // headline-lg: 24px / 600 / -0.02em / 32px line height
    headlineLarge = TextStyle(
        fontFamily = SpaceGroteskFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.02).em,
    ),
    // headline-md: 18px / 500 / 0em / 24px line height
    headlineMedium = TextStyle(
        fontFamily = SpaceGroteskFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.em,
    ),
    // body-md: 14px / 400 / 0.01em / 20px line height
    bodyMedium = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.01.em,
    ),
    // body-sm: 12px / 400 / 0.01em / 18px line height
    bodySmall = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.01.em,
    ),
    labelLarge = TextStyle(
        fontFamily = SpaceGroteskFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.1.em,
    ),
    // label-caps: 11px / 700 / 0.1em tracking / 16px line height
    labelMedium = TextStyle(
        fontFamily = SpaceGroteskFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.1.em,
    ),
    // code: 13px / 400 / 0em / 20px line height
    labelSmall = TextStyle(
        fontFamily = JetBrainsMonoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.em,
    )
)
