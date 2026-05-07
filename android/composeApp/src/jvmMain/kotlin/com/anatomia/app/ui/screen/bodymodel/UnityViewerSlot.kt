package com.anatomia.app.ui.screen.bodymodel

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.anatomia.app.ui.theme.AppColors

@Composable
actual fun UnityViewerSlot(onOrganSelected: (organId: String) -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Modelo 3D disponible solo en Android",
            fontSize = 14.sp,
            color = AppColors.TextMuted,
        )
    }
}
