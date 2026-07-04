<template>
  <div class="page-container merchant-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">商户列表</h2>
        <p class="page-desc">管理平台所有入驻商户信息</p>
      </div>
      <div class="header-actions">
        <el-button>
          <el-icon><Upload /></el-icon>
          批量导入
        </el-button>
        <el-button type="primary" @click="showAddDialog = true">
          <el-icon><Plus /></el-icon>
          新增商户
        </el-button>
      </div>
    </div>

    <div class="card-shadow filter-card">
      <el-form :model="filterForm" inline class="filter-form">
        <el-form-item label="商户名称">
          <el-input v-model="filterForm.name" placeholder="请输入商户名称" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="商户号">
          <el-input v-model="filterForm.merchantId" placeholder="请输入商户号" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="商户状态">
          <el-select v-model="filterForm.status" placeholder="全部状态" clearable style="width: 140px">
            <el-option label="待审核" :value="0" />
            <el-option label="正常" :value="1" />
            <el-option label="已冻结" :value="2" />
            <el-option label="已注销" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="入驻时间">
          <el-date-picker
            v-model="filterForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            style="width: 260px"
          />
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
        <div class="table-actions">
          <el-button type="danger" size="default" :disabled="selectedRows.length === 0">
            <el-icon><Delete /></el-icon>
            批量删除
          </el-button>
          <el-button size="default" :disabled="selectedRows.length === 0">
            <el-icon><Download /></el-icon>
            批量导出
          </el-button>
        </div>
        <div class="table-info">
          共 <span class="highlight">{{ total }}</span> 条记录
        </div>
      </div>
      <el-table
        :data="tableData"
        style="width: 100%"
        stripe
        v-loading="loading"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column prop="merchantNo" label="商户号" width="140">
          <template #default="{ row }">
            <span class="mono-text">{{ row.merchantNo ?? row.merchantId ?? '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="merchantName" label="商户名称" min-width="200">
          <template #default="{ row }">
            <div class="merchant-cell">
              <el-avatar :size="32" :src="row.avatar" :style="{ background: row.avatarColor ?? getAvatarColor(row.merchantNo ?? row.merchantId) }">
                {{ (row.merchantName ?? row.shortName ?? row.merchantNo ?? '?').charAt(0) }}
              </el-avatar>
              <div>
                <div class="merchant-name">{{ row.merchantName ?? row.shortName ?? row.merchantNo ?? '-' }}</div>
                <div class="merchant-short">{{ row.shortName ?? '' }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="industry" label="所属行业" width="120">
          <template #default="{ row }">
            {{ getIndustryText(row.industry ?? row.industryType) }}
          </template>
        </el-table-column>
        <el-table-column prop="contactName" label="联系人" width="100">
          <template #default="{ row }">
            {{ row.contactName ?? row.contact ?? '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="contactPhone" label="联系电话" width="130">
          <template #default="{ row }">
            {{ row.contactPhone ?? row.phone ?? '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="level" label="商户等级" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small" effect="plain">{{ row.level ?? 'L1' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="feeRate" label="费率" width="100" align="right">
          <template #default="{ row }">
            {{ row.feeRate != null ? Number(row.feeRate).toFixed(2) + '%' : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="入驻时间" width="160">
          <template #default="{ row }">
            {{ row.createdAt ?? row.createTime ?? '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="viewDetail(row)">详情</el-button>
            <el-button type="primary" link size="small">编辑</el-button>
            <el-button type="warning" link size="small" v-if="row.status === 1 || row.status === 'NORMAL'">冻结</el-button>
            <el-button type="success" link size="small" v-if="row.status === 2 || row.status === 'FROZEN'">解冻</el-button>
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
          @current-change="loadData"
          @size-change="loadData"
        />
      </div>
    </div>

    <el-drawer
      v-model="drawerVisible"
      title="商户详情"
      direction="rtl"
      size="600px"
      :destroy-on-close="true"
    >
      <div v-if="currentMerchant" class="merchant-detail">
        <div class="detail-header">
          <el-avatar :size="64" :src="currentMerchant.avatar" :style="{ background: currentMerchant.avatarColor ?? getAvatarColor(currentMerchant.merchantNo ?? currentMerchant.merchantId) }">
            {{ (currentMerchant.merchantName ?? currentMerchant.shortName ?? currentMerchant.merchantNo ?? '?').charAt(0) }}
          </el-avatar>
          <div class="detail-title">
            <h3>{{ currentMerchant.merchantName ?? currentMerchant.name ?? currentMerchant.shortName ?? '-' }}</h3>
            <div class="detail-meta">
              <span class="mono-text">{{ currentMerchant.merchantNo ?? currentMerchant.merchantId ?? '-' }}</span>
              <el-tag :type="getStatusType(currentMerchant.status)" size="small" style="margin-left: 12px">
                {{ getStatusText(currentMerchant.status) }}
              </el-tag>
            </div>
          </div>
        </div>

        <el-tabs v-model="activeTab" class="detail-tabs">
          <el-tab-pane label="基本信息" name="basic">
            <el-descriptions :column="2" border class="info-descriptions">
              <el-descriptions-item label="商户简称">{{ currentMerchant.shortName ?? '-' }}</el-descriptions-item>
              <el-descriptions-item label="所属行业">{{ getIndustryText(currentMerchant.industry ?? currentMerchant.industryType) }}</el-descriptions-item>
              <el-descriptions-item label="联系人">{{ currentMerchant.contactName ?? currentMerchant.contact ?? '-' }}</el-descriptions-item>
              <el-descriptions-item label="联系电话">{{ currentMerchant.contactPhone ?? currentMerchant.phone ?? '-' }}</el-descriptions-item>
              <el-descriptions-item label="电子邮箱">{{ currentMerchant.contactEmail ?? currentMerchant.email ?? '-' }}</el-descriptions-item>
              <el-descriptions-item label="入驻时间">{{ currentMerchant.createdAt ?? currentMerchant.createTime ?? '-' }}</el-descriptions-item>
              <el-descriptions-item label="结算周期">{{ currentMerchant.settleCycle ?? '-' }}</el-descriptions-item>
              <el-descriptions-item label="商户等级">{{ currentMerchant.level ?? 'L1' }}</el-descriptions-item>
              <el-descriptions-item label="营业执照号" :span="2">
                <span class="mono-text">{{ currentMerchant.businessLicense ?? currentMerchant.licenseNo ?? '-' }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="商户地址" :span="2">{{ currentMerchant.address ?? '-' }}</el-descriptions-item>
            </el-descriptions>
          </el-tab-pane>
          <el-tab-pane label="交易统计" name="stats">
            <el-row :gutter="16">
              <el-col :span="8" v-for="stat in merchantStats" :key="stat.label">
                <div class="stat-item">
                  <div class="stat-value" :style="{ color: stat.color }">{{ stat.value }}</div>
                  <div class="stat-label">{{ stat.label }}</div>
                </div>
              </el-col>
            </el-row>
          </el-tab-pane>
          <el-tab-pane label="费率配置" name="rates">
            <el-table :data="rateConfig" style="width: 100%" size="small">
              <el-table-column prop="channel" label="支付通道" />
              <el-table-column prop="payMethod" label="支付方式" />
              <el-table-column prop="rate" label="费率" align="right">
                <template #default="{ row }">
                  <span class="rate-text">{{ row.rate }}%</span>
                </template>
              </el-table-column>
              <el-table-column prop="status" label="状态" align="center">
                <template #default="{ row }">
                  <el-tag :type="row.status === 'active' ? 'success' : 'info'" size="small">
                    {{ row.status === 'active' ? '已启用' : '已禁用' }}
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>
          <el-tab-pane label="操作日志" name="logs">
            <el-timeline>
              <el-timeline-item
                v-for="(log, index) in operationLogs"
                :key="index"
                :timestamp="log.time"
                :type="log.type"
              >
                {{ log.content }}
              </el-timeline-item>
            </el-timeline>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-drawer>

    <el-dialog v-model="showAddDialog" title="新增商户" width="600px" destroy-on-close>
      <el-form :model="addForm" label-width="100px">
        <el-form-item label="商户名称" required>
          <el-input v-model="addForm.name" placeholder="请输入商户名称" />
        </el-form-item>
        <el-form-item label="商户简称" required>
          <el-input v-model="addForm.shortName" placeholder="请输入商户简称" />
        </el-form-item>
        <el-form-item label="所属行业" required>
          <el-select v-model="addForm.industry" placeholder="请选择行业" style="width: 100%">
            <el-option label="电商零售" value="电商零售" />
            <el-option label="餐饮美食" value="餐饮美食" />
            <el-option label="教育培训" value="教育培训" />
            <el-option label="出行交通" value="出行交通" />
            <el-option label="数字娱乐" value="数字娱乐" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="联系人" required>
          <el-input v-model="addForm.contact" placeholder="请输入联系人姓名" />
        </el-form-item>
        <el-form-item label="联系电话" required>
          <el-input v-model="addForm.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="电子邮箱">
          <el-input v-model="addForm.email" placeholder="请输入电子邮箱" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="handleAddMerchant">确认提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Upload, Plus, Search, Refresh, Delete, Download } from '@element-plus/icons-vue'
import { merchantApi } from '@/api/merchant'
import type { MerchantItem } from '@/types/merchant'

const loading = ref(false)
const showAddDialog = ref(false)
const drawerVisible = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const selectedRows = ref<MerchantItem[]>([])
const currentMerchant = ref<MerchantItem | null>(null)
const activeTab = ref('basic')

const filterForm = reactive({
  name: '',
  merchantId: '',
  merchantName: '',
  merchantNo: '',
  status: '' as number | string,
  dateRange: [] as Date[] | string[]
})

const addForm = reactive({
  name: '',
  shortName: '',
  industry: '',
  contact: '',
  contactName: '',
  phone: '',
  contactPhone: '',
  email: ''
})

const avatarColors = ['#165DFF', '#00B42A', '#FF7D00', '#722ED1', '#14C9C9', '#F53F3F']

const tableData = ref<MerchantItem[]>([])

const merchantStatusMap: Record<number, { text: string; type: string }> = {
  0: { text: '待审核', type: 'warning' },
  1: { text: '正常', type: 'success' },
  2: { text: '已冻结', type: 'info' },
  3: { text: '已注销', type: 'danger' },
  4: { text: '已驳回', type: 'danger' }
}

const industryMap: Record<string, string> = {
  E_COMMERCE: '电商零售',
  CATERING: '餐饮美食',
  EDUCATION: '教育培训',
  TRANSPORT: '出行交通',
  ENTERTAINMENT: '数字娱乐',
  HEALTHCARE: '医疗健康',
  FINANCE: '金融服务',
  OTHER: '其他',
  '电商零售': '电商零售',
  '餐饮美食': '餐饮美食',
  '教育培训': '教育培训',
  '出行交通': '出行交通',
  '数字娱乐': '数字娱乐',
  '医疗健康': '医疗健康',
  '金融服务': '金融服务'
}

function getAvatarColor(merchantNo?: string): string {
  if (!merchantNo) return avatarColors[0]
  let hash = 0
  for (let i = 0; i < merchantNo.length; i++) {
    hash = merchantNo.charCodeAt(i) + ((hash << 5) - hash)
  }
  return avatarColors[Math.abs(hash) % avatarColors.length]
}

function getIndustryText(industry: string | undefined): string {
  if (!industry) return '其他'
  return industryMap[industry] ?? industry
}

const merchantStats = computed(() => [
  { label: '今日交易额', value: '¥--', color: 'var(--primary-color)' },
  { label: '今日笔数', value: '--', color: 'var(--success-color)' },
  { label: '成功率', value: '--', color: 'var(--warning-color)' },
  { label: '累计交易额', value: '¥' + formatNumber(currentMerchant.value?.totalAmount ?? 0), color: 'var(--purple-color)' },
  { label: '累计笔数', value: '--', color: 'var(--cyan-color)' },
  { label: '退款率', value: '--', color: 'var(--danger-color)' }
])

const rateConfig = [
  { channel: '微信支付', payMethod: 'JSAPI/Native', rate: 0.6, status: 'active' },
  { channel: '微信支付', payMethod: 'H5', rate: 0.6, status: 'active' },
  { channel: '支付宝', payMethod: '手机网站', rate: 0.55, status: 'active' },
  { channel: '支付宝', payMethod: '电脑网站', rate: 0.55, status: 'active' },
  { channel: '银联支付', payMethod: '云闪付', rate: 0.5, status: 'inactive' }
]

const operationLogs = [
  { time: '2026-06-28 10:30:00', content: '管理员修改了商户费率配置', type: 'primary' },
  { time: '2026-06-25 14:20:00', content: '商户更新了结算银行账户信息', type: 'success' },
  { time: '2026-06-20 09:15:00', content: '商户提交了进件申请', type: 'warning' },
  { time: '2026-06-20 11:00:00', content: '商户进件审核通过', type: 'success' }
]

const formatNumber = (num: number | undefined) => {
  if (num === null || num === undefined || isNaN(num)) return '0'
  return num.toLocaleString('zh-CN')
}

const getStatusType = (status: number | string | undefined) => {
  if (status === undefined || status === null) return 'info'
  if (typeof status === 'number') {
    return merchantStatusMap[status]?.type ?? 'info'
  }
  const statusStr = String(status).toUpperCase()
  if (statusStr === 'NORMAL' || statusStr === '1') return 'success'
  if (statusStr === 'PENDING' || statusStr === '0') return 'warning'
  if (statusStr === 'FROZEN' || statusStr === '2') return 'info'
  if (statusStr === 'DISABLED' || statusStr === 'CANCELED' || statusStr === 'REJECTED' || statusStr === '3' || statusStr === '4') return 'danger'
  return 'info'
}

const getStatusText = (status: number | string | undefined) => {
  if (status === undefined || status === null) return '未知'
  if (typeof status === 'number') {
    return merchantStatusMap[status]?.text ?? '未知'
  }
  const statusStr = String(status).toUpperCase()
  if (statusStr === 'NORMAL') return '正常'
  if (statusStr === 'PENDING') return '待审核'
  if (statusStr === 'FROZEN') return '已冻结'
  if (statusStr === 'DISABLED' || statusStr === 'CANCELED') return '已注销'
  if (statusStr === 'REJECTED') return '已驳回'
  return String(status)
}

async function loadData() {
  loading.value = true
  try {
    const params: any = {
      page: currentPage.value,
      pageSize: pageSize.value
    }
    if (filterForm.merchantName || filterForm.name) {
      params.merchantName = filterForm.merchantName || filterForm.name
    }
    if (filterForm.merchantNo || filterForm.merchantId) {
      params.merchantNo = filterForm.merchantNo || filterForm.merchantId
    }
    if (filterForm.status !== '' && filterForm.status !== undefined && filterForm.status !== null) {
      params.status = filterForm.status
    }
    if (filterForm.dateRange && filterForm.dateRange.length === 2) {
      params.startTime = filterForm.dateRange[0]
      params.endTime = filterForm.dateRange[1]
    }
    const res = await merchantApi.getList(params)
    if (res) {
      tableData.value = res.list ?? []
      total.value = res.total ?? 0
    }
  } catch (e) {
    console.error('Failed to load merchants:', e)
    tableData.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadData()
}

const handleReset = () => {
  filterForm.name = ''
  filterForm.merchantId = ''
  filterForm.merchantName = ''
  filterForm.merchantNo = ''
  filterForm.status = ''
  filterForm.dateRange = []
  currentPage.value = 1
  loadData()
}

const handleSelectionChange = (rows: MerchantItem[]) => {
  selectedRows.value = rows
}

const viewDetail = (row: MerchantItem) => {
  currentMerchant.value = row
  drawerVisible.value = true
  activeTab.value = 'basic'
}

const handleAddMerchant = () => {
  ElMessage.success('商户创建成功，待审核')
  showAddDialog.value = false
  loadData()
}

onMounted(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
.merchant-page {
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

.filter-card {
  padding: 20px 20px 0;
  margin-bottom: 16px;
}

.filter-form {
  .el-form-item {
    margin-bottom: 20px;
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

.table-actions {
  display: flex;
  gap: 8px;
}

.table-info {
  font-size: 13px;
  color: var(--text-secondary);

  .highlight {
    color: var(--primary-color);
    font-weight: 600;
  }
}

.merchant-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.merchant-name {
  font-weight: 500;
  color: var(--text-primary);
}

.merchant-short {
  font-size: 12px;
  color: var(--text-secondary);
}

.mono-text {
  font-family: 'SF Mono', Monaco, 'Courier New', monospace;
  color: var(--primary-color);
}

.amount-text {
  font-weight: 600;
  font-family: 'SF Mono', Monaco, 'Courier New', monospace;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.merchant-detail {
  padding: 0 4px;
}

.detail-header {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: var(--bg-hover);
  border-radius: var(--radius-lg);
  margin-bottom: 24px;
}

.detail-title {
  h3 {
    margin: 0 0 6px 0;
    font-size: 18px;
    font-weight: 600;
  }

  .detail-meta {
    display: flex;
    align-items: center;
  }
}

:deep(.detail-tabs) {
  .el-tabs__nav-wrap::after {
    background: var(--border-light);
  }
}

.info-descriptions {
  margin-top: 8px;
}

.stat-item {
  text-align: center;
  padding: 20px;
  background: var(--bg-hover);
  border-radius: var(--radius-lg);
  margin-bottom: 16px;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 6px;
}

.stat-label {
  font-size: 13px;
  color: var(--text-secondary);
}

.rate-text {
  font-weight: 600;
  color: var(--primary-color);
}
</style>
