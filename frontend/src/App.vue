<template>
  <div id="app-container" :class="{ 'dark': isDark }">
    <!-- 顶部导航栏 - 毛玻璃效果 -->
    <header class="navbar">
      <div class="navbar-inner">
        <!-- 左侧 Logo -->
        <router-link to="/" class="logo">
          <div class="logo-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M12 2L2 7l10 5 10-5-10-5z"/>
              <path d="M2 17l10 5 10-5"/>
              <path d="M2 12l10 5 10-5"/>
            </svg>
          </div>
          <span class="logo-text">CampusHub</span>
        </router-link>

        <!-- 导航链接 -->
        <nav class="nav-links" v-if="userStore.isLoggedIn">
          <router-link to="/" class="nav-item" active-class="active">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="3" width="7" height="7" rx="1"/>
              <rect x="14" y="3" width="7" height="7" rx="1"/>
              <rect x="14" y="14" width="7" height="7" rx="1"/>
              <rect x="3" y="14" width="7" height="7" rx="1"/>
            </svg>
            需求广场
          </router-link>
          <router-link v-if="userStore.isRequester" to="/tasks/publish" class="nav-item" active-class="active">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"/>
              <line x1="12" y1="8" x2="12" y2="16"/>
              <line x1="8" y1="12" x2="16" y2="12"/>
            </svg>
            发布需求
          </router-link>
          <router-link to="/my-tasks" class="nav-item" active-class="active">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M16 4h2a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h2"/>
              <rect x="8" y="2" width="8" height="4" rx="1" ry="1"/>
              <path d="M9 14l2 2 4-4"/>
            </svg>
            我的需求
          </router-link>
          <router-link v-if="userStore.isAdmin" to="/admin" class="nav-item" active-class="active">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="3"/>
              <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"/>
            </svg>
            后台管理
          </router-link>
        </nav>

        <!-- 右侧操作区 -->
        <div class="nav-actions">
          <!-- 角色切换按钮 -->
          <button
            v-if="userStore.isLoggedIn && !userStore.isAdmin"
            class="role-switch-btn"
            @click="userStore.switchRole()"
          >
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M16 3l4 4-4 4"/>
              <path d="M20 7H4"/>
              <path d="M8 21l-4-4 4-4"/>
              <path d="M4 17h16"/>
            </svg>
            <span>切换为{{ userStore.isRequester ? '服务方' : '需求方' }}</span>
          </button>

          <!-- 管理员标识 -->
          <span v-if="userStore.isLoggedIn && userStore.isAdmin" class="admin-badge">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
            </svg>
            管理员
          </span>

          <!-- 用户切换下拉 -->
          <el-select v-model="currentUserId" placeholder="切换用户" size="small"
            class="user-select" @change="switchUser">
            <el-option label="(未登录)" value="" />
            <el-option label="张三(需求方)" :value="1" />
            <el-option label="李四(服务方)" :value="2" />
            <el-option label="管理员" :value="3" />
          </el-select>
        </div>
      </div>
    </header>

    <!-- 主内容区 -->
    <main class="main-content">
      <router-view v-slot="{ Component }">
        <transition name="page-fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>

    <!-- 底部装饰 -->
    <div class="bg-decoration">
      <div class="blob blob-1"></div>
      <div class="blob blob-2"></div>
      <div class="blob blob-3"></div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useUserStore } from '@/user/userStore'

const userStore = useUserStore()
const currentUserId = ref(null)
const isDark = ref(false)

async function switchUser(userId) {
  if (!userId || userId === '') { userStore.logout(); return }
  const accounts = { 1: '20240001', 2: '20240002', 3: 'admin001' }
  const roleMap = { 1: 'requester', 2: 'provider', 3: 'admin' }
  const nameMap = { 1: '张三(需求方)', 2: '李四(服务方)', 3: '管理员' }
  try {
    const { default: http } = await import('@/common/http')
    const res = await http.post('/auth/login/password', {
      account: accounts[userId], password: 'Test123456'
    })
    const d = res.data.data
    userStore.login({
      id: d.userId, name: nameMap[userId], nickname: d.nickname,
      role: roleMap[userId], avatar: null, creditScore: 5.0
    }, d.token)
  } catch { userStore.logout() }
}
</script>

