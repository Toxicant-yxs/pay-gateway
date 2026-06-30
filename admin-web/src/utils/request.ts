import axios, { type AxiosInstance, type AxiosRequestConfig, type AxiosResponse, type InternalAxiosRequestConfig } from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getToken, removeToken } from '@/utils/auth'
import router from '@/router'
import { useUserStore } from '@/stores/user'

const service: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api/v1',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

let isReloginShowing = false

service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = getToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    const requestId = `req_${Date.now()}_${Math.random().toString(36).substring(2, 11)}`
    config.headers['X-Request-Id'] = requestId
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

function handleUnauthorized() {
  if (!isReloginShowing) {
    isReloginShowing = true
    try {
      const userStore = useUserStore()
      userStore.logout()
    } catch {}
    ElMessageBox.confirm('登录状态已过期，请重新登录', '系统提示', {
      confirmButtonText: '重新登录',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      router.push('/login')
    }).finally(() => {
      isReloginShowing = false
    })
  }
}

service.interceptors.response.use(
  (response: AxiosResponse) => {
    const res = response.data

    if (res === undefined || res === null) {
      return res
    }

    if (res.code === undefined && response.config.responseType === 'blob') {
      return res
    }

    if (res.code === undefined) {
      return res
    }

    if (res.code === 0 || res.code === 200) {
      return res.data
    }

    if (res.code === 401) {
      handleUnauthorized()
      return Promise.reject(new Error('Unauthorized'))
    }

    if (res.code === 403) {
      ElMessage.error(res.message || '没有权限访问该资源')
      return Promise.reject(new Error(res.message || 'Forbidden'))
    }

    ElMessage.error(res.message || '请求失败')
    return Promise.reject(new Error(res.message || '请求失败'))
  },
  (error) => {
    let message = '网络异常，请稍后重试'

    if (error.response) {
      const { status } = error.response
      switch (status) {
        case 400:
          message = '请求参数错误'
          break
        case 401:
          handleUnauthorized()
          return Promise.reject(error)
        case 403:
          message = '没有权限访问该资源'
          break
        case 404:
          message = '请求资源不存在'
          break
        case 500:
          message = '服务器内部错误'
          break
        case 502:
          message = '网关错误'
          break
        case 503:
          message = '服务不可用'
          break
        case 504:
          message = '网关超时'
          break
        default:
          message = `请求失败(${status})`
      }
    } else if (error.code === 'ECONNABORTED') {
      message = '请求超时，请稍后重试'
    } else if (error.message === 'Network Error') {
      message = '网络连接失败，请检查网络'
    }

    if (error.response?.status !== 401) {
      ElMessage.error(message)
    }
    return Promise.reject(error)
  }
)

export function request<T = any>(config: AxiosRequestConfig): Promise<T> {
  return new Promise((resolve, reject) => {
    service(config)
      .then((response: any) => {
        resolve(response as T)
      })
      .catch((error: any) => {
        reject(error)
      })
  })
}

export function get<T = any>(url: string, params?: any, config?: AxiosRequestConfig): Promise<T> {
  return request<T>({ method: 'GET', url, params, ...config })
}

export function post<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
  return request<T>({ method: 'POST', url, data, ...config })
}

export function put<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
  return request<T>({ method: 'PUT', url, data, ...config })
}

export function del<T = any>(url: string, config?: AxiosRequestConfig): Promise<T> {
  return request<T>({ method: 'DELETE', url, ...config })
}

export { request as default }
export const axiosInstance = service
