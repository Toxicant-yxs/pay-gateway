<template>
  <div class="error-page">
    <div class="error-content">
      <div class="error-illustration">
        <svg viewBox="0 0 404 300" xmlns="http://www.w3.org/2000/svg">
          <defs>
            <linearGradient id="grad1" x1="0%" y1="0%" x2="100%" y2="100%">
              <stop offset="0%" style="stop-color:#165DFF;stop-opacity:0.1" />
              <stop offset="100%" style="stop-color:#165DFF;stop-opacity:0.05" />
            </linearGradient>
          </defs>
          <circle cx="200" cy="150" r="120" fill="url(#grad1)" />
          <text x="200" y="140" text-anchor="middle" class="error-code">404</text>
          <text x="200" y="185" text-anchor="middle" class="error-sub">页面未找到</text>
          <circle cx="120" cy="100" r="8" fill="#165DFF" opacity="0.3">
            <animate attributeName="cy" values="100;90;100" dur="3s" repeatCount="indefinite" />
          </circle>
          <circle cx="290" cy="110" r="6" fill="#00B42A" opacity="0.3">
            <animate attributeName="cy" values="110;120;110" dur="2.5s" repeatCount="indefinite" />
          </circle>
          <circle cx="310" cy="200" r="10" fill="#FF7D00" opacity="0.2">
            <animate attributeName="cy" values="200;190;200" dur="3.5s" repeatCount="indefinite" />
          </circle>
          <circle cx="100" cy="200" r="5" fill="#F53F3F" opacity="0.3">
            <animate attributeName="cy" values="200;210;200" dur="2s" repeatCount="indefinite" />
          </circle>
        </svg>
      </div>
      <h2 class="error-title">抱歉，您访问的页面不存在</h2>
      <p class="error-desc">请检查您输入的网址是否正确，或返回首页继续操作</p>
      <div class="error-actions">
        <el-button type="primary" size="large" @click="goHome">
          <el-icon><HomeFilled /></el-icon>返回首页
        </el-button>
        <el-button size="large" @click="goBack">
          <el-icon><Back /></el-icon>返回上一页
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { HomeFilled, Back } from '@element-plus/icons-vue'

const router = useRouter()

const goHome = () => {
  router.replace('/dashboard')
}

const goBack = () => {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.replace('/dashboard')
  }
}
</script>

<style lang="scss" scoped>
.error-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-page);
  padding: 20px;
}

.error-content {
  text-align: center;
}

.error-illustration {
  width: 400px;
  max-width: 100%;
  margin: 0 auto 24px;

  svg {
    width: 100%;
    height: auto;
  }

  .error-code {
    font-size: 72px;
    font-weight: 800;
    fill: var(--primary-color);
    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  }

  .error-sub {
    font-size: 18px;
    fill: var(--text-secondary);
    font-weight: 500;
  }
}

.error-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 12px;
}

.error-desc {
  font-size: 14px;
  color: var(--text-secondary);
  margin: 0 0 32px;
}

.error-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
  flex-wrap: wrap;
}
</style>
