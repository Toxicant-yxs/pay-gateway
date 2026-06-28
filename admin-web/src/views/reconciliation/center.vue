<template>
  <div class="page-container reconciliation-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">对账管理</h2>
        <p class="page-desc">管理支付交易对账与差错处理</p>
      </div>
      <div class="header-actions">
        <el-button @click="handleRefresh">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
        <el-button type="primary">
          <el-icon><DataAnalysis /></el-icon>
          手动对账
        </el-button>
      </div>
    </div>

    <div class="stats-grid">
      <div v-for="stat in statsData" :key="stat.label" class="metric-card" :class="stat.type">
        <div class="stat-icon" :style="{ background: stat.bgColor, color: stat.iconColor }">
          <el-icon :size="24"><component :is="stat.icon" /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-label">{{ stat.label }}</div>
          <div class="stat-value">{{ stat.value }}</div>
        </div>
      </div>
    </div>

    <div class="card-shadow filter-card">
      <el-form :model="filterForm" inline class="filter-form">
        <el-form-item label="批次号">
          <el-input v-model="filterForm.batchNo" placeholder="请输入对账批次号" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="对账日期">
          <el-date-picker
            v-model="filterForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            style="width: 260px"
          />
        </el-form-item>
        <el-form-item label="支付通道">
          <el-select v-model="filterForm.channel" placeholder="全部通道" clearable style="width: 140px">
            <el-option label="微信支付" value="wechat" />
            <el-option label="支付宝" value="alipay" />
            <el-option label="银联支付" value="unionpay" />
            <el-option label="Visa/MC" value="card" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filterForm.status" placeholder="全部状态" clearable style="width: 140px">
            <el-option label="对账中" value="processing" />
            <el-option label="对账成功" value="success" />
            <el-option label="对账失败" value="failed" />
            <el-option label="有差错" value="error" />
          </el-select>
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
        <div class="table-actions">
          <el-button :disabled="selectedRows.length === 0">
            <el-icon><Download /></el-icon>
            批量导出
          </el-button>
        </div>
        <div class="table-info">
          共 <span class="highlight">{{ total }}</span> 条记录
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
        <el-table-column prop="batchNo" label="批次号" min-width="180" fixed="left">
          <template #default="{ row }">
            <span class="link-text mono-text" @click="viewDetail(row)">{{ row.batchNo }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="reconDate" label="对账日期" width="110" />
        <el-table-column prop="channel" label="支付通道" width="110">
          <template #default="{ row }">
            <el-tag size="small" effect="plain" :color="getChannelColor(row.channel)" style="color: #fff; border: none">
              {{ row.channel }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="平台交易" min-width="160" align="right">
          <template #default="{ row }">
            <div>{{ row.platformCount }}笔</div>
            <div class="amount-text">¥{{ formatNumber(row.platformAmount) }}</div>
          </template>
        </el-table-column>
        <el-table-column label="通道交易" min-width="160" align="right">
          <template #default="{ row }">
            <div>{{ row.channelCount }}笔</div>
            <div class="amount-text">¥{{ formatNumber(row.channelAmount) }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="errorCount" label="差错笔数" width="100" align="center">
          <template #default="{ row }">
            <span :class="{ 'error-count': row.errorCount > 0 }">{{ row.errorCount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="对账状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="completeTime" label="完成时间" width="160" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="viewDetail(row)">
              <el-icon><View /></el-icon>详情
            </el-button>
            <el-button type="primary" link size="small" @click="downloadBill(row)">
              <el-icon><Download /></el-icon>对账单
            </el-button>
            <el-button type="warning" link size="small" v-if="row.errorCount > 0" @click="handleProcessError(row)">
              <el-icon><Warning /></el-icon>差错处理
            </el-button>
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
      title="对账详情"
      direction="rtl"
      size="800px"
      :destroy-on-close="true"
    >
      <div v-if="currentBatch" class="recon-detail">
        <div class="detail-header-card">
          <div class="batch-info">
            <div class="batch-no">
              <span class="label">批次号</span>
              <span class="mono-text value">{{ currentBatch.batchNo }}</span>
            </div>
            <el-tag :type="getStatusType(currentBatch.status)" size="small">
              {{ getStatusText(currentBatch.status) }}
            </el-tag>
          </div>
          <el-descriptions :column="3" border class="info-descriptions">
            <el-descriptions-item label="对账日期">{{ currentBatch.reconDate }}</el-descriptions-item>
            <el-descriptions-item label="支付通道">{{ currentBatch.channel }}</el-descriptions-item>
            <el-descriptions-item label="完成时间">{{ currentBatch.completeTime }}</el-descriptions-item>
            <el-descriptions-item label="平台笔数">{{ currentBatch.platformCount }}笔</el-descriptions-item>
            <el-descriptions-item label="通道笔数">{{ currentBatch.channelCount }}笔</el-descriptions-item>
            <el-descriptions-item label="差错笔数">{{ currentBatch.errorCount }}笔</el-descriptions-item>
            <el-descriptions-item label="平台金额">
              <span class="amount-text">¥{{ formatNumber(currentBatch.platformAmount) }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="通道金额">
              <span class="amount-text">¥{{ formatNumber(currentBatch.channelAmount) }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="差异金额">
              <span class="amount-text diff-amount">¥{{ formatNumber(Math.abs(currentBatch.platformAmount - currentBatch.channelAmount)) }}</span>
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="error-section">
          <div class="section-title">
            <el-icon><Warning /></el-icon>
            差错明细
            <el-tag size="small" type="warning" v-if="currentBatch.errorCount > 0">{{ currentBatch.errorCount }}条</el-tag>
          </div>
          <el-table :data="currentErrors" style="width: 100%" stripe size="small">
            <el-table-column prop="diffType" label="差异类型" width="110" align="center">
              <template #default="{ row }">
                <el-tag :type="getDiffType(row.diffType)" size="small">
                  {{ getDiffTypeText(row.diffType) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="orderNo" label="订单号" min-width="180">
              <template #default="{ row }">
                <span class="mono-text">{{ row.orderNo }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="platformAmount" label="平台金额" width="120" align="right">
              <template #default="{ row }">
                <span class="amount-text">¥{{ formatNumber(row.platformAmount) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="channelAmount" label="通道金额" width="120" align="right">
              <template #default="{ row }">
                <span class="amount-text">¥{{ formatNumber(row.channelAmount) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="diffAmount" label="差异金额" width="120" align="right">
              <template #default="{ row }">
                <span class="amount-text diff-amount">¥{{ formatNumber(Math.abs(row.diffAmount)) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="processStatus" label="处理状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="getProcessStatusType(row.processStatus)" size="small">
                  {{ getProcessStatusText(row.processStatus) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100" fixed="right">
              <template #default="{ row }">
                <el-button
                  type="primary"
                  link
                  size="small"
                  :disabled="row.processStatus !== 'pending'"
                  @click="openErrorDialog(row)"
                >
                  处理
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </el-drawer>

    <el-dialog v-model="errorDialogVisible" title="差错处理" width="560px" destroy-on-close>
      <div v-if="currentError" class="error-dialog-content">
        <el-descriptions :column="2" border class="error-info">
          <el-descriptions-item label="差异类型">
            <el-tag :type="getDiffType(currentError.diffType)" size="small">
              {{ getDiffTypeText(currentError.diffType) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="订单号">
            <span class="mono-text">{{ currentError.orderNo }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="平台金额">
            <span class="amount-text">¥{{ formatNumber(currentError.platformAmount) }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="通道金额">
            <span class="amount-text">¥{{ formatNumber(currentError.channelAmount) }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="差异金额" :span="2">
            <span class="amount-text diff-amount">¥{{ formatNumber(Math.abs(currentError.diffAmount)) }}</span>
          </el-descriptions-item>
        </el-descriptions>
        <el-form :model="errorForm" label-width="100px" style="margin-top: 20px">
          <el-form-item label="处理方式" required>
            <el-radio-group v-model="errorForm.handleType">
              <el-radio value="confirm">确认差异</el-radio>
              <el-radio value="supplement" v-if="currentError.diffType === 'short'">补单</el-radio>
              <el-radio value="refund" v-if="currentError.diffType === 'long' || currentError.diffType === 'amount'">退款</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="处理备注">
            <el-input
              v-model="errorForm.remark"
              type="textarea"
              :rows="3"
              placeholder="请输入处理备注说明"
            />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="errorDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitErrorHandle">确认处理</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { Document, DataAnalysis, Warning, Download, Search, Refresh, View, CircleClose, Money } from '@element-plus/icons-vue'

const loading = ref(false)
const drawerVisible = ref(false)
const errorDialogVisible = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(8)
const selectedRows = ref<any[]>([])
const currentBatch = ref<any>(null)
const currentErrors = ref<any[]>([])
const currentError = ref<any>(null)

const filterForm = reactive({
  batchNo: '',
  dateRange: [],
  channel: '',
  status: ''
})

const errorForm = reactive({
  handleType: 'confirm',
  remark: ''
})

const statsData = [
  { label: '今日对账笔数', value: '12,856', type: '', icon: Document, bgColor: 'var(--primary-bg)', iconColor: 'var(--primary-color)' },
  { label: '今日对账金额', value: '¥2,845,632', type: 'success', icon: Money, bgColor: 'var(--success-bg)', iconColor: 'var(--success-color)' },
  { label: '差错笔数', value: '23', type: 'warning', icon: Warning, bgColor: 'var(--warning-bg)', iconColor: 'var(--warning-color)' },
  { label: '待处理差错', value: '8', type: 'danger', icon: CircleClose, bgColor: 'var(--danger-bg)', iconColor: 'var(--danger-color)' }
]

const generateErrors = (batchId: number, count: number) => {
  const types = ['long', 'short', 'amount']
  const statuses = ['pending', 'confirmed', 'supplement', 'refunded']
  const errors = []
  for (let i = 0; i < count; i++) {
    const type = types[i % 3]
    const platformBase = 10000 + Math.floor(Math.random() * 50000)
    let channelAmt = platformBase
    let diffAmt = 0
    if (type === 'long') {
      diffAmt = Math.floor(Math.random() * 5000) + 1000
      channelAmt = platformBase + diffAmt
    } else if (type === 'short') {
      diffAmt = -(Math.floor(Math.random() * 5000) + 1000)
      channelAmt = platformBase + diffAmt
    } else {
      diffAmt = Math.floor(Math.random() * 2000) - 1000
      channelAmt = platformBase + diffAmt
    }
    errors.push({
      id: `${batchId}-${i}`,
      diffType: type,
      orderNo: `PAY202606${28 - batchId}${String(1000 + i).padStart(4, '0')}`,
      platformAmount: platformBase,
      channelAmount: channelAmt,
      diffAmount: diffAmt,
      processStatus: i === 0 ? 'pending' : statuses[(i + 1) % 4]
    })
  }
  return errors
}

const tableData = ref([
  { id: 1, batchNo: 'RC202606280001', reconDate: '2026-06-28', channel: '微信支付', platformCount: 3256, platformAmount: 85623400, channelCount: 3256, channelAmount: 85623400, errorCount: 0, status: 'success', completeTime: '2026-06-28 23:55:32' },
  { id: 2, batchNo: 'RC202606280002', reconDate: '2026-06-28', channel: '支付宝', platformCount: 4521, platformAmount: 124568900, channelCount: 4518, channelAmount: 124235600, errorCount: 3, status: 'error', completeTime: '2026-06-28 23:58:15' },
  { id: 3, batchNo: 'RC202606280003', reconDate: '2026-06-28', channel: '银联支付', platformCount: 1856, platformAmount: 56892300, channelCount: 1856, channelAmount: 56892300, errorCount: 0, status: 'success', completeTime: '2026-06-28 23:45:08' },
  { id: 4, batchNo: 'RC202606270001', reconDate: '2026-06-27', channel: '微信支付', platformCount: 3892, platformAmount: 98452100, channelCount: 3890, channelAmount: 98125600, errorCount: 2, status: 'error', completeTime: '2026-06-27 23:52:44' },
  { id: 5, batchNo: 'RC202606270002', reconDate: '2026-06-27', channel: '支付宝', platformCount: 5123, platformAmount: 145236700, channelCount: 0, channelAmount: 0, errorCount: 5, status: 'failed', completeTime: '2026-06-27 23:59:59' },
  { id: 6, batchNo: 'RC202606270003', reconDate: '2026-06-27', channel: 'Visa/MC', platformCount: 425, platformAmount: 23568900, channelCount: 425, channelAmount: 23568900, errorCount: 0, status: 'success', completeTime: '2026-06-27 23:40:22' },
  { id: 7, batchNo: 'RC202606260001', reconDate: '2026-06-26', channel: '微信支付', platformCount: 2985, platformAmount: 78956200, channelCount: 2984, channelAmount: 78825600, errorCount: 1, status: 'processing', completeTime: '-' },
  { id: 8, batchNo: 'RC202606260002', reconDate: '2026-06-26', channel: '支付宝', platformCount: 4215, platformAmount: 112568300, channelCount: 4215, channelAmount: 112568300, errorCount: 0, status: 'success', completeTime: '2026-06-26 23:50:18' }
])

tableData.value.forEach((item: any) => {
  if (item.errorCount > 0) {
    item.errors = generateErrors(item.id, item.errorCount)
  } else {
    item.errors = []
  }
})

const formatNumber = (num: number) => (num / 100).toLocaleString('zh-CN', { minimumFractionDigits: 2 })

const getChannelColor = (channel: string) => {
  const map: Record<string, string> = { '微信支付': '#07C160', '支付宝': '#1677FF', '银联支付': '#E60012', 'Visa/MC': '#1A1F71' }
  return map[channel] || 'var(--primary-color)'
}

const getStatusType = (status: string) => {
  const map: Record<string, string> = { success: 'success', processing: 'warning', failed: 'danger', error: 'warning' }
  return map[status] || 'info'
}

const getStatusText = (status: string) => {
  const map: Record<string, string> = { success: '对账成功', processing: '对账中', failed: '对账失败', error: '有差错' }
  return map[status] || '未知'
}

const getDiffType = (type: string) => {
  const map: Record<string, string> = { long: 'danger', short: 'warning', amount: '' }
  return map[type] || 'info'
}

const getDiffTypeText = (type: string) => {
  const map: Record<string, string> = { long: '长款', short: '短款', amount: '金额不一致' }
  return map[type] || '未知'
}

const getProcessStatusType = (status: string) => {
  const map: Record<string, string> = { pending: 'warning', confirmed: 'success', supplement: '', refunded: 'info' }
  return map[status] || 'info'
}

const getProcessStatusText = (status: string) => {
  const map: Record<string, string> = { pending: '待处理', confirmed: '已确认', supplement: '已补单', refunded: '已退款' }
  return map[status] || '未知'
}

const handleSearch = () => {
  loading.value = true
  setTimeout(() => loading.value = false, 500)
}

const handleReset = () => {
  filterForm.batchNo = ''
  filterForm.dateRange = []
  filterForm.channel = ''
  filterForm.status = ''
}

const handleRefresh = () => {
  loading.value = true
  setTimeout(() => {
    loading.value = false
    ElMessage.success('数据已刷新')
  }, 500)
}

const handleSelectionChange = (rows: any[]) => {
  selectedRows.value = rows
}

const viewDetail = (row: any) => {
  currentBatch.value = row
  currentErrors.value = row.errors || []
  drawerVisible.value = true
}

const downloadBill = (row: any) => {
  ElMessage.success(`正在下载对账单：${row.batchNo}`)
}

const handleProcessError = (row: any) => {
  currentBatch.value = row
  currentErrors.value = row.errors || []
  drawerVisible.value = true
}

const openErrorDialog = (error: any) => {
  currentError.value = error
  errorForm.handleType = 'confirm'
  errorForm.remark = ''
  errorDialogVisible.value = true
}

const submitErrorHandle = () => {
  if (!errorForm.remark) {
    ElMessage.warning('请输入处理备注')
    return
  }
  ElMessage.success('差错处理成功')
  errorDialogVisible.value = false
  if (currentError.value) {
    currentError.value.processStatus = errorForm.handleType === 'confirm' ? 'confirmed' : errorForm.handleType === 'supplement' ? 'supplement' : 'refunded'
  }
}
</script>

<style lang="scss" scoped>
.reconciliation-page {
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

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}

.metric-card {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-content {
  flex: 1;
}

.stat-label {
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 6px;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
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
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.table-actions {
  display: flex;
  gap: 8px;
}

.table-info {
  font-size: 13px;
  color: var(--text-secondary);

  .highlight {
    color: var(--primary-color);
    font-weight: 600;
  }
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

.error-count {
  color: var(--danger-color);
  font-weight: 600;
}

.diff-amount {
  color: var(--danger-color);
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.recon-detail {
  padding: 20px;
}

.detail-header-card {
  background: var(--bg-hover);
  border-radius: var(--radius-lg);
  padding: 20px;
  margin-bottom: 20px;
}

.batch-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.batch-no {
  display: flex;
  align-items: center;
  gap: 12px;

  .label {
    font-size: 13px;
    color: var(--text-secondary);
  }

  .value {
    font-size: 16px;
    font-weight: 600;
    color: var(--primary-color);
  }
}

.info-descriptions {
  margin-top: 8px;
}

.error-section {
  .section-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 15px;
    font-weight: 600;
    margin-bottom: 16px;
    padding-bottom: 12px;
    border-bottom: 1px solid var(--border-light);
    color: var(--text-primary);

    .el-icon {
      color: var(--warning-color);
    }
  }
}

.error-dialog-content {
  .error-info {
    background: var(--danger-bg);
    border-radius: var(--radius-md);
    padding: 4px;
  }
}
</style>
