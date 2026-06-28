<template>
  <div class="page-container route-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">支付路由规则</h2>
        <p class="page-desc">智能分配支付通道，优化成功率与成本</p>
      </div>
      <div class="header-actions">
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

    <div class="card-shadow table-card">
      <div class="table-header">
        <div class="table-info">
          <el-icon><Sort /></el-icon>
          共 <span class="highlight">{{ tableData.length }}</span> 条规则，按优先级从高到低匹配
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
        <el-table-column prop="name" label="规则名称" min-width="160">
          <template #default="{ row }">
            <div class="rule-name">
              <el-icon><Promotion /></el-icon>
              <span>{{ row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="适用商户" width="140">
          <template #default="{ row }">
            <el-tag v-if="row.isAllMerchants" type="info" size="small">全部商户</el-tag>
            <el-tooltip v-else :content="row.merchantName" placement="top">
              <el-tag type="primary" size="small">指定商户</el-tag>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column label="通道条件" min-width="220">
          <template #default="{ row }">
            <div class="condition-list">
              <div class="condition-item">
                <span class="condition-label">支付方式：</span>
                <span class="condition-value">{{ row.payMethodsText }}</span>
              </div>
              <div class="condition-item" v-if="row.minAmount > 0 || row.maxAmount < 999999">
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
        <el-table-column label="目标通道" min-width="200">
          <template #default="{ row }">
            <div class="channel-target">
              <el-tag size="small" class="route-type-tag" :type="getRouteTypeTag(row.routeType)">
                {{ getRouteTypeText(row.routeType) }}
              </el-tag>
              <div class="channel-list">
                <div v-for="(channel, idx) in row.channels" :key="idx" class="channel-item">
                  <span class="channel-name">{{ channel.name }}</span>
                  <span v-if="row.routeType === 'weight'" class="channel-weight">{{ channel.weight }}%</span>
                </div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-switch v-model="row.status" active-value="active" inactive-value="inactive" @change="handleStatusChange(row)" />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row, $index }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">
              <el-icon><Edit /></el-icon>编辑
            </el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">
              <el-icon><Delete /></el-icon>删除
            </el-button>
            <el-button type="primary" link size="small" :disabled="$index === 0" @click="moveUp($index)">
              <el-icon><ArrowUp /></el-icon>上移
            </el-button>
            <el-button type="primary" link size="small" :disabled="$index === tableData.length - 1" @click="moveDown($index)">
              <el-icon><ArrowDown /></el-icon>下移
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑路由规则' : '新增路由规则'" width="700px" destroy-on-close>
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="110px">
        <el-form-item label="规则名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入规则名称" />
        </el-form-item>
        <el-form-item label="适用商户" prop="isAllMerchants">
          <el-radio-group v-model="formData.isAllMerchants">
            <el-radio :value="true">全部商户</el-radio>
            <el-radio :value="false">指定商户</el-radio>
          </el-radio-group>
          <el-select
            v-if="!formData.isAllMerchants"
            v-model="formData.merchantId"
            placeholder="请选择商户"
            style="width: 240px; margin-left: 12px"
          >
            <el-option label="星辰电商平台" value="M100001" />
            <el-option label="云海餐饮连锁" value="M100002" />
            <el-option label="智学在线教育" value="M100003" />
            <el-option label="速达出行科技" value="M100004" />
          </el-select>
        </el-form-item>
        <el-form-item label="支付方式" prop="payMethods">
          <el-checkbox-group v-model="formData.payMethods">
            <el-checkbox label="wechat">微信支付</el-checkbox>
            <el-checkbox label="alipay">支付宝</el-checkbox>
            <el-checkbox label="unionpay">银联支付</el-checkbox>
          </el-checkbox-group>
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
        <el-form-item label="路由方式" prop="routeType">
          <el-radio-group v-model="formData.routeType">
            <el-radio label="priority">优先级</el-radio>
            <el-radio label="weight">权重</el-radio>
            <el-radio label="roundrobin">轮询</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="目标通道">
          <div class="channel-config">
            <div v-for="(channel, idx) in formData.channels" :key="idx" class="channel-config-item">
              <el-select v-model="channel.id" placeholder="选择通道" style="width: 200px" @change="onChannelChange(idx, $event)">
                <el-option label="微信支付-直连" value="wx1" />
                <el-option label="微信支付-服务商" value="wx2" />
                <el-option label="支付宝-直连" value="ali1" />
                <el-option label="支付宝-服务商" value="ali2" />
                <el-option label="银联商务" value="union1" />
              </el-select>
              <el-input-number
                v-if="formData.routeType === 'weight'"
                v-model="channel.weight"
                :min="1"
                :max="100"
                controls-position="right"
                style="width: 120px; margin-left: 12px"
              />
              <span v-if="formData.routeType === 'weight'" style="margin-left: 8px">%</span>
              <el-button type="danger" link style="margin-left: 12px" @click="removeChannel(idx)" v-if="formData.channels.length > 1">
                <el-icon><Delete /></el-icon>移除
              </el-button>
            </div>
            <el-button type="primary" link @click="addChannel" style="margin-top: 8px">
              <el-icon><Plus /></el-icon>添加通道
            </el-button>
            <div v-if="formData.routeType === 'weight'" class="weight-tip">
              <span :class="{ 'weight-error': totalWeight !== 100 }">当前权重合计：{{ totalWeight }}%</span>
              <span style="color: var(--text-secondary); margin-left: 12px">需等于100%</span>
            </div>
          </div>
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
import { ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Edit, Delete, ArrowUp, ArrowDown, Sort, Connection, Promotion } from '@element-plus/icons-vue'

const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()

const formData = reactive({
  id: null as number | null,
  name: '',
  isAllMerchants: true,
  merchantId: '',
  merchantName: '',
  payMethods: [] as string[],
  minAmount: 0,
  maxAmount: 999999,
  timeRange: [] as string[],
  timeStart: '',
  timeEnd: '',
  routeType: 'weight',
  channels: [] as Array<{ id: string; name: string; weight: number }>
})

const formRules: FormRules = {
  name: [{ required: true, message: '请输入规则名称', trigger: 'blur' }],
  payMethods: [{ required: true, type: 'array', message: '请选择支付方式', trigger: 'change' }],
  routeType: [{ required: true, message: '请选择路由方式', trigger: 'change' }]
}

const merchantMap: Record<string, string> = {
  'M100001': '星辰电商平台',
  'M100002': '云海餐饮连锁',
  'M100003': '智学在线教育',
  'M100004': '速达出行科技'
}

const channelMap: Record<string, string> = {
  'wx1': '微信支付-直连',
  'wx2': '微信支付-服务商',
  'ali1': '支付宝-直连',
  'ali2': '支付宝-服务商',
  'union1': '银联商务'
}

const tableData = ref([
  {
    id: 1,
    priority: 1,
    name: '大额交易优先通道',
    isAllMerchants: false,
    merchantId: 'M100001',
    merchantName: '星辰电商平台',
    payMethods: ['wechat', 'alipay'],
    payMethodsText: '微信/支付宝',
    minAmount: 10000,
    maxAmount: 999999,
    timeStart: '',
    timeEnd: '',
    routeType: 'priority',
    channels: [{ id: 'wx2', name: '微信支付-服务商', weight: 100 }],
    status: 'active',
    createTime: '2026-06-01 10:30:00'
  },
  {
    id: 2,
    priority: 2,
    name: '小额交易分流规则',
    isAllMerchants: true,
    merchantId: '',
    merchantName: '',
    payMethods: ['wechat', 'alipay'],
    payMethodsText: '微信/支付宝',
    minAmount: 0,
    maxAmount: 100,
    timeStart: '',
    timeEnd: '',
    routeType: 'weight',
    channels: [
      { id: 'wx1', name: '微信支付-直连', weight: 40 },
      { id: 'ali1', name: '支付宝-直连', weight: 60 }
    ],
    status: 'active',
    createTime: '2026-06-05 14:20:00'
  },
  {
    id: 3,
    priority: 3,
    name: '夜间支付备用通道',
    isAllMerchants: true,
    merchantId: '',
    merchantName: '',
    payMethods: ['wechat', 'alipay', 'unionpay'],
    payMethodsText: '微信/支付宝/银联',
    minAmount: 0,
    maxAmount: 999999,
    timeStart: '22:00',
    timeEnd: '06:00',
    routeType: 'roundrobin',
    channels: [
      { id: 'ali2', name: '支付宝-服务商', weight: 0 },
      { id: 'union1', name: '银联商务', weight: 0 }
    ],
    status: 'active',
    createTime: '2026-06-10 09:15:00'
  },
  {
    id: 4,
    priority: 4,
    name: '云海餐饮专属通道',
    isAllMerchants: false,
    merchantId: 'M100002',
    merchantName: '云海餐饮连锁',
    payMethods: ['wechat'],
    payMethodsText: '微信支付',
    minAmount: 0,
    maxAmount: 500,
    timeStart: '10:00',
    timeEnd: '22:00',
    routeType: 'weight',
    channels: [
      { id: 'wx1', name: '微信支付-直连', weight: 70 },
      { id: 'wx2', name: '微信支付-服务商', weight: 30 }
    ],
    status: 'active',
    createTime: '2026-06-12 16:45:00'
  },
  {
    id: 5,
    priority: 5,
    name: '银联支付兜底规则',
    isAllMerchants: true,
    merchantId: '',
    merchantName: '',
    payMethods: ['unionpay'],
    payMethodsText: '银联支付',
    minAmount: 0,
    maxAmount: 999999,
    timeStart: '',
    timeEnd: '',
    routeType: 'priority',
    channels: [{ id: 'union1', name: '银联商务', weight: 100 }],
    status: 'active',
    createTime: '2026-06-15 11:00:00'
  },
  {
    id: 6,
    priority: 6,
    name: '教育行业优惠通道',
    isAllMerchants: false,
    merchantId: 'M100003',
    merchantName: '智学在线教育',
    payMethods: ['alipay'],
    payMethodsText: '支付宝',
    minAmount: 0,
    maxAmount: 999999,
    timeStart: '',
    timeEnd: '',
    routeType: 'priority',
    channels: [{ id: 'ali2', name: '支付宝-服务商', weight: 100 }],
    status: 'inactive',
    createTime: '2026-06-18 13:30:00'
  },
  {
    id: 7,
    priority: 7,
    name: '出行高峰负载均衡',
    isAllMerchants: false,
    merchantId: 'M100004',
    merchantName: '速达出行科技',
    payMethods: ['wechat', 'alipay'],
    payMethodsText: '微信/支付宝',
    minAmount: 0,
    maxAmount: 200,
    timeStart: '07:00',
    timeEnd: '09:00',
    routeType: 'weight',
    channels: [
      { id: 'wx1', name: '微信支付-直连', weight: 30 },
      { id: 'wx2', name: '微信支付-服务商', weight: 35 },
      { id: 'ali1', name: '支付宝-直连', weight: 35 }
    ],
    status: 'active',
    createTime: '2026-06-20 08:00:00'
  },
  {
    id: 8,
    priority: 8,
    name: '默认全通道轮询',
    isAllMerchants: true,
    merchantId: '',
    merchantName: '',
    payMethods: ['wechat', 'alipay', 'unionpay'],
    payMethodsText: '全部支付方式',
    minAmount: 0,
    maxAmount: 999999,
    timeStart: '',
    timeEnd: '',
    routeType: 'roundrobin',
    channels: [
      { id: 'wx1', name: '微信支付-直连', weight: 0 },
      { id: 'ali1', name: '支付宝-直连', weight: 0 },
      { id: 'union1', name: '银联商务', weight: 0 }
    ],
    status: 'active',
    createTime: '2026-06-01 00:00:00'
  }
])

const totalWeight = computed(() => {
  return formData.channels.reduce((sum, ch) => sum + (ch.weight || 0), 0)
})

const getPayMethodsText = (methods: string[]) => {
  const map: Record<string, string> = { wechat: '微信', alipay: '支付宝', unionpay: '银联' }
  return methods.map(m => map[m]).join('/')
}

const formatAmountRange = (row: any) => {
  if (row.minAmount > 0 && row.maxAmount < 999999) return `¥${row.minAmount} - ¥${row.maxAmount}`
  if (row.minAmount > 0) return `> ¥${row.minAmount}`
  if (row.maxAmount < 999999) return `< ¥${row.maxAmount}`
  return '不限'
}

const getRouteTypeTag = (type: string) => {
  const map: Record<string, string> = { priority: '', weight: 'success', roundrobin: 'warning' }
  return map[type] || 'info'
}

const getRouteTypeText = (type: string) => {
  const map: Record<string, string> = { priority: '优先级', weight: '权重', roundrobin: '轮询' }
  return map[type] || type
}

const resetForm = () => {
  formData.id = null
  formData.name = ''
  formData.isAllMerchants = true
  formData.merchantId = ''
  formData.merchantName = ''
  formData.payMethods = []
  formData.minAmount = 0
  formData.maxAmount = 999999
  formData.timeRange = []
  formData.timeStart = ''
  formData.timeEnd = ''
  formData.routeType = 'weight'
  formData.channels = [{ id: '', name: '', weight: formData.routeType === 'weight' ? 50 : 0 }]
}

const handleAdd = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  isEdit.value = true
  formData.id = row.id
  formData.name = row.name
  formData.isAllMerchants = row.isAllMerchants
  formData.merchantId = row.merchantId
  formData.merchantName = row.merchantName
  formData.payMethods = [...row.payMethods]
  formData.minAmount = row.minAmount
  formData.maxAmount = row.maxAmount
  formData.timeStart = row.timeStart
  formData.timeEnd = row.timeEnd
  formData.timeRange = row.timeStart && row.timeEnd ? [row.timeStart, row.timeEnd] : []
  formData.routeType = row.routeType
  formData.channels = row.channels.map((ch: any) => ({ ...ch }))
  dialogVisible.value = true
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm(`确定要删除规则「${row.name}」吗？`, '删除确认', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    const idx = tableData.value.findIndex(item => item.id === row.id)
    if (idx > -1) {
      tableData.value.splice(idx, 1)
      tableData.value.forEach((item, i) => item.priority = i + 1)
      ElMessage.success('删除成功')
    }
  }).catch(() => {})
}

