<template>
  <div class="page-container refund-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">退款订单</h2>
        <p class="page-desc">管理和审核所有退款申请订单</p>
      </div>
      <div class="header-actions">
        <el-button>
          <el-icon><Download /></el-icon>
          导出退款单
        </el-button>
      </div>
    </div>

    <div class="stats-bar card-shadow">
      <div class="stat-item" v-for="stat in statsData" :key="stat.label">
        <div class="stat-icon" :style="{ background: stat.bgColor, color: stat.color }">
          <el-icon :size="24">
            <component :is="stat.icon" />
          </el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-label">{{ stat.label }}</div>
          <div class="stat-value" :style="{ color: stat.color }">{{ stat.value }}</div>
        </div>
      </div>
    </div>

    <div class="card-shadow filter-card">
      <div class="filter-header">
        <span class="filter-title">筛选条件</span>
        <el-link type="primary" :underline="false" @click="showMoreFilter = !showMoreFilter">
          {{ showMoreFilter ? '收起' : '展开更多' }}
          <el-icon class="el-icon--right" :class="{ 'is-rotate': showMoreFilter }"><ArrowDown /></el-icon>
        </el-link>
      </div>
      <el-form :model="filterForm" class="filter-form">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-form-item label="退款单号">
              <el-input v-model="filterForm.refundNo" placeholder="请输入退款单号" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="原订单号">
              <el-input v-model="filterForm.orderNo" placeholder="请输入原订单号" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="商户号">
              <el-input v-model="filterForm.merchantId" placeholder="请输入商户号" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="退款状态">
              <el-select v-model="filterForm.status" placeholder="全部状态" clearable style="width: 100%">
                <el-option label="待审核" value="pending" />
                <el-option label="退款中" value="processing" />
                <el-option label="退款成功" value="success" />
                <el-option label="退款失败" value="failed" />
                <el-option label="已拒绝" value="rejected" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20" v-show="showMoreFilter">
          <el-col :span="6">
            <el-form-item label="支付通道">
              <el-select v-model="filterForm.channel" placeholder="全部通道" clearable style="width: 100%">
                <el-option label="微信支付" value="wechat" />
                <el-option label="支付宝" value="alipay" />
                <el-option label="银联支付" value="unionpay" />
                <el-option label="Visa/MC" value="card" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="退款原因">
              <el-select v-model="filterForm.reason" placeholder="全部原因" clearable style="width: 100%">
                <el-option label="用户申请" value="user_request" />
                <el-option label="重复支付" value="duplicate" />
                <el-option label="商品问题" value="product_issue" />
                <el-option label="其他原因" value="other" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="申请时间">
              <el-date-picker
                v-model="filterForm.dateRange"
                type="datetimerange"
                range-separator="至"
                start-placeholder="开始时间"
                end-placeholder="结束时间"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24" style="text-align: right">
            <el-button type="primary" @click="handleSearch">
              <el-icon><Search /></el-icon>
              查询
            </el-button>
            <el-button @click="handleReset">
              <el-icon><Refresh /></el-icon>
              重置
            </el-button>
          </el-col>
        </el-row>
      </el-form>
    </div>

    <div class="card-shadow table-card">
      <div class="table-header">
        <div class="table-actions">
          <el-button size="default" :disabled="selectedRows.length === 0">
            <el-icon><Download /></el-icon>
            批量导出
          </el-button>
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
        <el-table-column prop="refundNo" label="退款单号" min-width="200" fixed="left">
          <template #default="{ row }">
            <span class="link-text mono-text" @click="viewDetail(row)">{{ row.refundNo }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="orderNo" label="原订单号" min-width="180">
          <template #default="{ row }">
            <span class="mono-text">{{ row.orderNo }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="merchantName" label="商户名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="channel" label="支付通道" width="110">
          <template #default="{ row }">
            <el-tag size="small" effect="plain" :color="getChannelColor(row.channel)" style="color: #fff; border: none">
              {{ row.channel }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="refundAmount" label="退款金额" width="120" align="right">
          <template #default="{ row }">
            <span class="amount-text">CNY {{ formatNumber(row.refundAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="refundReason" label="退款原因" min-width="120" show-overflow-tooltip />
        <el-table-column prop="status" label="退款状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="applyTime" label="申请时间" width="160" />
        <el-table-column prop="finishTime" label="完成时间" width="160">
          <template #default="{ row }">
            {{ row.finishTime || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="viewDetail(row)">详情</el-button>
            <el-button type="success" link size="small" v-if="row.status === 'pending'" @click="handleApprove(row)">同意</el-button>
            <el-button type="danger" link size="small" v-if="row.status === 'pending'" @click="handleReject(row)">拒绝</el-button>
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
        />
      </div>
    </div>

    <el-drawer
      v-model="drawerVisible"
      title="退款详情"
      direction="rtl"
      size="680px"
      :destroy-on-close="true"
    >
      <div v-if="currentRefund" class="refund-detail">
        <div class="detail-status-bar" :class="currentRefund.status">
          <div class="status-icon">
            <el-icon :size="40" color="#fff">
              <CircleCheck v-if="currentRefund.status === 'success'" />
              <CircleClose v-else-if="currentRefund.status === 'failed' || currentRefund.status === 'rejected'" />
              <Clock v-else-if="currentRefund.status === 'pending' || currentRefund.status === 'processing'" />
              <Warning v-else />
            </el-icon>
          </div>
          <div class="status-info">
            <h3>{{ getStatusText(currentRefund.status) }}</h3>
            <p>退款单号：<span class="mono-text">{{ currentRefund.refundNo }}</span></p>
          </div>
        </div>

        <el-tabs v-model="activeTab" class="detail-tabs">
          <el-tab-pane label="退款信息" name="refund">
            <el-descriptions title="退款信息" :column="2" border class="info-section">
              <el-descriptions-item label="退款单号" :span="2">
                <span class="mono-text">{{ currentRefund.refundNo }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="原订单号" :span="2">
                <span class="mono-text">{{ currentRefund.orderNo }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="商户名称">{{ currentRefund.merchantName }}</el-descriptions-item>
              <el-descriptions-item label="商户号">
                <span class="mono-text">{{ currentRefund.merchantId }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="支付通道">{{ currentRefund.channel }}</el-descriptions-item>
              <el-descriptions-item label="原订单金额">
                <span class="amount-text">CNY {{ formatNumber(currentRefund.orderAmount) }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="退款金额" label-style="color: var(--danger-color)">
                <span class="amount-text" style="color: var(--danger-color)">CNY {{ formatNumber(currentRefund.refundAmount) }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="退款原因">{{ currentRefund.refundReason }}</el-descriptions-item>
              <el-descriptions-item label="申请人">{{ currentRefund.applicant }}</el-descriptions-item>
              <el-descriptions-item label="申请时间">{{ currentRefund.applyTime }}</el-descriptions-item>
              <el-descriptions-item label="完成时间">{{ currentRefund.finishTime || '-' }}</el-descriptions-item>
              <el-descriptions-item label="退款备注" :span="2">{{ currentRefund.remark || '-' }}</el-descriptions-item>
            </el-descriptions>

            <el-descriptions title="渠道信息" :column="2" border class="info-section" style="margin-top: 20px">
              <el-descriptions-item label="渠道退款号">
                <span class="mono-text">{{ currentRefund.channelRefundNo || '-' }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="渠道返回码">{{ currentRefund.channelCode || '-' }}</el-descriptions-item>
              <el-descriptions-item label="渠道返回信息" :span="2">{{ currentRefund.channelMsg || '-' }}</el-descriptions-item>
            </el-descriptions>
          </el-tab-pane>
          <el-tab-pane label="操作日志" name="logs">
            <el-timeline>
              <el-timeline-item
                v-for="(log, index) in operationLogs"
                :key="index"
                :timestamp="log.time"
                :type="log.type"
              >
                <div class="log-content">
                  <span class="log-title">{{ log.title }}</span>
                  <span class="log-operator" v-if="log.operator">操作人：{{ log.operator }}</span>
                  <span class="log-desc" v-if="log.desc">{{ log.desc }}</span>
                </div>
              </el-timeline-item>
            </el-timeline>
          </el-tab-pane>
        </el-tabs>

        <div class="detail-actions" v-if="currentRefund.status === 'pending'">
          <el-button type="success" @click="handleApprove(currentRefund)">
            <el-icon><CircleCheck /></el-icon>
            同意退款
          </el-button>
          <el-button type="danger" @click="handleReject(currentRefund)">
            <el-icon><CircleClose /></el-icon>
            拒绝退款
          </el-button>
        </div>
      </div>
    </el-drawer>

    <el-dialog v-model="rejectDialogVisible" title="拒绝退款" width="500px" destroy-on-close>
      <el-form :model="rejectForm" label-width="80px">
        <el-form-item label="拒绝原因" required>
          <el-input
            v-model="rejectForm.reason"
            type="textarea"
            :rows="4"
            placeholder="请输入拒绝原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmReject">确认拒绝</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Download, ArrowDown, Search, Refresh, RefreshLeft, Money, Warning, Document, CircleCheck, CircleClose, Clock } from '@element-plus/icons-vue'

const loading = ref(false)
const showMoreFilter = ref(false)
const drawerVisible = ref(false)
const rejectDialogVisible = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(156)
const selectedRows = ref<any[]>([])
const currentRefund = ref<any>(null)
const activeTab = ref('refund')

const filterForm = reactive({
  refundNo: '',
  orderNo: '',
  merchantId: '',
  channel: '',
  status: '',
  reason: '',
  dateRange: []
})

const rejectForm = reactive({
  reason: ''
})

const statsData = [
  { label: '今日退款笔数', value: '86', color: 'var(--primary-color)', bgColor: 'rgba(22, 119, 255, 0.1)', icon: Document },
  { label: '今日退款金额', value: '¥48,632', color: 'var(--danger-color)', bgColor: 'rgba(245, 63, 63, 0.1)', icon: Money },
  { label: '退款成功率', value: '96.8%', color: 'var(--success-color)', bgColor: 'rgba(0, 180, 42, 0.1)', icon: RefreshLeft },
  { label: '待处理退款', value: '12', color: 'var(--warning-color)', bgColor: 'rgba(255, 125, 0, 0.1)', icon: Warning }
]

const tableData = ref([
  { refundNo: 'REF20260629000100001', orderNo: 'PAY20260628000123456', merchantName: '星辰电商平台', merchantId: 'M100001', channel: '微信支付', orderAmount: 29900, refundAmount: 29900, refundReason: '用户申请退款', status: 'pending', applicant: '张三', applyTime: '2026-06-29 09:15:32', finishTime: null, remark: '用户不想要了', channelRefundNo: null, channelCode: null, channelMsg: null },
  { refundNo: 'REF20260629000100002', orderNo: 'PAY20260628000123455', merchantName: '云海餐饮连锁', merchantId: 'M100002', channel: '支付宝', orderAmount: 128000, refundAmount: 64000, refundReason: '商品问题', status: 'processing', applicant: '李四', applyTime: '2026-06-29 08:45:20', finishTime: null, remark: '部分菜品缺货', channelRefundNo: null, channelCode: null, channelMsg: null },
  { refundNo: 'REF20260629000100003', orderNo: 'PAY20260628000123453', merchantName: '智学在线教育', merchantId: 'M100003', channel: '微信支付', orderAmount: 9900, refundAmount: 9900, refundReason: '重复支付', status: 'success', applicant: '王五', applyTime: '2026-06-29 07:30:15', finishTime: '2026-06-29 07:32:08', remark: '用户不小心支付了两次', channelRefundNo: '500001234202606291234567890', channelCode: 'SUCCESS', channelMsg: '退款成功' },
  { refundNo: 'REF20260628000100004', orderNo: 'PAY20260627000123400', merchantName: '速达出行科技', merchantId: 'M100004', channel: '银联支付', orderAmount: 156000, refundAmount: 156000, refundReason: '其他原因', status: 'failed', applicant: '赵六', applyTime: '2026-06-28 16:20:45', finishTime: '2026-06-28 16:25:33', remark: '渠道账户余额不足', channelRefundNo: '2026062812345678', channelCode: 'INSUFFICIENT_BALANCE', channelMsg: '退款账户余额不足' },
  { refundNo: 'REF20260628000100005', orderNo: 'PAY20260627000123401', merchantName: '趣玩数字娱乐', merchantId: 'M100005', channel: '支付宝', orderAmount: 45600, refundAmount: 45600, refundReason: '用户申请退款', status: 'rejected', applicant: '钱七', applyTime: '2026-06-28 14:10:22', finishTime: '2026-06-28 14:30:18', remark: '游戏道具已使用，不符合退款条件', channelRefundNo: null, channelCode: null, channelMsg: null },
  { refundNo: 'REF20260628000100006', orderNo: 'PAY20260627000123402', merchantName: '某跨境电商', merchantId: 'M100007', channel: 'Visa/MC', orderAmount: 56800, refundAmount: 28400, refundReason: '商品问题', status: 'success', applicant: 'John', applyTime: '2026-06-28 11:05:33', finishTime: '2026-06-28 11:10:45', remark: '商品有破损，部分退款', channelRefundNo: 'REF2026062812345', channelCode: '0000', channelMsg: 'Refund Success' },
  { refundNo: 'REF20260628000100007', orderNo: 'PAY20260627000123403', merchantName: '星辰电商平台', merchantId: 'M100001', channel: '微信支付', orderAmount: 19900, refundAmount: 19900, refundReason: '用户申请退款', status: 'pending', applicant: '孙八', applyTime: '2026-06-28 10:30:00', finishTime: null, remark: '7天无理由退款', channelRefundNo: null, channelCode: null, channelMsg: null },
  { refundNo: 'REF20260628000100008', orderNo: 'PAY20260626000123350', merchantName: '云海餐饮连锁', merchantId: 'M100002', channel: '支付宝', orderAmount: 35600, refundAmount: 35600, refundReason: '重复支付', status: 'success', applicant: '周九', applyTime: '2026-06-28 09:15:28', finishTime: '2026-06-28 09:18:52', remark: '网络问题导致重复扣款', channelRefundNo: '2026062822001234567890', channelCode: '10000', channelMsg: 'Success' },
  { refundNo: 'REF20260628000100009', orderNo: 'PAY20260626000123351', merchantName: '智学在线教育', merchantId: 'M100003', channel: '微信支付', orderAmount: 29900, refundAmount: 29900, refundReason: '其他原因', status: 'processing', applicant: '吴十', applyTime: '2026-06-28 08:00:15', finishTime: null, remark: '课程内容与描述不符', channelRefundNo: null, channelCode: null, channelMsg: null },
  { refundNo: 'REF20260627000100010', orderNo: 'PAY20260626000123352', merchantName: '康美医疗健康', merchantId: 'M100006', channel: '微信支付', orderAmount: 89900, refundAmount: 89900, refundReason: '用户申请退款', status: 'success', applicant: '郑十一', applyTime: '2026-06-27 20:45:30', finishTime: '2026-06-27 20:50:22', remark: '服务预约取消', channelRefundNo: '4200001234202606271234567890', channelCode: 'SUCCESS', channelMsg: '退款成功' }
])

const operationLogs = ref([
  { time: '2026-06-29 09:15:32', title: '提交退款申请', operator: '张三', type: 'primary' },
  { time: '2026-06-29 09:16:00', title: '系统自动校验', desc: '订单信息校验通过', type: 'success' }
])

const formatNumber = (num: number) => (num / 100).toLocaleString('zh-CN', { minimumFractionDigits: 2 })

const getChannelColor = (channel: string) => {
  const map: Record<string, string> = { '微信支付': '#07C160', '支付宝': '#1677FF', '银联支付': '#E60012', 'Visa/MC': '#1A1F71' }
  return map[channel] || 'var(--primary-color)'
}

const getStatusType = (status: string) => {
  const map: Record<string, string> = { success: 'success', pending: 'warning', processing: '', failed: 'danger', rejected: 'info' }
  return map[status] || 'info'
}

const getStatusText = (status: string) => {
  const map: Record<string, string> = { success: '退款成功', pending: '待审核', processing: '退款中', failed: '退款失败', rejected: '已拒绝' }
  return map[status] || '未知'
}

const handleSearch = () => {
  loading.value = true
  setTimeout(() => loading.value = false, 500)
}

const handleReset = () => {
  Object.keys(filterForm).forEach(key => {
    (filterForm as any)[key] = key === 'dateRange' ? [] : ''
  })
}

const handleSelectionChange = (rows: any[]) => {
  selectedRows.value = rows
}

const viewDetail = (row: any) => {
  currentRefund.value = row
  drawerVisible.value = true
  activeTab.value = 'refund'
  generateLogs(row)
}

const generateLogs = (row: any) => {
  const logs: any[] = [
    { time: row.applyTime, title: '提交退款申请', operator: row.applicant, type: 'primary' },
    { time: row.applyTime, title: '系统自动校验', desc: '订单信息校验通过', type: 'success' }
  ]
  if (row.status === 'processing') {
    logs.unshift({ time: row.applyTime, title: '审核通过，发起退款', operator: '系统管理员', type: 'success' })
  }
  if (row.status === 'success') {
    logs.unshift({ time: row.finishTime, title: '退款成功', desc: '渠道返回退款成功', type: 'success' })
    logs.unshift({ time: row.applyTime, title: '审核通过，发起退款', operator: '系统管理员', type: 'success' })
  }
  if (row.status === 'failed') {
    logs.unshift({ time: row.finishTime, title: '退款失败', desc: row.remark, type: 'danger' })
    logs.unshift({ time: row.applyTime, title: '审核通过，发起退款', operator: '系统管理员', type: 'success' })
  }
  if (row.status === 'rejected') {
    logs.unshift({ time: row.finishTime, title: '退款被拒绝', desc: row.remark, operator: '系统管理员', type: 'danger' })
  }
  operationLogs.value = logs.reverse()
}

const handleApprove = (row: any) => {
  ElMessageBox.confirm('确定要同意该退款申请吗？同意后将立即发起退款。', '确认同意', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    ElMessage.success('已同意退款申请，正在发起退款')
    if (drawerVisible.value) {
      drawerVisible.value = false
    }
  }).catch(() => {})
}

const handleReject = (row: any) => {
  currentRefund.value = row
  rejectForm.reason = ''
  rejectDialogVisible.value = true
}

const confirmReject = () => {
  if (!rejectForm.reason.trim()) {
    ElMessage.warning('请输入拒绝原因')
    return
  }
  ElMessage.success('已拒绝退款申请')
  rejectDialogVisible.value = false
  if (drawerVisible.value) {
    drawerVisible.value = false
  }
}
</script>

<style lang="scss" scoped>
.refund-page {
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

.stats-bar {
  padding: 20px;
  margin-bottom: 16px;
  display: flex;
  gap: 24px;
}

.stat-item {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: var(--bg-page);
  border-radius: var(--radius-lg);

  .stat-icon {
    width: 56px;
    height: 56px;
    border-radius: var(--radius-md);
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }

  .stat-content {
    .stat-label {
      font-size: 13px;
      color: var(--text-secondary);
      margin-bottom: 6px;
    }

    .stat-value {
      font-size: 24px;
      font-weight: 700;
    }
  }
}

.filter-card {
  padding: 20px;
  margin-bottom: 16px;
}

.filter-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--border-light);
}

.filter-title {
  font-size: 15px;
  font-weight: 600;
}

.filter-form {
  .el-form-item {
    margin-bottom: 16px;
  }
}

.is-rotate {
  transform: rotate(180deg);
  transition: transform 0.3s;
}

.table-card {
  padding: 20px;
}

.table-header {
  margin-bottom: 16px;
}

.table-actions {
  display: flex;
  gap: 8px;
}

.link-text {
  color: var(--primary-color);
  cursor: pointer;

  &:hover {
    text-decoration: underline;
  }
}

.mono-text {
  font-family: 'SF Mono', Monaco, 'Courier New', monospace;
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

.refund-detail {
  padding: 0 4px;
}

.detail-status-bar {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 24px;
  border-radius: var(--radius-lg);
  margin-bottom: 24px;
  color: #fff;

  &.success { background: linear-gradient(135deg, #00B42A, #52C41A); }
  &.failed { background: linear-gradient(135deg, #F53F3F, #FF7875); }
  &.pending, &.processing { background: linear-gradient(135deg, #FF7D00, #FAAD14); }
  &.rejected { background: linear-gradient(135deg, #86909C, #C9CDD4); }

  .status-icon {
    width: 64px;
    height: 64px;
    background: rgba(255, 255, 255, 0.2);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .status-info {
    h3 {
      margin: 0 0 6px 0;
      font-size: 22px;
      font-weight: 600;
    }
    p {
      margin: 0;
      font-size: 14px;
      opacity: 0.9;
    }
  }
}

.info-section {
  margin-top: 8px;
}

.log-content {
  .log-title {
    display: block;
    font-weight: 500;
    color: var(--text-primary);
    margin-bottom: 4px;
  }

  .log-operator {
    display: block;
    font-size: 12px;
    color: var(--text-secondary);
    margin-bottom: 2px;
  }

  .log-desc {
    display: block;
    font-size: 12px;
    color: var(--text-secondary);
  }
}

.detail-actions {
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid var(--border-light);
  display: flex;
  gap: 12px;
}
</style>
