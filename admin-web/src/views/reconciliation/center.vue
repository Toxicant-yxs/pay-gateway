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
        <el-form-item label="任务号">
          <el-input v-model="filterForm.taskNo" placeholder="请输入对账任务号" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="对账日期">
          <el-date-picker
            v-model="filterForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            style="width: 260px"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="支付通道">
          <el-select v-model="filterForm.channelCode" placeholder="全部通道" clearable style="width: 140px">
            <el-option label="微信支付" value="WECHAT" />
            <el-option label="支付宝" value="ALIPAY" />
            <el-option label="银联支付" value="UNIONPAY" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filterForm.status" placeholder="全部状态" clearable style="width: 140px">
            <el-option label="待执行" :value="0" />
            <el-option label="执行中" :value="1" />
            <el-option label="成功" :value="2" />
            <el-option label="失败" :value="3" />
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
        <el-table-column prop="taskNo" label="任务号" min-width="180" fixed="left">
          <template #default="{ row }">
            <span class="link-text mono-text" @click="viewDetail(row)">{{ row.taskNo }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="reconDate" label="对账日期" width="110" />
        <el-table-column prop="channelCode" label="支付通道" width="110">
          <template #default="{ row }">
            <el-tag size="small" effect="plain" :color="getChannelColor(row.channelCode)" style="color: #fff; border: none">
              {{ getChannelName(row.channelCode) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="平台交易" min-width="160" align="right">
          <template #default="{ row }">
            <div>{{ row.totalCount }}笔</div>
            <div class="amount-text">¥{{ formatAmount(row.totalAmount) }}</div>
          </template>
        </el-table-column>
        <el-table-column label="成功交易" min-width="160" align="right">
          <template #default="{ row }">
            <div>{{ row.successCount }}笔</div>
            <div class="amount-text">¥{{ formatAmount(row.successAmount) }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="diffCount" label="差错笔数" width="100" align="center">
          <template #default="{ row }">
            <span :class="{ 'error-count': row.diffCount > 0 }">{{ row.diffCount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="endTime" label="完成时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.endTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="viewDetail(row)">
              <el-icon><View /></el-icon>详情
            </el-button>
            <el-button type="primary" link size="small" @click="downloadBill(row)">
              <el-icon><Download /></el-icon>对账单
            </el-button>
            <el-button type="warning" link size="small" v-if="row.status === 3" @click="retryTask(row)">
              <el-icon><RefreshRight /></el-icon>重试
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
          @current-change="loadData"
          @size-change="loadData"
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
      <div v-if="currentTask" class="recon-detail" v-loading="detailLoading">
        <div class="detail-header-card">
          <div class="batch-info">
            <div class="batch-no">
              <span class="label">任务号</span>
              <span class="mono-text value">{{ currentTask.taskNo }}</span>
            </div>
            <el-tag :type="getStatusType(currentTask.status)" size="small">
              {{ getStatusText(currentTask.status) }}
            </el-tag>
          </div>
          <el-descriptions :column="3" border class="info-descriptions">
            <el-descriptions-item label="对账日期">{{ currentTask.reconDate }}</el-descriptions-item>
            <el-descriptions-item label="支付通道">{{ getChannelName(currentTask.channelCode) }}</el-descriptions-item>
            <el-descriptions-item label="完成时间">{{ formatDateTime(currentTask.endTime) }}</el-descriptions-item>
            <el-descriptions-item label="总笔数">{{ currentTask.totalCount }}笔</el-descriptions-item>
            <el-descriptions-item label="成功笔数">{{ currentTask.successCount }}笔</el-descriptions-item>
            <el-descriptions-item label="差错笔数">{{ currentTask.diffCount }}笔</el-descriptions-item>
            <el-descriptions-item label="总金额">
              <span class="amount-text">¥{{ formatAmount(currentTask.totalAmount) }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="成功金额">
              <span class="amount-text">¥{{ formatAmount(currentTask.successAmount) }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="差异金额">
              <span class="amount-text diff-amount">¥{{ formatAmount(Math.abs(currentTask.diffAmount)) }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="开始时间" :span="1">{{ formatDateTime(currentTask.startTime) }}</el-descriptions-item>
            <el-descriptions-item label="创建时间" :span="2">{{ formatDateTime(currentTask.createdAt) }}</el-descriptions-item>
            <el-descriptions-item v-if="currentTask.remark" label="备注" :span="3">{{ currentTask.remark }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="error-section" v-if="currentTask.diffCount > 0">
          <div class="section-title">
            <el-icon><Warning /></el-icon>
            差错明细
            <el-tag size="small" type="warning">{{ currentTask.diffCount }}条</el-tag>
          </div>
          <el-table :data="diffData" style="width: 100%" stripe size="small" v-loading="diffLoading">
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
            <el-table-column prop="channelOrderNo" label="渠道订单号" min-width="180">
              <template #default="{ row }">
                <span class="mono-text">{{ row.channelOrderNo || '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="orderAmount" label="平台金额" width="120" align="right">
              <template #default="{ row }">
                <span class="amount-text">¥{{ formatAmount(row.orderAmount) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="channelAmount" label="通道金额" width="120" align="right">
              <template #default="{ row }">
                <span class="amount-text">¥{{ formatAmount(row.channelAmount) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="diffAmount" label="差异金额" width="120" align="right">
              <template #default="{ row }">
                <span class="amount-text diff-amount">¥{{ formatAmount(Math.abs(row.diffAmount)) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="处理状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="getProcessStatusType(row.status)" size="small">
                  {{ getProcessStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100" fixed="right">
              <template #default="{ row }">
                <el-button
                  type="primary"
                  link
                  size="small"
                  :disabled="row.status === 1"
                  @click="openErrorDialog(row)"
                >
                  处理
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination-wrapper" v-if="diffTotal > pageSize">
            <el-pagination
              v-model:current-page="diffPage"
              v-model:page-size="diffPageSize"
              :page-sizes="[10, 20, 50]"
              :total="diffTotal"
              layout="total, prev, pager, next"
              background
              small
              @current-change="loadDiffs"
              @size-change="loadDiffs"
            />
          </div>
        </div>
      </div>
    </el-drawer>

    <el-dialog v-model="errorDialogVisible" title="差错处理" width="560px" destroy-on-close>
      <div v-if="currentDiff" class="error-dialog-content">
        <el-descriptions :column="2" border class="error-info">
          <el-descriptions-item label="差异类型">
            <el-tag :type="getDiffType(currentDiff.diffType)" size="small">
              {{ getDiffTypeText(currentDiff.diffType) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="订单号">
            <span class="mono-text">{{ currentDiff.orderNo }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="平台金额">
            <span class="amount-text">¥{{ formatAmount(currentDiff.orderAmount) }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="通道金额">
            <span class="amount-text">¥{{ formatAmount(currentDiff.channelAmount) }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="差异金额" :span="2">
            <span class="amount-text diff-amount">¥{{ formatAmount(Math.abs(currentDiff.diffAmount)) }}</span>
          </el-descriptions-item>
        </el-descriptions>
        <el-form :model="errorForm" label-width="100px" style="margin-top: 20px">
          <el-form-item label="处理备注">
            <el-input
              v-model="errorForm.note"
              type="textarea"
              :rows="3"
              placeholder="请输入处理备注说明"
            />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="errorDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitErrorHandle" :loading="handleLoading">确认处理</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Document, DataAnalysis, Warning, Download, Search, Refresh, View, CircleClose, Money, RefreshRight } from '@element-plus/icons-vue'
import { reconApi } from '@/api/reconciliation'
import type { ReconTask, ReconDetail, ReconStats } from '@/types/reconciliation'

const loading = ref(false)
const detailLoading = ref(false)
const diffLoading = ref(false)
const handleLoading = ref(false)
const drawerVisible = ref(false)
const errorDialogVisible = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const selectedRows = ref<ReconTask[]>([])
const currentTask = ref<ReconTask | null>(null)
const diffData = ref<ReconDetail[]>([])
const diffPage = ref(1)
const diffPageSize = ref(10)
const diffTotal = ref(0)
const currentDiff = ref<ReconDetail | null>(null)

const filterForm = reactive({
  taskNo: '',
  dateRange: [] as string[],
  channelCode: '',
  status: '' as number | string
})

const errorForm = reactive({
  note: ''
})

const channelNameMap: Record<string, string> = {
  WECHAT: '微信支付',
  ALIPAY: '支付宝',
  UNIONPAY: '银联支付'
}

const channelColors: Record<string, string> = {
  WECHAT: '#07C160',
  ALIPAY: '#1677FF',
  UNIONPAY: '#E60012'
}

const statsData = ref([
  { label: '今日对账笔数', value: '--', type: '', icon: Document, bgColor: 'var(--primary-bg)', iconColor: 'var(--primary-color)' },
  { label: '今日对账金额', value: '--', type: 'success', icon: Money, bgColor: 'var(--success-bg)', iconColor: 'var(--success-color)' },
  { label: '差错笔数', value: '--', type: 'warning', icon: Warning, bgColor: 'var(--warning-bg)', iconColor: 'var(--warning-color)' },
  { label: '待处理差错', value: '--', type: 'danger', icon: CircleClose, bgColor: 'var(--danger-bg)', iconColor: 'var(--danger-color)' }
])

const tableData = ref<ReconTask[]>([])

function padZero(n: number): string {
  return n < 10 ? '0' + n : '' + n
}

function formatDateTime(dateStr?: string): string {
  if (!dateStr) return '-'
  try {
    const date = new Date(dateStr)
    if (isNaN(date.getTime())) return dateStr
    return date.getFullYear() + '-' +
      padZero(date.getMonth() + 1) + '-' +
      padZero(date.getDate()) + ' ' +
      padZero(date.getHours()) + ':' +
      padZero(date.getMinutes()) + ':' +
      padZero(date.getSeconds())
  } catch {
    return dateStr
  }
}

function formatAmount(amount: number | undefined | null): string {
  if (amount === null || amount === undefined || isNaN(amount as number)) return '0.00'
  return (amount / 100).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function getChannelName(code: string): string {
  if (!code) return '-'
  return channelNameMap[code] ?? code
}

function getChannelColor(channel: string): string {
  if (!channel) return 'var(--primary-color)'
  return channelColors[channel] ?? 'var(--primary-color)'
}

const reconStatusMap: Record<number, { text: string; type: string }> = {
  0: { text: '待执行', type: 'info' },
  1: { text: '执行中', type: 'warning' },
  2: { text: '成功', type: 'success' },
  3: { text: '失败', type: 'danger' }
}

function getStatusType(status: number) {
  return reconStatusMap[status]?.type ?? 'info'
}

function getStatusText(status: number) {
  return reconStatusMap[status]?.text ?? '未知'
}

const diffTypeMap: Record<string, { text: string; type: string }> = {
  SHORT: { text: '短款', type: 'warning' },
  EXTRA: { text: '长款', type: 'danger' },
  MISMATCH: { text: '金额不一致', type: 'danger' }
}

function getDiffType(type: string) {
  return diffTypeMap[type]?.type ?? 'info'
}

function getDiffTypeText(type: string) {
  return diffTypeMap[type]?.text ?? type
}

function getProcessStatusType(status: number) {
  if (status === 1) return 'success'
  return 'warning'
}

function getProcessStatusText(status: number) {
  if (status === 1) return '已处理'
  return '待处理'
}

async function loadStats() {
  try {
    const res = await reconApi.getStats()
    if (res) {
      statsData.value[0].value = (res.todayCount ?? 0).toLocaleString()
      statsData.value[1].value = '¥' + formatAmount(res.diffAmount ?? 0)
      statsData.value[2].value = (res.diffCount ?? 0).toLocaleString()
      statsData.value[3].value = (res.pendingDiffCount ?? 0).toLocaleString()
    }
  } catch (e) {
    console.error('Failed to load stats:', e)
  }
}

async function loadData() {
  loading.value = true
  try {
    const params: any = {
      page: currentPage.value,
      pageSize: pageSize.value
    }
    if (filterForm.taskNo) params.taskNo = filterForm.taskNo
    if (filterForm.channelCode) params.channelCode = filterForm.channelCode
    if (filterForm.status !== '' && filterForm.status !== undefined && filterForm.status !== null) {
      params.status = filterForm.status
    }
    if (filterForm.dateRange && filterForm.dateRange.length === 2) {
      params.startDate = filterForm.dateRange[0]
      params.endDate = filterForm.dateRange[1]
    }
    const res = await reconApi.getTasks(params)
    if (res) {
      tableData.value = res.list ?? []
      total.value = res.total ?? 0
    }
  } catch (e) {
    console.error('Failed to load recon tasks:', e)
    tableData.value = []
    total.value = 0
    ElMessage.error('加载对账任务失败')
  } finally {
    loading.value = false
  }
}

async function loadDiffs() {
  if (!currentTask.value) return
  diffLoading.value = true
  try {
    const res = await reconApi.getTaskDiffs(currentTask.value.id, {
      page: diffPage.value,
      pageSize: diffPageSize.value
    })
    if (res) {
      diffData.value = res.list ?? []
      diffTotal.value = res.total ?? 0
    }
  } catch (e) {
    console.error('Failed to load diffs:', e)
    diffData.value = []
    diffTotal.value = 0
  } finally {
    diffLoading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadData()
}

const handleReset = () => {
  filterForm.taskNo = ''
  filterForm.dateRange = []
  filterForm.channelCode = ''
  filterForm.status = ''
  currentPage.value = 1
  loadData()
}

const handleRefresh = () => {
  loadData()
  loadStats()
  ElMessage.success('数据已刷新')
}

const handleSelectionChange = (rows: ReconTask[]) => {
  selectedRows.value = rows
}

const viewDetail = async (row: ReconTask) => {
  currentTask.value = row
  diffPage.value = 1
  drawerVisible.value = true
  if (row.diffCount > 0) {
    await loadDiffs()
  } else {
    diffData.value = []
    diffTotal.value = 0
  }
}

const downloadBill = (row: ReconTask) => {
  ElMessage.success(`正在下载对账单：${row.taskNo}`)
}

const retryTask = async (row: ReconTask) => {
  try {
    await reconApi.retryTask(row.id)
    ElMessage.success('任务已重新提交')
    loadData()
  } catch (e) {
    console.error('Failed to retry task:', e)
    ElMessage.error('重试失败')
  }
}

const openErrorDialog = (diff: ReconDetail) => {
  currentDiff.value = diff
  errorForm.note = ''
  errorDialogVisible.value = true
}

const submitErrorHandle = async () => {
  if (!currentDiff.value) return
  handleLoading.value = true
  try {
    await reconApi.handleDiff(currentDiff.value.id, {
      action: 'CONFIRM',
      note: errorForm.note
    })
    ElMessage.success('差错处理成功')
    errorDialogVisible.value = false
    await loadDiffs()
    await loadStats()
  } catch (e) {
    console.error('Failed to handle diff:', e)
    ElMessage.error('处理失败')
  } finally {
    handleLoading.value = false
  }
}

onMounted(() => {
  loadData()
  loadStats()
})
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
