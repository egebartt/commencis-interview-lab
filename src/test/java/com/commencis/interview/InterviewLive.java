package com.commencis.interview;

import com.commencis.api.ApiClient;
import com.commencis.core.Driver;
import com.commencis.mobile.actions.MobileActions;
import com.commencis.mobile.pages.ApiDemosPage;
import com.commencis.mobile.pages.InterviewPage;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Interview live coding")
class InterviewLive {

    /** Driver ilk mobil aksiyonda acilir; API testleri cihaz istemez. */
    private final Driver driver = new Driver();

    /** Cucumber yolunda bu satiri PicoContainer yapar. */
    private final MobileActions mobile = new MobileActions(driver);

    private final ApiClient api = new ApiClient();

    @AfterEach
    void quitDriver() {
        driver.quit();
    }


    @Test
    @DisplayName("API - basliga gore kitaplari filtreleme")
    void shouldFilterBooksByTitle() {
        api.baseUrl("https://demo.api-platform.com");
        api.header("Accept", "application/json");
        //api.header("Content-Type", "application/json");
        //api.bearerToken("deneme");
        api.queryParams(Map.of(
                "title", "1636",
                "page", "1",
                "order[title]", "asc"));

        Response response = api.get("/books");
        response.prettyPrint();

        int statusCode = response.statusCode();
        String contentType = response.contentType();
        long responseTime = response.time();

        List<Object> bookList = response.jsonPath().getList("$");
        List<String> titles = response.jsonPath().getList("title", String.class);

        String firstTitle = response.path("[0].title");
        String firstAuthor = response.path("[0].author");
        String firstCondition = response.path("[0].condition");
        int firstRating = response.path("[0].rating");

        assertAll(
                () -> assertTrue(response.contentType().contains("application/json")),
                () -> assertEquals(200, statusCode),
                () -> assertTrue(contentType.startsWith("application/json")),
                () -> assertTrue(responseTime < 10_000, "Yanit " + responseTime + " ms surdu"),
                () -> assertFalse(bookList.isEmpty()),
                () -> assertEquals(30, bookList.size()),
                () -> assertFalse(firstTitle.isBlank()),
                () -> assertEquals("1636", firstTitle),
                () -> assertTrue(firstAuthor != null && !firstAuthor.isBlank()),
                () -> assertTrue(firstCondition != null && !firstCondition.isBlank()),
                () -> assertTrue(firstRating >= 0 && firstRating <= 5, "Gecersiz rating: " + firstRating),
                () -> assertFalse(titles.isEmpty()),
                () -> assertTrue(titles.stream().allMatch("1636"::equals), "Filtreye uymayan kitap basligi bulundu: " + titles)
        );

    }

