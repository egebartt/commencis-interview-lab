package com.commencis.interview.interview;

import com.commencis.interview.api.ApiClient;
import com.commencis.interview.core.Csv;
import com.commencis.interview.core.Driver;
import com.commencis.interview.mobile.actions.MobileActions;
import com.commencis.interview.mobile.pages.ApiDemosPage;
import com.commencis.interview.mobile.pages.ControlsPage;
import com.commencis.interview.mobile.pages.InterviewPage;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
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


    /**
     * Bastan sona akis, verilen sirayla: adres -> header -> parametre -> kimlik dogrulama ->
     * request body -> dogrulama. Verilen case hangi adimdan basliyorsa oradan devam edilir.
     *
     * <p>Bu demoda gecerli token alinamaz: Swagger'in kullandigi {@code api-platform-swagger}
     * public client'tir, {@code client_credentials} akisina kapalidir. Token'i yanittan cekip
     * kullanan calisan ornek {@link #apiPlatformDemo()} icinde.
     */
    @Test
    @DisplayName("API - bastan sona akis")
    void apiFlow() {

        // 1) Adres: full base url. config.properties'e bagli degil.
        api.baseUrl("https://demo.api-platform.com");

        // 2) Header: senaryo boyunca kalir, sonraki istekler de tasir.
        api.header("X-Request-Id", "commencis-interview");
        api.header("Accept-Language", "en");

        // 3) Parametre: URL elle kurulmaz, encoding'i Rest Assured yapar.
        //    Okuma isteklerinin tamami token takilmadan once yapilir; gerekcesi 4b'de.
        api.queryParams(Map.of("page", "1", "title", "Hyperion"));
        Response filtered = api.get("/books");

        // queryParams gonderimden sonra temizlenir: bu istek filtresiz gider.
        Response all = api.get("/books");
        Response missing = api.get("/books/00000000-0000-0000-0000-000000000000");

        // 4a) OAuth2: token endpoint govdeyi form-encoded ister, JSON kabul etmez.
        //     Gercek bir gizli anahtarla 200 doner ve token.path("access_token") okunur.
        api.baseUrl("https://demo.api-platform.com/oidc/realms/demo/protocol/openid-connect");
        api.formParams(Map.of(
                "grant_type", "client_credentials",
                "client_id", "api-platform-swagger",
                "client_secret", "gizli-anahtar-buraya"));
        Response token = api.post("/token");

        // 4b) Elle verilen token. Bir kez takilinca senaryonun geri kalanini etkiler:
        //     gecersiz token bu API'de public okumayi da 401 yapar.
        api.baseUrl("https://demo.api-platform.com");
        api.bearerToken("mulakatta-verilen-token");

        // 5) Request body: korumali endpoint. Map JSON'a cevrilir.
        Response create = api.post("/admin/books", Map.of(
                "book", "https://openlibrary.org/books/OL2055137M.json",
                "condition", "https://schema.org/NewCondition"));

        // 6) Dogrulama cesitleri
        assertEquals(200, filtered.statusCode());
        assertTrue(filtered.header("Content-Type").contains("application/json"));
        assertTrue(filtered.header("allow").contains("GET"));
        assertTrue(filtered.time() < 15_000, "Yanit " + filtered.time() + " ms surdu");

        assertEquals(1, filtered.jsonPath().getList("$").size());
        assertEquals("Hyperion", filtered.path("[0].title"));
        assertNotNull(filtered.path("[0].author"));
        assertEquals(2, (int) filtered.path("[0].rating"));

        String condition = filtered.path("[0].condition");
        assertTrue(condition.endsWith("UsedCondition"), condition);

        List<String> titles = all.jsonPath().getList("title");
        assertEquals(30, titles.size());
        assertTrue(titles.stream().noneMatch(String::isBlank));

        assertEquals(404, missing.statusCode());
        assertEquals(404, (int) missing.path("status"));
        assertEquals("Not Found", missing.path("detail"));

        assertEquals(401, token.statusCode());
        assertEquals("unauthorized_client", token.path("error"));

        assertEquals(401, create.statusCode());
        assertEquals(401, api.get("/books").statusCode());
    }

    @Test
    @DisplayName("API - kopyala-yapistir referansi")
    void apiReference() {

        Response post = api.get("/posts/1"); // config.properties base url gelir, burada relative path yeter

        // 12) Base url'e hic dokunmadan full URL de verilebilir.
        Response direct = api.get("https://jsonplaceholder.typicode.com/todos/1");
        assertEquals(200, direct.statusCode());
        assertEquals(1, (int) direct.path("id"));
        assertEquals(404, api.get("/posts/999999").statusCode());
        assertTrue(direct.time() < 10_000, "Yanit " + direct.time() + " ms surdu"); //Yanit suresi


        // 2) Status code dogrulamasi.
        assertEquals(200, post.statusCode());

        // 3) Tek alan okuma. path()'in donus tipi cagrildigi yerden cikarilir; bu yuzden once
        int id = post.path("id");
        String title = post.path("title");
        assertEquals(1, id);
        assertNotNull(title);

        // 4) Ic ice alan: nokta ile inilir.
        Response user = api.get("/users/1");
        assertEquals(200, user.statusCode());
        assertEquals("Bret", user.path("username"));
        assertEquals("Gwenborough", user.path("address.city"));
        assertEquals("Romaguera-Crona", user.path("company.name"));

        // 5) Dizi donen yanit: [0].id gibi indeksli yazim. Cast gerekir, cunku assertEquals'in
        Response posts = api.get("/posts");
        assertEquals(1, (int) posts.path("[0].id"));
        assertEquals(1, (int) posts.path("[0].userId"));

        // Liste boyutu ve tum liste uzerinde kontrol.
        List<Object> allPosts = posts.jsonPath().getList("$");
        assertEquals(100, allPosts.size());
        List<Integer> firstUserPostIds = posts.jsonPath().getList("findAll { it.userId == 1 }.id");
        assertEquals(10, firstUserPostIds.size());

        // 6) Query parametresi. URL elle kurulmaz, encoding'i Rest Assured yapar.
        api.queryParams(Map.of("postId", "1"));
        Response comments = api.get("/comments");
        assertEquals(200, comments.statusCode());
        assertEquals(1, (int) comments.path("[0].postId"));

        // 7) POST + request body. Java 21 text block: JSON kacis karakteri olmadan yazilir.
        Response created = api.post("/posts", """
                {
                  "userId": 7,
                  "title": "Commencis interview",
                  "body": "Live coding"
                }
                """);
        assertEquals(201, created.statusCode());
        assertEquals("Commencis interview", created.path("title"));

        // Govde Map olarak da verilebilir; JSON'a Rest Assured cevirir.
        Response createdFromMap = api.post("/posts", Map.of("userId", 7, "title", "Map body"));
        assertEquals(201, createdFromMap.statusCode());
        assertEquals("Map body", createdFromMap.path("title"));

        // 8) Header ve Bearer token. Ikisi de senaryo boyunca kalir, sonraki istekler de tasir.
        //    Sabit token config'ten de gelebilir (-Dapi.token=...); bu metot testin kendi icinde
        //    alinan token icindir. Raporda Authorization maskeli gorunur, istek maskesiz gider.
        api.header("X-Request-Id", "interview-live");
        api.bearerToken("mulakatta-verilen-token");
        Response withToken = api.get("/posts/2");
        assertEquals(200, withToken.statusCode());

        // 9) Zincirleme: ilk yanittan alinan deger ikinci istekte kullanilir.
        int createdUserId = created.path("userId");
        Response updated = api.put("/posts/1", """
                { "title": "Updated from previous response", "userId": %d }
                """.formatted(createdUserId));
        assertEquals(200, updated.statusCode());
        assertEquals("Updated from previous response", updated.path("title"));
        assertEquals(createdUserId, (int) updated.path("userId"));

        // 10) PATCH ve DELETE.
        Response patched = api.patch("/posts/1", """
                { "title": "Patched" }
                """);
        assertEquals(200, patched.statusCode());
        assertEquals(200, api.delete("/posts/1").statusCode());

        // 11) Base url'i yalnizca bu test icin degistir; config.properties'e dokunulmaz.
        //     Mulakatta baska bir adres verilirse buraya yazilir.
        api.baseUrl("https://jsonplaceholder.typicode.com");
        assertEquals(200, api.get("/albums/1").statusCode());


    }

    /**
     * Ikinci referans: demo.api-platform.com (auth'suz okuma) + token'i login yanitindan cekip
     * sonraki isteklere takma. dummyjson kullanicisi o servisin dokumante ettigi test hesabidir.
     */
    @Test
    @DisplayName("API - demo API + token akisi")
    void apiPlatformDemo() {
        api.baseUrl("https://demo.api-platform.com");

        Response books = api.get("/books");
        assertEquals(200, books.statusCode());
        assertTrue(books.time() < 15_000);

        List<Object> firstPage = books.jsonPath().getList("$");
        assertEquals(30, firstPage.size());

        String firstTitle = books.path("[0].title");
        String firstAuthor = books.path("[0].author");
        assertNotNull(firstTitle);
        assertNotNull(firstAuthor);

        api.queryParams(Map.of("title", "Hyperion"));
        Response filtered = api.get("/books");
        assertEquals(200, filtered.statusCode());
        assertEquals(1, filtered.jsonPath().getList("$").size());
        assertEquals("Hyperion", filtered.path("[0].title"));
        assertEquals("Dan Simmons", filtered.path("[0].author"));

        api.queryParams(Map.of("order[title]", "asc"));
        Response sorted = api.get("/books");
        List<String> sortedTitles = sorted.jsonPath().getList("title");
        assertTrue(sortedTitles.get(0).compareToIgnoreCase(sortedTitles.get(sortedTitles.size() - 1)) < 0);

        api.queryParams(Map.of("page", "2"));
        Response secondPage = api.get("/books");
        assertEquals(200, secondPage.statusCode());
        assertNotEquals(firstTitle, secondPage.path("[0].title"));

        Response single = api.get("/books/01a01234-4c6c-7162-8fc1-6534326ac426");
        assertEquals(200, single.statusCode());
        assertEquals("Hyperion", single.path("title"));
        assertEquals("Dan Simmons", single.path("author"));
        assertEquals("https://schema.org/UsedCondition", single.path("condition"));
        assertEquals(2, (int) single.path("rating"));

        assertEquals(404, api.get("/books/00000000-0000-0000-0000-000000000000").statusCode());
        assertEquals(401, api.post("/admin/books", Map.of("book", "x")).statusCode());

        api.baseUrl("https://dummyjson.com");

        Response login = api.post("/auth/login", Map.of("username", "emilys", "password", "emilyspass"));
        assertEquals(200, login.statusCode());

        String accessToken = login.path("accessToken");
        assertNotNull(accessToken);
        api.bearerToken(accessToken);

        Response me = api.get("/auth/me");
        assertEquals(200, me.statusCode());
        assertEquals(1, (int) me.path("id"));
        assertEquals("emilys", me.path("username"));
        assertEquals("Emily", me.path("firstName"));

        api.queryParams(Map.of("limit", "5"));
        Response products = api.get("/products");
        assertEquals(200, products.statusCode());
        assertEquals(5, products.jsonPath().getList("products").size());
        assertNotNull(products.path("products[0].title"));
        assertTrue((int) products.path("total") > 0);
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
