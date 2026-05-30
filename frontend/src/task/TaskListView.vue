<template>
  <div class="task-list-page">
    <!-- 页面标题区域 -->
    <div class="page-hero">
      <h1 class="hero-title">
        <span class="title-emoji">&#x1F4E3;</span>
        需求广场
      </h1>
      <p class="hero-subtitle">发现校园里的互助机会，找到你需要的帮助</p>
    </div>

    <!-- 筛选栏 -->
    <TaskFilter @filter-change="handleFilterChange" />

    <!-- 加载态 -->
    <div v-if="loading" class="loading-state">
      <div class="loading-spinner"></div>
      <p>正在加载需求...</p>
    </div>

    <!-- 空状态 -->
    <div v-else-if="tasks.length === 0" class="empty-state">
      <div class="empty-icon">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <path d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"/>
          <path d="M12 11v6M9 14h6"/>
        </svg>
      </div>
      <h3>暂无需求</h3>
      <p>当前没有符合条件的需求，试试调整筛选条件</p>
    </div>

    <!-- 需求卡片网格 -->
    <TransitionGroup v-else name="card-list" tag="div" class="task-grid">
      <TaskCard v-for="(task, index) in tasks" :key="task.id" :task="task" :style="{ '--delay': index * 0.05 + 's' }" />
    </TransitionGroup>

    <!-- 分页器 -->
    <div class="pagination-wrapper" v-if="pagination.total > pagination.pageSize">
      <Pagination
        :page="pagination.page"
        :page-size="pagination.pageSize"
        :total="pagination.total"
        @page-change="page => { pagination.page = page; fetchTasks() }"
        @size-change="size => { pagination.pageSize = size; pagination.page = 1; fetchTasks() }"
      />
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import taskApi from '@/task/taskApi'
import TaskFilter from '@/task/TaskFilter.vue'
import TaskCard from '@/task/TaskCard.vue'
import Pagination from '@/common/ui/Pagination.vue'

const loading = ref(false)
const tasks = reactive([])
const filters = reactive({})
const pagination = reactive({ page: 1, pageSize: 20, total: 0 })

async function fetchTasks() {
  loading.value = true
  try {
    const params = {
      page: pagination.page, page_size: pagination.pageSize,
      sort: filters.sort || 'latest',
      ...(filters.categoryId && { categoryId: filters.categoryId }),
      ...(filters.status && { status: filters.status }),
      ...(filters.keyword && { keyword: filters.keyword })
    }
    const res = await taskApi.list(params)
    tasks.splice(0, tasks.length, ...(res.data?.data?.items || []))
    pagination.total = res.data?.data?.pagination?.total || 0
  } catch {
    tasks.splice(0, tasks.length)
  } finally {
    loading.value = false
  }
}

function handleFilterChange(f) {
  Object.assign(filters, f)
  pagination.page = 1
  fetchTasks()
}

onMounted(fetchTasks)
</script>

<style scoped>
.task-list-page {
  animation: fade-in 0.5s ease;
}

@keyframes fade-in {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

/* 页面标题区域 */
.page-hero {
  text-align: center;
  padding: 32px 0 40px;
}

.hero-title {
  font-size: 32px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
}

.title-emoji {
  font-size: 36px;
}

.hero-subtitle {
  font-size: 16px;
  color: var(--text-secondary);
  margin: 0;
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
  margin: 0;
}

/* 卡片网格 */
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

/* 分页器包装 */
.pagination-wrapper {
  margin-top: 40px;
  display: flex;
  justify-content: center;
}
</style>
