package com.openplatform.common.security.support;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import org.junit.jupiter.api.Test;

class RsaPemKeyLoaderTest {

    @Test
    void shouldLoadPkcs8PrivateKeyAndX509PublicKey() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair keyPair = generator.generateKeyPair();
        String privateKeyPem = toPem("PRIVATE KEY", keyPair.getPrivate().getEncoded());
        String publicKeyPem = toPem("PUBLIC KEY", keyPair.getPublic().getEncoded());

        RSAPrivateKey privateKey = RsaPemKeyLoader.loadPrivateKey(privateKeyPem);
        RSAPublicKey publicKey = RsaPemKeyLoader.loadPublicKey(publicKeyPem);

        assertArrayEquals(keyPair.getPrivate().getEncoded(), privateKey.getEncoded());
        assertArrayEquals(keyPair.getPublic().getEncoded(), publicKey.getEncoded());
    }

    private String toPem(String type, byte[] encodedKey) {
        return "-----BEGIN " + type + "-----\n"
                + Base64.getMimeEncoder(64, new byte[]{'\n'}).encodeToString(encodedKey)
                + "\n-----END " + type + "-----";
    }
}
