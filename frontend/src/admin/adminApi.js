import http from '@/common/http'

export default {
  /** 仪表盘统计 */
  getStats() { return http.get('/admin/stats') },

  /** 用户列表 */
  listUsers(params) { return http.get('/admin/users', { params }) },

  /** 启用/禁用用户 */
  toggleUserStatus(id) { return http.put(`/admin/users/${id}/status`) },

  /** 全部需求列表 */
  listTasks(params) { return http.get('/admin/tasks', { params }) },

  /** 强制删除需求 */
  deleteTask(id) { return http.delete(`/admin/tasks/${id}`) }
}
