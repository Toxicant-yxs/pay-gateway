package com.paygateway.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MerchantStatusEnum {

    PENDING_AUDIT(0, "待审核"),
    NORMAL(1, "正常"),
    FROZEN(2, "冻结"),
    DISABLED(3, "禁用"),
    REJECTED(4, "驳回");

    private final Integer code;
    private final String desc;

    public static MerchantStatusEnum getByCode(Integer code) {
        for (MerchantStatusEnum e : values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }
}
