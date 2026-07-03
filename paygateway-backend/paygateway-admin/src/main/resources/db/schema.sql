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

CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_code VARCHAR(32) NOT NULL UNIQUE,
    role_name VARCHAR(64) NOT NULL,
    description VARCHAR(256),
    data_scope TINYINT NOT NULL DEFAULT 1,
    status TINYINT NOT NULL DEFAULT 1,
    sort_order INT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS sys_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_id BIGINT DEFAULT 0,
    permission_code VARCHAR(64) NOT NULL UNIQUE,
    permission_name VARCHAR(64) NOT NULL,
    permission_type TINYINT NOT NULL DEFAULT 1,
    path VARCHAR(256),
    component VARCHAR(256),
    icon VARCHAR(64),
    sort_order INT DEFAULT 0,
    visible TINYINT NOT NULL DEFAULT 1,
    status TINYINT NOT NULL DEFAULT 1,
    remark VARCHAR(256),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_permission_parent ON sys_permission(parent_id);

CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_role (user_id, role_id)
);

CREATE TABLE IF NOT EXISTS sys_role_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_role_permission (role_id, permission_id)
);

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

CREATE TABLE IF NOT EXISTS channel_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    channel_code VARCHAR(32) NOT NULL UNIQUE,
    channel_name VARCHAR(64) NOT NULL,
    channel_type VARCHAR(16) NOT NULL DEFAULT '1',
    pay_types VARCHAR(256) NOT NULL,
    app_id VARCHAR(128),
    mch_id VARCHAR(64),
    api_key CLOB,
    api_url VARCHAR(256),
    cert_path VARCHAR(256),
    fee_rate DECIMAL(8,6) DEFAULT 0.006000,
    status TINYINT NOT NULL DEFAULT 1,
    priority INT DEFAULT 100,
    weight INT DEFAULT 100,
    avg_success_rate DECIMAL(5,2) DEFAULT 99.50,
    avg_latency BIGINT DEFAULT 200,
    current_qps INT DEFAULT 0,
    daily_amount DECIMAL(18,2) DEFAULT 0.00,
    daily_count BIGINT DEFAULT 0,
    config CLOB,
    remark VARCHAR(512),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS channel_route (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    route_no VARCHAR(32) NOT NULL UNIQUE,
    route_name VARCHAR(128) NOT NULL,
    channel_id BIGINT NOT NULL,
    channel_code VARCHAR(32) NOT NULL,
    pay_type VARCHAR(32),
    min_amount DECIMAL(15,2) DEFAULT 0.01,
    max_amount DECIMAL(15,2) DEFAULT 500000.00,
    priority INT DEFAULT 100,
    status TINYINT NOT NULL DEFAULT 1,
    time_start VARCHAR(8),
    time_end VARCHAR(8),
    remark VARCHAR(512),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0
);

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

CREATE TABLE IF NOT EXISTS risk_blacklist (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(32) NOT NULL,
    value VARCHAR(256) NOT NULL,
    reason VARCHAR(512),
    status TINYINT NOT NULL DEFAULT 1,
    expire_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_blacklist_type ON risk_blacklist(type, status);

CREATE TABLE IF NOT EXISTS recon_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_no VARCHAR(32) NOT NULL UNIQUE,
    channel_code VARCHAR(32) NOT NULL,
    recon_date DATE NOT NULL,
    status TINYINT NOT NULL DEFAULT 0,
    total_count INT DEFAULT 0,
    total_amount DECIMAL(18,2) DEFAULT 0.00,
    match_count INT DEFAULT 0,
    diff_count INT DEFAULT 0,
    channel_total_count INT DEFAULT 0,
    channel_total_amount DECIMAL(18,2) DEFAULT 0.00,
    bill_file VARCHAR(256),
    started_at TIMESTAMP,
    finished_at TIMESTAMP,
    error_msg VARCHAR(1024),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS recon_detail (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id BIGINT NOT NULL,
    order_no VARCHAR(32),
    diff_type VARCHAR(32),
    our_amount DECIMAL(15,2),
    channel_amount DECIMAL(15,2),
    our_status TINYINT,
    channel_status TINYINT,
    status TINYINT NOT NULL DEFAULT 0,
    handle_note VARCHAR(512),
    handled_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS system_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    config_key VARCHAR(64) NOT NULL UNIQUE,
    config_value CLOB NOT NULL,
    config_group VARCHAR(32) NOT NULL DEFAULT 'basic',
    description VARCHAR(256),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS system_notification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    notify_no VARCHAR(32) NOT NULL UNIQUE,
    title VARCHAR(256) NOT NULL,
    content CLOB,
    category VARCHAR(32) NOT NULL DEFAULT 'system',
    level VARCHAR(10) NOT NULL DEFAULT 'info',
    is_read TINYINT NOT NULL DEFAULT 0,
    related_id VARCHAR(64),
    related_type VARCHAR(32),
    read_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS operation_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    log_no VARCHAR(32) NOT NULL UNIQUE,
    user_id BIGINT,
    username VARCHAR(32),
    operation VARCHAR(256),
    module VARCHAR(32),
    method VARCHAR(16),
    request_url VARCHAR(256),
    request_params CLOB,
    ip VARCHAR(45),
    status TINYINT DEFAULT 1,
    error_msg VARCHAR(1024),
    cost_time BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
