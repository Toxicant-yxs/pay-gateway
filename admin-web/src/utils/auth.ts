const TOKEN_KEY = 'pay_gateway_token'
const USER_KEY = 'pay_gateway_user'

export interface UserInfo {
  userId: string
  username: string
  realName: string
  avatar?: string
  roles: string[]
  permissions: string[]
}

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token)
}

export function removeToken(): void {
  localStorage.removeItem(TOKEN_KEY)
}

export function getUserInfo(): UserInfo | null {
  try {
    const userStr = localStorage.getItem(USER_KEY)
    if (!userStr) return null
    return JSON.parse(userStr) as UserInfo
  } catch {
    removeUserInfo()
    return null
  }
}

export function setUserInfo(user: UserInfo): void {
  localStorage.setItem(USER_KEY, JSON.stringify(user))
}

export function removeUserInfo(): void {
  localStorage.removeItem(USER_KEY)
}

export function isTokenValid(token: string | null): boolean {
  if (!token) return false
  try {
    const parts = token.split('.')
    if (parts.length !== 3) return false
    const payload = JSON.parse(atob(parts[1]))
    if (payload.exp && payload.exp * 1000 < Date.now()) {
      return false
    }
    return true
  } catch {
    return false
  }
}

export function logout(): void {
  removeToken()
  removeUserInfo()
}

export function generateMockToken(userId: string): string {
  const header = btoa(JSON.stringify({ alg: 'HS256', typ: 'JWT' }))
  const payload = btoa(JSON.stringify({
    sub: userId,
    iat: Math.floor(Date.now() / 1000),
    exp: Math.floor(Date.now() / 1000) + 7 * 24 * 60 * 60
  }))
  const signature = btoa('mock_signature')
  return `${header}.${payload}.${signature}`
}
