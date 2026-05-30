import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  const userId = ref(null)
  const nickname = ref('')
  const role = ref('')
  const avatar = ref(null)
  const creditScore = ref(5.0)
  const token = ref(null)

  const isRequester = computed(() => role.value === 'requester')
  const isProvider = computed(() => role.value === 'provider')
  const isAdmin = computed(() => role.value === 'admin')
  const isLoggedIn = computed(() => userId.value !== null)

  function login(user, jwtToken) {
    userId.value = user.id
    nickname.value = user.nickname || user.name
    role.value = user.role
    avatar.value = user.avatar
    creditScore.value = user.creditScore ?? 5.0
    token.value = jwtToken
    localStorage.setItem('campusHubToken', jwtToken)
  }

  function logout() {
    userId.value = null
    nickname.value = ''
    role.value = ''
    avatar.value = null
    creditScore.value = 5.0
    token.value = null
    localStorage.removeItem('campusHubToken')
  }

  function restoreToken() {
    const saved = localStorage.getItem('campusHubToken')
    if (saved) token.value = saved
  }

  /** 普通用户在需求方/服务方之间切换身份 */
  async function switchRole() {
    if (role.value === 'admin') return
    const newRole = role.value === 'requester' ? 'provider' : 'requester'
    const newId = newRole === 'requester' ? 1 : 2
    const { default: http } = await import('@/common/http')
    try {
      const res = await http.post('/auth/login/password', {
        account: newRole === 'requester' ? '20240001' : '20240002',
        password: 'Test123456'
      })
      const d = res.data.data
      login({ id: d.userId, nickname: d.nickname, role: newRole, avatar: null, creditScore: 5.0 }, d.token)
    } catch {
      // 回退
      role.value = newRole
      userId.value = newId
      nickname.value = newRole === 'requester' ? '张三(需求方)' : '李四(服务方)'
    }
  }

  return { userId, nickname, role, avatar, creditScore, token,
           isRequester, isProvider, isAdmin, isLoggedIn,
           login, logout, restoreToken, switchRole }
})
