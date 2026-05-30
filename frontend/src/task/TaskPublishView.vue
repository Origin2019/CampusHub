<template>
  <div class="task-publish-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <button class="back-btn" @click="$router.push('/')">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M19 12H5M12 19l-7-7 7-7"/>
        </svg>
        返回
      </button>
      <div class="header-content">
        <h1 class="page-title">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10"/>
            <line x1="12" y1="8" x2="12" y2="16"/>
            <line x1="8" y1="12" x2="16" y2="12"/>
          </svg>
          发布新需求
        </h1>
        <p class="page-subtitle">填写需求详情，让更多同学帮助你</p>
      </div>
    </div>

    <!-- 表单卡片 -->
    <div class="form-card">
      <TaskForm />
    </div>

    <!-- 提示信息 -->
    <div class="tips-card">
      <h4>
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="12" cy="12" r="10"/>
          <path d="M12 16v-4M12 8h.01"/>
        </svg>
        发布提示
      </h4>
      <ul>
        <li>标题要简洁明了，让别人一眼就能理解你的需求</li>
        <li>详细描述需求内容，包括具体要求和注意事项</li>
        <li>合理设置报酬，免费互助也欢迎</li>
        <li>设置合理的截止时间，给接单者足够的时间完成</li>
      </ul>
    </div>
  </div>
</template>

<script setup>
import TaskForm from '@/task/TaskForm.vue'
</script>

<style scoped>
.task-publish-page {
  max-width: 800px;
  margin: 0 auto;
  animation: fade-in 0.5s ease;
}

@keyframes fade-in {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

/* 页面头部 */
.page-header {
  margin-bottom: 24px;
}

.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-full);
  background: var(--bg-secondary);
  color: var(--text-secondary);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all var(--transition-base);
  margin-bottom: 20px;
}

.back-btn:hover {
  border-color: var(--primary);
  color: var(--primary);
}

.back-btn svg {
  width: 16px;
  height: 16px;
}

.header-content {
  text-align: center;
  padding: 20px 0;
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

/* 表单卡片 */
.form-card {
  background: var(--bg-secondary);
  border-radius: var(--radius-xl);
  padding: 32px;
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow-sm);
}

/* 提示卡片 */
.tips-card {
  background: rgba(16, 185, 129, 0.05);
  border: 1px solid rgba(16, 185, 129, 0.15);
  border-radius: var(--radius-xl);
  padding: 20px 24px;
  margin-top: 20px;
}

.tips-card h4 {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: var(--primary-dark);
  margin: 0 0 12px;
}

.tips-card h4 svg {
  width: 18px;
  height: 18px;
}

.tips-card ul {
  margin: 0;
  padding-left: 20px;
}

.tips-card li {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.8;
}
</style>
