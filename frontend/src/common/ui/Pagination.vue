<template>
  <div class="pagination-wrapper" v-if="total > 0">
    <el-pagination
      v-model:current-page="current"
      :page-size="pageSize"
      :total="total"
      :page-sizes="[10, 20, 50]"
      layout="total, sizes, prev, pager, next"
      background
      @current-change="$emit('page-change', $event)"
      @size-change="$emit('size-change', $event)"
    />
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  page: { type: Number, default: 1 },
  pageSize: { type: Number, default: 20 },
  total: { type: Number, default: 0 }
})

defineEmits(['page-change', 'size-change'])

const current = computed({
  get: () => props.page,
  set: val => {}
})
</script>

<style scoped>
.pagination-wrapper { display: flex; justify-content: center; margin-top: 24px; }
</style>
