package com.commencis.frameworktest;

import com.commencis.api.ApiClient;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Uctan uca canary: {@code ApiClient -> ReportFilter -> Allure eki} zincirini gercek bir HTTP
 * istegi ile calistirir ve iki seyi ayni anda dogrular:
 *
 * <ol>
 *   <li>Secret <b>gercekten gonderilir</b>: sunucu Authorization header'ini ve govdeyi maskesiz
 *       alir, yanit nesnesi de degismez. Maskeleme testi bozmamalidir.</li>
 *   <li>Secret <b>rapora yazilmaz</b>: {@code target/allure-results} altindaki eklerde hicbir
 *       gizli deger bulunmaz.</li>
 * </ol>
 *
 * <p>{@link RedactionTest} maskeleme fonksiyonlarini tek tek dogrular; bu test onlarin gercekten
 * bagli oldugunu dogrular. Biri kaldirilirsa unit testler yesil kalir, bu test kirmizi olur.
 *
 * <p>Sunucu JDK'nin kendi {@code HttpServer}'idir: ek bagimlilik ve ag erisimi gerekmez.
 */
@DisplayName("API report canary")
class ApiReportCanaryTest {

    private static final String SECRET = "canary-secret-" + UUID.randomUUID();

    /** Bu kosuma ait ekleri bulmak icin; gizli olmayan, aranabilir bir iz. */
    private final String marker = "canary-" + UUID.randomUUID();

    private HttpServer server;
    private String baseUrl;

    private final AtomicReference<String> receivedAuthorization = new AtomicReference<>();
    private final AtomicReference<String> receivedBody = new AtomicReference<>();

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/canary", this::handle);
        server.start();
        baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    @Test
    @DisplayName("Secret gonderilir ama Allure ekine yazilmaz")
    void secretIsSentButNeverReported() throws IOException {
        ApiClient api = new ApiClient();
        api.headers(Map.of("Authorization", "Bearer " + SECRET, "X-Request-Id", marker));
        api.queryParams(Map.of("access_token", SECRET));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("userId", 7);
        body.put("password", SECRET);

        Response response = api.post(baseUrl + "/canary", body);

        // 1) Istek maskelenmeden gitti: maskeleme yalnizca rapor kopyasini etkiler.
        assertEquals(200, response.statusCode());
        assertEquals("Bearer " + SECRET, receivedAuthorization.get());
        assertTrue(receivedBody.get().contains(SECRET), "Govde maskelenmis halde gonderilmis");
        assertEquals(SECRET, response.path("access_token"), "Yanit nesnesi degistirilmis");

        // 2) Rapor eki uretildi ve icinde hicbir secret yok.
        List<Path> attachments = attachmentsContaining(marker);
        assertFalse(attachments.isEmpty(),
                "Allure eki bulunamadi; bu test hicbir sey dogrulamiyor olurdu. "
                        + "Sonuc dizini: " + resultsDirectory());

        for (Path attachment : attachments) {
            String content = Files.readString(attachment, StandardCharsets.UTF_8);
            assertFalse(content.contains(SECRET), "Secret rapora sizdi: " + attachment + "\n" + content);
        }
    }

    private void handle(HttpExchange exchange) throws IOException {
        receivedAuthorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
        receivedBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));

        // Yanit da secret tasir: response ekinin maskelendigi buradan anlasilir.
        byte[] payload = ("{\"access_token\":\"" + SECRET + "\",\"marker\":\"" + marker + "\"}")
                .getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, payload.length);
        exchange.getResponseBody().write(payload);
        exchange.close();
    }

    /** Bu kosumun izini tasiyan ek dosyalari. */
    private List<Path> attachmentsContaining(String text) {
        Path results = resultsDirectory();
        if (!Files.isDirectory(results)) {
            return List.of();
        }
        List<Path> matches = new ArrayList<>();
        try (Stream<Path> files = Files.list(results)) {
            for (Path file : files.filter(Files::isRegularFile).toList()) {
                try {
                    if (Files.readString(file, StandardCharsets.UTF_8).contains(text)) {
                        matches.add(file);
                    }
                } catch (IOException | RuntimeException ignored) {
                    // Binary ek (ekran goruntusu) veya o an yazilan dosya: aranan iz zaten metin.
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Allure sonuc dizini okunamadi: " + results, e);
        }
        return matches;
    }

    private static Path resultsDirectory() {
        return Path.of(System.getProperty("allure.results.directory", "target/allure-results"));
    }
}
