package com.openplatform.common.security.support;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * 加载 PKCS#8 私钥和 X.509 公钥格式的 RSA PEM 内容。
 */
public final class RsaPemKeyLoader {

    private static final String PRIVATE_KEY_BEGIN = "-----BEGIN PRIVATE KEY-----";

    private static final String PRIVATE_KEY_END = "-----END PRIVATE KEY-----";

    private static final String PUBLIC_KEY_BEGIN = "-----BEGIN PUBLIC KEY-----";

    private static final String PUBLIC_KEY_END = "-----END PUBLIC KEY-----";

    private RsaPemKeyLoader() {
    }

    public static RSAPrivateKey loadPrivateKey(String pemContent) {
        byte[] encodedKey = decodePem(pemContent, PRIVATE_KEY_BEGIN, PRIVATE_KEY_END);
        try {
            return (RSAPrivateKey) keyFactory().generatePrivate(new PKCS8EncodedKeySpec(encodedKey));
        } catch (InvalidKeySpecException exception) {
            throw new IllegalArgumentException("Invalid RSA private key", exception);
        }
    }

    public static RSAPublicKey loadPublicKey(String pemContent) {
        byte[] encodedKey = decodePem(pemContent, PUBLIC_KEY_BEGIN, PUBLIC_KEY_END);
        try {
            return (RSAPublicKey) keyFactory().generatePublic(new X509EncodedKeySpec(encodedKey));
        } catch (InvalidKeySpecException exception) {
            throw new IllegalArgumentException("Invalid RSA public key", exception);
        }
    }

    private static byte[] decodePem(String pemContent, String beginMarker, String endMarker) {
        if (pemContent == null || !pemContent.contains(beginMarker) || !pemContent.contains(endMarker)) {
            throw new IllegalArgumentException("Invalid PEM content");
        }
        String base64Content = pemContent
                .replace(beginMarker, "")
                .replace(endMarker, "")
                .replaceAll("\\s", "");
        try {
            return Base64.getDecoder().decode(base64Content);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Invalid PEM base64 content", exception);
        }
    }

    private static KeyFactory keyFactory() {
        try {
            return KeyFactory.getInstance("RSA");
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("RSA algorithm is unavailable", exception);
        }
    }
}
