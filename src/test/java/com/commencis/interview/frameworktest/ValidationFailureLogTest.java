package com.commencis.interview.frameworktest;

import com.commencis.interview.api.ApiClient;
import com.commencis.interview.api.RequestSpecFactory;
import com.sun.net.httpserver.HttpServer;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Basarisiz bir Rest Assured dogrulamasinin ham govdeyi konsola basmadigini dogrular.
 *
 * <p>{@code enableLoggingOfRequestAndResponseIfValidationFails} acikken Rest Assured govdeyi
 * maskelemeden stdout'a yazar; stdout da failsafe-reports XML'ine gecer. Hata kaniti olarak
 * maskelenmis Allure ekleri kullanildigi icin o ayar kapalidir ve bu test kapali kalmasini
 * sabitler.
 */
@Tag("unit")
@DisplayName("Validation failure logging")
class ValidationFailureLogTest {

    static final String CANARY = "CANARY_VALIDATION_SECRET";

    private HttpServer server;
    private String baseUrl;

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        server.createContext("/", exchange -> {
            byte[] payload = ("{\"client_secret\":\"" + CANARY + "\",\"title\":\"visible\"}")
                    .getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, payload.length);
            try (OutputStream stream = exchange.getResponseBody()) {
                stream.write(payload);
            }
        });
        server.start();
        baseUrl = "http://" + server.getAddress().getHostString() + ":" + server.getAddress().getPort();
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    @Test
    @DisplayName("Basarisiz status dogrulamasi ham govdeyi stdout/stderr'e yazmaz")
    void failedValidationDoesNotPrintRawBody() {
        ByteArrayOutputStream capturedOut = new ByteArrayOutputStream();
        ByteArrayOutputStream capturedErr = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;

        Response response;
        try {
            System.setOut(new PrintStream(capturedOut, true, StandardCharsets.UTF_8));
            System.setErr(new PrintStream(capturedErr, true, StandardCharsets.UTF_8));

            response = new ApiClient(RequestSpecFactory.create(), baseUrl).get("/token");

            assertThrows(AssertionError.class, () -> response.then().statusCode(418),
                    "dogrulama bilerek basarisiz olmali");
        } finally {
            System.setOut(originalOut);
            System.setErr(originalErr);
        }

        assertTrue(response.asString().contains(CANARY), "yanit gercekten gizli deger tasimali");
        assertFalse(capturedOut.toString(StandardCharsets.UTF_8).contains(CANARY),
                "stdout ham govde icermemeli");
        assertFalse(capturedErr.toString(StandardCharsets.UTF_8).contains(CANARY),
                "stderr ham govde icermemeli");
    }
}
