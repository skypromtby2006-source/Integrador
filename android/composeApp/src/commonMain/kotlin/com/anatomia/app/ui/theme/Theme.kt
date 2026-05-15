package com.anatomia.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle

enum class AppTheme { LIGHT, DARK, AUTO }

data class SuccessColors(
    val success            : Color,
    val onSuccess          : Color,
    val successContainer   : Color,
    val onSuccessContainer : Color
)

val LocalSuccessColors = staticCompositionLocalOf {
    SuccessColors(
        success            = SuccessLight,
        onSuccess          = OnSuccessLight,
        successContainer   = SuccessContainerLight,
        onSuccessContainer = OnSuccessContainerLight
    )
}

@Composable
fun DidactaiTheme(
    appTheme: AppTheme = AppTheme.AUTO,
    fontSizeIndex: Int = 2,
    content: @Composable () -> Unit
) {
    val systemInDark = isSystemInDarkTheme()

    val useDark = when (appTheme) {
        AppTheme.LIGHT -> false
        AppTheme.DARK  -> true
        AppTheme.AUTO  -> systemInDark
    }

    val colorScheme = if (useDark) DidactaiDarkColors else DidactaiLightColors

    val scaledTypography = remember(fontSizeIndex) {
        val mult = when (fontSizeIndex) {
            0 -> 0.82f
            1 -> 0.91f
            2 -> 1.00f
            3 -> 1.14f
            4 -> 1.29f
            else -> 1.00f
        }
        scaleTypography(DidactaiTypography, mult)
    }

    val successColors = if (useDark) {
        SuccessColors(SuccessDark, OnSuccessDark, SuccessContainerDark, OnSuccessContainerDark)
    } else {
        SuccessColors(SuccessLight, OnSuccessLight, SuccessContainerLight, OnSuccessContainerLight)
    }

    CompositionLocalProvider(LocalSuccessColors provides successColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography  = scaledTypography,
            shapes      = DidactaiShapes,
            content     = content
        )
    }
}

private fun scaleTypography(base: Typography, factor: Float): Typography {
    fun TextStyle.scaled() = copy(fontSize = fontSize * factor, lineHeight = lineHeight * factor)
    return base.copy(
        displayLarge   = base.displayLarge.scaled(),
        displayMedium  = base.displayMedium.scaled(),
        displaySmall   = base.displaySmall.scaled(),
        headlineLarge  = base.headlineLarge.scaled(),
        headlineMedium = base.headlineMedium.scaled(),
        headlineSmall  = base.headlineSmall.scaled(),
        titleLarge     = base.titleLarge.scaled(),
        titleMedium    = base.titleMedium.scaled(),
        titleSmall     = base.titleSmall.scaled(),
        bodyLarge      = base.bodyLarge.scaled(),
        bodyMedium     = base.bodyMedium.scaled(),
        bodySmall      = base.bodySmall.scaled(),
        labelLarge     = base.labelLarge.scaled(),
        labelMedium    = base.labelMedium.scaled(),
        labelSmall     = base.labelSmall.scaled(),
    )
}