    @Test
    @DisplayName("API - Rest Assured reference")
    void apiReferences() {

        api.baseUrl("https://demo.api-platform.com");

        api.headers(Map.of(
                "Accept", "application/json",              // Response'u JSON olarak bekliyorum
                "Content-Type", "application/json",            // Gonderdigim body JSON formatinda
                "Accept-Language", "en-US",                    // Response dili tercihi
                "X-Request-Id", "interview-request-123",       // Request takibi icin custom id
                "X-Correlation-Id", "correlation-123",         // Servisler arasi request takibi
                "User-Agent", "CommencisInterviewTests/1.0"        // Request'i atan client bilgisi
        ));

        api.header("Accept", "application/json");

        // Bearer authentication:
        // api.bearerToken(accessToken);  ==  api.header("Authorization", "Bearer " + accessToken);

        // /books?itemsPerPage=5&page=1&title=1636&order[title]=asc
        api.queryParams(Map.of(
                "itemsPerPage", "5",           // Sayfada kac kayit donsun
                "page", "1",                       // Pagination
                "title", "1636",                   // Title filtresi
                "author", "Eric Flint",            // Author filtresi
                "condition", "DamagedCondition",   // Condition filtresi
                "order[title]", "asc"                  // Title'a gore siralama
        ));

        Response response = api.get("/books");

        response.prettyPrint();                                      // Response body'yi formatli olarak konsola basar

        int statusCode = response.statusCode();                      // HTTP status code
        String statusLine = response.statusLine();                   // HTTP/1.1 200 OK
        String contentType = response.contentType();                 // application/json
        long responseTime = response.time();                         // Response suresi ms
        String rawBody = response.asString();                        // Body'nin String hali
        String contentTypeHeader = response.getHeader("Content-Type"); // Belirli response header'i


        String firstTitle = response.path("[0].title");
        String firstAuthor = response.path("[0].author");
        String firstCondition = response.path("[0].condition");
        int firstRating = response.path("[0].rating");
        String firstBookUrl = response.path("[0].book");
        String firstReview = response.path("[0].reviews[0]");


        String jsonTitle = response.jsonPath().getString("[0].title");                              // String alan okur
        int jsonRating = response.jsonPath().getInt("[0].rating");                                  // int alan okur
        List<Object> bookList = response.jsonPath().getList("$");                                   // Root array'in tamamini alir
        List<String> titles = response.jsonPath().getList("title", String.class);                   // Tum title alanlarini alir
        List<String> authors = response.jsonPath().getList("author", String.class);                 // Tum author alanlarini alir
        List<String> conditions = response.jsonPath().getList("condition", String.class);           // Tum condition alanlarini alir
        List<String> firstBookReviews = response.jsonPath().getList("[0].reviews", String.class);   // Ilk kitabin reviews listesini alir


        String hyperionAuthor = response.jsonPath().getString("find { it.title == 'Hyperion' }.author");                  // Hyperion'u bulup author'ini getirir
        String firstHighRatedTitle = response.jsonPath().getString("find { it.rating != null && it.rating >= 3 }.title"); // Ilk rating >= 3 kitabi bulur

        List<String> highRatedTitles = response.jsonPath().getList("findAll { it.rating != null && it.rating >= 3 }.title", String.class);   // Rating >= 3 olan tum title'lari alir
        List<String> usedBooks = response.jsonPath().getList("findAll { it.condition.contains('UsedCondition') }.title", String.class);      // UsedCondition olan kitaplari alir
        List<String> ericFlintBooks = response.jsonPath().getList("findAll { it.author == 'Eric Flint' }.title", String.class);              // Eric Flint'in tum kitaplarini alir

        boolean hasHyperion = titles.stream().anyMatch("Hyperion"::equals);                                       // En az bir title Hyperion mu
        boolean allTitlesFilled = titles.stream().allMatch(title -> title != null && !title.isBlank());    // Tum title'lar dolu mu
        boolean noBlankAuthors = authors.stream().noneMatch(author -> author == null || author.isBlank()); // Bos author hic var mi
        boolean titleMatchesPattern = firstTitle.matches("[A-Za-z0-9' :.-]+");                             // String regex'e uyuyor mu

        assertAll(
                () -> assertEquals(200, statusCode),                         // Beklenen HTTP status code
                () -> assertTrue(contentType.startsWith("application/json")),         // JSON response mu
                () -> assertTrue(contentTypeHeader.contains("application/json")),     // Header JSON iceriyor mu
                () -> assertTrue(responseTime < 10_000),                     // Response suresi limit altinda mi
                () -> assertFalse(rawBody.isBlank()),                                 // Response body bos mu

                () -> assertNotNull(bookList),                                 // Liste null olmamali
                () -> assertFalse(bookList.isEmpty()),                         // Liste bos olmamali
                () -> assertTrue(bookList.size() > 0),                // En az bir kayit olmali
                () -> assertTrue(bookList.size() <= 30),              // Maksimum beklenen liste boyutu

                () -> assertNotNull(firstTitle),                              // Title null olmamali
                () -> assertFalse(firstTitle.isBlank()),                      // Title bos olmamali
                () -> assertTrue(firstTitle.length() > 0),           // Title uzunlugu sifirdan buyuk olmali

                () -> assertNotNull(firstAuthor),                            // Author null olmamali
                () -> assertFalse(firstAuthor.isBlank()),                    // Author bos olmamali

                () -> assertNotNull(firstCondition),                         // Condition null olmamali
                () -> assertTrue(firstCondition.startsWith("https://")),     // Condition URL ile basliyor mu
                () -> assertTrue(firstCondition.contains("schema.org")),     // Beklenen domain'i iceriyor mu
                () -> assertTrue(firstCondition.endsWith("Condition")),      // Beklenen suffix ile bitiyor mu

                () -> assertTrue(firstRating >= 0),                 // Rating minimum sinirin ustunde mi
                () -> assertTrue(firstRating <= 5),                 // Rating maximum sinirin altinda mi

                () -> assertFalse(titles.isEmpty()),                         // Title listesi bos olmamali
                () -> assertEquals(bookList.size(), titles.size()),          // Her kitap icin title var mi

                () -> assertTrue(hasHyperion),                               // Hyperion listede var mi
                () -> assertTrue(allTitlesFilled),                           // Tum title'lar dolu mu
                () -> assertTrue(noBlankAuthors),                            // Bos author bulunmuyor mu
                () -> assertTrue(titleMatchesPattern),                       // Title regex'e uyuyor mu

                () -> assertNotNull(hyperionAuthor),                         // find sonucu bulundu mu
                () -> assertFalse(highRatedTitles.isEmpty()),                // findAll sonucu bos degil mi
                () -> assertTrue(highRatedTitles.size() > 0),       // Kosula uyan kayit var mi
                () -> assertNotNull(usedBooks),                              // Filtre sonucu null degil mi
                () -> assertNotNull(ericFlintBooks)                          // Filtre sonucu null degil mi
        );

    /*
    api.formParams(Map.of(
            "grant_type", "authorization_code",      // OAuth grant tipi
            "client_id", "api-platform-swagger",     // OAuth client kimligi
            "client_secret", "client-secret",        // Client secret gerekiyorsa
            "code", authorizationCode,               // Login sonrasi alinan authorization code
            "redirect_uri", redirectUri,             // OAuth callback adresi
            "code_verifier", codeVerifier,           // PKCE code verifier
            "scope", "openid"                        // OIDC authentication scope
    ));

    Response tokenResponse = api.post("/oidc/realms/demo/protocol/openid-connect/token");
    tokenResponse.prettyPrint();
    String accessToken = tokenResponse.path("access_token");
    api.bearerToken(accessToken);
    */

        String requestBody = """
            {
              "book": "https://openlibrary.org/books/OL2055137M.json",
              "condition": "https://schema.org/NewCondition"
            }
            """;

        Response postResponse = api.post("/admin/books", requestBody);

        postResponse.prettyPrint();

        int postStatusCode = postResponse.statusCode();                       // POST status code
        String createdBook = postResponse.path("book");                  // Olusturulan book alani
        String createdTitle = postResponse.path("title");                // Response title donuyorsa al
        String createdCondition = postResponse.path("condition");        // Olusturulan condition
        String createdResourceId = postResponse.path("@id");             // JSON-LD resource id varsa al

        assertAll(
                () -> assertEquals(201, postStatusCode),           // Resource olusturuldu mu
                () -> assertNotNull(createdBook),                           // Book response'ta var mi
                () -> assertFalse(createdBook.isBlank()),                   // Book bos degil mi
                () -> assertNotNull(createdCondition),                      // Condition var mi
                () -> assertTrue(createdCondition.contains("NewCondition")) // Gonderilen condition dondu mu
        );
    }

