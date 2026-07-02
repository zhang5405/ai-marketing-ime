# AI 营销输入法 — 项目构建指南 v2.0

## 项目概述

基于 BiBi Keyboard (Apache 2.0) 二次开发，新增"营销话术库 + AI营销助手"模块。
三面板键盘：主键盘（语音/MIC） + 话术库面板 + AI营销面板。

## 完整项目结构

```
ai输入法/
├── BiBi-Keyboard/          # 二次开发基础框架（Apache 2.0）
│   ├── app/
│   │   ├── build.gradle.kts      # 已含 kotlinx-serialization
│   │   ├── libs/
│   │   │   └── sherpa-onnx-1.13.3.aar  # ✅ 本地ASR引擎
│   │   └── src/main/
│   │       ├── AndroidManifest.xml
│   │       ├── java/com/brycewg/asrkb/
│   │       │   ├── asr/          # ASR引擎 (68文件，11引擎)
│   │       │   ├── ime/          # IME框架 (已修改4文件)
│   │       │   ├── marketing/    # ⭐ 新增营销模块
│   │       │   │   ├── MarketingDataRepository.kt    # 话术数据仓库
│   │       │   │   ├── ScriptPanelController.kt      # 话术面板控制器
│   │       │   │   └── MarketingAiPanelController.kt  # AI面板控制器
│   │       │   ├── ui/           # 设置页/悬浮服务
│   │       │   └── store/        # 数据存储
│   │       └── res/
│   │           ├── layout/
│   │           │   ├── layout_script_panel.xml       # ⭐ 话术面板布局
│   │           │   ├── layout_marketing_ai_panel.xml  # ⭐ AI面板布局
│   │           │   └── item_script.xml               # ⭐ 话术列表项
│   │           ├── values/
│   │           │   ├── marketing_ids.xml             # ⭐ 新增ID
│   │           │   └── marketing_strings.xml         # ⭐ 新增文案
│   │           └── drawable/
│   │               ├── bg_chip_selected.xml          # ⭐ Chip选中背景
│   │               └── bg_chip_unselected.xml        # ⭐ Chip未选背景
│   └── gradle/wrapper/
├── backend/                 # Python 后端
│   ├── app/
│   │   ├── main.py              # FastAPI 入口
│   │   ├── api/scripts.py      # 话术 CRUD API
│   │   ├── api/ai.py            # AI 代理 SSE 接口
│   │   ├── services/ai_service.py # DeepSeek 客户端
│   │   ├── models/script.py     # 话术数据模型
│   │   └── core/config.py       # 配置管理
│   ├── seed_data.py            # 15条种子数据
│   ├── requirements.txt
│   └── .env                    # API Key 配置
├── deps/                    # 外部依赖
│   ├── rime_libs/
│   │   ├── arm64-v8a/librime_jni.so    # ✅ 5.2MB
│   │   ├── armeabi-v7a/librime_jni.so  # ✅ 3.6MB
│   │   └── x86_64/librime_jni.so       # ✅ 5.2MB
│   └── trime-*.apk / trime-source.zip  # Trime参考文件
├── AI营销输入法_BiBI改造技术方案.html
└── PROJECT_GUIDE.md         # 本文档
```

## 修改的文件清单

| 文件 | 修改内容 |
|------|---------|
| `ImeKeyboardViewFactory.kt` | 添加 createScriptPanel() / createMarketingAiPanel() 方法，加入面板构造链 |
| `ImeViewRefs.kt` | 添加 layoutScriptPanel / layoutMarketingAiPanel 视图引用 |
| `AsrKeyboardService.kt` | 添加 ScriptPanelController / MarketingAiPanelController 实例化、面板显示/隐藏、Ext按钮绑定（Ext1→话术库，Ext2→AI营销） |
| `ImeMainKeyboardBinder.kt` | 添加 showScriptPanel / showMarketingAiPanel 回调参数 |

## 编译步骤

### 前置条件
- JDK 21（已安装：`C:\android-tools\jdk\jdk21.0.11_10`）
- Android SDK (compileSdk 37) — 需要安装
- Android NDK（如需要本地ASR模型）

### 编译命令
```bash
cd BiBi-Keyboard

# 设置环境变量
export JAVA_HOME="C:\android-tools\jdk\jdk21.0.11_10"
export ANDROID_HOME="你的SDK路径"

# 编译 debug APK
./gradlew assembleDebug
```

### 后端启动
```bash
cd backend
# 编辑 .env 填入 DeepSeek API Key
pip install -r requirements.txt
python seed_data.py          # 初始化种子数据
uvicorn app.main:app --reload  # 启动服务 (http://localhost:8000/docs)
```

## 使用方式

编译安装后：
1. 打开任意输入框
2. 键盘顶部 Ext1 按钮 → 进入话术库面板（分类浏览/搜索/收藏/一键输入）
3. 键盘顶部 Ext2 按钮 → 进入 AI 营销助手面板（润色/回复/生成三种模式）
4. 原有语音功能完整保留

## 版本历史

| 日期 | 里程碑 |
|------|--------|
| 2026-06-29 | 确定 BiBi Keyboard 为二次开发基础 |
| 2026-06-29 | 后端搭建完成 + 种子数据 + API 测试通过 |
| 2026-06-29 | Android 端代码完整实现 |
| 2026-06-30 | 下载 BiBi Keyboard 源码 + sherpa-onnx + Trime APK |
| 2026-06-30 | 提取 librime_jni.so 三架构 |
| 2026-06-30 | 营销面板完整整合到 BiBi 项目 |
