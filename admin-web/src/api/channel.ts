import request from '@/utils/request'
import type { ChannelInfo, ChannelQuery, RouteRule } from '@/types/channel'
import type { PageResult } from '@/types/api'

export const channelApi = {
  getList(params?: ChannelQuery) {
    return request<ChannelInfo[]>({
      url: '/channels',
      method: 'get',
      params
    })
  },

  getDetail(channelId: string) {
    return request<ChannelInfo>({
      url: `/channels/${channelId}`,
      method: 'get'
    })
  },

  create(data: Partial<ChannelInfo>) {
    return request({
      url: '/channels',
      method: 'post',
      data
    })
  },

  update(channelId: string, data: Partial<ChannelInfo>) {
    return request({
      url: `/channels/${channelId}`,
      method: 'put',
      data
    })
  },

  updateStatus(channelId: string, status: 0 | 1) {
    return request({
      url: `/channels/${channelId}/status`,
      method: 'put',
      data: { status }
    })
  },

  getRouteRules(params?: { page?: number; pageSize?: number; status?: number }) {
    return request<PageResult<RouteRule>>({
      url: '/channels/routes',
      method: 'get',
      params
    })
  },

  createRouteRule(data: Partial<RouteRule>) {
    return request({
      url: '/channels/routes',
      method: 'post',
      data
    })
  },

  updateRouteRule(ruleId: string, data: Partial<RouteRule>) {
    return request({
      url: `/channels/routes/${ruleId}`,
      method: 'put',
      data
    })
  },

  deleteRouteRule(ruleId: string) {
    return request({
      url: `/channels/routes/${ruleId}`,
      method: 'delete'
    })
  }
}
