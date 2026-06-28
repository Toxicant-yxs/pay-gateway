<template>
  <div class="page-container channel-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">通道配置</h2>
        <p class="page-desc">管理和配置所有支付通道，实时监控通道运行状态</p>
      </div>
      <div class="header-actions">
        <el-button>
          <el-icon><RefreshRight /></el-icon>
          刷新状态
        </el-button>
        <el-button type="primary">
          <el-icon><Plus /></el-icon>
          添加通道
        </el-button>
      </div>
    </div>

    <el-row :gutter="20" class="metric-cards">
      <el-col :span="6">
        <div class="card-shadow metric-card">
          <div class="metric-header">
            <span class="metric-label">已接入通道</span>
            <div class="metric-icon" style="background: var(--primary-bg); color: var(--primary-color)">
              <el-icon :size="20"><Connection /></el-icon>
            </div>
          </div>
          <div class="metric-value">{{ metrics.totalChannels }}</div>
          <div class="metric-footer">
            <span class="metric-sub-text">覆盖 {{ metrics.payMethods }} 种支付方式</span>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="card-shadow metric-card success">
          <div class="metric-header">
            <span class="metric-label">运行中通道</span>
            <div class="metric-icon" style="background: var(--success-bg); color: var(--success-color)">
              <el-icon :size="20"><CircleCheck /></el-icon>
            </div>
          </div>
          <div class="metric-value" style="color: var(--success-color)">{{ metrics.runningChannels }}</div>
          <div class="metric-footer">
            <span class="trend up">
              <el-icon><TrendCharts /></el-icon>
              98.5% 可用率
            </span>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="card-shadow metric-card danger">
          <div class="metric-header">
            <span class="metric-label">异常通道</span>
            <div class="metric-icon" style="background: var(--danger-bg); color: var(--danger-color)">
              <el-icon :size="20"><Warning /></el-icon>
            </div>
          </div>
          <div class="metric-value" style="color: var(--danger-color)">{{ metrics.errorChannels }}</div>
          <div class="metric-footer">
            <span class="metric-sub-text" style="color: var(--danger-color)">需立即处理</span>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="card-shadow metric-card warning">
          <div class="metric-header">
            <span class="metric-label">今日交易笔数</span>
            <div class="metric-icon" style="background: var(--warning-bg); color: var(--warning-color)">
              <el-icon :size="20"><CreditCard /></el-icon>
            </div>
          </div>
          <div class="metric-value">{{ metrics.todayTransactions }}</div>
          <div class="metric-footer">
            <span class="trend up">
              <el-icon><TrendCharts /></el-icon>
              12.3%
            </span>
            <span class="compare-text">较昨日增长</span>
          </div>
        </div>
      </el-col>
    </el-row>

    <div class="card-shadow filter-card">
      <el-form :model="filterForm" class="filter-form" inline>
        <el-form-item label="通道名称">
          <el-input v-model="filterForm.name" placeholder="请输入通道名称" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="通道类型">
          <el-select v-model="filterForm.type" placeholder="全部类型" clearable style="width: 160px">
            <el-option label="第三方支付" value="thirdparty" />
            <el-option label="银行卡支付" value="bankcard" />
            <el-option label="国际支付" value="international" />
            <el-option label="数字人民币" value="digital" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filterForm.status" placeholder="全部状态" clearable style="width: 140px">
            <el-option label="正常运行" value="normal" />
            <el-option label="异常" value="error" />
            <el-option label="维护中" value="maintenance" />
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

    <div class="channel-grid">
      <div v-for="channel in filteredChannels" :key="channel.id" class="card-shadow channel-card" :class="{ disabled: channel.status === 'maintenance' }">
        <div class="channel-card-header">
          <div class="channel-logo" :style="{ background: channel.color }">
            <span class="logo-text">{{ channel.shortName }}</span>
          </div>
          <div class="channel-info">
            <div class="channel-name-row">
              <span class="channel-name">{{ channel.name }}</span>
              <el-tag size="small" effect="plain" :type="getTypeTagType(channel.type)">{{ channel.typeLabel }}</el-tag>
            </div>
            <div class="channel-status-row">
              <el-tag :type="getStatusTagType(channel.status)" size="small" effect="dark" round>
                <el-icon class="status-icon">
                  <CircleCheck v-if="channel.status === 'normal'" />
                  <CircleClose v-else-if="channel.status === 'error'" />
                  <Warning v-else />
                </el-icon>
                {{ getStatusText(channel.status) }}
              </el-tag>
              <el-switch
                v-model="channel.enabled"
                :active-text="channel.enabled ? '已启用' : '已停用'"
                :disabled="channel.status === 'maintenance'"
                @change="(val: boolean) => handleToggle(channel, val)"
              />
            </div>
          </div>
        </div>

        <div class="channel-pay-methods">
          <el-tag v-for="method in channel.payMethods" :key="method" size="small" effect="plain" class="method-tag">{{ method }}</el-tag>
        </div>

        <div class="channel-metrics">
          <div class="metric-item">
            <span class="metric-label-sm">今日成功率</span>
            <span class="metric-value-sm" :class="getRateClass(channel.successRate)">{{ channel.successRate }}%</span>
          </div>
          <div class="metric-item">
            <span class="metric-label-sm">平均延迟</span>
            <span class="metric-value-sm" :class="getLatencyClass(channel.latency)">{{ channel.latency }}ms</span>
          </div>
          <div class="metric-item">
            <span class="metric-label-sm">峰值QPS</span>
            <span class="metric-value-sm">{{ channel.qps }}</span>
          </div>
        </div>

        <div class="channel-progress">
          <el-progress
            :percentage="channel.successRate"
            :stroke-width="4"
            :show-text="false"
            :color="getProgressColor(channel.successRate)"
          />
        </div>

        <div class="channel-actions">
          <el-button type="primary" link size="small" @click="handleConfig(channel)">
            <el-icon><Setting /></el-icon>
            配置
          </el-button>
          <el-button type="primary" link size="small">
            <el-icon><Money /></el-icon>
            费率
          </el-button>
          <el-button type="primary" link size="small" :class="{ 'is-danger': channel.enabled }" @click="handleToggle(channel, !channel.enabled)">
            <el-icon><SwitchButton /></el-icon>
            {{ channel.enabled ? '停用' : '启用' }}
          </el-button>
          <el-button type="primary" link size="small">
            <el-icon><Edit /></el-icon>
            编辑
          </el-button>
        </div>
      </div>
    </div>

    <el-dialog
      v-model="configDialogVisible"
      :title="currentChannel ? currentChannel.name + ' - 通道配置' : '通道配置'"
      width="560px"
      :destroy-on-close="true"
      class="config-dialog"
    >
      <el-form :model="configForm" :rules="configRules" ref="configFormRef" label-width="100px" v-if="currentChannel">
        <el-form-item label="商户号" prop="merchantId">
          <el-input v-model="configForm.merchantId" placeholder="请输入商户号" />
        </el-form-item>
        <el-form-item label="应用ID" prop="appId">
          <el-input v-model="configForm.appId" placeholder="请输入应用ID" />
        </el-form-item>
        <el-form-item label="API密钥" prop="apiKey">
          <el-input v-model="configForm.apiKey" type="password" show-password placeholder="请输入API密钥" />
        </el-form-item>
        <el-form-item label="APIv3密钥" prop="apiV3Key">
          <el-input v-model="configForm.apiV3Key" type="password" show-password placeholder="请输入APIv3密钥（可选）" />
        </el-form-item>
        <el-form-item label="回调地址" prop="notifyUrl">
          <el-input v-model="configForm.notifyUrl" placeholder="请输入支付结果回调地址" />
        </el-form-item>
        <el-form-item label="签名方式">
          <el-select v-model="configForm.signType" style="width: 100%">
            <el-option label="MD5" value="MD5" />
            <el-option label="HMAC-SHA256" value="HMAC-SHA256" />
            <el-option label="RSA2" value="RSA2" />
            <el-option label="SM3" value="SM3" />
          </el-select>
        </el-form-item>
        <el-form-item label="连接超时">
          <el-input-number v-model="configForm.connectTimeout" :min="1" :max="30" controls-position="right" />
          <span style="margin-left: 8px; color: var(--text-secondary)">秒</span>
        </el-form-item>
        <el-form-item label="费率配置">
          <el-row :gutter="12">
            <el-col :span="12">
              <el-input-number v-model="configForm.feeRate" :min="0" :max="10" :step="0.01" :precision="2" controls-position="right" style="width: 100%" />
            </el-col>
            <el-col :span="12" style="display: flex; align-items: center; color: var(--text-secondary); font-size: 13px">
              %（交易手续费率）
            </el-col>
          </el-row>
        </el-form-item>
        <el-form-item label="启用通道">
          <el-switch v-model="configForm.enabled" active-text="启用" inactive-text="停用" />
        </el-form-item>
        <el-form-item label="测试模式">
          <el-switch v-model="configForm.sandbox" active-text="沙箱环境" inactive-text="生产环境" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="configDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveConfig">
          <el-icon><Check /></el-icon>
          保存配置
        </el-button>
        <el-button type="success" @click="handleTestConnection">
          <el-icon><Connection /></el-icon>
          测试连接
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import {
  RefreshRight, Plus, Connection, Setting, Money, CreditCard,
  CircleCheck, CircleClose, Warning, SwitchButton, Edit, Search,
  Refresh, TrendCharts, Check
} from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'

