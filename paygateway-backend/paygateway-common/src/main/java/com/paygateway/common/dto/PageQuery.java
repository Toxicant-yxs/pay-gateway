package com.paygateway.common.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class PageQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer page = 1;
    private Integer pageSize = 20;

    public Integer getPage() {
        return page == null || page < 1 ? 1 : page;
    }

    public Integer getPageSize() {
        if (pageSize == null || pageSize < 1) {
            return 20;
        }
        return Math.min(pageSize, 100);
    }

    public Long getOffset() {
        return (long) (getPage() - 1) * getPageSize();
    }
}
