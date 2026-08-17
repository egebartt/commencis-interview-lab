package com.commencis.interview.live;

import com.commencis.interview.core.data.CsvData;
import com.commencis.interview.core.data.CsvOutput;
import com.commencis.interview.core.data.JsonData;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Mulakatta verilen case'leri hizlica yazmak icin calisma alani.
 *
 * <p>Yeni bir ekran icin gereken tek sey locator'i ilgili {@code *Locators} sinifina eklemektir;
 * buradan sayfa adi + element adiyla cagrilir. Ayni case'ler feature dosyasina da yazilabilir,
 * ikisi de ayni altyapiya iner.
 *
 * <pre>
 * mvnw.cmd clean verify -Papi    "-Dit.test=LiveCodingTest"
 * mvnw.cmd clean verify -Pmobile "-Dit.test=LiveCodingTest"
 * </pre>
 */
@DisplayName("Live coding")
class LiveCodingTest extends BaseTest {

    private static final String API_DEMOS = "Api Demos Page";

    @Test
    @Tag("api")
    @DisplayName("Kayit okunur ve alanlari dogrulanir")
    void getsExistingPost() {
        Response response = api().get("/posts/1");

        assertEquals(200, response.statusCode());
        assertEquals(1, (int) response.path("id"));
    }

    @Test
    @Tag("api")
    @DisplayName("JSON dosyasindaki govde ile kayit olusturulur")
    void createsPostFromJsonFile() {
        Response response = api().post("/posts", JsonData.read("testdata/json/create-post.json"));

        assertEquals(201, response.statusCode());
        assertEquals("Commencis interview lab", response.path("title"));
    }

    @Test
    @Tag("api")
    @DisplayName("CSV satirindan govde uretilir ve yanit CSV'ye yazilir")
    void createsPostFromCsvRowAndWritesOutput() {
        String body = CsvData.rowAsJson("testdata/csv/posts.csv", "case", "happy_path");

        Response response = api().post("/posts", body);

        assertEquals(201, response.statusCode());
        CsvOutput.append("created-posts.csv", response, List.of("id", "title", "userId"));
    }

    @Test
    @Tag("api")
    @DisplayName("Senaryoya ozel base URL ile bagimsiz bir adrese istek atilir")
    void callsIndependentBaseUrl() {
        Response response = api().get("https://jsonplaceholder.typicode.com/users/1");

        assertEquals(200, response.statusCode());
        assertEquals("Bret", response.path("username"));
    }

    @Test
    @Tag("api")
    @DisplayName("Query parametreleri encoding'i Rest Assured'a birakilarak gonderilir")
    void sendsQueryParameters() {
        Response response = api().send(io.restassured.http.Method.GET, "/comments", null,
                Map.of(), Map.of("postId", 1), Map.of());

        assertEquals(200, response.statusCode());
        assertEquals(1, (int) response.path("[0].postId"));
    }

    @Test
    @Tag("mobile")
    @DisplayName("Views menusu acilir")
    void opensViewsMenu() {
        ui().click(API_DEMOS, "VIEWS_MENU");

        assertTrue(ui().isVisible(API_DEMOS, "BUTTONS_OPTION"), "Views ekraninda Buttons gorunmedi");
    }

    @Test
    @Tag("mobile")
    @DisplayName("Spinner dropdown'undan gezegen secilir")
    void selectsPlanetFromSpinner() {
        ui().click(API_DEMOS, "VIEWS_MENU");
        ui().scrollAndClick(API_DEMOS, "SPINNER_OPTION");
        ui().click(API_DEMOS, "PLANET_DROPDOWN");
        ui().click(com.commencis.interview.mobile.locator.DynamicLocators.byText("Jupiter"));

        assertEquals("Jupiter", ui().text(API_DEMOS, "PLANET_DROPDOWN_VALUE"));
    }
}
