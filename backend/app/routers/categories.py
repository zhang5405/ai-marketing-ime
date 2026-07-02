# -*- coding: utf-8 -*-
"""话术分类管理 API"""
from fastapi import APIRouter, HTTPException
from sqlalchemy import select, func
from sqlalchemy.ext.asyncio import AsyncSession

from app.database import get_db
from app.models import Category
from app.schemas import CategoryCreate, CategoryUpdate, CategoryOut

router = APIRouter(prefix="/api/categories", tags=["分类管理"])


@router.get("", response_model=list[CategoryOut])
async def list_categories(db: AsyncSession = get_db):
    """获取所有分类（带话术数量）"""
    query = (
        select(
            Category,
            func.count(Category.phrases).label("phrase_count")
        )
        .outerjoin(Category.phrases)
        .group_by(Category.id)
        .order_by(Category.sort_order, Category.id)
    )
    result = await db.execute(query)
    rows = result.all()
    return [
        CategoryOut(
            id=cat.id,
            name=cat.name,
            icon=cat.icon,
            sort_order=cat.sort_order,
            created_at=cat.created_at,
            phrase_count=count,
        )
        for cat, count in rows
    ]


@router.get("/{cat_id}", response_model=CategoryOut)
async def get_category(cat_id: int, db: AsyncSession = get_db):
    cat = await db.get(Category, cat_id)
    if not cat:
        raise HTTPException(status_code=404, detail="分类不存在")
    result = await db.execute(
        select(func.count(Phrase.id)).where(Phrase.category_id == cat_id)
    )
    count = result.scalar() or 0
    return CategoryOut(
        id=cat.id, name=cat.name, icon=cat.icon,
        sort_order=cat.sort_order, created_at=cat.created_at,
        phrase_count=count
    )


@router.post("", response_model=CategoryOut, status_code=201)
async def create_category(data: CategoryCreate, db: AsyncSession = get_db):
    cat = Category(**data.model_dump())
    db.add(cat)
    await db.commit()
    await db.refresh(cat)
    return CategoryOut(
        id=cat.id, name=cat.name, icon=cat.icon,
        sort_order=cat.sort_order, created_at=cat.created_at,
        phrase_count=0
    )


@router.patch("/{cat_id}", response_model=CategoryOut)
async def update_category(cat_id: int, data: CategoryUpdate, db: AsyncSession = get_db):
    cat = await db.get(Category, cat_id)
    if not cat:
        raise HTTPException(status_code=404, detail="分类不存在")
    for key, value in data.model_dump(exclude_unset=True).items():
        setattr(cat, key, value)
    await db.commit()
    await db.refresh(cat)
    result = await db.execute(
        select(func.count(Phrase.id)).where(Phrase.category_id == cat_id)
    )
    count = result.scalar() or 0
    return CategoryOut(
        id=cat.id, name=cat.name, icon=cat.icon,
        sort_order=cat.sort_order, created_at=cat.created_at,
        phrase_count=count
    )


@router.delete("/{cat_id}", status_code=204)
async def delete_category(cat_id: int, db: AsyncSession = get_db):
    cat = await db.get(Category, cat_id)
    if not cat:
        raise HTTPException(status_code=404, detail="分类不存在")
    await db.delete(cat)
    await db.commit()
