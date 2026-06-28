<template>
  <div class="page-container reconciliation-report-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">对账报表分析</h2>
        <p class="page-desc">全面监控交易对账数据，多维度分析通道与商户结算情况</p>
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
            @change="handleDateRangeChange"
          />
        </div>
        <el-button type="primary">
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
          <div class="metric-footer">
            <span class="trend" :class="metric.trend > 0 ? 'up' : 'down'">
              <el-icon><TrendCharts v-if="metric.trend > 0" /><Bottom v-else /></el-icon>
              {{ Math.abs(metric.trend) }}%
            </span>
            <span class="compare-text">较上期{{ metric.trend > 0 ? '增长' : '下降' }}</span>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-section">
      <el-col :span="16">
        <div class="card-shadow chart-card">
          <div class="card-header">
            <div class="card-title">
              <el-icon :size="18" color="var(--primary-color)"><TrendCharts /></el-icon>
              按日交易趋势
            </div>
          </div>
          <div ref="trendChartRef" class="chart-container"></div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="card-shadow chart-card">
          <div class="card-header">
            <div class="card-title">
              <el-icon :size="18" color="var(--purple-color)"><PieChart /></el-icon>
              通道交易占比
            </div>
          </div>
          <div class="pie-chart-wrapper">
            <div ref="pieChartRef" class="pie-chart-container"></div>
            <div class="pie-legend">
              <div v-for="(item, index) in channelDistribution" :key="index" class="legend-item">
                <div class="legend-dot" :style="{ background: item.color }"></div>
                <span class="legend-label">{{ item.name }}</span>
                <span class="legend-value">{{ item.percent }}%</span>
              </div>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="table-section">
      <el-col :span="24">
        <div class="card-shadow chart-card">
          <div class="card-header">
            <div class="card-title">
              <el-icon :size="18" color="var(--primary-color)"><DataAnalysis /></el-icon>
              通道对账汇总
            </div>
          </div>
          <el-table :data="channelSummary" style="width: 100%" stripe>
            <el-table-column prop="channel" label="支付通道" min-width="140">
              <template #default="{ row }">
                <div class="channel-cell">
                  <div class="channel-logo" :style="{ background: row.color }">{{ row.channel.charAt(0) }}</div>
                  <span>{{ row.channel }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="transCount" label="交易笔数" min-width="110" align="right">
              <template #default="{ row }">
                <span class="num-text">{{ formatNumber(row.transCount) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="transAmount" label="交易金额" min-width="130" align="right">
              <template #default="{ row }">
                <span class="amount-text">¥{{ formatNumber(row.transAmount) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="fee" label="手续费" min-width="110" align="right">
              <template #default="{ row }">
                <span class="fee-text">¥{{ formatNumber(row.fee) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="netAmount" label="净额" min-width="130" align="right">
              <template #default="{ row }">
                <span class="net-text">¥{{ formatNumber(row.netAmount) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="successRate" label="成功率" min-width="110">
              <template #default="{ row }">
                <el-progress
                  :percentage="row.successRate"
                  :stroke-width="8"
                  :color="getRateColor(row.successRate)"
                  :format="() => row.successRate + '%'"
                />
              </template>
            </el-table-column>
            <el-table-column prop="errorRate" label="差错率" min-width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="row.errorRate > 1 ? 'danger' : row.errorRate > 0.3 ? 'warning' : 'success'" size="small">
                  {{ row.errorRate }}%
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="bottom-section">
      <el-col :span="24">
        <div class="card-shadow chart-card">
          <div class="card-header">
            <div class="card-title">
              <el-icon :size="18" color="var(--warning-color)"><DataAnalysis /></el-icon>
              商户对账结算 TOP10
            </div>
            <el-link type="primary" :underline="false">查看全部</el-link>
          </div>
          <el-table :data="merchantTop10" style="width: 100%" stripe>
            <el-table-column label="排名" width="80" align="center">
              <template #default="{ $index }">
                <div class="rank-cell">
                  <span :class="['rank-badge', `rank-${$index + 1}`]">{{ $index + 1 }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="merchantId" label="商户号" min-width="140">
              <template #default="{ row }">
                <span class="merchant-id">{{ row.merchantId }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="merchantName" label="商户名称" min-width="180" />
            <el-table-column prop="transCount" label="交易笔数" min-width="110" align="right">
              <template #default="{ row }">
                <span class="num-text">{{ formatNumber(row.transCount) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="transAmount" label="交易金额" min-width="130" align="right">
              <template #default="{ row }">
                <span class="amount-text">¥{{ formatNumber(row.transAmount) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="fee" label="手续费" min-width="110" align="right">
              <template #default="{ row }">
                <span class="fee-text">¥{{ formatNumber(row.fee) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="settleAmount" label="结算金额" min-width="130" align="right">
              <template #default="{ row }">
                <span class="settle-text">¥{{ formatNumber(row.settleAmount) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="successRate" label="成功率" min-width="110" align="center">
              <template #default="{ row }">
                <span :class="['rate-text', getRateClass(row.successRate)]">{{ row.successRate }}%</span>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import {
  Download, TrendCharts, Bottom, PieChart, DataAnalysis,
  Wallet, CreditCard, Coin, Money
} from '@element-plus/icons-vue'
import dayjs from 'dayjs'

const dateRange = ref<[Date, Date]>([dayjs().startOf('day').toDate(), dayjs().endOf('day').toDate()])
const dateShortcut = ref('today')
const trendChartRef = ref<HTMLElement>()
const pieChartRef = ref<HTMLElement>()
let trendChart: echarts.ECharts | null = null
let pieChart: echarts.ECharts | null = null

const dateShortcuts = [
  {
    text: '今日',
    value: () => [dayjs().startOf('day').toDate(), dayjs().endOf('day').toDate()]
  },
  {
    text: '昨日',
    value: () => [dayjs().subtract(1, 'day').startOf('day').toDate(), dayjs().subtract(1, 'day').endOf('day').toDate()]
  },
  {
    text: '近7天',
    value: () => [dayjs().subtract(6, 'day').startOf('day').toDate(), dayjs().endOf('day').toDate()]
  },
  {
    text: '近30天',
    value: () => [dayjs().subtract(29, 'day').startOf('day').toDate(), dayjs().endOf('day').toDate()]
  },
  {
    text: '本月',
    value: () => [dayjs().startOf('month').toDate(), dayjs().endOf('month').toDate()]
  },
  {
    text: '上月',
    value: () => [dayjs().subtract(1, 'month').startOf('month').toDate(), dayjs().subtract(1, 'month').endOf('month').toDate()]
  }
]

const metrics = ref([
  {
    label: '总交易笔数',
    value: '156,842',
    trend: 8.6,
    icon: CreditCard,
    iconBg: 'var(--primary-bg)',
    iconColor: 'var(--primary-color)'
  },
  {
    label: '总交易金额',
    value: '¥28,456,320',
    trend: 12.3,
    icon: Wallet,
    iconBg: 'var(--success-bg)',
    iconColor: 'var(--success-color)'
  },
  {
    label: '总手续费',
    value: '¥156,842',
    trend: 5.2,
    icon: Coin,
    iconBg: 'var(--warning-bg)',
    iconColor: 'var(--warning-color)'
  },
  {
    label: '净结算金额',
    value: '¥28,299,478',
    trend: 12.5,
    icon: Money,
    iconBg: 'var(--purple-bg)',
    iconColor: 'var(--purple-color)'
  }
])

const channelDistribution = ref([
  { name: '微信支付', value: 10854352, percent: 38.2, color: '#07C160' },
  { name: '支付宝', value: 9987645, percent: 35.1, color: '#1677FF' },
  { name: '银联支付', value: 4532108, percent: 15.9, color: '#E60012' },
  { name: 'Visa/MC', value: 3082215, percent: 10.8, color: '#1A1F71' }
])

const channelSummary = ref([
  { channel: '微信支付', transCount: 58632, transAmount: 10854352, fee: 65126, netAmount: 10789226, successRate: 99.89, errorRate: 0.11, color: '#07C160' },
  { channel: '支付宝', transCount: 54218, transAmount: 9987645, fee: 59926, netAmount: 9927719, successRate: 99.92, errorRate: 0.08, color: '#1677FF' },
  { channel: '银联支付', transCount: 26854, transAmount: 4532108, fee: 22661, netAmount: 4509447, successRate: 98.56, errorRate: 1.44, color: '#E60012' },
  { channel: 'Visa/MC', transCount: 17138, transAmount: 3082215, fee: 9129, netAmount: 3073086, successRate: 99.21, errorRate: 0.79, color: '#1A1F71' }
])

const merchantTop10 = ref([
  { merchantId: 'M10001', merchantName: '某电商平台旗舰店', transCount: 28654, transAmount: 5682340, fee: 34094, settleAmount: 5648246, successRate: 99.92 },
  { merchantId: 'M10002', merchantName: '某SaaS云服务商', transCount: 18923, transAmount: 4235600, fee: 25414, settleAmount: 4210186, successRate: 99.88 },
  { merchantId: 'M10003', merchantName: '某在线教育平台', transCount: 15632, transAmount: 3258900, fee: 19553, settleAmount: 3239347, successRate: 99.76 },
  { merchantId: 'M10004', merchantName: '某跨境电商公司', transCount: 12458, transAmount: 2896400, fee: 8632, settleAmount: 2887768, successRate: 99.45 },
  { merchantId: 'M10005', merchantName: '某零售连锁集团', transCount: 21356, transAmount: 2568700, fee: 15412, settleAmount: 2553288, successRate: 98.68 },
  { merchantId: 'M10006', merchantName: '某出行科技公司', transCount: 16874, transAmount: 2156300, fee: 12938, settleAmount: 2143362, successRate: 99.82 },
  { merchantId: 'M10007', merchantName: '某生鲜配送平台', transCount: 14235, transAmount: 1896500, fee: 11379, settleAmount: 1885121, successRate: 99.56 },
  { merchantId: 'M10008', merchantName: '某数字内容平台', transCount: 9856, transAmount: 1658200, fee: 9949, settleAmount: 1648251, successRate: 99.95 },
  { merchantId: 'M10009', merchantName: '某医疗健康平台', transCount: 8745, transAmount: 1456800, fee: 8741, settleAmount: 1448059, successRate: 99.34 },
  { merchantId: 'M10010', merchantName: '某游戏娱乐公司', transCount: 10109, transAmount: 1246580, fee: 7479, settleAmount: 1239101, successRate: 99.71 }
])

const formatNumber = (num: number) => {
  return num.toLocaleString('zh-CN')
}

const getRateColor = (rate: number) => {
  if (rate >= 99.5) return '#00B42A'
  if (rate >= 98) return '#FF7D00'
  return '#F53F3F'
}

const getRateClass = (rate: number) => {
  if (rate >= 99.5) return 'success'
  if (rate >= 98) return 'warning'
  return 'danger'
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
  dateRange.value = [start.toDate(), end.toDate()]
  updateChartsData()
}

const handleDateRangeChange = () => {
  dateShortcut.value = ''
  updateChartsData()
}

const initTrendChart = () => {
  if (!trendChartRef.value) return
  trendChart = echarts.init(trendChartRef.value)

  const days = ['06-23', '06-24', '06-25', '06-26', '06-27', '06-28', '06-29']
  const countData = [19856, 21342, 18756, 23458, 22876, 24563, 25991]
  const amountData = [3658900, 3987200, 3521400, 4256800, 4123500, 4458600, 4449920]

  const option = {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255,255,255,0.95)',
      borderColor: 'var(--border-color)',
      textStyle: { color: 'var(--text-primary)' },
      axisPointer: { type: 'cross' }
    },
    legend: {
      data: ['交易笔数', '交易金额'],
      top: 0,
      right: 0,
      textStyle: { color: 'var(--text-regular)' }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '40px',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: days,
      axisLine: { lineStyle: { color: 'var(--border-color)' } },
      axisLabel: { color: 'var(--text-secondary)' }
    },
    yAxis: [
      {
        type: 'value',
        name: '笔数',
        splitLine: { lineStyle: { color: 'var(--border-light)', type: 'dashed' } },
        axisLabel: { color: 'var(--text-secondary)' }
      },
      {
        type: 'value',
        name: '金额(万)',
        splitLine: { show: false },
        axisLabel: { color: 'var(--text-secondary)' }
      }
    ],
    series: [
      {
        name: '交易笔数',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        lineStyle: { width: 2, color: '#165DFF' },
        itemStyle: { color: '#165DFF' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(22, 93, 255, 0.2)' },
            { offset: 1, color: 'rgba(22, 93, 255, 0.02)' }
          ])
        },
        data: countData
      },
      {
        name: '交易金额',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        yAxisIndex: 1,
        lineStyle: { width: 2, color: '#00B42A' },
        itemStyle: { color: '#00B42A' },
        data: amountData.map(v => Math.floor(v / 10000))
      }
    ]
  }

  trendChart.setOption(option)
}

const initPieChart = () => {
  if (!pieChartRef.value) return
  pieChart = echarts.init(pieChartRef.value)

  const option = {
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(255,255,255,0.95)',
      borderColor: 'var(--border-color)',
      textStyle: { color: 'var(--text-primary)' },
      formatter: '{b}: {d}%'
    },
    series: [
      {
        type: 'pie',
        radius: ['50%', '75%'],
        center: ['50%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 6,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: { show: false },
        emphasis: {
          label: {
            show: true,
            fontSize: 14,
            fontWeight: 'bold'
          },
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.2)'
          }
        },
        labelLine: { show: false },
        data: channelDistribution.value.map(item => ({
          value: item.value,
          name: item.name,
          itemStyle: { color: item.color }
        }))
      }
    ]
  }

  pieChart.setOption(option)
}

const updateChartsData = () => {
}

onMounted(() => {
  nextTick(() => {
    initTrendChart()
    initPieChart()

    window.addEventListener('resize', () => {
      trendChart?.resize()
      pieChart?.resize()
    })
  })
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
    margin-bottom: 8px;
    letter-spacing: -0.5px;
  }

  .metric-footer {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 13px;
  }

  .trend {
    display: flex;
    align-items: center;
    gap: 2px;
    font-weight: 500;
    padding: 2px 8px;
    border-radius: var(--radius-sm);

    &.up {
      color: var(--success-color);
      background: var(--success-bg);
    }

    &.down {
      color: var(--danger-color);
      background: var(--danger-bg);
    }
  }

  .compare-text {
    color: var(--text-secondary);
  }
}

.chart-section {
  margin-bottom: 20px;
}

.table-section {
  margin-bottom: 20px;
}

.bottom-section {
  margin-bottom: 20px;
}

.chart-card {
  padding: 20px;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
  }

  .card-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 16px;
    font-weight: 600;
    color: var(--text-primary);
  }
}

.chart-container {
  height: 340px;
}

.pie-chart-wrapper {
  display: flex;
  align-items: center;
  gap: 20px;
  height: 340px;
}

.pie-chart-container {
  width: 200px;
  height: 280px;
  flex-shrink: 0;
}

.pie-legend {
  flex: 1;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 0;
  border-bottom: 1px solid var(--border-light);

  &:last-child {
    border-bottom: none;
  }
}

.legend-dot {
  width: 10px;
  height: 10px;
  border-radius: 3px;
  flex-shrink: 0;
}

.legend-label {
  font-size: 14px;
  color: var(--text-primary);
  flex: 1;
}

.legend-value {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-regular);
  width: 60px;
  text-align: right;
}

.channel-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.channel-logo {
  width: 28px;
  height: 28px;
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-weight: 600;
  font-size: 14px;
  flex-shrink: 0;
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

.fee-text {
  font-family: 'SF Mono', Monaco, 'Courier New', monospace;
  color: var(--warning-color);
}

.net-text {
  font-weight: 600;
  color: var(--success-color);
  font-family: 'SF Mono', Monaco, 'Courier New', monospace;
}

.settle-text {
  font-weight: 600;
  color: var(--primary-color);
  font-family: 'SF Mono', Monaco, 'Courier New', monospace;
}

.rank-cell {
  display: flex;
  justify-content: center;
}

.rank-badge {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 13px;
  background: var(--bg-hover);
  color: var(--text-secondary);

  &.rank-1 {
    background: linear-gradient(135deg, #FFD700, #FFA500);
    color: #fff;
  }

  &.rank-2 {
    background: linear-gradient(135deg, #C0C0C0, #A0A0A0);
    color: #fff;
  }

  &.rank-3 {
    background: linear-gradient(135deg, #CD7F32, #A0522D);
    color: #fff;
  }
}

.merchant-id {
  font-family: 'SF Mono', Monaco, 'Courier New', monospace;
  color: var(--primary-color);
}

.rate-text {
  font-weight: 500;

  &.success {
    color: var(--success-color);
  }

  &.warning {
    color: var(--warning-color);
  }

  &.danger {
    color: var(--danger-color);
  }
}
</style>
