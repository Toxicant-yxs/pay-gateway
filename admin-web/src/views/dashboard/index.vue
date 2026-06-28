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
import { ref, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import {
  Download, TrendCharts, Bottom, Bell, Connection, PieChart, List, ArrowRight,
  Wallet, CreditCard, CircleCheck, Timer
} from '@element-plus/icons-vue'
import dayjs from 'dayjs'

const dateRange = ref<[Date, Date]>([dayjs().subtract(7, 'day').toDate(), new Date()])
const chartPeriod = ref('24h')
const distributionType = ref('channel')
const trendChartRef = ref<HTMLElement>()
const pieChartRef = ref<HTMLElement>()
let trendChart: echarts.ECharts | null = null
let pieChart: echarts.ECharts | null = null

const metrics = ref([
  {
    label: '今日交易额',
    value: '¥12,845,632',
    trend: 12.5,
    type: '',
    icon: Wallet,
    iconBg: 'var(--primary-bg)',
    iconColor: 'var(--primary-color)'
  },
  {
    label: '交易笔数',
    value: '86,429',
    trend: 8.3,
    type: 'success',
    icon: CreditCard,
    iconBg: 'var(--success-bg)',
    iconColor: 'var(--success-color)'
  },
  {
    label: '支付成功率',
    value: '99.72%',
    trend: -0.15,
    type: 'warning',
    icon: CircleCheck,
    iconBg: 'var(--warning-bg)',
    iconColor: 'var(--warning-color)'
  },
  {
    label: '平均响应时间',
    value: '38ms',
    trend: -5.2,
    type: '',
    icon: Timer,
    iconBg: 'var(--purple-bg)',
    iconColor: 'var(--purple-color)'
  }
])

const alerts = ref([
  { level: 'danger', title: '银联通道响应超时率超过5%', time: '2分钟前' },
  { level: 'warning', title: '风控拦截规则触发：IP异常高频访问', time: '5分钟前' },
  { level: 'warning', title: '商户M10086日交易量超预警阈值', time: '12分钟前' },
  { level: 'info', title: '自动对账完成，差异笔数0笔', time: '30分钟前' },
  { level: 'info', title: '微信支付版本更新通知', time: '1小时前' }
])

const channels = ref([
  { name: '微信支付', desc: 'Native/JSAPI/H5', status: 'normal', successRate: 99.89, latency: 32, qps: 1256, color: '#07C160' },
  { name: '支付宝', desc: '电脑网站/手机网站', status: 'normal', successRate: 99.92, latency: 28, qps: 1089, color: '#1677FF' },
  { name: '银联支付', desc: '银联在线/云闪付', status: 'warning', successRate: 97.56, latency: 86, qps: 425, color: '#E60012' },
  { name: 'Visa/MC', desc: '国际信用卡', status: 'normal', successRate: 99.21, latency: 156, qps: 234, color: '#1A1F71' }
])

const distributionData = ref([
  { name: '微信支付', value: 38.5, amount: 4945568, color: '#07C160' },
  { name: '支付宝', value: 35.2, amount: 4521663, color: '#1677FF' },
  { name: '银联支付', value: 15.8, amount: 2029610, color: '#E60012' },
  { name: 'Visa/MC', value: 10.5, amount: 1348791, color: '#1A1F71' }
])

const recentTransactions = ref([
  { orderNo: 'PAY20260628000123456', merchant: '某电商平台', channel: '微信支付', amount: 29900, payMethod: 'JSAPI', status: '支付成功', time: '2026-06-28 14:32:15' },
  { orderNo: 'PAY20260628000123455', merchant: '某SaaS服务商', channel: '支付宝', amount: 128000, payMethod: '电脑网站', status: '支付成功', time: '2026-06-28 14:31:42' },
  { orderNo: 'PAY20260628000123454', merchant: '某跨境电商', channel: 'Visa/MC', amount: 56800, payMethod: '信用卡', status: '支付中', time: '2026-06-28 14:31:08' },
  { orderNo: 'PAY20260628000123453', merchant: '某在线教育', channel: '微信支付', amount: 9900, payMethod: 'H5', status: '支付成功', time: '2026-06-28 14:30:55' },
  { orderNo: 'PAY20260628000123452', merchant: '某零售连锁', channel: '银联支付', amount: 156000, payMethod: '云闪付', status: '支付失败', time: '2026-06-28 14:30:33' },
  { orderNo: 'PAY20260628000123451', merchant: '某出行平台', channel: '支付宝', amount: 45600, payMethod: '手机网站', status: '支付成功', time: '2026-06-28 14:30:12' }
])

const formatNumber = (num: number) => {
  return num.toLocaleString('zh-CN')
}

const getStatusType = (status: string) => {
  if (status === '支付成功') return 'success'
  if (status === '支付失败') return 'danger'
  if (status === '支付中') return 'warning'
  return 'info'
}

const initTrendChart = () => {
  if (!trendChartRef.value) return
  trendChart = echarts.init(trendChartRef.value)

  const hours = Array.from({ length: 24 }, (_, i) => `${i}:00`)
  const successData = []
  const failData = []
  const amountData = []

  for (let i = 0; i < 24; i++) {
    const base = 2000 + Math.sin(i / 4) * 1500 + Math.random() * 800
    const fail = Math.floor(base * 0.005 + Math.random() * 20)
    successData.push(Math.floor(base))
    failData.push(fail)
    amountData.push(Math.floor(base * (150 + Math.random() * 100)))
  }

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
      data: hours,
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
        data: successData
      },
      {
        name: '失败笔数',
        type: 'line',
        smooth: true,
        symbol: 'none',
        lineStyle: { width: 2, color: '#F53F3F' },
        data: failData
      },
      {
        name: '交易金额',
        type: 'line',
        smooth: true,
        symbol: 'none',
        yAxisIndex: 1,
        lineStyle: { width: 2, color: '#00B42A' },
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
          value: item.value,
          name: item.name,
          itemStyle: { color: item.color }
        }))
      }
    ]
  }

  pieChart.setOption(option)
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
