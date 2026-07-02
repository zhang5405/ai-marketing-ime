# -*- coding: utf-8 -*-
from datetime import datetime
from typing import Optional
from pydantic import BaseModel


# ========== Category Schemas ==========
class CategoryCreate(BaseModel):
    name: str
    icon: str = "📁"
    sort_order: int = 0


class CategoryUpdate(BaseModel):
    name: Optional[str] = None
    icon: Optional[str] = None
    sort_order: Optional[int] = None


class CategoryOut(BaseModel):
    id: int
    name: str
    icon: str
    sort_order: int
    created_at: datetime
    phrase_count: int = 0

    class Config:
        from_attributes = True


# ========== Phrase Schemas ==========
class PhraseCreate(BaseModel):
    category_id: int
    title: str
    content: str
    tags: str = ""


class PhraseUpdate(BaseModel):
    category_id: Optional[int] = None
    title: Optional[str] = None
    content: Optional[str] = None
    tags: Optional[str] = None


class PhraseOut(BaseModel):
    id: int
    category_id: int
    title: str
    content: str
    tags: str
    usage_count: int
    created_at: datetime
    updated_at: datetime

    class Config:
        from_attributes = True


class PhraseWithCategory(PhraseOut):
    category_name: str = ""


# ========== AI Schemas ==========
class PolishRequest(BaseModel):
    text: str
    style: str = "natural"  # natural / professional / friendly / sales
    mode: str = "polish"     # polish / reply


class AIFeedback(BaseModel):
    choices: list
    usage: Optional[dict] = None
