package com.aiime.ime

import android.content.Context
import com.osfans.trime.core.Rime
import com.osfans.trime.core.ContextProto
import com.osfans.trime.core.CommitProto
import java.io.File
import java.io.FileOutputStream

/**
 * RIME 输入法引擎桥接 — 基于 librime_jni.so (从 Trime APK 提取)
 *
 * 核心流程：
 * 1. initialize() — 复制 RIME 配置/词库到私有目录 → 启动引擎
 * 2. processInput(pinyin) — simulateRimeKeySequence → 获取候选词
 * 3. selectCandidate(i) — 选词 → getRimeCommit 获取提交文本
 * 4. destroy() — 关闭引擎
 *
 * JNI 桥接通过 com.osfans.trime.core.RimeJni 实现，
 * 匹配 librime_jni.so 的 native 方法签名。
 */
class RimeEngine(private val context: Context) {

    data class ProcessResult(
        val composingText: String,
        val candidates: List<String>,
    )

    private var initialized = false

    /** RIME 共享数据目录（schema + dictionary） */
    private val sharedDataDir: File
        get() = File(context.filesDir, "rime")

    /** RIME 用户数据目录（学习词库、同步数据） */
    private val userDataDir: File
        get() = File(context.filesDir, "rime_user")

    // ==================== 生命周期 ====================

    /**
     * 初始化 RIME 引擎
     * 1. 将 assets/rime/ 中的配置文件复制到 app 私有目录
     * 2. 调用 JNI startupRime 启动引擎
     */
    fun initialize() {
        if (initialized) return

        try {
            // 检查 JNI 是否可用
            if (!Rime.isLoaded()) {
                android.util.Log.e("RimeEngine", "librime_jni.so not loaded, falling back to simulator")
                initialized = true
                return
            }

            // 复制 RIME 数据文件到私有目录
            prepareRimeData()

            // 启动 RIME 引擎
            Rime.startupRime(
                sharedDataDir.absolutePath,
                userDataDir.absolutePath,
                "1.0.0",
                true,  // fullCheck = true on first launch
            )
            initialized = true
            android.util.Log.i("RimeEngine", "RIME engine initialized successfully")
        } catch (e: Exception) {
            android.util.Log.e("RimeEngine", "Failed to initialize RIME: ${e.message}", e)
            initialized = true // Mark as initialized to use fallback
        }
    }

    fun destroy() {
        if (!initialized) return
        try {
            if (Rime.isLoaded()) {
                Rime.exitRime()
            }
        } catch (e: Exception) {
            android.util.Log.e("RimeEngine", "Error destroying RIME: ${e.message}")
        }
        initialized = false
    }

    // ==================== 输入处理 ====================

    /**
     * 处理拼音输入，返回候选词列表
     */
    fun processInput(pinyinInput: String): ProcessResult {
        if (pinyinInput.isEmpty()) {
            return ProcessResult("", emptyList())
        }

        // 如果 JNI 未加载，使用本地模拟器
        if (!initialized || !Rime.isLoaded()) {
            return localPinyinSimulator(pinyinInput)
        }

        return try {
            // 先清除之前的组合状态
            Rime.clearRimeComposition()

            // 模拟按键序列输入拼音
            Rime.simulateRimeKeySequence(pinyinInput)

            // 获取上下文（包含组合文本和候选词）
            val ctx = Rime.getRimeContext()
            val comp = ctx?.composition
            val menu = ctx?.menu

            // 组合文本（拼音显示）
            val composingText = comp?.preedit ?: pinyinInput

            // 候选词列表
            val candidates = menu?.candidates?.mapNotNull { it.text } ?: emptyList()

            ProcessResult(composingText, candidates)
        } catch (e: Exception) {
            android.util.Log.e("RimeEngine", "processInput error: ${e.message}")
            localPinyinSimulator(pinyinInput)
        }
    }

