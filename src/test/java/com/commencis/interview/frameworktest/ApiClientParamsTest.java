package com.commencis.interview.frameworktest;

import com.commencis.interview.api.ApiClient;
import com.commencis.interview.api.RequestSpecFactory;
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
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Query ve path parametrelerinin URL'e dogru gectigini dogrular.
 *
 * <p>Dis servise cikilmaz: loopback uzerinde JDK'nin kendi HTTP sunucusu acilir ve gelen
 * istegin yolu/query'si okunur. Boylece encoding davranisi ag kosullarindan bagimsiz sabitlenir.
 */
@Tag("unit")
@DisplayName("ApiClient query and path parameters")
class ApiClientParamsTest {

    private final AtomicReference<String> receivedPath = new AtomicReference<>();
    private final AtomicReference<String> receivedQuery = new AtomicReference<>();

    private HttpServer server;
    private ApiClient api;

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        server.createContext("/", exchange -> {
            receivedPath.set(exchange.getRequestURI().getPath());
            receivedQuery.set(exchange.getRequestURI().getQuery());
            byte[] body = "{\"ok\":true}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream stream = exchange.getResponseBody()) {
                stream.write(body);
            }
        });
        server.start();

        String baseUrl = "http://" + server.getAddress().getHostString() + ":" + server.getAddress().getPort();
        api = new ApiClient(RequestSpecFactory.create(), baseUrl);
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    @Test
    @DisplayName("Query parametreleri encode edilerek gonderilir")
    void queryParametersAreEncoded() {
        api.send(Method.GET, "/search", null, Map.of(), Map.of("q", "a b&c"), Map.of());

        assertEquals("/search", receivedPath.get());
        assertEquals("a b&c", decodeQueryValue(receivedQuery.get()));
    }

    @Test
    @DisplayName("Path parametreleri URL sablonuna yerlestirilir")
    void pathParametersAreSubstituted() {
        api.send(Method.GET, "/posts/{id}/comments", null, Map.of(), Map.of(), Map.of("id", 42));

        assertEquals("/posts/42/comments", receivedPath.get());
        assertNull(receivedQuery.get(), "path parametresi query'ye eklenmemeli");
    }

    @Test
    @DisplayName("POST govdesi query parametresine donusmez")
    void bodyIsNotTurnedIntoFormParameters() {
        api.send(Method.POST, "/posts", "{\"title\":\"x\"}", Map.of(), Map.of("userId", 1), Map.of());

        assertEquals("/posts", receivedPath.get());
        assertEquals("1", decodeQueryValue(receivedQuery.get()));
    }

    @Test
    @DisplayName("Parametresiz istekte query bos kalir")
    void noParametersMeansNoQueryString() {
        api.get("/posts/1");

        assertEquals("/posts/1", receivedPath.get());
        assertNull(receivedQuery.get());
    }

    /** {@code q=a%20b%26c} veya {@code q=a+b%26c} bicimlerinin ikisini de cozer. */
    private static String decodeQueryValue(String query) {
        int separator = query.indexOf('=');
        return URLDecoder.decode(query.substring(separator + 1), StandardCharsets.UTF_8);
    }
}
