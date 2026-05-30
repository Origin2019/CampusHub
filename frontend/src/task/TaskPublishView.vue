<template>
  <div class="task-publish-page">
    <el-page-header @back="$router.push('/')" content="发布需求" />
    <el-card shadow="never" style="margin-top:16px">
      <TaskForm />
    </el-card>
  </div>
</template>

<script setup>
import TaskForm from '@/task/TaskForm.vue'
</script>