    /**
     * 选中某个候选词，返回提交的文本
     */
    fun selectCandidate(index: Int): String? {
        if (!initialized || !Rime.isLoaded()) return null

        return try {
            Rime.selectRimeCandidate(index, false)
            Rime.commitRimeComposition()
            val commit = Rime.getRimeCommit()
            commit?.text
        } catch (e: Exception) {
            android.util.Log.e("RimeEngine", "selectCandidate error: ${e.message}")
            null
        }
    }

    /**
     * 清除当前组合状态
     */
    fun clear() {
        if (!initialized || !Rime.isLoaded()) return
        try {
            Rime.clearRimeComposition()
        } catch (e: Exception) {
            android.util.Log.e("RimeEngine", "clear error: ${e.message}")
        }
    }

    // ==================== RIME 数据准备 ====================

    /**
     * 将 assets/rime/ 中的文件复制到 app 私有目录
     */
    private fun prepareRimeData() {
        try {
            // 只在首次或版本更新时复制
            sharedDataDir.mkdirs()
            userDataDir.mkdirs()

            // 创建 build 标记文件，避免每次重启都 fullCheck
            val versionFile = File(sharedDataDir, "installation.yaml")
            if (versionFile.exists()) {
                // 已初始化，跳过复制
                return
            }

            // 复制 assets/rime/ 中的所有文件
            val assetFiles = context.assets.list("rime") ?: emptyArray()
            for (fileName in assetFiles) {
                val targetFile = File(sharedDataDir, fileName)
                if (targetFile.exists()) continue

                try {
                    context.assets.open("rime/$fileName").use { input ->
                        FileOutputStream(targetFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.w("RimeEngine", "Failed to copy $fileName: ${e.message}")
                }
            }

            // 创建 installation.yaml
            versionFile.writeText(
                """# RIME installation info
install_id: ai_marketing_ime
sync_dir: ${userDataDir.absolutePath}
""".trimIndent()
            )

            android.util.Log.i("RimeEngine", "RIME data prepared: ${assetFiles.size} files")
        } catch (e: Exception) {
            android.util.Log.e("RimeEngine", "Error preparing RIME data: ${e.message}", e)
        }
    }

    // ==================== 回退：本地拼音模拟器 ====================

    private val pinyinMap = mapOf(
        "nihao" to listOf("你好"),
        "nih" to listOf("你好", "拟好", "腻好"),
        "zaijian" to listOf("再见"),
        "xiexie" to listOf("谢谢"),
        "duibuqi" to listOf("对不起"),
        "meiguanxi" to listOf("没关系"),
        "shangwu" to listOf("上午"),
        "xiawu" to listOf("下午"),
        "wanshang" to listOf("晚上"),
        "mingtian" to listOf("明天"),
        "jintian" to listOf("今天"),
        "zuotian" to listOf("昨天"),
        "xin" to listOf("新", "心", "信", "辛", "欣"),
        "xi" to listOf("西", "喜", "细", "系", "息"),
        "ying" to listOf("营", "应", "影", "硬", "迎"),
        "xiao" to listOf("销", "小", "笑", "效", "消"),
        "kehu" to listOf("客户"),
        "chanpin" to listOf("产品"),
        "jiage" to listOf("价格"),
        "fuwu" to listOf("服务"),
        "hezuo" to listOf("合作"),
        "shichang" to listOf("市场"),
        "yingxiao" to listOf("营销"),
        "tuiguang" to listOf("推广"),
        "zixun" to listOf("咨询"),
        "x" to listOf("下", "小", "新", "想", "行"),
        "y" to listOf("有", "一", "要", "也", "用"),
    )

    private fun localPinyinSimulator(input: String): ProcessResult {
        pinyinMap[input]?.let { candidates ->
            return ProcessResult(input, candidates)
        }
        val prefixMatches = pinyinMap.entries
            .filter { it.key.startsWith(input) }
            .flatMap { it.value }
            .take(5)
        if (prefixMatches.isNotEmpty()) {
            return ProcessResult(input, prefixMatches)
        }
        return ProcessResult(input, listOf(input))
    }
}
