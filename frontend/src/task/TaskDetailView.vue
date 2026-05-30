<template>
  <div class="task-detail-page" v-loading="loading">
    <template v-if="task">
      <!-- 返回按钮 -->
      <el-button text @click="$router.push('/')">&larr; 返回列表</el-button>

      <el-card shadow="never" style="margin-top:12px">
        <!-- 标题行 -->
        <div class="detail-header">
          <h2>{{ task.title }}</h2>
          <el-tag :type="statusType">{{ statusLabel }}</el-tag>
        </div>

        <!-- 基本信息 -->
        <el-descriptions :column="2" border style="margin-top:16px">
          <el-descriptions-item label="分类">{{ task.categoryName }}</el-descriptions-item>
          <el-descriptions-item label="报酬">
            <span class="reward">{{ task.reward ? '¥' + task.reward : '免费/面议' }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="地点">{{ task.location || '不限' }}</el-descriptions-item>
          <el-descriptions-item label="发布时间">{{ formatTime(task.publishedAt) }}</el-descriptions-item>
          <el-descriptions-item label="截止时间">{{ formatTime(task.deadline) || '无截止' }}</el-descriptions-item>
        </el-descriptions>

        <!-- 描述 -->
        <div class="detail-section">
          <h4>需求详情</h4>
          <p class="description">{{ task.description }}</p>
        </div>

        <!-- 发布者信息 -->
        <div class="detail-section" v-if="task.publisher">
          <h4>发布者</h4>
          <div class="publisher-info">
            <el-avatar v-if="task.publisher.avatar" :src="task.publisher.avatar" :size="32" />
            <span>{{ task.publisher.nickname }}</span>
          </div>
        </div>

        <!-- 操作按钮（接单按钮留给订单模块开发） -->
        <div class="detail-actions" v-if="userStore.isProvider && task.status === 'published'">
          <el-button type="success" disabled>接单（下一模块开发）</el-button>
        </div>
      </el-card>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import taskApi from '@/task/taskApi'
import { useUserStore } from '@/user/userStore'

const route = useRoute()
const userStore = useUserStore()
const loading = ref(true)
const task = ref(null)

const statusMap = { published: ['已发布', 'primary'], in_progress: ['进行中', 'warning'], completed: ['已完成', 'success'], cancelled: ['已取消', 'info'] }
const statusLabel = task.value ? (statusMap[task.value.status]?.[0] || task.value.status) : ''
const statusType = task.value ? (statusMap[task.value.status]?.[1] || 'info') : 'info'

function formatTime(d) { return d ? new Date(d).toLocaleString('zh-CN') : '' }

onMounted(async () => {
  try {
    const res = await taskApi.getById(route.params.id)
    task.value = res.data.data
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.detail-header { display: flex; align-items: center; gap: 12px; }
.detail-header h2 { margin: 0; }
.reward { color: #e6a23c; font-weight: bold; }
.detail-section { margin-top: 24px; }
.detail-section h4 { margin: 0 0 8px; color: #606266; }
.description { color: #303133; line-height: 1.8; white-space: pre-wrap; }
.publisher-info { display: flex; align-items: center; gap: 8px; }
.detail-actions { margin-top: 24px; text-align: center; }
</style>
