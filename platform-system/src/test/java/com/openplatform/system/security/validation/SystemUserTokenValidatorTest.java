package com.openplatform.system.security.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.openplatform.common.security.constant.JwtClaimConstants;
import com.openplatform.system.security.service.UserPermissionService;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

class SystemUserTokenValidatorTest {

    private final UserPermissionService service = mock(UserPermissionService.class);
    private final SystemUserTokenValidator validator = new SystemUserTokenValidator(service);

    @Test
    void shouldAcceptCurrentAuthVersion() {
        when(service.isAuthVersionCurrent(1L, 10L, 3)).thenReturn(true);
        assertFalse(validator.validate(jwt(3)).hasErrors());
    }

    @Test
    void shouldRejectOutdatedAuthVersion() {
        when(service.isAuthVersionCurrent(1L, 10L, 3)).thenReturn(false);
        assertTrue(validator.validate(jwt(3)).hasErrors());
    }

    private Jwt jwt(int version) {
        Instant now = Instant.now();
        return new Jwt("token", now, now.plusSeconds(60), Map.of("alg", "RS256"), Map.of(
                JwtClaimConstants.USER_ID, 1L,
                JwtClaimConstants.TENANT_ID, 10L,
                JwtClaimConstants.AUTH_VERSION, version));
    }
}
