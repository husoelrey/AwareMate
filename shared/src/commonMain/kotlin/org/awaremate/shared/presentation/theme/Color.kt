package org.awaremate.shared.presentation.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Primary - Deep Forest & Fresh Mint
val ForestGreenLight = Color(0xFF2D6A4F)
val ForestGreenOnLight = Color(0xFFFFFFFF)
val ForestGreenContainerLight = Color(0xFFD8F3DC)
val ForestGreenOnContainerLight = Color(0xFF081C15)

val MintGreenDark = Color(0xFF74C69D)
val MintGreenOnDark = Color(0xFF081C15)
val MintGreenContainerDark = Color(0xFF1B4332)
val MintGreenOnContainerDark = Color(0xFFD8F3DC)

// Secondary - Warm Sage & Soft Moss
val WarmSageLight = Color(0xFF52796F)
val WarmSageOnLight = Color(0xFFFFFFFF)
val WarmSageContainerLight = Color(0xFFB7E4C7)
val WarmSageOnContainerLight = Color(0xFF1B4332)

val SoftSageDark = Color(0xFF95D5B2)
val SoftSageOnDark = Color(0xFF1B4332)
val SoftSageContainerDark = Color(0xFF2D6A4F)
val SoftSageOnContainerDark = Color(0xFFD8F3DC)

// Tertiary - Sunlight Amber & Warm Glow
val SunlightAmberLight = Color(0xFFD97706)
val SunlightAmberOnLight = Color(0xFFFFFFFF)
val SunlightAmberContainerLight = Color(0xFFFEF3C7)
val SunlightAmberOnContainerLight = Color(0xFF78350F)

val WarmGlowDark = Color(0xFFFBBF24)
val WarmGlowOnDark = Color(0xFF451A03)
val WarmGlowContainerDark = Color(0xFF78350F)
val WarmGlowOnContainerDark = Color(0xFFFEF3C7)

// Error - Gentle Coral (Non-punitive, anti-shame)
val GentleCoralLight = Color(0xFFE07A5F)
val GentleCoralOnLight = Color(0xFFFFFFFF)
val GentleCoralContainerLight = Color(0xFFFDE8E1)
val GentleCoralOnContainerLight = Color(0xFF5C2018)

val GentleCoralDark = Color(0xFFF4A261)
val GentleCoralOnDark = Color(0xFF431B10)
val GentleCoralContainerDark = Color(0xFF7A301E)
val GentleCoralOnContainerDark = Color(0xFFFDE8E1)

// Neutrals - Warm Nature Off-White & Deep Night Garden Slate
val NatureBackgroundLight = Color(0xFFFBFBFA)
val NatureOnBackgroundLight = Color(0xFF1A1C1A)
val NatureSurfaceLight = Color(0xFFF4F6F0)
val NatureOnSurfaceLight = Color(0xFF1A1C1A)
val NatureSurfaceVariantLight = Color(0xFFE5E9E2)
val NatureOnSurfaceVariantLight = Color(0xFF414842)
val NatureOutlineLight = Color(0xFF717871)

val NatureBackgroundDark = Color(0xFF121614)
val NatureOnBackgroundDark = Color(0xFFE2E3DF)
val NatureSurfaceDark = Color(0xFF1A201D)
val NatureOnSurfaceDark = Color(0xFFE2E3DF)
val NatureSurfaceVariantDark = Color(0xFF2D3732)
val NatureOnSurfaceVariantDark = Color(0xFFC1C8C1)
val NatureOutlineDark = Color(0xFF8B938B)

// Companion Category Colors
val HappinessColor = Color(0xFFEAB308) // Warm Golden Sunflower
val EnergyColor = Color(0xFFF97316)    // Energizing Tangerine
val WisdomColor = Color(0xFF3B82F6)    // Calm Sky Blue
val CreativityColor = Color(0xFF8B5CF6) // Gentle Lavender Violet

val AwareMateLightColorScheme = lightColorScheme(
    primary = ForestGreenLight,
    onPrimary = ForestGreenOnLight,
    primaryContainer = ForestGreenContainerLight,
    onPrimaryContainer = ForestGreenOnContainerLight,
    secondary = WarmSageLight,
    onSecondary = WarmSageOnLight,
    secondaryContainer = WarmSageContainerLight,
    onSecondaryContainer = WarmSageOnContainerLight,
    tertiary = SunlightAmberLight,
    onTertiary = SunlightAmberOnLight,
    tertiaryContainer = SunlightAmberContainerLight,
    onTertiaryContainer = SunlightAmberOnContainerLight,
    error = GentleCoralLight,
    onError = GentleCoralOnLight,
    errorContainer = GentleCoralContainerLight,
    onErrorContainer = GentleCoralOnContainerLight,
    background = NatureBackgroundLight,
    onBackground = NatureOnBackgroundLight,
    surface = NatureSurfaceLight,
    onSurface = NatureOnSurfaceLight,
    surfaceVariant = NatureSurfaceVariantLight,
    onSurfaceVariant = NatureOnSurfaceVariantLight,
    outline = NatureOutlineLight
)

val AwareMateDarkColorScheme = darkColorScheme(
    primary = MintGreenDark,
    onPrimary = MintGreenOnDark,
    primaryContainer = MintGreenContainerDark,
    onPrimaryContainer = MintGreenOnContainerDark,
    secondary = SoftSageDark,
    onSecondary = SoftSageOnDark,
    secondaryContainer = SoftSageContainerDark,
    onSecondaryContainer = SoftSageOnContainerDark,
    tertiary = WarmGlowDark,
    onTertiary = WarmGlowOnDark,
    tertiaryContainer = WarmGlowContainerDark,
    onTertiaryContainer = WarmGlowOnContainerDark,
    error = GentleCoralDark,
    onError = GentleCoralOnDark,
    errorContainer = GentleCoralContainerDark,
    onErrorContainer = GentleCoralOnContainerDark,
    background = NatureBackgroundDark,
    onBackground = NatureOnBackgroundDark,
    surface = NatureSurfaceDark,
    onSurface = NatureOnSurfaceDark,
    surfaceVariant = NatureSurfaceVariantDark,
    onSurfaceVariant = NatureOnSurfaceVariantDark,
    outline = NatureOutlineDark
)
