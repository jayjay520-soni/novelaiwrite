# NOVEL-AI-WRITER

基于 Spring Boot + Vue 3 的前后端分离 AI 小说生成系统。

## 项目结构

```
novel-ai-writer/
├── backend/          # Spring Boot 3.2 后端
├── frontend/         # Vite 5 + Vue 3 前端
└── docs/             # 数据库 SQL、接口文档
    ├── init.sql
    └── api.md
```

## 技术栈

| 模块 | 技术 |
|------|------|
| 后端 | Spring Boot 3.2, MyBatis-Plus 3.5.5, MySQL 8, Redis 7, JWT |
| 前端 | Vite 5, Vue 3, Element Plus, Pinia, Vue Router 4 |

## 快速启动

### 前置条件

- JDK 17+
- Maven 3.8+
- Node.js 18+
- MySQL 8.0+
- Redis 7+

### 1. 初始化数据库

```bash
mysql -u root -p < docs/init.sql
```

### 2. 配置后端

编辑 `backend/src/main/resources/application.yml`，修改数据库和 Redis 连接信息，以及大模型 API 密钥。

### 3. 启动后端

```bash
cd backend
mvn spring-boot:run
```

后端运行在 `http://localhost:8080`

### 4. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端运行在 `http://localhost:5173`

### 测试账号

- 用户名: `demo`
- 密码: `123456`

## 功能概览

### 后端

- 全局 RESTful 统一返回、异常处理、跨域配置
- JWT 登录拦截、用户注册/登录/信息管理
- Redis 接口限流、作品缓存
- SSE 长连接流式输出
- 异步线程池处理长篇小说生成与文件导出
- 大模型 API 转发层（密钥后端隐藏）

### 前端

- Axios 封装，自动携带 JWT，401 自动跳转
- EventSource SSE 流式打字机渲染
- 分段编辑、复制、TXT/Markdown 导出
- PC 优先自适应布局
- Pinia 全局状态管理

## 生产构建

```bash
# 前端
cd frontend && npm run build

# 后端
cd backend && mvn clean package -DskipTests
java -jar target/novel-ai-writer-backend-1.0.0.jar
```
