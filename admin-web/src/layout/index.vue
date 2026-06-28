<template>
  <div class="layout-container">
    <aside class="sidebar" :class="{ collapsed: isCollapsed }">
      <div class="logo-area">
        <div class="logo-icon">
          <el-icon :size="28" color="#fff"><CreditCard /></el-icon>
        </div>
        <span v-show="!isCollapsed" class="logo-text">PayGateway</span>
      </div>
      <el-scrollbar class="menu-scroll">
        <el-menu
          :default-active="activeMenu"
          :collapse="isCollapsed"
          :unique-opened="true"
          router
          background-color="#001529"
          text-color="rgba(255,255,255,0.65)"
          active-text-color="#fff"
        >
          <template v-for="route in menuRoutes" :key="route.path">
            <el-sub-menu v-if="route.children && route.children.filter(c => c.meta?.title).length > 1" :index="route.path">
              <template #title>
                <el-icon><component :is="route.meta.icon" /></el-icon>
                <span>{{ route.meta.title }}</span>
              </template>
              <el-menu-item
                v-for="child in route.children.filter(c => c.meta?.title)"
                :key="child.path"
                :index="resolvePath(route.path, child.path)"
              >
                {{ child.meta.title }}
              </el-menu-item>
            </el-sub-menu>
            <el-menu-item v-else :index="getMenuIndex(route)">
              <el-icon><component :is="route.meta.icon" /></el-icon>
              <template #title>{{ route.meta.title }}</template>
            </el-menu-item>
          </template>
        </el-menu>
      </el-scrollbar>
    </aside>
    <div class="main-area">
      <header class="header">
        <div class="header-left">
          <el-icon class="collapse-btn" :size="20" @click="toggleCollapse">
            <Fold v-if="!isCollapsed" />
            <Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <template v-for="(item, idx) in breadcrumbs" :key="item.path">
              <el-breadcrumb-item v-if="item.isLink && idx < breadcrumbs.length - 1">
                <router-link :to="item.path" class="breadcrumb-link">{{ item.title }}</router-link>
              </el-breadcrumb-item>
              <el-breadcrumb-item v-else>{{ item.title }}</el-breadcrumb-item>
            </template>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-tooltip content="搜索" placement="bottom">
            <el-icon class="header-icon" :size="18"><Search /></el-icon>
          </el-tooltip>
          <el-tooltip content="消息" placement="bottom">
            <el-badge :value="5" :max="9" class="header-icon-badge">
              <el-icon class="header-icon" :size="18"><Bell /></el-icon>
            </el-badge>
          </el-tooltip>
          <el-tooltip content="全屏" placement="bottom">
            <el-icon class="header-icon" :size="18" @click="toggleFullscreen"><FullScreen /></el-icon>
          </el-tooltip>
          <el-divider direction="vertical" />
          <el-dropdown trigger="click" @command="handleCommand">
            <div class="user-info">
              <el-avatar :size="32" class="user-avatar">
                <img :src="userAvatar" alt="avatar" />
              </el-avatar>
              <span class="username">{{ displayName }}</span>
              <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile"><el-icon><User /></el-icon>个人中心</el-dropdown-item>
                <el-dropdown-item command="settings"><el-icon><Setting /></el-icon>账户设置</el-dropdown-item>
                <el-dropdown-item divided command="logout"><el-icon><SwitchButton /></el-icon>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>
      <TagsView />
      <main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="fade">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  CreditCard, Fold, Expand, Search, Bell, FullScreen, User, Setting,
  SwitchButton, ArrowDown, DataAnalysis, OfficeBuilding, Tickets,
  Connection, Warning, Document
} from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'
import { useTagsViewStore } from '../stores/tagsView'
import TagsView from '../components/TagsView/index.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const tagsViewStore = useTagsViewStore()
const isCollapsed = ref(false)

const toggleCollapse = () => {
  isCollapsed.value = !isCollapsed.value
}

const toggleFullscreen = () => {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen()
  } else {
    document.exitFullscreen()
  }
}

const menuRoutes = computed(() => {
  return router.options.routes.filter(r => 
    !r.meta?.hidden && 
    r.component &&
    r.meta?.title
  )
})

const activeMenu = computed(() => route.path)

const breadcrumbs = computed(() => {
  const matched = route.matched.filter(r => r.meta && r.meta.title)
  const result: { path: string; title: string; isLink: boolean }[] = []
  const titles = new Set<string>()
  for (const r of matched) {
    const title = r.meta.title as string
    if (titles.has(title)) continue
    titles.add(title)
    const hasChildren = r.children && r.children.some((c: any) => c.meta?.title)
    const isRedirect = !!r.redirect
    const isLink = isRedirect || (!hasChildren)
    let path = r.path
    if (isRedirect && typeof r.redirect === 'string') {
      path = r.redirect
    } else if (hasChildren && !isRedirect) {
      const firstChild = r.children?.find((c: any) => c.meta?.title)
      if (firstChild) {
        path = resolvePath(r.path, firstChild.path)
      }
    }
    result.push({ path, title, isLink })
  }
  if (result.length > 0 && result[0].title !== '数据概览') {
    result.unshift({ path: '/dashboard', title: '首页', isLink: true })
  }
  return result
})

