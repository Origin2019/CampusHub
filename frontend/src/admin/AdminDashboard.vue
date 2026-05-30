<template>
  <div class="admin-dashboard" v-loading="loading">
    <h2>管理后台 — 仪表盘</h2>

    <!-- 快捷入口 -->
    <div class="quick-actions">
      <el-button type="primary" @click="$router.push('/admin/users')">用户管理</el-button>
      <el-button type="primary" @click="$router.push('/admin/tasks')">需求管理</el-button>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="16">
      <!-- 用户统计 — 点击进入用户管理 -->
      <el-col :span="6" v-for="c in userCards" :key="c.label">
        <el-card shadow="hover" class="stat-card clickable" @click="$router.push('/admin/users')">
          <div class="stat-value">{{ c.value }}</div>
          <div class="stat-label">{{ c.label }}</div>
        </el-card>
      </el-col>
      <!-- 需求统计 — 点击进入需求管理 -->
      <el-col :span="6" v-for="c in taskCards" :key="c.label">
        <el-card shadow="hover" class="stat-card clickable" @click="$router.push('/admin/tasks')">
          <div class="stat-value">{{ c.value }}</div>
          <div class="stat-label">{{ c.label }}</div>
        </el-card>
      </el-col>
      <!-- 信息卡片 — 不可点击 -->
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value">{{ stats.totalOrders || 0 }}</div>
          <div class="stat-label">已取消需求</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 管理操作说明 -->
    <el-card shadow="never" style="margin-top:24px">
      <h4>管理操作指南</h4>
      <p>用户管理：查看全部用户，按角色/关键词筛选，启用或禁用用户账号。</p>
      <p>需求管理：查看全部需求，按状态筛选，强制删除违规需求。</p>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import adminApi from '@/admin/adminApi'

const loading = ref(false)
const stats = reactive({})

const userCards = reactive([
  { label: '总用户 →', value: 0, key: 'totalUsers' },
  { label: '禁用用户', value: 0, key: 'bannedUsers' }
])
const taskCards = reactive([
  { label: '总需求 →', value: 0, key: 'totalTasks' },
  { label: '待接单', value: 0, key: 'publishedTasks' },
  { label: '进行中', value: 0, key: 'inProgressTasks' },
  { label: '已完成', value: 0, key: 'completedTasks' },
  { label: '今日新增', value: 0, key: 'todayNewTasks' }
])

onMounted(async () => {
  loading.value = true
  try {
    const res = await adminApi.getStats()
    Object.assign(stats, res.data?.data || {})
  } finally {
    userCards.forEach(c => { c.value = stats[c.key] ?? 0 })
    taskCards.forEach(c => { c.value = stats[c.key] ?? 0 })
    loading.value = false
  }
})
</script>

<style scoped>
.quick-actions { margin-bottom: 20px; display: flex; gap: 12px; }
.stat-card { text-align: center; margin-bottom: 16px; }
.stat-card.clickable { cursor: pointer; }
.stat-card.clickable:hover { border-color: #409eff; }
.stat-value { font-size: 28px; font-weight: bold; color: #409eff; }
.stat-label { font-size: 13px; color: #909399; margin-top: 4px; }
</style>
