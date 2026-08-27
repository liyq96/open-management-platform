package com.openplatform.common.core.constant;

/**
 * 平台通用常量。
 */
public final class CommonConstants {

    public static final String SUCCESS_CODE = "200";

    public static final String REQUEST_ID_HEADER = "X-Request-Id";

    public static final String REQUEST_ID_ATTRIBUTE = CommonConstants.class.getName() + ".REQUEST_ID";

    public static final String REQUEST_ID_MDC_KEY = "requestId";

    public static final String GATEWAY_TIMESTAMP_HEADER = "X-Platform-Gateway-Timestamp";

    public static final String GATEWAY_SIGNATURE_HEADER = "X-Platform-Gateway-Signature";

    private CommonConstants() {
    }
}
