import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api/auth'
import { getToken, setToken, removeToken, getUserInfo, setUserInfo, removeUserInfo, logout as authLogout, type UserInfo } from '@/utils/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(getToken() || '')
  const userInfo = ref<UserInfo | null>(getUserInfo())

  const isLoggedIn = computed(() => !!token.value && !!userInfo.value)
  const username = computed(() => userInfo.value?.realName || userInfo.value?.username || '')
  const avatar = computed(() => userInfo.value?.avatar || '')
  const roles = computed(() => userInfo.value?.roles || (userInfo.value?.roleCode ? [userInfo.value.roleCode] : ['admin']))
  const permissions = computed(() => userInfo.value?.permissions || ['*'])

  async function login(loginForm: { username: string; password: string }) {
    const res = await authApi.login(loginForm)
    const { token: newToken, userInfo: newUserInfo } = res
    const adaptedUserInfo: UserInfo = {
      ...newUserInfo,
      roles: newUserInfo.roles || (newUserInfo.roleCode ? [newUserInfo.roleCode] : ['admin']),
      permissions: newUserInfo.permissions || ['*'],
      lastLoginTime: newUserInfo.loginTime || newUserInfo.lastLoginTime
    }
    token.value = newToken
    userInfo.value = adaptedUserInfo
    setToken(newToken)
    setUserInfo(adaptedUserInfo)
    return { token: newToken, userInfo: adaptedUserInfo }
  }

  async function fetchUserInfo() {
    try {
      const info = await authApi.getUserInfo()
      const adaptedUserInfo: UserInfo = {
        ...info,
        roles: info.roles || (info.roleCode ? [info.roleCode] : ['admin']),
        permissions: info.permissions || ['*'],
        lastLoginTime: info.loginTime || info.lastLoginTime
      }
      userInfo.value = adaptedUserInfo
      setUserInfo(adaptedUserInfo)
      return adaptedUserInfo
    } catch (e) {
      return null
    }
  }

  function loadFromStorage() {
    token.value = getToken() || ''
    userInfo.value = getUserInfo()
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    authLogout()
    try {
      authApi.logout()
    } catch {}
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
    fetchUserInfo,
    logout,
    resetToken,
    loadFromStorage,
    hasPermission,
    hasRole
  }
})
