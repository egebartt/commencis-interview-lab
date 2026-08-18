package com.commencis.interview.frameworktest;

import com.commencis.interview.core.Redaction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Rapora giden metinlerde secret sizmasini engelleyen tek katman.
 *
 * <p>Bu testler "canary" niteligindedir: bir sizinti gozle fark edilmez, rapor yayinlandiktan
 * sonra ortaya cikar.
 */
@DisplayName("Redaction")
class RedactionTest {

    private static final String SECRET = "super-secret-value";

    @Test
    @DisplayName("Isim karsilastirmasi yazim bicimine takilmaz")
    void normalizesSecretNames() {
        assertTrue(Redaction.isSecret("Authorization"));
        assertTrue(Redaction.isSecret("access_token"));
        assertTrue(Redaction.isSecret("accessToken"));
        assertTrue(Redaction.isSecret("Access-Token"));
        assertTrue(Redaction.isSecret("X-Api-Key"));
        assertTrue(Redaction.isSecret("Client-Secret"));
        assertTrue(Redaction.isSecret("password"));

        assertFalse(Redaction.isSecret("X-Request-Id"));
        assertFalse(Redaction.isSecret("Content-Type"));
        assertFalse(Redaction.isSecret("userId"));
    }

    @Test
    @DisplayName("Header degeri maskelenir, digerleri korunur")
    void masksHeaderValues() {
        assertEquals(Redaction.MASK, Redaction.maskHeader("Authorization", "Bearer " + SECRET));
        assertEquals("interview-1", Redaction.maskHeader("X-Request-Id", "interview-1"));
    }

    @Test
    @DisplayName("URL'deki kullanici bilgisi maskelenir")
    void masksUrlUserInfo() {
        String masked = Redaction.maskUrl("https://bartu:" + SECRET + "@appium.example.com/wd/hub");

        assertFalse(masked.contains(SECRET), masked);
        assertTrue(masked.startsWith("https://" + Redaction.MASK + "@"), masked);
    }

    @Test
    @DisplayName("Gizli query parametresinin degeri maskelenir, normal parametre kalir")
    void masksSecretQueryParameters() {
        String masked = Redaction.maskUrl("https://api.example.com/v1/posts?postId=1&api_key=" + SECRET);

        assertFalse(masked.contains(SECRET), masked);
        assertTrue(masked.contains("postId=1"), masked);
        assertTrue(masked.contains("api_key=" + Redaction.MASK), masked);
    }

    @Test
    @DisplayName("JSON govdedeki gizli alanlar ic ice yapida da maskelenir")
    void masksNestedJsonFields() {
        String body = """
                {
                  "userId": 7,
                  "password": "%s",
                  "sessions": [ { "token": "%s" } ]
                }
                """.formatted(SECRET, SECRET);

        String masked = Redaction.maskJson(body);

        assertFalse(masked.contains(SECRET), masked);
        assertTrue(masked.contains("\"userId\""), masked);
    }

    @Test
    @DisplayName("Gizli adli nesnenin tamami maskelenir, icine inilmez")
    void masksWholeSecretObject() {
        // "value" gizli bir isim degil: icine inilseydi acikta kalirdi.
        String masked = Redaction.maskJson(
                "{ \"auth\": { \"scope\": \"read\", \"value\": \"" + SECRET + "\" } }");

        assertFalse(masked.contains(SECRET), masked);
        assertTrue(masked.contains("\"auth\": \"" + Redaction.MASK + "\""), masked);
    }

    @Test
    @DisplayName("Map govde toString ile degil JSON'a cevrilerek maskelenir")
    void masksMapBody() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("userId", 7);
        body.put("password", SECRET);

        String masked = Redaction.maskBody(body);

        assertFalse(masked.contains(SECRET), masked);
        assertTrue(masked.contains("\"userId\""), masked);
    }

    @Test
    @DisplayName("POJO govde de JSON'a cevrilerek maskelenir")
    void masksPojoBody() {
        String masked = Redaction.maskBody(new LoginRequest("bartu", SECRET));

        assertFalse(masked.contains(SECRET), masked);
        assertTrue(masked.contains("bartu"), masked);
    }

    @Test
    @DisplayName("URL-encode edilmis query anahtari da taninir")
    void masksEncodedQueryKey() {
        String masked = Redaction.maskUrl("https://api.example.com/v1?access%5Ftoken=" + SECRET + "&page=2");

        assertFalse(masked.contains(SECRET), masked);
        assertTrue(masked.contains("page=2"), masked);
    }

    @Test
    @DisplayName("JSON olmayan form govdesi de maskelenir")
    void masksFormEncodedBody() {
        String masked = Redaction.maskJson("grant_type=password&client_secret=" + SECRET);

        assertFalse(masked.contains(SECRET), masked);
        assertTrue(masked.contains("grant_type=password"), masked);
    }

    @Test
    @DisplayName("Binary ve stream govdelerin icerigi rapora yazilmaz")
    void neverWritesBinaryBodies() {
        assertEquals("<binary body, 3 bytes>", Redaction.maskBody(new byte[] {1, 2, 3}));
        assertTrue(Redaction.maskBody(new ByteArrayInputStream(SECRET.getBytes())).startsWith("<stream body"));
        assertFalse(Redaction.maskBody(new ByteArrayInputStream(SECRET.getBytes())).contains(SECRET));
    }

    /** Serialize edilebilen basit bir istek nesnesi. */
    private record LoginRequest(String username, String password) {
    }
}
