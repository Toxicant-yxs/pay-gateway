<template>
  <div class="page-container dashboard-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">数据概览</h2>
        <p class="page-desc">实时监控支付网关核心指标与运行状态</p>
      </div>
      <div class="header-actions">
        <el-date-picker
          v-model="dateRange"
          type="datetimerange"
          range-separator="至"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
          size="default"
          style="width: 360px"
        />
        <el-button type="primary">
          <el-icon><Download /></el-icon>
          导出报表
        </el-button>
      </div>
    </div>

    <el-row :gutter="20" class="metric-cards">
      <el-col :span="6" v-for="(metric, index) in metrics" :key="index">
        <div class="card-shadow metric-card" :class="metric.type">
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
            <span class="compare-text">较昨日{{ metric.trend > 0 ? '增长' : '下降' }}</span>
          </div>
          <div class="metric-sparkline" ref="sparklineRefs" :data-index="index"></div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-section">
      <el-col :span="16">
        <div class="card-shadow chart-card">
          <div class="card-header">
            <div class="card-title">
              <el-icon :size="18" color="var(--primary-color)"><TrendCharts /></el-icon>
              实时交易趋势
            </div>
            <el-radio-group v-model="chartPeriod" size="small">
              <el-radio-button label="1h">近1小时</el-radio-button>
              <el-radio-button label="24h">近24小时</el-radio-button>
              <el-radio-button label="7d">近7天</el-radio-button>
            </el-radio-group>
          </div>
          <div ref="trendChartRef" class="chart-container"></div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="card-shadow chart-card alerts-card">
          <div class="card-header">
            <div class="card-title">
              <el-icon :size="18" color="var(--danger-color)"><Bell /></el-icon>
              实时告警
              <el-tag type="danger" size="small" effect="dark" round class="alert-count">{{ alerts.length }}</el-tag>
            </div>
            <el-link type="primary" :underline="false">查看全部</el-link>
          </div>
          <div class="alert-list">
            <div v-for="(alert, index) in alerts" :key="index" class="alert-item" :class="alert.level">
              <div class="alert-dot"></div>
              <div class="alert-content">
                <p class="alert-title">{{ alert.title }}</p>
                <p class="alert-time">{{ alert.time }}</p>
              </div>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="bottom-section">
      <el-col :span="12">
        <div class="card-shadow chart-card">
          <div class="card-header">
            <div class="card-title">
              <el-icon :size="18" color="var(--primary-color)"><Connection /></el-icon>
              支付通道状态
            </div>
            <el-link type="primary" :underline="false">通道管理</el-link>
          </div>
          <div class="channel-grid">
            <div v-for="channel in channels" :key="channel.name" class="channel-item">
              <div class="channel-header">
                <div class="channel-logo" :style="{ background: channel.color }">
                  {{ channel.name.charAt(0) }}
                </div>
                <div class="channel-info">
                  <div class="channel-name">{{ channel.name }}</div>
                  <div class="channel-desc">{{ channel.desc }}</div>
                </div>
                <el-tag :type="channel.status === 'normal' ? 'success' : channel.status === 'warning' ? 'warning' : 'danger'" size="small">
                  {{ channel.status === 'normal' ? '正常' : channel.status === 'warning' ? '波动' : '异常' }}
                </el-tag>
              </div>
              <div class="channel-metrics">
                <div class="channel-metric">
                  <span class="metric-label-sm">成功率</span>
                  <span class="metric-value-sm" :class="channel.successRate >= 99 ? 'success' : channel.successRate >= 97 ? 'warning' : 'danger'">
                    {{ channel.successRate }}%
                  </span>
                </div>
                <div class="channel-metric">
                  <span class="metric-label-sm">延迟</span>
                  <span class="metric-value-sm">{{ channel.latency }}ms</span>
                </div>
                <div class="channel-metric">
                  <span class="metric-label-sm">QPS</span>
                  <span class="metric-value-sm">{{ channel.qps }}</span>
                </div>
              </div>
              <div class="channel-progress">
                <el-progress
                  :percentage="channel.successRate"
                  :stroke-width="4"
                  :show-text="false"
                  :color="channel.successRate >= 99 ? '#00B42A' : channel.successRate >= 97 ? '#FF7D00' : '#F53F3F'"
                />
              </div>
            </div>
          </div>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="card-shadow chart-card">
          <div class="card-header">
            <div class="card-title">
              <el-icon :size="18" color="var(--purple-color)"><PieChart /></el-icon>
              交易金额分布
            </div>
            <el-radio-group v-model="distributionType" size="small">
              <el-radio-button label="channel">按通道</el-radio-button>
              <el-radio-button label="currency">按币种</el-radio-button>
            </el-radio-group>
          </div>
          <div class="distribution-content">
            <div ref="pieChartRef" class="pie-chart-container"></div>
            <div class="distribution-legend">
              <div v-for="(item, index) in distributionData" :key="index" class="legend-item">
                <div class="legend-dot" :style="{ background: item.color }"></div>
                <span class="legend-label">{{ item.name }}</span>
                <span class="legend-value">{{ item.value }}%</span>
                <span class="legend-amount">¥{{ formatNumber(item.amount) }}</span>
              </div>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="recent-transactions">
      <el-col :span="24">
        <div class="card-shadow chart-card">
          <div class="card-header">
            <div class="card-title">
              <el-icon :size="18" color="var(--primary-color)"><List /></el-icon>
              最近交易
            </div>
            <el-link type="primary" :underline="false" @click="$router.push('/transaction/list')">
              查看全部
              <el-icon class="el-icon--right"><ArrowRight /></el-icon>
            </el-link>
          </div>
          <el-table :data="recentTransactions" style="width: 100%" stripe>
            <el-table-column prop="orderNo" label="订单号" min-width="180">
              <template #default="{ row }">
                <span class="order-no">{{ row.orderNo }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="merchant" label="商户" min-width="140" />
            <el-table-column prop="channel" label="支付通道" min-width="100">
              <template #default="{ row }">
                <el-tag size="small" effect="plain">{{ row.channel }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="amount" label="金额" min-width="120" align="right">
              <template #default="{ row }">
                <span class="amount-text">¥{{ formatNumber(row.amount) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="payMethod" label="支付方式" min-width="100" />
            <el-table-column prop="status" label="状态" min-width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)" size="small">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="time" label="交易时间" min-width="160" />
            <el-table-column label="操作" width="100" fixed="right">
              <template #default>
                <el-button type="primary" link size="small">详情</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import * as echarts from 'echarts'
import {
  Download, TrendCharts, Bottom, Bell, Connection, PieChart, List, ArrowRight,
  Wallet, CreditCard, CircleCheck, Timer
} from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import { dashboardApi } from '@/api/dashboard'
import type { DashboardStatistics, TrendData, ChannelStatusItem, AlertItem, RecentTrade } from '@/types/dashboard'

const dateRange = ref<[Date, Date]>([dayjs().subtract(7, 'day').toDate(), new Date()])
const chartPeriod = ref<'1h' | '24h' | '7d'>('24h')
const distributionType = ref<'channel' | 'currency'>('channel')
const trendChartRef = ref<HTMLElement>()
const pieChartRef = ref<HTMLElement>()
let trendChart: echarts.ECharts | null = null
let pieChart: echarts.ECharts | null = null

const statistics = ref<DashboardStatistics>({
  todayAmount: 0,
  todayAmountGrowth: 0,
  todayCount: 0,
  todayCountGrowth: 0,
  successRate: 0,
  successRateChange: 0,
  avgLatency: 0,
  avgLatencyChange: 0
})

const channelColors: Record<string, string> = {
  wechat: '#07C160',
  WECHAT: '#07C160',
  alipay: '#1677FF',
  ALIPAY: '#1677FF',
  unionpay: '#E60012',
  UNIONPAY: '#E60012',
  visa: '#1A1F71',
  VISA: '#1A1F71',
  dcb: '#D4382F',
  DCB: '#D4382F'
}

const channelNameMap: Record<string, string> = {
  wechat: '微信支付',
  WECHAT: '微信支付',
  alipay: '支付宝',
  ALIPAY: '支付宝',
  unionpay: '银联支付',
  UNIONPAY: '银联支付',
  visa: 'Visa/MC',
  VISA: 'Visa/MC',
  dcb: '数字人民币',
  DCB: '数字人民币'
}

const metrics = computed(() => [
  {
    label: '今日交易额',
    value: '¥' + formatNumber(statistics.value.todayAmount ?? 0),
    trend: statistics.value.todayAmountGrowth ?? 0,
    type: '',
    icon: Wallet,
    iconBg: 'var(--primary-bg)',
    iconColor: 'var(--primary-color)'
  },
  {
    label: '交易笔数',
    value: formatNumber(statistics.value.todayCount ?? 0),
    trend: statistics.value.todayCountGrowth ?? 0,
    type: 'success',
    icon: CreditCard,
    iconBg: 'var(--success-bg)',
    iconColor: 'var(--success-color)'
  },
  {
    label: '支付成功率',
    value: (statistics.value.successRate ?? 0).toFixed(2) + '%',
    trend: statistics.value.successRateChange ?? 0,
    type: 'warning',
    icon: CircleCheck,
    iconBg: 'var(--warning-bg)',
    iconColor: 'var(--warning-color)'
  },
  {
    label: '平均响应时间',
    value: Math.round(statistics.value.avgLatency ?? 0) + 'ms',
    trend: -(statistics.value.avgLatencyChange ?? 0),
    type: '',
    icon: Timer,
    iconBg: 'var(--purple-bg)',
    iconColor: 'var(--purple-color)'
  }
])

const alerts = ref<Array<{ level: string; title: string; time: string }>>([])

const channels = ref<Array<{ name: string; desc: string; status: string; successRate: number; latency: number; qps: number; color: string }>>([])

const distributionData = ref([
  { name: '微信支付', value: 0, amount: 0, color: '#07C160' },
  { name: '支付宝', value: 0, amount: 0, color: '#1677FF' },
  { name: '银联支付', value: 0, amount: 0, color: '#E60012' },
  { name: 'Visa/MC', value: 0, amount: 0, color: '#1A1F71' }
])

const recentTransactions = ref<Array<{ orderNo: string; merchant: string; channel: string; amount: number; payMethod: string; status: string; time: string; statusCode: number | string }>>([])

const tradeStatusMap: Record<number, { text: string; type: string }> = {
  0: { text: '待支付', type: 'info' },
  1: { text: '支付中', type: 'warning' },
  2: { text: '支付成功', type: 'success' },
  3: { text: '支付失败', type: 'danger' },
  4: { text: '已关闭', type: 'info' },
  5: { text: '已退款', type: '' },
  6: { text: '部分退款', type: 'warning' }
}

const alertLevelMap: Record<string, string> = {
  HIGH: 'danger',
  high: 'danger',
  MEDIUM: 'warning',
  medium: 'warning',
  LOW: 'info',
  low: 'info'
}

function formatNumber(num: number) {
  if (num === null || num === undefined || isNaN(num)) return '0'
  return num.toLocaleString('zh-CN')
}

function getStatusType(status: number | string) {
  if (typeof status === 'number') {
    return tradeStatusMap[status]?.type ?? 'info'
  }
  const statusStr = String(status).toUpperCase()
  if (statusStr === 'SUCCESS' || statusStr === '2' || status === '支付成功') return 'success'
  if (statusStr === 'FAILED' || statusStr === 'FAIL' || statusStr === '3' || status === '支付失败') return 'danger'
  if (statusStr === 'PAYING' || statusStr === 'PROCESSING' || statusStr === '1' || status === '支付中') return 'warning'
  return 'info'
}

function getStatusText(status: number | string) {
  if (typeof status === 'number') {
    return tradeStatusMap[status]?.text ?? '未知'
  }
  const statusStr = String(status).toUpperCase()
  if (statusStr === 'SUCCESS') return '支付成功'
  if (statusStr === 'FAILED' || statusStr === 'FAIL') return '支付失败'
  if (statusStr === 'PAYING' || statusStr === 'PROCESSING') return '支付中'
  if (statusStr === 'PENDING' || statusStr === '0') return '待支付'
  if (statusStr === 'CLOSED' || statusStr === '4') return '已关闭'
  if (statusStr === 'REFUNDED' || statusStr === '5') return '已退款'
  if (statusStr === 'PARTIAL_REFUNDED' || statusStr === '6') return '部分退款'
  return String(status)
}

function getChannelName(code: string) {
  return channelNameMap[code] ?? code ?? '未知通道'
}

function getChannelColor(code: string) {
  return channelColors[code] ?? 'var(--primary-color)'
}

function parsePayTypes(payTypes: string | string[] | undefined): string[] {
  if (!payTypes) return []
  if (Array.isArray(payTypes)) return payTypes
  return String(payTypes).split(',').map(t => t.trim()).filter(Boolean)
}

function getChannelStatus(status: number | string | undefined): 'normal' | 'warning' | 'danger' {
  if (status === 1 || status === 'NORMAL' || status === '1') return 'normal'
  const rate = 0
  if (typeof status === 'number' && status === 0) return 'danger'
  return 'warning'
}

function formatAlertTime(time: string | undefined): string {
  if (!time) return '刚刚'
  const now = dayjs()
  const t = dayjs(time)
  const diffMin = now.diff(t, 'minute')
  if (diffMin < 1) return '刚刚'
  if (diffMin < 60) return `${diffMin}分钟前`
  const diffHour = now.diff(t, 'hour')
  if (diffHour < 24) return `${diffHour}小时前`
  const diffDay = now.diff(t, 'day')
  if (diffDay < 30) return `${diffDay}天前`
  return t.format('YYYY-MM-DD HH:mm')
}

async function loadStatistics() {
  try {
    const data = await dashboardApi.getStatistics()
    if (data) {
      statistics.value = {
        todayAmount: data.todayAmount ?? 0,
        todayAmountGrowth: data.todayAmountGrowth ?? 0,
        todayCount: data.todayCount ?? 0,
        todayCountGrowth: data.todayCountGrowth ?? 0,
        successRate: data.successRate ?? 0,
        successRateChange: data.successRateChange ?? 0,
        avgLatency: data.avgLatency ?? 0,
        avgLatencyChange: data.avgLatencyChange ?? 0
      }
    }
  } catch (e) {
    console.error('Failed to load statistics:', e)
  }
}

async function loadTrend() {
  try {
    const data = await dashboardApi.getTrend({ type: chartPeriod.value })
    if (data) {
      updateTrendChart(data)
    }
  } catch (e) {
    console.error('Failed to load trend:', e)
  }
}

function updateTrendChart(data: TrendData) {
  if (!trendChart) return
  const xAxisData = data.xAxis ?? data.xaxis ?? []
  const successCount = data.successCount ?? []
  const failCount = data.failCount ?? []
  const successAmount = data.successAmount ?? []

  const option = {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255,255,255,0.95)',
      borderColor: 'var(--border-color)',
      textStyle: { color: 'var(--text-primary)' },
      axisPointer: { type: 'cross' }
    },
    legend: {
      data: ['成功笔数', '失败笔数', '交易金额'],
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
      data: xAxisData.length > 0 ? xAxisData : Array.from({ length: 24 }, (_, i) => `${i}:00`),
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
        name: '成功笔数',
        type: 'line',
        smooth: true,
        symbol: 'none',
        lineStyle: { width: 2, color: '#165DFF' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(22, 93, 255, 0.25)' },
            { offset: 1, color: 'rgba(22, 93, 255, 0.02)' }
          ])
        },
        data: successCount.length > 0 ? successCount : [0]
      },
      {
        name: '失败笔数',
        type: 'line',
        smooth: true,
        symbol: 'none',
        lineStyle: { width: 2, color: '#F53F3F' },
        data: failCount.length > 0 ? failCount : [0]
      },
      {
        name: '交易金额',
        type: 'line',
        smooth: true,
        symbol: 'none',
        yAxisIndex: 1,
        lineStyle: { width: 2, color: '#00B42A' },
        data: successAmount.length > 0 ? successAmount.map((v: number) => Math.floor((v ?? 0) / 10000)) : [0]
      }
    ]
  }
  trendChart.setOption(option, true)
}

