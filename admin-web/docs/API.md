# API接口文档

**文档版本**：v1.0
**最后更新**：2026-06-29
**项目名称**：PayGateway 企业支付网关运营管理平台
**文档状态**：✅ 已完成
**说明**：本文档定义前后端接口约定，MVP阶段前端使用Mock数据，v1.0正式版将对接以下API。

---

## 1. 通用约定

### 1.1 基础信息

| 项目 | 约定 |
|------|------|
| API基础路径 | `/api/v1` |
| 协议 | HTTPS |
| 数据格式 | JSON |
| 字符编码 | UTF-8 |
| 时间格式 | ISO 8601（`YYYY-MM-DD HH:mm:ss`） |
| 时区 | Asia/Shanghai（UTC+8） |
| 分页默认 | page=1, pageSize=10 |

### 1.2 请求头

| Header | 必填 | 说明 |
|--------|------|------|
| Content-Type | 是 | `application/json` |
| Authorization | 是（登录接口除外） | `Bearer {jwt_token}` |
| X-Request-Id | 否 | 请求追踪ID，不传则服务端生成 |

### 1.3 统一响应格式

```json
{
  "code": 0,
  "message": "success",
  "data": {},
  "requestId": "req_20260628_abc123",
  "timestamp": 1719552000000
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| code | number | 业务状态码，0=成功，非0=失败 |
| message | string | 提示信息 |
| data | any | 响应数据（对象/数组/分页） |
| requestId | string | 请求追踪ID |
| timestamp | number | 服务器时间戳（毫秒） |

### 1.4 分页响应格式

```json
{
  "code": 0,
  "data": {
    "list": [],
    "total": 56,
    "page": 1,
    "pageSize": 10,
    "totalPages": 6
  }
}
```

### 1.5 错误码规范

| HTTP状态码 | 业务错误码 | 说明 |
|------------|------------|------|
| 200 | 0 | 请求成功 |
| 400 | 40000 | 请求参数错误 |
| 400 | 40001 | 参数校验失败 |
| 401 | 40100 | 未授权（Token缺失） |
| 401 | 40101 | Token已过期 |
| 401 | 40102 | Token无效 |
| 403 | 40300 | 无权限访问 |
| 404 | 40400 | 资源不存在 |
| 409 | 40900 | 资源冲突 |
| 429 | 42900 | 请求过于频繁 |
| 500 | 50000 | 服务器内部错误 |
| 503 | 50300 | 服务暂不可用 |

---

## 2. 认证接口

### 2.1 用户登录

**POST** `/api/v1/auth/login`

**请求体**：
```json
{
  "username": "admin",
  "password": "Admin@123",
  "captchaId": "captcha_xxx",
  "captchaCode": "a3x9"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | string | 是 | 用户名 |
| password | string | 是 | 密码（RSA加密传输） |
| captchaId | string | 否 | 验证码ID（连续失败3次后必填） |
| captchaCode | string | 否 | 验证码 |

**响应数据**：
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 7200,
  "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
  "userInfo": {
    "userId": "U10001",
    "username": "admin",
    "realName": "超级管理员",
    "role": "SUPER_ADMIN",
    "avatar": "https://cdn.example.com/avatars/default.png",
    "email": "t9斯e@qXBk1qE.a運",
    "phone": "NOkT4YYwjyD"
  }
}
```

### 2.2 刷新Token

**POST** `/api/v1/auth/refresh`

**请求体**：
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIs..."
}
```

**响应数据**：同登录响应

### 2.3 获取当前用户信息

**GET** `/api/v1/auth/me`

**响应数据**：同登录响应中的userInfo

### 2.4 修改密码

**PUT** `/api/v1/auth/password`

**请求体**：
```json
{
  "oldPassword": "OldPwd@123",
  "newPassword": "NewPwd@456",
  "confirmPassword": "NewPwd@456"
}
```

### 2.5 退出登录

**POST** `/api/v1/auth/logout`

---

## 3. Dashboard接口

### 3.1 获取核心指标

**GET** `/api/v1/dashboard/statistics`

**查询参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| startTime | string | 否 | 开始时间 |
| endTime | string | 否 | 结束时间 |

**响应数据**：
```json
{
  "todayAmount": 12845632.00,
  "todayAmountGrowth": 12.5,
  "todayCount": 86429,
  "todayCountGrowth": 8.3,
  "successRate": 99.72,
  "successRateChange": -0.15,
  "avgResponseTime": 38,
  "avgResponseTimeChange": -5.2
}
```

### 3.2 获取交易趋势

**GET** `/api/v1/dashboard/trend`

**查询参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| type | string | 是 | 1h/24h/7d |

**响应数据**：
```json
{
  "xAxis": ["00:00", "01:00", "..."],
  "successAmount": [1200000, 1100000, "..."],
  "successCount": [8000, 7500, "..."],
  "failCount": [50, 45, "..."]
}
```

