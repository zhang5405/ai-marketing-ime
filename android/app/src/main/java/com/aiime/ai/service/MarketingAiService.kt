package com.aiime.ai.service

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException

// ==================== 枚举定义 ====================

enum class AiMode(
    val id: String,
    val displayName: String,
    val icon: ImageVector,
) {
    POLISH("polish", "润色", Icons.Filled.AutoFixHigh),
    REPLY("reply", "智能回复", Icons.Filled.Chat),
    GENERATE("generate", "生成话术", Icons.Filled.AutoAwesome),
}

enum class AiTone(val id: String, val displayName: String) {
    PROFESSIONAL("professional", "专业"),
    WARM("warm", "亲切"),
    PERSUASIVE("persuasive", "有说服力"),
}

// ==================== 数据类 ====================

data class AiConfig(
    val apiKey: String = "",
    val apiBase: String = "https://api.deepseek.com/v1",
    val polishModel: String = "deepseek-chat",
    val deepReplyModel: String = "deepseek-reasoner",
)

data class AiResult(
    val text: String,
    val mode: AiMode,
    val tokensUsed: Int = 0,
)

// ==================== Marketing AI 服务 ====================

class MarketingAiService(private val config: AiConfig) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(90, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    private val JSON = "application/json; charset=utf-8".toMediaType()

    // ==================== Prompt 模板 ====================

    private val SYSTEM_PROMPTS = mapOf(
        AiMode.POLISH to """
你是一个专业的营销话术润色助手。
请将用户提供的文字润色为专业、自然的营销话术。

要求：
1. 保持原意不变，优化表达方式
2. 去除冗余和口语化表达
3. 融入营销技巧（FAB法则、痛点共鸣、场景化描述）
4. 输出直接就是润色后的话术，不要加任何解释
5. 结果长度应与原文相近或稍长，不要过分扩充
        """.trimIndent(),

        AiMode.REPLY to """
你是一个专业的销售话术顾问，帮助销售人员回复客户。

请根据用户提供的客户问题，生成3个不同角度的话术回复供选择：

要求：
1. 识别客户问题的核心需求和潜在顾虑
2. 每个回复标注【方案一/二/三】并使用不同的切入角度（价值导向/情感共鸣/专业分析）
3. 融入逼单技巧和促成话术
4. 回复要自然、口语化，像真人聊天，不要像机器人
5. 每条回复控制在50-150字
        """.trimIndent(),

        AiMode.GENERATE to """
你是一个专业的营销话术库生成助手。

请根据用户描述的场景，生成5条可直接使用的营销话术。

要求：
1. 每条话术标注编号【1/2/3/4/5】
2. 每条话术控制在50-200字
3. 覆盖不同的切入角度和风格
4. 要自然、口语化、有说服力
5. 按照开场→痛点→方案→促成的逻辑组织
        """.trimIndent(),
    )

    private val TONE_PROMPTS = mapOf(
        AiTone.PROFESSIONAL to "保持专业、可信的语气",
        AiTone.WARM to "保持温暖、亲切的语气",
        AiTone.PERSUASIVE to "保持有说服力、能促单的语气",
    )

    // ==================== 基础 API 调用 ====================

    suspend fun processText(
        mode: AiMode,
        text: String,
        tone: AiTone = AiTone.PROFESSIONAL,
        context: String? = null,
    ): AiResult = withContext(Dispatchers.IO) {
        val systemPrompt = buildSystemPrompt(mode, tone, context)
        val userPrompt = buildUserPrompt(mode, text, context)
        val model = if (mode == AiMode.REPLY) config.deepReplyModel else config.polishModel

        val requestBody = JSONObject().apply {
            put("model", model)
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", systemPrompt)
                })
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", userPrompt)
                })
            })
            put("temperature", 0.7)
            put("max_tokens", 2048)
            put("stream", false)
        }

        val request = Request.Builder()
            .url("${config.apiBase}/chat/completions")
            .header("Authorization", "Bearer ${config.apiKey}")
            .header("Content-Type", "application/json")
            .post(requestBody.toString().toRequestBody(JSON))
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            throw IOException("API 调用失败: ${response.code} ${response.message}")
        }

        val responseBody = response.body?.string() ?: throw IOException("空响应")
        val json = JSONObject(responseBody)
        val resultText = json.getJSONArray("choices")
            .getJSONObject(0)
            .getJSONObject("message")
            .getString("content")
            .trim()

        val tokensUsed = json.optJSONObject("usage")?.optInt("total_tokens") ?: 0

        AiResult(resultText, mode, tokensUsed)
    }

    // ==================== 流式 API 调用 ====================

    fun processTextStream(
        mode: AiMode,
        text: String,
        tone: AiTone = AiTone.PROFESSIONAL,
        context: String? = null,
    ): Flow<String> = flow {
        val systemPrompt = buildSystemPrompt(mode, tone, context)
        val userPrompt = buildUserPrompt(mode, text, context)
        val model = if (mode == AiMode.REPLY) config.deepReplyModel else config.polishModel

        val requestBody = JSONObject().apply {
            put("model", model)
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", systemPrompt)
                })
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", userPrompt)
                })
            })
            put("temperature", 0.7)
            put("max_tokens", 2048)
            put("stream", true)
        }

        val request = Request.Builder()
            .url("${config.apiBase}/chat/completions")
            .header("Authorization", "Bearer ${config.apiKey}")
            .header("Content-Type", "application/json")
            .post(requestBody.toString().toRequestBody(JSON))
            .build()

        val response = withContext(Dispatchers.IO) {
            client.newCall(request).execute()
        }

        if (!response.isSuccessful) {
            throw IOException("API 调用失败: ${response.code}")
        }

        val reader = response.body?.byteStream()?.bufferedReader()
            ?: throw IOException("空响应")

        var line: String?
        while (withContext(Dispatchers.IO) { reader.readLine() }.also { line = it } != null) {
            val currentLine = line ?: break
            if (currentLine.startsWith("data: ")) {
                val data = currentLine.removePrefix("data: ")
                if (data == "[DONE]") break
                try {
                    val json = JSONObject(data)
                    val delta = json.getJSONArray("choices")
                        .getJSONObject(0)
                        .optJSONObject("delta")
                    val content = delta?.optString("content") ?: ""
                    if (content.isNotEmpty()) {
                        emit(content)
                    }
                } catch (_: Exception) {
                    // 跳过解析错误
                }
            }
        }
    }

    // ==================== Prompt 构建 ====================

    private fun buildSystemPrompt(mode: AiMode, tone: AiTone, context: String?): String {
        val base = SYSTEM_PROMPTS[mode] ?: SYSTEM_PROMPTS[AiMode.POLISH]!!
        val extras = mutableListOf<String>()

        TONE_PROMPTS[tone]?.let { extras.add(it) }
        if (!context.isNullOrBlank()) {
            extras.add("参考背景信息：$context")
        }

        return if (extras.isEmpty()) base
        else "$base\n\n${extras.joinToString("。")}。"
    }

    private fun buildUserPrompt(mode: AiMode, text: String, context: String?): String {
        return when (mode) {
            AiMode.REPLY -> "客户说：\n$text\n\n请生成回复话术。"
            AiMode.GENERATE -> "场景描述：\n$text\n\n请生成营销话术。"
            AiMode.POLISH -> "请润色以下文字：\n$text"
        }
    }
}