async function loadChannels() {
  try {
    const data = await dashboardApi.getChannelStatus()
    if (Array.isArray(data)) {
      channels.value = data.map((ch: ChannelStatusItem) => {
        const chName = getChannelName(ch.channelCode ?? '')
        const payTypesArr = parsePayTypes(ch.payTypes)
        const successRate = ch.avgSuccessRate ?? ch.successRate ?? 0
        let status: 'normal' | 'warning' | 'danger' = 'normal'
        if (ch.status === 0 || ch.status === 'DISABLED' || ch.status === 'ABNORMAL') {
          status = 'danger'
        } else if (successRate < 97) {
          status = 'warning'
        }
        return {
          name: ch.channelName ?? chName,
          desc: payTypesArr.slice(0, 3).join('/') || '多种支付方式',
          status,
          successRate: Number(successRate?.toFixed(2) ?? 0),
          latency: Math.round(ch.avgLatency ?? 0),
          qps: ch.dailyCount ?? ch.qps ?? 0,
          color: getChannelColor(ch.channelCode ?? '')
        }
      })
      if (channels.value.length === 0) {
        channels.value = [
          { name: '微信支付', desc: 'Native/JSAPI/H5', status: 'normal', successRate: 99.89, latency: 32, qps: 0, color: '#07C160' },
          { name: '支付宝', desc: '电脑网站/手机网站', status: 'normal', successRate: 99.92, latency: 28, qps: 0, color: '#1677FF' }
        ]
      }
    }
  } catch (e) {
    console.error('Failed to load channels:', e)
    channels.value = [
      { name: '微信支付', desc: 'Native/JSAPI/H5', status: 'normal', successRate: 99.89, latency: 32, qps: 0, color: '#07C160' },
      { name: '支付宝', desc: '电脑网站/手机网站', status: 'normal', successRate: 99.92, latency: 28, qps: 0, color: '#1677FF' }
    ]
  }
}

