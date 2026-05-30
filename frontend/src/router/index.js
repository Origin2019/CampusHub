import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/',              name: 'TaskList',    component: () => import('@/task/TaskListView.vue') },
  { path: '/tasks/:id',     name: 'TaskDetail',  component: () => import('@/task/TaskDetailView.vue') },
  { path: '/tasks/publish', name: 'TaskPublish',  component: () => import('@/task/TaskPublishView.vue'),  meta: { requiresAuth: true } },
  { path: '/my-tasks',      name: 'MyTasks',     component: () => import('@/task/MyTasksView.vue'),     meta: { requiresAuth: true } },
  { path: '/admin',         name: 'AdminDashboard', component: () => import('@/admin/AdminDashboard.vue'), meta: { requiresAuth: true } },
  { path: '/admin/users',   name: 'AdminUsers',     component: () => import('@/admin/UserManagement.vue'), meta: { requiresAuth: true } },
  { path: '/admin/tasks',   name: 'AdminTasks',     component: () => import('@/admin/TaskManagement.vue'), meta: { requiresAuth: true } }
]

const router = createRouter({ history: createWebHistory(), routes })

router.beforeEach((to, from, next) => {
  import('@/user/userStore').then(({ useUserStore }) => {
    const userStore = useUserStore()
    if (to.meta.requiresAuth && !userStore.isLoggedIn) { next(false); return }
    next()
  })
})

export default router
