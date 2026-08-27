package com.openplatform.common.web.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openplatform.common.core.constant.CommonConstants;
import com.openplatform.common.core.security.InternalAccessSigner;
import com.openplatform.common.web.config.InternalAccessProperties;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class InternalAccessFilterTest {

    private static final String SECRET = "0123456789abcdef0123456789abcdef";
    private static final long NOW = 1_700_000_000_000L;

    private InternalAccessFilter filter;

    @BeforeEach
    void setUp() {
        InternalAccessProperties properties = new InternalAccessProperties();
        properties.setEnabled(true);
        properties.setSecret(SECRET);
        properties.setMaxClockSkew(Duration.ofSeconds(30));
        filter = new InternalAccessFilter(properties, new ObjectMapper(),
                Clock.fixed(Instant.ofEpochMilli(NOW), ZoneOffset.UTC));
    }

    @Test
    void shouldAllowGatewaySignedRequest() throws Exception {
        MockHttpServletRequest request = signedRequest(NOW);
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean invoked = new AtomicBoolean();

        filter.doFilter(request, response, (ignoredRequest, ignoredResponse) -> invoked.set(true));

        assertEquals(true, invoked.get());
    }

    @Test
    void shouldRejectDirectAndExpiredRequests() throws Exception {
        MockHttpServletResponse directResponse = new MockHttpServletResponse();
        filter.doFilter(new MockHttpServletRequest(), directResponse,
                (ignoredRequest, ignoredResponse) -> { });
        assertEquals(403, directResponse.getStatus());

        MockHttpServletResponse expiredResponse = new MockHttpServletResponse();
        filter.doFilter(signedRequest(NOW - 31_000L), expiredResponse,
                (ignoredRequest, ignoredResponse) -> { });
        assertEquals(403, expiredResponse.getStatus());
    }

    private MockHttpServletRequest signedRequest(long timestamp) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(CommonConstants.REQUEST_ID_HEADER, "request-1");
        request.addHeader(CommonConstants.GATEWAY_TIMESTAMP_HEADER, Long.toString(timestamp));
        request.addHeader(CommonConstants.GATEWAY_SIGNATURE_HEADER,
                InternalAccessSigner.sign(SECRET, timestamp, "request-1"));
        return request;
    }
}
