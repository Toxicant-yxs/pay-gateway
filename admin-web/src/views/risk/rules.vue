<template>
  <div class="page-container risk-rules-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">风控规则管理</h2>
        <p class="page-desc">配置和管理各类风险控制规则，实时监控交易安全</p>
      </div>
      <div class="header-actions">
        <el-button @click="loadData">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增规则
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
        </div>
      </el-col>
    </el-row>

    <div class="card-shadow rules-card">
      <el-tabs v-model="activeCategory" class="rule-tabs" @tab-change="handleSearch">
        <el-tab-pane label="交易风控" name="transaction">
          <template #label>
            <span class="tab-label">
              <el-icon><CreditCard /></el-icon>
              交易风控
            </span>
          </template>
        </el-tab-pane>
        <el-tab-pane label="商户风控" name="merchant">
          <template #label>
            <span class="tab-label">
              <el-icon><OfficeBuilding /></el-icon>
              商户风控
            </span>
          </template>
        </el-tab-pane>
        <el-tab-pane label="账号风控" name="account">
          <template #label>
            <span class="tab-label">
              <el-icon><User /></el-icon>
              账号风控
            </span>
          </template>
        </el-tab-pane>
        <el-tab-pane label="设备风控" name="device">
          <template #label>
            <span class="tab-label">
              <el-icon><Monitor /></el-icon>
              设备风控
            </span>
          </template>
        </el-tab-pane>
      </el-tabs>

      <div class="filter-bar">
        <div class="filter-left">
          <el-input
            v-model="filterForm.keyword"
            placeholder="搜索规则名称或编码"
            clearable
            style="width: 240px"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-select v-model="filterForm.action" placeholder="处置方式" clearable style="width: 140px">
            <el-option label="拦截" value="BLOCK" />
            <el-option label="审核" value="REVIEW" />
            <el-option label="预警" value="ALERT" />
          </el-select>
          <el-select v-model="filterForm.status" placeholder="状态" clearable style="width: 120px">
            <el-option label="已启用" :value="1" />
            <el-option label="已禁用" :value="0" />
          </el-select>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </div>
      </div>

      <el-table
        :data="tableData"
        style="width: 100%"
        stripe
        v-loading="loading"
      >
        <el-table-column prop="ruleCode" label="规则编码" width="160">
          <template #default="{ row }">
            <span class="mono-text rule-id">{{ row.ruleCode ?? row.id ?? '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="ruleName" label="规则名称" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="rule-name-cell">
              <span class="rule-name">{{ row.ruleName }}</span>
              <el-tag v-if="row.riskLevel === 'HIGH'" type="danger" size="small" effect="dark">高危</el-tag>
              <el-tag v-else-if="row.riskLevel === 'MEDIUM'" type="warning" size="small" effect="dark">中危</el-tag>
              <el-tag v-else type="info" size="small" effect="dark">低危</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="category" label="规则分类" width="120">
          <template #default="{ row }">
            <el-tag size="small" effect="plain">{{ getCategoryText(row.category) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="规则描述" min-width="260" show-overflow-tooltip />
        <el-table-column prop="action" label="处置方式" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getActionType(row.action)" size="small">
              {{ getActionText(row.action) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="90" align="center">
          <template #default="{ row }">
            <span class="priority-tag" :class="'p' + (row.priority ?? 0)">P{{ row.priority ?? 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status === 1"
              @change="(val: boolean) => handleToggleStatus(row, val)"
              active-color="var(--primary-color)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="160">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button type="primary" link size="small" @click="handleCopy(row)">
              <el-icon><Document /></el-icon>
              复制
            </el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @current-change="loadData"
          @size-change="loadData"
        />
      </div>
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑规则' : '新增规则'"
      width="680px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="ruleForm"
        :rules="formRules"
        label-width="100px"
        class="rule-form"
      >
        <el-form-item label="规则名称" prop="ruleName">
          <el-input v-model="ruleForm.ruleName" placeholder="请输入规则名称" />
        </el-form-item>
        <el-form-item label="规则分类" prop="category">
          <el-select v-model="ruleForm.category" placeholder="请选择分类" style="width: 100%">
            <el-option label="交易风控" value="transaction" />
            <el-option label="商户风控" value="merchant" />
            <el-option label="账号风控" value="account" />
            <el-option label="设备风控" value="device" />
          </el-select>
        </el-form-item>
        <el-form-item label="处置方式" prop="action">
          <el-radio-group v-model="ruleForm.action">
            <el-radio value="BLOCK">拦截</el-radio>
            <el-radio value="REVIEW">审核</el-radio>
            <el-radio value="ALERT">预警</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="风险等级" prop="riskLevel">
          <el-radio-group v-model="ruleForm.riskLevel">
            <el-radio value="HIGH">高危</el-radio>
            <el-radio value="MEDIUM">中危</el-radio>
            <el-radio value="LOW">低危</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-select v-model="ruleForm.priority" placeholder="请选择优先级" style="width: 100%">
            <el-option label="P0 - 最高" :value="0" />
            <el-option label="P1 - 高" :value="1" />
            <el-option label="P2 - 中" :value="2" />
            <el-option label="P3 - 低" :value="3" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  Refresh, Plus, Edit, Delete, Document, Search, Key,
  TrendCharts, CreditCard, OfficeBuilding, User, Monitor
} from '@element-plus/icons-vue'
import { riskApi } from '@/api/risk'

interface RiskRuleItem {
  id?: string | number
  ruleCode?: string
  ruleName?: string
  category?: string
  conditionExpr?: string
  action?: string
  riskLevel?: string
  priority?: number
  status?: number
  description?: string
  createdAt?: string
  [key: string]: any
}

const loading = ref(false)
const activeCategory = ref('transaction')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()

const metrics = ref([
  {
    label: '启用规则数',
    value: '--',
    type: '',
    icon: Key,
    iconBg: 'var(--primary-bg)',
    iconColor: 'var(--primary-color)'
  },
  {
    label: '今日拦截',
    value: '--',
    type: 'danger',
    icon: TrendCharts,
    iconBg: 'var(--danger-bg)',
    iconColor: 'var(--danger-color)'
  },
  {
    label: '拦截率',
    value: '--',
    type: 'warning',
    icon: TrendCharts,
    iconBg: 'var(--warning-bg)',
    iconColor: 'var(--warning-color)'
  },
  {
    label: '待处理事件',
    value: '--',
    type: 'success',
    icon: Document,
    iconBg: 'var(--success-bg)',
    iconColor: 'var(--success-color)'
  }
])

const tableData = ref<RiskRuleItem[]>([])

const filterForm = reactive({
  keyword: '',
  category: '',
  action: '' as 'BLOCK' | 'REVIEW' | 'ALERT' | '',
  status: '' as number | ''
})

const ruleForm = reactive({
  ruleName: '',
  category: 'transaction',
  action: 'BLOCK' as 'BLOCK' | 'REVIEW' | 'ALERT',
  riskLevel: 'MEDIUM' as 'HIGH' | 'MEDIUM' | 'LOW',
  priority: 2
})

const formRules: FormRules = {
  ruleName: [{ required: true, message: '请输入规则名称', trigger: 'blur' }],
  category: [{ required: true, message: '请选择规则分类', trigger: 'change' }],
  action: [{ required: true, message: '请选择处置方式', trigger: 'change' }],
  riskLevel: [{ required: true, message: '请选择风险等级', trigger: 'change' }],
  priority: [{ required: true, message: '请选择优先级', trigger: 'change' }]
}

function formatDate(dateStr: string | undefined | null): string {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  if (isNaN(date.getTime())) return dateStr
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

const categoryMap: Record<string, string> = {
  transaction: '交易风控',
  merchant: '商户风控',
  account: '账号风控',
  device: '设备风控',
  TRADE_LIMIT: '交易限制',
  FREQUENCY: '频次控制',
  IP_BLACKLIST: 'IP黑名单',
  DEVICE: '设备风控',
  GEO: '地理位置'
}

function getCategoryText(category: string): string {
  return categoryMap[category] ?? category
}

const getActionType = (action: string) => {
  const map: Record<string, string> = { BLOCK: 'danger', REVIEW: 'warning', ALERT: 'info', block: 'danger', review: 'warning', warn: 'info' }
  return map[action] || 'info'
}

const getActionText = (action: string) => {
  const map: Record<string, string> = { BLOCK: '拦截', REVIEW: '审核', ALERT: '预警', block: '拦截', review: '审核', warn: '预警' }
  return map[action] || action
}

const resetForm = () => {
  ruleForm.ruleName = ''
  ruleForm.category = 'transaction'
  ruleForm.action = 'BLOCK'
  ruleForm.riskLevel = 'MEDIUM'
  ruleForm.priority = 2
}

const handleAdd = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row: RiskRuleItem) => {
  isEdit.value = true
  ruleForm.ruleName = row.ruleName ?? ''
  ruleForm.category = (row.category ?? 'TRADE_LIMIT') as any
  ruleForm.action = (String(row.action ?? 'BLOCK').toUpperCase() as 'BLOCK' | 'REVIEW' | 'ALERT')
  ruleForm.riskLevel = (String(row.riskLevel ?? 'MEDIUM').toUpperCase() as 'HIGH' | 'MEDIUM' | 'LOW')
  ruleForm.priority = row.priority ?? 2
  dialogVisible.value = true
}

const handleCopy = (row: RiskRuleItem) => {
  isEdit.value = false
  ruleForm.ruleName = (row.ruleName ?? '') + ' (副本)'
  ruleForm.category = (row.category ?? 'TRADE_LIMIT') as any
  ruleForm.action = (String(row.action ?? 'BLOCK').toUpperCase() as 'BLOCK' | 'REVIEW' | 'ALERT')
  ruleForm.riskLevel = (String(row.riskLevel ?? 'MEDIUM').toUpperCase() as 'HIGH' | 'MEDIUM' | 'LOW')
  ruleForm.priority = row.priority ?? 2
  dialogVisible.value = true
  ElMessage.success('规则已复制，请修改后保存')
}

const handleDelete = (row: RiskRuleItem) => {
  ElMessageBox.confirm(
    `确定要删除规则「${row.ruleName}」吗？删除后无法恢复。`,
    '删除确认',
    {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    ElMessage.success('删除成功')
    loadData()
  }).catch(() => {})
}

const handleToggleStatus = async (row: RiskRuleItem, val: boolean) => {
  try {
    await riskApi.updateRuleStatus(String(row.ruleCode ?? row.id ?? ''), val ? 1 : 0)
    row.status = val ? 1 : 0
    ElMessage.success(`${row.ruleName} 已${val ? '启用' : '禁用'}`)
  } catch (e) {
    console.error('Failed to toggle rule status:', e)
    ElMessage.error('状态更新失败')
  }
}

async function loadData() {
  loading.value = true
  try {
    const params: any = {
      page: currentPage.value,
      pageSize: pageSize.value
    }
    if (filterForm.keyword) params.keyword = filterForm.keyword
    if (activeCategory.value) params.category = activeCategory.value.toUpperCase()
    if (filterForm.action) params.action = filterForm.action
    if (filterForm.status !== '') params.status = filterForm.status
    const res = await riskApi.getRules(params)
    if (res) {
      tableData.value = (res.list ?? []) as unknown as RiskRuleItem[]
      total.value = res.total ?? 0
      const enabledCount = tableData.value.filter(r => r.status === 1).length
      metrics.value[0].value = String(enabledCount)
    }
  } catch (e) {
    console.error('Failed to load risk rules:', e)
    tableData.value = []
    total.value = 0
    ElMessage.error('加载风控规则失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadData()
}

const handleReset = () => {
  filterForm.keyword = ''
  filterForm.category = ''
  filterForm.action = ''
  filterForm.status = ''
  currentPage.value = 1
  loadData()
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate((valid) => {
    if (valid) {
      ElMessage.success(isEdit.value ? '规则更新成功' : '规则创建成功')
      dialogVisible.value = false
      loadData()
    }
  })
}

onMounted(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
.risk-rules-page {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
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

.metric-cards {
  margin-bottom: 20px;
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
    letter-spacing: -0.5px;
  }
}

.rules-card {
  padding: 20px;
}

.rule-tabs {
  margin-bottom: 16px;

  :deep(.el-tabs__header) {
    margin: 0 0 16px 0;
    border-bottom: 1px solid var(--border-light);
  }

  .tab-label {
    display: flex;
    align-items: center;
    gap: 6px;
  }
}

.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--border-light);
}

.filter-left {
  display: flex;
  gap: 12px;
  align-items: center;
}

.mono-text {
  font-family: 'SF Mono', Monaco, 'Courier New', monospace;
}

.rule-id {
  color: var(--primary-color);
  font-size: 13px;
}

.rule-name-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.rule-name {
  font-weight: 500;
  color: var(--text-primary);
}

.priority-tag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 22px;
  border-radius: var(--radius-sm);
  font-size: 12px;
  font-weight: 600;
  font-family: 'SF Mono', Monaco, 'Courier New', monospace;

  &.p0 {
    background: var(--danger-bg);
    color: var(--danger-color);
  }

  &.p1 {
    background: var(--warning-bg);
    color: var(--warning-color);
  }

  &.p2 {
    background: var(--primary-bg);
    color: var(--primary-color);
  }

  &.p3 {
    background: var(--bg-hover);
    color: var(--text-secondary);
  }
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.rule-form {
  .condition-builder {
    width: 100%;
    border: 1px solid var(--border-color);
    border-radius: var(--radius-md);
    padding: 16px;
    background: var(--bg-page);
  }
}
</style>
