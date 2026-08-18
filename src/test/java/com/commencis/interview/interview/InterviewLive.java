package com.commencis.interview.interview;

import com.commencis.interview.api.ApiClient;
import com.commencis.interview.core.Driver;
import com.commencis.interview.mobile.actions.ElementActions;
import com.commencis.interview.mobile.pages.ApiDemosPage;
import com.commencis.interview.mobile.pages.InterviewPage;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Mulakatta hizli case yazmak icin <b>tek dosyalik</b> JUnit calisma alani.
 *
 * <p>Kalici senaryolarin yeri feature dosyalaridir; bu dosya prova bitince silinebilir, projede
 * hicbir sey ona bagli degildir. Ayni altyapiyi kullanir: {@link Driver}, {@link ApiClient},
 * {@link ElementActions}, Page Object'ler.
 *
 * <p>Cucumber yolunda nesne zincirini PicoContainer kurar. Burada Cucumber yoktur, bu yuzden ayni
 * zincir elle ve gorunur bicimde kurulur: {@code Driver -> ElementActions -> Page}. Mulakatta
 * "DI olmadan bu nasil baglanir" sorusunun cevabi da bu iki satirdir.
 *
 * <p>Sinif adi bilerek {@code *Test} ile bitmez: varsayilan kosumda ({@code mvnw clean verify})
 * calismaz, yalnizca acikca istendiginde kosar.
 *
 * <pre>
 * .\mvnw.cmd clean verify "-Dit.test=InterviewLive"
 * .\mvnw.cmd clean verify "-Dit.test=InterviewLive#opensViewsMenu"
 * </pre>
 *
 * <p>IntelliJ'de tek testi sag tik &gt; Run ile de calistirabilirsiniz.
 */
@DisplayName("Interview live coding")
class InterviewLive {

    /** Driver ilk mobil aksiyonda acilir; API testleri cihaz istemez. */
    private final Driver driver = new Driver();

    /** Cucumber yolunda bu satiri PicoContainer yapar. */
    private final ElementActions element = new ElementActions(driver);

    private final ApiClient api = new ApiClient();

    @AfterEach
    void quitDriver() {
        driver.quit();
    }

    // ------------------------------------------------------------------
    // API
    // ------------------------------------------------------------------

    @Test
    @DisplayName("API - mevcut post okunur")
    void getsExistingPost() {
        Response response = api.get("/posts/1");

        assertEquals(200, response.statusCode());
        assertEquals(1, (int) response.path("id"));
    }

    @Test
    @DisplayName("API - inline govde ve header ile post olusturulur")
    void createsPost() {
        api.headers(Map.of("X-Request-Id", "interview-live-1"));

        Response response = api.post("/posts", """
                {
                  "userId": 7,
                  "title": "Interview live coding",
                  "body": "Gecici API senaryosu"
                }
                """);

        assertEquals(201, response.statusCode());
        assertEquals("Interview live coding", response.path("title"));
    }

    @Test
    @DisplayName("API - query parametresiyle yorumlar filtrelenir")
    void filtersCommentsWithQueryParam() {
        api.queryParams(Map.of("postId", "1"));

        Response response = api.get("/comments");

        assertEquals(200, response.statusCode());
        assertEquals(1, (int) response.path("[0].postId"));
    }

    // ------------------------------------------------------------------
    // Mobil
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Mobil - Views menusu acilir")
    void opensViewsMenu() {
        InterviewPage interviewPage = new InterviewPage(element);

        assertTrue(interviewPage.isHomeVisible(), "Interview ana ekrani gorunmedi");
        interviewPage.openViews();

        assertTrue(interviewPage.isButtonsVisible(), "Interview Views ekraninda Buttons gorunmedi");
    }

    @Test
    @DisplayName("Mobil - Spinner'dan gezegen secilir")
    void selectsPlanetFromSpinner() {
        ApiDemosPage apiDemos = new ApiDemosPage(element);

        apiDemos.openViews();
        apiDemos.openSpinner();
        apiDemos.selectPlanet("Jupiter");

        assertEquals("Jupiter", apiDemos.selectedPlanet());
    }
}
