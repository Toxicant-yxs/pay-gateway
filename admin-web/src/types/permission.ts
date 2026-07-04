export interface SysUser {
  id: number | string
  username: string
  realName: string
  avatar?: string
  email?: string
  phone?: string
  roleCode?: string
  roleName?: string
  status: number
  roleIds?: number[]
  lastLoginTime?: string
  lastLoginIp?: string
  createdAt?: string
  updatedAt?: string
}

export interface SysRole {
  id: number | string
  roleCode: string
  roleName: string
  description?: string
  dataScope?: number
  status: number
  sortOrder?: number
  permissionIds?: number[]
  createdAt?: string
  updatedAt?: string
}

export interface SysPermission {
  id: number | string
  parentId: number | string
  permissionCode: string
  permissionName: string
  permissionType: number
  path?: string
  component?: string
  icon?: string
  sortOrder?: number
  visible?: number
  status: number
  remark?: string
  children?: SysPermission[]
  createdAt?: string
  updatedAt?: string
}
