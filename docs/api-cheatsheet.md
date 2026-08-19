# API cheat sheet (Rest Assured)

`ApiClient` isteği kurar ve gönderir; elde kalan tek şey `Response` nesnesidir. Bu dosya o nesneyle
ne yapılabileceğinin kopyala-yapıştır listesidir. Assertion `ApiClient` içinde **yoktur**: doğrulama
testin ya da Step Definition'ın işidir.

Gherkin adımlarının listesi ayrı dosyada: [step-catalog.md](step-catalog.md)

Sürümler: Rest Assured 6.0.1 · JUnit 5 · Gson 2.13.2 (classpath'teki tek object mapper)

---

## 1. İstek — `ApiClient`

```java
private final ApiClient api = new ApiClient();

api.baseUrl("https://demo.api-platform.com");              // yalnız bu oturum için; config.properties etkilenmez
api.header("X-Request-Id", "commencis-interview");         // tek header
api.headers(Map.of("Accept-Language", "en"));              // toplu header
api.bearerToken(accessToken);                              // Authorization: Bearer ...
api.queryParams(Map.of("page", "1", "title", "Hyperion")); // URL elle kurulmaz, encoding Rest Assured'ın işi
api.pathParams(Map.of("id", 5));                           // /users/{id} şablonunu doldurur
api.formParams(Map.of("grant_type", "client_credentials"));// form-encoded gövde (OAuth2 token endpoint)
api.body(Map.of("title", "Commencis"));                    // gövdeyi ayrıca kurmak için

Response response = api.get("/books");                     // relative path veya full URL
Response created  = api.post("/posts", Map.of("userId", 7));
Response replaced = api.put("/posts/1", "{ \"title\": \"x\" }");
Response patched  = api.patch("/posts/1", "{ \"title\": \"x\" }");
Response deleted  = api.delete("/posts/1");
Response last     = api.response();                        // en son yanıt (Step Definition'lar bunu okur)
```

| Ömür | Ne | Not |
| --- | --- | --- |
| Senaryo boyunca | `baseUrl`, `header`, `bearerToken` | Sonraki istekler de aynı kimlikle gider |
| Tek istek | `body`, `queryParams`, `formParams`, `pathParams` | Gönderimden sonra temizlenir |

Her istek ve yanıt Allure raporuna otomatik eklenir (secret'lar maskeli). `prettyPrint()` yalnızca
konsol içindir, rapor için gerekmez.

---

## 2. İstek — ham Rest Assured DSL

`ApiClient` aşağıdaki zinciri senin yerine kurar. Ama "Rest Assured'ı nasıl kullanırsın" diye
sorulursa beklenen cevap budur, ve teste doğrudan da yazabilirsin — `given()` statik import'tur.

Zincirin üç bölümü vardır ve **sıra zorunludur**:

| Bölüm | Ne yapar | Döndürdüğü tip |
| --- | --- | --- |
| `given()` | İsteği kurar: adres, header, parametre, gövde | `RequestSpecification` |
| `.when()` | İsteği **gönderir**; HTTP metodu buradadır | `Response` |
| `.then()` | Yanıtı doğrular | `ValidatableResponse` |

`.then()` bir `Response` **döndürmez**. Nesneyi almak için zincirin sonuna `.extract().response()`
gerekir. Doğrulamayı JUnit ile yapacaksan `.then()`'i hiç yazmayıp `.when().get(...)` çıktısını
doğrudan `Response`'a atarsın.

### `given()` — istek kurma

| Metot | Ne işe yarar |
| --- | --- |
| `.baseUri("https://dummyjson.com")` | Ana adres. Sonraki `.get("/posts")` buna eklenir |
| `.basePath("/api/v1")` | Bütün isteklerde ortak olan path parçası; `baseUri`'den sonra gelir |
| `.header("x-api-key", "12345")` | Tek header ekler |
| `.headers(Map.of(...))` | Birden fazla header'ı tek seferde ekler |
| `.contentType("application/json")` | **Gönderdiğin** gövdenin tipi (`Content-Type`) |
| `.accept("application/json")` | **İstediğin** yanıt formatı (`Accept`) |
| `.queryParam("limit", 10)` | `?limit=10`. Encoding'i Rest Assured yapar, URL'i elle birleştirme |
| `.pathParam("id", 5)` | `/users/{id}` şablonundaki `{id}`'yi doldurur |
| `.formParam("username", "bartu")` | `application/x-www-form-urlencoded` gövde. OAuth2 token endpoint'leri bunu ister |
| `.cookie("sessionId", "abc123")` | İsteğe cookie ekler |
| `.auth().basic("user", "pass")` | Basic auth; `Authorization: Basic <base64>` üretir |
| `.auth().oauth2(token)` | `Authorization: Bearer <token>` üretir. `.header("Authorization", "Bearer " + token)` ile aynı işi yapar |
| `.body(requestBody)` | Request body: `String`, `Map` veya POJO |
| `.log().all()` | İsteğin tamamını konsola basar. **Maskeleme yoktur** — token açık görünür |

### `.when()` — gönderme

`.get(path)` · `.post(path)` · `.put(path)` · `.patch(path)` · `.delete(path)`

İstek bu satırda gider; öncesindeki her şey sadece kurulumdur.

### `.then()` — doğrulama

| Metot | Ne işe yarar |
| --- | --- |
| `.statusCode(201)` | HTTP status'ü doğrular |
| `.contentType(ContentType.JSON)` | Yanıt tipini doğrular |
| `.header("Content-Type", containsString("json"))` | Response header'ını doğrular |
| `.body("title", equalTo("Rest Assured Test"))` | Gövdedeki alanı doğrular (GPath + Hamcrest matcher) |
| `.time(lessThan(2000L))` | Yanıt süresini doğrular |
| `.log().all()` | Yanıtın tamamını konsola basar |
| `.extract().response()` | Zinciri bitirip `Response` nesnesini verir |

### Çalışan tam örnek

```java
String requestBody = """
        {
          "title": "Rest Assured Test",
          "userId": 1
        }
        """;

Response response = given()
        .baseUri("https://dummyjson.com")
        .header("x-api-key", "12345")
        .contentType("application/json")
        .accept("application/json")
        .queryParam("limit", 10)
        .body(requestBody)
        .log().all()
    .when()
        .post("/posts/add")
    .then()
        .statusCode(201)
        .body("title", equalTo("Rest Assured Test"))
        .log().all()
        .extract().response();

assertTrue(response.jsonPath().getInt("id") > 0);
```

### DSL'de sık yapılan hatalar

| Hata | Ne olur |
| --- | --- |
| `Response r = given()....log().all();` | **Derlenmez.** `given()` zinciri `RequestSpecification` döndürür; `.when().post(...)` yoksa istek hiç gitmez |
| `Response r = ....then().statusCode(200);` | **Derlenmez.** `.then()` `ValidatableResponse` döndürür; `.extract().response()` ekle |
| `.body(...)` ile `.formParam(...)` birlikte | `IllegalStateException: You can either send form parameters OR body content in POST, not both!` |
| `.contentType("application/json")` + `.formParam(...)` | Form parametreleri urlencoded ister; JSON tipiyle sunucu gövdeyi çözemez |
| `.header("Authorization", ...)` sonra `.headers(Map.of("Authorization", ...))` | Aynı header iki kez eklenir; hangisinin gideceği garanti değil |
| `.pathParam("id", 5)` ama path'te `{id}` yok | Parametre kullanılmaz, sessizce yok sayılır |
| `.get("/users/:id")` | `:id` Rest Assured sözdizimi değil (Express/Postman yazımı); düz metin olarak gider. Doğrusu `{id}` |

### `ApiClient` karşılıkları

| Ham DSL | Bu projede |
| --- | --- |
| `.baseUri(url)` | `api.baseUrl(url)` |
| `.basePath(p)` | Yok — `baseUrl`'e dahil et |
| `.header(k, v)` / `.headers(map)` | `api.header(k, v)` / `api.headers(map)` |
| `.contentType(...)` / `.accept(...)` | Spec'te sabit: `application/json` |
| `.queryParam(k, v)` | `api.queryParams(Map.of(...))` |
| `.pathParam(k, v)` | `api.pathParams(Map.of(...))` |
| `.formParam(k, v)` | `api.formParams(Map.of(...))` |
| `.auth().oauth2(token)` | `api.bearerToken(token)` |
| `.body(x)` | `api.body(x)` veya `api.post(url, x)` |
| `.log().all()` | Yok — `response.prettyPrint()`; istek/yanıt zaten Allure'a maskeli eklenir |
| `.cookie(k, v)` | Yok |
| `.when().get(p)` | `api.get(p)` |
| `.then().statusCode(200)` | `assertEquals(200, response.statusCode())` |

Karar: **`ApiClient` varsayılan**. Ham DSL'i tek seferlik bir denemede ya da `ApiClient`'ta
karşılığı olmayan bir şey gerektiğinde (cookie, basic auth) kullan.

## 3. Yanıt bilgileri

```java
int statusCode = response.statusCode();                   // HTTP durum kodunu verir: 200, 201, 404
String statusLine = response.statusLine();                // Tam durum satırını verir: HTTP/1.1 200 OK
String contentType = response.contentType();              // İçerik tipini verir: application/json
long responseTime = response.time();                      // Yanıt süresini milisaniye olarak verir
long responseSeconds = response.timeIn(TimeUnit.SECONDS); // Yanıt süresini belirtilen birimde verir
```

## 4. Response body

```java
String rawBody = response.asString();                     // Response body'yi ham String olarak döndürür
String prettyBody = response.asPrettyString();            // Response body'yi formatlı String olarak döndürür
byte[] bodyBytes = response.asByteArray();                // Response body'yi byte dizisine dönüştürür
InputStream bodyStream = response.asInputStream();        // Response body'yi InputStream olarak döndürür
```

## 5. Konsola yazdırma

```java
response.print();                                         // Body'yi ham biçimde konsola basar, String döndürür
response.prettyPrint();                                   // Body'yi formatlı biçimde konsola basar, String döndürür
response.peek();                                          // Body'yi ham basar, aynı Response nesnesini döndürür
response.prettyPeek();                                    // Body'yi formatlı basar, aynı Response nesnesini döndürür
```

`print`/`prettyPrint` **String**, `peek`/`prettyPeek` **Response** döner — bu yüzden zincirin
ortasına yalnızca peek konur: `api.get("/books").prettyPeek().jsonPath().getList("$")`

## 6. JSON alanlarını okuma — `path()`

```java
int id = response.path("id");                             // Root seviyesindeki id alanını okur
String title = response.path("title");                    // Root seviyesindeki title alanını okur
String city = response.path("address.city");              // İç içe address nesnesindeki city alanını okur
int firstId = response.path("[0].id");                    // Root array içindeki ilk nesnenin id alanını okur
String firstTitle = response.path("[0].title");           // Root array içindeki ilk nesnenin title alanını okur
String firstReview = response.path("[0].reviews[0]");     // İlk nesnenin reviews dizisindeki ilk değeri okur
```

> `path()` dönüş tipini **atandığı yerden** çıkarır. Doğrudan `assertEquals` içine yazılınca tip
> belli olmadığı için cast gerekir: `assertEquals(1, (int) response.path("[0].id"));`

## 7. Tipi açıkça belirterek JSON okuma

```java
int id = response.jsonPath().getInt("id");                 // id alanını int olarak döndürür
String name = response.jsonPath().getString("name");       // name alanını String olarak döndürür
boolean active = response.jsonPath().getBoolean("active"); // active alanını boolean olarak döndürür
float price = response.jsonPath().getFloat("price");       // price alanını float olarak döndürür
double total = response.jsonPath().getDouble("total");     // total alanını double olarak döndürür
```

## 8. Liste işlemleri

```java
List<Object> items = response.jsonPath().getList("$");                    // Root seviyesindeki bütün array'i List olarak alır
List<String> titles = response.jsonPath().getList("title", String.class); // Bütün title değerlerini alır
List<Integer> ids = response.jsonPath().getList("id", Integer.class);     // Bütün id değerlerini alır
int itemCount = response.jsonPath().getList("$").size();                  // Root array içindeki eleman sayısını verir
```

Yanıt array değil nesne dönüyorsa kök `$` değil alan adıdır: `response.jsonPath().getList("products")`

## 9. Liste filtreleme (GPath)

```java
List<Integer> ids = response.jsonPath().getList("findAll { it.rating >= 4 }.id");             // Rating'i en az 4 olan kayıtların id'lerini alır
String author = response.jsonPath().getString("find { it.title == '1636' }.author");          // Başlığı 1636 olan ilk kaydın yazarını alır
List<String> authors = response.jsonPath().getList("findAll { it.title == '1636' }.author");  // Eşleşen tüm yazarları alır
```

`find` ilk eşleşmeyi, `findAll` hepsini verir. İfade Groovy'dir: içerideki string **tek tırnakla**
yazılır, dıştaki Java string'ini kapatmasın diye.

## 10. Map işlemleri

```java
Map<String, Object> address = response.jsonPath().getMap("address"); // address nesnesini Map olarak alır
Map<String, Object> firstItem = response.jsonPath().getMap("[0]");   // Root array içindeki ilk nesneyi Map olarak alır
```

## 11. Header işlemleri

```java
String contentTypeHeader = response.getHeader("Content-Type"); // Content-Type header değerini verir
String location = response.getHeader("Location");              // Location header değerini verir
String requestId = response.getHeader("X-Request-Id");         // Özel bir header değerini verir
Headers headers = response.getHeaders();                       // Response içindeki bütün header'ları verir
```

Header yoksa `getHeader(...)` `null` döner; `contains` çağırmadan önce varlığını doğrula.

## 12. Cookie işlemleri

```java
String session = response.getCookie("SESSION");               // SESSION cookie değerini verir
Map<String, String> cookies = response.getCookies();          // Bütün cookie'leri Map olarak verir
Cookie cookie = response.getDetailedCookie("SESSION");        // Cookie'nin domain, path ve expiry gibi detaylarını verir
Cookies detailedCookies = response.getDetailedCookies();      // Bütün cookie'leri detaylarıyla verir
```

> Detaylı cookie'nin tipi `io.restassured.http.Cookie`'dir. `DetailedCookie` diye bir sınıf
> Rest Assured 6.0.1'de yoktur; metot adındaki "detailed" yalnızca "değer değil, bütün alanlar"
> demektir.

## 13. Java nesnesine dönüştürme

```java
User user = response.as(User.class);                               // JSON response'u User nesnesine dönüştürür
List<User> users = response.as(new TypeRef<List<User>>() {});      // JSON array response'u User listesine dönüştürür
Book firstBook = response.jsonPath().getObject("[0]", Book.class); // Yalnızca ilk kaydı nesneye çevirir
```

Hedef tip Java 21 record'u olabilir:

```java
record Book(String title, String author, int rating) {}
```

Dönüşümü classpath'teki tek mapper olan **Gson** yapar. Küçük doğrulamalarda nesneye çevirmeye
gerek yoktur; `getList("title", String.class)` çoğu case'i tek satırda kapatır.

## 14. Rest Assured ile doğrudan doğrulama

```java
response.then().statusCode(200);                              // Status code'un 200 olduğunu doğrular
response.then().contentType(ContentType.JSON);                // Content-Type'ın JSON olduğunu doğrular
response.then().body("id", equalTo(1));                       // Body içindeki id alanının 1 olduğunu doğrular
response.then().body("[0].title", equalTo("1636"));           // İlk kaydın title değerini doğrular
response.then().header("Content-Type", containsString("application/json")); // Header değerini doğrular
response.then().time(lessThan(10_000L));                      // Response süresinin 10 saniyeden kısa olduğunu doğrular
```

Zincirlenebilir: `response.then().statusCode(200).body("[0].title", equalTo("1636"));`

## 15. JUnit ile karşılıkları

```java
assertEquals(200, response.statusCode());                          // Status code'un 200 olduğunu doğrular
assertTrue(response.contentType().startsWith("application/json")); // Response tipinin JSON olduğunu doğrular
assertEquals("1636", response.path("[0].title"));                  // İlk kitabın başlığını doğrular
assertNotNull(response.path("[0].author"));                        // İlk kitabın author alanının bulunduğunu doğrular
assertFalse(response.jsonPath().getList("$").isEmpty());           // Response listesinin boş olmadığını doğrular
assertTrue(response.time() < 10_000);                              // Response süresinin 10 saniyeden kısa olduğunu doğrular
```

İlk fail'de durmasın istiyorsan hepsini tek blokta topla:

```java
assertAll(
        () -> assertEquals(200, response.statusCode()),
        () -> assertEquals("1636", response.path("[0].title")),
        () -> assertTrue(response.time() < 10_000, "Yanit " + response.time() + " ms surdu"));
```

Mesaj son parametredir (`assertTrue(kosul, "neden")`) — fail çıktısını okunur yapan tek şey odur.

### Hangisini seç

| | `then()` | JUnit assert |
| --- | --- | --- |
| Okunurluk | Tek satırda zincir | Değer önce okunur, sonra doğrulanır |
| Hata mesajı | Hamcrest üretir | Kendin yazarsın |
| Bu projede | Hızlı deneme için | **Varsayılan** — Step Definition'ların tamamı böyle |

Bir testte ikisini karıştırma: fail çıktısı iki ayrı formatta üretilir, rapor okunmaz olur.

---

## 16. Import listesi

```java
import io.restassured.response.Response;
import io.restassured.http.ContentType;
import io.restassured.http.Cookie;
import io.restassured.http.Cookies;
import io.restassured.http.Headers;
import io.restassured.common.mapper.TypeRef;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;   // ham DSL kullanacaksan
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.jupiter.api.Assertions.*;
```

## 17. Sık takılınan yerler

| Belirti | Sebep / çözüm |
| --- | --- |
| `assertEquals(1, response.path("id"))` → *reference to assertEquals is ambiguous* | `path()` tipi çıkaramıyor, `int`/`long` overload'ları çakışıyor → `(int)` cast et |
| `getList("$")` boş veya hata veriyor | Yanıt array değil nesne → alan adıyla oku (`getList("products")`) |
| İkinci istek eski gövdeyi taşıyor sanılıyor | Taşımaz: gövde ve query gönderimden sonra temizlenir; header ve base url kalır |
| Relative path'te `IllegalStateException` | `api.base.url` boş → ya full URL ver ya `api.baseUrl(...)` çağır |
| Geçersiz token'dan sonra public endpoint de 401 | `bearerToken` senaryo boyunca kalır; okuma isteklerini token'dan önce yap |
| `given()...log().all()` `Response`'a atanamıyor | `.when().<metot>(path)` eksik; zincir henüz istek göndermedi |
| `You can either send form parameters OR body content` | `.body()` ve `.formParam()` aynı istekte; birini sil |
| `asInputStream()` ikinci okumada boş | Stream bir kez tüketilir; tekrar gerekiyorsa `asString()` kullan |
| Token repo'ya girecek | Secret dosyaya yazılmaz: `-Dapi.token=...` veya `API_TOKEN` ortam değişkeni |
