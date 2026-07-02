"""Pydantic 数据模型"""
from datetime import datetime
from typing import Optional, List, Any
from pydantic import BaseModel, Field, field_validator, ConfigDict


# ========== 话术 ==========

class ScriptCreate(BaseModel):
    title: str = Field(..., min_length=1, max_length=200, description="话术标题")
    content: str = Field(..., min_length=1, description="话术内容")
    category: str = Field(default="other", description="分类")
    tags: List[str] = Field(default_factory=list, description="标签")


class ScriptUpdate(BaseModel):
    title: Optional[str] = Field(None, max_length=200)
    content: Optional[str] = None
    category: Optional[str] = None
    tags: Optional[List[str]] = None
    is_favorite: Optional[bool] = None


class ScriptResponse(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: int
    title: str
    content: str
    category: Any  # can be str or enum
    tags: List[str]
    is_favorite: bool
    usage_count: int
    created_at: Optional[str] = None
    updated_at: Optional[str] = None

    @field_validator("category", mode="before")
    @classmethod
    def coerce_category(cls, v: Any) -> str:
        if hasattr(v, "value"):
            return v.value
        return str(v) if v else "other"

    @field_validator("tags", mode="before")
    @classmethod
    def coerce_tags(cls, v: Any) -> List[str]:
        if isinstance(v, list):
            return v
        if isinstance(v, str):
            return [t.strip() for t in v.split(",") if t.strip()]
        return []

    @field_validator("created_at", "updated_at", mode="before")
    @classmethod
    def coerce_datetime(cls, v: Any) -> Optional[str]:
        if v is None:
            return None
        if isinstance(v, datetime):
            return v.isoformat()
        return str(v)


class ScriptListResponse(BaseModel):
    total: int
    items: List[ScriptResponse]


# ========== AI ==========

class AIRequest(BaseModel):
    """AI 润色/回复请求"""
    text: str = Field(..., min_length=1, description="待处理的文本")
    mode: str = Field(
        default="polish",
        description="模式: polish(润色), reply(深度回复), generate(生成新话术)"
    )
    context: Optional[str] = Field(
        default=None,
        description="额外的上下文信息（客户问题、产品信息等）"
    )
    tone: Optional[str] = Field(
        default="professional",
        description="语气: professional(专业), warm(亲切), persuasive(说服力强)"
    )
    category: Optional[str] = Field(
        default=None,
        description="话术分类导向: greeting/product/objection/closing/follow_up"
    )


class AIResponse(BaseModel):
    result: str = Field(..., description="AI 处理结果")
    mode: str
    tokens_used: int = 0
