package com.commencis.interview.frameworktest;

import com.commencis.interview.api.ApiClient;
import com.commencis.interview.api.ApiRequestException;
import com.commencis.interview.api.RequestSpecFactory;
import io.restassured.http.Method;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Istek gonderilemedigi zaman hatanin turunun mesajda gorunmesini dogrular.
 * Dis aga cikilmaz: siniflandirma bilinen exception tipleriyle ve loopback ile denenir.
 */
@Tag("unit")
@DisplayName("API transport error reporting")
class ApiErrorReportingTest {

    private static final String URL = "https://example.invalid/users";

    @Test
    @DisplayName("Base URL yokken relative path verilirse ne yapilacagi soylenir")
    void relativePathWithoutBaseUrlIsExplained() {
        ApiClient clientWithoutBaseUrl = new ApiClient(RequestSpecFactory.create(), "");

        ApiRequestException exception =
                assertThrows(ApiRequestException.class, () -> clientWithoutBaseUrl.get("/posts/1"));

        assertEquals(ApiRequestException.Category.REQUEST_ERROR, exception.category());
        assertTrue(exception.getMessage().contains("api.base.url"),
                "mesaj eksik ayari soylemeli: " + exception.getMessage());
    }

    @Test
    @DisplayName("Host cozulemezse DNS_ERROR raporlanir")
    void dnsFailureIsReportedAsDnsError() {
        ApiRequestException exception =
                ApiRequestException.from(Method.GET, URL, new UnknownHostException("example.invalid"));

        assertEquals(ApiRequestException.Category.DNS_ERROR, exception.category());
        assertEquals("DNS_ERROR - GET " + URL + " failed: UnknownHostException: example.invalid",
                exception.getMessage());
    }

    @Test
    @DisplayName("Gercek neden sarmalanmis olsa da cause zincirinden bulunur")
    void connectionRefusedIsFoundInCauseChain() {
        Throwable wrapped = new IllegalStateException("request failed",
                new ConnectException("Connection refused: connect"));

        ApiRequestException exception = ApiRequestException.from(Method.POST, URL, wrapped);

        assertEquals(ApiRequestException.Category.CONNECTION_ERROR, exception.category());
        assertTrue(exception.getMessage().startsWith("CONNECTION_ERROR - POST " + URL + " failed:"),
                "mesaj kategori/method/url ile baslamali: " + exception.getMessage());
        assertTrue(exception.getMessage().contains("Connection refused"),
                "mesaj kok nedeni tasimali: " + exception.getMessage());
        assertSame(wrapped, exception.getCause(), "orijinal hata cause olarak korunmali");
    }

    @Test
    @DisplayName("Yanit beklerken sure asilirsa TIMEOUT_ERROR raporlanir")
    void readTimeoutIsReportedAsTimeoutError() {
        ApiRequestException exception =
                ApiRequestException.from(Method.GET, URL, new SocketTimeoutException("Read timed out"));

        assertEquals(ApiRequestException.Category.TIMEOUT_ERROR, exception.category());
        assertTrue(exception.getMessage().contains("Read timed out"), exception.getMessage());
    }

    @Test
    @DisplayName("Gercek bir istek baglanamazsa client CONNECTION_ERROR firlatir")
    void unreachablePortIsReportedAsConnectionError() throws IOException {
        // Once bos bir port alinir, sonra kapatilir: dinleyen kimse olmadigi icin loopback
        // baglantisi aninda reddedilir. Sabit port veya dis servis kullanilmaz.
        int closedPort;
        try (ServerSocket socket = new ServerSocket(0)) {
            closedPort = socket.getLocalPort();
        }
        String url = "http://127.0.0.1:" + closedPort + "/posts/1";
        ApiClient api = new ApiClient(RequestSpecFactory.create());

        ApiRequestException exception = assertThrows(ApiRequestException.class, () -> api.get(url));

        assertEquals(ApiRequestException.Category.CONNECTION_ERROR, exception.category());
        assertTrue(exception.getMessage().startsWith("CONNECTION_ERROR - GET " + url + " failed:"),
                exception.getMessage());
    }

    @Test
    @DisplayName("Taninmayan hata REQUEST_ERROR olarak raporlanir")
    void unknownFailureFallsBackToRequestError() {
        ApiRequestException exception =
                ApiRequestException.from(Method.PUT, URL, new IllegalArgumentException("Cannot serialize body"));

        assertEquals(ApiRequestException.Category.REQUEST_ERROR, exception.category());
        assertTrue(exception.getMessage().contains("IllegalArgumentException: Cannot serialize body"),
                exception.getMessage());
    }
}
