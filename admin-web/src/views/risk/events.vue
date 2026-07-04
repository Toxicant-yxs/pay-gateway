<template>
  <div class="page-container events-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">风险事件监控</h2>
        <p class="page-desc">实时监控平台风险事件，及时处理可疑交易行为</p>
      </div>
      <div class="header-actions">
        <el-button @click="loadData">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
        <el-badge :value="pendingCount" :hidden="pendingCount === 0" class="badge-btn">
          <el-button type="warning">
            <el-icon><Bell /></el-icon>
            待处理预警
          </el-button>
        </el-badge>
      </div>
    </div>

    <div class="stats-row">
      <div class="stat-card metric-card" v-for="stat in statsCards" :key="stat.key" :class="stat.type">
        <div class="stat-icon" :style="{ background: stat.bgColor, color: stat.iconColor }">
          <el-icon><component :is="stat.icon" /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stat.value }}</div>
          <div class="stat-label">{{ stat.label }}</div>
        </div>
      </div>
    </div>

    <div class="card-shadow filter-card">
      <el-form :model="filterForm" inline class="filter-form">
        <el-form-item label="事件编号">
          <el-input v-model="filterForm.eventNo" placeholder="请输入事件编号" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="风险等级">
          <el-select v-model="filterForm.riskLevel" placeholder="全部等级" clearable style="width: 120px">
            <el-option label="高风险" value="HIGH" />
            <el-option label="中风险" value="MEDIUM" />
            <el-option label="低风险" value="LOW" />
          </el-select>
        </el-form-item>
        <el-form-item label="事件分类">
          <el-select v-model="filterForm.category" placeholder="全部类型" clearable style="width: 160px">
            <el-option label="交易限制" value="TRADE_LIMIT" />
            <el-option label="频次控制" value="FREQUENCY" />
            <el-option label="IP黑名单" value="IP_BLACKLIST" />
            <el-option label="设备风控" value="DEVICE" />
            <el-option label="地理位置" value="GEO" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理状态">
          <el-select v-model="filterForm.status" placeholder="全部状态" clearable style="width: 120px">
            <el-option label="待处理" :value="0" />
            <el-option label="处理中" :value="1" />
            <el-option label="已处理" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="filterForm.dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            style="width: 340px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="card-shadow table-card">
      <div class="table-header">
        <div class="table-info">
          共 <span class="highlight">{{ total }}</span> 条风险事件
        </div>
      </div>
      <el-table
        :data="tableData"
        style="width: 100%"
        stripe
        v-loading="loading"
        row-key="id"
      >
        <el-table-column prop="eventNo" label="事件编号" width="160">
          <template #default="{ row }">
            <span class="mono-text">{{ row.eventNo ?? row.id ?? '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="triggeredAt" label="触发时间" width="160">
          <template #default="{ row }">
            {{ formatDate(row.triggeredAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="riskLevel" label="风险等级" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getRiskTagType(row.riskLevel)" size="small" effect="dark">
              <el-icon style="margin-right: 2px"><Warning /></el-icon>
              {{ getRiskLevelText(row.riskLevel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="category" label="事件分类" width="120">
          <template #default="{ row }">
            {{ getCategoryText(row.category) }}
          </template>
        </el-table-column>
        <el-table-column prop="merchantNo" label="关联商户/订单" min-width="180">
          <template #default="{ row }">
            <div class="related-cell">
              <div v-if="row.merchantNo" class="related-name">商户号：{{ row.merchantNo }}</div>
              <div v-if="row.orderNo" class="related-id mono-text">订单号：{{ row.orderNo }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="triggerRule" label="触发规则" width="180" show-overflow-tooltip />
        <el-table-column prop="eventDetail" label="风险描述" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            {{ typeof row.eventDetail === 'string' ? row.eventDetail : JSON.stringify(row.eventDetail) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="处理状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleProcess(row)" v-if="row.status === 0 || row.status === 'PENDING'">
              处理
            </el-button>
            <el-button type="primary" link size="small" @click="viewDetail(row)">
              <el-icon><View /></el-icon>
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @current-change="loadData"
          @size-change="loadData"
        />
      </div>
    </div>

    <el-drawer
      v-model="drawerVisible"
      :title="drawerTitle"
      direction="rtl"
      size="720px"
      :destroy-on-close="true"
    >
      <div v-if="currentEvent" class="event-detail">
        <div class="detail-header" :class="getRiskLevelClass(currentEvent.riskLevel)">
          <div class="header-left">
            <div class="risk-badge" :class="getRiskLevelClass(currentEvent.riskLevel)">
              <el-icon><Warning /></el-icon>
            </div>
            <div class="header-info">
              <h3>{{ getCategoryText(currentEvent.category) }}</h3>
              <div class="header-meta">
                <span class="mono-text">{{ currentEvent.eventNo ?? currentEvent.id }}</span>
                <el-divider direction="vertical" />
                <span><el-icon><Clock /></el-icon> {{ formatDate(currentEvent.triggeredAt) }}</span>
              </div>
            </div>
          </div>
          <el-tag :type="getRiskTagType(currentEvent.riskLevel)" size="large" effect="dark">
            {{ getRiskLevelText(currentEvent.riskLevel) }}
          </el-tag>
        </div>

        <el-tabs v-model="activeTab" class="detail-tabs">
          <el-tab-pane label="事件信息" name="info">
            <div class="section-title">
              <el-icon><Document /></el-icon>
              基本信息
            </div>
            <el-descriptions :column="2" border class="info-descriptions">
              <el-descriptions-item label="事件编号">
                <span class="mono-text">{{ currentEvent.eventNo ?? currentEvent.id }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="触发时间">{{ formatDate(currentEvent.triggeredAt) }}</el-descriptions-item>
              <el-descriptions-item label="风险等级">
                <el-tag :type="getRiskTagType(currentEvent.riskLevel)" size="small" effect="dark">
                  {{ getRiskLevelText(currentEvent.riskLevel) }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="事件分类">{{ getCategoryText(currentEvent.category) }}</el-descriptions-item>
              <el-descriptions-item label="商户号" v-if="currentEvent.merchantNo">
                <span class="mono-text">{{ currentEvent.merchantNo }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="订单号" v-if="currentEvent.orderNo">
                <span class="mono-text">{{ currentEvent.orderNo }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="触发规则" :span="2">{{ currentEvent.triggerRule ?? '-' }}</el-descriptions-item>
              <el-descriptions-item label="处理状态">
                <el-tag :type="getStatusTagType(currentEvent.status)" size="small">
                  {{ getStatusText(currentEvent.status) }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="处理时间" v-if="currentEvent.handledAt">
                {{ formatDate(currentEvent.handledAt) }}
              </el-descriptions-item>
              <el-descriptions-item label="处理备注" :span="2" v-if="currentEvent.handleNote">
                <div class="risk-desc">{{ currentEvent.handleNote }}</div>
              </el-descriptions-item>
              <el-descriptions-item label="事件详情" :span="2">
                <div class="risk-desc">
                  {{ typeof currentEvent.eventDetail === 'string' ? currentEvent.eventDetail : JSON.stringify(currentEvent.eventDetail, null, 2) }}
                </div>
              </el-descriptions-item>
            </el-descriptions>
          </el-tab-pane>
        </el-tabs>
      </div>

      <div class="drawer-footer" v-if="currentEvent && (currentEvent.status === 0 || currentEvent.status === 'PENDING')">
        <div class="handle-form">
          <el-input
            v-model="handleRemark"
            type="textarea"
            :rows="3"
            placeholder="请输入处理备注..."
            maxlength="200"
            show-word-limit
          />
        </div>
        <div class="handle-actions">
          <el-button type="success" @click="handleConfirmRisk">
            <el-icon><CircleCheck /></el-icon>
            确认处理
          </el-button>
          <el-button @click="handleMarkFalse">
            <el-icon><Warning /></el-icon>
            标记忽略
          </el-button>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Warning, Bell, CircleCheck, Search, Refresh, View, Document, Clock
} from '@element-plus/icons-vue'
import { riskApi } from '@/api/risk'

interface RiskEventItem {
  id?: string | number
  eventId?: string
  eventNo?: string
  category?: string
  riskLevel?: string
  orderNo?: string
  merchantNo?: string
  merchantName?: string
  triggerRule?: string
  eventDetail?: any
  status?: number | string
  handleNote?: string
  triggeredAt?: string
  handledAt?: string
  [key: string]: any
}

const loading = ref(false)
const drawerVisible = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const currentEvent = ref<RiskEventItem | null>(null)
const activeTab = ref('info')
const handleRemark = ref('')
const drawerTitle = computed(() => currentEvent.value ? `事件详情 - ${currentEvent.value.eventNo ?? currentEvent.value.eventId ?? currentEvent.value.id ?? ''}` : '事件详情')

const pendingCount = computed(() => tableData.value.filter(i => {
  const s = String(i.status ?? '').toUpperCase()
  return i.status === 0 || s === 'PENDING'
}).length)

const statsCards = computed(() => [
  { key: 'total', label: '风险事件总数', value: total.value, icon: Warning, type: 'primary', bgColor: 'var(--primary-bg)', iconColor: 'var(--primary-color)' },
  { key: 'high', label: '高风险事件', value: tableData.value.filter(i => String(i.riskLevel ?? '').toUpperCase() === 'HIGH').length, icon: Warning, type: 'danger', bgColor: 'var(--danger-bg)', iconColor: 'var(--danger-color)' },
  { key: 'medium', label: '中风险事件', value: tableData.value.filter(i => String(i.riskLevel ?? '').toUpperCase() === 'MEDIUM').length, icon: Bell, type: 'warning', bgColor: 'var(--warning-bg)', iconColor: 'var(--warning-color)' },
  { key: 'low', label: '低风险事件', value: tableData.value.filter(i => String(i.riskLevel ?? '').toUpperCase() === 'LOW').length, icon: Warning, type: 'success', bgColor: 'var(--success-bg)', iconColor: 'var(--success-color)' },
  { key: 'pending', label: '待处理事件', value: pendingCount.value, icon: CircleCheck, type: 'success', bgColor: 'var(--cyan-bg)', iconColor: 'var(--cyan-color)' }
])

const filterForm = reactive({
  eventNo: '',
  riskLevel: '' as 'HIGH' | 'MEDIUM' | 'LOW' | '',
  category: '' as 'TRADE_LIMIT' | 'FREQUENCY' | 'IP_BLACKLIST' | 'DEVICE' | 'GEO' | '',
  status: '' as number | '',
  dateRange: [] as Date[] | string[]
})

const tableData = ref<RiskEventItem[]>([])

function formatDate(dateStr: string | undefined | null): string {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  if (isNaN(date.getTime())) return dateStr
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

const categoryMap: Record<string, string> = {
  TRADE_LIMIT: '交易限制',
  FREQUENCY: '频次控制',
  IP_BLACKLIST: 'IP黑名单',
  DEVICE: '设备风控',
  GEO: '地理位置',
  abnormal_transaction: '异常交易',
  suspicious_login: '可疑登录',
  large_amount: '大额交易',
  frequency_operation: '频繁操作',
  blacklist_hit: '黑名单命中',
  offsite_transaction: '异地交易',
  merchant_abnormal: '商户异常'
}

function getCategoryText(category: string | undefined): string {
  if (!category) return '-'
  return categoryMap[category] ?? category
}

function getRiskLevelClass(level: string | undefined): string {
  const upper = String(level ?? '').toUpperCase()
  if (upper === 'HIGH') return 'high'
  if (upper === 'MEDIUM') return 'medium'
  return 'low'
}

const getRiskTagType = (level: string | undefined) => {
  const upper = String(level ?? '').toUpperCase()
  if (upper === 'HIGH') return 'danger'
  if (upper === 'MEDIUM') return 'warning'
  if (upper === 'LOW') return 'success'
  return 'info'
}

const getRiskLevelText = (level: string | undefined) => {
  const upper = String(level ?? '').toUpperCase()
  if (upper === 'HIGH') return '高风险'
  if (upper === 'MEDIUM') return '中风险'
  if (upper === 'LOW') return '低风险'
  return '未知'
}

const getStatusTagType = (status: number | string | undefined) => {
  const s = String(status ?? '').toUpperCase()
  if (status === 0 || s === 'PENDING') return 'danger'
  if (status === 1 || s === 'PROCESSING') return 'warning'
  if (status === 2 || s === 'PROCESSED' || s === 'IGNORED') return 'success'
  return 'info'
}

const getStatusText = (status: number | string | undefined) => {
  const s = String(status ?? '').toUpperCase()
  if (status === 0 || s === 'PENDING') return '待处理'
  if (status === 1 || s === 'PROCESSING') return '处理中'
  if (status === 2 || s === 'PROCESSED') return '已处理'
  if (s === 'IGNORED') return '已忽略'
  return '未知'
}

async function loadData() {
  loading.value = true
  try {
    const params: any = {
      page: currentPage.value,
      pageSize: pageSize.value
    }
    if (filterForm.eventNo) params.eventNo = filterForm.eventNo
    if (filterForm.riskLevel) params.riskLevel = filterForm.riskLevel
    if (filterForm.category) params.category = filterForm.category
    if (filterForm.status !== '') params.status = filterForm.status
    if (filterForm.dateRange && filterForm.dateRange.length === 2) {
      params.startTime = filterForm.dateRange[0]
      params.endTime = filterForm.dateRange[1]
    }
    const res = await riskApi.getEvents(params)
    if (res) {
      tableData.value = res.list ?? []
      total.value = res.total ?? 0
    }
  } catch (e) {
    console.error('Failed to load risk events:', e)
    tableData.value = []
    total.value = 0
    ElMessage.error('加载风险事件失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadData()
}

const handleReset = () => {
  filterForm.eventNo = ''
  filterForm.riskLevel = ''
  filterForm.category = ''
  filterForm.status = ''
  filterForm.dateRange = []
  currentPage.value = 1
  loadData()
}

const viewDetail = (row: RiskEventItem) => {
  currentEvent.value = row
  activeTab.value = 'info'
  handleRemark.value = ''
  drawerVisible.value = true
}

const handleProcess = (row: RiskEventItem) => {
  viewDetail(row)
}

const getEventIdentifier = (row: RiskEventItem): string | number => {
  return row.id ?? row.eventId ?? row.eventNo ?? ''
}

const handleConfirmRisk = async () => {
  if (!currentEvent.value) return
  const eventId = getEventIdentifier(currentEvent.value)
  try {
    await riskApi.handleEvent(eventId as any, {
      action: 'PROCESSED',
      note: handleRemark.value || '已确认处理'
    })
    ElMessage.success('处理成功')
    drawerVisible.value = false
    loadData()
  } catch (e) {
    console.error('Failed to handle event:', e)
    ElMessage.error('处理失败')
  }
}

const handleMarkFalse = async () => {
  if (!currentEvent.value) return
  const eventId = getEventIdentifier(currentEvent.value)
  try {
    await riskApi.handleEvent(eventId as any, {
      action: 'IGNORED',
      note: handleRemark.value || '标记为忽略'
    })
    ElMessage.success('已标记为忽略')
    drawerVisible.value = false
    loadData()
  } catch (e) {
    console.error('Failed to ignore event:', e)
    ElMessage.error('操作失败')
  }
}

onMounted(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
.events-page {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
  }

  .page-title {
    font-size: 22px;
    font-weight: 600;
    margin: 0 0 4px 0;
  }

  .page-desc {
    font-size: 13px;
    color: var(--text-secondary);
    margin: 0;
  }

  .header-actions {
    display: flex;
    gap: 12px;
    align-items: center;
  }

  .badge-btn {
    :deep(.el-badge__content) {
      top: 8px;
      right: 12px;
    }
  }
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  cursor: pointer;
  transition: transform var(--transition-fast), box-shadow var(--transition-fast);

  &:hover {
    transform: translateY(-2px);
  }

  .stat-icon {
    width: 52px;
    height: 52px;
    border-radius: var(--radius-lg);
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 24px;
    flex-shrink: 0;
  }

  .stat-content {
    flex: 1;
  }

  .stat-value {
    font-size: 28px;
    font-weight: 700;
    color: var(--text-primary);
    line-height: 1.2;
    font-family: 'SF Mono', Monaco, 'Courier New', monospace;
  }

  .stat-label {
    font-size: 13px;
    color: var(--text-secondary);
    margin-top: 4px;
  }
}

.filter-card {
  padding: 20px 20px 0;
  margin-bottom: 16px;
}

.filter-form {
  .el-form-item {
    margin-bottom: 20px;
  }
}

.table-card {
  padding: 20px;
}

.table-header {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  margin-bottom: 16px;
}

.table-info {
  font-size: 13px;
  color: var(--text-secondary);

  .highlight {
    color: var(--primary-color);
    font-weight: 600;
  }
}

.mono-text {
  font-family: 'SF Mono', Monaco, 'Courier New', monospace;
  color: var(--primary-color);
  font-size: 12px;
}

.related-cell {
  .related-name {
    font-weight: 500;
    color: var(--text-primary);
    font-size: 13px;
  }

  .related-id {
    margin-top: 2px;
  }
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.event-detail {
  padding: 20px;
  padding-bottom: 140px;
}

.detail-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px;
  border-radius: var(--radius-lg);
  margin-bottom: 20px;

  &.high {
    background: linear-gradient(135deg, var(--danger-bg), #FFF0F0);
    border: 1px solid rgba(245, 63, 63, 0.15);
  }

  &.medium {
    background: linear-gradient(135deg, var(--warning-bg), #FFF7E6);
    border: 1px solid rgba(255, 125, 0, 0.15);
  }

  &.low {
    background: linear-gradient(135deg, var(--success-bg), #F0FFF4);
    border: 1px solid rgba(0, 180, 42, 0.15);
  }

  .header-left {
    display: flex;
    align-items: center;
    gap: 16px;
  }

  .risk-badge {
    width: 48px;
    height: 48px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 24px;

    &.high {
      background: var(--danger-color);
      color: #fff;
    }

    &.medium {
      background: var(--warning-color);
      color: #fff;
    }

    &.low {
      background: var(--success-color);
      color: #fff;
    }
  }

  .header-info {
    h3 {
      margin: 0 0 6px 0;
      font-size: 18px;
      font-weight: 600;
    }

    .header-meta {
      display: flex;
      align-items: center;
      font-size: 13px;
      color: var(--text-secondary);
      gap: 4px;

      .el-icon {
        margin-right: 2px;
        vertical-align: middle;
      }
    }
  }
}

:deep(.detail-tabs) {
  .el-tabs__nav-wrap::after {
    background: var(--border-light);
  }
}

.section-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--border-light);

  .el-icon {
    color: var(--primary-color);
  }
}

.info-descriptions {
  margin-top: 8px;
}

.risk-desc {
  line-height: 1.6;
  color: var(--text-regular);
  white-space: pre-wrap;
  word-break: break-all;
}

.drawer-footer {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 16px 20px;
  background: var(--bg-container);
  border-top: 1px solid var(--border-light);
  box-shadow: 0 -4px 12px rgba(0, 0, 0, 0.04);
}

.handle-form {
  margin-bottom: 12px;
}

.handle-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}

:deep(.el-drawer__body) {
  position: relative;
  overflow-y: auto;
}
</style>
