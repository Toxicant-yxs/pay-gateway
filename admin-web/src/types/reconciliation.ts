import type { ChannelCode } from './transaction'

export type ReconStatus = 'PENDING' | 'DOWNLOADING' | 'PARSING' | 'COMPARING' | 'COMPLETED' | 'ERROR'

export interface ReconTask {
  taskId: string
  taskNo: string
  taskDate: string
  channelCode: ChannelCode
  channelName: string
  channelFileUrl?: string
  channelCount: number
  channelAmount: number
  platformCount: number
  platformAmount: number
  diffCount: number
  diffAmount: number
  successCount: number
  status: ReconStatus
  errorMsg?: string
  startedAt?: string
  completedAt?: string
  createdAt: string
}

export interface ReconDetail {
  detailId: string
  taskId: string
  orderNo: string
  channelOrderNo: string
  diffType: DiffType
  platformAmount?: number
  channelAmount?: number
  platformStatus?: string
  channelStatus?: string
  handleStatus: 'PENDING' | 'HANDLED' | 'IGNORED'
  handleNote?: string
  createdAt: string
}

export type DiffType = 'AMOUNT_MISMATCH' | 'CHANNEL_ONLY' | 'PLATFORM_ONLY' | 'STATUS_MISMATCH'

export interface ReconStats {
  todayCount: number
  diffCount: number
  diffAmount: number
  pendingDiffCount: number
}

export interface ReconQuery {
  channelCode?: ChannelCode | ''
  status?: ReconStatus | ''
  startDate?: string
  endDate?: string
  page?: number
  pageSize?: number
}

export interface ReconReport {
  reportId: string
  reportName: string
  reportType: 'DAILY' | 'MONTHLY' | 'CUSTOM'
  channelCode: ChannelCode
  channelName: string
  startDate: string
  endDate: string
  totalCount: number
  totalAmount: number
  diffCount: number
  fileUrl?: string
  createdAt: string
}