async function loadAlerts() {
  try {
    const data = await dashboardApi.getAlerts({ page: 1, pageSize: 10 })
    if (data && Array.isArray(data.list)) {
      alerts.value = data.list.map((alert: AlertItem) => ({
        level: alertLevelMap[alert.level] ?? 'info',
        title: alert.title ?? alert.message ?? alert.content ?? '系统通知',
        time: formatAlertTime(alert.createdAt ?? alert.createdTime ?? alert.time)
      }))
    }
    if (alerts.value.length === 0) {
      alerts.value = [
        { level: 'info', title: '系统运行正常', time: '刚刚' }
      ]
    }
  } catch (e) {
    console.error('Failed to load alerts:', e)
    alerts.value = [
      { level: 'info', title: '系统运行正常', time: '刚刚' }
    ]
  }
}

async function loadRecentTransactions() {
  try {
    const data = await dashboardApi.getRecentTransactions({ limit: 10 })
    if (Array.isArray(data)) {
      recentTransactions.value = data.map((trade: RecentTrade) => {
        const channelCode = trade.channelCode ?? trade.channel ?? ''
        const payMethod = trade.payType ?? trade.payMethod ?? '-'
        return {
          orderNo: trade.orderNo ?? '-',
          merchant: trade.merchantName ?? trade.merchantNo ?? '未知商户',
          channel: getChannelName(channelCode),
          amount: trade.amount ?? 0,
          payMethod,
          status: getStatusText(trade.status),
          time: trade.createdAt ?? trade.createdTime ?? trade.time ?? '-',
          statusCode: trade.status
        }
      })
    }
  } catch (e) {
    console.error('Failed to load recent transactions:', e)
    recentTransactions.value = []
  }
}

