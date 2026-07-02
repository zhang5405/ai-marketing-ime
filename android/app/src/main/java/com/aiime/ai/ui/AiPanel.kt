package com.aiime.ai.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aiime.ai.service.AiMode
import com.aiime.ai.service.AiTone
import kotlinx.coroutines.launch

// ==================== AI 面板主入口 ====================

@Composable
fun AiPanel(
    onProcessText: (AiMode, String, AiTone, String?) -> Unit,
    onCopyResult: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var currentMode by remember { mutableStateOf(AiMode.POLISH) }
    var selectedTone by remember { mutableStateOf(AiTone.PROFESSIONAL) }
    var inputText by remember { mutableStateOf("") }
    var contextText by remember { mutableStateOf("") }
    var showContext by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    var resultText by remember { mutableStateOf("") }
    var streamingText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    Column(modifier = modifier.fillMaxSize()) {
        // ===== 模式切换 =====
        AiModeSelector(
            currentMode = currentMode,
            onModeChange = { currentMode = it },
        )

        Spacer(Modifier.height(6.dp))

        // ===== 语气选择 =====
        AiToneSelector(
            currentTone = selectedTone,
            onToneChange = { selectedTone = it },
        )

        Spacer(Modifier.height(8.dp))

        // ===== 输入区域 =====
        OutlinedTextField(
            value = inputText,
            onValueChange = { inputText = it },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 60.dp, max = 120.dp),
            placeholder = {
                Text(
                    when (currentMode) {
                        AiMode.POLISH -> "输入需要润色的文字..."
                        AiMode.REPLY -> "输入客户的问题或回复..."
                        AiMode.GENERATE -> "描述你需要的场景，如：客户拒绝后挽回话术..."
                    },
                    fontSize = 13.sp,
                )
            },
            textStyle = MaterialTheme.typography.bodySmall,
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
            ),
        )

        // 上下文输入（折叠式）
        AnimatedVisibility(visible = showContext) {
            OutlinedTextField(
                value = contextText,
                onValueChange = { contextText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
                    .heightIn(min = 40.dp, max = 80.dp),
                placeholder = {
                    Text(
                        "附加背景：产品信息、客户画像等...",
                        fontSize = 12.sp,
                    )
                },
                textStyle = MaterialTheme.typography.bodySmall,
                shape = RoundedCornerShape(10.dp),
            )
        }

        Spacer(Modifier.height(6.dp))

        // ===== 操作按钮行 =====
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(
                onClick = { showContext = !showContext },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Icon(
                    imageVector = if (showContext) Icons.Filled.Remove else Icons.Filled.Add,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
                Spacer(Modifier.width(2.dp))
                Text(
                    "背景",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            }

            Button(
                onClick = {
                    if (inputText.isNotBlank() && !isProcessing) {
                        isProcessing = true
                        resultText = ""
                        streamingText = ""
                        scope.launch {
                            onProcessText(currentMode, inputText, selectedTone, contextText.ifBlank { null })
                            isProcessing = false
                        }
                    }
                },
                enabled = inputText.isNotBlank() && !isProcessing,
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("处理中...", fontSize = 13.sp)
                } else {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        when (currentMode) {
                            AiMode.POLISH -> "开始润色"
                            AiMode.REPLY -> "生成回复"
                            AiMode.GENERATE -> "生成话术"
                        },
                        fontSize = 13.sp,
                    )
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        // ===== 结果区域 =====
        HorizontalDivider()

        if (resultText.isNotEmpty()) {
            Column(modifier = Modifier.padding(top = 8.dp)) {
                // 结果标题
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "AI 生成结果",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    // 一键输入按钮
                    FilledTonalIconButton(
                        onClick = { onCopyResult(resultText) },
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            Icons.Filled.Send,
                            "一键输入",
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }

                Spacer(Modifier.height(6.dp))

                // 结果显示
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = resultText,
                        style = MaterialTheme.typography.bodySmall,
                        lineHeight = 20.sp,
                        modifier = Modifier
                            .padding(12.dp)
                            .then(
                                // 让文本可选择
                                Modifier.clip(RoundedCornerShape(10.dp))
                            ),
                    )
                }
            }
        }

        // 加载状态
        if (isProcessing) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "AI 正在为你生成...",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    )
                }
            }
        }
    }
}

// ==================== 模式选择器 ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiModeSelector(
    currentMode: AiMode,
    onModeChange: (AiMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    SingleChoiceSegmentedButtonRow(modifier = modifier) {
        AiMode.entries.forEachIndexed { index, mode ->
            SegmentedButton(
                selected = currentMode == mode,
                onClick = { onModeChange(mode) },
                shape = SegmentedButtonDefaults.itemShape(index, AiMode.entries.size),
                icon = {
                    Icon(
                        imageVector = mode.icon,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                },
            ) {
                Text(mode.displayName, fontSize = 12.sp)
            }
        }
    }
}

// ==================== 语气选择器 ====================

@Composable
fun AiToneSelector(
    currentTone: AiTone,
    onToneChange: (AiTone) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        AiTone.entries.forEach { tone ->
            val isSelected = currentTone == tone
            FilterChip(
                selected = isSelected,
                onClick = { onToneChange(tone) },
                label = {
                    Text(
                        text = tone.displayName,
                        fontSize = 11.sp,
                    )
                },
                modifier = Modifier.height(28.dp),
            )
        }
    }
}
