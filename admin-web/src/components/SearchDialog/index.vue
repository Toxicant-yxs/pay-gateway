<template>
  <teleport to="body">
    <transition name="search-fade">
      <div v-if="visible" class="search-mask" @click.self="close">
        <div class="search-dialog">
          <div class="search-input-wrapper">
            <el-icon :size="20" class="search-icon"><Search /></el-icon>
            <input
              ref="inputRef"
              v-model="keyword"
              class="search-input"
              placeholder="搜索页面、商户、订单..."
              @keydown.enter="handleEnter"
              @keydown.down.prevent="moveDown"
              @keydown.up.prevent="moveUp"
              @keydown.esc="close"
            />
            <span class="search-hint">按 ESC 关闭</span>
          </div>
          <div v-if="keyword" class="search-results">
            <div class="result-group" v-if="pageResults.length > 0">
              <div class="group-title">页面导航</div>
              <div
                v-for="(item, idx) in pageResults"
                :key="'page-' + item.path"
                class="result-item"
                :class="{ active: activeIndex === idx }"
                @click="goTo(item.path)"
                @mouseenter="activeIndex = idx"
              >
                <el-icon :size="16"><component :is="item.icon" /></el-icon>
                <span class="result-text">{{ item.title }}</span>
                <span class="result-path">{{ item.breadcrumb }}</span>
              </div>
            </div>
            <div class="result-group" v-if="merchantResults.length > 0">
              <div class="group-title">商户</div>
              <div
                v-for="(item, idx) in merchantResults"
                :key="'mch-' + item.id"
                class="result-item"
                :class="{ active: activeIndex === pageResults.length + idx }"
                @click="goTo('/merchant/list')"
                @mouseenter="activeIndex = pageResults.length + idx"
              >
                <el-icon :size="16"><OfficeBuilding /></el-icon>
                <span class="result-text">{{ item.name }}</span>
                <el-tag size="small" :type="item.status === '正常' ? 'success' : 'warning'">{{ item.status }}</el-tag>
              </div>
            </div>
            <div class="result-group" v-if="orderResults.length > 0">
              <div class="group-title">交易订单</div>
              <div
                v-for="(item, idx) in orderResults"
                :key="'ord-' + item.id"
                class="result-item"
                :class="{ active: activeIndex === pageResults.length + merchantResults.length + idx }"
                @click="goTo('/transaction/list')"
                @mouseenter="activeIndex = pageResults.length + merchantResults.length + idx"
              >
                <el-icon :size="16"><Tickets /></el-icon>
                <span class="result-text mono">{{ item.orderNo }}</span>
                <span class="result-amount">¥{{ item.amount }}</span>
              </div>
            </div>
            <div v-if="totalResults === 0" class="no-results">
              <el-icon :size="32" color="#c0c4cc"><Search /></el-icon>
              <p>未找到相关结果</p>
            </div>
          </div>
          <div v-else class="search-shortcuts">
            <div class="shortcut-title">快捷入口</div>
            <div class="shortcut-list">
              <div v-for="item in shortcuts" :key="item.path" class="shortcut-item" @click="goTo(item.path)">
                <el-icon :size="16"><component :is="item.icon" /></el-icon>
                <span>{{ item.title }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </transition>
  </teleport>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search, OfficeBuilding, Tickets, DataAnalysis, Warning, Connection, Document, Setting, User } from '@element-plus/icons-vue'

const router = useRouter()
const visible = ref(false)
const keyword = ref('')
const inputRef = ref<HTMLInputElement>()
const activeIndex = ref(0)

const shortcuts = [
  { title: '数据概览', path: '/dashboard', icon: DataAnalysis },
  { title: '商户列表', path: '/merchant/list', icon: OfficeBuilding },
  { title: '交易订单', path: '/transaction/list', icon: Tickets },
  { title: '风控规则', path: '/risk/rules', icon: Warning },
  { title: '通道配置', path: '/channel/list', icon: Connection },
  { title: '对账管理', path: '/reconciliation/center', icon: Document },
  { title: '系统设置', path: '/system/settings', icon: Setting },
  { title: '个人中心', path: '/profile', icon: User }
]

const allPages = [
  { title: '数据概览', path: '/dashboard', icon: DataAnalysis, breadcrumb: '首页' },
  { title: '商户列表', path: '/merchant/list', icon: OfficeBuilding, breadcrumb: '商户管理' },
  { title: '进件审核', path: '/merchant/audit', icon: OfficeBuilding, breadcrumb: '商户管理' },
  { title: '交易订单', path: '/transaction/list', icon: Tickets, breadcrumb: '交易管理' },
  { title: '退款订单', path: '/transaction/refund', icon: Tickets, breadcrumb: '交易管理' },
  { title: '通道配置', path: '/channel/list', icon: Connection, breadcrumb: '支付通道' },
  { title: '路由规则', path: '/channel/route', icon: Connection, breadcrumb: '支付通道' },
  { title: '风控规则', path: '/risk/rules', icon: Warning, breadcrumb: '风控中心' },
  { title: '风险事件', path: '/risk/events', icon: Warning, breadcrumb: '风控中心' },
  { title: '对账管理', path: '/reconciliation/center', icon: Document, breadcrumb: '对账中心' },
  { title: '对账报表', path: '/reconciliation/report', icon: Document, breadcrumb: '对账中心' },
  { title: '系统设置', path: '/system/settings', icon: Setting, breadcrumb: '系统设置' },
  { title: '个人中心', path: '/profile', icon: User, breadcrumb: '个人中心' }
]

