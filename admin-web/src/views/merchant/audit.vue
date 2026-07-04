<template>
  <div class="page-container audit-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">进件审核</h2>
        <p class="page-desc">审核商户入驻申请资料</p>
      </div>
    </div>

    <div class="stats-grid">
      <div class="card-shadow metric-card warning">
        <div class="stat-icon" style="background: var(--warning-bg); color: var(--warning-color);">
          <el-icon size="24"><Clock /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.pending }}</div>
          <div class="stat-label">待审核</div>
        </div>
      </div>
      <div class="card-shadow metric-card success">
        <div class="stat-icon" style="background: var(--success-bg); color: var(--success-color);">
          <el-icon size="24"><CircleCheck /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.approved }}</div>
          <div class="stat-label">已通过</div>
        </div>
      </div>
      <div class="card-shadow metric-card danger">
        <div class="stat-icon" style="background: var(--danger-bg); color: var(--danger-color);">
          <el-icon size="24"><CircleClose /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.rejected }}</div>
          <div class="stat-label">已驳回</div>
        </div>
      </div>
      <div class="card-shadow metric-card">
        <div class="stat-icon" style="background: var(--primary-bg); color: var(--primary-color);">
          <el-icon size="24"><Document /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.todayNew }}</div>
          <div class="stat-label">今日新增</div>
        </div>
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
        <el-form-item label="审核状态">
          <el-select v-model="filterForm.status" placeholder="全部状态" clearable style="width: 140px">
            <el-option label="待审核" value="pending" />
            <el-option label="审核中" value="reviewing" />
            <el-option label="已通过" value="approved" />
            <el-option label="已驳回" value="rejected" />
          </el-select>
        </el-form-item>
        <el-form-item label="申请时间">
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
        <div class="table-info">
          共 <span class="highlight">{{ total }}</span> 条记录
        </div>
      </div>
      <el-table
        :data="tableData"
        style="width: 100%"
        stripe
        v-loading="loading"
      >
        <el-table-column prop="applyNo" label="申请单号" width="160">
          <template #default="{ row }">
            <span class="mono-text">{{ row.applyNo }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="商户名称" min-width="200">
          <template #default="{ row }">
            <div class="merchant-cell">
              <el-avatar :size="32" :style="{ background: row.avatarColor }">
                {{ row.name.charAt(0) }}
              </el-avatar>
              <div>
                <div class="merchant-name">{{ row.name }}</div>
                <div class="merchant-short">{{ row.shortName }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="industry" label="所属行业" width="120" />
        <el-table-column prop="contact" label="联系人" width="100">
          <template #default="{ row }">
            <div class="contact-cell">
              <el-icon size="14"><User /></el-icon>
              {{ row.contact }}
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="联系电话" width="130">
          <template #default="{ row }">
            <div class="contact-cell">
              <el-icon size="14"><Phone /></el-icon>
              {{ row.phone }}
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="applyTime" label="申请时间" width="160" />
        <el-table-column prop="status" label="审核状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getAuditStatusType(row.status)" size="small">
              {{ getAuditStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleAudit(row)" v-if="row.status === 'pending' || row.status === 'reviewing'">审核</el-button>
            <el-button type="primary" link size="small" @click="viewDetail(row)">详情</el-button>
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
      v-model="auditDrawerVisible"
      :title="isDetailMode ? '申请详情' : '审核进件'"
      direction="rtl"
      size="700px"
      :destroy-on-close="true"
    >
      <div v-if="currentApply" class="audit-detail">
        <div class="detail-header">
          <el-avatar :size="64" :style="{ background: currentApply.avatarColor }">
            {{ currentApply.name.charAt(0) }}
          </el-avatar>
          <div class="detail-title">
            <h3>{{ currentApply.name }}</h3>
            <div class="detail-meta">
              <span class="mono-text">{{ currentApply.applyNo }}</span>
              <el-tag :type="getAuditStatusType(currentApply.status)" size="small" style="margin-left: 12px">
                {{ getAuditStatusText(currentApply.status) }}
              </el-tag>
            </div>
          </div>
        </div>

        <el-tabs v-model="activeTab" class="detail-tabs">
          <el-tab-pane label="基本信息" name="basic">
            <div class="info-section">
              <div class="section-title">
                <el-icon><OfficeBuilding /></el-icon>
                商户基本信息
              </div>
              <el-descriptions :column="2" border class="info-descriptions">
                <el-descriptions-item label="商户名称">{{ currentApply.name }}</el-descriptions-item>
                <el-descriptions-item label="商户简称">{{ currentApply.shortName }}</el-descriptions-item>
                <el-descriptions-item label="商户号"><span class="mono-text">{{ currentApply.merchantId }}</span></el-descriptions-item>
                <el-descriptions-item label="所属行业">{{ currentApply.industry }}</el-descriptions-item>
                <el-descriptions-item label="联系人">{{ currentApply.contact }}</el-descriptions-item>
                <el-descriptions-item label="联系电话">{{ currentApply.phone }}</el-descriptions-item>
                <el-descriptions-item label="电子邮箱">{{ currentApply.email }}</el-descriptions-item>
                <el-descriptions-item label="经营地区">{{ currentApply.region }}</el-descriptions-item>
                <el-descriptions-item label="商户地址" :span="2">{{ currentApply.address }}</el-descriptions-item>
                <el-descriptions-item label="申请时间" :span="2">{{ currentApply.applyTime }}</el-descriptions-item>
              </el-descriptions>
            </div>
          </el-tab-pane>
          <el-tab-pane label="营业执照" name="license">
            <div class="info-section">
              <div class="section-title">
                <el-icon><Document /></el-icon>
                营业执照信息
              </div>
              <el-descriptions :column="2" border class="info-descriptions">
                <el-descriptions-item label="企业名称">{{ currentApply.licenseName }}</el-descriptions-item>
                <el-descriptions-item label="统一社会信用代码"><span class="mono-text">{{ currentApply.licenseNo }}</span></el-descriptions-item>
                <el-descriptions-item label="法定代表人">{{ currentApply.legalPerson }}</el-descriptions-item>
                <el-descriptions-item label="注册资本">{{ currentApply.registeredCapital }}</el-descriptions-item>
                <el-descriptions-item label="成立日期">{{ currentApply.establishDate }}</el-descriptions-item>
                <el-descriptions-item label="营业期限">{{ currentApply.businessPeriod }}</el-descriptions-item>
                <el-descriptions-item label="经营范围" :span="2">{{ currentApply.businessScope }}</el-descriptions-item>
                <el-descriptions-item label="注册地址" :span="2">{{ currentApply.registeredAddress }}</el-descriptions-item>
              </el-descriptions>
              <div class="license-preview">
                <div class="preview-title">营业执照照片</div>
                <div class="preview-box">
                  <el-icon size="48" color="var(--text-secondary)"><Stamp /></el-icon>
                  <span class="preview-text">营业执照图片预览</span>
                </div>
              </div>
            </div>
          </el-tab-pane>
          <el-tab-pane label="结算账户" name="settlement">
            <div class="info-section">
              <div class="section-title">
                <el-icon><OfficeBuilding /></el-icon>
                结算银行账户
              </div>
              <el-descriptions :column="2" border class="info-descriptions">
                <el-descriptions-item label="账户类型">{{ currentApply.accountType }}</el-descriptions-item>
                <el-descriptions-item label="开户银行">{{ currentApply.bankName }}</el-descriptions-item>
                <el-descriptions-item label="开户支行">{{ currentApply.bankBranch }}</el-descriptions-item>
                <el-descriptions-item label="联行号"><span class="mono-text">{{ currentApply.bankCode }}</span></el-descriptions-item>
                <el-descriptions-item label="账户名称">{{ currentApply.accountName }}</el-descriptions-item>
                <el-descriptions-item label="银行账号"><span class="mono-text">{{ currentApply.accountNo }}</span></el-descriptions-item>
              </el-descriptions>
            </div>
          </el-tab-pane>
          <el-tab-pane label="法人信息" name="legal">
            <div class="info-section">
              <div class="section-title">
                <el-icon><User /></el-icon>
                法定代表人信息
              </div>
              <el-descriptions :column="2" border class="info-descriptions">
                <el-descriptions-item label="姓名">{{ currentApply.legalPerson }}</el-descriptions-item>
                <el-descriptions-item label="证件类型">身份证</el-descriptions-item>
                <el-descriptions-item label="证件号码"><span class="mono-text">{{ currentApply.idCardNo }}</span></el-descriptions-item>
                <el-descriptions-item label="证件有效期">{{ currentApply.idCardValidity }}</el-descriptions-item>
                <el-descriptions-item label="联系电话">{{ currentApply.legalPhone }}</el-descriptions-item>
                <el-descriptions-item label="电子邮箱">{{ currentApply.legalEmail }}</el-descriptions-item>
              </el-descriptions>
              <div class="license-preview">
                <div class="preview-title">身份证照片</div>
                <div class="id-preview-group">
                  <div class="preview-box small">
                    <el-icon size="36" color="var(--text-secondary)"><User /></el-icon>
                    <span class="preview-text">人像面</span>
                  </div>
                  <div class="preview-box small">
                    <el-icon size="36" color="var(--text-secondary)"><Document /></el-icon>
                    <span class="preview-text">国徽面</span>
                  </div>
                </div>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>

        <div v-if="!isDetailMode && (currentApply.status === 'pending' || currentApply.status === 'reviewing')" class="audit-actions">
          <el-form :model="auditForm" label-width="80px" class="reject-form">
            <el-form-item label="驳回原因" v-if="showRejectForm">
              <el-input
                v-model="auditForm.rejectReason"
                type="textarea"
                :rows="3"
                placeholder="请填写驳回原因，商户可根据原因修改后重新提交"
              />
            </el-form-item>
          </el-form>
          <div class="action-btns">
            <el-button v-if="!showRejectForm" @click="handleShowReject" type="danger">
              <el-icon><CircleClose /></el-icon>
              驳回
            </el-button>
            <template v-else>
              <el-button @click="showRejectForm = false; auditForm.rejectReason = ''">取消</el-button>
              <el-button type="danger" @click="handleReject" :disabled="!auditForm.rejectReason.trim()">确认驳回</el-button>
            </template>
            <el-button type="success" @click="handleApprove" v-if="!showRejectForm">
              <el-icon><CircleCheck /></el-icon>
              通过审核
            </el-button>
          </div>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Document, CircleCheck, CircleClose, Clock, User, OfficeBuilding, Phone, Stamp } from '@element-plus/icons-vue'

const loading = ref(false)
const auditDrawerVisible = ref(false)
const isDetailMode = ref(false)
const showRejectForm = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(10)
const currentApply = ref<any>(null)
const activeTab = ref('basic')

const stats = reactive({
  pending: 5,
  approved: 128,
  rejected: 12,
  todayNew: 3
})

const filterForm = reactive({
  name: '',
  merchantId: '',
  status: '',
  dateRange: [] as any[]
})

const auditForm = reactive({
  rejectReason: ''
})

const tableData = ref([
  { applyNo: 'AP202606290001', merchantId: 'M200001', name: '恒信电子商务有限公司', shortName: '恒信电商', industry: '电商零售', contact: '陈建国', phone: '138****1234', email: 'chen@hengxin.com', region: '上海市浦东新区', address: '上海市浦东新区陆家嘴金融中心88号', applyTime: '2026-06-29 09:15:00', status: 'pending', avatarColor: '#165DFF', licenseName: '恒信电子商务有限公司', licenseNo: '91310000MA1FL8AB2X', legalPerson: '陈建国', registeredCapital: '1000万元', establishDate: '2023-03-15', businessPeriod: '2023-03-15 至 2053-03-14', businessScope: '互联网销售、电子产品销售、日用品销售', registeredAddress: '上海市浦东新区陆家嘴金融中心88号', accountType: '对公账户', bankName: '中国工商银行', bankBranch: '上海浦东支行', bankCode: '102290026582', accountName: '恒信电子商务有限公司', accountNo: '6222 0210 0112 3456 789', idCardNo: '310***********1234', idCardValidity: '2020-05-10 至 2040-05-09', legalPhone: '138****1234', legalEmail: 'chen@hengxin.com' },
  { applyNo: 'AP202606290002', merchantId: 'M200002', name: '美味餐饮管理有限公司', shortName: '美味餐饮', industry: '餐饮美食', contact: '林小雨', phone: '139****5678', email: 'lin@meiwei.com', region: '北京市朝阳区', address: '北京市朝阳区三里屯太古里南区', applyTime: '2026-06-29 10:30:00', status: 'reviewing', avatarColor: '#00B42A', licenseName: '美味餐饮管理有限公司', licenseNo: '91110105MA01G4CD3K', legalPerson: '林小雨', registeredCapital: '500万元', establishDate: '2024-01-20', businessPeriod: '2024-01-20 至 长期', businessScope: '餐饮服务、食品销售、餐饮管理', registeredAddress: '北京市朝阳区三里屯太古里南区', accountType: '对公账户', bankName: '招商银行', bankBranch: '北京朝阳支行', bankCode: '308100005621', accountName: '美味餐饮管理有限公司', accountNo: '6226 0901 0234 5678 901', idCardNo: '110***********5678', idCardValidity: '2019-08-15 至 2039-08-14', legalPhone: '139****5678', legalEmail: 'lin@meiwei.com' },
  { applyNo: 'AP202606280003', merchantId: 'M200003', name: '智慧教育培训学校', shortName: '智慧教育', industry: '教育培训', contact: '王志强', phone: '137****9012', email: 'wang@zhihui.com', region: '杭州市西湖区', address: '杭州市西湖区文三路电子信息街区', applyTime: '2026-06-28 14:20:00', status: 'pending', avatarColor: '#FF7D00', licenseName: '智慧教育培训学校有限公司', licenseNo: '91330106MA2KD4EF5L', legalPerson: '王志强', registeredCapital: '300万元', establishDate: '2022-09-01', businessPeriod: '2022-09-01 至 2052-08-31', businessScope: '教育培训、教育咨询、图书销售', registeredAddress: '杭州市西湖区文三路电子信息街区', accountType: '对公账户', bankName: '中国建设银行', bankBranch: '杭州西湖支行', bankCode: '105331000125', accountName: '智慧教育培训学校有限公司', accountNo: '6227 0033 2876 5432 109', idCardNo: '330***********9012', idCardValidity: '2021-03-20 至 2041-03-19', legalPhone: '137****9012', legalEmail: 'wang@zhihui.com' },
  { applyNo: 'AP202606280004', merchantId: 'M200004', name: '飞驰出行科技有限公司', shortName: '飞驰出行', industry: '出行交通', contact: '赵飞翔', phone: '136****3456', email: 'zhao@feichi.com', region: '深圳市南山区', address: '深圳市南山区科技园南区科苑路', applyTime: '2026-06-28 16:45:00', status: 'approved', avatarColor: '#722ED1', licenseName: '飞驰出行科技有限公司', licenseNo: '91440300MA5F7GH89P', legalPerson: '赵飞翔', registeredCapital: '2000万元', establishDate: '2021-06-10', businessPeriod: '2021-06-10 至 长期', businessScope: '网约车服务、汽车租赁、技术开发', registeredAddress: '深圳市南山区科技园南区科苑路', accountType: '对公账户', bankName: '平安银行', bankBranch: '深圳南山支行', bankCode: '307584008925', accountName: '飞驰出行科技有限公司', accountNo: '6230 5830 0987 6543 210', idCardNo: '440***********3456', idCardValidity: '2018-12-01 至 2038-11-30', legalPhone: '136****3456', legalEmail: 'zhao@feichi.com' },
  { applyNo: 'AP202606270005', merchantId: 'M200005', name: '欢乐数字娱乐有限公司', shortName: '欢乐娱乐', industry: '数字娱乐', contact: '钱伟明', phone: '135****7890', email: 'qian@huanle.com', region: '广州市天河区', address: '广州市天河区珠江新城花城大道', applyTime: '2026-06-27 11:00:00', status: 'rejected', avatarColor: '#14C9C9', licenseName: '欢乐数字娱乐有限公司', licenseNo: '91440101MA5CL3MN6Q', legalPerson: '钱伟明', registeredCapital: '800万元', establishDate: '2023-11-25', businessPeriod: '2023-11-25 至 2053-11-24', businessScope: '网络游戏开发、数字内容服务、动漫设计', registeredAddress: '广州市天河区珠江新城花城大道', accountType: '对公账户', bankName: '中国银行', bankBranch: '广州天河支行', bankCode: '104581003698', accountName: '欢乐数字娱乐有限公司', accountNo: '6217 8520 0765 4321 098', idCardNo: '440***********7890', idCardValidity: '2020-07-08 至 2040-07-07', legalPhone: '135****7890', legalEmail: 'qian@huanle.com' },
  { applyNo: 'AP202606270006', merchantId: 'M200006', name: '康源医疗器械有限公司', shortName: '康源医疗', industry: '医疗健康', contact: '孙美华', phone: '134****2345', email: 'sun@kangyuan.com', region: '成都市高新区', address: '成都市高新区天府大道中段', applyTime: '2026-06-27 09:30:00', status: 'approved', avatarColor: '#F53F3F', licenseName: '康源医疗器械有限公司', licenseNo: '91510100MA67QRST4U', legalPerson: '孙美华', registeredCapital: '1500万元', establishDate: '2020-04-18', businessPeriod: '2020-04-18 至 长期', businessScope: '医疗器械销售、医疗设备租赁、健康咨询', registeredAddress: '成都市高新区天府大道中段', accountType: '对公账户', bankName: '中国农业银行', bankBranch: '成都高新支行', bankCode: '103651087412', accountName: '康源医疗器械有限公司', accountNo: '6228 4804 1234 5678 901', idCardNo: '510***********2345', idCardValidity: '2019-11-12 至 2039-11-11', legalPhone: '134****2345', legalEmail: 'sun@kangyuan.com' },
  { applyNo: 'AP202606260007', merchantId: 'M200007', name: '美家家居用品有限公司', shortName: '美家家居', industry: '家居建材', contact: '周雅婷', phone: '133****6789', email: 'zhou@meijia.com', region: '南京市建邺区', address: '南京市建邺区河西大街万达广场', applyTime: '2026-06-26 15:10:00', status: 'pending', avatarColor: '#165DFF', licenseName: '美家家居用品有限公司', licenseNo: '91320105MA1WXYZ12V', legalPerson: '周雅婷', registeredCapital: '600万元', establishDate: '2022-07-05', businessPeriod: '2022-07-05 至 2052-07-04', businessScope: '家居用品销售、家具制造、室内装饰', registeredAddress: '南京市建邺区河西大街万达广场', accountType: '对公账户', bankName: '交通银行', bankBranch: '南京建邺支行', bankCode: '301301000258', accountName: '美家家居用品有限公司', accountNo: '6222 6011 4567 8901 234', idCardNo: '320***********6789', idCardValidity: '2021-09-25 至 2041-09-24', legalPhone: '133****6789', legalEmail: 'zhou@meijia.com' },
  { applyNo: 'AP202606260008', merchantId: 'M200008', name: '绿源生态农业有限公司', shortName: '绿源农业', industry: '农林牧渔', contact: '吴大伟', phone: '132****0123', email: 'wu@lvyuan.com', region: '昆明市呈贡区', address: '昆明市呈贡区斗南花卉市场', applyTime: '2026-06-26 10:45:00', status: 'reviewing', avatarColor: '#00B42A', licenseName: '绿源生态农业有限公司', licenseNo: '91530114MA6PABCD9W', legalPerson: '吴大伟', registeredCapital: '200万元', establishDate: '2024-03-12', businessPeriod: '2024-03-12 至 2054-03-11', businessScope: '农产品种植、花卉种植、农产品销售', registeredAddress: '昆明市呈贡区斗南花卉市场', accountType: '对公账户', bankName: '中国邮政储蓄银行', bankBranch: '昆明呈贡支行', bankCode: '403731000369', accountName: '绿源生态农业有限公司', accountNo: '6217 9935 7890 1234 567', idCardNo: '530***********0123', idCardValidity: '2022-01-18 至 2042-01-17', legalPhone: '132****0123', legalEmail: 'wu@lvyuan.com' },
  { applyNo: 'AP202606250009', merchantId: 'M200009', name: '悦动健身连锁有限公司', shortName: '悦动健身', industry: '运动健身', contact: '郑凯旋', phone: '131****4567', email: 'zheng@yuedong.com', region: '武汉市洪山区', address: '武汉市洪山区光谷步行街世界城', applyTime: '2026-06-25 13:20:00', status: 'rejected', avatarColor: '#FF7D00', licenseName: '悦动健身连锁有限公司', licenseNo: '91420111MA4KLMN5X', legalPerson: '郑凯旋', registeredCapital: '400万元', establishDate: '2023-05-20', businessPeriod: '2023-05-20 至 2043-05-19', businessScope: '健身服务、体育用品销售、健康管理', registeredAddress: '武汉市洪山区光谷步行街世界城', accountType: '对公账户', bankName: '兴业银行', bankBranch: '武汉洪山支行', bankCode: '309521000741', accountName: '悦动健身连锁有限公司', accountNo: '6229 0812 2345 6789 012', idCardNo: '420***********4567', idCardValidity: '2020-10-30 至 2040-10-29', legalPhone: '131****4567', legalEmail: 'zheng@yuedong.com' },
  { applyNo: 'AP202606250010', merchantId: 'M200010', name: '新锐文化传媒有限公司', shortName: '新锐传媒', industry: '文化传媒', contact: '黄子涵', phone: '130****8901', email: 'huang@xinrui.com', region: '西安市雁塔区', address: '西安市雁塔区高新技术产业开发区', applyTime: '2026-06-25 08:50:00', status: 'approved', avatarColor: '#722ED1', licenseName: '新锐文化传媒有限公司', licenseNo: '91610113MA6W123Y1Z', legalPerson: '黄子涵', registeredCapital: '700万元', establishDate: '2021-12-08', businessPeriod: '2021-12-08 至 长期', businessScope: '广告设计、影视制作、文化活动策划', registeredAddress: '西安市雁塔区高新技术产业开发区', accountType: '对公账户', bankName: '中信银行', bankBranch: '西安高新支行', bankCode: '302791003574', accountName: '新锐文化传媒有限公司', accountNo: '6217 7119 3456 7890 123', idCardNo: '610***********8901', idCardValidity: '2019-04-22 至 2039-04-21', legalPhone: '130****8901', legalEmail: 'huang@xinrui.com' }
])

const getAuditStatusType = (status: string) => {
  const map: Record<string, string> = { pending: 'warning', reviewing: 'primary', approved: 'success', rejected: 'danger' }
  return map[status] || 'info'
}

const getAuditStatusText = (status: string) => {
  const map: Record<string, string> = { pending: '待审核', reviewing: '审核中', approved: '已通过', rejected: '已驳回' }
  return map[status] || '未知'
}

const handleSearch = () => {
  loading.value = true
  setTimeout(() => loading.value = false, 500)
}

const handleReset = () => {
  filterForm.name = ''
  filterForm.merchantId = ''
  filterForm.status = ''
  filterForm.dateRange = []
}

const resetAuditForm = () => {
  showRejectForm.value = false
  auditForm.rejectReason = ''
  activeTab.value = 'basic'
}

const handleAudit = (row: any) => {
  isDetailMode.value = false
  currentApply.value = row
  resetAuditForm()
  auditDrawerVisible.value = true
}

const viewDetail = (row: any) => {
  isDetailMode.value = true
  currentApply.value = row
  resetAuditForm()
  auditDrawerVisible.value = true
}

const handleShowReject = () => {
  showRejectForm.value = true
}

const handleApprove = () => {
  if (currentApply.value) {
    const originalStatus = currentApply.value.status
    currentApply.value.status = 'approved'
    stats.approved++
    if (originalStatus === 'pending' || originalStatus === 'reviewing') stats.pending--
  }
  ElMessage.success('审核通过成功')
  auditDrawerVisible.value = false
}

const handleReject = () => {
  if (!auditForm.rejectReason.trim()) {
    ElMessage.warning('请填写驳回原因')
    return
  }
  if (currentApply.value) {
    const originalStatus = currentApply.value.status
    currentApply.value.status = 'rejected'
    stats.rejected++
    if (originalStatus === 'pending' || originalStatus === 'reviewing') stats.pending--
  }
  ElMessage.success('已驳回申请')
  auditDrawerVisible.value = false
}
</script>

<style lang="scss" scoped>
.audit-page {
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
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 16px;

  .metric-card {
    display: flex;
    align-items: center;
    gap: 16px;
  }

  .stat-icon {
    width: 56px;
    height: 56px;
    border-radius: var(--radius-lg);
    display: flex;
    align-items: center;
    justify-content: center;
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
  justify-content: space-between;
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

.contact-cell {
  display: flex;
  align-items: center;
  gap: 4px;
  color: var(--text-regular);
}

.mono-text {
  font-family: 'SF Mono', Monaco, 'Courier New', monospace;
  color: var(--primary-color);
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.audit-detail {
  padding: 0;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.detail-header {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: var(--bg-hover);
  margin: 0;
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
  flex: 1;
  overflow-y: auto;
  padding: 0 20px;

  .el-tabs__nav-wrap::after {
    background: var(--border-light);
  }

  .el-tabs__content {
    padding-bottom: 20px;
  }
}

.info-section {
  margin-top: 8px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--border-light);
}

.info-descriptions {
  margin-bottom: 16px;
}

.license-preview {
  margin-top: 16px;

  .preview-title {
    font-size: 14px;
    font-weight: 500;
    color: var(--text-regular);
    margin-bottom: 12px;
  }

  .preview-box {
    width: 100%;
    height: 200px;
    border: 2px dashed var(--border-color);
    border-radius: var(--radius-lg);
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 8px;
    background: var(--bg-hover);

    &.small {
      width: 200px;
      height: 140px;
    }

    .preview-text {
      font-size: 13px;
      color: var(--text-secondary);
    }
  }

  .id-preview-group {
    display: flex;
    gap: 16px;
  }
}

.audit-actions {
  padding: 16px 20px;
  border-top: 1px solid var(--border-light);
  background: var(--bg-container);
  flex-shrink: 0;
}

.reject-form {
  margin-bottom: 12px;
}

.action-btns {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
