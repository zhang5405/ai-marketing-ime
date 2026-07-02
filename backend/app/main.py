# -*- coding: utf-8 -*-
"""AI 营销输入法后端 - FastAPI 入口"""
import os
from contextlib import asynccontextmanager
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.core.database import init_db
from app.api import scripts, ai
from app.core.config import settings


@asynccontextmanager
async def lifespan(app: FastAPI):
    # 启动时初始化数据库
    init_db()
    print("✅ 数据库初始化完成 (scripts.db)")
    yield
    print("👋 服务关闭")


app = FastAPI(
    title=settings.PROJECT_NAME,
    description="话术库管理 + DeepSeek AI 润色，SSE 流式输出",
    version=settings.VERSION,
    lifespan=lifespan,
)

# CORS：允许手机浏览器跨域访问（本地调试用）
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # Tailscale 环境下允许所有来源
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 注册 API 路由
app.include_router(scripts.router)
app.include_router(ai.router)


@app.get("/")
async def root():
    return {
        "name": settings.PROJECT_NAME,
        "version": settings.VERSION,
        "docs": "/docs",
        "admin": "/admin",
        "status": "running",
        "note": "请访问 /docs 查看 API 文档，或 /admin 进入话术管理后台",
    }


@app.get("/health")
async def health():
    return {"status": "ok", "version": settings.VERSION}


@app.get("/admin")
async def admin():
    from fastapi.responses import FileResponse
    path = os.path.join(os.path.dirname(__file__), "../static/admin.html")
    return FileResponse(path)


@app.get("/api")
async def api_info():
    return {
        "name": settings.PROJECT_NAME,
        "version": settings.VERSION,
        "endpoints": {
            "scripts_list": "GET  /api/scripts",
            "scripts_create": "POST /api/scripts",
            "scripts_detail": "GET  /api/scripts/{id}",
            "scripts_update": "PUT  /api/scripts/{id}",
            "scripts_delete": "DELETE /api/scripts/{id}",
            "scripts_favorite": "PATCH /api/scripts/{id}/favorite",
            "categories_list": "GET  /api/scripts/categories/list",
            "ai_process": "POST /api/ai/process",
            "ai_stream": "POST /api/ai/stream",
            "ai_modes": "GET  /api/ai/modes",
        },
    }


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(
        "app.main:app",
        host=settings.HOST,
        port=settings.PORT,
        reload=True,
    )
