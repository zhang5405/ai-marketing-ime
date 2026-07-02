#!/bin/bash
# AI 营销输入法 · 后端启动脚本

set -e

# 颜色
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo ""
echo "========================================"
echo "  AI 营销输入法 · 后端启动器"
echo "========================================"
echo ""

# Python 检查
if ! command -v python3 &> /dev/null; then
    echo -e "${YELLOW}[错误] 未找到 Python3${NC}"
    exit 1
fi

# 虚拟环境
VENV_PATH="$(dirname "$0")/../../.workbuddy/binaries/python/envs/backend"
PYTHON_PATH="$VENV_PATH/bin/python"

if [ ! -f "$PYTHON_PATH" ]; then
    echo -e "${YELLOW}[信息] 创建虚拟环境...${NC}"
    "$(dirname "$0")/../../.workbuddy/binaries/python/versions/3.13.12/python.exe" -m venv "$VENV_PATH"
fi

if [ ! -f "$VENV_PATH/bin/pip" ]; then
    echo -e "${YELLOW}[信息] 安装依赖包...${NC}"
    "$PYTHON_PATH" -m pip install -r requirements.txt -i https://pypi.tuna.tsinghua.edu.cn/simple --trusted-host pypi.tuna.tsinghua.edu.cn
fi

# API Key
echo ""
read -p "DeepSeek API Key（直接回车跳过）: " API_KEY
if [ -n "$API_KEY" ]; then
    export DEEPSEEK_API_KEY="$API_KEY"
    echo -e "${YELLOW}[信息] API Key 已设置（本次会话有效）${NC}"
fi

# 初始化数据库
echo ""
echo -e "${GREEN}[1/2] 初始化数据库...${NC}"
"$PYTHON_PATH" -X utf8 seed_data.py

# 启动服务
echo ""
echo -e "${GREEN}[2/2] 启动后端服务...${NC}"
echo ""
echo "访问地址："
echo "  本地管理后台: http://localhost:8000/admin"
echo "  API 文档:     http://localhost:8000/docs"
echo "  健康检查:     http://localhost:8000/health"
echo ""
echo "按 Ctrl+C 停止服务"
echo ""

"$PYTHON_PATH" -X utf8 -m uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
