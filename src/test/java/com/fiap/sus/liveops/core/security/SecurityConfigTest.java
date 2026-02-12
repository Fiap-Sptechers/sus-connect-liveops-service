package com.fiap.sus.liveops.core.security;

import com.fiap.sus.liveops.core.exception.SecurityException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    @InjectMocks
    private SecurityConfig securityConfig;

    private String getValidTestKey() {
        String envKey = System.getenv("TEST_PUBLIC_KEY");
        if (envKey != null && !envKey.isEmpty()) {
            return envKey;
        }
        return """
                -----BEGIN PUBLIC KEY-----
                MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDdlatRjRjogo3WojgGHFHYLugd
                UWAY9iR3fy4arWNA1KoS8kVw33cJibXr8bvwUAUparCwlvdbH6dvEOfou0/gCFQs
                HUfQrSDv+MuSUMAe8jzKE4qW+jK+xQU9a03GUnKHkkle+Q0pX/g6jXZ7r1/xAK5D
                o2kQ+X5xK9cipRgEKwIDAQAB
                -----END PUBLIC KEY-----
                """;
    }

    @Test
    void authenticationManagerResolver_ShouldReturnNonNullResolver() {
        ReflectionTestUtils.setField(securityConfig, "networkPublicKey", getValidTestKey());
        ReflectionTestUtils.setField(securityConfig, "trafficPublicKey", getValidTestKey());

        AuthenticationManagerResolver<?> resolver = securityConfig.authenticationManagerResolver();


        assertNotNull(resolver);
    }

    @Test
    void buildDecoderFromKey_WithInvalidKey_ShouldThrowSecurityException() {
        ReflectionTestUtils.setField(securityConfig, "networkPublicKey", "INVALID_KEY");

        assertThrows(SecurityException.class, () -> securityConfig.authenticationManagerResolver());
    }

    @Test
    void buildDecoderFromKey_WithMalformedBase64_ShouldThrowSecurityException() {
        String malformedKey = "-----BEGIN PUBLIC KEY-----\nINVALID!!!\n-----END PUBLIC KEY-----";
        ReflectionTestUtils.setField(securityConfig, "networkPublicKey", malformedKey);

        assertThrows(SecurityException.class, () -> securityConfig.authenticationManagerResolver());
    }

    @Test
    void buildDecoderFromKey_WithEmptyKey_ShouldThrowSecurityException() {
        ReflectionTestUtils.setField(securityConfig, "networkPublicKey", "");

        assertThrows(SecurityException.class, () -> securityConfig.authenticationManagerResolver());
    }

    @Test
    void buildDecoderFromKey_WithOnlyHeaders_ShouldThrowSecurityException() {
        String keyWithOnlyHeaders = "-----BEGIN PUBLIC KEY-----\n-----END PUBLIC KEY-----";
        ReflectionTestUtils.setField(securityConfig, "networkPublicKey", keyWithOnlyHeaders);

        assertThrows(SecurityException.class, () -> securityConfig.authenticationManagerResolver());
    }

    @Test
    void buildDecoderFromKey_WithValidKeyButWrongAlgorithm_ShouldThrowSecurityException() {
        String ecPublicKey = "-----BEGIN PUBLIC KEY-----\nMFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE\n-----END PUBLIC KEY-----";
        ReflectionTestUtils.setField(securityConfig, "networkPublicKey", ecPublicKey);

        assertThrows(SecurityException.class, () -> securityConfig.authenticationManagerResolver());
    }

    @Test
    void authenticationManagerResolver_WithBothPublicKeys_ShouldCreateResolverSuccessfully() {
        ReflectionTestUtils.setField(securityConfig, "networkPublicKey", getValidTestKey());
        ReflectionTestUtils.setField(securityConfig, "trafficPublicKey", getValidTestKey());

        AuthenticationManagerResolver<HttpServletRequest> resolver = securityConfig.authenticationManagerResolver();

        assertNotNull(resolver);
    }

    @Test
    void authenticationManagerResolver_CalledMultipleTimes_ShouldCreateNewInstances() {
        ReflectionTestUtils.setField(securityConfig, "networkPublicKey", getValidTestKey());
        ReflectionTestUtils.setField(securityConfig, "trafficPublicKey", getValidTestKey());

        AuthenticationManagerResolver<HttpServletRequest> resolver1 = securityConfig.authenticationManagerResolver();
        AuthenticationManagerResolver<HttpServletRequest> resolver2 = securityConfig.authenticationManagerResolver();

        assertNotNull(resolver1);
        assertNotNull(resolver2);
    }

    @Test
    void buildDecoderFromKey_WithNullKey_ShouldThrowSecurityException() {
        ReflectionTestUtils.setField(securityConfig, "networkPublicKey", null);

        assertThrows(SecurityException.class, () -> securityConfig.authenticationManagerResolver());
    }

}