<style>
/* ===== CSS Variables - 设计系统 ===== */
:root {
  /* 主色调 - 清新薄荷绿 */
  --primary: #10b981;
  --primary-light: #34d399;
  --primary-dark: #059669;
  
  /* 强调色 - 温暖橙 */
  --accent: #f59e0b;
  --accent-light: #fbbf24;
  --accent-dark: #d97706;
  
  /* 中性色 */
  --bg-primary: #f8fafc;
  --bg-secondary: #ffffff;
  --bg-tertiary: #f1f5f9;
  
  --text-primary: #0f172a;
  --text-secondary: #475569;
  --text-muted: #94a3b8;
  
  /* 状态色 */
  --success: #22c55e;
  --warning: #f59e0b;
  --error: #ef4444;
  --info: #3b82f6;
  
  /* 边框和阴影 */
  --border-color: #e2e8f0;
  --shadow-sm: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
  --shadow-md: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -2px rgba(0, 0, 0, 0.1);
  --shadow-lg: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -4px rgba(0, 0, 0, 0.1);
  --shadow-xl: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 8px 10px -6px rgba(0, 0, 0, 0.1);
  
  /* 圆角 */
  --radius-sm: 6px;
  --radius-md: 12px;
  --radius-lg: 16px;
  --radius-xl: 24px;
  --radius-full: 9999px;
  
  /* 动画 */
  --transition-fast: 150ms ease;
  --transition-base: 250ms ease;
  --transition-slow: 350ms ease;
}

/* ===== 全局样式重置 ===== */
*, *::before, *::after {
  box-sizing: border-box;
}

body {
  margin: 0;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'PingFang SC', 'Hiragino Sans GB', sans-serif;
  background: var(--bg-primary);
  color: var(--text-primary);
  line-height: 1.6;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

/* ===== 主容器 ===== */
#app-container {
  min-height: 100vh;
  position: relative;
  overflow-x: hidden;
}

/* ===== 导航栏 - 毛玻璃效果 ===== */
.navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(226, 232, 240, 0.8);
}

.navbar-inner {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 24px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 32px;
}

/* Logo */
.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
  color: var(--text-primary);
  font-weight: 700;
  font-size: 20px;
  transition: var(--transition-base);
}

.logo:hover {
  transform: scale(1.02);
}

.logo-icon {
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, var(--primary), var(--primary-light));
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.3);
}

.logo-icon svg {
  width: 20px;
  height: 20px;
}

.logo-text {
  background: linear-gradient(135deg, var(--primary-dark), var(--primary));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* 导航链接 */
.nav-links {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  justify-content: center;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: var(--radius-full);
  text-decoration: none;
  color: var(--text-secondary);
  font-size: 14px;
  font-weight: 500;
  transition: var(--transition-base);
  position: relative;
}

.nav-item svg {
  width: 18px;
  height: 18px;
  flex-shrink: 0;
}

.nav-item:hover {
  color: var(--primary);
  background: rgba(16, 185, 129, 0.08);
}

.nav-item.active {
  color: var(--primary);
  background: rgba(16, 185, 129, 0.12);
}

/* 右侧操作区 */
.nav-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.role-switch-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-full);
  background: var(--bg-secondary);
  color: var(--text-secondary);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: var(--transition-base);
}

.role-switch-btn:hover {
  border-color: var(--primary);
  color: var(--primary);
  background: rgba(16, 185, 129, 0.04);
}

.role-switch-btn svg {
  width: 16px;
  height: 16px;
}

