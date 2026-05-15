package com.anatomia.app.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// M3 light scheme — from m3-theme.css
val Primary              = Color(0xFF40798C)
val OnPrimary            = Color(0xFFFFFFFF)
val PrimaryContainer     = Color(0xFFC2E8FB)
val OnPrimaryContainer   = Color(0xFF001F2A)

val Secondary            = Color(0xFF82008C)
val OnSecondary          = Color(0xFFFFFFFF)
val SecondaryContainer   = Color(0xFFFFD6FA)
val OnSecondaryContainer = Color(0xFF34003A)

val Tertiary             = Color(0xFFD23AAD)
val OnTertiary           = Color(0xFFFFFFFF)
val TertiaryContainer    = Color(0xFFFFD8EC)
val OnTertiaryContainer  = Color(0xFF380025)

val Error                = Color(0xFFBA1A1A)
val OnError              = Color(0xFFFFFFFF)
val ErrorContainer       = Color(0xFFFFDAD6)
val OnErrorContainer     = Color(0xFF410002)

val Background           = Color(0xFFF8FAFA)
val OnBackground         = Color(0xFF1A1C1D)
val Surface              = Color(0xFFF8FAFA)
val OnSurface            = Color(0xFF1A1C1D)
val SurfaceVariant       = Color(0xFFDDE3E5)
val OnSurfaceVariant     = Color(0xFF41484B)
val SurfaceContainerLowest  = Color(0xFFFFFFFF)
val SurfaceContainerLow     = Color(0xFFF2F4F4)
val SurfaceContainer        = Color(0xFFECEEEE)
val SurfaceContainerHigh    = Color(0xFFE6E8E8)
val SurfaceContainerHighest = Color(0xFFE1E3E3)

val Outline              = Color(0xFF71787B)
val OutlineVariant       = Color(0xFFC1C7C9)

// Custom tokens (CLAUDE.md spec)
val Mint                = Color(0xFF59FFCC)
val Magenta             = Color(0xFFD45FBD)
val Success             = Color(0xFF1B8A5A)
val OnSuccess           = Color(0xFFFFFFFF)
val SuccessContainer    = Color(0xFFB7F0D6)
val OnSuccessContainer  = Color(0xFF00391E)

// CSS --app-success* values (used directly for visual fidelity)
val AppSuccess              = Color(0xFF0E8C66)
val AppOnSuccess            = Color(0xFFFFFFFF)
val AppSuccessContainer     = Color(0xFF59FFCC) // = Mint
val AppOnSuccessContainer   = Color(0xFF003824)

// ── Light ColorScheme ─────────────────────────────────────────
val DidactaiLightColors = lightColorScheme(
    primary                = Color(0xFF40798C),
    onPrimary              = Color(0xFFFFFFFF),
    primaryContainer       = Color(0xFFC2E8FB),
    onPrimaryContainer     = Color(0xFF001F2A),
    secondary              = Color(0xFF82008C),
    onSecondary            = Color(0xFFFFFFFF),
    secondaryContainer     = Color(0xFFFFD6FA),
    onSecondaryContainer   = Color(0xFF34003A),
    tertiary               = Color(0xFFD23AAD),
    onTertiary             = Color(0xFFFFFFFF),
    tertiaryContainer      = Color(0xFFFFD8EC),
    onTertiaryContainer    = Color(0xFF380025),
    error                  = Color(0xFFBA1A1A),
    onError                = Color(0xFFFFFFFF),
    errorContainer         = Color(0xFFFFDAD6),
    onErrorContainer       = Color(0xFF410002),
    background             = Color(0xFFF8FAFA),
    onBackground           = Color(0xFF1A1C1D),
    surface                = Color(0xFFF8FAFA),
    onSurface              = Color(0xFF1A1C1D),
    surfaceVariant         = Color(0xFFDDE3E5),
    onSurfaceVariant       = Color(0xFF41484B),
    outline                = Color(0xFF71787B),
    outlineVariant         = Color(0xFFC1C7C9),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow    = Color(0xFFF2F4F4),
    surfaceContainer       = Color(0xFFECEEEE),
    surfaceContainerHigh   = Color(0xFFE6E8E8),
    surfaceContainerHighest= Color(0xFFE1E3E3),
    inverseSurface         = Color(0xFF2F3133),
    inverseOnSurface       = Color(0xFFF0F0F1),
    inversePrimary         = Color(0xFF8ECDE2),
)

// ── Dark ColorScheme ──────────────────────────────────────────
val DidactaiDarkColors = darkColorScheme(
    primary                = Color(0xFF8ECDE2),
    onPrimary              = Color(0xFF003547),
    primaryContainer       = Color(0xFF1F4757),
    onPrimaryContainer     = Color(0xFFC2E8FB),
    secondary              = Color(0xFFFAACFF),
    onSecondary            = Color(0xFF560062),
    secondaryContainer     = Color(0xFF650070),
    onSecondaryContainer   = Color(0xFFFFD6FA),
    tertiary               = Color(0xFFFFA9E0),
    onTertiary             = Color(0xFF5E0044),
    tertiaryContainer      = Color(0xFF84005F),
    onTertiaryContainer    = Color(0xFFFFD8EC),
    error                  = Color(0xFFFFB4AB),
    onError                = Color(0xFF690005),
    errorContainer         = Color(0xFF93000A),
    onErrorContainer       = Color(0xFFFFDAD6),
    background             = Color(0xFF111414),
    onBackground           = Color(0xFFE1E3E3),
    surface                = Color(0xFF111414),
    onSurface              = Color(0xFFE1E3E3),
    surfaceVariant         = Color(0xFF41484B),
    onSurfaceVariant       = Color(0xFFC1C7C9),
    outline                = Color(0xFF8B9194),
    outlineVariant         = Color(0xFF41484B),
    surfaceContainerLowest = Color(0xFF0B0D0E),
    surfaceContainerLow    = Color(0xFF1A1C1D),
    surfaceContainer       = Color(0xFF1E2021),
    surfaceContainerHigh   = Color(0xFF282A2B),
    surfaceContainerHighest= Color(0xFF333536),
    inverseSurface         = Color(0xFFE1E3E3),
    inverseOnSurface       = Color(0xFF2F3133),
    inversePrimary         = Color(0xFF1F5C71),
)

// ── Custom success tokens (light / dark) ──────────────────────
val SuccessLight             = Color(0xFF0E8C66)
val OnSuccessLight           = Color(0xFFFFFFFF)
val SuccessContainerLight    = Color(0xFF59FFCC)
val OnSuccessContainerLight  = Color(0xFF003824)

val SuccessDark              = Color(0xFF59FFCC)
val OnSuccessDark            = Color(0xFF003824)
val SuccessContainerDark     = Color(0xFF00513A)
val OnSuccessContainerDark   = Color(0xFF74FFD4)
