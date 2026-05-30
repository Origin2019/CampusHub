<template>
  <el-card shadow="never" class="filter-bar">
    <el-row :gutter="16" align="middle">
      <!-- 分类筛选 -->
      <el-col :span="6">
        <el-select v-model="filters.categoryId" placeholder="全部分类" clearable @change="emitFilter">
          <el-option
            v-for="cat in categories"
            :key="cat.id"
            :label="cat.name"
            :value="cat.id"
          />
        </el-select>
      </el-col>
      <!-- 状态筛选 -->
      <el-col :span="5">
        <el-select v-model="filters.status" placeholder="全部状态" clearable @change="emitFilter">
          <el-option label="已发布" value="published" />
          <el-option label="进行中" value="in_progress" />
          <el-option label="已完成" value="completed" />
        </el-select>
      </el-col>
      <!-- 关键词搜索 -->
      <el-col :span="7">
        <el-input
          v-model="filters.keyword"
          placeholder="搜索需求标题..."
          clearable
          @keyup.enter="emitFilter"
          @clear="emitFilter"
        />
      </el-col>
      <!-- 排序 -->
      <el-col :span="4">
        <el-select v-model="filters.sort" @change="emitFilter">
          <el-option label="最新发布" value="latest" />
          <el-option label="报酬最高" value="reward" />
        </el-select>
      </el-col>
    </el-row>
  </el-card>
</template>

<script setup>
import { reactive, onMounted } from 'vue'
import http from '@/common/http'

const emit = defineEmits(['filter-change'])

/** 筛选条件 */
const filters = reactive({
  categoryId: null,
  status: '',
  keyword: '',
  sort: 'latest'
})

/** 分类列表（从后端 categories 表获取） */
const categories = reactive([])

/** 加载分类下拉数据 */
onMounted(async () => {
  try {
    const res = await http.get('/categories')
    categories.push(...res.data.data)
  } catch {
    // 分类接口未开发时的硬编码回退
    categories.push(
      { id: 1, name: '快递代取' },
      { id: 2, name: '学习辅导' },
      { id: 3, name: '二手交易' },
      { id: 4, name: '组队匹配' }
    )
  }
})

/** 筛选项变化时通知父组件重新加载列表 */
function emitFilter() {
  emit('filter-change', { ...filters })
}
</script>

<style scoped>
.filter-bar { margin-bottom: 16px; }
</style>
