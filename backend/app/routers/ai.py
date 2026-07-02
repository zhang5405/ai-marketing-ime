# -*- coding: utf-8 -*-
"""AI 润色接口 - SSE 流式"""
import asyncio
from fastapi import APIRouter, HTTPException
from fastapi.responses import StreamingResponse
from openai import AsyncOpenAI

from app.config import DEEPSEEK_API_KEY, DEEPSEEK_BASE_URL, MODEL_CHAT, MODEL_REASONER
from app.schemas import PolishRequest

router = APIRouter(prefix="/api/ai", tags=["AI 能力"])

# 润色风格 prompt 模板
STYLE_PROMPTS = {
    "natural": "请对以下文字进行润色，使其表达更加自然流畅，符合日常聊天风格：\n\n",
    "professional": "请对以下文字进行润色，使其表达更加专业、正式，适合商务沟通：\n\n",
    "friendly": "请对以下文字进行润色，使其表达更加亲切友好，有温度：\n\n",
    "sales": "请对以下营销话术进行优化，提升成交转化率，保持专业但有感染力：\n\n",
}

REPLY_PROMPTS = {
    "natural": "作为聊天助手，请帮我生成一条自然的回复消息：\n\n用户说：",
    "professional": "作为专业客服，请帮我生成一条正式的回复消息：\n\n用户说：",
    "friendly": "作为热情的销售，请帮我生成一条有感染力的回复：\n\n用户说：",
    "sales": "作为金牌销售，请帮我生成一条高转化率的营销回复：\n\n用户说：",
}


def build_prompt(text: str, style: str, mode: str) -> str:
    if mode == "reply":
        template = REPLY_PROMPTS.get(style, REPLY_PROMPTS["natural"])
    else:
        template = STYLE_PROMPTS.get(style, STYLE_PROMPTS["natural"])
    return f"{template}{text}"


@router.post("/polish")
async def ai_polish(request: PolishRequest):
    """AI 润色 / AI 回复，SSE 流式输出"""
    if not DEEPSEEK_API_KEY:
        raise HTTPException(
            status_code=500,
            detail="DeepSeek API Key 未配置。请设置环境变量 DEEPSEEK_API_KEY 后重启服务。",
        )
    if not request.text.strip():
        raise HTTPException(status_code=400, detail="输入文字不能为空")

    client = AsyncOpenAI(api_key=DEEPSEEK_API_KEY, base_url=DEEPSEEK_BASE_URL)
    prompt = build_prompt(request.text, request.style, request.mode)

    async def event_stream():
        try:
            stream = await client.chat.completions.create(
                model=MODEL_CHAT,
                messages=[{"role": "user", "content": prompt}],
                stream=True,
                temperature=0.7,
                max_tokens=1024,
            )
            async for chunk in stream:
                delta = chunk.choices[0].delta.content or ""
                if delta:
                    yield f"data: {delta}\n\n"
                    await asyncio.sleep(0)  # 让出控制权
            yield "data: [DONE]\n\n"
        except Exception as e:
            yield f"data: [ERROR] {str(e)}\n\n"

    return StreamingResponse(
        event_stream(),
        media_type="text/event-stream; charset=utf-8",
        headers={
            "Cache-Control": "no-cache",
            "X-Accel-Buffering": "no",
        },
    )


@router.post("/polish-sync")
async def ai_polish_sync(request: PolishRequest):
    """AI 润色 - 非流式版本（方便调试）"""
    if not DEEPSEEK_API_KEY:
        raise HTTPException(
            status_code=500,
            detail="DeepSeek API Key 未配置。请设置环境变量 DEEPSEEK_API_KEY 后重启服务。",
        )
    if not request.text.strip():
        raise HTTPException(status_code=400, detail="输入文字不能为空")

    client = AsyncOpenAI(api_key=DEEPSEEK_API_KEY, base_url=DEEPSEEK_BASE_URL)
    prompt = build_prompt(request.text, request.style, request.mode)

    try:
        response = await client.chat.completions.create(
            model=MODEL_CHAT,
            messages=[{"role": "user", "content": prompt}],
            stream=False,
            temperature=0.7,
            max_tokens=1024,
        )
        return {
            "result": response.choices[0].message.content,
            "usage": {
                "prompt_tokens": response.usage.prompt_tokens,
                "completion_tokens": response.usage.completion_tokens,
                "total_tokens": response.usage.total_tokens,
            },
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.get("/models")
async def list_models():
    """查询可用模型"""
    return {
        "chat": MODEL_CHAT,
        "reasoner": MODEL_REASONER,
        "configured": bool(DEEPSEEK_API_KEY),
    }
