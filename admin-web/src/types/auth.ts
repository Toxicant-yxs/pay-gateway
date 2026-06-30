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
  refreshToken: string
  userInfo: UserInfo
}

export interface UserInfo {
  userId: string
  username: string
  realName: string
  role: UserRole
  avatar: string
  email: string
  phone: string
  department?: string
  lastLoginTime?: string
}

export type UserRole = 'SUPER_ADMIN' | 'OPERATOR' | 'RISK_OFFICER' | 'FINANCE' | 'AUDITOR'

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
