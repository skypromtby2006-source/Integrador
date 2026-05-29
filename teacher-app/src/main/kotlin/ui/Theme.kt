package ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ── Tokens de color (mismos que m3-theme.css) ─────────────────────────────────
object DColors {
    val Primary             = Color(0xFF40798C)
    val OnPrimary           = Color(0xFFFFFFFF)
    val PrimaryContainer    = Color(0xFFC2E8FB)
    val OnPrimaryContainer  = Color(0xFF001F2A)

    val Secondary           = Color(0xFF82008C)
    val OnSecondary         = Color(0xFFFFFFFF)
    val SecondaryContainer  = Color(0xFFFFD6FA)
    val OnSecondaryContainer= Color(0xFF34003A)

    val Tertiary            = Color(0xFFD23AAD)
    val TertiaryContainer   = Color(0xFFFFD8EC)
    val OnTertiary          = Color(0xFFFFFFFF)
    val OnTertiaryContainer = Color(0xFF380025)

    val Error               = Color(0xFFBA1A1A)
    val ErrorContainer      = Color(0xFFFFDAD6)
    val OnError             = Color(0xFFFFFFFF)
    val OnErrorContainer    = Color(0xFF410002)

    // Fondo oscuro para desktop (más cómodo para maestros que trabajan largas horas)
    val Background          = Color(0xFF12151A)
    val Surface             = Color(0xFF1A1D22)
    val SurfaceContainer    = Color(0xFF22262D)
    val SurfaceContainerHigh= Color(0xFF2A2E36)
    val OnSurface           = Color(0xFFE2E4E8)
    val OnSurfaceVariant    = Color(0xFF8E959F)
    val OutlineVariant      = Color(0xFF2E333B)
    val Outline             = Color(0xFF52585F)

    val Mint                = Color(0xFF59FFCC)
    val Success             = Color(0xFF1B8A5A)
    val SuccessContainer    = Color(0xFF003920)
    val OnSuccessContainer  = Color(0xFFB7F0D6)

    // Sidebar
    val Sidebar             = Color(0xFF161920)
    val SidebarActive       = Color(0xFF1F2A30)
}

// ── Color scheme de Material 3 ────────────────────────────────────────────────
private val darkColorScheme = darkColorScheme(
    primary              = DColors.Primary,
    onPrimary            = DColors.OnPrimary,
    primaryContainer     = DColors.PrimaryContainer,
    onPrimaryContainer   = DColors.OnPrimaryContainer,
    secondary            = DColors.Secondary,
    onSecondary          = DColors.OnSecondary,
    secondaryContainer   = DColors.SecondaryContainer,
    onSecondaryContainer = DColors.OnSecondaryContainer,
    tertiary             = DColors.Tertiary,
    onTertiary           = DColors.OnTertiary,
    tertiaryContainer    = DColors.TertiaryContainer,
    onTertiaryContainer  = DColors.OnTertiaryContainer,
    error                = DColors.Error,
    errorContainer       = DColors.ErrorContainer,
    onError              = DColors.OnError,
    onErrorContainer     = DColors.OnErrorContainer,
    background           = DColors.Background,
    onBackground         = DColors.OnSurface,
    surface              = DColors.Surface,
    onSurface            = DColors.OnSurface,
    onSurfaceVariant     = DColors.OnSurfaceVariant,
    outline              = DColors.Outline,
    outlineVariant       = DColors.OutlineVariant
)

@Composable
fun DidactaiTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme,
        content     = content
    )
}