const displayName = computed(() => userStore.username || '超级管理员')
const userAvatar = computed(() => userStore.avatar || 'https://api.dicebear.com/7.x/avataaars/svg?seed=admin')

const resolvePath = (parent: string, child: string) => {
  if (!child) return parent
  if (child.startsWith('/')) return child
  return `${parent}/${child}`.replace(/\/+/g, '/')
}

const getMenuIndex = (route: any) => {
  if (route.redirect) return route.redirect as string
  const firstChild = route.children?.find((c: any) => c.meta?.title)
  if (firstChild) {
    return resolvePath(route.path, firstChild.path)
  }
  return route.path
}

const handleCommand = async (command: string) => {
  if (command === 'logout') {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    userStore.logout()
    tagsViewStore.delAllViews()
    ElMessage.success('已退出登录')
    router.push('/login')
  } else if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'settings') {
    router.push('/system/settings')
  }
}
</script>

<style lang="scss" scoped>
.layout-container {
  display: flex;
  width: 100%;
  height: 100%;
}

.sidebar {
  width: var(--sidebar-width);
  height: 100vh;
  background: #001529;
  transition: width var(--transition-normal);
  position: fixed;
  left: 0;
  top: 0;
  z-index: 100;
  overflow: hidden;

  &.collapsed {
    width: var(--sidebar-collapsed-width);
  }
}

.logo-area {
  height: var(--header-height);
  display: flex;
  align-items: center;
  padding: 0 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  gap: 12px;
}

.logo-icon {
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, var(--primary-color), var(--cyan-color));
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.logo-text {
  font-size: 18px;
  font-weight: 600;
  color: #fff;
  white-space: nowrap;
  letter-spacing: 0.5px;
}

.menu-scroll {
  height: calc(100vh - var(--header-height));
}

:deep(.el-menu) {
  border-right: none;
}

:deep(.el-menu-item),
:deep(.el-sub-menu__title) {
  height: 48px;
  line-height: 48px;
  margin: 4px 8px;
  border-radius: var(--radius-md);

  &:hover {
    background: rgba(255, 255, 255, 0.08) !important;
    color: #fff !important;
  }
}

:deep(.el-menu-item.is-active) {
  background: linear-gradient(135deg, var(--primary-color), var(--primary-light)) !important;
  color: #fff !important;
}

:deep(.el-sub-menu .el-menu-item) {
  min-width: auto;
  margin: 2px 0;
  padding-left: 52px !important;

  &.is-active {
    background: rgba(22, 93, 255, 0.2) !important;
    color: var(--primary-light) !important;
  }
}

.main-area {
  flex: 1;
  margin-left: var(--sidebar-width);
  transition: margin-left var(--transition-normal);
  display: flex;
  flex-direction: column;
  min-height: 100vh;

  .collapsed + & {
    margin-left: var(--sidebar-collapsed-width);
  }
}

.header {
  height: var(--header-height);
  background: var(--bg-container);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-shadow: var(--shadow-sm);
  position: sticky;
  top: 0;
  z-index: 99;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.collapse-btn {
  cursor: pointer;
  color: var(--text-regular);
  transition: color var(--transition-fast);
  padding: 8px;
  border-radius: var(--radius-sm);

  &:hover {
    color: var(--primary-color);
    background: var(--primary-bg);
  }
}

.breadcrumb-link {
  color: var(--text-regular);
  text-decoration: none;
  transition: color var(--transition-fast);

  &:hover {
    color: var(--primary-color);
  }
}

:deep(.el-breadcrumb__inner) {
  color: var(--text-regular);
  font-weight: 400;

  &.is-link {
    color: var(--text-regular);
  }
}

:deep(.el-breadcrumb__item:last-child .el-breadcrumb__inner) {
  color: var(--text-primary);
  font-weight: 500;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-icon {
  cursor: pointer;
  color: var(--text-regular);
  padding: 8px;
  border-radius: var(--radius-sm);
  transition: all var(--transition-fast);

  &:hover {
    color: var(--primary-color);
    background: var(--primary-bg);
  }
}

.header-icon-badge {
  padding: 8px;
  cursor: pointer;
  color: var(--text-regular);

  &:hover {
    color: var(--primary-color);
  }
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: var(--radius-md);
  transition: background var(--transition-fast);

  &:hover {
    background: var(--bg-hover);
  }
}

.user-avatar {
  background: var(--primary-bg);
}

.username {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}

.main-content {
  flex: 1;
  background: var(--bg-page);
  overflow-y: auto;
}
</style>
