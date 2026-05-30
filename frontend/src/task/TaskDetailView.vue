<template>
  <div class="task-detail-page" v-loading="loading">
    <template v-if="task">
      <!-- 返回按钮 -->
      <button class="back-btn" @click="$router.push('/')">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M19 12H5M12 19l-7-7 7-7"/>
        </svg>
        返回列表
      </button>

      <!-- 主内容区 -->
      <div class="detail-container">
        <!-- 左侧：主要信息 -->
        <div class="detail-main">
          <!-- 标题区 -->
          <div class="detail-header">
            <div class="header-top">
              <span class="status-badge" :class="statusClass">
                <span class="status-dot"></span>
                {{ statusLabel }}
              </span>
              <span class="category-badge">{{ task.categoryName }}</span>
            </div>
            <h1 class="detail-title">{{ task.title }}</h1>
            <div class="header-meta">
              <span class="meta-item">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
                  <line x1="16" y1="2" x2="16" y2="6"/>
                  <line x1="8" y1="2" x2="8" y2="6"/>
                  <line x1="3" y1="10" x2="21" y2="10"/>
                </svg>
                {{ formatTime(task.publishedAt) }}
              </span>
              <span class="meta-item">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0118 0z"/>
                  <circle cx="12" cy="10" r="3"/>
                </svg>
                {{ task.location || '不限地点' }}
              </span>
            </div>
          </div>

          <!-- 详情卡片 -->
          <div class="detail-card">
            <h3 class="card-section-title">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z"/>
                <polyline points="14,2 14,8 20,8"/>
                <line x1="16" y1="13" x2="8" y2="13"/>
                <line x1="16" y1="17" x2="8" y2="17"/>
                <polyline points="10,9 9,9 8,9"/>
              </svg>
              需求详情
            </h3>
            <p class="description">{{ task.description }}</p>
          </div>

          <!-- 时间信息 -->
          <div class="detail-card">
            <h3 class="card-section-title">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"/>
                <polyline points="12 6 12 12 16 14"/>
              </svg>
              时间信息
            </h3>
            <div class="time-grid">
              <div class="time-item">
                <span class="time-label">发布时间</span>
                <span class="time-value">{{ formatTime(task.publishedAt) }}</span>
              </div>
              <div class="time-item">
                <span class="time-label">截止时间</span>
                <span class="time-value" :class="{ 'urgent': isUrgent }">
                  {{ formatTime(task.deadline) || '无截止' }}
                  <span v-if="isUrgent" class="urgent-tag">即将截止</span>
                </span>
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧：操作区 -->
        <div class="detail-sidebar">
          <!-- 报酬卡片 -->
          <div class="reward-card">
            <span class="reward-label">任务报酬</span>
            <div class="reward-amount" v-if="task.reward">
              <span class="currency">&#xFFE5;</span>
              <span class="amount">{{ task.reward }}</span>
            </div>
            <div class="reward-amount free" v-else>
              免费互助
            </div>
          </div>

          <!-- 发布者信息 -->
          <div class="publisher-card" v-if="task.publisher">
            <h4>发布者</h4>
            <div class="publisher-info">
              <div class="publisher-avatar">
                <img v-if="task.publisher.avatar" :src="task.publisher.avatar" alt="" />
                <span v-else class="avatar-placeholder">{{ task.publisher.nickname?.charAt(0) || 'U' }}</span>
              </div>
              <div class="publisher-details">
                <span class="publisher-name">{{ task.publisher.nickname }}</span>
                <span class="publisher-meta">需求方</span>
              </div>
            </div>
          </div>

          <!-- 操作按钮 -->
          <div class="action-buttons" v-if="userStore.isProvider && task.status === 'published'">
            <button class="accept-btn" disabled>
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M22 11.08V12a10 10 0 11-5.93-9.14"/>
                <polyline points="22 4 12 14.01 9 11.01"/>
              </svg>
              接单（开发中）
            </button>
            <p class="action-hint">接单功能将在下一版本上线</p>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import taskApi from '@/task/taskApi'
import { useUserStore } from '@/user/userStore'

const route = useRoute()
const userStore = useUserStore()
const loading = ref(true)
const task = ref(null)

const statusMap = {
  published: ['待接单', 'published'],
  in_progress: ['进行中', 'in-progress'],
  completed: ['已完成', 'completed'],
  cancelled: ['已取消', 'cancelled']
}

const statusLabel = computed(() => task.value ? (statusMap[task.value.status]?.[0] || task.value.status) : '')
const statusClass = computed(() => task.value ? (statusMap[task.value.status]?.[1] || 'default') : 'default')

const isUrgent = computed(() => {
  if (!task.value?.deadline) return false
  const diff = new Date(task.value.deadline) - new Date()
  return diff > 0 && diff < 86400000 * 2 // 2天内
})

function formatTime(d) { 
  if (!d) return ''
  return new Date(d).toLocaleString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

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
.task-detail-page {
  animation: fade-in 0.5s ease;
}

@keyframes fade-in {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

/* 返回按钮 */
.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-full);
  background: var(--bg-secondary);
  color: var(--text-secondary);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all var(--transition-base);
  margin-bottom: 24px;
}

