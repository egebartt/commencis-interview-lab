package com.commencis.interview.frameworktest;

import com.commencis.interview.core.security.SensitiveHeaders;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("unit")
@DisplayName("Sensitive header redaction")
class SensitiveHeadersTest {

    @Test
    @DisplayName("Header adi buyuk/kucuk harften bagimsiz taninir")
    void detectsSecretHeaderRegardlessOfCase() {
        assertTrue(SensitiveHeaders.isSecret("Authorization"));
        assertTrue(SensitiveHeaders.isSecret("authorization"));
        assertTrue(SensitiveHeaders.isSecret("AUTHORIZATION"));
        assertTrue(SensitiveHeaders.isSecret("  x-api-key  "));
        assertFalse(SensitiveHeaders.isSecret("Content-Type"));
        assertFalse(SensitiveHeaders.isSecret(null));
    }

    @Test
    @DisplayName("Yalnizca gizli header degerleri maskelenir")
    void redactsOnlySecretValues() {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("authorization", "Bearer super-secret");
        headers.put("X-Client-Secret", "shhh");

        Map<String, String> redacted = SensitiveHeaders.redact(headers);

        assertEquals("application/json", redacted.get("Content-Type"));
        assertEquals(SensitiveHeaders.MASK, redacted.get("authorization"));
        assertEquals(SensitiveHeaders.MASK, redacted.get("X-Client-Secret"));
        assertEquals("Bearer super-secret", headers.get("authorization"), "girdi degistirilmemeli");
    }

    @Test
    @DisplayName("URL'deki kullanici bilgisi temizlenir")
    void redactsUserInfoInUrl() {
        assertEquals("https://***@api.example.com/v1/posts",
                SensitiveHeaders.redactUrl("https://user:pass@api.example.com/v1/posts"));
    }

    @Test
    @DisplayName("Kullanici bilgisi olmayan URL degismez")
    void leavesPlainUrlUntouched() {
        assertEquals("https://api.example.com/v1/posts",
                SensitiveHeaders.redactUrl("https://api.example.com/v1/posts"));
        assertEquals("/posts/1", SensitiveHeaders.redactUrl("/posts/1"));
    }

    @Test
    @DisplayName("Yazim bicimi ne olursa olsun ayni ad taninir")
    void nameMatchingIgnoresSeparatorsAndCase() {
        assertTrue(SensitiveHeaders.isSecret("access_token"));
        assertTrue(SensitiveHeaders.isSecret("accessToken"));
        assertTrue(SensitiveHeaders.isSecret("Access-Token"));
        assertTrue(SensitiveHeaders.isSecret("CLIENT_SECRET"));
        assertTrue(SensitiveHeaders.isSecret("apiKey"));
        assertFalse(SensitiveHeaders.isSecret("postId"));
        assertFalse(SensitiveHeaders.isSecret("title"));
    }

    @Test
    @DisplayName("Gizli query parametresinin yalnizca degeri maskelenir")
    void masksSecretQueryParameterValue() {
        assertEquals("https://api.example.com/posts?access_token=***&postId=1",
                SensitiveHeaders.redactUrl("https://api.example.com/posts?access_token=abc123&postId=1"));
    }

    @Test
    @DisplayName("Sira ve encoding korunur, digerleri aynen kalir")
    void keepsOrderAndEncodingOfOtherParameters() {
        assertEquals("https://api.example.com/search?q=a%20b&api_key=***&page=2",
                SensitiveHeaders.redactUrl("https://api.example.com/search?q=a%20b&api_key=k1&page=2"));
    }

    @Test
    @DisplayName("Degersiz parametre ve kullanici bilgisi birlikte islenir")
    void handlesFlagParameterAndUserInfoTogether() {
        assertEquals("https://***@api.example.com/posts?debug&password=***",
                SensitiveHeaders.redactUrl("https://user:pw@api.example.com/posts?debug&password=hunter2"));
    }
}
