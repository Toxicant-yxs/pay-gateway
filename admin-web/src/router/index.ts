import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { getToken, isTokenValid, getUserInfo, logout } from '../utils/auth'

const whiteList = ['/login']

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/login/index.vue'),
    meta: { title: '登录', hidden: true, requiresAuth: false }
  },
  {
    path: '/',
    redirect: '/dashboard'
  },
  {
    path: '/dashboard',
    component: () => import('../layout/index.vue'),
    meta: { title: '数据概览', icon: 'DataAnalysis', requiresAuth: true },
    children: [
      {
        path: '',
        name: 'Dashboard',
        component: () => import('../views/dashboard/index.vue'),
        meta: { title: '数据概览', requiresAuth: true }
      }
    ]
  },
  {
    path: '/merchant',
    component: () => import('../layout/index.vue'),
    redirect: '/merchant/list',
    meta: { title: '商户管理', icon: 'OfficeBuilding', requiresAuth: true },
    children: [
      {
        path: 'list',
        name: 'MerchantList',
        component: () => import('../views/merchant/list.vue'),
        meta: { title: '商户列表', requiresAuth: true }
      },
      {
        path: 'audit',
        name: 'MerchantAudit',
        component: () => import('../views/merchant/audit.vue'),
        meta: { title: '进件审核', requiresAuth: true }
      }
    ]
  },
  {
    path: '/transaction',
    component: () => import('../layout/index.vue'),
    redirect: '/transaction/list',
    meta: { title: '交易管理', icon: 'Tickets', requiresAuth: true },
    children: [
      {
        path: 'list',
        name: 'TransactionList',
        component: () => import('../views/transaction/list.vue'),
        meta: { title: '交易订单', requiresAuth: true }
      },
      {
        path: 'refund',
        name: 'RefundList',
        component: () => import('../views/transaction/refund.vue'),
        meta: { title: '退款订单', requiresAuth: true }
      }
    ]
  },
  {
    path: '/channel',
    component: () => import('../layout/index.vue'),
    redirect: '/channel/list',
    meta: { title: '支付通道', icon: 'Connection', requiresAuth: true },
    children: [
      {
        path: 'list',
        name: 'ChannelList',
        component: () => import('../views/channel/list.vue'),
        meta: { title: '通道配置', requiresAuth: true }
      },
      {
        path: 'route',
        name: 'ChannelRoute',
        component: () => import('../views/channel/route.vue'),
        meta: { title: '路由规则', requiresAuth: true }
      }
    ]
  },
  {
    path: '/risk',
    component: () => import('../layout/index.vue'),
    redirect: '/risk/rules',
    meta: { title: '风控中心', icon: 'Warning', requiresAuth: true },
    children: [
      {
        path: 'rules',
        name: 'RiskRules',
        component: () => import('../views/risk/rules.vue'),
        meta: { title: '风控规则', requiresAuth: true }
      },
      {
        path: 'events',
        name: 'RiskEvents',
        component: () => import('../views/risk/events.vue'),
        meta: { title: '风险事件', requiresAuth: true }
      }
    ]
  },
  {
    path: '/reconciliation',
    component: () => import('../layout/index.vue'),
    redirect: '/reconciliation/center',
    meta: { title: '对账中心', icon: 'Document', requiresAuth: true },
    children: [
      {
        path: 'center',
        name: 'ReconciliationCenter',
        component: () => import('../views/reconciliation/center.vue'),
        meta: { title: '对账管理', requiresAuth: true }
      },
      {
        path: 'report',
        name: 'ReconciliationReport',
        component: () => import('../views/reconciliation/report.vue'),
        meta: { title: '对账报表', requiresAuth: true }
      }
    ]
  },
  {
    path: '/system',
    component: () => import('../layout/index.vue'),
    redirect: '/system/settings',
    meta: { title: '系统设置', icon: 'Setting', requiresAuth: true },
    children: [
      {
        path: 'settings',
        name: 'SystemSettings',
        component: () => import('../views/system/settings.vue'),
        meta: { title: '系统配置', requiresAuth: true }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/dashboard'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, _from, next) => {
  try {
    document.title = `${(to.meta?.title as string) || ''} - 企业支付网关`

    const token = getToken()
    const requiresAuth = to.matched.some(record => record.meta?.requiresAuth !== false)

    if (whiteList.indexOf(to.path) !== -1) {
      if (token && isTokenValid(token)) {
        next({ path: '/' })
      } else {
        next()
      }
      return
    }

    if (requiresAuth) {
      if (!token || !isTokenValid(token)) {
        logout()
        next({ path: '/login', query: { redirect: to.fullPath } })
        return
      }
      const user = getUserInfo()
      if (!user) {
        logout()
        next({ path: '/login', query: { redirect: to.fullPath } })
        return
      }
    }

    next()
  } catch {
    logout()
    next({ path: '/login' })
  }
})

export default router
