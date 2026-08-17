package com.commencis.interview.frameworktest;

import com.commencis.interview.core.context.ApiContext;
import com.sun.net.httpserver.HttpServer;
import io.restassured.http.Method;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Senaryo icindeki istek durumunun omrunu sabitler: header'lar tasinir, govde/query/path tasinmaz.
 * Loopback uzerinde JDK'nin kendi HTTP sunucusu kullanilir; dis servise cikilmaz.
 */
@Tag("unit")
@DisplayName("ApiContext request state")
class ApiContextTest {

    private record Received(String path, String query, String body, String trace) {
    }

    private final List<Received> received = new ArrayList<>();

    private HttpServer server;
    private ApiContext api;

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        server.createContext("/", exchange -> {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            received.add(new Received(
                    exchange.getRequestURI().getPath(),
                    exchange.getRequestURI().getQuery(),
                    body,
                    exchange.getRequestHeaders().getFirst("X-Trace")));
            byte[] payload = "{\"ok\":true}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, payload.length);
            try (OutputStream stream = exchange.getResponseBody()) {
                stream.write(payload);
            }
        });
        server.start();

        api = new ApiContext();
        api.baseUrl("http://" + server.getAddress().getHostString() + ":" + server.getAddress().getPort());
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    @Test
    @DisplayName("Govde, query ve path parametreleri sonraki istege tasinmaz")
    void perRequestStateIsNotCarriedOver() {
        api.putHeaders(Map.of("X-Trace", "commencis"));
        api.putQueryParams(Map.of("postId", "1"));
        api.putPathParams(Map.of("id", "7"));
        api.body("{\"title\":\"first\"}");

        api.send(Method.POST, "/posts/{id}");
        api.send(Method.GET, "/comments");

        assertEquals(2, received.size());

        Received first = received.get(0);
        assertEquals("/posts/7", first.path(), "path parametresi yerlesmeli");
        assertEquals("postId=1", first.query());
        assertTrue(first.body().contains("first"), "ilk istegin govdesi gitmeli");

        Received second = received.get(1);
        assertEquals("/comments", second.path());
        assertNull(second.query(), "query parametresi ikinci istege tasinmamali");
        assertEquals("", second.body(), "govde ikinci istege tasinmamali");
    }

    @Test
    @DisplayName("Header'lar senaryo boyunca korunur")
    void headersSurviveAcrossRequests() {
        api.putHeaders(Map.of("X-Trace", "commencis"));

        api.send(Method.GET, "/posts/1");
        api.send(Method.GET, "/posts/2");

        assertEquals(2, received.size());
        assertEquals("commencis", received.get(0).trace());
        assertEquals("commencis", received.get(1).trace(), "header senaryo boyunca kalmali");
    }

    @Test
    @DisplayName("Istek gonderilmeden yanit istenirse ne yapilacagi soylenir")
    void responseBeforeAnyRequestIsExplained() {
        IllegalStateException error = org.junit.jupiter.api.Assertions
                .assertThrows(IllegalStateException.class, () -> new ApiContext().response());

        assertTrue(error.getMessage().contains("When"), error.getMessage());
    }
}
