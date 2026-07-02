# -*- coding: utf-8 -*-
"""话术 CRUD API"""
from fastapi import APIRouter, HTTPException, Query
from sqlalchemy import select, func, or_
from sqlalchemy.ext.asyncio import AsyncSession

from app.database import get_db
from app.models import Phrase, Category
from app.schemas import PhraseCreate, PhraseUpdate, PhraseOut, PhraseWithCategory

router = APIRouter(prefix="/api/phrases", tags=["话术管理"])


@router.get("", response_model=list[PhraseWithCategory])
async def list_phrases(
    category_id: int | None = None,
    keyword: str | None = None,
    page: int = Query(1, ge=1),
    page_size: int = Query(20, ge=1, le=100),
    db: AsyncSession = get_db,
):
    """话术列表，支持分类过滤、关键词搜索、分页"""
    query = select(Phrase, Category.name.label("category_name")).outerjoin(Category)
    if category_id:
        query = query.where(Phrase.category_id == category_id)
    if keyword:
        kw = f"%{keyword}%"
        query = query.where(
            or_(Phrase.title.ilike(kw), Phrase.content.ilike(kw), Phrase.tags.ilike(kw))
        )
    query = query.order_by(Phrase.usage_count.desc(), Phrase.id.desc())
    query = query.offset((page - 1) * page_size).limit(page_size)
    result = await db.execute(query)
    rows = result.all()
    return [
        PhraseWithCategory(
            id=p.id, category_id=p.category_id, title=p.title,
            content=p.content, tags=p.tags, usage_count=p.usage_count,
            created_at=p.created_at, updated_at=p.updated_at,
            category_name=cat_name or "",
        )
        for p, cat_name in rows
    ]


@router.get("/{phrase_id}", response_model=PhraseOut)
async def get_phrase(phrase_id: int, db: AsyncSession = get_db):
    phrase = await db.get(Phrase, phrase_id)
    if not phrase:
        raise HTTPException(status_code=404, detail="话术不存在")
    return phrase


@router.post("", response_model=PhraseOut, status_code=201)
async def create_phrase(data: PhraseCreate, db: AsyncSession = get_db):
    # 验证分类存在
    cat = await db.get(Category, data.category_id)
    if not cat:
        raise HTTPException(status_code=400, detail="分类不存在")
    phrase = Phrase(**data.model_dump())
    db.add(phrase)
    await db.commit()
    await db.refresh(phrase)
    return phrase


@router.post("/batch", response_model=list[PhraseOut], status_code=201)
async def batch_import_phrases(items: list[PhraseCreate], db: AsyncSession = get_db):
    """批量导入话术"""
    created = []
    for data in items:
        cat = await db.get(Category, data.category_id)
        if cat:
            phrase = Phrase(**data.model_dump())
            db.add(phrase)
            created.append(phrase)
    await db.commit()
    for p in created:
        await db.refresh(p)
    return created


@router.patch("/{phrase_id}", response_model=PhraseOut)
async def update_phrase(phrase_id: int, data: PhraseUpdate, db: AsyncSession = get_db):
    phrase = await db.get(Phrase, phrase_id)
    if not phrase:
        raise HTTPException(status_code=404, detail="话术不存在")
    for key, value in data.model_dump(exclude_unset=True).items():
        setattr(phrase, key, value)
    await db.commit()
    await db.refresh(phrase)
    return phrase


@router.delete("/{phrase_id}", status_code=204)
async def delete_phrase(phrase_id: int, db: AsyncSession = get_db):
    phrase = await db.get(Phrase, phrase_id)
    if not phrase:
        raise HTTPException(status_code=404, detail="话术不存在")
    await db.delete(phrase)
    await db.commit()


@router.post("/{phrase_id}/use", response_model=PhraseOut)
async def use_phrase(phrase_id: int, db: AsyncSession = get_db):
    """记录话术使用次数"""
    phrase = await db.get(Phrase, phrase_id)
    if not phrase:
        raise HTTPException(status_code=404, detail="话术不存在")
    phrase.usage_count = (phrase.usage_count or 0) + 1
    await db.commit()
    await db.refresh(phrase)
    return phrase
