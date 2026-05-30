<template>
  <div class="task-tile" @click="$router.push(`/tasks/${task.id}`)">
    <!-- 顶部：状态标签 + 报酬 -->
    <div class="tile-top">
      <el-tag size="small" :type="statusType">{{ statusLabel }}</el-tag>
      <span class="reward" v-if="task.reward">¥{{ task.reward }}</span>
      <span class="reward free" v-else>免费</span>
    </div>
    <!-- 标题 -->
    <h4 class="tile-title">{{ task.title }}</h4>
    <!-- 描述 -->
    <p class="tile-desc">{{ truncate(task.description, 60) }}</p>
    <!-- 底部：分类 + 发布者 + 时间 -->
    <div class="tile-footer">
      <span class="category">{{ task.categoryName }}</span>
      <span class="publisher">{{ task.publisher?.nickname || '匿名' }}</span>
      <span class="time">{{ formatDate(task.publishedAt) }}</span>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({ task: { type: Object, required: true } })

const statusMap = {
  published: ['已发布', 'primary'],
  in_progress: ['进行中', 'warning'],
  completed: ['已完成', 'success'],
  cancelled: ['已取消', 'info']
}
const statusLabel = statusMap[props.task.status]?.[0] || props.task.status
const statusType = statusMap[props.task.status]?.[1] || 'info'

function truncate(text, max) { return text?.length > max ? text.slice(0, max) + '…' : text || '' }
function formatDate(d) { return d ? new Date(d).toLocaleDateString('zh-CN') : '' }
</script>

<style scoped>
.task-tile {
  background: #fff; border-radius: 8px; padding: 16px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.08); cursor: pointer;
  display: flex; flex-direction: column; gap: 8px;
  transition: box-shadow 0.2s;
}
.task-tile:hover { box-shadow: 0 2px 12px rgba(0,0,0,0.14); }

.tile-top { display: flex; justify-content: space-between; align-items: center; }
.reward { color: #e6a23c; font-weight: bold; font-size: 15px; }
.reward.free { color: #909399; font-size: 13px; }

.tile-title { margin: 0; font-size: 15px; line-height: 1.4; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.tile-desc { margin: 0; font-size: 12px; color: #909399; line-height: 1.5; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; min-height: 36px; }
.tile-footer { display: flex; justify-content: space-between; font-size: 11px; color: #c0c4cc; }
</style>