.back-btn:hover {
  border-color: var(--primary);
  color: var(--primary);
  background: rgba(16, 185, 129, 0.04);
}

.back-btn svg {
  width: 18px;
  height: 18px;
}

/* 主容器 */
.detail-container {
  display: grid;
  grid-template-columns: 1fr 360px;
  gap: 24px;
  align-items: start;
}

@media (max-width: 1024px) {
  .detail-container {
    grid-template-columns: 1fr;
  }
}

/* 左侧主内容 */
.detail-main {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 头部区域 */
.detail-header {
  background: var(--bg-secondary);
  border-radius: var(--radius-xl);
  padding: 28px;
  border: 1px solid var(--border-color);
}

.header-top {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  border-radius: var(--radius-full);
  font-size: 13px;
  font-weight: 600;
  background: rgba(16, 185, 129, 0.1);
  color: var(--primary-dark);
}

.status-badge.in-progress {
  background: rgba(245, 158, 11, 0.1);
  color: var(--accent-dark);
}

.status-badge.completed {
  background: rgba(34, 197, 94, 0.1);
  color: #15803d;
}

.status-badge.cancelled {
  background: var(--bg-tertiary);
  color: var(--text-muted);
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: currentColor;
}

.category-badge {
  padding: 6px 14px;
  border-radius: var(--radius-full);
  font-size: 13px;
  font-weight: 500;
  background: var(--bg-tertiary);
  color: var(--text-secondary);
}

.detail-title {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 16px;
  line-height: 1.3;
}

.header-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: var(--text-secondary);
}

.meta-item svg {
  width: 16px;
  height: 16px;
  color: var(--text-muted);
}

/* 详情卡片 */
.detail-card {
  background: var(--bg-secondary);
  border-radius: var(--radius-xl);
  padding: 24px;
  border: 1px solid var(--border-color);
}

.card-section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 16px;
}

.card-section-title svg {
  width: 20px;
  height: 20px;
  color: var(--primary);
}

.description {
  font-size: 15px;
  line-height: 1.8;
  color: var(--text-secondary);
  margin: 0;
  white-space: pre-wrap;
}

/* 时间网格 */
.time-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.time-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.time-label {
  font-size: 12px;
  font-weight: 500;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.time-value {
  font-size: 15px;
  font-weight: 500;
  color: var(--text-primary);
  display: flex;
  align-items: center;
  gap: 8px;
}

.time-value.urgent {
  color: var(--error);
}

.urgent-tag {
  padding: 2px 8px;
  background: rgba(239, 68, 68, 0.1);
  color: var(--error);
  font-size: 11px;
  font-weight: 600;
  border-radius: var(--radius-full);
}

/* 右侧边栏 */
.detail-sidebar {
  display: flex;
  flex-direction: column;
  gap: 16px;
  position: sticky;
  top: 88px;
}

/* 报酬卡片 */
.reward-card {
  background: linear-gradient(135deg, var(--primary), var(--primary-light));
  border-radius: var(--radius-xl);
  padding: 28px;
  text-align: center;
  color: white;
  box-shadow: 0 8px 24px rgba(16, 185, 129, 0.25);
}

.reward-label {
  font-size: 13px;
  font-weight: 500;
  opacity: 0.9;
  display: block;
  margin-bottom: 8px;
}

.reward-amount {
  font-size: 40px;
  font-weight: 700;
  display: flex;
  align-items: baseline;
  justify-content: center;
}

.reward-amount .currency {
  font-size: 24px;
  margin-right: 4px;
}

.reward-amount.free {
  font-size: 24px;
}

/* 发布者卡片 */
.publisher-card {
  background: var(--bg-secondary);
  border-radius: var(--radius-xl);
  padding: 20px;
  border: 1px solid var(--border-color);
}

.publisher-card h4 {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-muted);
  margin: 0 0 16px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.publisher-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.publisher-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  overflow: hidden;
  background: linear-gradient(135deg, var(--primary-light), var(--primary));
  display: flex;
  align-items: center;
  justify-content: center;
}

.publisher-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder {
  color: white;
  font-size: 20px;
  font-weight: 600;
}

.publisher-details {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.publisher-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.publisher-meta {
  font-size: 13px;
  color: var(--text-muted);
}

/* 操作按钮 */
.action-buttons {
  background: var(--bg-secondary);
  border-radius: var(--radius-xl);
  padding: 20px;
  border: 1px solid var(--border-color);
}

.accept-btn {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 14px 24px;
  border: none;
  border-radius: var(--radius-lg);
  background: linear-gradient(135deg, var(--success), #4ade80);
  color: white;
  font-size: 15px;
  font-weight: 600;
  cursor: not-allowed;
  opacity: 0.6;
  transition: all var(--transition-base);
}

.accept-btn svg {
  width: 20px;
  height: 20px;
}

.action-hint {
  font-size: 12px;
  color: var(--text-muted);
  text-align: center;
  margin: 12px 0 0;
}

@media (max-width: 1024px) {
  .detail-sidebar {
    position: static;
  }
  
  .time-grid {
    grid-template-columns: 1fr;
  }
}
</style>
