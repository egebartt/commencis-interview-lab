package com.commencis.interview.frameworktest;

import com.commencis.interview.core.report.AllureAttachments;
import com.commencis.interview.core.security.SensitiveHeaders;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Rapor ekinin gizli deger sizdirmadigini ve gercek adresi gosterdigini dogrular. */
@Tag("unit")
@DisplayName("Allure request attachment")
class AllureAttachmentsTest {

    @Test
    @DisplayName("Gercek adres query parametreleriyle birlikte yazilir")
    void writesResolvedUriWithQuery() {
        String text = AllureAttachments.requestText(
                "GET", "https://api.example.com/posts/7/comments?postId=1", Map.of(), null);

        assertTrue(text.startsWith("GET https://api.example.com/posts/7/comments?postId=1"), text);
    }

    @Test
    @DisplayName("Gizli header'lar buyuk/kucuk harften bagimsiz maskelenir")
    void masksSecretHeadersRegardlessOfCase() {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("authorization", "Bearer super-secret");
        headers.put("X-Api-Key", "key-12345");

        String text = AllureAttachments.requestText("POST", "https://api.example.com/posts", headers, null);

        assertTrue(text.contains("Content-Type: application/json"), text);
        assertTrue(text.contains("authorization: " + SensitiveHeaders.MASK), text);
        assertFalse(text.contains("super-secret"), text);
        assertFalse(text.contains("key-12345"), text);
    }

    @Test
    @DisplayName("URL'deki kullanici bilgisi rapora yazilmaz")
    void redactsUserInfoInUri() {
        String text = AllureAttachments.requestText(
                "GET", "https://user:hunter2@api.example.com/posts/1", Map.of(), null);

        assertFalse(text.contains("hunter2"), text);
        assertTrue(text.contains("***@api.example.com/posts/1"), text);
    }

    @Test
    @DisplayName("Govde varsa eke yazilir, bos govde satir acmaz")
    void writesBodyOnlyWhenPresent() {
        String withBody = AllureAttachments.requestText(
                "POST", "https://api.example.com/posts", Map.of(), "{\"title\":\"x\"}");
        String withoutBody = AllureAttachments.requestText(
                "GET", "https://api.example.com/posts/1", Map.of(), "");

        assertTrue(withBody.contains("\"title\": \"x\""), withBody);
        assertTrue(withoutBody.trim().endsWith("/posts/1"), withoutBody);
    }

    @Test
    @DisplayName("Gizli query parametresi eke maskelenmis yazilir")
    void masksSecretQueryParameterInAttachment() {
        String text = AllureAttachments.requestText(
                "GET", "https://api.example.com/posts?access_token=abc123&postId=1", Map.of(), null);

        assertFalse(text.contains("abc123"), text);
        assertTrue(text.contains("access_token=***"), text);
        assertTrue(text.contains("postId=1"), text);
    }

    @Test
    @DisplayName("Ic ice JSON alanlari maskelenir")
    void masksNestedJsonFields() {
        String body = """
                { "user": { "name": "bartu", "password": "hunter2",
                            "credentials": { "clientSecret": "cs-9" } } }
                """;

        String text = AllureAttachments.bodyText(body);

        assertFalse(text.contains("hunter2"), text);
        assertFalse(text.contains("cs-9"), text);
        assertTrue(text.contains("\"password\": \"***\""), text);
        assertTrue(text.contains("\"clientSecret\": \"***\""), text);
        assertTrue(text.contains("bartu"), "gizli olmayan alan gorunmeli: " + text);
    }

    @Test
    @DisplayName("Dizi icindeki gizli alanlar maskelenir")
    void masksSecretFieldsInsideArrays() {
        String body = """
                { "sessions": [ { "id": 1, "access_token": "at-1" },
                                { "id": 2, "access_token": "at-2" } ] }
                """;

        String text = AllureAttachments.bodyText(body);

        assertFalse(text.contains("at-1"), text);
        assertFalse(text.contains("at-2"), text);
        assertTrue(text.contains("\"id\": 1"), text);
    }

    @Test
    @DisplayName("Map govde gecerli JSON olarak yazilir")
    void writesMapBodyAsValidJson() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("title", "Map body");
        body.put("client_secret", "cs-42");

        String text = AllureAttachments.bodyText(body);

        assertFalse(text.contains("cs-42"), text);
        assertTrue(text.contains("\"title\": \"Map body\""), text);
        assertTrue(text.contains("\"client_secret\": \"***\""), text);
        assertFalse(text.contains("title=Map body"), "String.valueOf ciktisi olmamali: " + text);
    }

    @Test
    @DisplayName("JSON olmayan govde eski davranisla yazilir")
    void keepsNonJsonBodyAsIs() {
        assertTrue(AllureAttachments.bodyText("plain text body").contains("plain text body"));
    }

    @Test
    @DisplayName("byte[] govde icerigi rapora yazilmaz")
    void doesNotWriteBinaryBodyContent() {
        byte[] body = "CANARY_BINARY_SECRET".getBytes(StandardCharsets.UTF_8);

        String text = AllureAttachments.bodyText(body);

        assertFalse(text.contains("CANARY_BINARY_SECRET"), text);
        assertTrue(text.contains("binary govde"), text);
        assertEquals("CANARY_BINARY_SECRET", new String(body, StandardCharsets.UTF_8),
                "gercek govde degistirilmemeli");
    }

    @Test
    @DisplayName("Stream govde rapor icin tuketilmez")
    void doesNotConsumeStreamBody() throws IOException {
        InputStream stream = new ByteArrayInputStream("secret payload".getBytes(StandardCharsets.UTF_8));

        String text = AllureAttachments.bodyText(stream);

        assertFalse(text.contains("secret payload"), text);
        assertEquals("secret payload",
                new String(stream.readAllBytes(), StandardCharsets.UTF_8), "stream okunmus olmamali");
    }
}
