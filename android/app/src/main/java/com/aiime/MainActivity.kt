package com.aiime

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aiime.script.data.ScriptDatabase
import com.aiime.script.data.ScriptRepository
import com.aiime.script.data.ScriptSeedData
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 初始化话术种子数据（首次启动）
        val scope = kotlinx.coroutines.MainScope()
        scope.launch {
            val db = androidx.room.Room.databaseBuilder(
                applicationContext,
                ScriptDatabase::class.java,
                ScriptDatabase.DATABASE_NAME,
            ).build()
            val repo = ScriptRepository(db.scriptDao())
            if (repo.count() == 0) {
                repo.insertAll(ScriptSeedData.SEED_SCRIPTS)
            }
        }

        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = androidx.compose.ui.graphics.Color(0xFF1A73E8),
                    secondary = androidx.compose.ui.graphics.Color(0xFFFF6D00),
                )
            ) {
                MainScreen(
                    onEnableIME = { openIMEEnableSettings() },
                    onOpenSettings = { startActivity(Intent(this, SettingsActivity::class.java)) },
                )
            }
        }
    }

    private fun openIMEEnableSettings() {
        startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
    }
}

@Composable
fun MainScreen(
    onEnableIME: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(48.dp))

        // Logo
        Text("⌨✨", fontSize = 64.sp)

        Spacer(Modifier.height(16.dp))

        Text(
            "AI营销输入法",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )

        Spacer(Modifier.height(8.dp))

        Text(
            "话术库 + AI润色 + 拼音输入",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        )

        Spacer(Modifier.height(48.dp))

        // 启用输入法按钮
        Button(
            onClick = onEnableIME,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(14.dp),
        ) {
            Icon(Icons.Filled.Keyboard, null)
            Spacer(Modifier.width(8.dp))
            Text("启用输入法", fontSize = 16.sp)
        }

        Spacer(Modifier.height(16.dp))

        // 功能卡片
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        ) {
            Column(Modifier.padding(16.dp)) {
                FeatureRow("📋", "话术库", "15条预置营销话术，支持分类/搜索/收藏/一键输入")
                Spacer(Modifier.height(12.dp))
                FeatureRow("✨", "AI助手", "润色优化、智能回复、场景生成，对接DeepSeek")
                Spacer(Modifier.height(12.dp))
                FeatureRow("⌨", "拼音输入", "标准QWERTY拼音键盘，RIME引擎候选词")
                Spacer(Modifier.height(12.dp))
                FeatureRow("💰", "极低成本", "月费仅¥1-10，本地部署无云服务费")
            }
        }

        Spacer(Modifier.height(16.dp))

        // 设置按钮
        OutlinedButton(
            onClick = onOpenSettings,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
        ) {
            Icon(Icons.Filled.Settings, null)
            Spacer(Modifier.width(8.dp))
            Text("设置与配置")
        }

        Spacer(Modifier.height(32.dp))

        Text(
            "v1.0.0 | Apache 2.0",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
        )
    }
}

@Composable
fun FeatureRow(icon: String, title: String, desc: String) {
    Row {
        Text(icon, fontSize = 24.sp)
        Spacer(Modifier.width(12.dp))
        Column {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            Text(
                desc,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )
        }
    }
}