const moveUp = (index: number) => {
  if (index === 0) return
  const temp = tableData.value[index]
  tableData.value[index] = tableData.value[index - 1]
  tableData.value[index - 1] = temp
  tableData.value.forEach((item, i) => item.priority = i + 1)
}

const moveDown = (index: number) => {
  if (index === tableData.value.length - 1) return
  const temp = tableData.value[index]
  tableData.value[index] = tableData.value[index + 1]
  tableData.value[index + 1] = temp
  tableData.value.forEach((item, i) => item.priority = i + 1)
}

const handleStatusChange = (row: any) => {
  ElMessage.success(`规则「${row.name}」已${row.status === 'active' ? '启用' : '禁用'}`)
}

const addChannel = () => {
  formData.channels.push({ id: '', name: '', weight: formData.routeType === 'weight' ? Math.max(0, 100 - totalWeight.value) : 0 })
}

const removeChannel = (idx: number) => {
  formData.channels.splice(idx, 1)
}

const onChannelChange = (idx: number, val: string) => {
  formData.channels[idx].name = channelMap[val] || ''
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate((valid) => {
    if (!valid) return
    if (formData.routeType === 'weight' && totalWeight.value !== 100) {
      ElMessage.warning('权重合计需等于100%')
      return
    }
    if (formData.channels.some(ch => !ch.id)) {
      ElMessage.warning('请选择所有目标通道')
      return
    }
    if (formData.timeRange && formData.timeRange.length === 2) {
      formData.timeStart = formData.timeRange[0]
      formData.timeEnd = formData.timeRange[1]
    } else {
      formData.timeStart = ''
      formData.timeEnd = ''
    }
    formData.merchantName = formData.merchantId ? merchantMap[formData.merchantId] || '' : ''
    const newRule = {
      id: formData.id || Date.now(),
      priority: formData.id ? tableData.value.findIndex(r => r.id === formData.id) + 1 : tableData.value.length + 1,
      name: formData.name,
      isAllMerchants: formData.isAllMerchants,
      merchantId: formData.merchantId,
      merchantName: formData.merchantName,
      payMethods: [...formData.payMethods],
      payMethodsText: getPayMethodsText(formData.payMethods),
      minAmount: formData.minAmount,
      maxAmount: formData.maxAmount,
      timeStart: formData.timeStart,
      timeEnd: formData.timeEnd,
      routeType: formData.routeType,
      channels: formData.channels.map(ch => ({ ...ch })),
      status: 'active',
      createTime: formData.id ? tableData.value.find(r => r.id === formData.id)?.createTime : new Date().toLocaleString('zh-CN', { hour12: false }).replace(/\//g, '-')
    }
    if (formData.id) {
      const idx = tableData.value.findIndex(r => r.id === formData.id)
      if (idx > -1) {
        tableData.value[idx] = newRule as any
      }
      ElMessage.success('规则更新成功')
    } else {
      tableData.value.push(newRule as any)
      ElMessage.success('规则创建成功')
    }
    tableData.value.forEach((item, i) => item.priority = i + 1)
    dialogVisible.value = false
  })
}
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

.channel-target {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.route-type-tag {
  width: fit-content;
}

.channel-list {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.channel-item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  background: var(--bg-hover);
  padding: 2px 8px;
  border-radius: var(--radius-sm);
  font-size: 12px;

  .channel-weight {
    color: var(--primary-color);
    font-weight: 600;
  }
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

.channel-config {
  width: 100%;
}

.channel-config-item {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.weight-tip {
  margin-top: 8px;
  font-size: 13px;

  .weight-error {
    color: var(--danger-color);
    font-weight: 600;
  }
}
</style>
