export interface LoginRequest {
  username: string
  password: string
  captchaId?: string
  captchaCode?: string
}

export interface LoginResponse {
  token: string
  tokenType: string
  expiresIn: number
  userInfo: UserInfo
}

export interface UserInfo {
  userId: number | string
  username: string
  realName: string
  roleCode?: string
  role?: string
  avatar: string
  email?: string
  phone?: string
  department?: string
  loginTime?: string
  lastLoginTime?: string
  roles?: string[]
  permissions?: string[]
}

export interface UpdatePasswordRequest {
  oldPassword: string
  newPassword: string
  confirmPassword: string
}

export interface LoginRecord {
  id: string
  loginTime: string
  ip: string
  location: string
  browser: string
  os: string
  status: 'success' | 'failed'
}
