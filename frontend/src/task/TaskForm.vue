<template>
  <el-form
    ref="formRef"
    :model="form"
    :rules="rules"
    label-position="top"
    class="task-form"
  >
    <div class="form-section">
      <h3 class="section-title">基本信息</h3>
      
      <el-form-item label="需求标题" prop="title">
        <el-input 
          v-model="form.title" 
          placeholder="例如：帮忙取快递、代买奶茶..." 
          maxlength="50" 
          show-word-limit 
          size="large"
        />
      </el-form-item>

      <div class="form-row">
        <el-form-item label="需求分类" prop="categoryId" class="form-col">
          <el-select v-model="form.categoryId" placeholder="选择分类" size="large" style="width: 100%">
            <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id">
              <div class="category-option">
                <span class="category-icon">{{ getCategoryIcon(cat.name) }}</span>
                <span>{{ cat.name }}</span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="任务报酬" prop="reward" class="form-col">
          <el-input-number 
            v-model="form.reward" 
            :min="0" 
            :precision="2" 
            :step="5"
            placeholder="可选，填0表示免费" 
            size="large"
            style="width: 100%"
          >
            <template #prefix>&#xFFE5;</template>
          </el-input-number>
        </el-form-item>
      </div>
    </div>

    <div class="form-section">
      <h3 class="section-title">详细描述</h3>
      
      <el-form-item label="需求详情" prop="description">
        <el-input 
          v-model="form.description" 
          type="textarea" 
          :rows="5" 
          placeholder="详细描述你的需求，包括具体要求、注意事项等..." 
          maxlength="500" 
          show-word-limit 
        />
      </el-form-item>
    </div>

    <div class="form-section">
      <h3 class="section-title">其他信息</h3>
      
      <div class="form-row">
        <el-form-item label="地点" prop="location" class="form-col">
          <el-input 
            v-model="form.location" 
            placeholder="例如：3号宿舍楼下" 
            maxlength="100"
            size="large"
          >
            <template #prefix>
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="width:16px;height:16px">
                <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0118 0z"/>
                <circle cx="12" cy="10" r="3"/>
              </svg>
            </template>
          </el-input>
        </el-form-item>
        
        <el-form-item label="截止时间" prop="deadline" class="form-col">
          <el-date-picker
            v-model="form.deadline"
            type="datetime"
            placeholder="选择截止时间"
            :disabled-date="beforeToday"
            size="large"
            style="width: 100%"
            format="YYYY年MM月DD日 HH:mm"
            value-format="YYYY-MM-DDTHH:mm:ss"
          />
        </el-form-item>
      </div>
    </div>

    <!-- 提交按钮 -->
    <div class="form-actions">
      <el-button size="large" @click="$router.back()">取消</el-button>
      <el-button type="primary" size="large" :loading="submitting" @click="submitForm">
        <svg v-if="!submitting" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="width:18px;height:18px;margin-right:6px">
          <path d="M22 2L11 13M22 2l-7 20-4-9-9-4 20-7z"/>
        </svg>
        {{ submitting ? '发布中...' : '发布需求' }}
      </el-button>
    </div>
  </el-form>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import taskApi from '@/task/taskApi'
import http from '@/common/http'

const router = useRouter()
const formRef = ref(null)
const submitting = ref(false)
const categories = reactive([])

const form = reactive({
  title: '',
  description: '',
  categoryId: null,
  reward: null,
  location: '',
  deadline: null
})

const rules = {
  title: [
    { required: true, message: '请输入需求标题', trigger: 'blur' },
    { min: 5, max: 50, message: '标题长度需在5-50字符之间', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入需求描述', trigger: 'blur' },
    { min: 10, max: 500, message: '描述长度需在10-500字符之间', trigger: 'blur' }
  ],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  deadline: [{ required: true, message: '请选择截止时间', trigger: 'change' }]
}

const categoryIcons = {
  '快递代取': '&#x1F4E6;',
  '学习辅导': '&#x1F4DA;',
  '二手交易': '&#x1F4B0;',
  '组队匹配': '&#x1F465;'
}

function getCategoryIcon(name) {
  return categoryIcons[name] || '&#x1F4CC;'
}

function beforeToday(date) { 
  return date.getTime() < Date.now() - 86400000 
}

onMounted(async () => {
  try {
    const res = await http.get('/categories')
    categories.push(...res.data.data)
  } catch {
    categories.push(
      { id: 1, name: '快递代取' },
      { id: 2, name: '学习辅导' },
      { id: 3, name: '二手交易' },
      { id: 4, name: '组队匹配' }
    )
  }
})

async function submitForm() {
  try {
    await formRef.value.validate()
  } catch {
    ElMessage.warning('请完善表单信息后再提交')
    return
  }

  submitting.value = true
  try {
    const res = await taskApi.create({
      title: form.title,
      description: form.description,
      categoryId: form.categoryId,
      reward: form.reward || null,
      location: form.location || null,
      deadline: form.deadline ? new Date(form.deadline).toISOString() : null
    })
    ElMessage.success('发布成功！')
    if (res.data?.data?.id) {
      router.push(`/tasks/${res.data.data.id}`)
    } else {
      router.push('/')
    }
  } catch {
    // 错误提示由 http.js 拦截器统一处理
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.task-form {
  max-width: 100%;
}

.form-section {
  margin-bottom: 32px;
}

.form-section:last-of-type {
  margin-bottom: 0;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--border-color);
  display: flex;
  align-items: center;
  gap: 8px;
}

.form-row {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

@media (max-width: 600px) {
  .form-row {
    grid-template-columns: 1fr;
  }
}

.form-col {
  margin-bottom: 0;
}

.category-option {
  display: flex;
  align-items: center;
  gap: 8px;
}

.category-icon {
  font-size: 16px;
}

/* 表单操作按钮 */
.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid var(--border-color);
}

.form-actions .el-button {
  min-width: 120px;
}

/* Element Plus 表单样式覆盖 */
:deep(.el-form-item__label) {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
  margin-bottom: 8px;
}

:deep(.el-input__wrapper),
:deep(.el-textarea__inner) {
  border-radius: var(--radius-md) !important;
  transition: all var(--transition-base);
}

:deep(.el-input__wrapper:hover),
:deep(.el-textarea__inner:hover) {
  box-shadow: 0 0 0 1px var(--primary) !important;
}

:deep(.el-input__wrapper.is-focus),
:deep(.el-textarea__inner:focus) {
  box-shadow: 0 0 0 2px rgba(16, 185, 129, 0.2) !important;
}

:deep(.el-textarea__inner) {
  padding: 12px 14px;
  line-height: 1.6;
}

:deep(.el-select__wrapper) {
  border-radius: var(--radius-md) !important;
}
</style>
