package com.aiime.ime

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aiime.script.data.ScriptCategory
import com.aiime.script.data.ScriptEntity
import com.aiime.script.ui.ScriptCategoryTabs
import com.aiime.script.ui.ScriptDetailDialog
import com.aiime.script.ui.ScriptList
import com.aiime.script.ui.ScriptSearchBar
import com.aiime.ai.ui.AiPanel
import com.aiime.ai.service.MarketingAiService
import com.aiime.ai.service.AiMode
import com.aiime.ai.service.AiTone
import com.aiime.ai.service.AiConfig
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * AI营销输入法 — 主键盘 Composable
 *
 * 三 Tab 布局：
 * Tab 1 - 拼音： 标准 QWERTY 拼音键盘 + 候选词栏
 * Tab 2 - 话术： 左侧分类 + 右侧话术列表
 * Tab 3 - AI：   模式选择 + 输入框 + AI 结果
 */
@Composable
fun MarketingKeyboard(
    viewModel: KeyboardViewModel,
    onKeyPress: (String) -> Unit,
    onCandidateSelect: (String) -> Unit,
    onScriptSelect: (String) -> Unit,
    onAiResult: (String) -> Unit,
) {
    val currentTab by viewModel.currentTab.collectAsState()
    val composingText by viewModel.composingText.collectAsState()
    val candidates by viewModel.candidates.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 6.dp, vertical = 4.dp),
    ) {
        // ===== 候选词栏（拼音 Tab 显示） =====
        if (currentTab == KeyboardTab.PINYIN) {
            CandidateBar(
                composingText = composingText,
                candidates = candidates,
                onCandidateClick = onCandidateSelect,
            )
        }

        // ===== 内容区域 =====
        when (currentTab) {
            KeyboardTab.PINYIN -> PinyinKeyboard(onKeyPress = onKeyPress)
            KeyboardTab.SCRIPTS -> ScriptTabContent(
                viewModel = viewModel,
                onScriptSelect = onScriptSelect,
            )
            KeyboardTab.AI -> AiTabContent(
                viewModel = viewModel,
                onAiResult = onAiResult,
            )
        }

        Spacer(Modifier.height(4.dp))

        // ===== 底部 Tab 切换栏 =====
        KeyboardTabBar(
            currentTab = currentTab,
            onTabChange = { viewModel.switchTab(it) },
        )
    }
}

// ==================== 候选词栏 ====================

@Composable
fun CandidateBar(
    composingText: String,
    candidates: List<String>,
    onCandidateClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 拼音显示
        if (composingText.isNotEmpty()) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
            ) {
                Text(
                    text = "  $composingText  ",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(vertical = 4.dp),
                )
            }
        }

        Spacer(Modifier.width(8.dp))

        // 候选词
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.weight(1f),
        ) {
            items(candidates) { candidate ->
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.clickable { onCandidateClick(candidate) },
                ) {
                    Text(
                        text = candidate,
                        fontSize = 15.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    )
                }
            }
        }
    }
}

// ==================== 拼音键盘 ====================

@Composable
fun PinyinKeyboard(
    onKeyPress: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val keyRows = listOf(
        listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"),
        listOf("a", "s", "d", "f", "g", "h", "j", "k", "l"),
        listOf("SHIFT", "z", "x", "c", "v", "b", "n", "m", "DEL"),
        listOf("123", ",", "SPACE", ".", "ENTER"),
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        keyRows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                row.forEach { key ->
                    PinyinKey(
                        key = key,
                        onClick = {
                            when (key) {
                                "DEL" -> onKeyPress("BACKSPACE")
                                "SPACE" -> onKeyPress("SPACE")
                                "ENTER" -> onKeyPress("ENTER")
                                "SHIFT", "123" -> { /* TODO: 大小写/数字切换 */ }
                                else -> onKeyPress(key)
                            }
                        },
                        modifier = when (key) {
                            "SPACE" -> Modifier.weight(4f)
                            "ENTER" -> Modifier.weight(2f)
                            "SHIFT", "DEL" -> Modifier.weight(1.5f)
                            else -> Modifier.weight(1f)
                        },
                    )
                }
            }
        }
    }
}

