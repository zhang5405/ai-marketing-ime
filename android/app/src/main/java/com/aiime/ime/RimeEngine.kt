package com.aiime.ime

import android.content.Context
import java.io.File

/**
 * RIME 输入法引擎桥接类
 *
 * 基于 librime-android (BSD) 的 JNI 桥接，
 * 负责：
 * 1. 初始化 RIME 引擎（加载拼音方案 + 词库）
 * 2. processInput(拼音串) → 候选词列表
 * 3. 选词确认
 * 4. 清理状态
 *
 * libRime 的 Android AAR 预编译版本：
 * https://github.com/HNIdesu/librime-android/releases
 *
 * 集成步骤：
 * 1. 下载 librime-android-*.aar 放到 app/libs/
 * 2. build.gradle 添加 implementation files('libs/librime-android-*.aar')
 * 3. 复制 RIME 配置/词库到 assets/rime/
 */
class RimeEngine(private val context: Context) {

    data class ProcessResult(
        val composingText: String,
        val candidates: List<String>,
    )

    private var initialized = false

    /**
     * 初始化 RIME 引擎
     * 需要将 assets/rime/ 复制到 app 私有目录
     */
    fun initialize() {
        // ============================================================
        // 注意：以下为 RIME 引擎初始化框架代码
        // 实际编译需要引入 librime-android AAR
        //
        // 集成 librime-android 后取消注释以下代码：
        //
        // val rimeDir = File(context.filesDir, "rime")
        // if (!rimeDir.exists() || rimeDir.listFiles()?.isEmpty() == true) {
        //     copyAssets("rime", rimeDir)
        // }
        //
        // Rime.init(rimeDir.absolutePath)
        // Rime.deployConfigFile("default.custom.yaml", "0.0.0.0")
        // Rime.deployerInitialize(null)
        //
        // initialized = true
        // ============================================================

        // 临时：使用本地拼音模拟器，等 AAR 集成就绪后替换
        initialized = true
    }

    fun destroy() {
        if (!initialized) return
        // Rime.destroy()
        initialized = false
    }

    /**
     * 处理拼音输入，返回组合文本和候选词
     */
    fun processInput(pinyinInput: String): ProcessResult {
        if (!initialized) {
            return ProcessResult(pinyinInput, emptyList())
        }

        // ============================================================
        // 集成 librime-android 后取消注释以下代码：
        //
        // val session = Rime.createSession()
        // Rime.simulateKeySequence(session, pinyinInput)
        //
        // val context = Rime.getContext(session)
        // val composingText = context.commitTextPreview ?: ""
        // val candidates = context.menu?.let { menu ->
        //     val list = mutableListOf<String>()
        //     for (i in 0 until menu.numCandidates) {
        //         val candidate = menu.getCandidateAt(i)
        //         candidate?.text?.let { list.add(it) }
        //     }
        //     list
        // } ?: emptyList()
        //
        // Rime.destroySession(session)
        //
        // return ProcessResult(composingText, candidates)
        // ============================================================

        // 临时：本地拼音分词模拟
        return localPinyinSimulator(pinyinInput)
    }

    fun clear() {
        if (!initialized) return
        // Rime.clearComposition(session)
    }

    // ============================================================
    // 临时拼音模拟器 — 在 RIME AAR 集成前使用
    // ============================================================

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
        // 尝试精确匹配
        pinyinMap[input]?.let { candidates ->
            return ProcessResult(input, candidates)
        }

        // 尝试前缀匹配
        val prefixMatches = pinyinMap.entries
            .filter { it.key.startsWith(input) }
            .flatMap { it.value }
            .take(5)

        if (prefixMatches.isNotEmpty()) {
            return ProcessResult(input, prefixMatches)
        }

        // 没有匹配，返回输入本身
        return ProcessResult(input, listOf(input))
    }

    companion object {
        /**
         * 从 assets 复制 RIME 配置到目标目录
         */
        private fun copyAssets(assetPath: String, targetDir: File) {
            targetDir.mkdirs()
            // 实际实现：遍历 assets/rime/ 下的所有文件复制到 targetDir
            // context.assets.list(assetPath)?.forEach { fileName ->
            //     ...
            // }
        }
    }
}