    @Test
    @DisplayName("API - get book by id")
    void getBookById() {

        /**
        api.formParams(Map.of(
                "Authorization", "Bearer 123",
                "client_id", "api-platform-swagger",
                "client_secret", "123123"
        ));



        String requestBody = """
            {
              "book": "https://openlibrary.org/books/OL2055137M.json",
              "condition": "https://schema.org/NewCondition"
            }
            """;
         */


        //Response response = api.post("/admin/books", requestBody);
        Response usersResponse = api.get("https://fake-json-api.mock.beeceptor.com/users");

        int id = usersResponse.jsonPath().getInt("[0].id");

        api.pathParams(Map.of("id", id));

        Response response = api.get("https://fake-json-api.mock.beeceptor.com/users/{id}");

        response.prettyPrint();
        assertEquals(200, response.statusCode());



    }

    @Test
    void getUserGeneric() {

        String token = "your_access_token";

        Response response =
                given()
                        .baseUri("https://dummyjson.com")
                        .header("Authorization", "Bearer " + token)
                        .when()
                        .get("/auth/me");

        response.prettyPrint();

        assertEquals(200, response.statusCode());

        String username = response.jsonPath().getString("username");
        assertNotNull(username);
    }
    @Test
    void createPostGeneric() {

        String token = "your_access_token";

        String requestBody = """
        {
          "title": "Rest Assured Test",
          "userId": 1
        }
        """;

        Response response =
                given()
                        .baseUri("https://dummyjson.com")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .body(requestBody)
                        .when()
                        .post("/posts/add");

        response.prettyPrint();

        assertEquals(201, response.statusCode());

        int id = response.jsonPath().getInt("id");
        String title = response.jsonPath().getString("title");

        assertEquals("Rest Assured Test", title);
        assertTrue(id > 0);
    }

