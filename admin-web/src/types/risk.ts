import type { TradeStatus } from './transaction'

export type RuleCategory = 'TRADE_LIMIT' | 'FREQUENCY' | 'IP_BLACKLIST' | 'DEVICE' | 'GEO'
export type RuleAction = 'BLOCK' | 'REVIEW' | 'ALERT'
export type RiskLevel = 'HIGH' | 'MEDIUM' | 'LOW'
export type EventStatus = 'PENDING' | 'PROCESSED' | 'IGNORED'

export interface RiskRule {
  ruleId: string
  ruleCode: string
  ruleName: string
  category: RuleCategory
  conditionExpr: ConditionExpr
  action: RuleAction
  riskLevel: RiskLevel
  priority: number
  status: 0 | 1
  effectStart?: string
  effectEnd?: string
  description?: string
  createdAt: string
  updatedAt: string
}

export interface ConditionExpr {
  conditions: Condition[]
  logic: 'AND' | 'OR'
}

export interface Condition {
  field: string
  operator: 'GT' | 'GTE' | 'LT' | 'LTE' | 'EQ' | 'NEQ' | 'IN' | 'NOT_IN' | 'BETWEEN'
  value: any
}

export interface RiskEvent {
  eventId: string
  eventNo: string
  category: RuleCategory
  riskLevel: RiskLevel
  orderNo?: string
  merchantName?: string
  triggerRule: string
  eventDetail: Record<string, any>
  status: EventStatus
  handleNote?: string
  triggeredAt: string
  handledAt?: string
}

export interface EventQuery {
  category?: RuleCategory | ''
  riskLevel?: RiskLevel | ''
  status?: EventStatus | ''
  orderNo?: string
  startTime?: string
  endTime?: string
  page?: number
  pageSize?: number
}

export interface RiskEventStats {
  highCount: number
  mediumCount: number
  lowCount: number
  pendingCount: number
}

export interface BlacklistItem {
  id: string
  type: 'IP' | 'CARD' | 'DEVICE' | 'MERCHANT' | 'USER'
  value: string
  reason: string
  status: 0 | 1
  expireAt?: string
  createdAt: string
}

export interface EventHandleRequest {
  action: 'PROCESSED' | 'IGNORED'
  note: string
  addBlacklist?: boolean
}
