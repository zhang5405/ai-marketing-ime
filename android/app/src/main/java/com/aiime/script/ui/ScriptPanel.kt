package com.aiime.script.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aiime.script.data.ScriptCategory
import com.aiime.script.data.ScriptEntity
import kotlinx.coroutines.launch

// ==================== 话术分类标签栏 ====================

@Composable
fun ScriptCategoryTabs(
    selectedCategory: String?,
    categories: List<ScriptCategory>,
    onCategorySelected: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier) {
        item {
            // "全部" 标签
            CategoryTabItem(
                category = null,
                isSelected = selectedCategory == null,
                onClick = { onCategorySelected(null) },
            )
        }
        items(categories) { cat ->
            CategoryTabItem(
                category = cat,
                isSelected = selectedCategory == cat.id,
                onClick = { onCategorySelected(cat.id) },
            )
        }
    }
}

@Composable
private fun CategoryTabItem(
    category: ScriptCategory?,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val bgColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
    else MaterialTheme.colorScheme.surface

    val textColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
    else MaterialTheme.colorScheme.onSurface

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = category?.icon ?: "📂",
            fontSize = 16.sp,
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = category?.displayName ?: "全部",
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = textColor,
        )
    }
}

// ==================== 话术列表 ====================

@Composable
fun ScriptList(
    scripts: List<ScriptEntity>,
    onScriptClick: (ScriptEntity) -> Unit,
    onFavoriteClick: (ScriptEntity) -> Unit,
    onDeleteClick: (ScriptEntity) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (scripts.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("📭", fontSize = 48.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    "暂无话术",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "在 AI 助手中可以生成话术并收藏到这里",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                )
            }
        }
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        items(scripts, key = { it.id }) { script ->
            ScriptCard(
                script = script,
                onClick = { onScriptClick(script) },
                onFavorite = { onFavoriteClick(script) },
                onDelete = { onDeleteClick(script) },
            )
        }
    }
}

@Composable
fun ScriptCard(
    script: ScriptEntity,
    onClick: () -> Unit,
    onFavorite: () -> Unit,
    onDelete: () -> Unit,
) {
    val category = ScriptCategory.fromId(script.category)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // 标题行
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${category.icon} ${script.title}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                // 收藏按钮
                IconButton(
                    onClick = onFavorite,
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        imageVector = if (script.isFavorite) Icons.Filled.Favorite
                        else Icons.Filled.FavoriteBorder,
                        contentDescription = "收藏",
                        tint = if (script.isFavorite) Color(0xFFE53935)
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            Spacer(Modifier.height(6.dp))

            // 话术内容预览
            Text(
                text = script.content.take(120) + if (script.content.length > 120) "..." else "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp,
            )

            Spacer(Modifier.height(8.dp))

            // 底部标签 + 使用次数
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // 标签（前3个）
                val tagList = script.tags.split(",")
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                    .take(3)

                tagList.forEach { tag ->
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    ) {
                        Text(
                            text = tag,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        )
                    }
                    Spacer(Modifier.width(4.dp))
                }

                Spacer(Modifier.weight(1f))

                // 使用次数
                if (script.usageCount > 0) {
                    Text(
                        text = "已用${script.usageCount}次",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                    )
                }
            }
        }
    }
}

// ==================== 话术详情/预览 Dialog ====================

@Composable
fun ScriptDetailDialog(
    script: ScriptEntity,
    onDismiss: () -> Unit,
    onCopy: (String) -> Unit,
    onEdit: (ScriptEntity) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = script.title,
                style = MaterialTheme.typography.titleMedium,
            )
        },
        text = {
            Column {
                // 分类标签
                val cat = ScriptCategory.fromId(script.category)
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Text(
                        text = "${cat.icon} ${cat.displayName}",
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }

                Spacer(Modifier.height(12.dp))

                // 完整话术内容
                Text(
                    text = script.content,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 22.sp,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onCopy(script.content)
                onDismiss()
            }) {
                Icon(Icons.Filled.ContentCopy, null, Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("一键输入")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        },
    )
}

// ==================== 话术搜索 ====================

@Composable
fun ScriptSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text("搜索话术...", fontSize = 13.sp) },
        leadingIcon = {
            Icon(Icons.Filled.Search, null, Modifier.size(18.dp))
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(
                    onClick = onClear,
                    modifier = Modifier.size(24.dp),
                ) {
                    Icon(Icons.Filled.Clear, "清除", Modifier.size(16.dp))
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
        ),
        textStyle = MaterialTheme.typography.bodySmall,
    )
}
