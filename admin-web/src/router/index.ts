import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import Layout from '../layout/index.vue'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/login/index.vue'),
    meta: { title: '登录', hidden: true }
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/dashboard/index.vue'),
        meta: { title: '数据概览', icon: 'DataAnalysis' }
      }
    ]
  },
  {
    path: '/merchant',
    component: Layout,
    redirect: '/merchant/list',
    meta: { title: '商户管理', icon: 'OfficeBuilding' },
    children: [
      {
        path: 'list',
        name: 'MerchantList',
        component: () => import('../views/merchant/list.vue'),
        meta: { title: '商户列表' }
      },
      {
        path: 'audit',
        name: 'MerchantAudit',
        component: () => import('../views/merchant/audit.vue'),
        meta: { title: '进件审核' }
      }
    ]
  },
  {
    path: '/transaction',
    component: Layout,
    redirect: '/transaction/list',
    meta: { title: '交易管理', icon: 'Tickets' },
    children: [
      {
        path: 'list',
        name: 'TransactionList',
        component: () => import('../views/transaction/list.vue'),
        meta: { title: '交易订单' }
      },
      {
        path: 'refund',
        name: 'RefundList',
        component: () => import('../views/transaction/refund.vue'),
        meta: { title: '退款订单' }
      }
    ]
  },
  {
    path: '/channel',
    component: Layout,
    redirect: '/channel/list',
    meta: { title: '支付通道', icon: 'Connection' },
    children: [
      {
        path: 'list',
        name: 'ChannelList',
        component: () => import('../views/channel/list.vue'),
        meta: { title: '通道配置' }
      },
      {
        path: 'route',
        name: 'ChannelRoute',
        component: () => import('../views/channel/route.vue'),
        meta: { title: '路由规则' }
      }
    ]
  },
  {
    path: '/risk',
    component: Layout,
    redirect: '/risk/rules',
    meta: { title: '风控中心', icon: 'Warning' },
    children: [
      {
        path: 'rules',
        name: 'RiskRules',
        component: () => import('../views/risk/rules.vue'),
        meta: { title: '风控规则' }
      },
      {
        path: 'events',
        name: 'RiskEvents',
        component: () => import('../views/risk/events.vue'),
        meta: { title: '风险事件' }
      }
    ]
  },
  {
    path: '/reconciliation',
    component: Layout,
    redirect: '/reconciliation/center',
    meta: { title: '对账中心', icon: 'Document' },
    children: [
      {
        path: 'center',
        name: 'ReconciliationCenter',
        component: () => import('../views/reconciliation/center.vue'),
        meta: { title: '对账管理' }
      },
      {
        path: 'report',
        name: 'ReconciliationReport',
        component: () => import('../views/reconciliation/report.vue'),
        meta: { title: '对账报表' }
      }
    ]
  },
  {
    path: '/system',
    component: Layout,
    redirect: '/system/settings',
    meta: { title: '系统设置', icon: 'Setting' },
    children: [
      {
        path: 'settings',
        name: 'SystemSettings',
        component: () => import('../views/system/settings.vue'),
        meta: { title: '系统配置' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, _from, next) => {
  document.title = `${to.meta.title || ''} - 企业支付网关`
  next()
})

export default router
