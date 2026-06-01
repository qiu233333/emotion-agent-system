# 毕设项目会话恢复摘要

更新时间：2026-06-01  
项目根目录：`F:\bishe`

## 一句话目标

本项目是“基于大语言模型的情绪记录与管理智能体系统”，当前采用 Vue 3 前端、Spring Boot 后端、MySQL 数据库，实现用户登录注册、JWT 认证、情绪日记 CRUD、规则 NLP 情绪分析、大语言模型情绪建议和 AI 陪伴对话。

## 关键决策

- 前端目录：`F:\bishe\qianduan\frontend`。
- 后端目录：`F:\bishe\houduan\backend`。
- 前端技术栈：Vue 3、Vue Router、Element Plus、Axios、Vite。
- 后端技术栈：Spring Boot 3、Java 17、MyBatis-Plus、MySQL、BCrypt、JWT。
- 后端服务端口：`8080`。
- 后端统一上下文路径：`/api`，所以接口完整路径形如 `http://127.0.0.1:8080/api/diary/list`。
- 前端开发端口：`5174`，启动命令在 `qianduan/frontend` 下执行 `npm run dev`。
- Vite 代理：前端请求 `/api` 会代理到 `http://127.0.0.1:8080`。
- 数据库名：`bishe_backend`。
- MySQL 开发账号：`root`，密码：`123456`。
- 后端统一返回 `Result` 对象，前端 Axios 响应拦截器统一返回 `response.data`。
- 登录后 token 存在 `localStorage.token`，用户信息存在 `localStorage.userInfo`。
- JWT 中保存 `userId` 和 `username`，后端通过 `AuthContext` 获取当前用户。
- 除 `/api/user/login` 和 `/api/user/register` 外，其余接口需要携带 token。
- 日记数据必须按当前登录用户隔离，不同用户只能看到自己的日记。
- 密码不再明文保存，新注册用户使用 BCrypt；旧明文测试密码登录成功后会自动升级为 BCrypt。
- 大语言模型调用采用 OpenAI SDK 兼容接口风格，实际实现用 Java `HttpClient` 请求 `/chat/completions`。
- 大模型配置从环境变量读取：`LLM_API_KEY`、`LLM_BASE_URL`、`LLM_MODEL`、`LLM_TIMEOUT_SECONDS`。
- `.env.example` 只放示例配置；真实 `.env` 不要提交。
- `AGENTS.md` 要求：每个模块、类、方法都写清注释，后续新增代码也要保持这个习惯。

## 整体架构思路

前端负责页面展示、路由守卫、表单提交、token 保存和接口调用。核心页面包括登录、注册、首页、情绪日记、历史记录、AI 陪伴、情绪统计、后台管理。

后端按 Controller、Service、Mapper、Entity 分层。Controller 负责 HTTP 接口，Service 处理业务逻辑，Mapper 访问数据库，Entity 对应数据库表。

情绪日记主流程：

1. 用户登录后前端保存 token。
2. 前端通过 Axios 自动携带 `Authorization: Bearer token`。
3. 后端 JWT 拦截器解析 token，把当前用户写入 `AuthContext`。
4. 新增日记时，后端根据当前用户 ID 设置 `user_id`。
5. 后端规则 NLP 模块分析日记正文，生成情绪极性、情绪类型、情绪强度、关键词和摘要。
6. 日记和分析结果一起保存到 `emotion_diary` 表。
7. 前端历史记录页查询当前用户日记并展示情绪分析结果。
8. 用户可调用大模型接口生成 `ai_suggestion`，后端保存到数据库。

AI 陪伴主流程：

1. 前端 AI 陪伴页调用 `/api/ai/chat`。
2. 后端根据 JWT 获取当前用户。
3. `DiaryContextService` 查询今日日记完整信息和近期历史日记摘要。
4. `LLMService` 把用户当前消息和日记上下文一起放入 Prompt。
5. 大模型返回温和、简短、可执行的陪伴式回复。

## 已完成部分

- 已完成前后端基础联通，后端 `/hello` 接口可被前端 Axios 请求。
- 已搭建 Vue 3 基础页面路由和布局：
  - `/login`
  - `/register`
  - `/dashboard`
  - `/diary`
  - `/history`
  - `/chat`
  - `/statistics`
  - `/admin`
