package com.commencis.interview;

import com.commencis.api.ApiClient;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;


@DisplayName("Interview live coding - API")
class InterviewApi {

    private static final Logger log = LoggerFactory.getLogger(InterviewApi.class);
    private final ApiClient api = new ApiClient();

    @Test
    @DisplayName("Interview API - saucelabs Scenario")
    void interViewApi() {

        /**
         * API:
         *
         * document: https://documenter.getpostman.com/view/4012288/TzK2bEa8#bcd848eb-d7ae-4b73-9a0c-59eb2254017e
         * adımlar:
         * 1. users altında add user yap
         * 2. get user ile eklenen userı doğrula
         * 3. update user ile firstname ve lastname'i random değerler ile değiştir
         * 4. get user ile değişen değerleri doğrula
         * 5. oluşturulan userı sil
         * 6. get user ile userın silindiğini doğrula
         */

        api.baseUrl("https://thinking-tester-contact-list.herokuapp.com");

        String random = UUID.randomUUID().toString().replace("-", "").substring(0, 8);

        String firstName = "Test" + random;
        String lastName = "User" + random;
        String email = "commencis." + random + "@example.com";
        String password = "commencis123";


        System.out.println("------------ 1. users altında add user yap ------------");


        Map<String, String> createBody = Map.of(
                "firstName", firstName,
                "lastName", lastName,
                "email", email,
                "password", password
        );

        Response createResponse = api.post("/users", createBody);
        createResponse.prettyPrint();

        assertAll(
                () -> assertEquals(201, createResponse.statusCode()),
                () -> assertEquals(firstName, createResponse.path("user.firstName")),
                () -> assertEquals(lastName, createResponse.path("user.lastName")),
                () -> assertEquals(email, createResponse.path("user.email")),
                () -> assertNotNull(createResponse.path("user._id")),
                () -> assertNotNull(createResponse.path("token"))
        );

        String token = createResponse.path("token");
        api.bearerToken(token);


        System.out.println("------------ 2. get user ile eklenen userı doğrula ------------");


        Response getResponse = api.get("/users/me");
        getResponse.prettyPrint();

        assertAll(
                () -> assertEquals(200, getResponse.statusCode()),
                () -> assertEquals(firstName, getResponse.path("firstName")),
                () -> assertEquals(lastName, getResponse.path("lastName")),
                () -> assertEquals(email, getResponse.path("email"))
        );


        System.out.println("------------ 3. update user ile firstname ve lastname'i random değerler ile değiştir ------------");


        String updatedFirstName = "Updated" + random;
        String updatedLastName = "Commencis" + random;

        Map<String, String> updateBody = Map.of(
                "firstName", updatedFirstName,
                "lastName", updatedLastName
        );

        Response updateResponse = api.patch("/users/me", updateBody);
        updateResponse.prettyPrint();

        assertAll(
                () -> assertEquals(200, updateResponse.statusCode()),
                () -> assertEquals(updatedFirstName, updateResponse.path("firstName")),
                () -> assertEquals(updatedLastName, updateResponse.path("lastName"))
        );


        System.out.println("------------ 4. get user ile değişen değerleri doğrula ------------");


        Response updatedGetResponse = api.get("/users/me");
        updatedGetResponse.prettyPrint();

        assertAll(
                () -> assertEquals(200, updatedGetResponse.statusCode()),
                () -> assertEquals(updatedFirstName, updatedGetResponse.path("firstName")),
                () -> assertEquals(updatedLastName, updatedGetResponse.path("lastName")),
                () -> assertEquals(email, updatedGetResponse.path("email"))
        );


        System.out.println("------------ 5. oluşturulan userı sil ------------");


        Response deleteResponse = api.delete("/users/me");
        deleteResponse.prettyPrint();

        assertEquals(200, deleteResponse.statusCode());


        System.out.println("------------ 6. get user ile userın silindiğini doğrula ------------");


        Response deletedUserResponse = api.get("/users/me");
        deletedUserResponse.prettyPrint();

        assertEquals(401, deletedUserResponse.statusCode(), "Silinen kullanici ayni token ile erisilebilir olmamali");


    }


    // EXAMPLE, NOT WORKING
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

    // EXAMPLE, NOT WORKING
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
}
