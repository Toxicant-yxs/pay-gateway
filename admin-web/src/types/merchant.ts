export interface MerchantStatusCount {
  pending: number
  normal: number
  frozen: number
  disabled: number
  rejected: number
}

export interface MerchantItem {
  id?: string
  merchantId?: string
  merchantNo: string
  merchantName: string
  name?: string
  industry?: string
  industryType?: IndustryType
  contactName: string
  contact?: string
  contactPhone: string
  phone?: string
  totalAmount?: number
  status: number | MerchantStatus
  createdAt: string
  createTime?: string
  level?: number | string
  authStatus?: 0 | 1
  settleCycle?: string
  dailyLimit?: number
  singleLimit?: number
  shortName?: string
  businessLicense?: string
  legalPerson?: string
  contactEmail?: string
  email?: string
  riskLevel?: number | string
  feeRate?: number
  avatar?: string
  avatarColor?: string
  address?: string
  licenseNo?: string
}

export type MerchantStatus = 'PENDING' | 'NORMAL' | 'FROZEN' | 'DISABLED' | 'REJECTED' | number
export type IndustryType = 'E_COMMERCE' | 'CATERING' | 'EDUCATION' | 'TRANSPORT' | 'ENTERTAINMENT' | 'HEALTHCARE' | 'FINANCE' | 'OTHER' | string

export interface MerchantQuery {
  merchantName?: string
  merchantNo?: string
  name?: string
  merchantId?: string
  status?: number | MerchantStatus | ''
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
