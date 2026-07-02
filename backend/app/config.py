# -*- coding: utf-8 -*-
"""配置文件 - 请填入你的 DeepSeek API Key"""
import os

# DeepSeek API 配置
# 申请地址: https://platform.deepseek.com/
# Phase 1 自用额度: 注册送 ¥10-15，可以跑很久
DEEPSEEK_API_KEY = os.getenv("DEEPSEEK_API_KEY", "")
DEEPSEEK_BASE_URL = "https://api.deepseek.com"

# DeepSeek 模型
MODEL_CHAT = "deepseek-chat"      # 通用对话/润色
MODEL_REASONER = "deepseek-reasoner"  # 深度思考推理

# 服务器配置
HOST = "0.0.0.0"
PORT = 8000

# 数据库
DATABASE_URL = "sqlite+aiosqlite:///data/phrases.db"

# Tailscale/内网配置
# 启动后，在同一 Tailscale 网络下的手机可以通过 http://<你的Tailscale IP>:8000 访问
# 你的 Tailscale IP 可以在 Tailscale 控制台查看，或运行 `tailscale ip -4`
