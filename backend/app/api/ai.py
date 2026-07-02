"""AI 接口"""
from fastapi import APIRouter, HTTPException
from sse_starlette.sse import EventSourceResponse

from ..schemas import AIRequest, AIResponse
from ..services.ai_service import call_deepseek, call_deepseek_stream

router = APIRouter(prefix="/api/ai", tags=["AI"])


@router.post("/process", response_model=AIResponse)
async def process_text(req: AIRequest):
    """非流式 AI 处理 - 润色/回复/生成"""
    try:
        result = await call_deepseek(
            mode=req.mode,
            text=req.text,
            context=req.context,
            tone=req.tone,
            category=req.category,
        )
        return AIResponse(**result)
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"AI 调用失败: {str(e)}")


@router.post("/stream")
async def process_text_stream(req: AIRequest):
    """流式 AI 处理 - SSE 接口，逐步返回结果"""
    async def event_generator():
        try:
            async for chunk in call_deepseek_stream(
                mode=req.mode,
                text=req.text,
                context=req.context,
                tone=req.tone,
                category=req.category,
            ):
                yield {"data": chunk}
            yield {"data": "[DONE]"}
        except Exception as e:
            yield {"data": f"[ERROR] {str(e)}"}

    return EventSourceResponse(event_generator())


@router.get("/modes")
def get_modes():
    """获取支持的 AI 模式"""
    return {
        "modes": [
            {
                "id": "polish",
                "name": "润色优化",
                "description": "将输入文字润色为专业的营销话术",
                "icon": "sparkles",
            },
            {
                "id": "reply",
                "name": "智能回复",
                "description": "根据客户问题生成多个回复方案",
                "icon": "chat",
            },
            {
                "id": "generate",
                "name": "生成话术",
                "description": "根据场景描述生成新话术",
                "icon": "wand",
            },
        ],
        "tones": [
            {"id": "professional", "name": "专业"},
            {"id": "warm", "name": "亲切"},
            {"id": "persuasive", "name": "说服力强"},
        ],
    }