function initTrendChart() {
  if (!trendChartRef.value) return
  trendChart = echarts.init(trendChartRef.value)
  const option = {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255,255,255,0.95)',
      borderColor: 'var(--border-color)',
      textStyle: { color: 'var(--text-primary)' },
      axisPointer: { type: 'cross' }
    },
    legend: {
      data: ['成功笔数', '失败笔数', '交易金额'],
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
      data: Array.from({ length: 24 }, (_, i) => `${i}:00`),
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
        name: '成功笔数',
        type: 'line',
        smooth: true,
        symbol: 'none',
        lineStyle: { width: 2, color: '#165DFF' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(22, 93, 255, 0.25)' },
            { offset: 1, color: 'rgba(22, 93, 255, 0.02)' }
          ])
        },
        data: [0]
      },
      {
        name: '失败笔数',
        type: 'line',
        smooth: true,
        symbol: 'none',
        lineStyle: { width: 2, color: '#F53F3F' },
        data: [0]
      },
      {
        name: '交易金额',
        type: 'line',
        smooth: true,
        symbol: 'none',
        yAxisIndex: 1,
        lineStyle: { width: 2, color: '#00B42A' },
        data: [0]
      }
    ]
  }
  trendChart.setOption(option)
}

function initPieChart() {
  if (!pieChartRef.value) return
  pieChart = echarts.init(pieChartRef.value)
  const option = {
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(255,255,255,0.95)',
      borderColor: 'var(--border-color)',
      textStyle: { color: 'var(--text-primary)' },
      formatter: '{b}: {c}%'
    },
    series: [
      {
        type: 'pie',
        radius: ['55%', '80%'],
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
        data: distributionData.value.map(item => ({
          value: item.value || 25,
          name: item.name,
          itemStyle: { color: item.color }
        }))
      }
    ]
  }
  pieChart.setOption(option)
}

