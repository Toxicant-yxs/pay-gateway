<template>
  <div class="page-container route-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">支付路由规则</h2>
        <p class="page-desc">智能分配支付通道，优化成功率与成本</p>
      </div>
      <div class="header-actions">
        <el-button @click="loadData">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增路由规则
        </el-button>
      </div>
    </div>

    <div class="card-shadow strategy-card">
      <div class="strategy-header">
        <el-icon class="strategy-icon"><Connection /></el-icon>
        <span class="strategy-title">路由策略说明</span>
      </div>
      <div class="strategy-content">
        <div class="strategy-item">
          <el-tag type="primary" size="small">优先级</el-tag>
          <span>按优先级顺序匹配，命中第一条规则即执行，数字越小优先级越高</span>
        </div>
        <div class="strategy-item">
          <el-tag type="success" size="small">权重</el-tag>
          <span>多通道按权重比例分配流量，适合分流与负载均衡场景</span>
        </div>
        <div class="strategy-item">
          <el-tag type="warning" size="small">轮询</el-tag>
          <span>按顺序轮流使用通道，实现最简单的流量均分</span>
        </div>
      </div>
    </div>

    <div class="card-shadow filter-card">
      <el-form :model="filterForm" class="filter-form" inline>
        <el-form-item label="规则名称">
          <el-input v-model="filterForm.routeName" placeholder="请输入规则名称" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filterForm.status" placeholder="全部状态" clearable style="width: 140px">
            <el-option label="已启用" :value="1" />
            <el-option label="已禁用" :value="0" />
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
        <div class="table-info">
          <el-icon><Sort /></el-icon>
          共 <span class="highlight">{{ total }}</span> 条规则，按优先级从高到低匹配
        </div>
      </div>
      <el-table
        :data="tableData"
        style="width: 100%"
        stripe
        v-loading="loading"
        row-key="id"
      >
        <el-table-column prop="priority" label="优先级" width="80" align="center">
          <template #default="{ row }">
            <span class="priority-badge" :class="{ 'priority-top': row.priority === 1 }">{{ row.priority }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="routeName" label="规则名称" min-width="180">
          <template #default="{ row }">
            <div class="rule-name">
              <el-icon><Promotion /></el-icon>
              <span>{{ row.routeName ?? row.ruleName ?? '-' }}</span>
            </div>
            <div class="rule-no mono-text" v-if="row.routeNo ?? row.ruleId">{{ row.routeNo ?? row.ruleId }}</div>
          </template>
        </el-table-column>
        <el-table-column label="目标通道" width="140">
          <template #default="{ row }">
            <el-tag size="small" effect="plain">
              {{ getChannelName(row.channelCode) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="路由条件" min-width="240">
          <template #default="{ row }">
            <div class="condition-list">
              <div class="condition-item" v-if="row.payType">
                <span class="condition-label">支付方式：</span>
                <span class="condition-value">{{ getPayTypeName(row.payType) }}</span>
              </div>
              <div class="condition-item" v-if="row.minAmount !== undefined || row.maxAmount !== undefined">
                <span class="condition-label">金额区间：</span>
                <span class="condition-value">{{ formatAmountRange(row) }}</span>
              </div>
              <div class="condition-item" v-if="row.timeStart && row.timeEnd">
                <span class="condition-label">时间段：</span>
                <span class="condition-value">{{ row.timeStart }} - {{ row.timeEnd }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status === 1"
              @change="(val: boolean) => handleStatusChange(row, val)"
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
              <el-icon><Edit /></el-icon>编辑
            </el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">
              <el-icon><Delete /></el-icon>删除
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑路由规则' : '新增路由规则'" width="700px" destroy-on-close>
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="110px">
        <el-form-item label="规则名称" prop="routeName">
          <el-input v-model="formData.routeName" placeholder="请输入规则名称" />
        </el-form-item>
        <el-form-item label="支付方式" prop="payType">
          <el-select v-model="formData.payType" placeholder="请选择支付方式" style="width: 100%">
            <el-option label="微信支付" value="WECHAT" />
            <el-option label="支付宝" value="ALIPAY" />
            <el-option label="银联支付" value="UNIONPAY" />
          </el-select>
        </el-form-item>
        <el-form-item label="目标通道" prop="channelCode">
          <el-select v-model="formData.channelCode" placeholder="请选择目标通道" style="width: 100%">
            <el-option label="微信支付" value="WECHAT" />
            <el-option label="支付宝" value="ALIPAY" />
            <el-option label="银联支付" value="UNIONPAY" />
          </el-select>
        </el-form-item>
        <el-form-item label="金额范围">
          <div class="range-input">
            <el-input-number v-model="formData.minAmount" :min="0" :precision="2" placeholder="最小金额" controls-position="right" style="width: 160px" />
            <span class="range-sep">-</span>
            <el-input-number v-model="formData.maxAmount" :min="0" :precision="2" placeholder="最大金额" controls-position="right" style="width: 160px" />
            <span class="range-unit">元</span>
          </div>
        </el-form-item>
        <el-form-item label="生效时间段">
          <el-time-picker
            v-model="formData.timeRange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            format="HH:mm"
            value-format="HH:mm"
            style="width: 260px"
          />
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-input-number v-model="formData.priority" :min="1" :max="100" controls-position="right" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="formData.remark" type="textarea" :rows="2" placeholder="请输入备注（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确认保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Edit, Delete, Search, Refresh, Sort, Connection, Promotion } from '@element-plus/icons-vue'
import { channelApi } from '@/api/channel'
import type { RouteRule } from '@/types/channel'

const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const tableData = ref<RouteRule[]>([])

const filterForm = reactive({
  routeName: '',
  status: '' as number | ''
})

const formData = reactive({
  id: null as string | number | null,
  routeNo: '',
  routeName: '',
  channelId: '',
  channelCode: '' as string,
  payType: '' as string,
  minAmount: undefined as number | undefined,
  maxAmount: undefined as number | undefined,
  priority: 1,
  status: 1 as 0 | 1,
  timeRange: [] as string[],
  timeStart: '',
  timeEnd: '',
  remark: ''
})

const formRules: FormRules = {
  routeName: [{ required: true, message: '请输入规则名称', trigger: 'blur' }],
  payType: [{ required: true, message: '请选择支付方式', trigger: 'change' }],
  channelCode: [{ required: true, message: '请选择目标通道', trigger: 'change' }],
  priority: [{ required: true, message: '请输入优先级', trigger: 'blur' }]
}

const channelNameMap: Record<string, string> = {
  WECHAT: '微信支付',
  ALIPAY: '支付宝',
  UNIONPAY: '银联支付',
  wechat: '微信支付',
  alipay: '支付宝',
  unionpay: '银联支付'
}

const payTypeMap: Record<string, string> = {
  WECHAT: '微信支付',
  ALIPAY: '支付宝',
  UNIONPAY: '银联支付',
  JSAPI: 'JSAPI',
  NATIVE: 'Native',
  H5: 'H5',
  APP: 'APP'
}

function formatDate(dateStr: string | undefined | null): string {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  if (isNaN(date.getTime())) return dateStr
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

function getChannelName(code: string | undefined): string {
  if (!code) return '-'
  return channelNameMap[code] ?? code
}

function getPayTypeName(payType: string | undefined): string {
  if (!payType) return '-'
  return payTypeMap[payType] ?? payType
}

function formatAmountRange(row: any): string {
  const min = row.minAmount
  const max = row.maxAmount
  if (min !== undefined && min !== null && min > 0 && max !== undefined && max !== null && max > 0) {
    return `¥${min} - ¥${max}`
  }
  if (min !== undefined && min !== null && min > 0) return `> ¥${min}`
  if (max !== undefined && max !== null && max > 0) return `< ¥${max}`
  return '不限'
}

function resetForm() {
  formData.id = null
  formData.routeNo = ''
  formData.routeName = ''
  formData.channelId = ''
  formData.channelCode = ''
  formData.payType = ''
  formData.minAmount = undefined
  formData.maxAmount = undefined
  formData.priority = 1
  formData.status = 1
  formData.timeRange = []
  formData.timeStart = ''
  formData.timeEnd = ''
  formData.remark = ''
}

const handleAdd = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  isEdit.value = true
  formData.id = row.id ?? null
  formData.routeNo = row.routeNo ?? row.ruleId ?? ''
  formData.routeName = row.routeName ?? row.ruleName ?? ''
  formData.channelId = row.channelId ?? ''
  formData.channelCode = row.channelCode ?? ''
  formData.payType = row.payType ?? (row.conditions?.payTypes?.[0] ?? '')
  formData.minAmount = row.minAmount ?? row.conditions?.minAmount
  formData.maxAmount = row.maxAmount ?? row.conditions?.maxAmount
  formData.priority = row.priority ?? 1
  formData.status = row.status === 1 ? 1 : 0
  formData.timeStart = row.timeStart ?? row.conditions?.timeStart ?? ''
  formData.timeEnd = row.timeEnd ?? row.conditions?.timeEnd ?? ''
  formData.timeRange = (formData.timeStart && formData.timeEnd) ? [formData.timeStart, formData.timeEnd] : []
  formData.remark = row.remark ?? row.description ?? ''
  dialogVisible.value = true
}

const handleDelete = (row: any) => {
  const name = row.routeName ?? row.ruleName ?? '该规则'
  ElMessageBox.confirm(`确定要删除规则「${name}」吗？`, '删除确认', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    ElMessage.success('删除成功')
    loadData()
  }).catch(() => {})
}

const handleStatusChange = async (row: any, val: boolean) => {
  try {
    const ruleId = String(row.id ?? row.routeNo ?? row.ruleId ?? '')
    await channelApi.updateRouteRule(ruleId, { status: val ? 1 : 0 } as any)
    row.status = val ? 1 : 0
    ElMessage.success(`规则已${val ? '启用' : '禁用'}`)
  } catch (e) {
    console.error('Failed to update route status:', e)
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
    if (filterForm.routeName) params.routeName = filterForm.routeName
    if (filterForm.status !== '') params.status = filterForm.status
    const res = await channelApi.getRouteRules(params)
    if (res) {
      tableData.value = res.list ?? []
      total.value = res.total ?? 0
    }
  } catch (e) {
    console.error('Failed to load route rules:', e)
    tableData.value = []
    total.value = 0
    ElMessage.error('加载路由规则失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadData()
}

const handleReset = () => {
  filterForm.routeName = ''
  filterForm.status = ''
  currentPage.value = 1
  loadData()
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate((valid) => {
    if (!valid) return
    if (formData.timeRange && formData.timeRange.length === 2) {
      formData.timeStart = formData.timeRange[0]
      formData.timeEnd = formData.timeRange[1]
    } else {
      formData.timeStart = ''
      formData.timeEnd = ''
    }
    ElMessage.success(isEdit.value ? '规则更新成功' : '规则创建成功')
    dialogVisible.value = false
    loadData()
  })
}

onMounted(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
.route-page {
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

.strategy-card {
  padding: 16px 20px;
  margin-bottom: 16px;
}

.strategy-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;

  .strategy-icon {
    font-size: 18px;
    color: var(--primary-color);
  }

  .strategy-title {
    font-weight: 600;
    font-size: 15px;
  }
}

.strategy-content {
  display: flex;
  gap: 32px;
  flex-wrap: wrap;
}

.strategy-item {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 13px;
  color: var(--text-regular);
}

.filter-card {
  padding: 20px;
  margin-bottom: 16px;

  .filter-form {
    .el-form-item {
      margin-bottom: 0;
      margin-right: 0;
    }
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

.table-info {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--text-secondary);

  .highlight {
    color: var(--primary-color);
    font-weight: 600;
  }
}

.priority-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: var(--bg-hover);
  color: var(--text-regular);
  font-weight: 600;
  font-size: 13px;

  &.priority-top {
    background: linear-gradient(135deg, var(--primary-color), var(--primary-light));
    color: white;
  }
}

.rule-name {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;

  .el-icon {
    color: var(--primary-color);
  }
}

.rule-no {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 2px;
}

.condition-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.condition-item {
  font-size: 13px;
  line-height: 1.5;
}

.condition-label {
  color: var(--text-secondary);
}

.condition-value {
  color: var(--text-regular);
}

.mono-text {
  font-family: 'SF Mono', Monaco, 'Courier New', monospace;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.range-input {
  display: flex;
  align-items: center;
  gap: 8px;
}

.range-sep {
  color: var(--text-secondary);
}

.range-unit {
  color: var(--text-secondary);
  font-size: 13px;
}
</style>
