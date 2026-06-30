-- 系统用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(32) NOT NULL UNIQUE,
    password VARCHAR(128) NOT NULL,
    real_name VARCHAR(32),
    avatar VARCHAR(256),
    email VARCHAR(64),
    phone VARCHAR(20),
    role_code VARCHAR(32) NOT NULL DEFAULT 'admin',
    status TINYINT NOT NULL DEFAULT 1,
    last_login_time TIMESTAMP,
    last_login_ip VARCHAR(45),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0
);

-- 商户信息表
CREATE TABLE IF NOT EXISTS merchant_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    merchant_no VARCHAR(32) NOT NULL UNIQUE,
    merchant_name VARCHAR(128) NOT NULL,
    short_name VARCHAR(64),
    industry VARCHAR(32) NOT NULL,
    business_license VARCHAR(64),
    legal_person VARCHAR(32),
    contact_name VARCHAR(32) NOT NULL,
    contact_phone VARCHAR(64) NOT NULL,
    contact_email VARCHAR(128),
    status TINYINT NOT NULL DEFAULT 0,
    level TINYINT DEFAULT 3,
    auth_status TINYINT DEFAULT 0,
    settle_cycle VARCHAR(16) DEFAULT 'T+1',
    risk_level VARCHAR(16) DEFAULT 'MEDIUM',
    daily_limit DECIMAL(15,2) DEFAULT 5000000.00,
    single_limit DECIMAL(15,2) DEFAULT 100000.00,
    fee_rate DECIMAL(8,6) DEFAULT 0.006000,
    remark VARCHAR(512),
    audit_remark VARCHAR(512),
    audit_time TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_merchant_name ON merchant_info(merchant_name);
CREATE INDEX IF NOT EXISTS idx_merchant_status ON merchant_info(status);
CREATE INDEX IF NOT EXISTS idx_merchant_created ON merchant_info(created_at);

-- 支付通道配置表
CREATE TABLE IF NOT EXISTS channel_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    channel_code VARCHAR(32) NOT NULL UNIQUE,
    channel_name VARCHAR(64) NOT NULL,
    channel_type VARCHAR(16) NOT NULL DEFAULT '1',
    pay_types VARCHAR(256) NOT NULL,
    app_id VARCHAR(128),
    mch_id VARCHAR(64),
    api_key CLOB,
    cert_path VARCHAR(256),
    status TINYINT NOT NULL DEFAULT 1,
    priority INT DEFAULT 100,
    weight INT DEFAULT 100,
    avg_success_rate DECIMAL(5,2) DEFAULT 99.50,
    avg_latency BIGINT DEFAULT 200,
    current_qps INT DEFAULT 0,
    daily_amount DECIMAL(18,2) DEFAULT 0.00,
    daily_count BIGINT DEFAULT 0,
    config CLOB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0
);

-- 交易订单表
CREATE TABLE IF NOT EXISTS trade_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(32) NOT NULL UNIQUE,
    merchant_order_no VARCHAR(64) NOT NULL,
    merchant_id BIGINT NOT NULL,
    merchant_no VARCHAR(32) NOT NULL,
    channel_id BIGINT,
    channel_code VARCHAR(32),
    channel_order_no VARCHAR(64),
    pay_type VARCHAR(20) NOT NULL,
    subject VARCHAR(256) NOT NULL,
    body CLOB,
    amount DECIMAL(15,2) NOT NULL,
    actual_amount DECIMAL(15,2),
    fee DECIMAL(15,2),
    fee_rate DECIMAL(8,6),
    currency CHAR(3) DEFAULT 'CNY',
    status TINYINT NOT NULL DEFAULT 0,
    client_ip VARCHAR(45) NOT NULL,
    notify_url VARCHAR(256),
    return_url VARCHAR(256),
    expire_time TIMESTAMP NOT NULL,
    paid_at TIMESTAMP,
    channel_response CLOB,
    extra CLOB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_trade_merchant_order ON trade_order(merchant_id, merchant_order_no);
CREATE INDEX IF NOT EXISTS idx_trade_status_created ON trade_order(status, created_at);

-- 退款订单表
CREATE TABLE IF NOT EXISTS refund_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    refund_no VARCHAR(32) NOT NULL UNIQUE,
    order_no VARCHAR(32) NOT NULL,
    merchant_id BIGINT NOT NULL,
    merchant_no VARCHAR(32) NOT NULL,
    channel_code VARCHAR(32),
    channel_refund_no VARCHAR(64),
    refund_amount DECIMAL(15,2) NOT NULL,
    refund_fee DECIMAL(15,2),
    refund_reason VARCHAR(256) NOT NULL,
    status TINYINT NOT NULL DEFAULT 0,
    channel_response CLOB,
    refunded_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_refund_order_no ON refund_order(order_no);
CREATE INDEX IF NOT EXISTS idx_refund_status_created ON refund_order(status, created_at);

-- 风控规则表
CREATE TABLE IF NOT EXISTS risk_rule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rule_code VARCHAR(64) NOT NULL UNIQUE,
    rule_name VARCHAR(128) NOT NULL,
    category VARCHAR(32) NOT NULL,
    condition_expr CLOB NOT NULL,
    action VARCHAR(20) NOT NULL,
    risk_level VARCHAR(10) NOT NULL DEFAULT 'MEDIUM',
    priority INT DEFAULT 100,
    status TINYINT NOT NULL DEFAULT 1,
    description VARCHAR(512),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0
);

-- 风险事件表
CREATE TABLE IF NOT EXISTS risk_event (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_no VARCHAR(32) NOT NULL UNIQUE,
    category VARCHAR(32) NOT NULL,
    risk_level VARCHAR(10) NOT NULL,
    order_no VARCHAR(32),
    merchant_no VARCHAR(32),
    trigger_rule VARCHAR(128),
    event_detail CLOB,
    status TINYINT NOT NULL DEFAULT 0,
    handle_note VARCHAR(512),
    triggered_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    handled_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_risk_status ON risk_event(status);

-- 对账任务表
CREATE TABLE IF NOT EXISTS recon_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_no VARCHAR(32) NOT NULL UNIQUE,
    task_date DATE NOT NULL,
    channel_code VARCHAR(32) NOT NULL,
    channel_count INT DEFAULT 0,
    channel_amount DECIMAL(18,2) DEFAULT 0.00,
    platform_count INT DEFAULT 0,
    platform_amount DECIMAL(18,2) DEFAULT 0.00,
    diff_count INT DEFAULT 0,
    diff_amount DECIMAL(18,2) DEFAULT 0.00,
    success_count INT DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 0,
    error_msg VARCHAR(1024),
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0
);

-- 系统配置表
CREATE TABLE IF NOT EXISTS sys_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    config_key VARCHAR(64) NOT NULL UNIQUE,
    config_value CLOB NOT NULL,
    config_name VARCHAR(128) NOT NULL,
    config_desc VARCHAR(512),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
