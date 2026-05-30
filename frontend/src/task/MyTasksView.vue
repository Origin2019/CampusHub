<template>
  <div class="my-tasks-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M16 4h2a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h2"/>
            <rect x="8" y="2" width="8" height="4" rx="1" ry="1"/>
            <path d="M9 14l2 2 4-4"/>
          </svg>
          我的需求
        </h1>
        <p class="page-subtitle">管理你发布和接收的所有需求</p>
      </div>
      
      <!-- 标签页 -->
      <div class="custom-tabs">
        <button 
          v-if="userStore.isRequester"
          class="tab-btn" 
          :class="{ active: tab === 'myPublished' }"
          @click="tab = 'myPublished'; fetchData()"
        >
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M12 19l7-7 3 3-7 7-3-3z"/>
            <path d="M18 13l-1.5-7.5L2 2l3.5 14.5L13 18l5-5z"/>
            <path d="M2 2l7.586 7.586"/>
            <circle cx="11" cy="11" r="2"/>
          </svg>
          我发布的
          <span class="tab-count" v-if="published.length">{{ published.length }}</span>
        </button>
        <button 
          v-if="userStore.isProvider"
          class="tab-btn" 
          :class="{ active: tab === 'myAccepted' }"
          @click="tab = 'myAccepted'; fetchData()"
        >
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z"/>
            <polyline points="14,2 14,8 20,8"/>
            <line x1="16" y1="13" x2="8" y2="13"/>
            <line x1="16" y1="17" x2="8" y2="17"/>
          </svg>
          我接单的
          <span class="tab-count" v-if="myAccepted.length">{{ myAccepted.length }}</span>
        </button>
        <button 
          class="tab-btn" 
          :class="{ active: tab === 'accepted' }"
          @click="tab = 'accepted'; fetchData()"
        >
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10"/>
            <polyline points="12 6 12 12 16 14"/>
          </svg>
          已接单的
          <span class="tab-count" v-if="acceptedCount">{{ acceptedCount }}</span>
        </button>
        <button 
          class="tab-btn" 
          :class="{ active: tab === 'completed' }"
          @click="tab = 'completed'; fetchData()"
        >
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M22 11.08V12a10 10 0 11-5.93-9.14"/>
            <polyline points="22 4 12 14.01 9 11.01"/>
          </svg>
          已完成的
          <span class="tab-count" v-if="completed.length">{{ completed.length }}</span>
        </button>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-state">
      <div class="loading-spinner"></div>
      <p>正在加载...</p>
    </div>

    <!-- 空状态 -->
    <div v-else-if="filtered.length === 0" class="empty-state">
      <div class="empty-icon">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <path d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"/>
        </svg>
      </div>
      <h3>{{ emptyDesc }}</h3>
      <p>{{ emptyHint }}</p>
      <router-link v-if="tab === 'myPublished'" to="/tasks/publish" class="empty-action">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="12" cy="12" r="10"/>
          <line x1="12" y1="8" x2="12" y2="16"/>
          <line x1="8" y1="12" x2="16" y2="12"/>
        </svg>
        发布第一个需求
      </router-link>
    </div>

    <!-- 任务列表 -->
    <TransitionGroup v-else name="card-list" tag="div" class="task-grid">
      <TaskCard v-for="(t, index) in filtered" :key="t.id" :task="t" :style="{ '--delay': index * 0.05 + 's' }" />
    </TransitionGroup>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import taskApi from '@/task/taskApi'
import TaskCard from '@/task/TaskCard.vue'
import { useUserStore } from '@/user/userStore'

const userStore = useUserStore()
const tab = ref(userStore.isRequester ? 'myPublished' : 'myAccepted')
const loading = ref(false)
const allTasks = reactive([])

const published = computed(() => allTasks.filter(t =>
  t.publisher?.userId === userStore.userId &&
  (t.status === 'published' || t.status === 'in_progress')
))

const accepted = computed(() => allTasks.filter(t =>
  t.publisher?.userId === userStore.userId && t.status === 'in_progress'
))

const myAccepted = computed(() => allTasks.filter(t =>
  t.publisher?.userId !== userStore.userId &&
  (t.status === 'in_progress')
))

const completed = computed(() => allTasks.filter(t => {
  if (userStore.isRequester) return t.publisher?.userId === userStore.userId && t.status === 'completed'
  return t.publisher?.userId !== userStore.userId && t.status === 'completed'
}))

const acceptedCount = computed(() => 
  userStore.isRequester ? accepted.value.length : myAccepted.value.length
)

