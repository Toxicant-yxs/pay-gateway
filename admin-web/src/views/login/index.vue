<template>
  <div class="login-container">
    <div class="login-content">
      <div class="login-left">
        <div class="brand-area">
          <div class="brand-logo">
            <el-icon :size="40" color="#fff"><CreditCard /></el-icon>
          </div>
          <h1 class="brand-title">企业统一支付网关</h1>
          <p class="brand-desc">Enterprise Unified Payment Gateway</p>
        </div>
        <div class="features">
          <div class="feature-item">
            <div class="feature-icon">
              <el-icon :size="24" color="#4080FF"><Key /></el-icon>
            </div>
            <div class="feature-text">
              <h3>PCI DSS 合规</h3>
              <p>金融级安全防护，满足全球监管要求</p>
            </div>
          </div>
          <div class="feature-item">
            <div class="feature-icon">
              <el-icon :size="24" color="#00B42A"><Connection /></el-icon>
            </div>
            <div class="feature-text">
              <h3>60+ 支付渠道</h3>
              <p>一次对接，接入全球所有主流支付方式</p>
            </div>
          </div>
          <div class="feature-item">
            <div class="feature-icon">
              <el-icon :size="24" color="#FF7D00"><TrendCharts /></el-icon>
            </div>
            <div class="feature-text">
              <h3>99.99% SLA</h3>
              <p>企业级高可用，异地多活架构保障</p>
            </div>
          </div>
        </div>
      </div>
      <div class="login-right">
        <div class="login-form-wrapper">
          <h2 class="login-title">欢迎登录</h2>
          <p class="login-subtitle">请输入您的账户信息以继续</p>
          <el-form
            ref="loginFormRef"
            :model="loginForm"
            :rules="loginRules"
            class="login-form"
            @submit.prevent="handleLogin"
          >
            <el-form-item prop="username">
              <el-input
                v-model="loginForm.username"
                placeholder="请输入用户名"
                size="large"
                :prefix-icon="User"
              />
            </el-form-item>
            <el-form-item prop="password">
              <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="请输入密码"
                size="large"
                :prefix-icon="Lock"
                show-password
                @keyup.enter="handleLogin"
              />
            </el-form-item>
            <div class="login-options">
              <el-checkbox v-model="loginForm.remember">记住密码</el-checkbox>
              <a href="#" class="forgot-password">忘记密码?</a>
            </div>
            <el-button
              type="primary"
              size="large"
              class="login-btn"
              :loading="loading"
              @click="handleLogin"
            >
              登 录
            </el-button>
          </el-form>
          <div class="login-footer">
            <p>&copy; 2026 PayGateway. All rights reserved.</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { CreditCard, Key, Connection, TrendCharts, User, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const loginFormRef = ref<FormInstance>()
const loading = ref(false)

const loginForm = reactive({
  username: 'admin',
  password: 'admin123',
  remember: true
})

const loginRules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await userStore.login(loginForm.username, loginForm.password)
        ElMessage.success('登录成功')
        const redirect = (route.query.redirect as string) || '/dashboard'
        router.push(redirect)
      } catch {
        ElMessage.error('登录失败，请重试')
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style lang="scss" scoped>
.login-container {
  width: 100%;
  height: 100vh;
  display: flex;
  background: linear-gradient(135deg, #001529 0%, #002140 50%, #003366 100%);
}

.login-content {
  display: flex;
  width: 100%;
  max-width: 1200px;
  margin: auto;
  padding: 40px;
  gap: 60px;
  align-items: center;
}

.login-left {
  flex: 1;
  color: #fff;
}

.brand-area {
  margin-bottom: 60px;
}

.brand-logo {
  width: 64px;
  height: 64px;
  background: linear-gradient(135deg, #165DFF, #14C9C9);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24px;
  box-shadow: 0 8px 24px rgba(22, 93, 255, 0.4);
}

.brand-title {
  font-size: 36px;
  font-weight: 700;
  margin-bottom: 8px;
  letter-spacing: 1px;
}

.brand-desc {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.6);
  font-weight: 300;
}

.features {
  display: flex;
  flex-direction: column;
  gap: 28px;
}

.feature-item {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.feature-icon {
  flex-shrink: 0;
  width: 48px;
  height: 48px;
  background: rgba(255, 255, 255, 0.08);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.feature-text {
  h3 {
    font-size: 16px;
    font-weight: 600;
    margin-bottom: 4px;
    margin-top: 4px;
  }
  p {
    font-size: 14px;
    color: rgba(255, 255, 255, 0.5);
    margin: 0;
  }
}

.login-right {
  width: 440px;
  flex-shrink: 0;
}

.login-form-wrapper {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  padding: 48px 40px;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.login-title {
  font-size: 28px;
  font-weight: 700;
  color: #1D2129;
  margin-bottom: 8px;
}

.login-subtitle {
  font-size: 14px;
  color: #86909C;
  margin-bottom: 40px;
}

.login-form {
  :deep(.el-form-item) {
    margin-bottom: 24px;
  }
}

.login-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
}

.forgot-password {
  color: #165DFF;
  text-decoration: none;
  font-size: 14px;
  &:hover {
    text-decoration: underline;
  }
}

.login-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 500;
  letter-spacing: 2px;
}

.login-footer {
  margin-top: 40px;
  text-align: center;
  p {
    font-size: 12px;
    color: #C9CDD4;
    margin: 0;
  }
}

@media (max-width: 1024px) {
  .login-left {
    display: none;
  }
  .login-content {
    justify-content: center;
  }
  .login-right {
    width: 100%;
    max-width: 440px;
  }
}
</style>
