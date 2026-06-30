package com.paygateway.common.result;

import lombok.Getter;

@Getter
public enum ResultCode {

    SUCCESS(0, "操作成功"),
    FAIL(500, "操作失败"),

    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未登录或Token已过期"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不允许"),

    USER_NOT_EXIST(1001, "用户不存在"),
    USER_PASSWORD_ERROR(1002, "密码错误"),
    USER_DISABLED(1003, "账号已被禁用"),
    USER_ALREADY_EXIST(1004, "用户已存在"),
    TOKEN_INVALID(1005, "Token无效"),
    TOKEN_EXPIRED(1006, "Token已过期"),

    MERCHANT_NOT_EXIST(2001, "商户不存在"),
    MERCHANT_STATUS_ERROR(2002, "商户状态异常"),
    MERCHANT_ALREADY_EXIST(2003, "商户号已存在"),
    MERCHANT_AUDIT_PENDING(2004, "商户审核中"),

    ORDER_NOT_EXIST(3001, "订单不存在"),
    ORDER_STATUS_ERROR(3002, "订单状态错误"),
    ORDER_AMOUNT_ERROR(3003, "订单金额错误"),
    ORDER_ALREADY_PAID(3004, "订单已支付"),
    ORDER_EXPIRED(3005, "订单已过期"),
    REFUND_AMOUNT_ERROR(3006, "退款金额超过可退金额"),

    CHANNEL_NOT_EXIST(4001, "支付通道不存在"),
    CHANNEL_DISABLED(4002, "支付通道已停用"),
    CHANNEL_ERROR(4003, "通道调用异常"),

    RISK_BLOCKED(5001, "风控拦截"),
    RISK_REVIEW(5002, "需要人工审核"),

    SYSTEM_ERROR(9999, "系统异常，请稍后重试");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
