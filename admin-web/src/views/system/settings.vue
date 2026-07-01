<template>
  <div class="page-container settings-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">系统配置</h2>
        <p class="page-desc">管理支付系统全局参数、支付规则与操作日志</p>
      </div>
    </div>

    <div class="card-shadow settings-card" v-loading="configLoading">
      <el-tabs v-model="activeTab" class="settings-tabs" @tab-change="handleTabChange">
        <el-tab-pane v-for="group in configGroups" :key="group.key" :name="group.key">
          <template #label>
            <span class="tab-label">
              <el-icon><component :is="group.icon" /></el-icon>
              {{ group.label }}
            </span>
          </template>
          <el-form label-width="140px" class="settings-form">
            <template v-for="item in groupItems[group.key]" :key="item.id">
              <el-form-item :label="item.description || item.configKey">
                <template v-if="isBooleanConfig(item.configKey, item.configValue)">
                  <el-switch
                    v-model="configValues[item.id]"
                    active-text="开启"
                    inactive-text="关闭"
                    :active-value="'true'"
                    :inactive-value="'false'"
                  />
                </template>
                <template v-else-if="isNumberConfig(item.configKey, item.configValue)">
                  <el-input-number
                    v-model="numberValues[item.id]"
                    controls-position="right"
                    style="width: 240px"
                  />
                </template>
                <template v-else-if="isTextareaConfig(item.configKey)">
                  <el-input
                    v-model="configValues[item.id]"
                    type="textarea"
                    :rows="4"
                    :placeholder="item.description || item.configKey"
                    style="max-width: 500px"
                  />
                </template>
                <template v-else>
                  <el-input
                    v-model="configValues[item.id]"
                    :placeholder="item.description || item.configKey"
                    style="max-width: 500px"
                    clearable
                  />
                </template>
              </el-form-item>
            </template>
            <el-form-item v-if="groupItems[group.key] && groupItems[group.key].length > 0">
              <el-button type="primary" @click="handleSaveConfig" :loading="saveLoading">
                <el-icon><Check /></el-icon>
                保存设置
              </el-button>
              <el-button @click="loadConfig">
                <el-icon><Refresh /></el-icon>
                重置
              </el-button>
            </el-form-item>
            <el-empty v-if="!groupItems[group.key] || groupItems[group.key].length === 0" description="暂无配置项" />
          </el-form>
        </el-tab-pane>

        <el-tab-pane name="logs">
          <template #label>
            <span class="tab-label">
              <el-icon><Document /></el-icon>
              操作日志
            </span>
          </template>
          <div class="log-filter">
            <el-form :model="logFilter" inline>
              <el-form-item label="操作人">
                <el-input v-model="logFilter.username" placeholder="请输入用户名" clearable style="width: 160px" />
              </el-form-item>
              <el-form-item label="操作时间">
                <el-date-picker
                  v-model="logFilter.dateRange"
                  type="datetimerange"
                  range-separator="至"
                  start-placeholder="开始时间"
                  end-placeholder="结束时间"
                  style="width: 340px"
                  value-format="YYYY-MM-DD HH:mm:ss"
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleLogSearch">
                  <el-icon><Search /></el-icon>
                  查询
                </el-button>
                <el-button @click="handleLogReset">
                  <el-icon><Refresh /></el-icon>
                  重置
                </el-button>
              </el-form-item>
            </el-form>
          </div>
          <el-table
            :data="logData"
            style="width: 100%"
            stripe
            v-loading="logLoading"
          >
            <el-table-column prop="username" label="操作人" width="120" />
            <el-table-column prop="operation" label="操作内容" min-width="200" show-overflow-tooltip />
            <el-table-column prop="method" label="请求方法" width="180">
              <template #default="{ row }">
                <span class="mono-text method-text">{{ row.method || '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="ip" label="IP地址" width="140">
              <template #default="{ row }">
                <span class="mono-text">{{ row.ip || '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="duration" label="耗时" width="100" align="right">
              <template #default="{ row }">
                <span class="duration-text">{{ row.duration ?? 0 }}ms</span>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="90" align="center">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
                  {{ row.status === 1 ? '成功' : '失败' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="操作时间" width="170">
              <template #default="{ row }">
                {{ formatDateTime(row.createdAt) }}
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination-wrapper">
            <el-pagination
              v-model:current-page="logPage"
              v-model:page-size="logPageSize"
              :page-sizes="[10, 20, 50, 100]"
              :total="logTotal"
              layout="total, sizes, prev, pager, next, jumper"
              background
              @current-change="loadLogs"
              @size-change="loadLogs"
            />
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Setting, CreditCard, Bell, Lock, Document, Check, Refresh, Search, DataBoard } from '@element-plus/icons-vue'
import { systemApi } from '@/api/system'
import type { SystemConfigItem, OperationLog } from '@/types/system'

const activeTab = ref('BASIC')
const configLoading = ref(false)
const saveLoading = ref(false)
const logLoading = ref(false)

const configGroups = [
  { key: 'BASIC', label: '基本设置', icon: Setting },
  { key: 'TRADE', label: '交易设置', icon: CreditCard },
  { key: 'NOTIFY', label: '通知设置', icon: Bell },
  { key: 'RISK', label: '风控设置', icon: Lock },
  { key: 'SETTLE', label: '结算设置', icon: DataBoard }
]

const groupItems = reactive<Record<string, SystemConfigItem[]>>({})
const configValues = reactive<Record<string, string>>({})
const numberValues = reactive<Record<string, number>>({})
const originalValues = reactive<Record<string, string>>({})

const logPage = ref(1)
const logPageSize = ref(10)
const logTotal = ref(0)
const logData = ref<OperationLog[]>([])

const logFilter = reactive({
  username: '',
  dateRange: [] as string[]
})

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

function isBooleanConfig(key: string, value: string): boolean {
  const boolKeys = ['enabled', 'switch', 'toggle', 'auto', 'test_mode', 'audit', 'verify']
  const lowerKey = key.toLowerCase()
  if (boolKeys.some(k => lowerKey.includes(k))) return true
  return value === 'true' || value === 'false'
}

function isNumberConfig(key: string, value: string): boolean {
  const numKeys = ['count', 'time', 'timeout', 'length', 'size', 'amount', 'rate', 'times', 'duration', 'cycle', 'expire', 'min', 'max', 'retry']
  const lowerKey = key.toLowerCase()
  if (numKeys.some(k => lowerKey.includes(k))) return true
  return !isNaN(Number(value)) && value !== ''
}

function isTextareaConfig(key: string): boolean {
  const textKeys = ['whitelist', 'list', 'urls', 'ips', 'receivers', 'remark', 'desc']
  const lowerKey = key.toLowerCase()
  return textKeys.some(k => lowerKey.includes(k))
}

async function loadConfig() {
  configLoading.value = true
  try {
    const res = await systemApi.getConfig()
    if (res) {
      Object.keys(groupItems).forEach(k => {
        groupItems[k] = []
      })
      Object.keys(configValues).forEach(k => delete configValues[k])
      Object.keys(numberValues).forEach(k => delete numberValues[k])
      Object.keys(originalValues).forEach(k => delete originalValues[k])

      const list: SystemConfigItem[] = res.list || []
      list.forEach(item => {
        const g = item.configGroup
        if (!groupItems[g]) groupItems[g] = []
        groupItems[g].push(item)
        configValues[item.id] = item.configValue
        originalValues[item.id] = item.configValue
        const num = Number(item.configValue)
        if (!isNaN(num)) {
          numberValues[item.id] = num
        }
      })
    }
  } catch (e) {
    console.error('Failed to load config:', e)
    ElMessage.error('加载配置失败')
  } finally {
    configLoading.value = false
  }
}

async function handleSaveConfig() {
  saveLoading.value = true
  try {
    const allItems: SystemConfigItem[] = []
    Object.keys(groupItems).forEach(gKey => {
      const items = groupItems[gKey] || []
      items.forEach(item => {
        let val = configValues[item.id]
        if (numberValues[item.id] !== undefined && isNumberConfig(item.configKey, item.configValue)) {
          val = String(numberValues[item.id])
        }
        allItems.push({
          ...item,
          configValue: val
        })
      })
    })
    await systemApi.updateConfig(allItems)
    ElMessage.success('配置保存成功')
    await loadConfig()
  } catch (e) {
    console.error('Failed to save config:', e)
    ElMessage.error('保存配置失败')
  } finally {
    saveLoading.value = false
  }
}

async function loadLogs() {
  logLoading.value = true
  try {
    const params: any = {
      page: logPage.value,
      pageSize: logPageSize.value
    }
    if (logFilter.username) params.username = logFilter.username
    if (logFilter.dateRange && logFilter.dateRange.length === 2) {
      params.startTime = logFilter.dateRange[0]
      params.endTime = logFilter.dateRange[1]
    }
    const res = await systemApi.getOperationLogs(params)
    if (res) {
      logData.value = res.list ?? []
      logTotal.value = res.total ?? 0
    }
  } catch (e) {
    console.error('Failed to load logs:', e)
    logData.value = []
    logTotal.value = 0
  } finally {
    logLoading.value = false
  }
}

const handleTabChange = (tab: string) => {
  if (tab === 'logs' && logData.value.length === 0) {
    loadLogs()
  }
}

const handleLogSearch = () => {
  logPage.value = 1
  loadLogs()
}

const handleLogReset = () => {
  logFilter.username = ''
  logFilter.dateRange = []
  logPage.value = 1
  loadLogs()
}

onMounted(() => {
  loadConfig()
})
</script>

<style lang="scss" scoped>
.settings-page {
  .page-header {
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
}

.settings-card {
  padding: 0 24px 24px;
}

.settings-tabs {
  :deep(.el-tabs__header) {
    margin-bottom: 24px;
    border-bottom: 1px solid var(--border-light);
  }

  :deep(.el-tabs__nav-wrap::after) {
    display: none;
  }

  :deep(.el-tabs__item) {
    font-size: 14px;
    padding: 0 20px;
    height: 50px;
    line-height: 50px;
    color: var(--text-regular);

    &.is-active {
      color: var(--primary-color);
      font-weight: 500;
    }
  }

  :deep(.el-tabs__active-bar) {
    background-color: var(--primary-color);
    height: 3px;
    border-radius: 2px;
  }
}

.tab-label {
  display: flex;
  align-items: center;
  gap: 6px;
}

.settings-form {
  max-width: 700px;

  .el-form-item {
    margin-bottom: 22px;
  }

  :deep(.el-form-item__label) {
    color: var(--text-regular);
    font-weight: 500;
  }
}

.log-filter {
  padding: 16px;
  background: var(--bg-hover);
  border-radius: var(--radius-md);
  margin-bottom: 16px;

  .el-form-item {
    margin-bottom: 0;
  }
}

.mono-text {
  font-family: 'SF Mono', Monaco, 'Courier New', monospace;
  font-size: 13px;
}

.method-text {
  color: var(--primary-color);
  background: var(--primary-bg);
  padding: 2px 8px;
  border-radius: 4px;
}

.duration-text {
  font-family: 'SF Mono', Monaco, 'Courier New', monospace;
  color: var(--success-color);
  font-weight: 500;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
