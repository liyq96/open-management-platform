package com.openplatform.common.web.support;

import com.openplatform.common.core.constant.CommonConstants;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 请求编号读取工具。
 */
public final class RequestIdSupport {

    private RequestIdSupport() {
    }

    public static String getRequestId(HttpServletRequest request) {
        Object requestId = request.getAttribute(CommonConstants.REQUEST_ID_ATTRIBUTE);
        return requestId == null ? null : requestId.toString();
    }
}
