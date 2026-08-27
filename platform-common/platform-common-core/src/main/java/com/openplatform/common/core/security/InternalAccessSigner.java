package com.openplatform.common.core.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Gateway 到内部服务的请求签名工具。
 */
public final class InternalAccessSigner {

    public static final int MIN_SECRET_LENGTH = 32;

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private InternalAccessSigner() {
    }

    public static String sign(String secret, long timestamp, String requestId) {
        validateSecret(secret);
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            byte[] signature = mac.doFinal(payload(timestamp, requestId).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(signature);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("HmacSHA256 is not supported", exception);
        } catch (java.security.InvalidKeyException exception) {
            throw new IllegalArgumentException("Internal access secret is invalid", exception);
        }
    }

    public static boolean matches(String expected, String actual) {
        if (expected == null || actual == null) {
            return false;
        }
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.US_ASCII),
                actual.getBytes(StandardCharsets.US_ASCII));
    }

    public static void validateSecret(String secret) {
        if (secret == null || secret.length() < MIN_SECRET_LENGTH) {
            throw new IllegalStateException("platform.internal-access.secret must contain at least 32 characters");
        }
    }

    private static String payload(long timestamp, String requestId) {
        return timestamp + "\n" + requestId;
    }
}
