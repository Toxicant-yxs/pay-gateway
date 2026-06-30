export interface MerchantStatusCount {
  pending: number
  normal: number
  frozen: number
  disabled: number
  rejected: number
}

export interface MerchantItem {
  merchantId: string
  merchantNo: string
  merchantName: string
  industry: IndustryType
  contactName: string
  contactPhone: string
  totalAmount: number
  status: MerchantStatus
  createdAt: string
  level: number
  authStatus: 0 | 1
  settleCycle: string
  dailyLimit: number
  singleLimit: number
  shortName?: string
  businessLicense?: string
  legalPerson?: string
  contactEmail?: string
}

export type MerchantStatus = 'PENDING' | 'NORMAL' | 'FROZEN' | 'DISABLED' | 'REJECTED'
export type IndustryType = 'E_COMMERCE' | 'CATERING' | 'EDUCATION' | 'TRANSPORT' | 'ENTERTAINMENT' | 'HEALTHCARE' | 'FINANCE' | 'OTHER'

export interface MerchantQuery {
  merchantName?: string
  merchantNo?: string
  status?: MerchantStatus | ''
  startTime?: string
  endTime?: string
  page?: number
  pageSize?: number
}

export interface MerchantAuditItem {
  auditId: string
  merchantId: string
  merchantNo: string
  merchantName: string
  industry: IndustryType
  contactName: string
  contactPhone: string
  submittedAt: string
  status: 'PENDING' | 'APPROVED' | 'REJECTED'
  rejectReason?: string
}

export interface AuditStats {
  pending: number
  approved: number
  rejected: number
  todayNew: number
}
