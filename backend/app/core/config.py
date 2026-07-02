"""应用配置"""
import os
from dotenv import load_dotenv

load_dotenv()


class Settings:
    PROJECT_NAME: str = "AI营销输入法后端"
    VERSION: str = "1.0.0"

    # 数据库 - 默认使用 SQLite 本地开发，生产切换 PostgreSQL
    DATABASE_URL: str = os.getenv(
        "DATABASE_URL",
        "sqlite:///./data/scripts.db"
    )

    # DeepSeek API
    DEEPSEEK_API_KEY: str = os.getenv("DEEPSEEK_API_KEY", "")
    DEEPSEEK_API_BASE: str = os.getenv(
        "DEEPSEEK_API_BASE",
        "https://api.deepseek.com/v1"
    )

    # AI 模型配置
    POLISH_MODEL: str = "deepseek-chat"       # 润色用 V3
    DEEP_REPLY_MODEL: str = "deepseek-reasoner"  # 深度回复用 R1

    # Tailscale - 监听所有接口以便手机通过 Tailscale 访问
    HOST: str = "0.0.0.0"
    PORT: int = int(os.getenv("PORT", "8000"))


settings = Settings()
