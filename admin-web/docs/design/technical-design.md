# 技术设计文档

**文档版本**：v1.0
**最后更新**：2026-06-29
**项目名称**：PayGateway 企业支付网关运营管理平台（Admin Web）
**文档状态**：✅ 已完成

---

## 1. 技术选型

### 1.1 前端技术栈

| 技术 | 版本 | 用途 | 选型理由 |
|------|------|------|----------|
| Vue 3 | 3.5.x | 前端框架 | Composition API、性能优、生态成熟 |
| TypeScript | 6.0.x | 类型系统 | 类型安全、IDE支持好、减少运行时错误 |
| Vite | 8.1.x | 构建工具 | 极速HMR、ESM原生支持、构建快 |
| Element Plus | 2.14.x | UI组件库 | Vue 3生态最成熟的企业级组件库 |
| Pinia | 3.0.x | 状态管理 | Vue 3官方推荐、TS友好、轻量 |
| Vue Router | 4.6.x | 路由管理 | 官方路由库、支持路由守卫、动态导入 |
| Axios | 1.18.x | HTTP客户端 | 拦截器机制、Promise API、社区成熟 |
| ECharts | 6.1.x | 图表可视化 | 功能强大、图表类型丰富、企业级标准 |
| Sass | 1.101.x | CSS预处理 | 变量、嵌套、Mixin，提升样式可维护性 |
| Day.js | 1.11.x | 日期处理 | 轻量（2KB）、API同Moment.js |

### 1.2 开发环境

- **Node.js**：≥ 18.x（推荐 20.x+）
- **包管理器**：npm ≥ 9.x
- **IDE**：VS Code（推荐Volar插件）
- **浏览器**：Chrome 90+（开发调试）

---

## 2. 项目架构

### 2.1 目录结构

```
admin-web/
├── public/                     # 静态资源
│   ├── favicon.svg             # 网站图标
│   └── icons.svg              # 图标雪碧图
├── src/
│   ├── assets/                # 静态资源（图片、字体等）
│   ├── components/            # 公共组件
│   │   ├── NotificationPanel/ # 消息通知面板
│   │   ├── SearchDialog/      # 全局搜索弹窗
│   │   └── TagsView/          # 多标签导航
│   ├── layout/                # 布局组件
│   │   └── index.vue          # 主布局框架
│   ├── router/                # 路由配置
│   │   └── index.ts           # 路由定义与守卫
│   ├── stores/                # Pinia状态管理
│   │   ├── user.ts            # 用户状态
│   │   └── tagsView.ts        # 标签页状态
│   ├── styles/                # 全局样式
│   │   └── index.scss         # 全局样式变量与重置
│   ├── utils/                 # 工具函数
│   │   ├── auth.ts            # Token管理与验证
│   │   └── request.ts         # Axios封装
│   ├── views/                 # 页面视图
│   │   ├── login/             # 登录页
│   │   ├── dashboard/         # 数据概览
│   │   ├── merchant/          # 商户管理（list、audit）
│   │   ├── transaction/       # 交易管理（list、refund）
│   │   ├── channel/           # 支付通道（list、route）
│   │   ├── risk/              # 风控中心（rules、events）
│   │   ├── reconciliation/    # 对账中心（center、report）
│   │   ├── system/            # 系统设置
│   │   ├── profile/           # 个人中心
│   │   └── error/             # 错误页面（404）
│   ├── App.vue                # 根组件
│   └── main.ts                # 应用入口
├── docs/                      # 项目文档
├── index.html                 # HTML入口
├── vite.config.ts             # Vite配置
├── tsconfig.json              # TypeScript配置
└── package.json               # 依赖配置
```

### 2.2 架构分层

```
┌─────────────────────────────────────────────┐
│                   Views                      │  页面视图层
│  (dashboard/merchant/transaction/...)        │
├─────────────────────────────────────────────┤
│                 Components                   │  公共组件层
│  (TagsView/NotificationPanel/SearchDialog)   │
├─────────────────────────────────────────────┤
│                   Layout                     │  布局框架层
│  (Sidebar/Header/Breadcrumb/TagsView/...)    │
├─────────────────────────────────────────────┤
│         Router  │  Stores  │  Utils          │  核心框架层
│   (路由守卫)    │ (Pinia)  │ (auth/request)  │
├─────────────────────────────────────────────┤
│            Element Plus + ECharts            │  第三方库
└─────────────────────────────────────────────┘
```