const filterForm = reactive({
  name: '',
  type: '',
  status: ''
})

const metrics = ref({
  totalChannels: 8,
  runningChannels: 5,
  errorChannels: 1,
  todayTransactions: '86,429',
  payMethods: 18
})

interface Channel {
  id: number
  name: string
  shortName: string
  type: string
  typeLabel: string
  color: string
  status: 'normal' | 'error' | 'maintenance'
  enabled: boolean
  payMethods: string[]
  successRate: number
  latency: number
  qps: number
}

const channels = ref<Channel[]>([
  {
    id: 1, name: '微信支付', shortName: '微', type: 'thirdparty', typeLabel: '第三方支付',
    color: '#07C160', status: 'normal', enabled: true,
    payMethods: ['JSAPI', 'Native', 'H5', 'APP', '小程序'],
    successRate: 99.89, latency: 32, qps: 1256
  },
  {
    id: 2, name: '支付宝', shortName: '支', type: 'thirdparty', typeLabel: '第三方支付',
    color: '#1677FF', status: 'normal', enabled: true,
    payMethods: ['电脑网站', '手机网站', 'APP', 'H5', '预授权'],
    successRate: 99.92, latency: 28, qps: 1089
  },
  {
    id: 3, name: '银联支付', shortName: '银', type: 'bankcard', typeLabel: '银行卡支付',
    color: '#E60012', status: 'error', enabled: true,
    payMethods: ['银联在线', '网关支付', '代收付'],
    successRate: 95.23, latency: 156, qps: 425
  },
  {
    id: 4, name: 'Visa/MC', shortName: 'V', type: 'international', typeLabel: '国际支付',
    color: '#1A1F71', status: 'normal', enabled: true,
    payMethods: ['Visa', 'MasterCard', 'JCB', 'AE'],
    successRate: 99.21, latency: 186, qps: 234
  },
  {
    id: 5, name: 'Apple Pay', shortName: 'A', type: 'thirdparty', typeLabel: '第三方支付',
    color: '#000000', status: 'normal', enabled: true,
    payMethods: ['Apple Pay Web', 'Apple Pay App'],
    successRate: 99.68, latency: 45, qps: 156
  },
  {
    id: 6, name: '云闪付', shortName: '云', type: 'bankcard', typeLabel: '银行卡支付',
    color: '#E60012', status: 'maintenance', enabled: false,
    payMethods: ['云闪付APP', '二维码支付'],
    successRate: 0, latency: 0, qps: 0
  },
  {
    id: 7, name: '数字人民币', shortName: '数', type: 'digital', typeLabel: '数字人民币',
    color: '#D4382F', status: 'normal', enabled: true,
    payMethods: ['数字人民币APP', '硬钱包'],
    successRate: 99.95, latency: 38, qps: 89
  },
  {
    id: 8, name: 'PayPal', shortName: 'P', type: 'international', typeLabel: '国际支付',
    color: '#003087', status: 'maintenance', enabled: false,
    payMethods: ['PayPal Checkout', 'PayPal Pro', 'Venmo'],
    successRate: 0, latency: 0, qps: 0
  }
])

