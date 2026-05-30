import http from '@/common/http'

/**
 * 需求模块 API 封装.
 * 对应后端 TaskController 的三个接口。
 */
export default {
  /** GET /v1/tasks — 分页查询需求列表 */
  list(params) {
    return http.get('/tasks', { params })
  },

  /** GET /v1/tasks/{id} — 查询需求详情 */
  getById(id) {
    return http.get(`/tasks/${id}`)
  },

  /** POST /v1/tasks — 发布新需求 */
  create(data) {
    return http.post('/tasks', data)
  }
}
