package com.aiime.ime

import android.inputmethodservice.InputMethodService
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import androidx.compose.runtime.*
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * AI营销输入法 — 核心 IME Service
 *
 * 基于 BiBi Keyboard 的 AsrKeyboardService 改造而来，
 * 将纯语音输入改为：拼音输入 + 话术库 + AI助手 三模式。
 *
 * 改造要点：
 * 1. 重写 onKeyDown → 交给 RIME 引擎处理拼音 → 获取候选词
 * 2. 键盘布局 → Compose 三 Tab 布局（拼音 / 话术 / AI）
 * 3. 语音能力保留可选，但不作为默认入口
 */
class MarketingKeyboardService : InputMethodService() {

    // ==================== 输入状态 ====================

    private var composingText = StringBuilder()
    private val rimeEngine: RimeEngine by lazy { RimeEngine(applicationContext) }

    val keyboardViewModel = KeyboardViewModel()

    override fun onCreate() {
        super.onCreate()
        rimeEngine.initialize()
        keyboardViewModel.initScriptDb(applicationContext)
    }

    override fun onDestroy() {
        super.onDestroy()
        rimeEngine.destroy()
    }

    // ==================== 键盘视图 ====================

    override fun onCreateInputView(): View {
        return ComposeView(this).apply {
            setContent {
                val vm = keyboardViewModel

                MarketingKeyboard(
                    viewModel = vm,
                    onKeyPress = { key -> handleKeyPress(key) },
                    onCandidateSelect = { candidate -> commitText(candidate) },
                    onScriptSelect = { script -> commitText(script) },
                    onAiResult = { text -> commitText(text) },
                )
            }
        }
    }

    // ==================== 输入处理 ====================

    private fun handleKeyPress(key: String) {
        val ic = currentInputConnection ?: return

        when (key) {
            "BACKSPACE" -> {
                if (composingText.isNotEmpty()) {
                    composingText.deleteCharAt(composingText.length - 1)
                    updateRimeComposition()
                } else {
                    ic.deleteSurroundingText(1, 0)
                }
            }
            "SPACE" -> {
                // 空格：提交当前候选词 或 输入空格
                val candidates = rimeEngine.getCandidates()
                if (composingText.isNotEmpty() && candidates.isNotEmpty()) {
                    commitText(candidates.first())
                } else {
                    ic.commitText(" ", 1)
                }
            }
            "ENTER" -> {
                ic.commitText("\n", 1)
                clearComposition()
            }
            "COMMIT_RAW" -> {
                // 直接提交拼音原文
                if (composingText.isNotEmpty()) {
                    ic.commitText(composingText.toString(), 1)
                    clearComposition()
                }
            }
            else -> {
                // 普通字符输入
                composingText.append(key)
                updateRimeComposition()
            }
        }
    }

    /** 更新 RIME 引擎组合状态 */
    private fun updateRimeComposition() {
        val ic = currentInputConnection ?: return

        if (composingText.isEmpty()) {
            ic.finishComposingText()
            keyboardViewModel.updateCandidates(emptyList())
            return
        }

        val result = rimeEngine.processInput(composingText.toString())
        keyboardViewModel.updateCandidates(result.candidates)
        keyboardViewModel.updateComposing(result.composingText)

        // 设置组合文本
        ic.setComposingText(result.composingText, 1)
    }

    /** 提交文本并清除组合状态 */
    private fun commitText(text: String) {
        val ic = currentInputConnection ?: return
        ic.commitText(text, 1)
        clearComposition()
    }

    private fun clearComposition() {
        composingText.clear()
        rimeEngine.clear()
        keyboardViewModel.updateCandidates(emptyList())
        keyboardViewModel.updateComposing("")
    }

    // ==================== 硬按键处理（物理键盘） ====================

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (event == null) return false

        // 处理删除键
        if (keyCode == KeyEvent.KEYCODE_DEL) {
            handleKeyPress("BACKSPACE")
            return true
        }

        // 处理回车
        if (keyCode == KeyEvent.KEYCODE_ENTER) {
            handleKeyPress("ENTER")
            return true
        }

        // 处理空格
        if (keyCode == KeyEvent.KEYCODE_SPACE) {
            handleKeyPress("SPACE")
            return true
        }

        // 处理字符输入
        val unicodeChar = event.unicodeChar
        if (unicodeChar > 0 && !Character.isISOControl(unicodeChar)) {
            handleKeyPress(unicodeChar.toChar().toString().lowercase())
            return true
        }

        return super.onKeyDown(keyCode, event)
    }
}

// ==================== 键盘 ViewModel ====================

class KeyboardViewModel : ViewModel() {

    // 输入状态
    private val _composingText = MutableStateFlow("")
    val composingText: StateFlow<String> = _composingText

    private val _candidates = MutableStateFlow<List<String>>(emptyList())
    val candidates: StateFlow<List<String>> = _candidates

    // 当前 Tab
    private val _currentTab = MutableStateFlow(KeyboardTab.PINYIN)
    val currentTab: StateFlow<KeyboardTab> = _currentTab

    // 话术库
    lateinit var scriptRepository: com.aiime.script.data.ScriptRepository
        private set

    // AI 服务
    var aiService: com.aiime.ai.service.MarketingAiService? = null

    fun initScriptDb(context: android.content.Context) {
        val db = androidx.room.Room.databaseBuilder(
            context.applicationContext,
            com.aiime.script.data.ScriptDatabase::class.java,
            com.aiime.script.data.ScriptDatabase.DATABASE_NAME,
        ).build()
        scriptRepository = com.aiime.script.data.ScriptRepository(db.scriptDao())
    }

    fun updateComposing(text: String) {
        _composingText.value = text
    }

    fun updateCandidates(list: List<String>) {
        _candidates.value = list
    }

    fun switchTab(tab: KeyboardTab) {
        _currentTab.value = tab
    }
}

enum class KeyboardTab(val displayName: String, val icon: String) {
    PINYIN("拼音", "⌨"),
    SCRIPTS("话术", "📋"),
    AI("AI", "✨"),
}
