package com.anatomia.app.ui.screen.agent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anatomia.app.agent.Question
import com.anatomia.app.ui.theme.AppColors

@Composable
fun AgentScreen(
    organId: String,
    onNavigateBack: () -> Unit,
    viewModel: AgentViewModel = viewModel { AgentViewModel(organId) },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    val messages = when (val s = uiState) {
        is AgentUiState.Loading  -> emptyList()
        is AgentUiState.Chatting -> s.messages
        is AgentUiState.Finished -> s.messages
    }
    val currentQuestion = (uiState as? AgentUiState.Chatting)?.currentQuestion
    val isFinished = uiState is AgentUiState.Finished

    // Scroll al final cuando llegan mensajes nuevos
    val itemCount = messages.size + (if (currentQuestion != null) 1 else 0) + (if (isFinished) 1 else 0)
    LaunchedEffect(itemCount) {
        if (itemCount > 0) listState.animateScrollToItem(itemCount - 1)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.ScreenBackground),
    ) {
        AgentTopBar(onNavigateBack = onNavigateBack)

        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
        ) {
            items(messages, key = { it.id }) { msg ->
                ChatBubble(message = msg)
            }

            if (currentQuestion != null) {
                item {
                    OptionsPanel(
                        question = currentQuestion,
                        onOptionSelected = { idx ->
                            viewModel.onOptionSelected(currentQuestion.id, idx)
                        },
                    )
                }
            }

            if (isFinished) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = onNavigateBack,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.PrimaryButton,
                            contentColor = AppColors.PrimaryButtonText,
                        ),
                    ) {
                        Text("Volver al modelo", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
private fun AgentTopBar(onNavigateBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .background(AppColors.SurfaceCard, RoundedCornerShape(10.dp))
                .size(36.dp),
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Volver",
                tint = AppColors.TextMuted,
            )
        }
        Text(
            text = "Agente anatomía",
            fontSize = 15.sp,
            color = AppColors.TextPrimary,
        )
        Spacer(modifier = Modifier.size(36.dp))
    }
}

@Composable
private fun ChatBubble(message: ChatMessage) {
    val isAgent = message.sender == Sender.AGENT
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isAgent) Arrangement.Start else Arrangement.End,
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .background(
                    color = if (isAgent) AppColors.SurfaceCard else AppColors.PrimaryButton,
                    shape = RoundedCornerShape(
                        topStart = if (isAgent) 4.dp else 12.dp,
                        topEnd   = if (isAgent) 12.dp else 4.dp,
                        bottomStart = 12.dp,
                        bottomEnd   = 12.dp,
                    ),
                )
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            Text(
                text = message.text,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                color = if (isAgent) AppColors.TextPrimary else AppColors.PrimaryButtonText,
            )
        }
    }
}

@Composable
private fun OptionsPanel(
    question: Question,
    onOptionSelected: (Int) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        question.options.forEachIndexed { index, option ->
            Surface(
                onClick = { onOptionSelected(index) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = AppColors.SurfaceCard,
                border = androidx.compose.foundation.BorderStroke(0.5.dp, AppColors.BorderSubtle),
            ) {
                Text(
                    text = option,
                    fontSize = 13.sp,
                    color = AppColors.TextPrimary,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                )
            }
        }
    }
}
