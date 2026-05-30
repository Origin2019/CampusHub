<template>
  <div class="my-tasks-page">
    <div class="page-header">
      <h2>我的需求</h2>
      <el-tabs v-model="tab" @tab-change="fetchData">
        <!-- 需求方：我发布的 -->
        <el-tab-pane v-if="userStore.isRequester" label="我发布的" name="myPublished" />
        <!-- 服务方：我接单的 -->
        <el-tab-pane v-if="userStore.isProvider" label="我接单的" name="myAccepted" />
        <!-- 已接单的（需求方：别人接了我的 / 服务方：我接了别人的） -->
        <el-tab-pane label="已接单的" name="accepted" />
        <!-- 已完成的 -->
        <el-tab-pane label="已完成的" name="completed" />
      </el-tabs>
    </div>

    <div v-if="loading" v-loading="loading" style="min-height:200px" />
    <el-empty v-else-if="filtered.length === 0" :description="emptyDesc" />

    <div v-else class="task-grid">
      <TaskCard v-for="t in filtered" :key="t.id" :task="t" />
    </div>
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

// 4 个维度的过滤
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

const filtered = computed(() => {
  if (tab.value === 'myPublished') return published.value
  if (tab.value === 'myAccepted') return myAccepted.value
  if (tab.value === 'accepted') return userStore.isRequester ? accepted.value : myAccepted.value
  return completed.value
})

const emptyDesc = computed(() => {
  const map = { myPublished: '你还没有发布过需求', myAccepted: '你还没有接过单',
                accepted: '暂无已接单的需求', completed: '暂无已完成的需求' }
  return map[tab.value] || '暂无数据'
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
.page-header { margin-bottom: 16px; }
.page-header h2 { margin: 0 0 8px; }
.task-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}
@media (max-width: 1200px) { .task-grid { grid-template-columns: repeat(3, 1fr); } }
@media (max-width: 900px)  { .task-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 600px)  { .task-grid { grid-template-columns: 1fr; } }
</style>
