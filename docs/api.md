# AI 小说创作平台 - 接口文档

> Base URL: `http://localhost:8080/api`
>
> 认证方式: Header `Authorization: Bearer {token}`

---

## 统一响应格式

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "timestamp": 1700000000000
}
```

| code | 说明 |
|------|------|
| 200  | 成功 |
| 400  | 参数错误 |
| 401  | 未登录 |
| 403  | 无权限 |
| 429  | 限流 |
| 500  | 服务器错误 |

---

## 1. 认证模块 `/auth`

### 1.1 用户注册

- **POST** `/auth/register`
- 无需认证

**请求体:**

```json
{
  "username": "testuser",
  "password": "123456",
  "nickname": "测试用户",
  "email": "test@example.com"
}
```

**响应 data:**

```json
{
  "token": "eyJhbG...",
  "userId": 1,
  "username": "testuser",
  "nickname": "测试用户",
  "avatar": null
}
```

### 1.2 用户登录

- **POST** `/auth/login`
- 无需认证

**请求体:**

```json
{
  "username": "demo",
  "password": "123456"
}
```

### 1.3 获取用户信息

- **GET** `/auth/info`
- 需要认证

### 1.4 更新用户信息

- **PUT** `/auth/info`
- 需要认证

**请求体:**

```json
{
  "nickname": "新昵称",
  "email": "new@example.com",
  "avatar": "https://..."
}
```

---

## 2. 小说作品模块 `/novel`

### 2.1 创建作品

- **POST** `/novel`

```json
{
  "title": "我的玄幻小说",
  "genre": "玄幻",
  "summary": "一个少年的修仙之路"
}
```

### 2.2 更新作品

- **PUT** `/novel/{id}`

```json
{
  "title": "新标题",
  "content": "小说正文...",
  "genre": "玄幻",
  "summary": "简介",
  "status": 1
}
```

### 2.3 删除作品

- **DELETE** `/novel/{id}`

### 2.4 作品详情

- **GET** `/novel/{id}`

### 2.5 作品列表

- **GET** `/novel/list`

### 2.6 异步生成长篇小说

- **POST** `/novel/{id}/generate-async`

```json
{
  "prompt": "继续创作...",
  "genre": "玄幻",
  "wordCount": 10000
}
```

### 2.7 异步导出

- **POST** `/novel/{id}/export?format=txt`
- format 可选: `txt` | `md`

---

## 3. 大模型模块 `/llm`

### 3.1 创建流式会话

- **POST** `/llm/stream`
- 需要认证

```json
{
  "prompt": "创作一段玄幻小说开头",
  "genre": "玄幻",
  "wordCount": 2000,
  "novelId": 1,
  "systemPrompt": "可选自定义系统提示词"
}
```

**响应 data:** `"clientId字符串"`

### 3.2 SSE 流式生成

- **GET** `/llm/stream/{clientId}?prompt=...&genre=...&wordCount=...&novelId=...&token=...`
- Content-Type: `text/event-stream`
- token 通过 Query 参数传递（EventSource 不支持自定义 Header）

**事件:**

| event   | data 说明 |
|---------|-----------|
| message | 文本片段（逐字推送） |
| done    | `[DONE]` 生成结束 |
| error   | 错误信息 |

**前端示例:**

```javascript
const es = new EventSource(`/api/llm/stream/${clientId}?prompt=...&token=${token}`)
es.onmessage = (e) => { console.log(e.data) }
```

---

## 4. 系统配置模块 `/config`

### 4.1 获取全部配置

- **GET** `/config`

### 4.2 获取单个配置

- **GET** `/config/{key}`

### 4.3 更新配置

- **PUT** `/config/{key}`

```json
{
  "value": "gpt-4o"
}
```

---

## 错误码说明

所有业务异常通过统一 Result 返回，HTTP 状态码通常为 200（SSE 除外），通过 `code` 字段区分。

限流触发时 HTTP 429，响应:

```json
{
  "code": 429,
  "message": "请求过于频繁，请稍后再试"
}
```