### 3.3 获取通道状态

**GET** `/api/v1/dashboard/channels`

**响应数据**：
```json
[
  {
    "channelId": "C001",
    "channelName": "微信支付",
    "channelCode": "WECHAT",
    "payTypes": ["Native", "JSAPI", "H5"],
    "status": "NORMAL",
    "successRate": 99.89,
    "avgLatency": 32,
    "qps": 1256
  }
]
```

通道状态枚举：`NORMAL`(正常)、`FLUCTUATING`(波动)、`ABNORMAL`(异常)、`DISABLED`(停用)

### 3.4 获取实时告警

**GET** `/api/v1/dashboard/alerts`

**查询参数**：page, pageSize

**响应数据**：分页列表
```json
{
  "alertId": "ALT202606280001",
  "type": "CHANNEL_TIMEOUT",
  "level": "HIGH",
  "title": "银联通道响应超时率超过5%",
  "content": "通道响应时间异常，请及时检查通道状态",
  "createdAt": "2026-06-28 14:30:00",
  "read": false
}
```

告警类型枚举：`CHANNEL_TIMEOUT`、`RISK_BLOCK`、`TRADE_WARNING`、`RECON_DONE`、`SYSTEM_NOTICE`
告警级别枚举：`HIGH`、`MEDIUM`、`LOW`

### 3.5 获取最近交易

**GET** `/api/v1/dashboard/recent-transactions`

**查询参数**：limit（默认6）

**响应数据**：交易列表（见交易订单数据结构）

### 3.6 获取通道金额分布

**GET** `/api/v1/dashboard/amount-distribution`

**查询参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| dimension | string | 是 | channel/currency |

**响应数据**：
```json
[
  {
    "name": "微信支付",
    "value": 4945568,
    "percentage": 38.5
  }
]
```

---

## 4. 商户管理接口

### 4.1 商户列表

**GET** `/api/v1/merchants`

**查询参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| merchantName | string | 否 | 商户名称（模糊搜索） |
| merchantNo | string | 否 | 商户号 |
| status | string | 否 | NORMAL/PENDING/FROZEN/DISABLED |
| startTime | string | 否 | 入驻开始时间 |
| endTime | string | 否 | 入驻结束时间 |
| page | number | 否 | 页码 |
| pageSize | number | 否 | 每页条数 |

**响应数据**：分页列表
```json
{
  "merchantId": "M100001",
  "merchantName": "星辰电商平台",
  "industry": "E_COMMERCE",
  "contactName": "张三",
  "contactPhone": "NOkT4YYwjyD",
  "totalAmount": 128560000.00,
  "status": "NORMAL",
  "createdAt": "2025-08-15 10:30:00"
}
```

### 4.2 商户详情

**GET** `/api/v1/merchants/{merchantId}`

**响应数据**：商户完整信息

### 4.3 新增商户

**POST** `/api/v1/merchants`

**请求体**：商户信息表单

### 4.4 更新商户

**PUT** `/api/v1/merchants/{merchantId}`

### 4.5 冻结/解冻商户

**PUT** `/api/v1/merchants/{merchantId}/status`

**请求体**：
```json
{
  "status": "FROZEN",
  "reason": "违规操作"
}
```

### 4.6 进件审核列表

**GET** `/api/v1/merchants/audits`

### 4.7 审核通过

**POST** `/api/v1/merchants/audits/{auditId}/approve`

### 4.8 审核驳回

**POST** `/api/v1/merchants/audits/{auditId}/reject`

**请求体**：
```json
{
  "reason": "营业执照信息不清晰"
}
```

---

## 5. 交易管理接口

### 5.1 交易订单列表

**GET** `/api/v1/transactions`

**查询参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| orderNo | string | 否 | 订单号 |
| merchantId | string | 否 | 商户号 |
| channelCode | string | 否 | 支付通道 |
| status | string | 否 | SUCCESS/PAYING/FAILED/REFUNDED/CLOSED |
| payType | string | 否 | JSAPI/NATIVE/H5/APP/PC/CREDIT_CARD |
| minAmount | number | 否 | 最小金额 |
| maxAmount | number | 否 | 最大金额 |
| startTime | string | 否 | 开始时间 |
| endTime | string | 否 | 结束时间 |
| page | number | 否 | 页码 |
| pageSize | number | 否 | 每页条数 |

**响应数据**：
```json
{
  "orderNo": "PAY20260628000123456",
  "merchantId": "M100001",
  "merchantName": "星辰电商平台",
  "channelCode": "WECHAT",
  "channelName": "微信支付",
  "payType": "JSAPI",
  "amount": 29900,
  "fee": 179.40,
  "currency": "CNY",
  "status": "SUCCESS",
  "clientIp": "192.168.1.100",
  "createdAt": "2026-06-28 14:32:15",
  "paidAt": "2026-06-28 14:32:18"
}
```

