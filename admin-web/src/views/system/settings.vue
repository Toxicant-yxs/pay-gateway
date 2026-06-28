<template>
  <div class="page-container settings-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">系统配置</h2>
        <p class="page-desc">管理支付系统全局参数、支付规则、通知渠道与安全策略</p>
      </div>
    </div>

    <div class="card-shadow settings-card">
      <el-tabs v-model="activeTab" class="settings-tabs">
        <el-tab-pane name="basic">
          <template #label>
            <span class="tab-label">
              <el-icon><Setting /></el-icon>
              基本设置
            </span>
          </template>
          <el-form :model="basicForm" label-width="120px" class="settings-form">
            <el-form-item label="系统名称">
              <el-input v-model="basicForm.systemName" placeholder="请输入系统名称" style="max-width: 400px" />
            </el-form-item>
            <el-form-item label="系统Logo">
              <div class="logo-upload">
                <el-upload
                  class="logo-uploader"
                  action="#"
                  :show-file-list="false"
                  :auto-upload="false"
                >
                  <img v-if="basicForm.logoUrl" :src="basicForm.logoUrl" class="logo-preview" />
                  <el-icon v-else class="logo-uploader-icon"><Plus /></el-icon>
                </el-upload>
                <div class="logo-tip">
                  <p>建议尺寸 200×60px，支持 PNG/JPG 格式，文件大小不超过 2MB</p>
                </div>
              </div>
            </el-form-item>
            <el-form-item label="首页URL">
              <el-input v-model="basicForm.homeUrl" placeholder="https://" style="max-width: 500px" />
            </el-form-item>
            <el-form-item label="ICP备案号">
              <el-input v-model="basicForm.icpNumber" placeholder="请输入ICP备案号" style="max-width: 300px" />
            </el-form-item>
            <el-form-item label="客服电话">
              <el-input v-model="basicForm.servicePhone" placeholder="请输入客服电话" style="max-width: 250px" />
            </el-form-item>
            <el-form-item label="客服邮箱">
              <el-input v-model="basicForm.serviceEmail" placeholder="请输入客服邮箱" style="max-width: 300px" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSave('basic')">
                <el-icon><Check /></el-icon>
                保存设置
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane name="payment">
          <template #label>
            <span class="tab-label">
              <el-icon><CreditCard /></el-icon>
              支付设置
            </span>
          </template>
          <el-form :model="paymentForm" label-width="140px" class="settings-form">
            <el-form-item label="测试模式">
              <el-switch v-model="paymentForm.testMode" active-text="开启" inactive-text="关闭" />
              <span class="form-tip">开启后所有交易走沙箱环境，不会产生真实资金流动</span>
            </el-form-item>
            <el-form-item label="自动结算">
              <el-switch v-model="paymentForm.autoSettle" active-text="开启" inactive-text="关闭" />
              <span class="form-tip">自动将已完成交易结算至商户账户</span>
            </el-form-item>
            <el-form-item label="退款审核">
              <el-switch v-model="paymentForm.refundAudit" active-text="需要" inactive-text="无需" />
              <span class="form-tip">退款申请是否需要人工审核后执行</span>
            </el-form-item>
            <el-form-item label="回调重试次数">
              <el-input-number v-model="paymentForm.retryCount" :min="0" :max="10" controls-position="right" />
              <span class="form-tip">支付/退款回调失败时最大重试次数</span>
            </el-form-item>
            <el-form-item label="订单超时时间">
              <el-input-number v-model="paymentForm.orderTimeout" :min="1" :max="1440" controls-position="right" />
              <span class="form-tip">分钟，未支付订单自动关闭时间</span>
            </el-form-item>
            <el-form-item label="结算周期">
              <el-select v-model="paymentForm.settleCycle" placeholder="请选择" style="width: 180px">
                <el-option label="T+0 实时到账" value="T+0" />
                <el-option label="T+1 次日到账" value="T+1" />
                <el-option label="D+0 自然日" value="D+0" />
                <el-option label="D+1 自然日次日" value="D+1" />
              </el-select>
            </el-form-item>
            <el-form-item label="单笔限额">
              <el-row :gutter="12">
                <el-col :span="6">
                  <el-input-number v-model="paymentForm.minAmount" :min="0" :precision="2" controls-position="right" style="width: 100%" placeholder="最低金额" />
                </el-col>
                <el-col :span="2" style="display: flex; align-items: center; justify-content: center; color: var(--text-secondary)">-</el-col>
                <el-col :span="6">
                  <el-input-number v-model="paymentForm.maxAmount" :min="0" :precision="2" controls-position="right" style="width: 100%" placeholder="最高金额" />
                </el-col>
                <el-col :span="4" style="display: flex; align-items: center; color: var(--text-secondary)">元</el-col>
              </el-row>
            </el-form-item>
            <el-form-item label="分账功能">
              <el-switch v-model="paymentForm.splitEnabled" active-text="开启" inactive-text="关闭" />
              <span class="form-tip">支持一笔交易分账至多个接收方</span>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSave('payment')">
                <el-icon><Check /></el-icon>
                保存设置
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane name="notification">
          <template #label>
            <span class="tab-label">
              <el-icon><Bell /></el-icon>
              通知设置
            </span>
          </template>
          <div class="notification-section">
            <h4 class="section-title">
              <el-icon><Message /></el-icon>
              通知渠道
            </h4>
            <el-form :model="notificationForm" label-width="120px" class="settings-form">
              <el-form-item label="邮件通知">
                <el-switch v-model="notificationForm.emailEnabled" active-text="开启" inactive-text="关闭" />
              </el-form-item>
              <el-form-item v-if="notificationForm.emailEnabled" label="SMTP服务器">
                <el-input v-model="notificationForm.smtpHost" placeholder="smtp.example.com" style="max-width: 350px" />
              </el-form-item>
              <el-form-item v-if="notificationForm.emailEnabled" label="发件邮箱">
                <el-input v-model="notificationForm.smtpUser" placeholder="n******@***********" style="max-width: 300px" />
              </el-form-item>
              <el-form-item label="短信通知">
                <el-switch v-model="notificationForm.smsEnabled" active-text="开启" inactive-text="关闭" />
              </el-form-item>
              <el-form-item v-if="notificationForm.smsEnabled" label="短信服务商">
                <el-select v-model="notificationForm.smsProvider" style="width: 200px">
                  <el-option label="阿里云短信" value="aliyun" />
                  <el-option label="腾讯云短信" value="tencent" />
                  <el-option label="华为云短信" value="huawei" />
                </el-select>
              </el-form-item>
              <el-form-item label="站内信">
                <el-switch v-model="notificationForm.internalEnabled" active-text="开启" inactive-text="关闭" />
                <span class="form-tip">在管理后台消息中心推送</span>
              </el-form-item>
              <el-form-item label="企业微信Webhook">
                <el-switch v-model="notificationForm.wecomEnabled" active-text="开启" inactive-text="关闭" />
              </el-form-item>
              <el-form-item v-if="notificationForm.wecomEnabled" label="Webhook地址">
                <el-input v-model="notificationForm.wecomWebhook" placeholder="https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=" style="max-width: 500px" />
              </el-form-item>
              <el-form-item label="钉钉Webhook">
                <el-switch v-model="notificationForm.dingtalkEnabled" active-text="开启" inactive-text="关闭" />
              </el-form-item>
              <el-form-item v-if="notificationForm.dingtalkEnabled" label="Webhook地址">
                <el-input v-model="notificationForm.dingtalkWebhook" placeholder="https://oapi.dingtalk.com/robot/send?access_token=" style="max-width: 500px" />
              </el-form-item>
            </el-form>
          </div>

          <div class="notification-section">
            <h4 class="section-title">
              <el-icon><User /></el-icon>
              事件通知接收人
            </h4>
            <el-table :data="notifyEvents" border class="notify-table" style="max-width: 800px">
              <el-table-column prop="eventName" label="事件类型" width="160" />
              <el-table-column label="通知渠道">
                <template #default="{ row }">
                  <el-checkbox-group v-model="row.channels">
                    <el-checkbox label="email" :disabled="!notificationForm.emailEnabled">邮件</el-checkbox>
                    <el-checkbox label="sms" :disabled="!notificationForm.smsEnabled">短信</el-checkbox>
                    <el-checkbox label="internal" :disabled="!notificationForm.internalEnabled">站内信</el-checkbox>
                    <el-checkbox label="wecom" :disabled="!notificationForm.wecomEnabled">企业微信</el-checkbox>
                    <el-checkbox label="dingtalk" :disabled="!notificationForm.dingtalkEnabled">钉钉</el-checkbox>
                  </el-checkbox-group>
                </template>
              </el-table-column>
              <el-table-column label="接收人" width="280">
                <template #default="{ row }">
                  <el-input v-model="row.receivers" placeholder="多人用逗号分隔" />
                </template>
              </el-table-column>
            </el-table>
          </div>

          <el-form label-width="120px" class="settings-form">
            <el-form-item>
              <el-button type="primary" @click="handleSave('notification')">
                <el-icon><Check /></el-icon>
                保存设置
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane name="security">
          <template #label>
            <span class="tab-label">
              <el-icon><Lock /></el-icon>
              安全设置
            </span>
          </template>
          <el-form :model="securityForm" label-width="140px" class="settings-form">
            <el-form-item label="密码最小长度">
              <el-input-number v-model="securityForm.minPasswordLength" :min="6" :max="32" controls-position="right" />
              <span class="form-tip">位</span>
            </el-form-item>
            <el-form-item label="密码复杂度要求">
              <el-checkbox-group v-model="securityForm.passwordComplexity">
                <el-checkbox label="uppercase">必须包含大写字母</el-checkbox>
                <el-checkbox label="lowercase">必须包含小写字母</el-checkbox>
                <el-checkbox label="number">必须包含数字</el-checkbox>
                <el-checkbox label="symbol">必须包含特殊字符</el-checkbox>
              </el-checkbox-group>
            </el-form-item>
            <el-form-item label="密码有效期">
              <el-input-number v-model="securityForm.passwordExpire" :min="0" :max="365" controls-position="right" />
              <span class="form-tip">天，0表示永不过期</span>
            </el-form-item>
            <el-form-item label="登录失败锁定">
              <el-switch v-model="securityForm.loginLockEnabled" active-text="开启" inactive-text="关闭" />
            </el-form-item>
            <template v-if="securityForm.loginLockEnabled">
              <el-form-item label="失败次数阈值">
                <el-input-number v-model="securityForm.loginFailCount" :min="1" :max="20" controls-position="right" />
                <span class="form-tip">次</span>
              </el-form-item>
              <el-form-item label="锁定时长">
                <el-input-number v-model="securityForm.lockDuration" :min="1" :max="1440" controls-position="right" />
                <span class="form-tip">分钟</span>
              </el-form-item>
            </template>
            <el-form-item label="IP白名单">
              <el-switch v-model="securityForm.ipWhitelistEnabled" active-text="开启" inactive-text="关闭" />
              <span class="form-tip">仅允许白名单内IP访问后台</span>
            </el-form-item>
            <el-form-item v-if="securityForm.ipWhitelistEnabled" label="白名单列表">
              <el-input
                v-model="securityForm.ipWhitelist"
                type="textarea"
                :rows="4"
                placeholder="每行一个IP或IP段，例如：&#10;192.168.1.1&#10;10.0.0.0/8"
                style="max-width: 500px"
              />
            </el-form-item>
            <el-form-item label="会话超时时间">
              <el-input-number v-model="securityForm.sessionTimeout" :min="5" :max="480" controls-position="right" />
              <span class="form-tip">分钟，无操作自动退出登录</span>
            </el-form-item>
            <el-form-item label="双因素认证">
              <el-switch v-model="securityForm.twoFactorEnabled" active-text="强制开启" inactive-text="可选" />
              <span class="form-tip">强制要求所有管理员开启2FA验证</span>
            </el-form-item>
            <el-form-item label="API签名验证">
              <el-switch v-model="securityForm.apiSignVerify" active-text="开启" inactive-text="关闭" />
              <span class="form-tip">所有API请求必须携带有效签名</span>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSave('security')">
                <el-icon><Check /></el-icon>
                保存设置
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { Setting, CreditCard, Bell, Lock, Message, User, Check, Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const activeTab = ref('basic')

const basicForm = reactive({
  systemName: '支付管理系统',
  logoUrl: '',
  homeUrl: 'https://example.com',
  icpNumber: '京ICP备12345678号',
  servicePhone: '400-888-8888',
  serviceEmail: 's******@***********'
})

const paymentForm = reactive({
  testMode: false,
  autoSettle: true,
  refundAudit: true,
  retryCount: 3,
  orderTimeout: 30,
  settleCycle: 'T+1',
  minAmount: 0.01,
  maxAmount: 50000,
  splitEnabled: false
})

const notificationForm = reactive({
  emailEnabled: true,
  smtpHost: 'smtp.example.com',
  smtpUser: 'n******@***********',
  smsEnabled: false,
  smsProvider: 'aliyun',
  internalEnabled: true,
  wecomEnabled: false,
  wecomWebhook: '',
  dingtalkEnabled: false,
  dingtalkWebhook: ''
})

interface NotifyEvent {
  key: string
  eventName: string
  channels: string[]
  receivers: string
}

const notifyEvents = ref<NotifyEvent[]>([
  { key: 'trade_success', eventName: '交易成功', channels: ['internal', 'email'], receivers: 'f***@***********' },
  { key: 'refund', eventName: '退款通知', channels: ['internal', 'email'], receivers: 'f***@***********' },
  { key: 'risk', eventName: '风控预警', channels: ['sms', 'wecom', 'internal'], receivers: '138******00,139******00' },
  { key: 'reconcile_error', eventName: '对账差错', channels: ['email', 'internal'], receivers: 'a****@***********' }
])

const securityForm = reactive({
  minPasswordLength: 8,
  passwordComplexity: ['lowercase', 'number'],
  passwordExpire: 90,
  loginLockEnabled: true,
  loginFailCount: 5,
  lockDuration: 30,
  ipWhitelistEnabled: false,
  ipWhitelist: '',
  sessionTimeout: 30,
  twoFactorEnabled: false,
  apiSignVerify: true
})

const handleSave = (_tab: string) => {
  ElMessage.success('设置保存成功')
}
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

.form-tip {
  margin-left: 12px;
  font-size: 12px;
  color: var(--text-secondary);
}

.logo-upload {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.logo-uploader {
  :deep(.el-upload) {
    width: 120px;
    height: 60px;
    border: 1px dashed var(--border-color);
    border-radius: var(--radius-md);
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    overflow: hidden;
    transition: border-color var(--transition-fast);

    &:hover {
      border-color: var(--primary-color);
    }
  }
}

.logo-preview {
  width: 120px;
  height: 60px;
  object-fit: contain;
}

.logo-uploader-icon {
  font-size: 24px;
  color: var(--text-placeholder);
}

.logo-tip {
  font-size: 12px;
  color: var(--text-secondary);

  p {
    margin: 0;
    line-height: 1.5;
  }
}

.notification-section {
  margin-bottom: 32px;
  padding-bottom: 24px;
  border-bottom: 1px solid var(--border-light);

  &:last-of-type {
    border-bottom: none;
    margin-bottom: 0;
    padding-bottom: 0;
  }
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 20px 0;
  padding-left: 0;
}

.notify-table {
  :deep(.el-checkbox-group) {
    display: flex;
    flex-wrap: wrap;
    gap: 0 16px;
  }

  :deep(.el-checkbox) {
    margin-right: 0;
  }
}
</style>
