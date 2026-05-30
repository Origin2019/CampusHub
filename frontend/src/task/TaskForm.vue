<template>
  <el-form
    ref="formRef"
    :model="form"
    :rules="rules"
    label-width="80px"
    style="max-width: 640px"
  >
    <el-form-item label="标题" prop="title">
      <el-input v-model="form.title" placeholder="5-50字，简述需求" maxlength="50" show-word-limit />
    </el-form-item>
    <el-form-item label="分类" prop="categoryId">
      <el-select v-model="form.categoryId" placeholder="选择分类">
        <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
      </el-select>
    </el-form-item>
    <el-form-item label="描述" prop="description">
      <el-input v-model="form.description" type="textarea" :rows="4" placeholder="10-500字，详细描述你的需求" maxlength="500" show-word-limit />
    </el-form-item>
    <el-form-item label="报酬(¥)" prop="reward">
      <el-input-number v-model="form.reward" :min="0" :precision="2" placeholder="可选，填0表示免费" style="width:100%" />
    </el-form-item>
    <el-form-item label="地点" prop="location">
      <el-input v-model="form.location" placeholder="如：3号宿舍楼下（可选）" maxlength="100" />
    </el-form-item>
    <el-form-item label="截止时间" prop="deadline">
      <el-date-picker
        v-model="form.deadline"
        type="datetime"
        placeholder="选择截止时间"
        :disabled-date="beforeToday"
        style="width:100%"
      />
    </el-form-item>
    <el-form-item>
      <el-button type="primary" :loading="submitting" @click="submitForm">发布需求</el-button>
      <el-button @click="$router.back()">取消</el-button>
    </el-form-item>
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

/** 表单数据 */
const form = reactive({
  title: '',
  description: '',
  categoryId: null,
  reward: null,
  location: '',
  deadline: null
})

/** Element Plus 校验规则 */
const rules = {
  title: [
    { required: true, message: '标题不能为空', trigger: 'blur' },
    { min: 5, max: 50, message: '标题长度需在5-50字符之间', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '描述不能为空', trigger: 'blur' },
    { min: 10, max: 500, message: '描述长度需在10-500字符之间', trigger: 'blur' }
  ],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  deadline: [{ required: true, message: '请选择截止时间', trigger: 'change' }]
}

/** 禁用今天之前的日期 */
function beforeToday(date) { return date.getTime() < Date.now() - 86400000 }

/** 加载分类下拉数据 */
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

/** 提交发布 */
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
    ElMessage.success('发布成功')
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
