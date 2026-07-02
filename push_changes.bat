@echo off
REM ============================================================
REM AI营销输入法 - Git 推送脚本
REM 运行前请确保已安装 Git (https://git-scm.com/download/win)
REM ============================================================

echo ===== 检查 Git =====
git --version >nul 2>&1
if errorlevel 1 (
    echo [错误] 未找到 Git！请先安装 Git。
    echo 下载地址：https://git-scm.com/download/win
    pause
    exit /b 1
)

cd /d "%~dp0"

echo ===== 拉取最新代码 =====
git pull origin main

echo ===== 添加所有修改 =====
git add android/gradle/wrapper/gradle-wrapper.properties
git add android/app/src/main/java/com/osfans/trime/core/
git add android/app/src/main/jniLibs/
git add android/app/src/main/assets/rime/
git add android/app/src/main/java/com/aiime/ime/RimeEngine.kt
git add android/app/src/main/java/com/aiime/script/data/ScriptRepository.kt

echo ===== 删除废弃文件 =====
git rm android/app/src/main/assets/rime/default.custom.yaml

echo ===== 查看变更 =====
git status

echo ===== 提交 =====
git commit -m "feat: 集成 RIME 拼音引擎 + 修复 CI 构建

- 降级 Gradle 9.4.1 -> 8.7 (兼容 AGP 8.2.2)
- 创建 JNI 桥接层 (com.osfans.trime.core.RimeJni + RimeProto)
- 复制 librime_jni.so 三架构到 jniLibs/
- 从 Trime APK 提取 RIME 配置/词库到 assets/rime/
- 重写 RimeEngine.kt 使用真实 JNI 引擎
- 修复 ScriptRepository 缺失 insertAll 方法"

echo ===== 推送到 GitHub =====
git push origin main

echo ===== 完成！ =====
echo 可以在 https://github.com/zhang5405/ai-marketing-ime/actions 查看构建状态
pause
