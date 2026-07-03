import request from '@/utils/request'
import type { SysUser, SysRole, SysPermission } from '@/types/permission'
import type { PageResult } from '@/types/api'

export const userApi = {
  getList(params?: { page?: number; pageSize?: number; keyword?: string; status?: number }) {
    return request<PageResult<SysUser>>({
      url: '/system/users',
      method: 'get',
      params
    })
  },

  getById(id: number | string) {
    return request<SysUser>({
      url: `/system/users/${id}`,
      method: 'get'
    })
  },

  create(data: Partial<SysUser>) {
    return request<SysUser>({
      url: '/system/users',
      method: 'post',
      data
    })
  },

  update(id: number | string, data: Partial<SysUser>) {
    return request<SysUser>({
      url: `/system/users/${id}`,
      method: 'put',
      data
    })
  },

  delete(id: number | string) {
    return request({
      url: `/system/users/${id}`,
      method: 'delete'
    })
  },

  updateStatus(id: number | string, status: number) {
    return request({
      url: `/system/users/${id}/status`,
      method: 'put',
      data: { status }
    })
  },

  resetPassword(id: number | string, newPassword: string) {
    return request({
      url: `/system/users/${id}/reset-password`,
      method: 'put',
      data: { newPassword }
    })
  },

  assignRoles(id: number | string, roleIds: number[]) {
    return request({
      url: `/system/users/${id}/roles`,
      method: 'put',
      data: { roleIds }
    })
  }
}

export const roleApi = {
  getList(params?: { page?: number; pageSize?: number; keyword?: string; status?: number }) {
    return request<PageResult<SysRole>>({
      url: '/system/roles',
      method: 'get',
      params
    })
  },

  getAll() {
    return request<SysRole[]>({
      url: '/system/roles/all',
      method: 'get'
    })
  },

  getById(id: number | string) {
    return request<SysRole>({
      url: `/system/roles/${id}`,
      method: 'get'
    })
  },

  create(data: Partial<SysRole>) {
    return request<SysRole>({
      url: '/system/roles',
      method: 'post',
      data
    })
  },

  update(id: number | string, data: Partial<SysRole>) {
    return request<SysRole>({
      url: `/system/roles/${id}`,
      method: 'put',
      data
    })
  },

  delete(id: number | string) {
    return request({
      url: `/system/roles/${id}`,
      method: 'delete'
    })
  },

  assignPermissions(id: number | string, permissionIds: number[]) {
    return request({
      url: `/system/roles/${id}/permissions`,
      method: 'put',
      data: { permissionIds }
    })
  },

  getRolePermissions(id: number | string) {
    return request<number[]>({
      url: `/system/roles/${id}/permissions`,
      method: 'get'
    })
  },

  getPermissionTree() {
    return request<SysPermission[]>({
      url: '/system/roles/permissions/tree',
      method: 'get'
    })
  }
}

export const permissionApi = {
  getList(params?: { keyword?: string; type?: number; status?: number }) {
    return request<SysPermission[]>({
      url: '/system/permissions',
      method: 'get',
      params
    })
  },

  getById(id: number | string) {
    return request<SysPermission>({
      url: `/system/permissions/${id}`,
      method: 'get'
    })
  },

  create(data: Partial<SysPermission>) {
    return request<SysPermission>({
      url: '/system/permissions',
      method: 'post',
      data
    })
  },

  update(id: number | string, data: Partial<SysPermission>) {
    return request<SysPermission>({
      url: `/system/permissions/${id}`,
      method: 'put',
      data
    })
  },

  delete(id: number | string) {
    return request({
      url: `/system/permissions/${id}`,
      method: 'delete'
    })
  }
}
