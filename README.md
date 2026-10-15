# AI 智能学习辅助平台

面向在校学生的 AI 学习辅助平台，对接通义千问大模型封装“AI 助教”能力，整合校内课程电子资料库与检索、课程预约等业务。前后端分离。

## 技术栈

- 后端：Spring Boot 2.7 + Spring Data JPA + MySQL 8 + JWT
- 前端：Vue 3 + Vite + Element Plus + Pinia + Vue Router + Axios
- AI：通义千问（DashScope OpenAI 兼容接口，qwen-plus）

## 目录结构

```
ai-study-platform/
├── backend/                 # Spring Boot 后端（端口 8080）
│   ├── pom.xml
│   ├── maven-settings.xml   # 阿里云 Maven 镜像（国内加速）
│   └── src/main/java/com/study/platform/
│       ├── config/          # JWT、过滤器、CORS、异常处理、初始化数据
│       ├── controller/      # 接口层
│       ├── service/         # 业务层（含通义千问调用）
│       ├── repository/      # JPA 数据访问
│       ├── entity/          # 实体
│       └── dto/             # 请求对象
└── frontend/                # Vue 3 前端（端口 5174）
    └── src/views/           # 登录、工作台、课程、资料库、预约、AI 助教
```

## 环境要求

- JDK 11（本机 E:\dev_runApp\jdk-11.0.11）
- Maven 3.6+（可用 IDEA 自带 Maven）
- Node.js 18+
- MySQL 8（本机 phpStudy 的 MySQL 8.0.12，root / 123456）

## 启动步骤

### 1. 数据库

MySQL 需已启动，首次启动后端会自动建表（JPA ddl-auto=update）。也可手动建库：

```
CREATE DATABASE study_platform DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. 后端

```
cd backend
mvn -s maven-settings.xml spring-boot:run
```

或打包后运行：

```
mvn -s maven-settings.xml -DskipTests package
java -jar target/study-platform-backend-1.0.0.jar
```

后端监听 http://localhost:8080 ，首次启动自动创建管理员 admin/admin123 和示例课程。

### 3. 前端

```
cd frontend
npm install
npm run dev
```

前端监听 http://localhost:5174 ，已配置代理把 `/api`、`/uploads` 转发到后端 8080。

## 默认账号

- 管理员：admin / admin123（可维护课程、确认预约、删除资料）
- 学生：在前端注册页自助注册

## 通义千问配置

编辑 `backend/src/main/resources/application.yml`：

```yaml
qwen:
  api-key: "你的 DashScope API Key"
  model: qwen-plus
  base-url: https://dashscope.aliyuncs.com/compatible-mode/v1
```

在阿里云百炼（DashScope）控制台获取 API Key。未配置时，AI 助教进入演示模式，返回提示语，不影响其它功能。

## 功能

- 登录 / 注册（JWT）
- 课程管理（管理员维护课程）
- 课程电子资料库：上传文件 + 关键字/分类/课程检索
- 课程预约：学生预约、管理员确认
- AI 助教：基于通义千问的对话问答
