# DevOps 平台

一个功能强大的企业级DevOps管理平台，支持CI/CD流水线、应用部署管理和监控告警。

## 技术栈

### 前端
- Vue 3 + Composition API
- Vite 构建工具
- Element Plus UI组件库
- Vue Router 路由管理
- Axios HTTP客户端
- ECharts 可视化图表

### 后端
- Spring Boot 2.7.x
- Spring Security 安全框架
- MyBatis-Plus ORM
- MySQL 数据库
- Lombok 代码生成

## 功能模块

### 1. CI/CD 流水线
- 流水线创建、编辑、删除
- 流水线执行历史
- 构建日志实时查看
- 代码仓库集成配置

### 2. 应用部署管理
- 应用列表和详情
- 部署环境配置（开发、测试、生产）
- 部署任务管理
- 部署历史记录

### 3. 监控告警
- 系统资源监控（CPU、内存、磁盘）
- 应用健康状态
- 告警规则配置
- 告警通知管理

## 项目结构

```
devops-platform/
├── backend/                 # Spring Boot 后端
│   ├── src/main/java/
│   │   └── com/devops/
│   │       ├── controller/  # 控制器
│   │       ├── service/     # 业务逻辑
│   │       ├── mapper/      # 数据访问
│   │       ├── entity/      # 实体类
│   │       └── config/      # 配置类
│   └── src/main/resources/
│       ├── mapper/          # MyBatis映射文件
│       └── application.yml  # 应用配置
│
└── frontend/                # Vue 3 前端
    ├── src/
    │   ├── api/             # API接口
    │   ├── components/     # 公共组件
    │   ├── views/          # 页面视图
    │   ├── router/         # 路由配置
    │   ├── stores/         # 状态管理
    │   └── utils/          # 工具函数
    └── package.json
```

## 快速开始

### 后端启动

1. 创建MySQL数据库
```sql
CREATE DATABASE devops_platform DEFAULT CHARACTER SET utf8mb4;
```

2. 修改配置文件 `backend/src/main/resources/application.yml`

3. 启动后端
```bash
cd backend
mvn spring-boot:run
```

后端默认端口：8080

### 前端启动

```bash
cd frontend
npm install
npm run dev
```

前端默认端口：5173

## 默认账户

- 用户名：admin
- 密码：admin123

## API文档

启动后访问：http://localhost:8080/swagger-ui.html

## 许可证

MIT License
