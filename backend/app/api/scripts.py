"""话术库 API"""
from typing import Optional
from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.orm import Session
from sqlalchemy import or_

from ..core.database import get_db
from ..models.script import Script, ScriptCategory
from ..schemas import (
    ScriptCreate,
    ScriptUpdate,
    ScriptResponse,
    ScriptListResponse,
)

router = APIRouter(prefix="/api/scripts", tags=["话术库"])


@router.get("", response_model=ScriptListResponse)
def list_scripts(
    category: Optional[str] = Query(None, description="按分类筛选"),
    keyword: Optional[str] = Query(None, description="搜索关键词"),
    favorite: Optional[bool] = Query(None, description="只看收藏"),
    page: int = Query(1, ge=1),
    page_size: int = Query(20, ge=1, le=100),
    db: Session = Depends(get_db),
):
    """获取话术列表，支持分类筛选、关键词搜索、收藏过滤"""
    query = db.query(Script)

    if category and category != "all":
        try:
            cat = ScriptCategory(category)
            query = query.filter(Script.category == cat)
        except ValueError:
            pass

    if keyword:
        kw = f"%{keyword}%"
        query = query.filter(
            or_(
                Script.title.like(kw),
                Script.content.like(kw),
                Script.tags.like(kw),
            )
        )

    if favorite:
        query = query.filter(Script.is_favorite == True)

    total = query.count()
    items = (
        query
        .order_by(Script.is_favorite.desc(), Script.usage_count.desc(), Script.created_at.desc())
        .offset((page - 1) * page_size)
        .limit(page_size)
        .all()
    )

    return ScriptListResponse(
        total=total,
        items=[ScriptResponse.model_validate(s) for s in items],
    )


@router.get("/{script_id}", response_model=ScriptResponse)
def get_script(script_id: int, db: Session = Depends(get_db)):
    """获取单条话术详情"""
    script = db.query(Script).filter(Script.id == script_id).first()
    if not script:
        raise HTTPException(status_code=404, detail="话术不存在")
    # 增加使用计数
    script.usage_count += 1
    db.commit()
    return ScriptResponse.model_validate(script)


@router.post("", response_model=ScriptResponse, status_code=201)
def create_script(data: ScriptCreate, db: Session = Depends(get_db)):
    """创建新话术"""
    try:
        cat = ScriptCategory(data.category)
    except ValueError:
        cat = ScriptCategory.OTHER

    script = Script(
        title=data.title,
        content=data.content,
        category=cat,
        tags=",".join(data.tags) if data.tags else "",
    )
    db.add(script)
    db.commit()
    db.refresh(script)
    return ScriptResponse.model_validate(script)


@router.put("/{script_id}", response_model=ScriptResponse)
def update_script(script_id: int, data: ScriptUpdate, db: Session = Depends(get_db)):
    """更新话术"""
    script = db.query(Script).filter(Script.id == script_id).first()
    if not script:
        raise HTTPException(status_code=404, detail="话术不存在")

    update_data = data.model_dump(exclude_unset=True)
    if "tags" in update_data and update_data["tags"] is not None:
        update_data["tags"] = ",".join(update_data["tags"])
    if "category" in update_data and update_data["category"] is not None:
        try:
            update_data["category"] = ScriptCategory(update_data["category"])
        except ValueError:
            pass

    for key, value in update_data.items():
        setattr(script, key, value)

    db.commit()
    db.refresh(script)
    return ScriptResponse.model_validate(script)


@router.patch("/{script_id}/favorite", response_model=ScriptResponse)
def toggle_favorite(script_id: int, db: Session = Depends(get_db)):
    """切换收藏状态"""
    script = db.query(Script).filter(Script.id == script_id).first()
    if not script:
        raise HTTPException(status_code=404, detail="话术不存在")
    script.is_favorite = not script.is_favorite
    db.commit()
    db.refresh(script)
    return ScriptResponse.model_validate(script)


@router.delete("/{script_id}", status_code=204)
def delete_script(script_id: int, db: Session = Depends(get_db)):
    """删除话术"""
    script = db.query(Script).filter(Script.id == script_id).first()
    if not script:
        raise HTTPException(status_code=404, detail="话术不存在")
    db.delete(script)
    db.commit()


@router.get("/categories/list", response_model=list[str])
def list_categories():
    """获取所有话术分类"""
    return [c.value for c in ScriptCategory]