@Composable
fun PinyinKey(
    key: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val displayText = when (key) {
        "DEL" -> "⌫"
        "SPACE" -> "空格"
        "ENTER" -> "↵"
        "SHIFT" -> "⇧"
        "123" -> "123"
        else -> key
    }

    val isSpecial = key in listOf("DEL", "SPACE", "ENTER", "SHIFT", "123", ",", ".")

    Surface(
        modifier = modifier
            .padding(2.dp)
            .height(42.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(6.dp),
        color = if (isSpecial)
            MaterialTheme.colorScheme.surfaceContainerHigh
        else
            MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 1.dp,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = displayText,
                fontSize = if (isSpecial) 13.sp else 16.sp,
                fontWeight = if (key in listOf("DEL", "ENTER")) FontWeight.Bold else FontWeight.Normal,
                color = if (isSpecial)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
        }
    }
}

// ==================== 话术 Tab 内容 ====================

@Composable
fun ScriptTabContent(
    viewModel: KeyboardViewModel,
    onScriptSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var scripts by remember { mutableStateOf<List<ScriptEntity>>(emptyList()) }
    var selectedScript by remember { mutableStateOf<ScriptEntity?>(null) }
    val scope = rememberCoroutineScope()

    Column(modifier = modifier.fillMaxWidth().height(200.dp)) {
        // 搜索栏
        ScriptSearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            onClear = { searchQuery = "" },
        )

        Spacer(Modifier.height(6.dp))

        // 分类 + 列表
        Row(modifier = Modifier.fillMaxSize()) {
            // 左侧分类
            ScriptCategoryTabs(
                selectedCategory = selectedCategory,
                categories = ScriptCategory.entries,
                onCategorySelected = { cat ->
                    selectedCategory = cat
                    searchQuery = ""
                },
                modifier = Modifier.width(90.dp),
            )

            Spacer(Modifier.width(8.dp))

            // 右侧列表
            ScriptList(
                scripts = scripts,
                onScriptClick = { script ->
                    scope.launch {
                        viewModel.scriptRepository.incrementUsage(script.id)
                    }
                    selectedScript = script
                },
                onFavoriteClick = { script ->
                    scope.launch {
                        viewModel.scriptRepository.toggleFavorite(script.id)
                    }
                },
                onDeleteClick = { script ->
                    scope.launch {
                        viewModel.scriptRepository.deleteById(script.id)
                    }
                },
                modifier = Modifier.weight(1f),
            )
        }
    }

    // 话术详情弹窗
    selectedScript?.let { script ->
        ScriptDetailDialog(
            script = script,
            onDismiss = { selectedScript = null },
            onCopy = { text -> onScriptSelect(text) },
            onEdit = { /* TODO */ },
        )
    }

    // 加载数据
    LaunchedEffect(selectedCategory, searchQuery) {
        val repo = viewModel.scriptRepository
        scripts = if (searchQuery.isNotEmpty()) {
            repo.search(searchQuery)
        } else {
            repo.getScripts(category = selectedCategory)
        }
    }
}

// ==================== AI Tab 内容 ====================

@Composable
fun AiTabContent(
    viewModel: KeyboardViewModel,
    onAiResult: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var resultText by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    AiPanel(
        onProcessText = { mode, text, tone, context ->
            scope.launch {
                val service = viewModel.aiService
                    ?: MarketingAiService(AiConfig()) // TODO: 从设置读取 API Key
                val result = service.processText(mode, text, tone, context)
                resultText = result.text
            }
        },
        onCopyResult = { text ->
            onAiResult(text)
        },
        modifier = modifier.height(210.dp),
    )
}

// ==================== Tab 切换栏 ====================

@Composable
fun KeyboardTabBar(
    currentTab: KeyboardTab,
    onTabChange: (KeyboardTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        KeyboardTab.entries.forEach { tab ->
            val isSelected = currentTab == tab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .then(
                        if (isSelected) Modifier.background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            RoundedCornerShape(8.dp),
                        ) else Modifier
                    )
                    .clickable { onTabChange(tab) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "${tab.icon} ${tab.displayName}",
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            }
        }
    }
}
