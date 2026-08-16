package com.commencis.interview.test.api;

import com.commencis.interview.base.BaseApiTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Adresi, govdeyi ve header'i dogrudan testin icine yazan kullanim ornekleri.
 * Iki bicim de ayni client uzerinden calisir: full URL ve {@code api.base.url} + relative path.
 */
@Tag("api")
@DisplayName("Full URL API requests")
class ManualUrlApiTest extends BaseApiTest {

    @Test
    @DisplayName("Full URL ile GET istegi gonderilir")
    void getsFromFullUrl() {
        String url = "https://jsonplaceholder.typicode.com/posts/1";

        api.get(url)
                .then()
                .statusCode(200)
                .body("id", equalTo(1));
    }

    @Test
    @DisplayName("api.base.url tanimliysa relative path yeterlidir")
    void getsFromConfiguredBaseUrl() {
        // Adres config.properties icindeki api.base.url'den gelir; test yalnizca endpoint'i soyler.
        api.get("/posts/1")
                .then()
                .statusCode(200)
                .body("id", equalTo(1));
    }

    @Test
    @DisplayName("Inline body ve manuel header'larla POST gonderilir")
    void postsInlineBodyWithManualHeaders() {
        String url = "https://jsonplaceholder.typicode.com/posts";
        String body = """
                {
                  "userId": 7,
                  "title": "Commencis interview",
                  "body": "First request body"
                }
                """;
        String bearerToken = "interview-bearer-token";
        String clientKey = "interview-client-key";
        String secretKey = "interview-secret-key";
        Map<String, String> headers = Map.of(
                "Authorization", "Bearer " + bearerToken,
                "Client-Key", clientKey,
                "Secret-Key", secretKey);

        api.post(url, body, headers)
                .then()
                .statusCode(201)
                .body("title", equalTo("Commencis interview"))
                .body("id", notNullValue());
    }

    @Test
    @DisplayName("Body Map olarak da verilebilir, JSON string yazmak sart degil")
    void postsMapBodyWithoutWritingJson() {
        String url = "https://jsonplaceholder.typicode.com/posts";
        // Map (veya JSON'a serialize edilebilen bir POJO) cevrilerek gonderilir; hizli deneme icin pratiktir.
        Map<String, Object> body = Map.of(
                "userId", 7,
                "title", "Map body",
                "body", "Serialization is handled by Rest Assured");

        api.post(url, body)
                .then()
                .statusCode(201)
                .body("title", equalTo("Map body"));
    }

    @Test
    @DisplayName("Inline body ile PUT istegi gonderilir")
    void putsInlineBodyToFullUrl() {
        String url = "https://jsonplaceholder.typicode.com/posts/1";
        String body = """
                {
                  "id": 1,
                  "userId": 7,
                  "title": "Updated interview post",
                  "body": "Body is written directly in the test"
                }
                """;

        api.put(url, body)
                .then()
                .statusCode(200)
                .body("id", equalTo(1))
                .body("title", equalTo("Updated interview post"));
    }

    @Test
    @DisplayName("Inline body ile PATCH istegi gonderilir")
    void patchesInlineBodyToFullUrl() {
        String url = "https://jsonplaceholder.typicode.com/posts/1";
        String body = """
                {
                  "title": "Partially updated interview post"
                }
                """;

        api.patch(url, body)
                .then()
                .statusCode(200)
                .body("id", equalTo(1))
                .body("title", equalTo("Partially updated interview post"));
    }

    @Test
    @DisplayName("Full URL ve manuel header ile DELETE istegi gonderilir")
    void deletesFromFullUrlWithManualHeader() {
        String url = "https://jsonplaceholder.typicode.com/posts/1";
        Map<String, String> headers = Map.of("X-Request-Id", "interview-delete-1");

        api.delete(url, headers)
                .then()
                .statusCode(200);
    }
}
