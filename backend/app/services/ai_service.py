"""AI 服务 - DeepSeek API 代理"""
import json
from typing import AsyncGenerator

import httpx

from ..core.config import settings

# ========================
#  营销 AI Prompt 模板
# ========================

TONE_MAP = {
    "professional": "保持专业、可信的语气",
    "warm": "保持温暖、亲切的语气",
    "persuasive": "保持有说服力、能促单的语气",
}

CATEGORY_MAP = {
    "greeting": "开场白/打招呼场景",
    "product": "产品介绍/功能说明场景",
    "objection": "客户异议处理场景",
    "closing": "逼单/促成成交场景",
    "follow_up": "客户跟进/回访场景",
}

SYSTEM_PROMPTS = {
    "polish": """你是一个专业的营销话术润色助手。
请将用户提供的文字润色为专业、自然的营销话术。

要求：
1. 保持原意不变，优化表达方式
2. 去除冗余和口语化表达
3. 融入营销技巧（FAB法则、痛点共鸣、场景化描述）
4. 输出直接就是润色后的话术，不要加任何解释
5. 结果长度应与原文相近或稍长，不要过分扩充""",

    "reply": """你是一个专业的销售话术顾问，帮助销售人员回复客户。

请根据用户提供的客户问题，生成3个不同角度的话术回复供选择：

要求：
1. 识别客户问题的核心需求和潜在顾虑
2. 每个回复标注【方案一/二/三】并使用不同的切入角度（价值导向/情感共鸣/专业分析）
3. 融入逼单技巧和促成话术
4. 回复要自然、口语化，像真人聊天，不要像机器人
5. 每条回复控制在50-150字""",

    "generate": """你是一个专业的营销话术库生成助手。

请根据用户描述的场景，生成5条可直接使用的营销话术。

要求：
1. 每条话术标注编号【1/2/3/4/5】
2. 每条话术控制在50-200字
3. 覆盖不同的切入角度和风格
4. 要自然、口语化、有说服力
5. 按照开场→痛点→方案→促成的逻辑组织""",
}


def _build_system_prompt(mode: str, tone: str, category: str) -> str:
    """构建完整的 system prompt"""
    base = SYSTEM_PROMPTS.get(mode, SYSTEM_PROMPTS["polish"])
    extras = []

    if tone and tone in TONE_MAP:
        extras.append(TONE_MAP[tone])

    if category and category in CATEGORY_MAP:
        extras.append(f"场景：{CATEGORY_MAP[category]}")

    if extras:
        base += "\n\n" + "。".join(extras) + "。"

    return base


def _build_user_prompt(mode: str, text: str, context: str) -> str:
    """构建 user prompt"""
    if mode == "reply":
        prompt = f"客户说：\n{text}"
        if context:
            prompt += f"\n\n背景信息：{context}"
        prompt += "\n\n请生成回复话术。"
    elif mode == "generate":
        prompt = f"场景描述：\n{text}"
        if context:
            prompt += f"\n\n产品/行业信息：{context}"
        prompt += "\n\n请生成营销话术。"
    else:
        prompt = f"请润色以下文字：\n{text}"
        if context:
            prompt += f"\n\n参考背景：{context}"
    return prompt


async def call_deepseek(
    mode: str,
    text: str,
    context: str = None,
    tone: str = "professional",
    category: str = None,
) -> dict:
    """调用 DeepSeek API（非流式）"""
    if not settings.DEEPSEEK_API_KEY:
        raise ValueError("DeepSeek API Key 未配置。请设置环境变量 DEEPSEEK_API_KEY 后重启服务。")

    system_prompt = _build_system_prompt(mode, tone, category)
    user_prompt = _build_user_prompt(mode, text, context)

    model = settings.POLISH_MODEL
    if mode == "reply":
        model = settings.DEEP_REPLY_MODEL

    async with httpx.AsyncClient(timeout=60.0) as client:
        response = await client.post(
            f"{settings.DEEPSEEK_API_BASE}/chat/completions",
            headers={
                "Authorization": f"Bearer {settings.DEEPSEEK_API_KEY}",
                "Content-Type": "application/json",
            },
            json={
                "model": model,
                "messages": [
                    {"role": "system", "content": system_prompt},
                    {"role": "user", "content": user_prompt},
                ],
                "temperature": 0.7,
                "max_tokens": 2048,
            },
        )
        response.raise_for_status()
        data = response.json()

        return {
            "result": data["choices"][0]["message"]["content"].strip(),
            "mode": mode,
            "tokens_used": data.get("usage", {}).get("total_tokens", 0),
        }


async def call_deepseek_stream(
    mode: str,
    text: str,
    context: str = None,
    tone: str = "professional",
    category: str = None,
) -> AsyncGenerator[str, None]:
    """调用 DeepSeek API（流式 SSE）"""
    if not settings.DEEPSEEK_API_KEY:
        raise ValueError("DeepSeek API Key 未配置。请设置环境变量 DEEPSEEK_API_KEY 后重启服务。")

    system_prompt = _build_system_prompt(mode, tone, category)
    user_prompt = _build_user_prompt(mode, text, context)

    model = settings.POLISH_MODEL
    if mode == "reply":
        model = settings.DEEP_REPLY_MODEL

    async with httpx.AsyncClient(timeout=90.0) as client:
        async with client.stream(
            "POST",
            f"{settings.DEEPSEEK_API_BASE}/chat/completions",
            headers={
                "Authorization": f"Bearer {settings.DEEPSEEK_API_KEY}",
                "Content-Type": "application/json",
            },
            json={
                "model": model,
                "messages": [
                    {"role": "system", "content": system_prompt},
                    {"role": "user", "content": user_prompt},
                ],
                "temperature": 0.7,
                "max_tokens": 2048,
                "stream": True,
            },
        ) as response:
            response.raise_for_status()
            async for line in response.aiter_lines():
                if line.startswith("data: "):
                    data = line[6:]
                    if data == "[DONE]":
                        break
                    try:
                        chunk = json.loads(data)
                        delta = chunk["choices"][0].get("delta", {})
                        content = delta.get("content", "")
                        if content:
                            yield content
                    except (json.JSONDecodeError, KeyError, IndexError):
                        continue
