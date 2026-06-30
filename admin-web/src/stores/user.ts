import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getToken, setToken, removeToken, getUserInfo, setUserInfo, removeUserInfo, generateMockToken, logout as authLogout, type UserInfo } from '@/utils/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(getToken() || '')
  const userInfo = ref<UserInfo | null>(getUserInfo())

  const isLoggedIn = computed(() => !!token.value && !!userInfo.value)
  const username = computed(() => userInfo.value?.realName || userInfo.value?.username || '')
  const avatar = computed(() => userInfo.value?.avatar || '')
  const roles = computed(() => userInfo.value?.roles || [])
  const permissions = computed(() => userInfo.value?.permissions || [])

  function login(username: string, _password: string): Promise<{ token: string; userInfo: UserInfo }> {
    return new Promise((resolve) => {
      setTimeout(() => {
        const mockUser: UserInfo = {
          userId: 'U000001',
          username: username,
          realName: username === 'admin' ? '超级管理员' : username,
          avatar: `https://api.dicebear.com/7.x/avataaars/svg?seed=${username}`,
          roles: ['admin'],
          permissions: ['*']
        }
        const mockToken = generateMockToken(mockUser.userId)

        token.value = mockToken
        userInfo.value = mockUser
        setToken(mockToken)
        setUserInfo(mockUser)

        resolve({ token: mockToken, userInfo: mockUser })
      }, 800)
    })
  }

  function loadFromStorage() {
    token.value = getToken() || ''
    userInfo.value = getUserInfo()
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    authLogout()
  }

  function resetToken() {
    token.value = ''
    userInfo.value = null
    removeToken()
    removeUserInfo()
  }

  function hasPermission(permission: string | string[]): boolean {
    if (permissions.value.includes('*')) return true
    if (Array.isArray(permission)) {
      return permission.some(p => permissions.value.includes(p))
    }
    return permissions.value.includes(permission)
  }

  function hasRole(role: string | string[]): boolean {
    if (roles.value.includes('admin')) return true
    if (Array.isArray(role)) {
      return role.some(r => roles.value.includes(r))
    }
    return roles.value.includes(role)
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    username,
    avatar,
    roles,
    permissions,
    login,
    logout,
    resetToken,
    loadFromStorage,
    hasPermission,
    hasRole
  }
})