交易状态枚举：`PAYING`(支付中)、`SUCCESS`(支付成功)、`FAILED`(支付失败)、`CLOSED`(已关闭)、`REFUNDED`(已退款)、`PARTIAL_REFUNDED`(部分退款)

### 5.2 交易详情

**GET** `/api/v1/transactions/{orderNo}`

**响应数据**：交易完整信息（含请求参数、响应参数、回调记录）

### 5.3 退款订单列表

**GET** `/api/v1/refunds`

**响应数据**：
```json
{
  "refundNo": "REF202606280001",
  "orderNo": "PAY20260628000123456",
  "merchantId": "M100001",
  "refundAmount": 29900,
  "refundReason": "用户申请退款",
  "status": "SUCCESS",
  "createdAt": "2026-06-28 15:00:00",
  "successAt": "2026-06-28 15:00:05"
}
```

退款状态枚举：`REFUNDING`(退款中)、`SUCCESS`(退款成功)、`FAILED`(退款失败)

### 5.4 发起退款

**POST** `/api/v1/transactions/{orderNo}/refund`

**请求体**：
```json
{
  "refundAmount": 29900,
  "refundReason": "用户申请退款"
}
```

### 5.5 退款重试

**POST** `/api/v1/refunds/{refundNo}/retry`

### 5.6 交易导出

**GET** `/api/v1/transactions/export`

响应：Excel文件流

---

## 6. 支付通道接口

### 6.1 通道列表

**GET** `/api/v1/channels`

### 6.2 通道详情

**GET** `/api/v1/channels/{channelId}`

### 6.3 创建/更新通道配置

**POST/PUT** `/api/v1/channels`

**请求体**：
```json
{
  "channelName": "微信支付",
  "channelCode": "WECHAT",
  "channelType": "THIRD_PARTY",
  "config": {
    "appId": "wx1234567890",
    "mchId": "1600000001",
    "apiKey": "xxxxxx",
    "certPath": "/certs/wechat/apiclient_cert.p12"
  },
  "feeRates": {
    "JSAPI": 0.006,
    "NATIVE": 0.006,
    "H5": 0.006
  },
  "limits": {
    "singleLimit": 50000,
    "dailyLimit": 10000000
  },
  "enabled": true
}
```

### 6.4 切换通道状态

**PUT** `/api/v1/channels/{channelId}/status`

### 6.5 路由规则列表

**GET** `/api/v1/channels/routes`

### 6.6 创建路由规则

**POST** `/api/v1/channels/routes`

**请求体**：
```json
{
  "ruleName": "小额交易优先微信",
  "priority": 1,
  "conditions": {
    "minAmount": 0,
    "maxAmount": 10000,
    "payTypes": ["JSAPI", "NATIVE"],
    "merchantIds": [],
    "timeRange": { "start": "00:00", "end": "23:59" }
  },
  "targets": [
    { "channelId": "C001", "weight": 70 },
    { "channelId": "C002", "weight": 30 }
  ],
  "enabled": true
}
```

### 6.7 更新路由规则

**PUT** `/api/v1/channels/routes/{ruleId}`

### 6.8 删除路由规则

**DELETE** `/api/v1/channels/routes/{ruleId}`

---

## 7. 风控接口

### 7.1 风控规则列表

**GET** `/api/v1/risk/rules`

**查询参数**：category（TRADE_LIMIT/FREQUENCY/IP_BLACKLIST/DEVICE/GEO）

### 7.2 创建/更新风控规则

**POST/PUT** `/api/v1/risk/rules`

**请求体**：
```json
{
  "ruleName": "单笔金额超限拦截",
  "category": "TRADE_LIMIT",
  "condition": {
    "field": "amount",
    "operator": "GT",
    "value": 1000000
  },
  "action": "BLOCK",
  "actionNote": "单笔交易金额超过100万",
  "enabled": true
}
```

动作枚举：`BLOCK`(拦截)、`REVIEW`(人工审核)、`ALERT`(预警)

### 7.3 切换规则状态

**PUT** `/api/v1/risk/rules/{ruleId}/status`

### 7.4 风险事件列表

**GET** `/api/v1/risk/events`

**查询参数**：type、level(HIGH/MEDIUM/LOW)、status(PENDING/PROCESSED/IGNORED)、时间范围

**响应数据**：
```json
{
  "eventId": "RISK202606280001",
  "type": "HIGH_FREQUENCY",
  "level": "HIGH",
  "orderNo": "PAY20260628000123456",
  "ruleName": "IP高频访问规则",
  "detail": {
    "ip": "192.168.1.xxx",
    "count": 200,
    "period": "5分钟"
  },
  "status": "PENDING",
  "triggeredAt": "2026-06-28 14:25:00"
}
```