const filteredChannels = computed(() => {
  return channels.value.filter(ch => {
    if (filterForm.name && !ch.name.includes(filterForm.name)) return false
    if (filterForm.type && ch.type !== filterForm.type) return false
    if (filterForm.status && ch.status !== filterForm.status) return false
    return true
  })
})

const configDialogVisible = ref(false)
const currentChannel = ref<Channel | null>(null)
const configFormRef = ref<FormInstance>()

const configForm = reactive({
  merchantId: '',
  appId: '',
  apiKey: '',
  apiV3Key: '',
  notifyUrl: '',
  signType: 'RSA2',
  connectTimeout: 10,
  feeRate: 0.6,
  enabled: true,
  sandbox: false
})

const configRules: FormRules = {
  merchantId: [{ required: true, message: '请输入商户号', trigger: 'blur' }],
  appId: [{ required: true, message: '请输入应用ID', trigger: 'blur' }],
  apiKey: [{ required: true, message: '请输入API密钥', trigger: 'blur' }],
  notifyUrl: [{ required: true, message: '请输入回调地址', trigger: 'blur' }]
}

const handleSearch = () => {}

const handleReset = () => {
  filterForm.name = ''
  filterForm.type = ''
  filterForm.status = ''
}

const getTypeTagType = (type: string) => {
  const map: Record<string, string> = { thirdparty: 'primary', bankcard: 'danger', international: '', digital: 'warning' }
  return map[type] || ''
}

