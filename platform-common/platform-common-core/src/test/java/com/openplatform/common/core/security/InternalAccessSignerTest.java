package com.openplatform.common.core.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class InternalAccessSignerTest {

    private static final String SECRET = "0123456789abcdef0123456789abcdef";

    @Test
    void shouldCreateStableSignatureAndCompareInConstantTime() {
        String signature = InternalAccessSigner.sign(SECRET, 1_700_000_000_000L, "request-1");

        assertTrue(InternalAccessSigner.matches(signature,
                InternalAccessSigner.sign(SECRET, 1_700_000_000_000L, "request-1")));
        assertFalse(InternalAccessSigner.matches(signature,
                InternalAccessSigner.sign(SECRET, 1_700_000_000_001L, "request-1")));
    }

    @Test
    void shouldRejectWeakSecret() {
        assertThrows(IllegalStateException.class,
                () -> InternalAccessSigner.sign("too-short", 1L, "request-1"));
    }
}