- 已实现顶部标题、左侧菜单、右侧内容区布局。
- 已封装前端 Axios：`src/utils/request.js`。
- 已封装前端日记 API：`src/api/diary.js`。
- 已封装前端 AI API：`src/api/ai.js`。
- 已实现数据库初始化 SQL：`docs/sql/init.sql`。
- 已设计两张核心表：
  - `user`
  - `emotion_diary`
- 已实现情绪日记后端 CRUD：
  - `POST /api/diary`
  - `GET /api/diary/list`
  - `GET /api/diary/{id}`
  - `PUT /api/diary/{id}`
  - `DELETE /api/diary/{id}`
- 已实现注册登录和 JWT 认证：
  - `POST /api/user/register`
  - `POST /api/user/login`
- 已实现 BCrypt 密码保存和旧明文密码自动升级。
- 已实现规则 NLP 情绪分析模块：
  - 积极词：开心、高兴、顺利、轻松、满意、期待。
  - 消极词：焦虑、难受、压力、烦、崩溃、担心、疲惫、失眠。
  - 输出 `emotionPolarity`、`emotionType`、`emotionScore`、`keywords`、`aiSummary`。
- 已实现大语言模型模块：
  - `POST /api/ai/suggestion`
  - `POST /api/ai/chat`
- 已实现 AI 建议保存到 `emotion_diary.ai_suggestion`。
- 已实现 AI 陪伴结合日记上下文：
  - 今日完整日记信息。
  - 近 7 天、最多 5 篇历史日记摘要。
- 已实现前端情绪日记页保存后展示本次情绪分析。
- 已实现历史记录页展示情绪极性、情绪类型、情绪强度、关键词、摘要和 AI 建议。
- 已实现前端 AI 陪伴页面调用后端大模型对话接口。

## 当前工作区状态

最近提交记录显示：

- `ded80fd ai陪伴功能优化增加上下文信息`
- `cb27e16 完成ai建议生成大模型调用`
- `9f92838 完成基础NLP情绪分析模块`
- `097be05 登录注册+JWT验证`
- `69d2bdb 完成前端日记接口+axios封装`
- `3b47569 完成情绪日记后端CRUD接口`
- `5a6b569 建数据库表`



## 重要文件修改记录

后端核心文件：

- `houduan/backend/src/main/resources/application.yaml`
  - 配置端口、`/api` 上下文路径、MySQL、JWT、LLM 环境变量导入。
- `houduan/backend/pom.xml`
  - 引入 Spring Web、Validation、MyBatis、MyBatis-Plus、MySQL、Lombok、BCrypt 相关依赖。
- `houduan/backend/src/main/java/com/bishe/backend/common/Result.java`
  - 后端统一响应对象。
- `houduan/backend/src/main/java/com/bishe/backend/common/GlobalExceptionHandler.java`
  - 全局异常处理。
- `houduan/backend/src/main/java/com/bishe/backend/entity/User.java`
  - 用户实体，对应 `user` 表。
- `houduan/backend/src/main/java/com/bishe/backend/entity/EmotionDiary.java`
  - 情绪日记实体，对应 `emotion_diary` 表。
- `houduan/backend/src/main/java/com/bishe/backend/controller/UserController.java`
  - 注册登录接口。
- `houduan/backend/src/main/java/com/bishe/backend/controller/EmotionDiaryController.java`
  - 情绪日记 CRUD 接口。
- `houduan/backend/src/main/java/com/bishe/backend/controller/AiController.java`
  - AI 建议和 AI 陪伴接口。
- `houduan/backend/src/main/java/com/bishe/backend/security/JwtUtil.java`
  - JWT 生成和解析。
- `houduan/backend/src/main/java/com/bishe/backend/security/JwtInterceptor.java`
  - 登录拦截器。
- `houduan/backend/src/main/java/com/bishe/backend/security/AuthContext.java`
  - 当前线程登录用户上下文。
- `houduan/backend/src/main/java/com/bishe/backend/service/impl/EmotionAnalyzeServiceImpl.java`
  - 规则 NLP 情绪分析。
- `houduan/backend/src/main/java/com/bishe/backend/service/impl/LLMServiceImpl.java`
  - OpenAI 兼容大模型调用。
