# AI 营销输入法

一个面向销售/营销人员的 Android 输入法，集成话术库、AI 润色/回复、拼音输入。

## 仓库结构

- `android/` — Android 输入法 App（Kotlin + Compose + Room + OkHttp）
- `backend/` — FastAPI 后端（话术管理 + AI 润色，默认端口 8000）
- `.github/workflows/android.yml` — GitHub Actions 自动构建 APK

## 快速开始

### 后端（本地）

```bash
cd backend
pip install -r requirements.txt
python seed_data.py
uvicorn app.main:app --host 0.0.0.0 --port 8000
```

访问后台：`http://localhost:8000/admin`

### Android 构建

每次 push 到 `main` 分支会自动触发 GitHub Actions 构建 APK。

手动构建（需要 JDK 21 + Android SDK）：

```bash
cd android
./gradlew assembleDebug
```

## 技术栈

- Android：Kotlin + Jetpack Compose + Room + OkHttp
- AI：DeepSeek V3（润色）/ R1（深度回复），通过后端代理
- 拼音：预留 librime-android 接口，当前为模拟实现

## 许可证

本项目基于 BiBi Keyboard（Apache 2.0）改造，保留了原项目的输入法框架与语音输入能力。