function handleResize() {
  trendChart?.resize()
  pieChart?.resize()
}

watch(chartPeriod, () => {
  loadTrend()
})

onMounted(() => {
  nextTick(() => {
    initTrendChart()
    initPieChart()
    loadStatistics()
    loadTrend()
    loadChannels()
    loadAlerts()
    loadRecentTransactions()
    window.addEventListener('resize', handleResize)
  })
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  trendChart?.dispose()
  pieChart?.dispose()
})
</script>

<style lang="scss" scoped>
.dashboard-page {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
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
    font-size: 28px;
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

  .metric-sparkline {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    height: 40px;
    opacity: 0.3;
  }
}

.chart-section {
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
  height: 320px;
}

.alerts-card {
  .alert-count {
    margin-left: 6px;
  }

  .alert-list {
    max-height: 320px;
    overflow-y: auto;
  }

  .alert-item {
    display: flex;
    align-items: flex-start;
    gap: 12px;
    padding: 12px;
    border-radius: var(--radius-md);
    margin-bottom: 8px;
    transition: background var(--transition-fast);

    &:hover {
      background: var(--bg-hover);
    }

    &:last-child {
      margin-bottom: 0;
    }

    &.danger {
      background: var(--danger-bg);
      .alert-dot { background: var(--danger-color); }
    }

    &.warning {
      background: var(--warning-bg);
      .alert-dot { background: var(--warning-color); }
    }

    &.info {
      background: var(--primary-bg);
      .alert-dot { background: var(--primary-color); }
    }
  }

  .alert-dot {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    margin-top: 6px;
    flex-shrink: 0;
  }

  .alert-content {
    flex: 1;
  }

  .alert-title {
    font-size: 14px;
    color: var(--text-primary);
    margin: 0 0 4px 0;
    font-weight: 500;
  }

  .alert-time {
    font-size: 12px;
    color: var(--text-secondary);
    margin: 0;
  }
}

