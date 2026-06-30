package com.paygateway.common.constants;

public interface CommonConstants {

    String REQUEST_ID_HEADER = "X-Request-Id";

    String TOKEN_HEADER = "Authorization";
    String TOKEN_PREFIX = "Bearer ";

    String DEFAULT_CHARSET = "UTF-8";

    Long DEFAULT_PAGE_NUM = 1L;
    Long DEFAULT_PAGE_SIZE = 20L;
    Long MAX_PAGE_SIZE = 100L;

    String DATE_FORMAT = "yyyy-MM-dd";
    String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    String TIME_FORMAT = "HH:mm:ss";

    Integer ENABLE = 1;
    Integer DISABLE = 0;

    Integer DELETED = 1;
    Integer NOT_DELETED = 0;
}
