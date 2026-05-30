<template>
  <div class="task-list-page">
    <!-- 筛选栏 -->
    <TaskFilter @filter-change="handleFilterChange" />

    <!-- 加载态 -->
    <div v-if="loading" v-loading="loading" style="min-height:300px" />

    <!-- 空状态 -->
    <el-empty v-else-if="tasks.length === 0" description="暂无需求" />

    <!-- 需求卡片网格 -->
    <div v-else class="task-grid">
      <TaskCard v-for="task in tasks" :key="task.id" :task="task" />
    </div>

    <!-- 分页器 -->
    <Pagination
      :page="pagination.page"
      :page-size="pagination.pageSize"
      :total="pagination.total"
      @page-change="page => { pagination.page = page; fetchTasks() }"
      @size-change="size => { pagination.pageSize = size; pagination.page = 1; fetchTasks() }"
    />
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
/* 卡片网格：PC 端 4 列，平板 3 列，手机 2 列 */
.task-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}
@media (max-width: 1200px) { .task-grid { grid-template-columns: repeat(3, 1fr); } }
@media (max-width: 900px)  { .task-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 600px)  { .task-grid { grid-template-columns: 1fr; } }
</style>
