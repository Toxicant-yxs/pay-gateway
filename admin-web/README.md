# PayGateway 企业支付网关运营管理平台

企业级支付网关运营管理后台，基于 Vue 3 + TypeScript + Vite + Element Plus 构建。

## 技术栈

- **框架**：Vue 3.5 + TypeScript 6.0
- **构建工具**：Vite 8.1
- **UI组件库**：Element Plus 2.14
- **状态管理**：Pinia 3.0
- **路由**：Vue Router 4.6
- **HTTP客户端**：Axios 1.18
- **图表**：ECharts 6.1
- **CSS预处理**：Sass 1.101
- **日期处理**：Day.js 1.11

## 功能模块

| 模块 | 路由 | 说明 | 状态 |
|------|------|------|------|
| 登录 | `/login` | JWT认证登录 | ✅ |
| 数据概览 | `/dashboard` | 核心指标、交易趋势、通道状态、实时告警 | ✅ |
| 商户列表 | `/merchant/list` | 商户信息管理、状态控制 | ✅ |
| 进件审核 | `/merchant/audit` | 商户入驻审核 | ✅ |
| 交易订单 | `/transaction/list` | 交易查询与详情 | ✅ |
| 退款订单 | `/transaction/refund` | 退款管理 | ✅ |
| 通道配置 | `/channel/list` | 支付通道接入配置 | ✅ |
| 路由规则 | `/channel/route` | 智能路由规则配置 | ✅ |
| 风控规则 | `/risk/rules` | 风控拦截规则管理 | ✅ |
| 风险事件 | `/risk/events` | 风险事件查看与处理 | ✅ |
| 对账管理 | `/reconciliation/center` | 对账任务管理 | ✅ |
| 对账报表 | `/reconciliation/report` | 对账报表与导出 | ✅ |
| 系统设置 | `/system/settings` | 系统参数配置 | ✅ |
| 个人中心 | `/profile` | 个人信息、密码修改、登录记录 | ✅ |
| 404页面 | `/404` | 页面未找到提示 | ✅ |

## 快速开始

### 环境要求

- Node.js >= 18.x
- npm >= 9.x

### 安装依赖

```bash
npm install
```

### 开发模式

```bash
npm run dev
```

启动后访问 http://localhost:5173/

### 生产构建

```bash
npm run build
```

构建产物输出到 `dist/` 目录。

### 预览构建产物

```bash
npm run preview
```

## 项目结构

```
admin-web/
├── docs/                      # 项目文档
│   ├── README.md              # 文档索引
│   ├── BRD.md                 # 业务需求文档
│   ├── PRD.md                 # 产品需求文档
│   ├── ROADMAP.md             # 项目路线图
│   ├── CHECKLIST.md           # 检查点清单
│   ├── API.md                 # API接口文档
│   └── design/
│       └── technical-design.md # 技术设计文档
├── public/                    # 静态资源
├── src/
│   ├── components/            # 公共组件
│   │   ├── NotificationPanel/ # 消息通知面板
│   │   ├── SearchDialog/      # 全局搜索弹窗(Ctrl+K)
│   │   └── TagsView/          # 多标签导航
│   ├── layout/                # 主布局框架
│   ├── router/                # 路由配置与守卫
│   ├── stores/                # Pinia状态管理
│   ├── styles/                # 全局样式
│   ├── utils/                 # 工具函数(auth/request)
│   ├── views/                 # 页面视图
│   ├── App.vue
│   └── main.ts
├── index.html
├── vite.config.ts
├── tsconfig.json
└── package.json
```

## 全局交互

- **多标签页导航**：打开的页面以标签形式展示，支持关闭、右键菜单
- **全局搜索**：`Ctrl+K`（Mac: `Cmd+K`）打开搜索弹窗，快速导航页面
- **消息通知**：铃铛图标查看实时告警和系统通知
- **面包屑导航**：自动根据路由生成，支持点击跳转
- **全屏模式**：点击全屏图标切换浏览器全屏

## 文档

完整项目文档见 [docs/](./docs/) 目录：

- [业务需求文档(BRD)](./docs/BRD.md)
- [产品需求文档(PRD)](./docs/PRD.md)
- [技术设计文档](./docs/design/technical-design.md)
- [项目路线图](./docs/ROADMAP.md)
- [检查点清单](./docs/CHECKLIST.md)
- [API接口文档](./docs/API.md)

## MVP登录说明

当前为MVP原型阶段，使用前端Mock认证：
- 访问任意页面会跳转登录页
- 输入任意用户名和密码即可登录
- Token有效期2小时，过期后自动跳转登录页

## License

Private