const getStatusTagType = (status: string) => {
  const map: Record<string, string> = { normal: 'success', error: 'danger', maintenance: 'info' }
  return map[status] || 'info'
}

const getStatusText = (status: string) => {
  const map: Record<string, string> = { normal: '正常运行', error: '异常', maintenance: '维护中' }
  return map[status] || '未知'
}

const getRateClass = (rate: number) => {
  if (rate >= 99) return 'success'
  if (rate >= 97) return 'warning'
  return 'danger'
}

const getLatencyClass = (latency: number) => {
  if (latency <= 50) return 'success'
  if (latency <= 100) return 'warning'
  return 'danger'
}

const getProgressColor = (rate: number) => {
  if (rate >= 99) return '#00B42A'
  if (rate >= 97) return '#FF7D00'
  return '#F53F3F'
}

const handleToggle = (channel: Channel, val: boolean) => {
  channel.enabled = val
}

const handleConfig = (channel: Channel) => {
  currentChannel.value = channel
  configForm.merchantId = 'MCH' + String(channel.id).padStart(6, '0')
  configForm.appId = channel.id <= 2 ? 'wx' + String(Date.now()).slice(-10) : 'APP' + String(channel.id).padStart(8, '0')
  configForm.apiKey = '****************************************'
  configForm.apiV3Key = ''
  configForm.notifyUrl = 'https://api.example.com/notify/' + channel.name.toLowerCase().replace(/[^a-z]/g, '')
  configForm.signType = channel.type === 'international' ? 'HMAC-SHA256' : channel.id <= 2 ? 'RSA2' : 'MD5'
  configForm.connectTimeout = 10
  configForm.feeRate = channel.id === 1 ? 0.6 : channel.id === 2 ? 0.55 : channel.type === 'international' ? 2.5 : 0.65
  configForm.enabled = channel.enabled
  configForm.sandbox = false
  configDialogVisible.value = true
}

const handleSaveConfig = async () => {
  if (!configFormRef.value) return
  await configFormRef.value.validate((valid) => {
    if (valid) {
      configDialogVisible.value = false
    }
  })
}

const handleTestConnection = () => {}
</script>

<style lang="scss" scoped>
.channel-page {
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
  }
}

.metric-cards {
  margin-bottom: 16px;
}

.metric-card {
  padding: 20px;

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

  .metric-sub-text {
    color: var(--text-secondary);
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

.filter-card {
  padding: 20px;
  margin-bottom: 20px;

  .filter-form {
    .el-form-item {
      margin-bottom: 0;
      margin-right: 0;
    }
  }
}

.channel-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.channel-card {
  padding: 20px;
  transition: all var(--transition-fast);
  position: relative;
  overflow: hidden;

  &:hover {
    transform: translateY(-2px);
    box-shadow: var(--shadow-lg);
  }

  &.disabled {
    opacity: 0.7;
  }

  &.disabled::after {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: repeating-linear-gradient(
      -45deg,
      transparent,
      transparent 10px,
      rgba(0, 0, 0, 0.02) 10px,
      rgba(0, 0, 0, 0.02) 20px
    );
    pointer-events: none;
  }
}

.channel-card-header {
  display: flex;
  gap: 14px;
  margin-bottom: 16px;
}

.channel-logo {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  .logo-text {
    color: #fff;
    font-size: 22px;
    font-weight: 700;
  }
}

.channel-info {
  flex: 1;
  min-width: 0;
}

.channel-name-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.channel-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.channel-status-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;

  .status-icon {
    margin-right: 2px;
    vertical-align: middle;
  }
}

.channel-pay-methods {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--border-light);

  .method-tag {
    font-size: 12px;
    border-radius: var(--radius-sm);
  }
}

.channel-metrics {
  display: flex;
  gap: 24px;
  margin-bottom: 12px;
}

.metric-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.metric-label-sm {
  font-size: 12px;
  color: var(--text-secondary);
}

.metric-value-sm {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
  font-family: 'SF Mono', Monaco, 'Courier New', monospace;

  &.success { color: var(--success-color); }
  &.warning { color: var(--warning-color); }
  &.danger { color: var(--danger-color); }
}

.channel-progress {
  margin-bottom: 16px;
}

.channel-actions {
  display: flex;
  gap: 4px;
  padding-top: 16px;
  border-top: 1px solid var(--border-light);

  .el-button {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 13px;
  }

  .el-button.is-danger {
    color: var(--danger-color);
  }
}

.config-dialog {
  :deep(.el-dialog__body) {
    padding-top: 10px;
  }

  .el-form-item {
    margin-bottom: 18px;
  }
}
</style>