const filtered = computed(() => {
  if (tab.value === 'myPublished') return published.value
  if (tab.value === 'myAccepted') return myAccepted.value
  if (tab.value === 'accepted') return userStore.isRequester ? accepted.value : myAccepted.value
  return completed.value
})

const emptyDesc = computed(() => {
  const map = { 
    myPublished: '还没有发布过需求', 
    myAccepted: '还没有接过单',
    accepted: '暂无已接单的需求', 
    completed: '暂无已完成的需求' 
  }
  return map[tab.value] || '暂无数据'
})

const emptyHint = computed(() => {
  const map = { 
    myPublished: '发布一个需求，让同学们帮助你', 
    myAccepted: '去需求广场看看有什么可以帮忙的',
    accepted: '等待同学接单中...', 
    completed: '完成的需求会在这里显示' 
  }
  return map[tab.value] || ''
})

async function fetchData() {
  loading.value = true
  try {
    const res = await taskApi.list({ page: 1, page_size: 200, sort: 'latest' })
    allTasks.splice(0, allTasks.length, ...(res.data?.data?.items || []))
  } catch {
    allTasks.splice(0, allTasks.length)
  } finally {
    loading.value = false
  }
}

onMounted(fetchData)
</script>

<style scoped>
.my-tasks-page {
  animation: fade-in 0.5s ease;
}

@keyframes fade-in {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

/* 页面头部 */
.page-header {
  margin-bottom: 32px;
}

.header-content {
  text-align: center;
  padding: 24px 0 32px;
}

.page-title {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 8px;
}

.page-title svg {
  width: 32px;
  height: 32px;
  color: var(--primary);
}

.page-subtitle {
  font-size: 15px;
  color: var(--text-secondary);
  margin: 0;
}

/* 自定义标签页 */
.custom-tabs {
  display: flex;
  justify-content: center;
  gap: 8px;
  flex-wrap: wrap;
}

.tab-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-full);
  background: var(--bg-secondary);
  color: var(--text-secondary);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all var(--transition-base);
}

.tab-btn:hover {
  border-color: var(--primary);
  color: var(--primary);
}

.tab-btn.active {
  background: linear-gradient(135deg, var(--primary), var(--primary-light));
  border-color: transparent;
  color: white;
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.25);
}

.tab-btn svg {
  width: 18px;
  height: 18px;
}

.tab-count {
  background: rgba(255, 255, 255, 0.2);
  padding: 2px 8px;
  border-radius: var(--radius-full);
  font-size: 12px;
  font-weight: 600;
}

.tab-btn:not(.active) .tab-count {
  background: var(--bg-tertiary);
  color: var(--text-muted);
}

/* 加载状态 */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 300px;
  gap: 16px;
}

.loading-spinner {
  width: 48px;
  height: 48px;
  border: 3px solid var(--border-color);
  border-top-color: var(--primary);
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.loading-state p {
  color: var(--text-muted);
  font-size: 14px;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 300px;
  padding: 48px 24px;
  text-align: center;
}

.empty-icon {
  width: 80px;
  height: 80px;
  background: var(--bg-tertiary);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
}

.empty-icon svg {
  width: 40px;
  height: 40px;
  color: var(--text-muted);
}

.empty-state h3 {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 8px;
}

.empty-state p {
  font-size: 14px;
  color: var(--text-muted);
  margin: 0 0 24px;
}

.empty-action {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  background: linear-gradient(135deg, var(--primary), var(--primary-light));
  color: white;
  font-size: 14px;
  font-weight: 600;
  text-decoration: none;
  border-radius: var(--radius-full);
  transition: all var(--transition-base);
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.25);
}

.empty-action:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(16, 185, 129, 0.35);
}

.empty-action svg {
  width: 18px;
  height: 18px;
}

/* 任务网格 */
.task-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

@media (max-width: 1200px) { .task-grid { grid-template-columns: repeat(3, 1fr); } }
@media (max-width: 900px)  { .task-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 600px)  { .task-grid { grid-template-columns: 1fr; } }

/* 卡片列表动画 */
.card-list-enter-active {
  transition: all 0.4s ease;
  transition-delay: var(--delay, 0s);
}

.card-list-leave-active {
  transition: all 0.3s ease;
}

.card-list-enter-from {
  opacity: 0;
  transform: translateY(20px) scale(0.95);
}

.card-list-leave-to {
  opacity: 0;
  transform: scale(0.95);
}

.card-list-move {
  transition: transform 0.3s ease;
}
</style>
