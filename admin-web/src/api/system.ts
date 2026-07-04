import request from '@/utils/request'
import type { SystemConfigItem, SystemConfigResponse, SystemNotification, OperationLog, SearchResult } from '@/types/system'
import type { PageResult } from '@/types/api'

export const systemApi = {
  getConfig() {
    return request<SystemConfigResponse>({
      url: '/system/config',
      method: 'get'
    })
  },

  updateConfig(data: SystemConfigItem[]) {
    return request({
      url: '/system/config',
      method: 'put',
      data
    })
  },

  getUnreadCount() {
    return request<number>({
      url: '/notifications/unread-count',
      method: 'get'
    })
  },

  getNotifications(params?: { category?: string; page?: number; pageSize?: number }) {
    return request<PageResult<SystemNotification>>({
      url: '/notifications',
      method: 'get',
      params
    })
  },

  markRead(notifyId: string) {
    return request({
      url: `/notifications/${notifyId}/read`,
      method: 'put'
    })
  },

  markAllRead() {
    return request({
      url: '/notifications/read-all',
      method: 'put'
    })
  },

  getOperationLogs(params?: { page?: number; pageSize?: number; username?: string; startTime?: string; endTime?: string }) {
    return request<PageResult<OperationLog>>({
      url: '/system/logs',
      method: 'get',
      params
    })
  },

  globalSearch(params: { keyword: string; types?: string }) {
    return request<SearchResult>({
      url: '/search',
      method: 'get',
      params
    })
  }
}
