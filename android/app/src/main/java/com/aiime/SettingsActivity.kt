package com.aiime

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 读取已保存的设置
        val prefs = getSharedPreferences("ai_ime_prefs", MODE_PRIVATE)

        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = androidx.compose.ui.graphics.Color(0xFF1A73E8),
                    secondary = androidx.compose.ui.graphics.Color(0xFFFF6D00),
                )
            ) {
                SettingsScreen(
                    savedApiKey = prefs.getString("api_key", "") ?: "",
                    savedApiBase = prefs.getString("api_base", "https://api.deepseek.com/v1") ?: "",
                    savedServerUrl = prefs.getString("server_url", "") ?: "",
                    onSave = { apiKey, apiBase, serverUrl ->
                        prefs.edit().apply {
                            putString("api_key", apiKey)
                            putString("api_base", apiBase)
                            putString("server_url", serverUrl)
                            apply()
                        }
                    },
                    onBack = { finish() },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    savedApiKey: String,
    savedApiBase: String,
    savedServerUrl: String,
    onSave: (String, String, String) -> Unit,
    onBack: () -> Unit,
) {
    var apiKey by remember { mutableStateOf(savedApiKey) }
    var apiBase by remember { mutableStateOf(savedApiBase) }
    var serverUrl by remember { mutableStateOf(savedServerUrl) }
    var showApiKey by remember { mutableStateOf(false) }
    var showSaved by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("← 返回")
                    }
                },
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            // ===== AI 配置 =====
            Text(
                "AI 配置",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            Spacer(Modifier.height(12.dp))

            // DeepSeek API Key
            OutlinedTextField(
                value = apiKey,
                onValueChange = { apiKey = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("DeepSeek API Key") },
                placeholder = { Text("sk-...") },
                visualTransformation = if (showApiKey) VisualTransformation.None
                else PasswordVisualTransformation(),
                trailingIcon = {
                    TextButton(onClick = { showApiKey = !showApiKey }) {
                        Text(if (showApiKey) "隐藏" else "显示", fontSize = 12.sp)
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
            )

            Spacer(Modifier.height(8.dp))

            // API Base URL
            OutlinedTextField(
                value = apiBase,
                onValueChange = { apiBase = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("API 地址") },
                placeholder = { Text("https://api.deepseek.com/v1") },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
            )

            Spacer(Modifier.height(20.dp))

            // ===== 后端服务器配置 =====
            Text(
                "后端服务器（可选）",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = serverUrl,
                onValueChange = { serverUrl = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("服务器地址") },
                placeholder = { Text("http://192.168.x.x:8000 或 Tailscale 地址") },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                supportingText = {
                    Text(
                        "本地服务地址或 Tailscale MagicDNS 地址，用于话术同步等。\n如不需要后端服务可留空。",
                        fontSize = 12.sp,
                    )
                },
            )

            Spacer(Modifier.height(32.dp))

            // ===== 保存按钮 =====
            Button(
                onClick = {
                    onSave(apiKey, apiBase, serverUrl)
                    showSaved = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text("保存设置", fontSize = 15.sp)
            }

            if (showSaved) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "✅ 设置已保存",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 13.sp,
                )
            }

            Spacer(Modifier.height(32.dp))

            // ===== 使用说明 =====
            Text(
                "使用说明",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            Spacer(Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                ),
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "1. 获取 DeepSeek API Key",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                    )
                    Text(
                        "访问 platform.deepseek.com 注册并获取 API Key",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "2. 填入上方 API Key",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                    )
                    Text(
                        "API Key 保存在本地，不会上传到任何第三方服务器",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "3. 在系统设置中启用 AI营销输入法",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                    )
                    Text(
                        "设置 → 语言和输入法 → 选择 AI营销输入法 → 切换到该输入法",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    )
                }
            }
        }
    }
}