.channel-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.channel-item {
  padding: 16px;
  border: 1px solid var(--border-light);
  border-radius: var(--radius-lg);
  transition: all var(--transition-fast);

  &:hover {
    border-color: var(--primary-color);
    box-shadow: var(--shadow-sm);
  }
}

.channel-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;
}

.channel-logo {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-weight: 600;
  font-size: 18px;
}

.channel-info {
  flex: 1;
}

.channel-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 2px;
}

.channel-desc {
  font-size: 12px;
  color: var(--text-secondary);
}

.channel-metrics {
  display: flex;
  gap: 20px;
  margin-bottom: 10px;
}

.channel-metric {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.metric-label-sm {
  font-size: 12px;
  color: var(--text-secondary);
}

.metric-value-sm {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);

  &.success { color: var(--success-color); }
  &.warning { color: var(--warning-color); }
  &.danger { color: var(--danger-color); }
}

.distribution-content {
  display: flex;
  gap: 20px;
  align-items: center;
}

.pie-chart-container {
  width: 180px;
  height: 220px;
  flex-shrink: 0;
}

.distribution-legend {
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

.legend-amount {
  font-size: 13px;
  color: var(--text-secondary);
  width: 100px;
  text-align: right;
}

.recent-transactions {
  .order-no {
    font-family: 'SF Mono', Monaco, 'Courier New', monospace;
    color: var(--primary-color);
    cursor: pointer;

    &:hover {
      text-decoration: underline;
    }
  }

  .amount-text {
    font-weight: 600;
    color: var(--text-primary);
    font-family: 'SF Mono', Monaco, 'Courier New', monospace;
  }
}
</style>
