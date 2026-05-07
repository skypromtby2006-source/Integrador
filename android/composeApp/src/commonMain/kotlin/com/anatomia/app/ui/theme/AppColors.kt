package com.anatomia.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Tokens de color de la aplicación.
 *
 * ¿Por qué un object con Color en vez de usar MaterialTheme.colorScheme directamente?
 *
 * Porque el diseño de la app usa una paleta oscura específica (#0d0f14, #151824, etc.)
 * que no mapea 1:1 a los roles semánticos de Material 3 (primary, secondary, surface...).
 * Usar MaterialTheme.colorScheme.surface para el fondo del sheet y
 * MaterialTheme.colorScheme.primary para el botón funcionaría, pero haría
 * el código difícil de leer: nadie sabe qué color es "surface" sin abrir el tema.
 *
 * Con AppColors, el nombre dice exactamente qué es cada color.
 * Cuando el diseño cambie (tema claro, accesibilidad, etc.), solo tocamos este archivo.
 *
 * En el futuro podemos convertir esto en un CompositionLocal para soporte
 * de tema claro/oscuro dinámico.
 */
object AppColors {

    // ── Fondos ───────────────────────────────────────────────────────────────
    val ScreenBackground = Color(0xFF0D0F14)
    val ViewerBackground = Color(0xFF111420)
    val SheetBackground  = Color(0xFF151824)
    val SurfaceCard      = Color(0xFF1A1E2E)

    // ── Texto ────────────────────────────────────────────────────────────────
    val TextPrimary  = Color(0xFFE8EAF0)
    val TextMuted    = Color(0xFF7A86AA)
    val TextHint     = Color(0xFF5A6A99)

    // ── Pestañas de sistema ──────────────────────────────────────────────────
    val TabActive        = Color(0xFF1E2D5A)
    val TabInactive      = Color(0xFF1A1E2E)
    val TabTextActive    = Color(0xFF7AA4F0)
    val TabTextInactive  = Color(0xFF7A86AA)
    val TabBorderActive  = Color(0xFF3D5AB8)
    val TabBorderInactive = Color(0xFF2A2E42)

    // ── Botón principal ──────────────────────────────────────────────────────
    val PrimaryButton     = Color(0xFF2040A0)
    val PrimaryButtonText = Color(0xFFA0C0FF)

    // ── Badge "Seleccionado" ─────────────────────────────────────────────────
    val BadgeBackground = Color(0xFF1A2D5A)
    val BadgeText       = Color(0xFF7AA4F0)

    // ── Sheet ────────────────────────────────────────────────────────────────
    val SheetHandle = Color(0xFF2E3448)

    // ── Bordes ───────────────────────────────────────────────────────────────
    val BorderSubtle   = Color(0xFF262B3E)
    val BorderMedium   = Color(0xFF2E3245)
    val BorderAccent   = Color(0xFF3A5AB8)
}
