<template>
  <div class="page-container events-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">风险事件监控</h2>
        <p class="page-desc">实时监控平台风险事件，及时处理可疑交易行为</p>
      </div>
      <div class="header-actions">
        <el-button @click="handleRefresh">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
        <el-badge :value="pendingCount" :hidden="pendingCount === 0" class="badge-btn">
          <el-button type="warning">
            <el-icon><Bell /></el-icon>
            待处理预警
          </el-button>
        </el-badge>
      </div>
    </div>

    <div class="stats-row">
      <div class="stat-card metric-card" v-for="stat in statsCards" :key="stat.key" :class="stat.type">
        <div class="stat-icon" :style="{ background: stat.bgColor, color: stat.iconColor }">
          <el-icon><component :is="stat.icon" /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stat.value }}</div>
          <div class="stat-label">{{ stat.label }}</div>
        </div>
      </div>
    </div>

    <div class="card-shadow filter-card">
      <el-form :model="filterForm" inline class="filter-form">
        <el-form-item label="事件ID">
          <el-input v-model="filterForm.eventId" placeholder="请输入事件ID" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="风险等级">
          <el-select v-model="filterForm.riskLevel" placeholder="全部等级" clearable style="width: 120px">
            <el-option label="高风险" value="high" />
            <el-option label="中风险" value="medium" />
            <el-option label="低风险" value="low" />
          </el-select>
        </el-form-item>
        <el-form-item label="事件类型">
          <el-select v-model="filterForm.eventType" placeholder="全部类型" clearable style="width: 160px">
            <el-option label="异常交易" value="abnormal_transaction" />
            <el-option label="可疑登录" value="suspicious_login" />
            <el-option label="大额交易" value="large_amount" />
            <el-option label="频繁操作" value="frequency_operation" />
            <el-option label="黑名单命中" value="blacklist_hit" />
            <el-option label="异地交易" value="offsite_transaction" />
            <el-option label="商户异常" value="merchant_abnormal" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理状态">
          <el-select v-model="filterForm.status" placeholder="全部状态" clearable style="width: 120px">
            <el-option label="待处理" value="pending" />
            <el-option label="处理中" value="processing" />
            <el-option label="已处理" value="processed" />
            <el-option label="已忽略" value="ignored" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="filterForm.dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            style="width: 340px"
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
        <div class="table-info">
          共 <span class="highlight">{{ total }}</span> 条风险事件
        </div>
      </div>
      <el-table
        :data="tableData"
        style="width: 100%"
        stripe
        v-loading="loading"
        row-key="eventId"
      >
        <el-table-column prop="eventId" label="事件ID" width="140">
          <template #default="{ row }">
            <span class="mono-text">{{ row.eventId }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="eventTime" label="事件时间" width="160" />
        <el-table-column prop="riskLevel" label="风险等级" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getRiskTagType(row.riskLevel)" size="small" effect="dark">
              <el-icon style="margin-right: 2px"><Warning /></el-icon>
              {{ getRiskLevelText(row.riskLevel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="eventType" label="事件类型" width="120">
          <template #default="{ row }">
            {{ getEventTypeText(row.eventType) }}
          </template>
        </el-table-column>
        <el-table-column prop="relatedParty" label="关联商户/用户" min-width="160">
          <template #default="{ row }">
            <div class="related-cell">
              <div class="related-name">{{ row.relatedName }}</div>
              <div class="related-id mono-text">{{ row.relatedId }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="triggerRule" label="触发规则" width="160" show-overflow-tooltip />
        <el-table-column prop="description" label="风险描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="处理状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleProcess(row)" v-if="row.status === 'pending' || row.status === 'processing'">
              处理
            </el-button>
            <el-button type="primary" link size="small" @click="viewDetail(row)">
              <el-icon><View /></el-icon>
              详情
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
        />
      </div>
    </div>

    <el-drawer
      v-model="drawerVisible"
      :title="drawerTitle"
      direction="rtl"
      size="720px"
      :destroy-on-close="true"
    >
      <div v-if="currentEvent" class="event-detail">
        <div class="detail-header" :class="currentEvent.riskLevel">
          <div class="header-left">
            <div class="risk-badge" :class="currentEvent.riskLevel">
              <el-icon><Warning /></el-icon>
            </div>
            <div class="header-info">
              <h3>{{ getEventTypeText(currentEvent.eventType) }}</h3>
              <div class="header-meta">
                <span class="mono-text">{{ currentEvent.eventId }}</span>
                <el-divider direction="vertical" />
                <span><el-icon><Clock /></el-icon> {{ currentEvent.eventTime }}</span>
              </div>
            </div>
          </div>
          <el-tag :type="getRiskTagType(currentEvent.riskLevel)" size="large" effect="dark">
            {{ getRiskLevelText(currentEvent.riskLevel) }}
          </el-tag>
        </div>

        <el-tabs v-model="activeTab" class="detail-tabs">
          <el-tab-pane label="事件信息" name="info">
            <div class="section-title">
              <el-icon><Document /></el-icon>
              基本信息
            </div>
            <el-descriptions :column="2" border class="info-descriptions">
              <el-descriptions-item label="事件ID">
                <span class="mono-text">{{ currentEvent.eventId }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="事件时间">{{ currentEvent.eventTime }}</el-descriptions-item>
              <el-descriptions-item label="风险等级">
                <el-tag :type="getRiskTagType(currentEvent.riskLevel)" size="small" effect="dark">
                  {{ getRiskLevelText(currentEvent.riskLevel) }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="事件类型">{{ getEventTypeText(currentEvent.eventType) }}</el-descriptions-item>
              <el-descriptions-item label="关联{{ currentEvent.relatedType === 'merchant' ? '商户' : '用户' }}">
                {{ currentEvent.relatedName }}
              </el-descriptions-item>
              <el-descriptions-item label="关联ID">
                <span class="mono-text">{{ currentEvent.relatedId }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="触发规则" :span="2">{{ currentEvent.triggerRule }}</el-descriptions-item>
              <el-descriptions-item label="风险分数">
                <span class="risk-score" :class="currentEvent.riskLevel">{{ currentEvent.riskScore }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="处理状态">
                <el-tag :type="getStatusTagType(currentEvent.status)" size="small">
                  {{ getStatusText(currentEvent.status) }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="风险描述" :span="2">
                <div class="risk-desc">{{ currentEvent.description }}</div>
              </el-descriptions-item>
            </el-descriptions>

            <div class="section-title" style="margin-top: 24px">
              <el-icon><Warning /></el-icon>
              触发详情
            </div>
            <div class="trigger-detail">
              <div class="trigger-item" v-for="(item, idx) in currentEvent.triggerDetails" :key="idx">
                <div class="trigger-label">{{ item.label }}</div>
                <div class="trigger-value" :class="{ abnormal: item.abnormal }">{{ item.value }}</div>
              </div>
            </div>
          </el-tab-pane>

          <el-tab-pane label="关联交易" name="transactions">
            <el-table :data="currentEvent.relatedTransactions" style="width: 100%" size="small">
              <el-table-column prop="transactionId" label="交易流水号" width="160">
                <template #default="{ row }">
                  <span class="mono-text">{{ row.transactionId }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="amount" label="交易金额" width="120" align="right">
                <template #default="{ row }">
                  <span class="amount-text">¥{{ row.amount.toLocaleString() }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="payMethod" label="支付方式" width="100" />
              <el-table-column prop="tradeTime" label="交易时间" width="160" />
              <el-table-column prop="status" label="交易状态" width="100" align="center">
                <template #default="{ row }">
                  <el-tag :type="row.status === 'success' ? 'success' : 'danger'" size="small">
                    {{ row.status === 'success' ? '成功' : '失败' }}
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>

          <el-tab-pane label="处理记录" name="records">
            <el-timeline>
              <el-timeline-item
                v-for="(record, idx) in currentEvent.handleRecords"
                :key="idx"
                :timestamp="record.time"
                :type="record.type"
                :hollow="idx === currentEvent.handleRecords.length - 1 && currentEvent.status === 'pending'"
              >
                <div class="timeline-content">
                  <div class="timeline-title">{{ record.title }}</div>
                  <div class="timeline-operator" v-if="record.operator">操作人：{{ record.operator }}</div>
                  <div class="timeline-remark" v-if="record.remark">备注：{{ record.remark }}</div>
                </div>
              </el-timeline-item>
            </el-timeline>
          </el-tab-pane>
        </el-tabs>
      </div>

      <div class="drawer-footer" v-if="currentEvent && (currentEvent.status === 'pending' || currentEvent.status === 'processing')">
        <div class="handle-form">
          <el-input
            v-model="handleRemark"
            type="textarea"
            :rows="3"
            placeholder="请输入处理备注..."
            maxlength="200"
            show-word-limit
          />
        </div>
        <div class="handle-actions">
          <el-button type="success" @click="handleConfirmRisk">
            <el-icon><CircleCheck /></el-icon>
            确认风险
          </el-button>
          <el-button type="warning" @click="handleMarkFalse">
            <el-icon><Warning /></el-icon>
            标记误报
          </el-button>
          <el-button @click="handleAddWhitelist">
            <el-icon><Document /></el-icon>
            加入白名单
          </el-button>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Warning, Bell, CircleCheck, Search, Refresh, View, Document, Clock
} from '@element-plus/icons-vue'

const loading = ref(false)
const drawerVisible = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(12)
const currentEvent = ref<any>(null)
const activeTab = ref('info')
const handleRemark = ref('')
const drawerTitle = computed(() => currentEvent.value ? `事件详情 - ${currentEvent.value.eventId}` : '事件详情')

const pendingCount = computed(() => tableData.value.filter(i => i.status === 'pending').length)

const statsCards = computed(() => [
  { key: 'total', label: '今日风险事件', value: total.value, icon: Warning, type: 'primary', bgColor: 'var(--primary-bg)', iconColor: 'var(--primary-color)' },
  { key: 'high', label: '高风险事件', value: tableData.value.filter(i => i.riskLevel === 'high').length, icon: Warning, type: 'danger', bgColor: 'var(--danger-bg)', iconColor: 'var(--danger-color)' },
  { key: 'medium', label: '中风险事件', value: tableData.value.filter(i => i.riskLevel === 'medium').length, icon: Bell, type: 'warning', bgColor: 'var(--warning-bg)', iconColor: 'var(--warning-color)' },
  { key: 'low', label: '低风险事件', value: tableData.value.filter(i => i.riskLevel === 'low').length, icon: Warning, type: 'success', bgColor: 'var(--success-bg)', iconColor: 'var(--success-color)' },
  { key: 'processed', label: '已处理事件', value: tableData.value.filter(i => i.status === 'processed' || i.status === 'ignored').length, icon: CircleCheck, type: 'success', bgColor: 'var(--cyan-bg)', iconColor: 'var(--cyan-color)' }
])

const filterForm = reactive({
  eventId: '',
  riskLevel: '',
  eventType: '',
  status: '',
  dateRange: []
})

const tableData = ref([
  {
    eventId: 'EVT202606290001', eventTime: '2026-06-29 09:15:32', riskLevel: 'high', eventType: 'abnormal_transaction',
    relatedType: 'merchant', relatedName: '速达出行科技', relatedId: 'M100004',
    triggerRule: '单小时交易金额超阈值', riskScore: 92,
    description: '该商户单小时交易金额达580万元，远超历史均值120万元，疑似异常交易行为',
    status: 'pending',
    triggerDetails: [
      { label: '近1小时交易额', value: '¥5,800,000', abnormal: true },
      { label: '历史小时均值', value: '¥1,200,000', abnormal: false },
      { label: '偏离倍数', value: '4.8倍', abnormal: true },
      { label: '交易笔数', value: '1,286笔', abnormal: false },
      { label: '平均客单价', value: '¥4,510', abnormal: true }
    ],
    relatedTransactions: [
      { transactionId: 'TXN202606290901', amount: 98000, payMethod: '微信支付', tradeTime: '2026-06-29 09:02:15', status: 'success' },
      { transactionId: 'TXN202606290902', amount: 156000, payMethod: '支付宝', tradeTime: '2026-06-29 09:05:33', status: 'success' },
      { transactionId: 'TXN202606290903', amount: 52000, payMethod: '银联支付', tradeTime: '2026-06-29 09:08:47', status: 'success' }
    ],
    handleRecords: [
      { time: '2026-06-29 09:15:32', title: '风险事件触发', type: 'danger', operator: '', remark: '系统自动检测到异常交易并生成风险事件' }
    ]
  },
  {
    eventId: 'EVT202606290002', eventTime: '2026-06-29 10:22:18', riskLevel: 'high', eventType: 'blacklist_hit',
    relatedType: 'user', relatedName: '用户138****5555', relatedId: 'U20260001',
    triggerRule: '黑名单用户交易拦截', riskScore: 98,
    description: '该用户已被列入欺诈黑名单，尝试发起金额25,800元交易，已被系统自动拦截',
    status: 'processing',
    triggerDetails: [
      { label: '黑名单类型', value: '欺诈黑名单', abnormal: true },
      { label: '列入原因', value: '历史多笔拒付记录', abnormal: true },
      { label: '本次交易金额', value: '¥25,800', abnormal: true },
      { label: '拦截结果', value: '已拦截', abnormal: false },
      { label: '关联设备', value: 'iPhone15,2 / iOS 17.5', abnormal: false }
    ],
    relatedTransactions: [
      { transactionId: 'TXN202606291001', amount: 25800, payMethod: '支付宝', tradeTime: '2026-06-29 10:22:10', status: 'failed' }
    ],
    handleRecords: [
      { time: '2026-06-29 10:22:18', title: '黑名单命中事件触发', type: 'danger', operator: '', remark: '系统自动拦截黑名单用户交易' },
      { time: '2026-06-29 10:30:00', title: '风控专员开始处理', type: 'primary', operator: '张风控', remark: '正在核实用户身份信息' }
    ]
  },
  {
    eventId: 'EVT202606290003', eventTime: '2026-06-29 11:05:44', riskLevel: 'medium', eventType: 'suspicious_login',
    relatedType: 'user', relatedName: '商户管理员-李某', relatedId: 'A100002',
    triggerRule: '异地登录+非常规设备', riskScore: 72,
    description: '检测到该账号在非常用IP地址（境外）使用新设备登录，存在账号被盗风险',
    status: 'pending',
    triggerDetails: [
      { label: '登录IP', value: '45.xxx.xxx.128（境外）', abnormal: true },
      { label: '登录地点', value: '新加坡', abnormal: true },
      { label: '设备信息', value: '未知设备 / Chrome 125', abnormal: true },
      { label: '常用IP', value: '上海 / 116.xxx.xxx.xxx', abnormal: false },
      { label: '上次登录时间', value: '2026-06-28 18:30:00', abnormal: false }
    ],
    relatedTransactions: [],
    handleRecords: [
      { time: '2026-06-29 11:05:44', title: '可疑登录事件触发', type: 'warning', operator: '', remark: '系统检测到异地异常登录' }
    ]
  },
  {
    eventId: 'EVT202606290004', eventTime: '2026-06-29 11:32:09', riskLevel: 'medium', eventType: 'large_amount',
    relatedType: 'merchant', relatedName: '云海餐饮连锁', relatedId: 'M100002',
    triggerRule: '单笔大额交易预警', riskScore: 65,
    description: '该商户产生一笔金额为186,000元的交易，超出商户设定的单笔限额100,000元',
    status: 'processed',
    triggerDetails: [
      { label: '交易金额', value: '¥186,000', abnormal: true },
      { label: '商户单笔限额', value: '¥100,000', abnormal: false },
      { label: '超出比例', value: '86%', abnormal: true },
      { label: '付款方', value: '企业用户-某科技公司', abnormal: false },
      { label: '商品信息', value: '企业团餐预付', abnormal: false }
    ],
    relatedTransactions: [
      { transactionId: 'TXN202606291101', amount: 186000, payMethod: '企业网银', tradeTime: '2026-06-29 11:28:55', status: 'success' }
    ],
    handleRecords: [
      { time: '2026-06-29 11:32:09', title: '大额交易预警触发', type: 'warning', operator: '', remark: '系统检测到大额交易超出限额' },
      { time: '2026-06-29 11:45:00', title: '确认风险通过', type: 'success', operator: '李审核', remark: '已联系商户核实，为正常企业团餐订单，予以放行' }
    ]
  },
  {
    eventId: 'EVT202606290005', eventTime: '2026-06-29 12:18:27', riskLevel: 'high', eventType: 'frequency_operation',
    relatedType: 'user', relatedName: '用户139****2233', relatedId: 'U20260008',
    triggerRule: '短时间高频次交易', riskScore: 88,
    description: '该用户在5分钟内连续发起23笔交易请求，涉嫌刷单或盗卡测试行为',
    status: 'pending',
    triggerDetails: [
      { label: '时间窗口', value: '5分钟', abnormal: false },
      { label: '交易次数', value: '23笔', abnormal: true },
      { label: '成功笔数', value: '0笔', abnormal: true },
      { label: '金额范围', value: '¥1.00 - ¥99.00', abnormal: true },
      { label: '卡BIN分布', value: '8家不同银行', abnormal: true }
    ],
    relatedTransactions: [
      { transactionId: 'TXN202606291201', amount: 1, payMethod: '微信支付', tradeTime: '2026-06-29 12:15:01', status: 'failed' },
      { transactionId: 'TXN202606291202', amount: 99, payMethod: '支付宝', tradeTime: '2026-06-29 12:15:32', status: 'failed' },
      { transactionId: 'TXN202606291203', amount: 50, payMethod: '微信支付', tradeTime: '2026-06-29 12:16:05', status: 'failed' }
    ],
    handleRecords: [
      { time: '2026-06-29 12:18:27', title: '高频交易事件触发', type: 'danger', operator: '', remark: '系统检测到疑似盗卡测试行为' }
    ]
  },
  {
    eventId: 'EVT202606290006', eventTime: '2026-06-29 13:05:51', riskLevel: 'low', eventType: 'offsite_transaction',
    relatedType: 'user', relatedName: '用户135****8899', relatedId: 'U20260015',
    triggerRule: '常用地以外交易', riskScore: 35,
    description: '该用户首次在异地（广州）发起交易，交易金额3,280元',
    status: 'processed',
    triggerDetails: [
      { label: '交易地点', value: '广州', abnormal: true },
      { label: '常用地点', value: '上海', abnormal: false },
      { label: '交易金额', value: '¥3,280', abnormal: false },
      { label: '设备指纹', value: '常用设备', abnormal: false },
      { label: '支付方式', value: '已绑定支付宝', abnormal: false }
    ],
    relatedTransactions: [
      { transactionId: 'TXN202606291301', amount: 3280, payMethod: '支付宝', tradeTime: '2026-06-29 13:04:22', status: 'success' }
    ],
    handleRecords: [
      { time: '2026-06-29 13:05:51', title: '异地交易预警触发', type: 'primary', operator: '', remark: '系统检测到首次异地交易' },
      { time: '2026-06-29 13:10:00', title: '短信验证通过', type: 'success', operator: '系统自动', remark: '用户已完成短信验证码验证，判定为正常交易' }
    ]
  },
  {
    eventId: 'EVT202606290007', eventTime: '2026-06-29 14:22:36', riskLevel: 'medium', eventType: 'merchant_abnormal',
    relatedType: 'merchant', relatedName: '星辰电商平台', relatedId: 'M100001',
    triggerRule: '商户退款率突增', riskScore: 68,
    description: '该商户近24小时退款率达12.5%，远超正常水平3%，需关注商户经营状况',
    status: 'processing',
    triggerDetails: [
      { label: '近24小时退款率', value: '12.5%', abnormal: true },
      { label: '正常退款率阈值', value: '3%', abnormal: false },
      { label: '退款笔数', value: '156笔', abnormal: true },
      { label: '退款总金额', value: '¥890,500', abnormal: true },
      { label: '退款原因分布', value: '质量问题45%/未发货35%', abnormal: true }
    ],
    relatedTransactions: [
      { transactionId: 'TXN202606291401', amount: 12800, payMethod: '微信支付', tradeTime: '2026-06-29 14:15:00', status: 'success' },
      { transactionId: 'TXN202606291402', amount: 8500, payMethod: '支付宝', tradeTime: '2026-06-29 14:18:30', status: 'success' }
    ],
    handleRecords: [
      { time: '2026-06-29 14:22:36', title: '商户退款率异常事件触发', type: 'warning', operator: '', remark: '系统检测到商户退款率异常突增' },
      { time: '2026-06-29 14:35:00', title: '风控专员开始处理', type: 'primary', operator: '王风控', remark: '正在联系商户了解退款原因' }
    ]
  },
  {
    eventId: 'EVT202606290008', eventTime: '2026-06-29 15:10:08', riskLevel: 'low', eventType: 'abnormal_transaction',
    relatedType: 'user', relatedName: '用户136****1122', relatedId: 'U20260023',
    triggerRule: '夜间大额交易提醒', riskScore: 28,
    description: '用户在凌晨时段发起一笔大额交易，金额为12,500元，已通过短信验证',
    status: 'ignored',
    triggerDetails: [
      { label: '交易时间', value: '03:12:45', abnormal: true },
      { label: '交易金额', value: '¥12,500', abnormal: false },
      { label: '交易商品', value: '酒店预订', abnormal: false },
      { label: '验证方式', value: '短信+密码', abnormal: false },
      { label: '历史夜间交易', value: '有过类似记录', abnormal: false }
    ],
    relatedTransactions: [
      { transactionId: 'TXN202606291501', amount: 12500, payMethod: '信用卡支付', tradeTime: '2026-06-29 03:12:45', status: 'success' }
    ],
    handleRecords: [
      { time: '2026-06-29 15:10:08', title: '夜间交易提醒触发', type: 'primary', operator: '', remark: '系统自动标记夜间大额交易' },
      { time: '2026-06-29 15:15:00', title: '标记为误报忽略', type: 'info', operator: '赵审核', remark: '核实为用户正常酒店预订行为，历史有夜间预订记录' }
    ]
  },
  {
    eventId: 'EVT202606290009', eventTime: '2026-06-29 15:48:55', riskLevel: 'high', eventType: 'suspicious_login',
    relatedType: 'merchant', relatedName: '康美医疗健康', relatedId: 'M100006',
    triggerRule: '密码连续错误+账户锁定', riskScore: 85,
    description: '该商户账户在10分钟内连续15次密码错误尝试后被锁定，疑似暴力破解攻击',
    status: 'processed',
    triggerDetails: [
      { label: '尝试次数', value: '15次', abnormal: true },
      { label: '时间窗口', value: '10分钟', abnormal: false },
      { label: '尝试IP', value: '3个不同IP', abnormal: true },
      { label: '账户状态', value: '已自动锁定', abnormal: false },
      { label: '最后登录成功', value: '7天前', abnormal: true }
    ],
    relatedTransactions: [],
    handleRecords: [
      { time: '2026-06-29 15:48:55', title: '暴力破解检测触发', type: 'danger', operator: '', remark: '系统检测到连续密码错误，自动锁定账户' },
      { time: '2026-06-29 16:00:00', title: '确认风险处置', type: 'success', operator: '系统自动+张风控', remark: '已通知商户重置密码，账户锁定24小时后自动解锁' }
    ]
  },
  {
    eventId: 'EVT202606290010', eventTime: '2026-06-29 16:25:13', riskLevel: 'medium', eventType: 'offsite_transaction',
    relatedType: 'user', relatedName: '用户137****4455', relatedId: 'U20260031',
    triggerRule: '跨境交易预警', riskScore: 60,
    description: '用户首次发起跨境交易，交易金额HK$8,600（约人民币7,920元），收款方为香港商户',
    status: 'pending',
    triggerDetails: [
      { label: '交易类型', value: '跨境支付', abnormal: true },
      { label: '交易金额', value: 'HK$8,600', abnormal: false },
      { label: '收款地区', value: '中国香港', abnormal: true },
      { label: '收款商户', value: '香港某数码专营店', abnormal: false },
      { label: '购汇用途', value: '数码产品购买', abnormal: false }
    ],
    relatedTransactions: [
      { transactionId: 'TXN202606291601', amount: 7920, payMethod: '国际信用卡', tradeTime: '2026-06-29 16:23:48', status: 'success' }
    ],
    handleRecords: [
      { time: '2026-06-29 16:25:13', title: '跨境交易预警触发', type: 'warning', operator: '', remark: '系统检测到用户首次跨境交易' }
    ]
  },
  {
    eventId: 'EVT202606290011', eventTime: '2026-06-29 17:02:39', riskLevel: 'low', eventType: 'frequency_operation',
    relatedType: 'user', relatedName: '用户133****6677', relatedId: 'U20260042',
    triggerRule: '查询频次过高', riskScore: 22,
    description: '该用户短时间内频繁查询余额和交易记录，1小时内达68次',
    status: 'ignored',
    triggerDetails: [
      { label: '查询类型', value: '余额/交易查询', abnormal: false },
      { label: '查询次数', value: '68次/小时', abnormal: true },
      { label: '平均间隔', value: '53秒', abnormal: false },
      { label: '设备信息', value: '常用手机', abnormal: false },
      { label: 'IP地址', value: '常用IP段', abnormal: false }
    ],
    relatedTransactions: [],
    handleRecords: [
      { time: '2026-06-29 17:02:39', title: '频繁查询提醒触发', type: 'primary', operator: '', remark: '系统检测到异常频繁查询操作' },
      { time: '2026-06-29 17:05:00', title: '自动忽略', type: 'info', operator: '系统自动', remark: '无资金类操作，判定为用户正常查账行为，自动忽略' }
    ]
  },
  {
    eventId: 'EVT202606290012', eventTime: '2026-06-29 17:35:20', riskLevel: 'medium', eventType: 'large_amount',
    relatedType: 'merchant', relatedName: '趣玩数字娱乐', relatedId: 'M100005',
    triggerRule: '单笔充值金额异常', riskScore: 58,
    description: '单个用户向该商户充值金额达50,000元，超出平台游戏类商户单笔充值限额',
    status: 'processed',
    triggerDetails: [
      { label: '充值金额', value: '¥50,000', abnormal: true },
      { label: '平台限额', value: '¥20,000', abnormal: false },
      { label: '充值用户', value: '用户188****9900', abnormal: false },
      { label: '用户注册时间', value: '2025-03-15', abnormal: false },
      { label: '历史充值记录', value: '月均3-5笔，每笔100-500元', abnormal: true }
    ],
    relatedTransactions: [
      { transactionId: 'TXN202606291701', amount: 50000, payMethod: '支付宝', tradeTime: '2026-06-29 17:33:12', status: 'success' }
    ],
    handleRecords: [
      { time: '2026-06-29 17:35:20', title: '大额充值预警触发', type: 'warning', operator: '', remark: '系统检测到用户单笔大额充值异常' },
      { time: '2026-06-29 17:45:00', title: '加入白名单', type: 'success', operator: '钱客服', remark: '已与用户电话核实，为正常充值行为，用户为高级VIP，申请提升限额，已加入大额交易白名单' }
    ]
  }
])

const getRiskTagType = (level: string) => {
  const map: Record<string, any> = { high: 'danger', medium: 'warning', low: 'success' }
  return map[level] || 'info'
}

const getRiskLevelText = (level: string) => {
  const map: Record<string, string> = { high: '高风险', medium: '中风险', low: '低风险' }
  return map[level] || '未知'
}

const getEventTypeText = (type: string) => {
  const map: Record<string, string> = {
    abnormal_transaction: '异常交易', suspicious_login: '可疑登录', large_amount: '大额交易',
    frequency_operation: '频繁操作', blacklist_hit: '黑名单命中', offsite_transaction: '异地交易',
    merchant_abnormal: '商户异常'
  }
  return map[type] || '未知类型'
}

const getStatusTagType = (status: string) => {
  const map: Record<string, any> = { pending: 'danger', processing: 'warning', processed: 'success', ignored: 'info' }
  return map[status] || 'info'
}

const getStatusText = (status: string) => {
  const map: Record<string, string> = { pending: '待处理', processing: '处理中', processed: '已处理', ignored: '已忽略' }
  return map[status] || '未知'
}

const handleSearch = () => {
  loading.value = true
  setTimeout(() => loading.value = false, 500)
}

const handleReset = () => {
  filterForm.eventId = ''
  filterForm.riskLevel = ''
  filterForm.eventType = ''
  filterForm.status = ''
  filterForm.dateRange = []
}

const handleRefresh = () => {
  loading.value = true
  setTimeout(() => {
    loading.value = false
    ElMessage.success('数据已刷新')
  }, 500)
}

const viewDetail = (row: any) => {
  currentEvent.value = row
  activeTab.value = 'info'
  handleRemark.value = ''
  drawerVisible.value = true
}

const handleProcess = (row: any) => {
  viewDetail(row)
}

const handleConfirmRisk = () => {
  ElMessageBox.confirm('确认该事件为风险事件？将记录风险处置结果。', '确认风险', { type: 'warning' }).then(() => {
    if (currentEvent.value) {
      currentEvent.value.status = 'processed'
      currentEvent.value.handleRecords.push({
        time: new Date().toLocaleString('zh-CN'), title: '确认风险处置', type: 'danger',
        operator: '当前操作员', remark: handleRemark.value || '确认为真实风险事件，已完成处置'
      })
      ElMessage.success('已确认风险事件')
      drawerVisible.value = false
    }
  }).catch(() => {})
}

const handleMarkFalse = () => {
  ElMessageBox.confirm('确认标记该事件为误报？事件将被标记为已处理。', '标记误报', { type: 'warning' }).then(() => {
    if (currentEvent.value) {
      currentEvent.value.status = 'processed'
      currentEvent.value.handleRecords.push({
        time: new Date().toLocaleString('zh-CN'), title: '标记为误报', type: 'success',
        operator: '当前操作员', remark: handleRemark.value || '经核实为误报，非真实风险'
      })
      ElMessage.success('已标记为误报')
      drawerVisible.value = false
    }
  }).catch(() => {})
}

const handleAddWhitelist = () => {
  ElMessageBox.confirm('确认将该主体加入白名单？后续同类行为将不再触发预警。', '加入白名单', { type: 'warning' }).then(() => {
    if (currentEvent.value) {
      currentEvent.value.status = 'ignored'
      currentEvent.value.handleRecords.push({
        time: new Date().toLocaleString('zh-CN'), title: '加入白名单', type: 'info',
        operator: '当前操作员', remark: handleRemark.value || '核实为正常行为，已加入白名单'
      })
      ElMessage.success('已加入白名单')
      drawerVisible.value = false
    }
  }).catch(() => {})
}
</script>

<style lang="scss" scoped>
.events-page {
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
    align-items: center;
  }

  .badge-btn {
    :deep(.el-badge__content) {
      top: 8px;
      right: 12px;
    }
  }
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  cursor: pointer;
  transition: transform var(--transition-fast), box-shadow var(--transition-fast);

  &:hover {
    transform: translateY(-2px);
  }

  .stat-icon {
    width: 52px;
    height: 52px;
    border-radius: var(--radius-lg);
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 24px;
    flex-shrink: 0;
  }

  .stat-content {
    flex: 1;
  }

  .stat-value {
    font-size: 28px;
    font-weight: 700;
    color: var(--text-primary);
    line-height: 1.2;
    font-family: 'SF Mono', Monaco, 'Courier New', monospace;
  }

  .stat-label {
    font-size: 13px;
    color: var(--text-secondary);
    margin-top: 4px;
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
  justify-content: flex-end;
  align-items: center;
  margin-bottom: 16px;
}

.table-info {
  font-size: 13px;
  color: var(--text-secondary);

  .highlight {
    color: var(--primary-color);
    font-weight: 600;
  }
}

.mono-text {
  font-family: 'SF Mono', Monaco, 'Courier New', monospace;
  color: var(--primary-color);
  font-size: 12px;
}

.related-cell {
  .related-name {
    font-weight: 500;
    color: var(--text-primary);
    font-size: 13px;
  }

  .related-id {
    margin-top: 2px;
  }
}

.amount-text {
  font-weight: 600;
  font-family: 'SF Mono', Monaco, 'Courier New', monospace;
  color: var(--text-primary);
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.event-detail {
  padding: 20px;
  padding-bottom: 140px;
}

.detail-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px;
  border-radius: var(--radius-lg);
  margin-bottom: 20px;

  &.high {
    background: linear-gradient(135deg, var(--danger-bg), #FFF0F0);
    border: 1px solid rgba(245, 63, 63, 0.15);
  }

  &.medium {
    background: linear-gradient(135deg, var(--warning-bg), #FFF7E6);
    border: 1px solid rgba(255, 125, 0, 0.15);
  }

  &.low {
    background: linear-gradient(135deg, var(--success-bg), #F0FFF4);
    border: 1px solid rgba(0, 180, 42, 0.15);
  }

  .header-left {
    display: flex;
    align-items: center;
    gap: 16px;
  }

  .risk-badge {
    width: 48px;
    height: 48px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 24px;

    &.high {
      background: var(--danger-color);
      color: #fff;
    }

    &.medium {
      background: var(--warning-color);
      color: #fff;
    }

    &.low {
      background: var(--success-color);
      color: #fff;
    }
  }

  .header-info {
    h3 {
      margin: 0 0 6px 0;
      font-size: 18px;
      font-weight: 600;
    }

    .header-meta {
      display: flex;
      align-items: center;
      font-size: 13px;
      color: var(--text-secondary);
      gap: 4px;

      .el-icon {
        margin-right: 2px;
        vertical-align: middle;
      }
    }
  }
}

:deep(.detail-tabs) {
  .el-tabs__nav-wrap::after {
    background: var(--border-light);
  }
}

.section-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--border-light);

  .el-icon {
    color: var(--primary-color);
  }
}

.info-descriptions {
  margin-top: 8px;
}

.risk-score {
  font-size: 18px;
  font-weight: 700;
  font-family: 'SF Mono', Monaco, 'Courier New', monospace;

  &.high { color: var(--danger-color); }
  &.medium { color: var(--warning-color); }
  &.low { color: var(--success-color); }
}

.risk-desc {
  line-height: 1.6;
  color: var(--text-regular);
}

.trigger-detail {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.trigger-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: var(--bg-hover);
  border-radius: var(--radius-md);

  .trigger-label {
    font-size: 13px;
    color: var(--text-secondary);
  }

  .trigger-value {
    font-weight: 600;
    font-family: 'SF Mono', Monaco, 'Courier New', monospace;
    color: var(--text-primary);

    &.abnormal {
      color: var(--danger-color);
    }
  }
}

.timeline-content {
  .timeline-title {
    font-weight: 500;
    color: var(--text-primary);
    margin-bottom: 4px;
  }

  .timeline-operator {
    font-size: 12px;
    color: var(--text-secondary);
    margin-bottom: 2px;
  }

  .timeline-remark {
    font-size: 13px;
    color: var(--text-regular);
    line-height: 1.5;
  }
}

.drawer-footer {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 16px 20px;
  background: var(--bg-container);
  border-top: 1px solid var(--border-light);
  box-shadow: 0 -4px 12px rgba(0, 0, 0, 0.04);
}

.handle-form {
  margin-bottom: 12px;
}

.handle-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}

:deep(.el-drawer__body) {
  position: relative;
  overflow-y: auto;
}
</style>
