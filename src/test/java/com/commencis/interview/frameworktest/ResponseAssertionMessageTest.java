package com.commencis.interview.frameworktest;

import com.commencis.interview.core.context.ApiContext;
import com.commencis.interview.core.context.ScenarioContext;
import com.commencis.interview.core.data.Placeholders;
import com.commencis.interview.stepdefinition.api.ApiResponseStepDefinitions;
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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Basarisiz bir status dogrulamasinin hata mesajina ham yanit govdesi koymadigini sabitler.
 * Hata mesaji failsafe raporuna ve Allure sonucuna maskelenmeden gectigi icin govde disarida kalir.
 */
@Tag("unit")
@DisplayName("Response assertion message")
class ResponseAssertionMessageTest {

    static final String CANARY = "CANARY_ASSERTION_SECRET";

    private HttpServer server;
    private ApiContext api;

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        server.createContext("/", exchange -> {
            byte[] payload = ("{\"client_secret\":\"" + CANARY + "\"}").getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, payload.length);
            try (OutputStream stream = exchange.getResponseBody()) {
                stream.write(payload);
            }
        });
        server.start();

        api = new ApiContext();
        api.baseUrl("http://" + server.getAddress().getHostString() + ":" + server.getAddress().getPort());
        api.send(Method.GET, "/token");
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    @Test
    @DisplayName("Status hatasinda mesaj ham govde tasimaz, Allure ekine yonlendirir")
    void failedStatusCheckDoesNotLeakResponseBody() {
        ScenarioContext context = new ScenarioContext();
        ApiResponseStepDefinitions steps =
                new ApiResponseStepDefinitions(api, context, new Placeholders(context));

        AssertionError error = assertThrows(AssertionError.class, () -> steps.checkStatus(418));

        assertFalse(error.getMessage().contains(CANARY), "mesaj gizli deger tasimamali: " + error.getMessage());
        assertFalse(error.getMessage().contains("client_secret"), error.getMessage());
        assertTrue(error.getMessage().contains("API response"), "mesaj Allure ekine yonlendirmeli: " + error.getMessage());
        assertTrue(api.response().asString().contains(CANARY), "Response nesnesi degistirilmemeli");
    }
}