---

## 3. 核心模块设计

### 3.1 认证授权模块

**文件**：[utils/auth.ts](file:///workspace/admin-web/src/utils/auth.ts)、[stores/user.ts](file:///workspace/admin-web/src/stores/user.ts)、[router/index.ts](file:///workspace/admin-web/src/router/index.ts)

**认证流程**：
1. 用户在登录页提交用户名密码
2. 后端验证通过后返回JWT Token
3. 前端将Token存储在localStorage中
4. 路由守卫（beforeEach）检查：
   - 访问登录页：已登录则跳转Dashboard
   - 访问其他页面：检查Token存在性和有效性
   - Token无效：清除状态，跳转登录页
5. Axios请求拦截器自动在Header中添加Authorization: Bearer {token}
6. Axios响应拦截器处理401错误：自动清除Token，跳转登录页

**Token验证机制**：
```typescript
function isTokenValid(token: string): boolean {
  try {
    const payload = JSON.parse(atob(token.split('.')[1]))
    return payload.exp * 1000 > Date.now()
  } catch {
    return false
  }
}
```

**路由守卫逻辑**：
```
beforeEach(to, from, next):
  if (to.path === '/login'):
    if (isLoggedIn()): next('/dashboard')
    else: next()
  else:
    if (to.meta.requiresAuth !== false && !isTokenValid()):
      next('/login?redirect=' + to.fullPath)
    else: next()
```

### 3.2 状态管理设计

**User Store**（[stores/user.ts](file:///workspace/admin-web/src/stores/user.ts)）：

| 状态 | 类型 | 说明 |
|------|------|------|
| token | string | JWT Token |
| username | string | 用户名 |
| role | string | 用户角色 |
| avatar | string | 头像URL |

| 方法 | 说明 |
|------|------|
| login(credentials) | 登录，存储Token和用户信息 |
| logout() | 登出，清除Token和用户信息 |
| fetchUserInfo() | 获取当前用户信息 |

**TagsView Store**（[stores/tagsView.ts](file:///workspace/admin-web/src/stores/tagsView.ts)）：

| 状态 | 类型 | 说明 |
|------|------|------|
| visitedViews | ViewItem[] | 已访问的标签列表 |
| cachedViews | string[] | 缓存的组件name列表 |

| 方法 | 说明 |
|------|------|
| addView(route) | 添加标签（基于name去重） |
| delView(path) | 关闭标签，返回重定向路径 |
| delOthersViews(path) | 关闭其他标签 |
| delAllViews() | 关闭所有标签 |

### 3.3 HTTP请求封装

**文件**：[utils/request.ts](file:///workspace/admin-web/src/utils/request.ts)

**设计要点**：
- 基于Axios创建实例，配置baseURL和timeout
- 请求拦截器：添加Token到Authorization Header
- 响应拦截器：
  - 统一处理业务错误码
  - 401未授权：自动跳转登录
  - 其他错误：ElMessage提示
- 支持GET/POST/PUT/DELETE方法封装

```typescript
// 拦截器核心逻辑
instance.interceptors.request.use(config => {
  const token = getToken()
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

instance.interceptors.response.use(
  response => response.data,
  error => {
    if (error.response?.status === 401) {
      const userStore = useUserStore()
      userStore.logout()
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)
```

### 3.4 路由设计

**文件**：[router/index.ts](file:///workspace/admin-web/src/router/index.ts)

**路由模式**：Hash模式（简化部署，无需后端配置）

**路由元信息**：
```typescript
interface RouteMeta {
  title: string           // 页面标题（用于面包屑、标签页、document.title）
  icon?: Component        // 菜单图标
  hidden?: boolean        // 是否在菜单中隐藏
  requiresAuth?: boolean  // 是否需要认证（默认true）
  affix?: boolean         // 是否固定标签（不可关闭，如Dashboard）
}
```

**路由表**：

| 路径 | 组件 | 标题 | 图标 | 备注 |
|------|------|------|------|------|
| /login | login/index | 登录 | - | 隐藏、无需认证 |
| / | Layout | - | - | 布局容器，重定向到/dashboard |
| /dashboard | dashboard/index | 数据概览 | DataAnalysis | 固定标签 |
| /merchant/list | merchant/list | 商户列表 | OfficeBuilding | 商户管理子菜单 |
| /merchant/audit | merchant/audit | 进件审核 | OfficeBuilding | 商户管理子菜单 |
| /transaction/list | transaction/list | 交易订单 | Tickets | 交易管理子菜单 |
| /transaction/refund | transaction/refund | 退款订单 | Tickets | 交易管理子菜单 |
| /channel/list | channel/list | 通道配置 | Connection | 支付通道子菜单 |
| /channel/route | channel/route | 路由规则 | Connection | 支付通道子菜单 |
| /risk/rules | risk/rules | 风控规则 | Warning | 风控中心子菜单 |
| /risk/events | risk/events | 风险事件 | Warning | 风控中心子菜单 |
| /reconciliation/center | reconciliation/center | 对账管理 | Document | 对账中心子菜单 |
| /reconciliation/report | reconciliation/report | 对账报表 | Document | 对账中心子菜单 |
| /system/settings | system/settings | 系统设置 | Setting | - |
| /profile | profile/index | 个人中心 | User | 隐藏 |
| /404 | error/404 | 页面未找到 | - | 无需认证、隐藏 |
| /:pathMatch(.*)* | - | - | - | 重定向到/404 |

**路由守卫**：
- beforeEach：认证检查、重定向处理
- afterEach：添加标签页、设置document.title

**懒加载**：所有页面组件使用动态导入（`() => import(...)`），实现代码分割

### 3.5 布局组件设计

**文件**：[layout/index.vue](file:///workspace/admin-web/src/layout/index.vue)

**布局结构**：
```
┌─────────────────────────────────────────────────────┐
│ Sidebar  │              Header                      │
│ (折叠)    │  [面包屑]  [搜索][铃铛][全屏][用户下拉]   │
│          ├───────────────────────────────────────────┤
│          │              TagsView                     │
│  菜单    │  [数据概览×] [商户列表×]                   │
│          ├───────────────────────────────────────────┤
│          │                                          │
│          │            Main Content                   │
│          │          (router-view)                    │
│          │                                          │
└──────────┴──────────────────────────────────────────┘
```

**组件特性**：
- 侧边栏折叠/展开（宽度220px ↔ 64px）
- 面包屑导航自动生成（去重、首页链接、点击跳转）
- 多标签页（TagsView）集成
- 全局搜索入口（Ctrl+K快捷键）
- 消息通知面板
- 全屏切换
- 用户下拉菜单（个人中心、账户设置、退出登录）

### 3.6 多标签页（TagsView）设计

**文件**：[components/TagsView/index.vue](file:///workspace/admin-web/src/components/TagsView/index.vue)

**功能特性**：
- 标签基于路由name去重（避免路径参数导致重复标签）
- Dashboard标签固定（affix: true，无关闭按钮）
- 活动标签高亮显示
- 关闭当前标签后自动跳转相邻标签
- 右键菜单：刷新页面、关闭当前、关闭其他、关闭所有
- 标签横向滚动（标签过多时）

**标签数据结构**：
```typescript
interface ViewItem {
  title: string
  path: string
  name: string
  affix: boolean  // 是否固定
}
```

### 3.7 全局样式设计

**文件**：[styles/index.scss](file:///workspace/admin-web/src/styles/index.scss)

**设计系统变量**：
```scss
// 主色
--primary-color: #165DFF;
--primary-light: #E8F3FF;
--primary-dark: #0E42D2;

// 功能色
--success-color: #00B42A;
--warning-color: #FF7D00;
--danger-color: #F53F3F;

// 文字色
--text-primary: #1D2129;
--text-regular: #4E5969;
--text-secondary: #86909C;
--text-placeholder: #C0C4CC;

// 背景色
--bg-page: #F5F7FA;
--bg-container: #FFFFFF;
--bg-hover: #F2F3F5;

// 边框
--border-base: #E5E6EB;
--border-light: #F2F3F5;

// 圆角
--radius-sm: 4px;
--radius-md: 8px;
--radius-lg: 12px;

// 阴影
--shadow-sm: 0 1px 4px rgba(0,0,0,0.04);
--shadow-md: 0 2px 12px rgba(0,0,0,0.06);
--shadow-lg: 0 8px 24px rgba(0,0,0,0.12);

// 间距
--spacing-xs: 4px;
--spacing-sm: 8px;
--spacing-md: 16px;
--spacing-lg: 24px;
--spacing-xl: 32px;

// 过渡
--transition-fast: 0.2s ease;
```

**公共样式类**：
- `.page-container`：页面容器，统一内边距
- `.card-shadow`：卡片阴影效果
- `.stat-card`：统计卡片
- `.filter-area`：筛选区域
- `.table-area`：表格区域
- `.mono`：等宽字体（订单号、金额等）

---

## 4. 页面组件规范

### 4.1 页面通用结构

所有列表页面遵循统一结构：
```vue
<template>
  <div class="page-container">
    <!-- 页面标题区 -->
    <div class="page-header">
      <h2>页面标题</h2>
      <p>页面描述</p>
    </div>

    <!-- 统计卡片（可选） -->
    <el-row :gutter="16" class="stat-cards">...</el-row>

    <!-- 筛选区 -->
    <el-card class="filter-area card-shadow">
      <el-form :inline="true">
        <el-form-item label="xx">...</el-form-item>
        <el-form-item>
          <el-button type="primary">查询</el-button>
          <el-button>重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格区 -->
    <el-card class="table-area card-shadow">
      <div class="table-header">
        <div class="table-title">...</div>
        <div class="table-actions">
          <el-button>操作按钮</el-button>
        </div>
      </div>
      <el-table :data="tableData" v-loading="loading">...</el-table>
      <el-pagination ... />
    </el-card>

    <!-- 弹窗/抽屉 -->
    <el-drawer /> 或 <el-dialog />
  </div>
</template>
```

### 4.2 组件命名规范

- 页面组件：PascalCase，与路由对应（如`MerchantList`对应`/merchant/list`）
- 公共组件：PascalCase，放在`components/`目录下（如`NotificationPanel`）
- 组件文件名：与组件名一致，使用`index.vue`作为目录入口文件

---

## 5. 数据Mock策略

MVP阶段使用前端Mock数据，通过以下方式：

1. **组件内Mock**：在Vue组件中直接定义模拟数据（ref/reactive）
2. **静态列表**：表格数据在组件setup中定义数组
3. **统计数据**：卡片指标直接写死模拟值
4. **图表数据**：ECharts配置中使用模拟数组

**后续对接后端**：
- 将Mock数据替换为API调用
- API接口封装在`api/`目录下（后续新增）
- 保持组件逻辑不变，仅替换数据源

---

## 6. 构建与部署

### 6.1 构建命令

```bash
# 开发模式（HMR）
npm run dev

# 生产构建（类型检查 + Vite构建）
npm run build

# 预览构建产物
npm run preview
```

### 6.2 构建产物

构建输出目录为`dist/`，包含：
- `index.html`：入口HTML
- `assets/`：静态资源（JS、CSS、图片等）
- 所有JS文件按路由进行代码分割（懒加载）
- ECharts单独打包为一个chunk（约370KB gzipped）
- Element Plus等第三方库打包在vendor chunk中

### 6.3 部署说明

- 构建产物为纯静态文件，可部署到任何静态文件服务器（Nginx、CDN等）
- 使用Hash路由模式，无需后端配置URL重写
- Nginx配置参考：
```nginx
server {
  listen 80;
  server_name admin.paygateway.com;
  root /usr/share/nginx/html;
  index index.html;

  location / {
    try_files $uri $uri/ /index.html;
  }

  location /api/ {
    proxy_pass http://backend-server:8080/;
  }
}
```

---

## 7. 性能优化策略

1. **路由懒加载**：所有页面组件动态导入，按需加载
2. **组件缓存**：使用`<keep-alive>`缓存已访问的页面组件
3. **图表按需引入**：ECharts按需引入折线图、饼图等
4. **图片优化**：使用SVG图标，避免大图片
5. **虚拟滚动**：后续大数据表格使用el-table-v2虚拟滚动
6. **防抖节流**：搜索输入、窗口resize等操作使用防抖
7. **CSS变量**：使用CSS自定义属性实现主题切换能力
8. **Gzip压缩**：生产构建开启Gzip/Brotli压缩

---

## 8. 后续技术规划

1. **API层封装**：统一的`api/`目录，按模块划分接口
2. **权限控制**：基于RBAC的按钮级/菜单级权限控制
3. **主题定制**：支持深色模式、主题色切换
4. **国际化**：vue-i18n多语言支持
5. **单元测试**：Vitest + Vue Test Utils
6. **E2E测试**：Playwright端到端测试
7. **错误监控**：集成Sentry前端错误监控
8. **PWA支持**：Service Worker实现离线缓存
