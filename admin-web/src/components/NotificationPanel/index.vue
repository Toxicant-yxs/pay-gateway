<template>
  <el-popover
    :visible="visible"
    placement="bottom-end"
    :width="380"
    trigger="click"
    popper-class="notification-popper"
    @show="onShow"
    @hide="onHide"
  >
    <template #reference>
      <el-badge :value="unreadCount" :max="99" class="header-icon-badge" @click="toggle">
        <el-icon class="header-icon" :size="18"><Bell /></el-icon>
      </el-badge>
    </template>
    <div class="notification-panel">
      <div class="panel-header">
        <div class="panel-tabs">
          <span
            v-for="tab in tabs"
            :key="tab.key"
            class="tab-item"
            :class="{ active: activeTab === tab.key }"
            @click="activeTab = tab.key"
          >
            {{ tab.label }}
            <el-badge v-if="tab.count > 0" :value="tab.count" :max="99" class="tab-badge" />
          </span>
        </div>
        <div class="panel-actions">
          <el-button text type="primary" size="small" @click="markAllRead">全部已读</el-button>
        </div>
      </div>
      <el-scrollbar class="panel-body" max-height="400px">
        <div v-if="filteredNotifications.length === 0" class="empty-state">
          <el-icon :size="48" color="#c0c4cc"><Bell /></el-icon>
          <p>暂无{{ activeTabLabel }}通知</p>
        </div>
        <div v-else class="notification-list">
          <div
            v-for="item in filteredNotifications"
            :key="item.id"
            class="notification-item"
            :class="{ unread: !item.read }"
            @click="handleItemClick(item)"
          >
            <div class="item-icon" :class="item.type">
              <el-icon :size="16">
                <WarningFilled v-if="item.type === 'risk'" />
                <InfoFilled v-else-if="item.type === 'system'" />
                <CircleCheckFilled v-else-if="item.type === 'success'" />
                <Bell v-else />
              </el-icon>
            </div>
            <div class="item-content">
              <div class="item-title">{{ item.title }}</div>
              <div class="item-desc">{{ item.content }}</div>
              <div class="item-time">{{ item.time }}</div>
            </div>
            <div v-if="!item.read" class="unread-dot"></div>
          </div>
        </div>
      </el-scrollbar>
      <div class="panel-footer">
        <router-link to="/risk/events" class="footer-link" @click="visible = false">
          查看全部通知 <el-icon><ArrowRight /></el-icon>
        </router-link>
      </div>
    </div>
  </el-popover>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Bell, WarningFilled, InfoFilled, CircleCheckFilled, ArrowRight
} from '@element-plus/icons-vue'

const router = useRouter()
const visible = ref(false)

interface NotificationItem {
  id: number
  type: 'risk' | 'system' | 'transaction' | 'success'
  title: string
  content: string
  time: string
  read: boolean
  link?: string
}

const notifications = ref<NotificationItem[]>([
  { id: 1, type: 'risk', title: '银联通道响应超时率超过5%', content: '通道响应时间异常，请及时检查通道状态', time: '2分钟前', read: false },
  { id: 2, type: 'risk', title: '风控拦截规则触发：IP异常高频访问', content: 'IP地址192.168.1.xxx 5分钟内访问超过200次', time: '5分钟前', read: false },
  { id: 3, type: 'transaction', title: '商户M10086日交易量超预警阈值', content: '今日交易额已达¥5,680,000，超过预警阈值', time: '12分钟前', read: false },
  { id: 4, type: 'success', title: '自动对账完成，差异笔数0笔', content: '2026-06-28日对账已完成，无差错', time: '30分钟前', read: true },
  { id: 5, type: 'system', title: '微信支付版本更新通知', content: '微信支付V3接口将于7月15日升级', time: '1小时前', read: true },
  { id: 6, type: 'transaction', title: '大额交易待审核', content: '商户M10001发起单笔¥500,000交易，需人工审核', time: '1小时前', read: false },
  { id: 7, type: 'risk', title: '可疑交易行为预警', content: '商户M10015短时间内多笔退款，疑似异常', time: '2小时前', read: false },
  { id: 8, type: 'system', title: '系统维护通知', content: '系统将于7月1日02:00-04:00进行例行维护', time: '3小时前', read: true }
])

