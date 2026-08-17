package com.commencis.interview.frameworktest;

import com.commencis.interview.api.ApiClient;
import com.commencis.interview.api.RequestSpecFactory;
import com.sun.net.httpserver.HttpServer;
import io.restassured.http.Method;
import io.restassured.response.Response;
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
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Gercek filtre yolundan gecen bir istekle rapor maskeleme kanaryasi.
 *
 * <p>Test iki seyi birlikte kanitlar: gonderilen istek ve alinan yanit <b>degismez</b> (sunucu
 * canary degerlerini oldugu gibi alir), buna karsilik rapora yazilan kopya maskelenir. Ikinci
 * kismin dis kaniti kosum sonrasi {@code target/allure-results} altinda canary aranmasidir.
 *
 * <p>Sunucu loopback'tedir; dis aga cikilmaz.
 */
@Tag("unit")
@DisplayName("Report redaction canary")
class ReportRedactionCanaryTest {

    static final String QUERY_CANARY = "CANARY_QUERY_SECRET";
    static final String BODY_CANARY = "CANARY_BODY_SECRET";

    private final AtomicReference<String> receivedQuery = new AtomicReference<>();
    private final AtomicReference<String> receivedBody = new AtomicReference<>();

    private HttpServer server;
    private ApiClient api;

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        server.createContext("/", exchange -> {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            receivedQuery.set(exchange.getRequestURI().getQuery());
            receivedBody.set(body);
            // Govdeyi aynen geri doner: yanit tarafindaki maskeleme de sinanabilsin.
            byte[] payload = body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, payload.length);
            try (OutputStream stream = exchange.getResponseBody()) {
                stream.write(payload);
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
    @DisplayName("Canary gercek istekte gider ve yanitta doner; maskeleme yalnizca rapor kopyasindadir")
    void canaryReachesServerAndResponseUnchanged() {
        String body = "{\"client_secret\":\"" + BODY_CANARY + "\",\"title\":\"visible\"}";

        Response response = api.send(Method.POST, "/token", body, Map.of(),
                Map.of("access_token", QUERY_CANARY), Map.of());

        assertEquals(200, response.statusCode());
        assertTrue(receivedQuery.get().contains(QUERY_CANARY), "istek maskelenmeden gonderilmeli");
        assertTrue(receivedBody.get().contains(BODY_CANARY), "govde maskelenmeden gonderilmeli");
        assertTrue(response.asString().contains(BODY_CANARY), "Response nesnesi degistirilmemeli");
        assertEquals("visible", response.path("title"));
    }
}
