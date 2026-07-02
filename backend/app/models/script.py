"""话术数据库模型"""
import enum
from sqlalchemy import Column, Integer, String, Text, Boolean, DateTime, Enum, func
from ..core.database import Base


class ScriptCategory(str, enum.Enum):
    """话术分类"""
    GREETING = "greeting"          # 开场白
    PRODUCT = "product"            # 产品介绍
    OBJECTION = "objection"        # 异议处理
    CLOSING = "closing"            # 促成成交
    FOLLOW_UP = "follow_up"        # 跟进
    SOCIAL = "social"              # 朋友圈/社交
    AFTER_SALES = "after_sales"    # 售后
    OTHER = "other"                # 其他


class Script(Base):
    """话术表"""
    __tablename__ = "scripts"

    id = Column(Integer, primary_key=True, autoincrement=True)
    title = Column(String(200), nullable=False, comment="话术标题")
    content = Column(Text, nullable=False, comment="话术内容")
    category = Column(
        Enum(ScriptCategory),
        nullable=False,
        default=ScriptCategory.OTHER,
        comment="话术分类"
    )
    tags = Column(String(500), default="", comment="标签，逗号分隔")
    is_favorite = Column(Boolean, default=False, comment="是否收藏")
    usage_count = Column(Integer, default=0, comment="使用次数")
    created_at = Column(
        DateTime(timezone=True),
        server_default=func.now(),
        comment="创建时间"
    )
    updated_at = Column(
        DateTime(timezone=True),
        server_default=func.now(),
        onupdate=func.now(),
        comment="更新时间"
    )

    def to_dict(self):
        return {
            "id": self.id,
            "title": self.title,
            "content": self.content,
            "category": self.category.value if self.category else None,
            "tags": [t.strip() for t in self.tags.split(",") if t.strip()] if self.tags else [],
            "is_favorite": self.is_favorite,
            "usage_count": self.usage_count,
            "created_at": self.created_at.isoformat() if self.created_at else None,
            "updated_at": self.updated_at.isoformat() if self.updated_at else None,
        }
