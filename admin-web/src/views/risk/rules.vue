<template>
  <div class="page-container risk-rules-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">风控规则管理</h2>
        <p class="page-desc">配置和管理各类风险控制规则，实时监控交易安全</p>
      </div>
      <div class="header-actions">
        <el-button @click="handleRefresh">
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
          <div class="metric-footer">
            <span class="trend" :class="metric.trend > 0 ? 'up' : 'down'">
              <el-icon><TrendCharts v-if="metric.trend > 0" /><Bottom v-else /></el-icon>
              {{ Math.abs(metric.trend) }}%
            </span>
            <span class="compare-text">较昨日{{ metric.trend > 0 ? '增长' : '下降' }}</span>
          </div>
        </div>
      </el-col>
    </el-row>

    <div class="card-shadow rules-card">
      <el-tabs v-model="activeCategory" class="rule-tabs">
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
            v-model="searchKeyword"
            placeholder="搜索规则名称或ID"
            clearable
            style="width: 240px"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-select v-model="filterAction" placeholder="处置方式" clearable style="width: 140px">
            <el-option label="拦截" value="block" />
            <el-option label="审核" value="review" />
            <el-option label="预警" value="warn" />
          </el-select>
          <el-select v-model="filterStatus" placeholder="状态" clearable style="width: 120px">
            <el-option label="已启用" :value="true" />
            <el-option label="已禁用" :value="false" />
          </el-select>
        </div>
      </div>

      <el-table
        :data="filteredTableData"
        style="width: 100%"
        stripe
        v-loading="loading"
      >
        <el-table-column prop="ruleId" label="规则ID" width="140">
          <template #default="{ row }">
            <span class="mono-text rule-id">{{ row.ruleId }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="ruleName" label="规则名称" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="rule-name-cell">
              <span class="rule-name">{{ row.ruleName }}</span>
              <el-tag v-if="row.riskLevel === 'high'" type="danger" size="small" effect="dark">高危</el-tag>
              <el-tag v-else-if="row.riskLevel === 'medium'" type="warning" size="small" effect="dark">中危</el-tag>
              <el-tag v-else type="info" size="small" effect="dark">低危</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="ruleType" label="规则类型" width="120">
          <template #default="{ row }">
            <el-tag size="small" effect="plain">{{ getRuleTypeText(row.ruleType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="condition" label="触发条件描述" min-width="260" show-overflow-tooltip />
        <el-table-column prop="action" label="处置方式" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getActionType(row.action)" size="small">
              {{ getActionText(row.action) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="90" align="center">
          <template #default="{ row }">
            <span class="priority-tag" :class="'p' + row.priority">P{{ row.priority }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.enabled"
              @change="handleToggleStatus(row)"
              active-color="var(--primary-color)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" width="160" />
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
          :total="filteredTableData.length"
          layout="total, sizes, prev, pager, next, jumper"
          background
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
            <el-radio value="block">拦截</el-radio>
            <el-radio value="review">审核</el-radio>
            <el-radio value="warn">预警</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="风险等级" prop="riskLevel">
          <el-radio-group v-model="ruleForm.riskLevel">
            <el-radio value="high">高危</el-radio>
            <el-radio value="medium">中危</el-radio>
            <el-radio value="low">低危</el-radio>
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
        <el-form-item label="生效时间">
          <el-date-picker
            v-model="ruleForm.effectiveTime"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="触发条件" prop="conditions">
          <div class="condition-builder">
            <div
              v-for="(cond, index) in ruleForm.conditions"
              :key="index"
              class="condition-row"
            >
              <el-select v-model="cond.field" placeholder="字段" style="width: 140px">
                <el-option label="交易金额" value="amount" />
                <el-option label="交易频次" value="frequency" />
                <el-option label="IP地址" value="ip" />
                <el-option label="地理位置" value="location" />
                <el-option label="设备指纹" value="deviceId" />
                <el-option label="商户号" value="merchantId" />
                <el-option label="用户ID" value="userId" />
                <el-option label="卡BIN" value="cardBin" />
              </el-select>
              <el-select v-model="cond.operator" placeholder="条件" style="width: 100px">
                <el-option label="大于" value="gt" />
                <el-option label="小于" value="lt" />
                <el-option label="等于" value="eq" />
                <el-option label="不等于" value="ne" />
                <el-option label="包含" value="in" />
                <el-option label="不包含" value="nin" />
              </el-select>
              <el-input v-model="cond.value" placeholder="值" style="flex: 1" />
              <el-button
                type="danger"
                link
                :icon="Delete"
                @click="removeCondition(index)"
                :disabled="ruleForm.conditions.length === 1"
              />
            </div>
            <el-button type="primary" link size="small" @click="addCondition">
              <el-icon><Plus /></el-icon>
              添加条件
            </el-button>
            <div class="logic-connector">
              <el-radio-group v-model="ruleForm.conditionLogic" size="small">
                <el-radio value="and">满足所有条件 (AND)</el-radio>
                <el-radio value="or">满足任一条件 (OR)</el-radio>
              </el-radio-group>
            </div>
          </div>
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
import { ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  Refresh, Plus, Edit, Delete, Document, Search, Warning, Key,
  TrendCharts, Bottom, CreditCard, OfficeBuilding, User, Monitor
} from '@element-plus/icons-vue'

const loading = ref(false)
const activeCategory = ref('transaction')
const searchKeyword = ref('')
const filterAction = ref('')
const filterStatus = ref<boolean | string>('')
const currentPage = ref(1)
const pageSize = ref(10)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()

const metrics = ref([
  {
    label: '启用规则数',
    value: '48',
    trend: 5.2,
    type: '',
    icon: Key,
    iconBg: 'var(--primary-bg)',
    iconColor: 'var(--primary-color)'
  },
  {
    label: '今日拦截',
    value: '326',
    trend: 12.8,
    type: 'danger',
    icon: Warning,
    iconBg: 'var(--danger-bg)',
    iconColor: 'var(--danger-color)'
  },
  {
    label: '拦截率',
    value: '2.54%',
    trend: -0.3,
    type: 'warning',
    icon: TrendCharts,
    iconBg: 'var(--warning-bg)',
    iconColor: 'var(--warning-color)'
  },
  {
    label: '待处理事件',
    value: '18',
    trend: -8.5,
    type: 'success',
    icon: Document,
    iconBg: 'var(--success-bg)',
    iconColor: 'var(--success-color)'
  }
])

interface RuleCondition {
  field: string
  operator: string
  value: string
}

interface RuleItem {
  id: number
  ruleId: string
  ruleName: string
  category: string
  ruleType: string
  condition: string
  action: string
  riskLevel: string
  priority: number
  enabled: boolean
  updateTime: string
  conditions?: RuleCondition[]
  conditionLogic?: string
  effectiveTime?: any
}

const tableData = ref<RuleItem[]>([
  { id: 1, ruleId: 'RULE-T-001', ruleName: '单笔大额交易拦截', category: 'transaction', ruleType: 'amount', condition: '单笔交易金额 > 50,000 CNY', action: 'block', riskLevel: 'high', priority: 0, enabled: true, updateTime: '2026-06-28 10:32:15' },
  { id: 2, ruleId: 'RULE-T-002', ruleName: '高频交易预警', category: 'transaction', ruleType: 'frequency', condition: '同一用户5分钟内交易次数 > 10笔', action: 'warn', riskLevel: 'medium', priority: 1, enabled: true, updateTime: '2026-06-27 16:45:22' },
  { id: 3, ruleId: 'RULE-T-003', ruleName: '夜间大额交易审核', category: 'transaction', ruleType: 'time_amount', condition: '00:00-05:00期间交易金额 > 10,000 CNY', action: 'review', riskLevel: 'medium', priority: 1, enabled: true, updateTime: '2026-06-26 09:18:33' },
  { id: 4, ruleId: 'RULE-T-004', ruleName: '黑名单卡BIN拦截', category: 'transaction', ruleType: 'cardbin', condition: '支付卡BIN在黑名单列表中', action: 'block', riskLevel: 'high', priority: 0, enabled: true, updateTime: '2026-06-28 08:22:47' },
  { id: 5, ruleId: 'RULE-M-001', ruleName: '新商户首笔大额审核', category: 'merchant', ruleType: 'new_merchant', condition: '入驻<7天的商户首笔交易 > 5,000 CNY', action: 'review', riskLevel: 'medium', priority: 2, enabled: true, updateTime: '2026-06-25 14:08:19' },
  { id: 6, ruleId: 'RULE-M-002', ruleName: '商户日交易额异常', category: 'merchant', ruleType: 'merchant_volume', condition: '商户日交易额超出历史均值300%', action: 'warn', riskLevel: 'high', priority: 0, enabled: true, updateTime: '2026-06-28 11:55:03' },
  { id: 7, ruleId: 'RULE-M-003', ruleName: '高风险行业商户拦截', category: 'merchant', ruleType: 'mcc', condition: '商户MCC码属于高风险行业（赌博、色情等）', action: 'block', riskLevel: 'high', priority: 0, enabled: false, updateTime: '2026-06-20 15:30:41' },
  { id: 8, ruleId: 'RULE-A-001', ruleName: '异地登录预警', category: 'account', ruleType: 'login_location', condition: '账号登录地与常用地距离 > 500km', action: 'warn', riskLevel: 'medium', priority: 2, enabled: true, updateTime: '2026-06-27 20:12:56' },
  { id: 9, ruleId: 'RULE-A-002', ruleName: '连续登录失败锁定', category: 'account', ruleType: 'login_fail', condition: '10分钟内连续登录失败 > 5次', action: 'block', riskLevel: 'high', priority: 1, enabled: true, updateTime: '2026-06-26 17:43:28' },
  { id: 10, ruleId: 'RULE-A-003', ruleName: '黑名单IP拦截', category: 'account', ruleType: 'ip_blacklist', condition: '请求来源IP在黑名单中', action: 'block', riskLevel: 'high', priority: 0, enabled: true, updateTime: '2026-06-28 06:05:14' },
  { id: 11, ruleId: 'RULE-D-001', ruleName: '模拟器设备拦截', category: 'device', ruleType: 'emulator', condition: '设备指纹识别为模拟器/越狱/Root设备', action: 'block', riskLevel: 'high', priority: 1, enabled: true, updateTime: '2026-06-24 13:27:09' },
  { id: 12, ruleId: 'RULE-D-002', ruleName: '设备关联多账号预警', category: 'device', ruleType: 'device_multi', condition: '同一设备24小时内关联账号数 > 3个', action: 'review', riskLevel: 'medium', priority: 2, enabled: true, updateTime: '2026-06-25 10:51:37' }
])

const filteredTableData = computed(() => {
  let data = tableData.value.filter(item => item.category === activeCategory.value)
  if (searchKeyword.value) {
    const kw = searchKeyword.value.toLowerCase()
    data = data.filter(item =>
      item.ruleName.toLowerCase().includes(kw) ||
      item.ruleId.toLowerCase().includes(kw)
    )
  }
  if (filterAction.value) {
    data = data.filter(item => item.action === filterAction.value)
  }
  if (filterStatus.value !== '') {
    data = data.filter(item => item.enabled === filterStatus.value)
  }
  return data
})

const ruleForm = reactive({
  ruleName: '',
  category: 'transaction',
  action: 'block',
  riskLevel: 'medium',
  priority: 2,
  effectiveTime: [],
  conditions: [
    { field: '', operator: '', value: '' }
  ] as RuleCondition[],
  conditionLogic: 'and'
})

const formRules: FormRules = {
  ruleName: [{ required: true, message: '请输入规则名称', trigger: 'blur' }],
  category: [{ required: true, message: '请选择规则分类', trigger: 'change' }],
  action: [{ required: true, message: '请选择处置方式', trigger: 'change' }],
  riskLevel: [{ required: true, message: '请选择风险等级', trigger: 'change' }],
  priority: [{ required: true, message: '请选择优先级', trigger: 'change' }]
}

const getRuleTypeText = (type: string) => {
  const map: Record<string, string> = {
    amount: '金额限制',
    frequency: '频次控制',
    time_amount: '时段限制',
    cardbin: '卡BIN校验',
    new_merchant: '新商户监控',
    merchant_volume: '交易量监控',
    mcc: '行业类别',
    login_location: '登录地点',
    login_fail: '登录失败',
    ip_blacklist: 'IP黑名单',
    emulator: '设备检测',
    device_multi: '设备关联'
  }
  return map[type] || type
}

const getActionType = (action: string) => {
  const map: Record<string, string> = { block: 'danger', review: 'warning', warn: 'info' }
  return map[action] || 'info'
}

const getActionText = (action: string) => {
  const map: Record<string, string> = { block: '拦截', review: '审核', warn: '预警' }
  return map[action] || action
}

const resetForm = () => {
  ruleForm.ruleName = ''
  ruleForm.category = 'transaction'
  ruleForm.action = 'block'
  ruleForm.riskLevel = 'medium'
  ruleForm.priority = 2
  ruleForm.effectiveTime = []
  ruleForm.conditions = [{ field: '', operator: '', value: '' }]
  ruleForm.conditionLogic = 'and'
}

const handleAdd = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row: RuleItem) => {
  isEdit.value = true
  ruleForm.ruleName = row.ruleName
  ruleForm.category = row.category
  ruleForm.action = row.action
  ruleForm.riskLevel = row.riskLevel
  ruleForm.priority = row.priority
  ruleForm.effectiveTime = row.effectiveTime || []
  ruleForm.conditions = row.conditions || [{ field: '', operator: '', value: '' }]
  ruleForm.conditionLogic = row.conditionLogic || 'and'
  dialogVisible.value = true
}

const handleCopy = (row: RuleItem) => {
  isEdit.value = false
  ruleForm.ruleName = row.ruleName + ' (副本)'
  ruleForm.category = row.category
  ruleForm.action = row.action
  ruleForm.riskLevel = row.riskLevel
  ruleForm.priority = row.priority
  ruleForm.effectiveTime = []
  ruleForm.conditions = [{ field: '', operator: '', value: '' }]
  ruleForm.conditionLogic = 'and'
  dialogVisible.value = true
  ElMessage.success('规则已复制，请修改后保存')
}

const handleDelete = (row: RuleItem) => {
  ElMessageBox.confirm(
    `确定要删除规则「${row.ruleName}」吗？删除后无法恢复。`,
    '删除确认',
    {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    const idx = tableData.value.findIndex(item => item.id === row.id)
    if (idx > -1) {
      tableData.value.splice(idx, 1)
    }
    ElMessage.success('删除成功')
  }).catch(() => {})
}

const handleToggleStatus = (row: RuleItem) => {
  ElMessage.success(`${row.ruleName} 已${row.enabled ? '启用' : '禁用'}`)
}

const handleRefresh = () => {
  loading.value = true
  setTimeout(() => {
    loading.value = false
    ElMessage.success('刷新成功')
  }, 500)
}

const handleSearch = () => {
  currentPage.value = 1
}

const addCondition = () => {
  ruleForm.conditions.push({ field: '', operator: '', value: '' })
}

const removeCondition = (index: number) => {
  ruleForm.conditions.splice(index, 1)
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate((valid) => {
    if (valid) {
      ElMessage.success(isEdit.value ? '规则更新成功' : '规则创建成功')
      dialogVisible.value = false
    }
  })
}
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

  .condition-row {
    display: flex;
    gap: 10px;
    align-items: center;
    margin-bottom: 12px;

    &:last-of-type {
      margin-bottom: 12px;
    }
  }

  .logic-connector {
    padding-top: 12px;
    border-top: 1px dashed var(--border-color);
    margin-top: 4px;
  }
}
</style>
