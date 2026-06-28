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
              <el-input v-model="filterForm.merchantId" placeholder="请输入商户号" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="支付通道">
              <el-select v-model="filterForm.channel" placeholder="全部通道" clearable style="width: 100%">
                <el-option label="微信支付" value="wechat" />
                <el-option label="支付宝" value="alipay" />
                <el-option label="银联支付" value="unionpay" />
                <el-option label="Visa/MC" value="card" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="订单状态">
              <el-select v-model="filterForm.status" placeholder="全部状态" clearable style="width: 100%">
                <el-option label="待支付" value="pending" />
                <el-option label="支付中" value="paying" />
                <el-option label="支付成功" value="success" />
                <el-option label="支付失败" value="failed" />
                <el-option label="已关闭" value="closed" />
                <el-option label="已退款" value="refunded" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20" v-show="showMoreFilter">
          <el-col :span="6">
            <el-form-item label="支付方式">
              <el-select v-model="filterForm.payMethod" placeholder="全部方式" clearable style="width: 100%">
                <el-option label="JSAPI" value="jsapi" />
                <el-option label="Native" value="native" />
                <el-option label="H5" value="h5" />
                <el-option label="APP" value="app" />
                <el-option label="信用卡" value="creditcard" />
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
                <el-option label="美元 USD" value="USD" />
                <el-option label="欧元 EUR" value="EUR" />
                <el-option label="日元 JPY" value="JPY" />
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
            <span class="link-text mono-text" @click="viewDetail(row)">{{ row.orderNo }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="merchantOrderNo" label="商户订单号" min-width="180">
          <template #default="{ row }">
            <span class="mono-text">{{ row.merchantOrderNo }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="merchantName" label="商户名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="channel" label="支付通道" width="110">
          <template #default="{ row }">
            <el-tag size="small" effect="plain" :color="getChannelColor(row.channel)" style="color: #fff; border: none">
              {{ row.channel }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="payMethod" label="支付方式" width="100" />
        <el-table-column prop="amount" label="订单金额" width="120" align="right">
          <template #default="{ row }">
            <span class="amount-text">{{ row.currency }} {{ formatNumber(row.amount) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column prop="payTime" label="支付时间" width="160" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="viewDetail(row)">详情</el-button>
            <el-button type="primary" link size="small">回调</el-button>
            <el-button type="danger" link size="small" v-if="row.status === 'success'">退款</el-button>
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
        <div class="detail-status-bar" :class="currentOrder.status">
          <div class="status-icon">
            <el-icon :size="40" color="#fff">
              <CircleCheck v-if="currentOrder.status === 'success'" />
              <CircleClose v-else-if="currentOrder.status === 'failed'" />
              <Clock v-else-if="currentOrder.status === 'pending' || currentOrder.status === 'paying'" />
              <Warning v-else />
            </el-icon>
          </div>
          <div class="status-info">
            <h3>{{ getStatusText(currentOrder.status) }}</h3>
            <p>订单号：<span class="mono-text">{{ currentOrder.orderNo }}</span></p>
          </div>
        </div>

        <el-tabs v-model="activeTab" class="detail-tabs">
          <el-tab-pane label="订单信息" name="order">
            <el-descriptions title="基本信息" :column="2" border class="info-section">
              <el-descriptions-item label="商户名称">{{ currentOrder.merchantName }}</el-descriptions-item>
              <el-descriptions-item label="商户号">
                <span class="mono-text">{{ currentOrder.merchantId }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="商户订单号" :span="2">
                <span class="mono-text">{{ currentOrder.merchantOrderNo }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="订单金额">
                <span class="amount-text">{{ currentOrder.currency }} {{ formatNumber(currentOrder.amount) }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="手续费">
                <span style="color: var(--warning-color)">{{ currentOrder.currency }} {{ currentOrder.fee }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="支付通道">{{ currentOrder.channel }}</el-descriptions-item>
              <el-descriptions-item label="支付方式">{{ currentOrder.payMethod }}</el-descriptions-item>
              <el-descriptions-item label="创建时间">{{ currentOrder.createTime }}</el-descriptions-item>
              <el-descriptions-item label="支付时间">{{ currentOrder.payTime || '-' }}</el-descriptions-item>
              <el-descriptions-item label="过期时间" :span="2">{{ currentOrder.expireTime || '-' }}</el-descriptions-item>
              <el-descriptions-item label="商品描述" :span="2">{{ currentOrder.subject }}</el-descriptions-item>
            </el-descriptions>
          </el-tab-pane>
          <el-tab-pane label="渠道信息" name="channel">
            <el-descriptions title="渠道返回" :column="1" border class="info-section">
              <el-descriptions-item label="渠道交易号">
                <span class="mono-text">{{ currentOrder.channelTradeNo || '-' }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="渠道返回码">{{ currentOrder.channelCode || '-' }}</el-descriptions-item>
              <el-descriptions-item label="渠道返回信息">{{ currentOrder.channelMsg || '-' }}</el-descriptions-item>
            </el-descriptions>
          </el-tab-pane>
          <el-tab-pane label="回调记录" name="notify">
            <el-timeline>
              <el-timeline-item
                v-for="(notify, index) in notifyRecords"
                :key="index"
                :timestamp="notify.time"
                :type="notify.success ? 'success' : 'danger'"
              >
                <div class="notify-record">
                  <span>回调{{ notify.success ? '成功' : '失败' }}</span>
                  <span class="notify-detail">次数：{{ index + 1 }}，耗时：{{ notify.duration }}ms</span>
                </div>
              </el-timeline-item>
            </el-timeline>
          </el-tab-pane>
          <el-tab-pane label="调用链路" name="trace">
            <div class="trace-info">
              <div class="trace-id">
                <span class="label">Trace ID:</span>
                <span class="mono-text value">{{ currentOrder.traceId }}</span>
                <el-button type="primary" link size="small">复制</el-button>
              </div>
              <div class="trace-timeline">
                <div v-for="(step, index) in traceSteps" :key="index" class="trace-step">
                  <div class="step-dot"></div>
                  <div class="step-line" v-if="index < traceSteps.length - 1"></div>
                  <div class="step-content">
                    <div class="step-header">
                      <span class="step-name">{{ step.name }}</span>
                      <span class="step-duration" :class="step.duration > 100 ? 'warning' : ''">{{ step.duration }}ms</span>
                    </div>
                    <div class="step-time">{{ step.time }}</div>
                  </div>
                </div>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>

        <div class="detail-actions" v-if="currentOrder.status === 'success'">
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
import { ref, reactive } from 'vue'
import { RefreshRight, Download, ArrowDown, Search, Refresh, Warning, CircleCheck, CircleClose, Clock, RefreshLeft, Promotion } from '@element-plus/icons-vue'

const loading = ref(false)
const showMoreFilter = ref(false)
const drawerVisible = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(286)
const selectedRows = ref<any[]>([])
const currentOrder = ref<any>(null)
const activeTab = ref('order')

const filterForm = reactive({
  orderNo: '',
  merchantId: '',
  channel: '',
  status: '',
  payMethod: '',
  minAmount: undefined as number | undefined,
  maxAmount: undefined as number | undefined,
  currency: '',
  dateRange: []
})

const statsData = [
  { label: '今日交易笔数', value: '12,856', color: 'var(--primary-color)' },
  { label: '今日交易金额', value: '¥2,845,632', color: 'var(--success-color)' },
  { label: '成功率', value: '99.72%', color: 'var(--success-color)' },
  { label: '待支付订单', value: '156', color: 'var(--warning-color)' },
  { label: '支付失败', value: '36', color: 'var(--danger-color)' }
]

const tableData = ref([
  { orderNo: 'PAY20260628000123456', merchantOrderNo: 'ORD20260628001', merchantName: '星辰电商平台', merchantId: 'M100001', channel: '微信支付', payMethod: 'JSAPI', amount: 29900, currency: 'CNY', fee: 179, status: 'success', createTime: '2026-06-28 14:32:15', payTime: '2026-06-28 14:32:18', subject: 'VIP会员年卡', channelTradeNo: '4200001234202606281234567890', channelCode: 'SUCCESS', channelMsg: '支付成功', traceId: 'abc123def456ghi789' },
  { orderNo: 'PAY20260628000123455', merchantOrderNo: 'ORD20260628002', merchantName: '云海餐饮连锁', merchantId: 'M100002', channel: '支付宝', payMethod: '手机网站', amount: 128000, currency: 'CNY', fee: 704, status: 'success', createTime: '2026-06-28 14:31:42', payTime: '2026-06-28 14:31:45', subject: '团建聚餐费用', channelTradeNo: '2026062822001234567890123456', channelCode: '10000', channelMsg: 'Success', traceId: 'def456ghi789abc123' },
  { orderNo: 'PAY20260628000123454', merchantOrderNo: 'ORD20260628003', merchantName: '某跨境电商', merchantId: 'M100007', channel: 'Visa/MC', payMethod: '信用卡', amount: 56800, currency: 'CNY', fee: 1704, status: 'paying', createTime: '2026-06-28 14:31:08', payTime: null, subject: '跨境商品订单', channelTradeNo: null, channelCode: null, channelMsg: null, traceId: 'ghi789abc123def456' },
  { orderNo: 'PAY20260628000123453', merchantOrderNo: 'ORD20260628004', merchantName: '智学在线教育', merchantId: 'M100003', channel: '微信支付', payMethod: 'H5', amount: 9900, currency: 'CNY', fee: 59, status: 'success', createTime: '2026-06-28 14:30:55', payTime: '2026-06-28 14:31:02', subject: '精品课程购买', channelTradeNo: '4200001234202606281234567891', channelCode: 'SUCCESS', channelMsg: '支付成功', traceId: 'jkl012mno345pqr678' },
  { orderNo: 'PAY20260628000123452', merchantOrderNo: 'ORD20260628005', merchantName: '速达出行科技', merchantId: 'M100004', channel: '银联支付', payMethod: '云闪付', amount: 156000, currency: 'CNY', fee: 780, status: 'failed', createTime: '2026-06-28 14:30:33', payTime: null, subject: '企业用车充值', channelTradeNo: null, channelCode: 'TIMEOUT', channelMsg: '渠道响应超时', traceId: 'mno345pqr678stu901' },
  { orderNo: 'PAY20260628000123451', merchantOrderNo: 'ORD20260628006', merchantName: '趣玩数字娱乐', merchantId: 'M100005', channel: '支付宝', payMethod: '电脑网站', amount: 45600, currency: 'CNY', fee: 251, status: 'success', createTime: '2026-06-28 14:30:12', payTime: '2026-06-28 14:30:18', subject: '游戏充值', channelTradeNo: '2026062822001234567890123457', channelCode: '10000', channelMsg: 'Success', traceId: 'pqr678stu901vwx234' }
])

const notifyRecords = [
  { time: '2026-06-28 14:32:20', success: true, duration: 45 },
  { time: '2026-06-28 14:32:19', success: false, duration: 3200 },
  { time: '2026-06-28 14:32:18', success: true, duration: 38 }
]

const traceSteps = [
  { name: 'API网关接收请求', time: '2026-06-28 14:32:15.123', duration: 2 },
  { name: '风控引擎校验', time: '2026-06-28 14:32:15.125', duration: 3 },
  { name: '智能路由决策', time: '2026-06-28 14:32:15.128', duration: 1 },
  { name: '订单服务创建订单', time: '2026-06-28 14:32:15.129', duration: 12 },
  { name: '微信支付渠道适配', time: '2026-06-28 14:32:15.141', duration: 8 },
  { name: '调用微信下单接口', time: '2026-06-28 14:32:15.149', duration: 256 },
  { name: '接收支付结果回调', time: '2026-06-28 14:32:18.405', duration: 5 },
  { name: '更新订单状态', time: '2026-06-28 14:32:18.410', duration: 18 },
  { name: '异步通知商户', time: '2026-06-28 14:32:18.428', duration: 45 }
]

const formatNumber = (num: number) => (num / 100).toLocaleString('zh-CN', { minimumFractionDigits: 2 })

const getChannelColor = (channel: string) => {
  const map: Record<string, string> = { '微信支付': '#07C160', '支付宝': '#1677FF', '银联支付': '#E60012', 'Visa/MC': '#1A1F71' }
  return map[channel] || 'var(--primary-color)'
}

const getStatusType = (status: string) => {
  const map: Record<string, string> = { success: 'success', pending: 'info', paying: 'warning', failed: 'danger', closed: 'info', refunded: '' }
  return map[status] || 'info'
}

const getStatusText = (status: string) => {
  const map: Record<string, string> = { success: '支付成功', pending: '待支付', paying: '支付中', failed: '支付失败', closed: '已关闭', refunded: '已退款' }
  return map[status] || '未知'
}

const handleSearch = () => {
  loading.value = true
  setTimeout(() => loading.value = false, 500)
}

const handleReset = () => {
  Object.keys(filterForm).forEach(key => {
    (filterForm as any)[key] = key === 'dateRange' ? [] : undefined
  })
}

const handleSelectionChange = (rows: any[]) => {
  selectedRows.value = rows
}

const viewDetail = (row: any) => {
  currentOrder.value = row
  drawerVisible.value = true
  activeTab.value = 'order'
}
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

.notify-record {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.notify-detail {
  font-size: 12px;
  color: var(--text-secondary);
}

.trace-info {
  padding: 8px 0;
}

.trace-id {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: var(--bg-hover);
  border-radius: var(--radius-md);
  margin-bottom: 20px;

  .label {
    color: var(--text-secondary);
    font-size: 13px;
  }

  .value {
    flex: 1;
    color: var(--primary-color);
  }
}

.trace-timeline {
  padding-left: 8px;
}

.trace-step {
  position: relative;
  padding-left: 24px;
  padding-bottom: 20px;

  &:last-child {
    padding-bottom: 0;
  }
}

.step-dot {
  position: absolute;
  left: 0;
  top: 4px;
  width: 10px;
  height: 10px;
  background: var(--primary-color);
  border-radius: 50%;
  border: 2px solid var(--primary-bg);
}

.step-line {
  position: absolute;
  left: 4px;
  top: 18px;
  width: 2px;
  height: calc(100% - 14px);
  background: var(--border-color);
}

.step-content {
  .step-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 2px;
  }

  .step-name {
    font-weight: 500;
    color: var(--text-primary);
  }

  .step-duration {
    font-family: 'SF Mono', Monaco, 'Courier New', monospace;
    font-size: 13px;
    color: var(--success-color);
    font-weight: 600;

    &.warning {
      color: var(--warning-color);
    }
  }

  .step-time {
    font-size: 12px;
    color: var(--text-secondary);
    font-family: 'SF Mono', Monaco, 'Courier New', monospace;
  }
}

.detail-actions {
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid var(--border-light);
  display: flex;
  gap: 12px;
}
</style>
