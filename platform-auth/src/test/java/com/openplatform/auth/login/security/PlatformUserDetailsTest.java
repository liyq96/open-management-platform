package com.openplatform.auth.login.security;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;
import org.junit.jupiter.api.Test;

class PlatformUserDetailsTest {

    @Test
    void shouldExposeDatabasePermissionsAsAuthorities() {
        PlatformUserDetails details = new PlatformUserDetails(
                1L, 10L, 20L, 2, "admin", "hash", true,
                Set.of("system:user:list"));

        assertEquals("system:user:list", details.getAuthorities().iterator().next().getAuthority());
    }
}
