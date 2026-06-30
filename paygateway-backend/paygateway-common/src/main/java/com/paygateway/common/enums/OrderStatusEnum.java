package com.paygateway.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrderStatusEnum {

    WAIT_PAY(0, "待支付"),
    PAYING(1, "支付中"),
    PAY_SUCCESS(2, "支付成功"),
    PAY_FAIL(3, "支付失败"),
    CLOSED(4, "已关闭"),
    REFUNDED(5, "已退款"),
    PARTIAL_REFUND(6, "部分退款");

    private final Integer code;
    private final String desc;

    public static OrderStatusEnum getByCode(Integer code) {
        for (OrderStatusEnum e : values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }
}
