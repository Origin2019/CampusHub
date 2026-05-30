<template>
  <div id="app-container">
    <el-container>
      <!-- 顶部导航栏 -->
      <el-header height="56px">
        <!-- 左侧 Logo -->
        <div class="header-left">
          <router-link to="/" class="logo">CampusHub</router-link>
        </div>

        <!-- 导航链接（竖线分隔，均匀占满顶栏） -->
        <nav class="header-nav" v-if="userStore.isLoggedIn">
          <router-link to="/">需求广场</router-link>
          <span class="sep">|</span>
          <router-link v-if="userStore.isRequester" to="/tasks/publish">发布需求</router-link>
          <span v-if="userStore.isRequester" class="sep">|</span>
          <router-link to="/my-tasks">我的需求</router-link>
          <template v-if="userStore.isAdmin">
            <span class="sep">|</span>
            <router-link to="/admin">后台管理</router-link>
          </template>
        </nav>

        <!-- 右侧操作区 -->
        <div class="header-right">
          <!-- 普通用户：身份切换按钮 -->
          <el-button
            v-if="userStore.isLoggedIn && !userStore.isAdmin"
            size="small"
            text
            style="color:#fff"
            @click="userStore.switchRole()"
          >
            切换为{{ userStore.isRequester ? '服务方' : '需求方' }}
          </el-button>

          <!-- 管理员标识 -->
          <span v-if="userStore.isLoggedIn && userStore.isAdmin" class="admin-badge">管理员</span>

          <!-- 测试用：用户切换下拉 -->
          <el-select v-model="currentUserId" placeholder="切用户" size="small"
            style="width:140px;margin-left:12px" @change="switchUser">
            <el-option label="(未登录)" value="" />
            <el-option label="张三(需求方)" :value="1" />
            <el-option label="李四(服务方)" :value="2" />
            <el-option label="管理员" :value="3" />
          </el-select>
        </div>
      </el-header>

      <!-- 主内容区 -->
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useUserStore } from '@/user/userStore'

const userStore = useUserStore()
const currentUserId = ref(null)

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
body { margin: 0; font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', sans-serif; }
#app-container { min-height: 100vh; background: #f5f7fa; }

/* 顶栏 */
.el-header {
  display: flex; align-items: center;
  background: #409eff; color: #fff; padding: 0 20px; gap: 24px;
}

/* Logo */
.logo { color: #fff; font-size: 18px; font-weight: bold; text-decoration: none; white-space: nowrap; }

/* 导航链接区 */
.header-nav {
  flex: 1; display: flex; align-items: center;
  justify-content: space-around; padding: 0 12px;
}
.header-nav a {
  color: rgba(255,255,255,0.85); text-decoration: none; font-size: 15px; white-space: nowrap;
}
.header-nav a:hover { color: #fff; }
.header-nav a.router-link-active { color: #fff; font-weight: bold; }
.header-nav .sep { color: rgba(255,255,255,0.4); font-size: 14px; user-select: none; }

/* 右侧操作区 */
.header-right { display: flex; align-items: center; white-space: nowrap; }
.admin-badge { background: #e6a23c; color: #fff; padding: 2px 10px; border-radius: 10px; font-size: 12px; }

/* 主内容 */
.el-main { max-width: 100%; margin: 0 auto; padding: 24px 32px; }
</style>