const tabs = computed(() => [
  { key: 'all', label: '全部', count: notifications.value.filter(n => !n.read).length },
  { key: 'risk', label: '风控', count: notifications.value.filter(n => n.type === 'risk' && !n.read).length },
  { key: 'transaction', label: '交易', count: notifications.value.filter(n => n.type === 'transaction' && !n.read).length },
  { key: 'system', label: '系统', count: notifications.value.filter(n => n.type === 'system' && !n.read).length }
])

const activeTab = ref('all')

const unreadCount = computed(() => notifications.value.filter(n => !n.read).length)

const activeTabLabel = computed(() => {
  const tab = tabs.value.find(t => t.key === activeTab.value)
  return tab ? tab.label : ''
})

const filteredNotifications = computed(() => {
  if (activeTab.value === 'all') return notifications.value
  return notifications.value.filter(n => n.type === activeTab.value)
})

const toggle = () => {
  visible.value = !visible.value
}

const onShow = () => {
  visible.value = true
}

const onHide = () => {
  visible.value = false
}

const handleItemClick = (item: NotificationItem) => {
  item.read = true
  if (item.type === 'risk') {
    visible.value = false
    router.push('/risk/events')
  } else if (item.type === 'transaction' || item.type === 'success') {
    visible.value = false
    router.push('/transaction/list')
  }
}

const markAllRead = () => {
  notifications.value.forEach(n => { n.read = true })
  ElMessage.success('已全部标记为已读')
}
</script>

<style lang="scss" scoped>
.notification-panel {
  margin: -12px;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-bottom: 1px solid var(--border-light);
}

.panel-tabs {
  display: flex;
  gap: 16px;
}

.tab-item {
  position: relative;
  font-size: 14px;
  color: var(--text-regular);
  cursor: pointer;
  padding: 4px 0;
  transition: color var(--transition-fast);
  display: flex;
  align-items: center;
  gap: 6px;

  &:hover {
    color: var(--primary-color);
  }

  &.active {
    color: var(--primary-color);
    font-weight: 500;

    &::after {
      content: '';
      position: absolute;
      bottom: -13px;
      left: 0;
      right: 0;
      height: 2px;
      background: var(--primary-color);
      border-radius: 1px;
    }
  }
}

.tab-badge {
  :deep(.el-badge__content) {
    font-size: 10px;
    height: 16px;
    line-height: 16px;
    padding: 0 4px;
  }
}

.panel-body {
  padding: 8px 0;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
  color: var(--text-secondary);
  gap: 12px;

  p {
    margin: 0;
    font-size: 13px;
  }
}

.notification-list {
  padding: 0 4px;
}

.notification-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px 16px;
  cursor: pointer;
  transition: background var(--transition-fast);
  position: relative;

  &:hover {
    background: var(--bg-hover);
  }

  &.unread {
    background: var(--primary-bg);
  }
}

.item-icon {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  margin-top: 2px;

  &.risk {
    background: #fef0f0;
    color: var(--danger-color);
  }

  &.system {
    background: var(--primary-bg);
    color: var(--primary-color);
  }

  &.transaction {
    background: #fdf6ec;
    color: var(--warning-color);
  }

  &.success {
    background: #f0f9eb;
    color: var(--success-color);
  }
}

.item-content {
  flex: 1;
  min-width: 0;
}

.item-title {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-primary);
  line-height: 1.4;
  margin-bottom: 4px;
}

.item-desc {
  font-size: 12px;
  color: var(--text-secondary);
  line-height: 1.4;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-time {
  font-size: 11px;
  color: var(--text-placeholder);
}

.unread-dot {
  width: 8px;
  height: 8px;
  background: var(--danger-color);
  border-radius: 50%;
  flex-shrink: 0;
  margin-top: 6px;
}

.panel-footer {
  padding: 10px 16px;
  border-top: 1px solid var(--border-light);
  text-align: center;
}

.footer-link {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: var(--primary-color);
  text-decoration: none;

  &:hover {
    opacity: 0.8;
  }
}
</style>

<style lang="scss">
.notification-popper {
  padding: 0 !important;
  border-radius: var(--radius-md) !important;
  box-shadow: var(--shadow-lg) !important;
}
</style>
