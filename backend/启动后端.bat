@echo off
chcp 65001 >nul
echo.
echo ========================================
echo   AI 营销输入法 · 后端启动器
echo ========================================
echo.

:: 检查 Python
python --version >nul 2>&1
if errorlevel 1 (
    echo [错误] 未找到 Python，请先安装 Python 3.10+
    pause
    exit /b 1
)

:: 检查虚拟环境
if not exist "..\..\.workbuddy\binaries\python\envs\backend\Scripts\python.exe" (
    echo [信息] 创建 Python 虚拟环境...
    ..\..\.workbuddy\binaries\python\versions\3.13.12\python.exe -m venv ..\..\.workbuddy\binaries\python\envs\backend
    echo [信息] 安装依赖包...
    call ..\..\.workbuddy\binaries\python\envs\backend\Scripts\pip install -r requirements.txt -i https://pypi.tuna.tsinghua.edu.cn/simple --trusted-host pypi.tuna.tsinghua.edu.cn
)

:: 检查 API Key
echo.
set /p API_KEY="请输入 DeepSeek API Key（或直接回车跳过）: "
if not "%API_KEY%"=="" (
    set DEEPSEEK_API_KEY=%API_KEY%
    echo [信息] API Key 已设置（本次会话有效）
)

:: 初始化数据库
echo.
echo [1/2] 初始化数据库...
..\..\.workbuddy\binaries\python\envs\backend\Scripts\python.exe -X utf8 seed_data.py
echo.

:: 启动服务
echo [2/2] 启动后端服务...
echo.
echo 访问地址：
echo   本地管理后台: http://localhost:8000/admin
echo   API 文档:     http://localhost:8000/docs
echo   健康检查:     http://localhost:8000/health
echo.
echo 按 Ctrl+C 停止服务
echo.
..\..\.workbuddy\binaries\python\envs\backend\Scripts\python.exe -X utf8 -m uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload

pause
