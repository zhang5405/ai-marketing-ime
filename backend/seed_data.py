# -*- coding: utf-8 -*-
"""初始化种子数据 - 15 条示例话术"""
import sys
import os
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from app.core.database import SessionLocal, init_db
from app.models.script import Script, ScriptCategory

# 话术分类映射
CATEGORY_MAP = {
    "开场话术": ScriptCategory.GREETING,
    "产品介绍": ScriptCategory.PRODUCT,
    "价格谈判": ScriptCategory.OBJECTION,
    "异议处理": ScriptCategory.OBJECTION,
    "催单跟进": ScriptCategory.FOLLOW_UP,
    "售后维护": ScriptCategory.AFTER_SALES,
}

SEED_PHRASES = [
    # 开场话术
    {
        "category": "开场话术",
        "title": "热情问候破冰",
        "content": "哈喽~ 感谢您的信任！我是您的专属顾问小美，看到您对我们产品感兴趣真的太开心了 💕 请问有什么我可以帮到您的吗？",
        "tags": ["开场", "热情", "破冰"],
    },
    {
        "category": "开场话术",
        "title": "识别需求型开场",
        "content": "您好！看您浏览我们的页面有一会儿了，是不是对某个产品特别感兴趣呀？直接告诉我您的需求，我来帮您筛选最合适的方案 ✨",
        "tags": ["开场", "需求识别"],
    },
    {
        "category": "开场话术",
        "title": "老客户回访",
        "content": "张姐您好呀！我是上次跟您联系的小李~ 自从您上次了解之后，我们刚好有几个新品到货，优惠力度特别大，第一时间就想到了您！",
        "tags": ["开场", "回访", "老客户"],
    },
    # 产品介绍
    {
        "category": "产品介绍",
        "title": "核心卖点提炼",
        "content": "这款产品最大的亮点就是「三免一保」：免安装费、免运费、免利息，保证正品授权。买得放心，用得省心！同价位找不到第二家了 👍",
        "tags": ["产品介绍", "卖点", "促成"],
    },
    {
        "category": "产品介绍",
        "title": "对比优势说明",
        "content": "跟市场上其他家比，我们的核心差异是：① 自有工厂，品质更稳定；② 售后响应 2 小时到位；③ 同样配置我们便宜 15%。贵有贵的原因，但性价比真的没话说！",
        "tags": ["产品介绍", "对比", "差异化"],
    },
    {
        "category": "产品介绍",
        "title": "场景化推荐",
        "content": "听您说平时工作比较忙，我建议您选择这套方案——操作简单，上手快，售后还有 7×24 小时在线，有问题随时有人管，完全不耽误您的时间！",
        "tags": ["产品介绍", "场景化", "针对性"],
    },
    # 价格谈判 / 异议处理
    {
        "category": "价格谈判",
        "title": "价值转移法",
        "content": "价格确实不便宜，但您算一笔账：用 3 年每天才多花 1 块钱，但品质和服务完全不是一个档次。与其买便宜的用两年就坏，不如一次到位更划算！",
        "tags": ["价格", "价值", "投资回报"],
    },
    {
        "category": "价格谈判",
        "title": "限时优惠法",
        "content": "这样吧，我跟店长申请了一个限时福利：今天下单可以额外赠送价值 299 元的配件礼包，这个礼包下周就恢复原价了，等于帮您省了 299！您看现在下单吗？",
        "tags": ["价格", "限时", "紧迫感"],
    },
    {
        "category": "价格谈判",
        "title": "分层报价法",
        "content": "我们有三种套餐：基础版 / 升级版 / 旗舰版。我建议您直接考虑升级版，功能比基础版多 40%，价格只贵 15%，是最多客户选择的口碑款！",
        "tags": ["价格", "套餐", "锚定"],
    },
    # 异议处理
    {
        "category": "异议处理",
        "title": "处理太贵了",
        "content": "我理解价格是重要考量。不过您说的贵，是基于跟哪款对比呢？如果跟劣质品比，我们确实贵；但跟同级别正品比，我们的性价比是最高的。您方便说说比较对象吗？",
        "tags": ["异议处理", "价格", "反问"],
    },
    {
        "category": "异议处理",
        "title": "处理我再看看",
        "content": "完全理解，买东西确实要多看看。我这边可以帮您把今天的优惠价保留 3 天，您先去比较没问题。比较完之后，如果发现我们性价比更好，随时回来找我，这个优惠还给您留着 😊",
        "tags": ["异议处理", "考虑", "留钩子"],
    },
    {
        "category": "异议处理",
        "title": "处理不信任",
        "content": "您的谨慎非常合理！我给您发一下我们的营业执照、产品检测报告、客户评价截图，三证齐全，假一赔四。您也可以在某宝某东搜我们的品牌，评分 4.9，口碑在这里摆着呢~",
        "tags": ["异议处理", "信任", "证据"],
    },
    # 催单跟进
    {
        "category": "催单跟进",
        "title": "优惠倒计时",
        "content": "李总，跟您说一下，这批货库存只剩最后 3 台了，而且活动价明天就结束。我已经帮您预留了一个名额，您看是今天转过来还是？不然明天恢复原价就亏了 😅",
        "tags": ["催单", "紧迫感", "库存"],
    },
    {
        "category": "催单跟进",
        "title": "关怀式跟进",
        "content": "王姐，上次聊完您考虑得怎么样了呀？有什么疑虑可以直接跟我说~ 无论买不买都没关系，能帮到您我就很开心啦！顺便说一下，这款下周要涨价了，早买早享受嘛 😊",
        "tags": ["催单", "关怀", "涨价预警"],
    },
    # 售后维护
    {
        "category": "售后维护",
        "title": "发货提醒",
        "content": "亲，您的订单已发货啦！快递单号：XXX，等待期间有任何问题随时找我。收到货后如果满意，记得给个 5 星好评哦，您的认可是我最大的动力 🌟",
        "tags": ["售后", "发货", "好评"],
    },
]


def seed():
    init_db()
    db = SessionLocal()
    try:
        count = 0
        for data in SEED_PHRASES:
            cat_name = data["category"]
            category = CATEGORY_MAP.get(cat_name, ScriptCategory.OTHER)
            tags_str = ",".join(data["tags"])

            # 检查是否已存在
            existing = db.query(Script).filter_by(title=data["title"]).first()
            if existing:
                print(f"  ⏭️ 跳过（已存在）：{data['title']}")
                continue

            script = Script(
                title=data["title"],
                content=data["content"],
                category=category,
                tags=tags_str,
            )
            db.add(script)
            count += 1
            print(f"  ✅ 添加：{data['title']}")

        db.commit()
        print(f"\n✅ 种子数据导入完成：{count} 条话术")
        print(f"📊 当前总数：{db.query(Script).count()} 条话术")
    finally:
        db.close()


if __name__ == "__main__":
    seed()
