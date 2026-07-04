import request from '@/utils/request'
import type { LoginRequest, LoginResponse, UserInfo, UpdatePasswordRequest, LoginRecord } from '@/types/auth'

export const authApi = {
  login(data: LoginRequest) {
    return request<LoginResponse>({
      url: '/auth/login',
      method: 'post',
      data
    })
  },

  logout() {
    return request({
      url: '/auth/logout',
      method: 'post'
    })
  },

  getUserInfo() {
    return request<UserInfo>({
      url: '/auth/me',
      method: 'get'
    })
  },

  refreshToken(refreshToken: string) {
    return request<LoginResponse>({
      url: '/auth/refresh',
      method: 'post',
      data: { refreshToken }
    })
  },

  updatePassword(data: UpdatePasswordRequest) {
    return request({
      url: '/auth/password',
      method: 'put',
      data
    })
  },

  updateProfile(data: Partial<UserInfo>) {
    return request({
      url: '/auth/profile',
      method: 'put',
      data
    })
  },

  getLoginRecords(params?: { page?: number; pageSize?: number }) {
    return request<{ list: LoginRecord[]; total: number }>({
      url: '/auth/login-records',
      method: 'get',
      params
    })
  }
}