const mockMerchants = [
  { id: 1, name: '星辰电商平台', status: '正常' },
  { id: 2, name: '云海餐饮连锁', status: '正常' },
  { id: 3, name: '智学在线教育', status: '待审核' },
  { id: 4, name: '速达出行科技', status: '已冻结' }
]

const mockOrders = [
  { id: 1, orderNo: 'PAY20260628000123456', amount: '29,900' },
  { id: 2, orderNo: 'PAY20260628000123455', amount: '128,000' },
  { id: 3, orderNo: 'PAY20260628000123454', amount: '56,800' }
]

const pageResults = computed(() => {
  if (!keyword.value) return []
  const kw = keyword.value.toLowerCase()
  return allPages.filter(p => p.title.toLowerCase().includes(kw))
})

const merchantResults = computed(() => {
  if (!keyword.value || keyword.value.length < 2) return []
  const kw = keyword.value.toLowerCase()
  return mockMerchants.filter(m => m.name.toLowerCase().includes(kw)).slice(0, 3)
})

const orderResults = computed(() => {
  if (!keyword.value || keyword.value.length < 6) return []
  return mockOrders.filter(o => o.orderNo.includes(keyword.value)).slice(0, 3)
})

const totalResults = computed(() => pageResults.value.length + merchantResults.value.length + orderResults.value.length)

watch(visible, (val) => {
  if (val) {
    keyword.value = ''
    activeIndex.value = 0
    nextTick(() => {
      inputRef.value?.focus()
    })
    document.body.style.overflow = 'hidden'
  } else {
    document.body.style.overflow = ''
  }
})

watch([pageResults, merchantResults, orderResults], () => {
  activeIndex.value = 0
})

const moveDown = () => {
  if (activeIndex.value < totalResults.value - 1) {
    activeIndex.value++
  }
}

const moveUp = () => {
  if (activeIndex.value > 0) {
    activeIndex.value--
  }
}

const handleEnter = () => {
  const allItems = [
    ...pageResults.value.map(p => ({ path: p.path })),
    ...merchantResults.value.map(() => ({ path: '/merchant/list' })),
    ...orderResults.value.map(() => ({ path: '/transaction/list' }))
  ]
  if (allItems[activeIndex.value]) {
    goTo(allItems[activeIndex.value].path)
  }
}

const goTo = (path: string) => {
  visible.value = false
  router.push(path)
}

const open = () => {
  visible.value = true
}

const close = () => {
  visible.value = false
}

const handleKeydown = (e: KeyboardEvent) => {
  if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
    e.preventDefault()
    open()
  }
}

onMounted(() => {
  document.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  document.removeEventListener('keydown', handleKeydown)
})

defineExpose({ open, close })
</script>

<style lang="scss" scoped>
.search-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.45);
  z-index: 2000;
  display: flex;
  justify-content: center;
  align-items: flex-start;
  padding-top: 120px;
}

.search-dialog {
  width: 600px;
  max-width: 90vw;
  background: var(--bg-container);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-lg);
  overflow: hidden;
}

.search-input-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  border-bottom: 1px solid var(--border-light);
}

.search-icon {
  color: var(--text-placeholder);
  flex-shrink: 0;
}

.search-input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 15px;
  color: var(--text-primary);
  background: transparent;

  &::placeholder {
    color: var(--text-placeholder);
  }
}

.search-hint {
  font-size: 12px;
  color: var(--text-placeholder);
  flex-shrink: 0;
  padding: 2px 6px;
  background: var(--bg-page);
  border-radius: var(--radius-sm);
}

.search-results {
  max-height: 400px;
  overflow-y: auto;
  padding: 8px 0;
}

.result-group {
  margin-bottom: 4px;
}

.group-title {
  font-size: 11px;
  color: var(--text-placeholder);
  padding: 8px 20px 4px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.result-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 20px;
  cursor: pointer;
  transition: background var(--transition-fast);
  color: var(--text-regular);

  &:hover, &.active {
    background: var(--primary-bg);
    color: var(--primary-color);
  }
}

.result-text {
  flex: 1;
  font-size: 14px;

  &.mono {
    font-family: 'SF Mono', Monaco, Consolas, monospace;
    font-size: 13px;
  }
}

.result-path {
  font-size: 12px;
  color: var(--text-placeholder);
}

.result-amount {
  font-size: 13px;
  font-weight: 600;
  color: var(--success-color);
  font-family: 'SF Mono', Monaco, Consolas, monospace;
}

.no-results {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 40px 0;
  color: var(--text-placeholder);

  p {
    margin: 0;
    font-size: 13px;
  }
}

.search-shortcuts {
  padding: 16px;
}

.shortcut-title {
  font-size: 12px;
  color: var(--text-secondary);
  margin-bottom: 12px;
  padding: 0 4px;
}

.shortcut-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 6px;
}

.shortcut-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border-radius: var(--radius-sm);
  cursor: pointer;
  font-size: 13px;
  color: var(--text-regular);
  transition: all var(--transition-fast);

  &:hover {
    background: var(--primary-bg);
    color: var(--primary-color);
  }
}
</style>

<style lang="scss">
.search-fade-enter-active,
.search-fade-leave-active {
  transition: opacity 0.2s ease;
}

.search-fade-enter-from,
.search-fade-leave-to {
  opacity: 0;

  .search-dialog {
    transform: translateY(-20px);
  }
}

.search-fade-enter-active .search-dialog {
  transition: transform 0.25s ease;
}
</style>
