<template>
  <div class="page-container reconciliation-report-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">对账报表分析</h2>
        <p class="page-desc">全面监控交易对账数据，多维度分析通道对账情况</p>
      </div>
      <div class="header-actions">
        <div class="date-filter">
          <el-radio-group v-model="dateShortcut" size="default" @change="handleShortcutChange">
            <el-radio-button label="today">今日</el-radio-button>
            <el-radio-button label="week">本周</el-radio-button>
            <el-radio-button label="month">本月</el-radio-button>
          </el-radio-group>
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            size="default"
            style="width: 280px"
            :shortcuts="dateShortcuts"
            value-format="YYYY-MM-DD"
            @change="handleDateRangeChange"
          />
          <el-select v-model="filterChannel" placeholder="全部通道" clearable style="width: 140px" @change="loadReports">
            <el-option label="微信支付" value="WECHAT" />
            <el-option label="支付宝" value="ALIPAY" />
            <el-option label="银联支付" value="UNIONPAY" />
          </el-select>
        </div>
        <el-button type="primary" @click="handleExport">
          <el-icon><Download /></el-icon>
          导出报表
        </el-button>
      </div>
    </div>

    <el-row :gutter="20" class="metric-cards">
      <el-col :span="6" v-for="(metric, index) in metrics" :key="index">
        <div class="card-shadow metric-card">
          <div class="metric-header">
            <span class="metric-label">{{ metric.label }}</span>
            <div class="metric-icon" :style="{ background: metric.iconBg }">
              <el-icon :size="20" :color="metric.iconColor"><component :is="metric.icon" /></el-icon>
            </div>
          </div>
          <div class="metric-value">{{ metric.value }}</div>
        </div>
      </el-col>
    </el-row>

    <div class="card-shadow table-card" style="margin-bottom: 20px;">
      <div class="table-header">
        <div class="table-title">
          <el-icon :size="18" color="var(--primary-color)"><DataAnalysis /></el-icon>
          对账报表列表
        </div>
      </div>
      <el-table
        :data="tableData"
        style="width: 100%"
        stripe
        v-loading="loading"
      >
        <el-table-column prop="reportName" label="报表名称" min-width="180">
          <template #default="{ row }">
            <span class="link-text">{{ row.reportName }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="channelCode" label="支付通道" width="120">
          <template #default="{ row }">
            <el-tag size="small" effect="plain" :color="getChannelColor(row.channelCode)" style="color: #fff; border: none">
              {{ getChannelName(row.channelCode) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="统计周期" min-width="200">
          <template #default="{ row }">
            {{ row.startDate }} ~ {{ row.endDate }}
          </template>
        </el-table-column>
        <el-table-column prop="totalCount" label="交易笔数" width="110" align="right">
          <template #default="{ row }">
            <span class="num-text">{{ (row.totalCount ?? 0).toLocaleString() }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="totalAmount" label="交易金额" width="140" align="right">
          <template #default="{ row }">
            <span class="amount-text">¥{{ formatAmount(row.totalAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="diffCount" label="差错笔数" width="110" align="center">
          <template #default="{ row }">
            <span :class="{ 'error-count': (row.diffCount ?? 0) > 0 }">{{ row.diffCount ?? 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="生成时间" width="170">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="downloadReport(row)">
              <el-icon><Download /></el-icon>下载
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
          @current-change="loadReports"
          @size-change="loadReports"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Download, DataAnalysis, Wallet, CreditCard, Coin, Money
} from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import { reconApi } from '@/api/reconciliation'
import type { ReconReport } from '@/types/reconciliation'

const loading = ref(false)
const dateRange = ref<[string, string]>([dayjs().format('YYYY-MM-DD'), dayjs().format('YYYY-MM-DD')])
const dateShortcut = ref('today')
const filterChannel = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const tableData = ref<ReconReport[]>([])

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

const dateShortcuts = [
  {
    text: '今日',
    value: () => [dayjs().format('YYYY-MM-DD'), dayjs().format('YYYY-MM-DD')]
  },
  {
    text: '昨日',
    value: () => [dayjs().subtract(1, 'day').format('YYYY-MM-DD'), dayjs().subtract(1, 'day').format('YYYY-MM-DD')]
  },
  {
    text: '近7天',
    value: () => [dayjs().subtract(6, 'day').format('YYYY-MM-DD'), dayjs().format('YYYY-MM-DD')]
  },
  {
    text: '近30天',
    value: () => [dayjs().subtract(29, 'day').format('YYYY-MM-DD'), dayjs().format('YYYY-MM-DD')]
  },
  {
    text: '本月',
    value: () => [dayjs().startOf('month').format('YYYY-MM-DD'), dayjs().endOf('month').format('YYYY-MM-DD')]
  },
  {
    text: '上月',
    value: () => [dayjs().subtract(1, 'month').startOf('month').format('YYYY-MM-DD'), dayjs().subtract(1, 'month').endOf('month').format('YYYY-MM-DD')]
  }
]

const metrics = ref([
  {
    label: '总对账笔数',
    value: '--',
    icon: CreditCard,
    iconBg: 'var(--primary-bg)',
    iconColor: 'var(--primary-color)'
  },
  {
    label: '总对账金额',
    value: '--',
    icon: Wallet,
    iconBg: 'var(--success-bg)',
    iconColor: 'var(--success-color)'
  },
  {
    label: '总手续费',
    value: '--',
    icon: Coin,
    iconBg: 'var(--warning-bg)',
    iconColor: 'var(--warning-color)'
  },
  {
    label: '差错笔数',
    value: '--',
    icon: Money,
    iconBg: 'var(--danger-bg)',
    iconColor: 'var(--danger-color)'
  }
])

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

function updateMetrics(reports: ReconReport[]) {
  let totalCount = 0
  let totalAmount = 0
  let totalDiff = 0
  reports.forEach(r => {
    totalCount += r.totalCount ?? 0
    totalAmount += r.totalAmount ?? 0
    totalDiff += r.diffCount ?? 0
  })
  metrics.value[0].value = totalCount.toLocaleString()
  metrics.value[1].value = '¥' + formatAmount(totalAmount)
  metrics.value[2].value = '¥0.00'
  metrics.value[3].value = totalDiff.toLocaleString()
}

async function loadReports() {
  loading.value = true
  try {
    const params: any = {
      page: currentPage.value,
      pageSize: pageSize.value
    }
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }
    if (filterChannel.value) {
      params.channelCode = filterChannel.value
    }
    const res = await reconApi.getReports(params)
    if (res) {
      tableData.value = res.list ?? []
      total.value = res.total ?? 0
      updateMetrics(tableData.value)
    }
  } catch (e) {
    console.error('Failed to load reports:', e)
    tableData.value = []
    total.value = 0
    ElMessage.error('加载报表失败')
  } finally {
    loading.value = false
  }
}

const handleShortcutChange = (val: string) => {
  const now = dayjs()
  let start: dayjs.Dayjs, end: dayjs.Dayjs
  switch (val) {
    case 'today':
      start = now.startOf('day')
      end = now.endOf('day')
      break
    case 'week':
      start = now.startOf('week')
      end = now.endOf('week')
      break
    case 'month':
      start = now.startOf('month')
      end = now.endOf('month')
      break
    default:
      start = now.startOf('day')
      end = now.endOf('day')
  }
  dateRange.value = [start.format('YYYY-MM-DD'), end.format('YYYY-MM-DD')]
  currentPage.value = 1
  loadReports()
}

const handleDateRangeChange = () => {
  dateShortcut.value = ''
  currentPage.value = 1
  loadReports()
}

const handleExport = () => {
  ElMessage.success('正在导出报表...')
}

const downloadReport = (row: ReconReport) => {
  ElMessage.success(`正在下载报表：${row.reportName}`)
}

onMounted(() => {
  loadReports()
})
</script>

<style lang="scss" scoped>
.reconciliation-report-page {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 20px;
  }

  .page-title {
    font-size: 22px;
    font-weight: 600;
    color: var(--text-primary);
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
    flex-wrap: wrap;
    justify-content: flex-end;
  }

  .date-filter {
    display: flex;
    gap: 12px;
    align-items: center;
    flex-wrap: wrap;
  }
}

.metric-cards {
  margin-bottom: 20px;
}

.metric-card {
  .metric-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 12px;
  }

  .metric-label {
    font-size: 14px;
    color: var(--text-secondary);
  }

  .metric-icon {
    width: 40px;
    height: 40px;
    border-radius: var(--radius-md);
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .metric-value {
    font-size: 26px;
    font-weight: 700;
    color: var(--text-primary);
    letter-spacing: -0.5px;
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

.table-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.link-text {
  color: var(--primary-color);
  cursor: pointer;

  &:hover {
    text-decoration: underline;
  }
}

.num-text {
  font-family: 'SF Mono', Monaco, 'Courier New', monospace;
  color: var(--text-regular);
}

.amount-text {
  font-weight: 600;
  color: var(--text-primary);
  font-family: 'SF Mono', Monaco, 'Courier New', monospace;
}

.error-count {
  color: var(--danger-color);
  font-weight: 600;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
