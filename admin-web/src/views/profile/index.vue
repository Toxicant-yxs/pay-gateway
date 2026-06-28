<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">个人中心</h2>
      <p class="page-desc">管理个人信息、修改密码、查看登录记录</p>
    </div>

    <div class="profile-layout">
      <div class="profile-left">
        <div class="card-shadow profile-card">
          <div class="avatar-section">
            <el-avatar :size="80" class="big-avatar">
              <img :src="userAvatar" alt="avatar" />
            </el-avatar>
            <h3 class="user-name">{{ displayName }}</h3>
            <el-tag type="primary" size="small">超级管理员</el-tag>
          </div>
          <el-divider />
          <div class="info-list">
            <div class="info-item">
              <el-icon :size="16"><User /></el-icon>
              <span class="info-label">用户ID</span>
              <span class="info-value">U100001</span>
            </div>
            <div class="info-item">
              <el-icon :size="16"><Message /></el-icon>
              <span class="info-label">邮箱</span>
              <span class="info-value">a****@****************</span>
            </div>
            <div class="info-item">
              <el-icon :size="16"><Phone /></el-icon>
              <span class="info-label">手机</span>
              <span class="info-value">138****8888</span>
            </div>
            <div class="info-item">
              <el-icon :size="16"><OfficeBuilding /></el-icon>
              <span class="info-label">部门</span>
              <span class="info-value">运营中心</span>
            </div>
            <div class="info-item">
              <el-icon :size="16"><Clock /></el-icon>
              <span class="info-label">上次登录</span>
              <span class="info-value">2026-06-28 09:15:32</span>
            </div>
          </div>
        </div>
      </div>

      <div class="profile-right">
        <el-tabs v-model="activeTab" class="profile-tabs">
          <el-tab-pane label="基本信息" name="base">
            <div class="card-shadow">
              <el-form :model="baseForm" label-width="100px" class="profile-form">
                <el-form-item label="用户名">
                  <el-input v-model="baseForm.username" disabled />
                </el-form-item>
                <el-form-item label="真实姓名">
                  <el-input v-model="baseForm.realName" placeholder="请输入真实姓名" />
                </el-form-item>
                <el-form-item label="邮箱">
                  <el-input v-model="baseForm.email" placeholder="请输入邮箱" />
                </el-form-item>
                <el-form-item label="手机号">
                  <el-input v-model="baseForm.phone" placeholder="请输入手机号" />
                </el-form-item>
                <el-form-item label="所属部门">
                  <el-input v-model="baseForm.department" disabled />
                </el-form-item>
                <el-form-item label="角色">
                  <el-input v-model="baseForm.role" disabled />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="saveBase">保存修改</el-button>
                </el-form-item>
              </el-form>
            </div>
          </el-tab-pane>

          <el-tab-pane label="修改密码" name="password">
            <div class="card-shadow">
              <el-form :model="pwdForm" :rules="pwdRules" ref="pwdFormRef" label-width="100px" class="profile-form">
                <el-form-item label="当前密码" prop="oldPassword">
                  <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入当前密码" />
                </el-form-item>
                <el-form-item label="新密码" prop="newPassword">
                  <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="请输入新密码（8-20位，含字母和数字）" />
                </el-form-item>
                <el-form-item label="确认密码" prop="confirmPassword">
                  <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="changePassword">确认修改</el-button>
                  <el-button @click="resetPwdForm">重置</el-button>
                </el-form-item>
              </el-form>
            </div>
          </el-tab-pane>

          <el-tab-pane label="登录记录" name="logs">
            <div class="card-shadow">
              <el-table :data="loginLogs" stripe>
                <el-table-column prop="time" label="登录时间" width="180" />
                <el-table-column prop="ip" label="IP地址" width="150" />
                <el-table-column prop="location" label="登录地点" width="180" />
                <el-table-column prop="browser" label="浏览器" width="160" />
                <el-table-column prop="os" label="操作系统" width="140" />
                <el-table-column prop="status" label="状态" width="100">
                  <template #default="{ row }">
                    <el-tag :type="row.status === '成功' ? 'success' : 'danger'" size="small">{{ row.status }}</el-tag>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { User, Message, Phone, OfficeBuilding, Clock } from '@element-plus/icons-vue'
import { useUserStore } from '../../stores/user'

const userStore = useUserStore()
const activeTab = ref('base')
const pwdFormRef = ref<FormInstance>()

const displayName = computed(() => userStore.username || '超级管理员')
const userAvatar = computed(() => userStore.avatar || 'https://api.dicebear.com/7.x/avataaars/svg?seed=admin')

const baseForm = reactive({
  username: 'admin',
  realName: '系********',
  email: 'a****@****************',
  phone: '13888888888',
  department: '运营中心',
  role: '超级管理员'
})

const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirm = (_rule: any, value: string, callback: any) => {
  if (value !== pwdForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const pwdRules: FormRules = {
  oldPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 8, max: 20, message: '密码长度为8-20位', trigger: 'blur' },
    { pattern: /^(?=.*[a-zA-Z])(?=.*\d)/, message: '密码必须包含字母和数字', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
  ]
}

const loginLogs = [
  { time: '2026-06-29 09:15:32', ip: '192.168.1.100', location: '北京市 联通', browser: 'Chrome 124.0', os: 'Windows 11', status: '成功' },
  { time: '2026-06-28 09:02:18', ip: '192.168.1.100', location: '北京市 联通', browser: 'Chrome 124.0', os: 'Windows 11', status: '成功' },
  { time: '2026-06-27 14:33:45', ip: '192.168.1.102', location: '北京市 联通', browser: 'Firefox 125.0', os: 'macOS 14.0', status: '成功' },
  { time: '2026-06-27 08:45:12', ip: '192.168.1.100', location: '北京市 联通', browser: 'Chrome 124.0', os: 'Windows 11', status: '成功' },
  { time: '2026-06-26 22:18:09', ip: '10.0.0.55', location: '内网IP', browser: 'Chrome 124.0', os: 'Windows 11', status: '失败' },
  { time: '2026-06-26 09:10:33', ip: '192.168.1.100', location: '北京市 联通', browser: 'Chrome 124.0', os: 'Windows 11', status: '成功' }
]

const saveBase = () => {
  ElMessage.success('基本信息保存成功')
}

const changePassword = async () => {
  if (!pwdFormRef.value) return
  await pwdFormRef.value.validate()
  ElMessage.success('密码修改成功，请重新登录')
}

const resetPwdForm = () => {
  pwdFormRef.value?.resetFields()
}
</script>

<style lang="scss" scoped>
.profile-layout {
  display: flex;
  gap: 20px;
}

.profile-left {
  width: 280px;
  flex-shrink: 0;
}

.profile-card {
  padding: 24px;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding-bottom: 8px;
}

.big-avatar {
  background: var(--primary-bg);
}

.user-name {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
}

.info-list {
  padding: 8px 0;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 0;
  color: var(--text-regular);
  font-size: 13px;

  .el-icon {
    color: var(--text-secondary);
    flex-shrink: 0;
  }
}

.info-label {
  color: var(--text-secondary);
  width: 60px;
  flex-shrink: 0;
}

.info-value {
  color: var(--text-primary);
  flex: 1;
  text-align: right;
}

.profile-right {
  flex: 1;
  min-width: 0;
}

.profile-form {
  padding: 24px;
  max-width: 600px;
}
</style>
