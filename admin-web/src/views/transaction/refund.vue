<template>
  <div class="page-container refund-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">退款订单</h2>
        <p class="page-desc">管理和审核所有退款申请订单</p>
      </div>
      <div class="header-actions">
        <el-button>
          <el-icon><Download /></el-icon>
          导出退款单
        </el-button>
      </div>
    </div>

    <div class="stats-bar card-shadow">
      <div class="stat-item" v-for="stat in statsData" :key="stat.label">
        <div class="stat-icon" :style="{ background: stat.bgColor, color: stat.color }">
          <el-icon :size="24">
            <component :is="stat.icon" />
          </el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-label">{{ stat.label }}</div>
          <div class="stat-value" :style="{ color: stat.color }">{{ stat.value }}</div>
        </div>
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
            <el-form-item label="退款单号">
              <el-input v-model="filterForm.refundNo" placeholder="请输入退款单号" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="原订单号">
              <el-input v-model="filterForm.orderNo" placeholder="请输入原订单号" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="商户号">
              <el-input v-model="filterForm.merchantNo" placeholder="请输入商户号" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="退款状态">
              <el-select v-model="filterForm.status" placeholder="全部状态" clearable style="width: 100%">
                <el-option label="退款中" :value="0" />
                <el-option label="退款成功" :value="1" />
                <el-option label="退款失败" :value="2" />
                <el-option label="已拒绝" :value="3" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20" v-show="showMoreFilter">
          <el-col :span="6">
            <el-form-item label="支付通道">
              <el-select v-model="filterForm.channelCode" placeholder="全部通道" clearable style="width: 100%">
                <el-option label="微信支付" value="WECHAT" />
                <el-option label="支付宝" value="ALIPAY" />
                <el-option label="银联支付" value="UNIONPAY" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="18">
            <el-form-item label="申请时间">
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

    <div class="card-shadow table-card">
      <div class="table-header">
        <div class="table-actions">
          <el-button size="default" :disabled="selectedRows.length === 0">
            <el-icon><Download /></el-icon>
            批量导出
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
        <el-table-column prop="refundNo" label="退款单号" min-width="200" fixed="left">
          <template #default="{ row }">
            <span class="link-text mono-text" @click="viewDetail(row)">{{ row.refundNo }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="orderNo" label="原订单号" min-width="180">
          <template #default="{ row }">
            <span class="mono-text">{{ row.orderNo }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="merchantName" label="商户名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="channelName" label="支付通道" width="110">
          <template #default="{ row }">
            <el-tag size="small" effect="plain" :color="getChannelColor(row.channelCode)" style="color: #fff; border: none">
              {{ row.channelName || getChannelName(row.channelCode) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="refundAmount" label="退款金额" width="120" align="right">
          <template #default="{ row }">
            <span class="amount-text">CNY {{ formatNumber(row.refundAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="refundReason" label="退款原因" min-width="120" show-overflow-tooltip />
        <el-table-column prop="status" label="退款状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="applyTime" label="申请时间" width="160" />
        <el-table-column prop="finishTime" label="完成时间" width="160">
          <template #default="{ row }">
            {{ row.finishTime || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="viewDetail(row)">详情</el-button>
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
      title="退款详情"
      direction="rtl"
      size="680px"
      :destroy-on-close="true"
    >
      <div v-if="currentRefund" class="refund-detail">
        <div class="detail-status-bar" :class="currentRefund.statusKey">
          <div class="status-icon">
            <el-icon :size="40" color="#fff">
              <CircleCheck v-if="currentRefund.status === 1" />
              <CircleClose v-else-if="currentRefund.status === 2 || currentRefund.status === 3" />
              <Clock v-else />
            </el-icon>
          </div>
          <div class="status-info">
            <h3>{{ getStatusText(currentRefund.status) }}</h3>
            <p>退款单号：<span class="mono-text">{{ currentRefund.refundNo }}</span></p>
          </div>
        </div>

        <el-tabs v-model="activeTab" class="detail-tabs">
          <el-tab-pane label="退款信息" name="refund">
            <el-descriptions title="退款信息" :column="2" border class="info-section">
              <el-descriptions-item label="退款单号" :span="2">
                <span class="mono-text">{{ currentRefund.refundNo }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="原订单号" :span="2">
                <span class="mono-text">{{ currentRefund.orderNo }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="商户号" :span="2">
                <span class="mono-text">{{ currentRefund.merchantNo }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="支付通道">{{ getChannelName(currentRefund.channelCode) }}</el-descriptions-item>
              <el-descriptions-item label="退款金额" label-style="color: var(--danger-color)">
                <span class="amount-text" style="color: var(--danger-color)">CNY {{ formatNumber(currentRefund.refundAmount) }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="退款手续费">CNY {{ formatNumber(currentRefund.refundFee) }}</el-descriptions-item>
              <el-descriptions-item label="退款原因" :span="2">{{ currentRefund.refundReason || '-' }}</el-descriptions-item>
              <el-descriptions-item label="申请时间">{{ currentRefund.applyTime }}</el-descriptions-item>
              <el-descriptions-item label="完成时间">{{ currentRefund.finishTime || '-' }}</el-descriptions-item>
            </el-descriptions>

            <el-descriptions title="渠道信息" :column="2" border class="info-section" style="margin-top: 20px">
              <el-descriptions-item label="渠道退款号">
                <span class="mono-text">{{ currentRefund.channelRefundNo || '-' }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="渠道返回信息" :span="2">{{ currentRefund.channelResponse || '-' }}</el-descriptions-item>
            </el-descriptions>
          </el-tab-pane>
          <el-tab-pane label="操作日志" name="logs">
            <el-timeline>
              <el-timeline-item
                v-for="(log, index) in operationLogs"
                :key="index"
                :timestamp="log.time"
                :type="log.type"
              >
                <div class="log-content">
                  <span class="log-title">{{ log.title }}</span>
                  <span class="log-operator" v-if="log.operator">操作人：{{ log.operator }}</span>
                  <span class="log-desc" v-if="log.desc">{{ log.desc }}</span>
                </div>
              </el-timeline-item>
            </el-timeline>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-drawer>

    <el-dialog v-model="rejectDialogVisible" title="拒绝退款" width="500px" destroy-on-close>
      <el-form :model="rejectForm" label-width="80px">
        <el-form-item label="拒绝原因" required>
          <el-input
            v-model="rejectForm.reason"
            type="textarea"
            :rows="4"
            placeholder="请输入拒绝原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmReject">确认拒绝</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Download, ArrowDown, Search, Refresh, RefreshLeft, Money, Warning, Document, CircleCheck, CircleClose, Clock } from '@element-plus/icons-vue'
import { refundApi } from '@/api/transaction'
import type { RefundOrder } from '@/types/transaction'

const loading = ref(false)
const showMoreFilter = ref(false)
const drawerVisible = ref(false)
const rejectDialogVisible = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const selectedRows = ref<any[]>([])
const currentRefund = ref<any>(null)
const activeTab = ref('refund')

const filterForm = reactive({
  refundNo: '',
  orderNo: '',
  merchantNo: '',
  channelCode: '',
  status: '' as number | string,
  dateRange: [] as string[]
})

const rejectForm = reactive({
  reason: ''
})

const CHANNEL_NAME_MAP: Record<string, string> = {
  WECHAT: '微信支付',
  ALIPAY: '支付宝',
  UNIONPAY: '银联支付'
}

const REFUND_STATUS_MAP: Record<number, { text: string; type: string; key: string }> = {
  0: { text: '退款中', type: 'warning', key: 'processing' },
  1: { text: '退款成功', type: 'success', key: 'success' },
  2: { text: '退款失败', type: 'danger', key: 'failed' },
  3: { text: '已拒绝', type: 'info', key: 'rejected' }
}

const statsData = computed(() => {
  const list = tableData.value
  const todayCount = list.length
  const todayAmount = list.reduce((s, r) => s + (r.refundAmount || 0), 0)
  const successCount = list.filter(r => r.status === 1).length
  const pendingCount = list.filter(r => r.status === 0).length
  const successRate = todayCount > 0 ? ((successCount / todayCount) * 100).toFixed(1) + '%' : '0%'
  return [
    { label: '退款笔数', value: String(total.value), color: 'var(--primary-color)', bgColor: 'rgba(22, 119, 255, 0.1)', icon: Document },
    { label: '退款金额', value: '¥' + todayAmount.toFixed(2), color: 'var(--danger-color)', bgColor: 'rgba(245, 63, 63, 0.1)', icon: Money },
    { label: '退款成功率', value: successRate, color: 'var(--success-color)', bgColor: 'rgba(0, 180, 42, 0.1)', icon: RefreshLeft },
    { label: '处理中退款', value: String(pendingCount), color: 'var(--warning-color)', bgColor: 'rgba(255, 125, 0, 0.1)', icon: Warning }
  ]
})

const tableData = ref<RefundOrder[]>([])

const operationLogs = ref<any[]>([])

const formatNumber = (num: number | undefined) => {
  if (num === undefined || num === null) return '0.00'
  return Number(num).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

const getChannelName = (code: string) => CHANNEL_NAME_MAP[code] || code || '-'
const getChannelColor = (code: string) => {
  const map: Record<string, string> = { WECHAT: '#07C160', ALIPAY: '#1677FF', UNIONPAY: '#E60012' }
  return map[code] || 'var(--primary-color)'
}

const getStatusType = (status: number) => REFUND_STATUS_MAP[status]?.type || 'info'
const getStatusText = (status: number) => REFUND_STATUS_MAP[status]?.text || '未知'
const getStatusKey = (status: number) => REFUND_STATUS_MAP[status]?.key || 'unknown'

function formatDate(dt: string | undefined): string {
  if (!dt) return '-'
  try {
    const d = new Date(dt)
    const pad = (n: number) => String(n).padStart(2, '0')
    return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
  } catch { return dt }
}

function mapRow(r: RefundOrder) {
  return {
    ...r,
    channel: r.channelName || getChannelName(r.channelCode || ''),
    channelDisplay: r.channelCode,
    merchantName: r.merchantName || r.merchantNo || '-',
    statusKey: getStatusKey(r.status ?? 0),
    applyTime: r.createdAt || '-',
    finishTime: r.refundedAt || null
  }
}

async function loadData() {
  loading.value = true
  try {
    const params: any = {
      page: currentPage.value,
      pageSize: pageSize.value
    }
    if (filterForm.refundNo) params.refundNo = filterForm.refundNo
    if (filterForm.orderNo) params.orderNo = filterForm.orderNo
    if (filterForm.merchantNo) params.merchantNo = filterForm.merchantNo
    if (filterForm.channelCode) params.channelCode = filterForm.channelCode
    if (filterForm.status !== '' && filterForm.status !== undefined && filterForm.status !== null) {
      params.status = filterForm.status
    }
    if (filterForm.dateRange && filterForm.dateRange.length === 2) {
      params.startTime = filterForm.dateRange[0]
      params.endTime = filterForm.dateRange[1]
    }
    const res = await refundApi.getList(params)
    if (res) {
      tableData.value = (res.list ?? []).map(mapRow)
      total.value = res.total ?? 0
    }
  } catch (e) {
    console.error('Failed to load refunds:', e)
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
  Object.keys(filterForm).forEach(key => {
    (filterForm as any)[key] = key === 'dateRange' ? [] : ''
  })
  currentPage.value = 1
  loadData()
}

const handleSelectionChange = (rows: any[]) => {
  selectedRows.value = rows
}

const viewDetail = (row: any) => {
  currentRefund.value = row
  drawerVisible.value = true
  activeTab.value = 'refund'
  generateLogs(row)
}

const generateLogs = (row: any) => {
  const logs: any[] = [
    { time: row.applyTime, title: '提交退款申请', type: 'primary' }
  ]
  if (row.statusKey === 'processing') {
    logs.push({ time: row.applyTime, title: '审核通过，发起退款', type: 'success' })
  }
  if (row.statusKey === 'success') {
    logs.push({ time: row.applyTime, title: '审核通过，发起退款', type: 'success' })
    logs.push({ time: row.finishTime, title: '退款成功', desc: '渠道返回退款成功', type: 'success' })
  }
  if (row.statusKey === 'failed') {
    logs.push({ time: row.applyTime, title: '审核通过，发起退款', type: 'success' })
    logs.push({ time: row.finishTime, title: '退款失败', desc: row.refundReason, type: 'danger' })
  }
  operationLogs.value = logs
}

const handleApprove = (row: any) => {
  ElMessageBox.confirm('确定要同意该退款申请吗？同意后将立即发起退款。', '确认同意', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    ElMessage.success('已同意退款申请，正在发起退款')
    if (drawerVisible.value) {
      drawerVisible.value = false
    }
    loadData()
  }).catch(() => {})
}

const handleReject = (row: any) => {
  currentRefund.value = row
  rejectForm.reason = ''
  rejectDialogVisible.value = true
}

const confirmReject = () => {
  if (!rejectForm.reason.trim()) {
    ElMessage.warning('请输入拒绝原因')
    return
  }
  ElMessage.success('已拒绝退款申请')
  rejectDialogVisible.value = false
  if (drawerVisible.value) {
    drawerVisible.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
.refund-page {
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

.stats-bar {
  padding: 20px;
  margin-bottom: 16px;
  display: flex;
  gap: 24px;
}

.stat-item {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: var(--bg-page);
  border-radius: var(--radius-lg);

  .stat-icon {
    width: 56px;
    height: 56px;
    border-radius: var(--radius-md);
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }

  .stat-content {
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

.is-rotate {
  transform: rotate(180deg);
  transition: transform 0.3s;
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

.refund-detail {
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
  &.pending, &.processing { background: linear-gradient(135deg, #FF7D00, #FAAD14); }
  &.rejected { background: linear-gradient(135deg, #86909C, #C9CDD4); }

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

.log-content {
  .log-title {
    display: block;
    font-weight: 500;
    color: var(--text-primary);
    margin-bottom: 4px;
  }

  .log-operator {
    display: block;
    font-size: 12px;
    color: var(--text-secondary);
    margin-bottom: 2px;
  }

  .log-desc {
    display: block;
    font-size: 12px;
    color: var(--text-secondary);
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