- `houduan/backend/src/main/java/com/bishe/backend/service/impl/DiaryContextServiceImpl.java`
  - AI 陪伴对话的今日和近期日记上下文整理。

前端核心文件：

- `qianduan/frontend/vite.config.ts`
  - 配置 `/api` 代理到后端。
- `qianduan/frontend/package.json`
  - 前端依赖和启动脚本。
- `qianduan/frontend/src/router/index.ts`
  - 页面路由和登录守卫。
- `qianduan/frontend/src/layouts/AppLayout.vue`
  - 系统顶部标题、左侧菜单和退出登录。
- `qianduan/frontend/src/utils/request.js`
  - Axios 实例、token 自动携带、401 自动跳转登录页。
- `qianduan/frontend/src/api/user.js`
  - 注册登录接口封装。
- `qianduan/frontend/src/api/diary.js`
  - 情绪日记接口封装。
- `qianduan/frontend/src/api/ai.js`
  - AI 建议和 AI 陪伴接口封装。
- `qianduan/frontend/src/views/LoginView.vue`
  - 登录页面。
- `qianduan/frontend/src/views/RegisterView.vue`
  - 注册页面。
- `qianduan/frontend/src/views/DiaryView.vue`
  - 情绪日记新增、分析展示、AI 建议生成；当前还加入了今日草稿本地保存改动，待验证。
- `qianduan/frontend/src/views/HistoryView.vue`
  - 历史日记列表、分析详情、AI 建议生成。
- `qianduan/frontend/src/views/ChatView.vue`
  - AI 陪伴对话；当前还加入了今日对话本地保存改动，待验证。
- `qianduan/frontend/src/utils/todayStorage.js`
  - 今日草稿和今日 AI 陪伴记录的本地存储工具；当前未提交，待验证编码和构建。

数据库文件：

- `docs/sql/init.sql`
  - 创建数据库 `bishe_backend`。
  - 会先删除 `emotion_diary` 和 `user` 表再重建，执行前要确认是否需要保留已有数据。
  - 插入测试用户 `admin`、`alice`、`bob`。
  - 插入若干测试日记。

环境配置文件：

- `.env.example`
  - 提供 LLM 配置示例。
- `.env`
  - 本地真实配置文件，不要提交，不要在聊天记录中泄露真实 API Key。

## 待办事项



功能待完善：

- AI 陪伴当前会结合用户日记上下文，但不一定把前端本地历史聊天记录一起传给后端；如果需要真正多轮上下文，需要扩展 `/api/ai/chat` 请求体，携带最近若干条对话。
- 情绪统计页面目前还是基础页面，后续需要做图表和统计接口。
- 后台管理页面目前还是基础页面，后续需要明确管理员功能。
- 日记详情、编辑、删除在前端历史记录页还可以继续完善。
- 用户头像、邮箱、角色权限等目前只做了基础字段，管理员权限控制还未细化。
- JWT secret 目前在配置中写了开发默认值，正式环境应改为环境变量。
- 大模型失败时已有友好提示，但可以继续增加更细的日志和错误分类。
- 后端测试目前比较少，后续可以补 Controller、Service、NLP 分析规则和权限隔离测试。
- 前端可以补登录、日记新增、历史记录、AI 对话的基本 E2E 验证。

## 启动和验证方式

后端：

```powershell
cd F:\bishe\houduan\backend
.\mvnw.cmd spring-boot:run
```

前端：

```powershell
cd F:\bishe\qianduan\frontend
npm run dev
```

前端构建检查：

```powershell
cd F:\bishe\qianduan\frontend
npm run build
```

后端测试：

```powershell
cd F:\bishe\houduan\backend
.\mvnw.cmd test
```

## 下次新会话建议先做的事

1. 读取本文件，确认当前项目目标和状态。
2. 执行 `git status --short`，确认未提交文件。
3. 优先检查前端 `DiaryView.vue`、`ChatView.vue`、`todayStorage.js` 是否存在真实乱码或语法错误。
4. 在 `qianduan/frontend` 执行 `npm run build`。
5. 如果构建失败，先修复本地保存相关前端文件。
6. 如果构建成功，再手动验证日记草稿和 AI 陪伴记录的 localStorage 行为。
7. 验证完成后再提交相关改动。

