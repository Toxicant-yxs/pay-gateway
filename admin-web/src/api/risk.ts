import request from '@/utils/request'
import type { RiskRule, RiskEvent, EventQuery, RiskEventStats, EventHandleRequest, BlacklistItem } from '@/types/risk'
import type { PageResult } from '@/types/api'

export const riskApi = {
  getRules(params?: { category?: string; status?: number; page?: number; pageSize?: number }) {
    return request<PageResult<RiskRule>>({
      url: '/risk/rules',
      method: 'get',
      params
    })
  },

  createRule(data: Partial<RiskRule>) {
    return request({
      url: '/risk/rules',
      method: 'post',
      data
    })
  },

  updateRule(ruleId: string, data: Partial<RiskRule>) {
    return request({
      url: `/risk/rules/${ruleId}`,
      method: 'put',
      data
    })
  },

  updateRuleStatus(ruleId: string, status: 0 | 1) {
    return request({
      url: `/risk/rules/${ruleId}/status`,
      method: 'put',
      data: { status }
    })
  },

  getEvents(params: EventQuery) {
    return request<PageResult<RiskEvent>>({
      url: '/risk/events',
      method: 'get',
      params
    })
  },

  getEventDetail(eventId: string) {
    return request<RiskEvent>({
      url: `/risk/events/${eventId}`,
      method: 'get'
    })
  },

  handleEvent(eventId: string, data: EventHandleRequest) {
    return request({
      url: `/risk/events/${eventId}/handle`,
      method: 'post',
      data
    })
  },

  getEventStats() {
    return request<RiskEventStats>({
      url: '/risk/events/stats',
      method: 'get'
    })
  },

  getBlacklist(params?: { type?: string; page?: number; pageSize?: number }) {
    return request<PageResult<BlacklistItem>>({
      url: '/risk/blacklist',
      method: 'get',
      params
    })
  },

  addBlacklist(data: Partial<BlacklistItem>) {
    return request({
      url: '/risk/blacklist',
      method: 'post',
      data
    })
  },

  removeBlacklist(id: string) {
    return request({
      url: `/risk/blacklist/${id}`,
      method: 'delete'
    })
  }
}