### 7.5 处理风险事件

**POST** `/api/v1/risk/events/{eventId}/handle`

**请求体**：
```json
{
  "action": "PROCESSED",
  "note": "确认为正常促销活动流量",
  "addBlacklist": false
}
```

### 7.6 IP黑白名单管理

**GET/POST/DELETE** `/api/v1/risk/ip-list`

---

## 8. 对账接口

### 8.1 对账任务列表

**GET** `/api/v1/reconciliation/tasks`

**响应数据**：
```json
{
  "taskId": "REC20260628",
  "taskDate": "2026-06-28",
  "channelCode": "WECHAT",
  "channelCount": 86500,
  "channelAmount": 1284563200,
  "platformCount": 86429,
  "platformAmount": 1284563200,
  "diffCount": 5,
  "diffAmount": 12500,
  "status": "COMPLETED",
  "completedAt": "2026-06-29 02:05:00"
}
```

对账状态枚举：`PROCESSING`(对账中)、`COMPLETED`(对账完成)、`ERROR`(对账异常)

### 8.2 对账差异列表

**GET** `/api/v1/reconciliation/tasks/{taskId}/diffs`

### 8.3 处理差异

**POST** `/api/v1/reconciliation/diffs/{diffId}/handle`

### 8.4 重新对账

**POST** `/api/v1/reconciliation/tasks/{taskId}/retry`

### 8.5 下载对账单

**GET** `/api/v1/reconciliation/tasks/{taskId}/download`

### 8.6 对账报表列表

**GET** `/api/v1/reconciliation/reports`

### 8.7 导出报表

**GET** `/api/v1/reconciliation/reports/export`

---

## 9. 系统设置接口

### 9.1 获取系统配置

**GET** `/api/v1/system/config`

### 9.2 更新系统配置

**PUT** `/api/v1/system/config`

**请求体**：
```json
{
  "basic": {
    "systemName": "PayGateway支付网关",
    "defaultTimezone": "Asia/Shanghai",
    "defaultCurrency": "CNY"
  },
  "trade": {
    "orderTimeout": 1800,
    "autoRefund": false,
    "callbackRetryTimes": 3
  },
  "notify": {
    "channels": ["IN_APP", "EMAIL"],
    "receivers": ["t9斯e@qXBk1qE.a運"]
  },
  "security": {
    "passwordMinLength": 8,
    "passwordRequireMixed": true,
    "loginFailLockCount": 5,
    "sessionTimeout": 7200
  }
}
```

---

## 10. 通知接口

### 10.1 获取未读消息数

**GET** `/api/v1/notifications/unread-count`

### 10.2 通知列表

**GET** `/api/v1/notifications`

**查询参数**：category（ALL/RISK/TRANSACTION/SYSTEM）、page、pageSize

### 10.3 标记已读

**PUT** `/api/v1/notifications/{notificationId}/read`

### 10.4 全部标记已读

**PUT** `/api/v1/notifications/read-all`

---

## 11. 全局搜索接口

### 11.1 全局搜索

**GET** `/api/v1/search`

**查询参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| keyword | string | 是 | 搜索关键词 |
| types | string | 否 | 搜索类型，逗号分隔（page/merchant/order） |

**响应数据**：
```json
{
  "pages": [
    { "title": "商户列表", "path": "/merchant/list", "icon": "OfficeBuilding" }
  ],
  "merchants": [
    { "merchantId": "M100001", "merchantName": "星辰电商平台", "status": "NORMAL" }
  ],
  "orders": [
    { "orderNo": "PAY20260628000123456", "amount": 29900, "status": "SUCCESS" }
  ]
}
```

---

## 12. 数据字典

### 12.1 行业类型
| 值 | 说明 |
|----|------|
| E_COMMERCE | 电商零售 |
| CATERING | 餐饮美食 |
| EDUCATION | 教育培训 |
| TRANSPORT | 出行交通 |
| ENTERTAINMENT | 数字娱乐 |
| HEALTHCARE | 医疗健康 |
| FINANCE | 金融服务 |
| OTHER | 其他 |

### 12.2 支付方式
| 值 | 说明 |
|----|------|
| JSAPI | 公众号支付 |
| NATIVE | 扫码支付 |
| H5 | H5支付 |
| APP | APP支付 |
| PC | 电脑网站支付 |
| WAP | 手机网站支付 |
| CREDIT_CARD | 信用卡支付 |
| CLOUD_FAST | 云闪付 |

### 12.3 商户状态
| 值 | 说明 |
|----|------|
| PENDING | 待审核 |
| NORMAL | 正常 |
| FROZEN | 已冻结 |
| DISABLED | 已禁用 |
| REJECTED | 已驳回 |