.admin-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  background: linear-gradient(135deg, var(--accent), var(--accent-light));
  color: white;
  font-size: 12px;
  font-weight: 600;
  border-radius: var(--radius-full);
  box-shadow: 0 2px 8px rgba(245, 158, 11, 0.3);
}

.admin-badge svg {
  width: 14px;
  height: 14px;
}

.user-select {
  width: 140px;
}

/* ===== 主内容区 ===== */
.main-content {
  padding-top: 88px;
  padding-bottom: 40px;
  min-height: 100vh;
  max-width: 1400px;
  margin: 0 auto;
  padding-left: 24px;
  padding-right: 24px;
}

/* ===== 页面过渡动画 ===== */
.page-fade-enter-active,
.page-fade-leave-active {
  transition: opacity 0.25s ease, transform 0.25s ease;
}

.page-fade-enter-from {
  opacity: 0;
  transform: translateY(10px);
}

.page-fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

/* ===== 背景装饰 - 流动的渐变球 ===== */
.bg-decoration {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  z-index: -1;
  overflow: hidden;
}

.blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.5;
  animation: blob-float 20s ease-in-out infinite;
}

.blob-1 {
  width: 600px;
  height: 600px;
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.15), rgba(52, 211, 153, 0.1));
  top: -200px;
  right: -200px;
  animation-delay: 0s;
}

.blob-2 {
  width: 500px;
  height: 500px;
  background: linear-gradient(135deg, rgba(245, 158, 11, 0.1), rgba(251, 191, 36, 0.08));
  bottom: -150px;
  left: -150px;
  animation-delay: -5s;
}

.blob-3 {
  width: 400px;
  height: 400px;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.08), rgba(147, 197, 253, 0.05));
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  animation-delay: -10s;
}

@keyframes blob-float {
  0%, 100% {
    transform: translate(0, 0) scale(1);
  }
  25% {
    transform: translate(30px, -30px) scale(1.05);
  }
  50% {
    transform: translate(-20px, 20px) scale(0.95);
  }
  75% {
    transform: translate(20px, 40px) scale(1.02);
  }
}

/* ===== Element Plus 样式覆盖 ===== */
.el-card {
  border: none !important;
  border-radius: var(--radius-lg) !important;
  box-shadow: var(--shadow-md) !important;
  transition: var(--transition-base) !important;
}

.el-card:hover {
  box-shadow: var(--shadow-lg) !important;
}

.el-button--primary {
  background: linear-gradient(135deg, var(--primary), var(--primary-light)) !important;
  border: none !important;
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.25) !important;
}

.el-button--primary:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(16, 185, 129, 0.35) !important;
}

.el-input__wrapper {
  border-radius: var(--radius-md) !important;
  box-shadow: none !important;
  border: 1px solid var(--border-color) !important;
  transition: var(--transition-base) !important;
}

.el-input__wrapper:hover,
.el-input__wrapper:focus-within {
  border-color: var(--primary) !important;
}

.el-select__wrapper {
  border-radius: var(--radius-md) !important;
}

.el-tag {
  border-radius: var(--radius-full) !important;
  border: none !important;
  font-weight: 500 !important;
}

.el-tag--primary {
  background: rgba(16, 185, 129, 0.1) !important;
  color: var(--primary-dark) !important;
}

.el-tag--warning {
  background: rgba(245, 158, 11, 0.1) !important;
  color: var(--accent-dark) !important;
}

.el-tag--success {
  background: rgba(34, 197, 94, 0.1) !important;
  color: #15803d !important;
}

.el-tag--info {
  background: rgba(148, 163, 184, 0.15) !important;
  color: var(--text-secondary) !important;
}

/* ===== 响应式 ===== */
@media (max-width: 768px) {
  .navbar-inner {
    padding: 0 16px;
  }
  
  .nav-links {
    display: none;
  }
  
  .logo-text {
    display: none;
  }
  
  .main-content {
    padding-left: 16px;
    padding-right: 16px;
  }
  
  .role-switch-btn span {
    display: none;
  }
}
</style>