    @Test
    void GenericExample() {

        String token = "your_access_token";

        String requestBody = """
        {
          "title": "Rest Assured Test",
          "userId": 1
        }
        """;

        Response response =
                given()

                        .baseUri("https://dummyjson.com")              // Ana URL
                        .basePath("/v1")                               // Ortak path

                        .header("Authorization", "Bearer " + token)    // Tek header
                        .header("x-api-key", "12345")

                        .headers(Map.of(                               // Birden fazla header
                                "Authorization", "Bearer " + token,
                                "Accept", "application/json"
                        ))

                        .contentType("application/json")               // Gönderdiğim body'nin tipi
                        .accept("application/json")                    // Response'u hangi formatta istiyorum

                        .queryParam("limit", 10)                       // ?limit=10
                        .queryParam("skip", 20)                        // &skip=20

                        .pathParam("id", 5)                            // /users/{id}

                        .formParam("username", "bartu")                // Form data
                        .formParam("password", "123456")

                        .cookie("sessionId", "abc123")                 // Cookie

                        .body(requestBody)                             // Request body
                        .log().all()                 // Request'i logla
                        .when()
                        .post("/posts/add");
        // Request'in tamamını logla

        response.prettyPrint();

        assertEquals(201, response.statusCode());

        int id = response.jsonPath().getInt("id");
        String title = response.jsonPath().getString("title");

        assertEquals("Rest Assured Test", title);
        assertTrue(id > 0);
    }


    @Test
    @DisplayName("Mobil - Views menusu acilir")
    void opensViewsMenu() {
        InterviewPage interviewPage = new InterviewPage(mobile);

        assertTrue(interviewPage.isHomeVisible(), "Interview ana ekrani gorunmedi");
        interviewPage.openViews();

        assertTrue(interviewPage.isButtonsVisible(), "Interview Views ekraninda Buttons gorunmedi");
    }

    @Test
    @DisplayName("Mobil - Spinner'dan gezegen secilir")
    void selectsPlanetFromSpinner() {
        ApiDemosPage apiDemos = new ApiDemosPage(mobile);

        apiDemos.openViews();
        apiDemos.openSpinner();
        apiDemos.selectPlanet("Jupiter");

        assertEquals("Jupiter", apiDemos.selectedPlanet());
    }


}
