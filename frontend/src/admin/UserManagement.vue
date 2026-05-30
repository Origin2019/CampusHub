<template>
  <div class="user-management" v-loading="loading">
    <h2>用户管理</h2>

    <el-row :gutter="12" style="margin-bottom:16px">
      <el-col :span="8">
        <el-input v-model="filters.keyword" placeholder="搜索姓名/邮箱" clearable @keyup.enter="search" />
      </el-col>
      <el-col :span="4">
        <el-select v-model="filters.role" placeholder="角色" clearable @change="search">
          <el-option label="需求方" value="requester" />
          <el-option label="服务方" value="provider" />
          <el-option label="管理员" value="admin" />
        </el-select>
      </el-col>
      <el-col :span="4">
        <el-button type="primary" @click="search">搜索</el-button>
      </el-col>
    </el-row>

    <el-table :data="users" border stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="name" label="姓名" width="100" />
      <el-table-column prop="email" label="邮箱" min-width="180" />
      <el-table-column prop="phoneMasked" label="手机号" width="130" />
      <el-table-column prop="role" label="角色" width="80">
        <template #default="{ row }">
          <el-tag :type="row.role === 'admin' ? 'danger' : row.role === 'requester' ? 'primary' : 'warning'" size="small">
            {{ roleLabel(row.role) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="creditScore" label="信用分" width="80" />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
            {{ row.status === 1 ? '正常' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button
            :type="row.status === 1 ? 'warning' : 'success'"
            size="small"
            @click="toggleStatus(row)"
          >
            {{ row.status === 1 ? '禁用' : '启用' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div style="text-align:center;margin-top:16px">
      <el-pagination
        v-model:current-page="pagination.page"
        :page-size="pagination.pageSize"
        :total="pagination.total"
        layout="total, prev, pager, next"
        @current-change="fetchUsers"
      />
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import adminApi from '@/admin/adminApi'

const loading = ref(false)
const users = reactive([])
const filters = reactive({ keyword: '', role: '' })
const pagination = reactive({ page: 1, pageSize: 20, total: 0 })

function roleLabel(r) {
  if (r === 'requester') return '需求方'
  if (r === 'provider') return '服务方'
  if (r === 'admin') return '管理员'
  return r
}

async function fetchUsers() {
  loading.value = true
  try {
    const res = await adminApi.listUsers({
      page: pagination.page, page_size: pagination.pageSize,
      keyword: filters.keyword || undefined,
      role: filters.role || undefined
    })
    const data = res.data.data
    users.splice(0, users.length, ...(data.items || []))
    pagination.total = data.pagination?.total || 0
  } catch {
    users.splice(0, users.length)
  } finally {
    loading.value = false
  }
}

function search() { pagination.page = 1; fetchUsers() }

async function toggleStatus(row) {
  try {
    await ElMessageBox.confirm(`确定要${row.status === 1 ? '禁用' : '启用'}用户「${row.name}」吗？`, '确认操作')
    await adminApi.toggleUserStatus(row.id)
    ElMessage.success('操作成功')
    row.status = row.status === 1 ? 0 : 1
  } catch { /* 取消 */ }
}

onMounted(fetchUsers)
</script>
