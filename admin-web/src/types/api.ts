export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
  requestId: string
  timestamp: number
}

export interface PageResult<T = any> {
  list: T[]
  total: number
  page: number
  pageSize: number
  totalPages: number
}

export interface PageQuery {
  page?: number
  pageSize?: number
}

export interface SelectOption {
  label: string
  value: string | number
}
