<template>
  <div class="page-container transaction-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">交易订单</h2>
        <p class="page-desc">查询和管理所有支付交易订单</p>
      </div>
      <div class="header-actions">
        <el-button>
          <el-icon><RefreshRight /></el-icon>
          同步订单
        </el-button>
        <el-button type="primary">
          <el-icon><Download /></el-icon>
          导出订单
        </el-button>
      </div>
    </div>

    <div class="card-shadow filter-card">
      <div class="filter-header">
        <span class="filter-title">筛选条件</span>
        <el-link type="primary" :underline="false" @click="showMoreFilter = !showMoreFilter">
          {{ showMoreFilter ? '收起' : '展开更多' }}
          <el-icon class="el-icon--right" :class="{ 'is-rotate': showMoreFilter }"><ArrowDown /></el-icon>
        </el-link>
      </div>
      <el-form :model="filterForm" class="filter-form">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-form-item label="订单号">
              <el-input v-model="filterForm.orderNo" placeholder="请输入订单号" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="商户号">
              <el-input v-model="filterForm.merchantNo" placeholder="请输入商户号" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="支付通道">
              <el-select v-model="filterForm.channelCode" placeholder="全部通道" clearable style="width: 100%">
                <el-option label="微信支付" value="WECHAT" />
                <el-option label="支付宝" value="ALIPAY" />
                <el-option label="银联支付" value="UNIONPAY" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="订单状态">
              <el-select v-model="filterForm.status" placeholder="全部状态" clearable style="width: 100%">
                <el-option label="待支付" :value="0" />
                <el-option label="支付中" :value="1" />
                <el-option label="支付成功" :value="2" />
                <el-option label="支付失败" :value="3" />
                <el-option label="已关闭" :value="4" />
                <el-option label="已退款" :value="5" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20" v-show="showMoreFilter">
          <el-col :span="6">
            <el-form-item label="支付方式">
              <el-select v-model="filterForm.payType" placeholder="全部方式" clearable style="width: 100%">
                <el-option label="JSAPI" value="JSAPI" />
                <el-option label="Native" value="NATIVE" />
                <el-option label="H5" value="H5" />
                <el-option label="APP" value="APP" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="金额区间">
              <div class="amount-range">
                <el-input-number v-model="filterForm.minAmount" placeholder="最小" :min="0" controls-position="right" style="width: 48%" />
                <span style="color: var(--text-secondary)">-</span>
                <el-input-number v-model="filterForm.maxAmount" placeholder="最大" :min="0" controls-position="right" style="width: 48%" />
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="币种">
              <el-select v-model="filterForm.currency" placeholder="全部币种" clearable style="width: 100%">
                <el-option label="人民币 CNY" value="CNY" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="交易时间">
              <el-date-picker
                v-model="filterForm.dateRange"
                type="datetimerange"
                range-separator="至"
                start-placeholder="开始时间"
                end-placeholder="结束时间"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24" style="text-align: right">
            <el-button type="primary" @click="handleSearch">
              <el-icon><Search /></el-icon>
              查询
            </el-button>
            <el-button @click="handleReset">
              <el-icon><Refresh /></el-icon>
              重置
            </el-button>
          </el-col>
        </el-row>
      </el-form>
    </div>

    <div class="stats-bar card-shadow">
      <div class="stat-item" v-for="stat in statsData" :key="stat.label">
        <div class="stat-label">{{ stat.label }}</div>
        <div class="stat-value" :style="{ color: stat.color }">{{ stat.value }}</div>
      </div>
    </div>

    <div class="card-shadow table-card">
      <div class="table-header">
        <div class="table-actions">
          <el-button size="default" :disabled="selectedRows.length === 0">
            <el-icon><Download /></el-icon>
            批量导出
          </el-button>
          <el-button size="default" type="warning" :disabled="selectedRows.length === 0">
            <el-icon><Warning /></el-icon>
            批量补单
          </el-button>
        </div>
      </div>
      <el-table
        :data="tableData"
        style="width: 100%"
        stripe
        v-loading="loading"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column prop="orderNo" label="订单号" min-width="200" fixed="left">
          <template #default="{ row }">
            <span class="link-text mono-text" @click="viewDetail(row)">{{ row.orderNo ?? '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="merchantNo" label="商户号" min-width="140">
          <template #default="{ row }">
            <span class="mono-text">{{ row.merchantNo ?? '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="merchantName" label="商户名称" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.merchantName ?? '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="channelName" label="支付通道" width="120">
          <template #default="{ row }">
            <el-tag size="small" effect="plain" :color="getChannelColor(row.channelCode ?? '')" style="color: #fff; border: none">
              {{ row.channelName ?? getChannelName(row.channelCode ?? '') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="payType" label="支付方式" width="100">
          <template #default="{ row }">
            {{ getPayTypeName(row.payType ?? '-') }}
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="订单金额" width="120" align="right">
          <template #default="{ row }">
            <span class="amount-text">{{ formatAmount(row.amount) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="actualAmount" label="实付金额" width="120" align="right">
          <template #default="{ row }">
            <span class="amount-text">{{ formatAmount(row.actualAmount ?? row.amount) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="fee" label="手续费" width="100" align="right">
          <template #default="{ row }">
            <span style="color: var(--warning-color)">{{ formatAmount(row.fee ?? 0) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="clientIp" label="客户端IP" width="130">
          <template #default="{ row }">
            {{ row.clientIp ?? '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="paidAt" label="支付时间" width="170">
          <template #default="{ row }">
            {{ row.paidAt ? formatDateTime(row.paidAt) : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="viewDetail(row)">详情</el-button>
            <el-button type="primary" link size="small">回调</el-button>
            <el-button type="danger" link size="small" v-if="row.status === 2">退款</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
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
      title="订单详情"
      direction="rtl"
      size="680px"
      :destroy-on-close="true"
    >
      <div v-if="currentOrder" class="order-detail">
        <div class="detail-status-bar" :class="getStatusClass(currentOrder.status)">
          <div class="status-icon">
            <el-icon :size="40" color="#fff">
              <CircleCheck v-if="currentOrder.status === 2" />
              <CircleClose v-else-if="currentOrder.status === 3" />
              <Clock v-else-if="currentOrder.status === 0 || currentOrder.status === 1" />
              <Warning v-else />
            </el-icon>
          </div>
          <div class="status-info">
            <h3>{{ getStatusText(currentOrder.status) }}</h3>
            <p>订单号：<span class="mono-text">{{ currentOrder.orderNo ?? '-' }}</span></p>
          </div>
        </div>

        <el-tabs v-model="activeTab" class="detail-tabs">
          <el-tab-pane label="订单信息" name="order">
            <el-descriptions title="基本信息" :column="2" border class="info-section">
              <el-descriptions-item label="商户名称">{{ currentOrder.merchantName ?? '-' }}</el-descriptions-item>
              <el-descriptions-item label="商户号">
                <span class="mono-text">{{ currentOrder.merchantNo ?? '-' }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="订单号" :span="2">
                <span class="mono-text">{{ currentOrder.orderNo ?? '-' }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="订单金额">
                <span class="amount-text">{{ formatAmount(currentOrder.amount) }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="手续费">
                <span style="color: var(--warning-color)">{{ formatAmount(currentOrder.fee ?? 0) }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="支付通道">{{ currentOrder.channelName ?? getChannelName(currentOrder.channelCode ?? '') }}</el-descriptions-item>
              <el-descriptions-item label="支付方式">{{ getPayTypeName(currentOrder.payType ?? '-') }}</el-descriptions-item>
              <el-descriptions-item label="客户端IP">{{ currentOrder.clientIp ?? '-' }}</el-descriptions-item>
              <el-descriptions-item label="创建时间">{{ formatDateTime(currentOrder.createdAt) }}</el-descriptions-item>
              <el-descriptions-item label="支付时间">{{ currentOrder.paidAt ? formatDateTime(currentOrder.paidAt) : '-' }}</el-descriptions-item>
              <el-descriptions-item label="过期时间" :span="2">{{ currentOrder.expireTime ? formatDateTime(currentOrder.expireTime) : '-' }}</el-descriptions-item>
              <el-descriptions-item label="商品描述" :span="2">{{ currentOrder.subject ?? '-' }}</el-descriptions-item>
            </el-descriptions>
          </el-tab-pane>
          <el-tab-pane label="渠道信息" name="channel">
            <el-descriptions title="渠道返回" :column="1" border class="info-section">
              <el-descriptions-item label="渠道交易号">
                <span class="mono-text">{{ currentOrder.channelOrderNo ?? '-' }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="渠道返回信息">{{ currentOrder.channelResponse ?? '-' }}</el-descriptions-item>
            </el-descriptions>
          </el-tab-pane>
        </el-tabs>

        <div class="detail-actions" v-if="currentOrder.status === 2">
          <el-button type="danger">
            <el-icon><RefreshLeft /></el-icon>
            发起退款
          </el-button>
          <el-button>
            <el-icon><Promotion /></el-icon>
            重新通知
          </el-button>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { RefreshRight, Download, ArrowDown, Search, Refresh, Warning, CircleCheck, CircleClose, Clock, RefreshLeft, Promotion } from '@element-plus/icons-vue'
import { tradeApi } from '@/api/transaction'
import type { TradeOrder } from '@/types/transaction'

const loading = ref(false)
const showMoreFilter = ref(false)
const drawerVisible = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const selectedRows = ref<TradeOrder[]>([])
const currentOrder = ref<TradeOrder | null>(null)
const activeTab = ref('order')
const tableData = ref<TradeOrder[]>([])

const filterForm = reactive({
  orderNo: '',
  merchantNo: '',
  channelCode: '',
  status: '' as number | string,
  payType: '',
  minAmount: undefined as number | undefined,
  maxAmount: undefined as number | undefined,
  currency: '',
  dateRange: [] as string[]
})

const tradeStatusMap: Record<number, { text: string; type: string; cls: string }> = {
  0: { text: '待支付', type: 'info', cls: 'pending' },
  1: { text: '支付中', type: 'warning', cls: 'paying' },
  2: { text: '支付成功', type: 'success', cls: 'success' },
  3: { text: '支付失败', type: 'danger', cls: 'failed' },
  4: { text: '已关闭', type: 'info', cls: 'closed' },
  5: { text: '已退款', type: '', cls: 'refunded' },
  6: { text: '部分退款', type: 'warning', cls: 'refunded' }
}

const channelColors: Record<string, string> = {
  WECHAT: '#07C160',
  ALIPAY: '#1677FF',
  UNIONPAY: '#E60012'
}

const channelNameMap: Record<string, string> = {
  WECHAT: '微信支付',
  ALIPAY: '支付宝',
  UNIONPAY: '银联云闪付'
}

const payTypeMap: Record<string, string> = {
  JSAPI: 'JSAPI',
  NATIVE: 'Native',
  H5: 'H5',
  APP: 'APP'
}

const statsData = computed(() => {
  const list = tableData.value
  const todayCount = total.value
  const todayAmount = list.reduce((s, r) => s + (Number(r.actualAmount ?? r.amount) || 0), 0)
  const successList = list.filter(r => r.status === 2)
  const successCount = successList.length
  const pendingCount = list.filter(r => r.status === 0 || r.status === 1).length
  const failCount = list.filter(r => r.status === 3).length
  const successRate = list.length > 0 ? ((successCount / list.length) * 100).toFixed(1) + '%' : '0%'
  return [
    { label: '今日交易笔数', value: String(todayCount), color: 'var(--primary-color)' },
    { label: '今日交易金额', value: '¥' + formatAmountNum(todayAmount), color: 'var(--success-color)' },
    { label: '成功率', value: successRate, color: 'var(--success-color)' },
    { label: '待支付订单', value: String(pendingCount), color: 'var(--warning-color)' },
    { label: '支付失败', value: String(failCount), color: 'var(--danger-color)' }
  ]
})

function formatAmountNum(amount: number | undefined | null): string {
  if (amount === null || amount === undefined || isNaN(amount as number)) return '0.00'
  return Number(amount).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function formatAmount(amount: number | undefined | null | string): string {
  if (amount === null || amount === undefined) return '¥0.00'
  const num = typeof amount === 'string' ? parseFloat(amount) : amount
  if (isNaN(num)) return '¥0.00'
  return '¥' + num.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function formatDateTime(dt: string | undefined | null): string {
  if (!dt) return '-'
  if (typeof dt === 'string' && dt.includes('T')) {
    return dt.replace('T', ' ').substring(0, 19)
  }
  return dt
}

function getChannelName(code: string): string {
  if (!code) return '-'
  const upper = code.toUpperCase()
  return channelNameMap[upper] ?? code
}

function getChannelColor(channel: string): string {
  if (!channel) return 'var(--primary-color)'
  return channelColors[channel.toUpperCase()] ?? 'var(--primary-color)'
}

function getPayTypeName(payType: string): string {
  if (!payType) return '-'
  return payTypeMap[payType.toUpperCase()] ?? payType
}

function getStatusType(status: number | string | undefined) {
  if (status === undefined || status === null) return 'info'
  if (typeof status === 'number') {
    return tradeStatusMap[status]?.type ?? 'info'
  }
  const statusStr = String(status).toUpperCase()
  if (statusStr === 'SUCCESS' || statusStr === '2') return 'success'
  if (statusStr === 'PAYING' || statusStr === 'PROCESSING' || statusStr === '1') return 'warning'
  if (statusStr === 'FAILED' || statusStr === 'FAIL' || statusStr === '3') return 'danger'
  if (statusStr === 'PENDING' || statusStr === '0') return 'info'
  if (statusStr === 'CLOSED' || statusStr === '4') return 'info'
  if (statusStr === 'REFUNDED' || statusStr === '5') return ''
  return 'info'
}

function getStatusText(status: number | string | undefined) {
  if (status === undefined || status === null) return '未知'
  if (typeof status === 'number') {
    return tradeStatusMap[status]?.text ?? '未知'
  }
  return String(status)
}

function getStatusClass(status: number | string | undefined) {
  if (status === undefined || status === null) return 'pending'
  if (typeof status === 'number') {
    return tradeStatusMap[status]?.cls ?? 'pending'
  }
  return 'pending'
}

async function loadData() {
  loading.value = true
  try {
    const params: any = {
      page: currentPage.value,
      pageSize: pageSize.value
    }
    if (filterForm.orderNo) params.orderNo = filterForm.orderNo
    if (filterForm.merchantNo) params.merchantNo = filterForm.merchantNo
    if (filterForm.channelCode) params.channelCode = filterForm.channelCode
    if (filterForm.status !== '' && filterForm.status !== undefined && filterForm.status !== null) {
      params.status = filterForm.status
    }
    if (filterForm.payType) params.payType = filterForm.payType
    if (filterForm.minAmount !== undefined) params.minAmount = filterForm.minAmount
    if (filterForm.maxAmount !== undefined) params.maxAmount = filterForm.maxAmount
    if (filterForm.dateRange && filterForm.dateRange.length === 2) {
      params.startTime = filterForm.dateRange[0]
      params.endTime = filterForm.dateRange[1]
    }
    const res = await tradeApi.getList(params)
    if (res) {
      tableData.value = res.list ?? []
      total.value = res.total ?? 0
    }
  } catch (e) {
    console.error('Failed to load trade orders:', e)
    tableData.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadData()
}

const handleReset = () => {
  filterForm.orderNo = ''
  filterForm.merchantNo = ''
  filterForm.channelCode = ''
  filterForm.status = ''
  filterForm.payType = ''
  filterForm.minAmount = undefined
  filterForm.maxAmount = undefined
  filterForm.currency = ''
  filterForm.dateRange = []
  currentPage.value = 1
  loadData()
}

const handleSelectionChange = (rows: TradeOrder[]) => {
  selectedRows.value = rows
}

const viewDetail = (row: TradeOrder) => {
  currentOrder.value = row
  drawerVisible.value = true
  activeTab.value = 'order'
}

onMounted(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
.transaction-page {
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
  }
}

.filter-card {
  padding: 20px;
  margin-bottom: 16px;
}

.filter-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--border-light);
}

.filter-title {
  font-size: 15px;
  font-weight: 600;
}

.filter-form {
  .el-form-item {
    margin-bottom: 16px;
  }
}

.amount-range {
  display: flex;
  align-items: center;
  gap: 8px;
}

.stats-bar {
  padding: 16px 20px;
  margin-bottom: 16px;
  display: flex;
  gap: 40px;
}

.stat-item {
  .stat-label {
    font-size: 13px;
    color: var(--text-secondary);
    margin-bottom: 6px;
  }

  .stat-value {
    font-size: 24px;
    font-weight: 700;
  }
}

.table-card {
  padding: 20px;
}

.table-header {
  margin-bottom: 16px;
}

.table-actions {
  display: flex;
  gap: 8px;
}

.link-text {
  color: var(--primary-color);
  cursor: pointer;

  &:hover {
    text-decoration: underline;
  }
}

.mono-text {
  font-family: 'SF Mono', Monaco, 'Courier New', monospace;
}

.amount-text {
  font-weight: 600;
  font-family: 'SF Mono', Monaco, 'Courier New', monospace;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.order-detail {
  padding: 0 4px;
}

.detail-status-bar {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 24px;
  border-radius: var(--radius-lg);
  margin-bottom: 24px;
  color: #fff;

  &.success { background: linear-gradient(135deg, #00B42A, #52C41A); }
  &.failed { background: linear-gradient(135deg, #F53F3F, #FF7875); }
  &.pending, &.paying { background: linear-gradient(135deg, #FF7D00, #FAAD14); }
  &.closed, &.refunded { background: linear-gradient(135deg, #86909C, #C9CDD4); }

  .status-icon {
    width: 64px;
    height: 64px;
    background: rgba(255, 255, 255, 0.2);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .status-info {
    h3 {
      margin: 0 0 6px 0;
      font-size: 22px;
      font-weight: 600;
    }
    p {
      margin: 0;
      font-size: 14px;
      opacity: 0.9;
    }
  }
}

.info-section {
  margin-top: 8px;
}

.detail-actions {
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid var(--border-light);
  display: flex;
  gap: 12px;
}
</style>
