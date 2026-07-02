# AI 营销输入法 · 后端

话术库管理 + DeepSeek AI 润色，SSE 流式输出。

## 快速启动

### Windows
双击 `启动后端.bat`，按提示操作。

### Mac / Linux / Git Bash
```bash
cd backend
bash 启动后端.sh
```

### 手动启动
```bash
# 设置 API Key（必须）
export DEEPSEEK_API_KEY=sk-your-key-here

# 初始化数据
python -X utf8 seed_data.py

# 启动服务
python -m uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
```

## 访问

| 地址 | 说明 |
|------|------|
| http://localhost:8000/admin | 话术管理后台（浏览器打开） |
| http://localhost:8000/docs | FastAPI 接口文档 |
| http://localhost:8000/health | 健康检查 |

## 依赖

- Python 3.10+
- DeepSeek API Key（申请：https://platform.deepseek.com/）

## API 概览

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/scripts` | GET | 话术列表（支持分类/关键词/分页） |
| `/api/scripts` | POST | 创建话术 |
| `/api/scripts/{id}` | GET | 获取话术（自动+1使用次数） |
| `/api/scripts/{id}` | PUT | 更新话术 |
| `/api/scripts/{id}/favorite` | PATCH | 切换收藏 |
| `/api/scripts/{id}` | DELETE | 删除话术 |
| `/api/scripts/categories/list` | GET | 分类列表 |
| `/api/ai/stream` | POST | AI 流式处理（SSE） |
| `/api/ai/process` | POST | AI 非流式处理 |
| `/api/ai/modes` | GET | AI 模式列表 |

## 手机端使用

1. 在电脑上安装 [Tailscale](https://tailscale.com/download)
2. 手机也安装 Tailscale，登录同一账号
3. 在输入法 App 设置里填入：`http://<你的Tailscale IP>:8000`

⚠️ 电脑关机时后端不可用。
