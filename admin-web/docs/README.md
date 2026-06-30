# 企业支付网关运营管理平台 - 文档中心

## 项目概述

**项目名称**：PayGateway 企业支付网关（含运营管理后台 Admin Web + 后端微服务）
**版本**：v0.1.0-MVP
**文档版本**：v1.1
**最后更新**：2026-06-30
**前端技术栈**：Vue 3 + TypeScript + Vite + Element Plus + Pinia + ECharts
**后端技术栈**：Java 21 + Spring Boot 3.x + Spring Cloud Alibaba + MySQL + Redis + RocketMQ

---

## 文档目录

| 文档 | 文件名 | 说明 | 状态 |
|------|--------|------|------|
| 业务需求文档 | [BRD.md](./BRD.md) | 业务背景、市场分析、商业目标、收益预期 | ✅ 已完成 |
| 产品需求文档 | [PRD.md](./PRD.md) | 功能需求、用户角色、页面说明、交互设计 | ✅ 已完成 |
| 前端技术设计 | [technical-design.md](./design/technical-design.md) | 前端架构、技术选型、组件设计、样式规范 | ✅ 已完成 |
| **后端技术设计** | [backend-technical-design.md](./design/backend-technical-design.md) | **后端微服务架构、数据模型、核心流程图、UML图** | ✅ 已完成 |
| 项目路线图 | [ROADMAP.md](./ROADMAP.md) | 版本规划、里程碑、迭代计划 | ✅ 已完成 |
| 检查点清单 | [CHECKLIST.md](./CHECKLIST.md) | 各阶段验收标准、功能检查点 | ✅ 已完成 |
| API接口文档 | [API.md](./API.md) | 前后端接口约定、数据格式、错误码 | ✅ 已完成 |

---

## 版本历史

| 版本 | 日期 | 作者 | 变更说明 |
|------|------|------|----------|
| v1.0 | 2026-06-29 | 开发团队 | 初始文档集创建，覆盖MVP版本前端文档 |
| v1.1 | 2026-06-30 | 开发团队 | 新增后端技术设计文档（微服务架构/数据模型/UML图/关键技术点） |

---

## 文档规范

1. **命名规范**：所有文档使用英文大写缩写命名（BRD/PRD/API等），中文内容
2. **版本标识**：每份文档头部标注文档版本号和最后更新日期
3. **状态标记**：
   - ✅ 已完成：文档已编写并审核通过
   - 🚧 进行中：文档正在编写中
   - 📋 待编写：文档已列入计划但未开始
4. **目录结构**：
   ```
   docs/
   ├── README.md                           # 本文档索引
   ├── BRD.md                              # 业务需求文档
   ├── PRD.md                              # 产品需求文档
   ├── ROADMAP.md                          # 项目路线图
   ├── CHECKLIST.md                        # 检查点清单
   ├── API.md                              # API接口文档
   └── design/
       ├── technical-design.md             # 前端技术设计文档
       └── backend-technical-design.md     # 后端技术设计文档
   ```
