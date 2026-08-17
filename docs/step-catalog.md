# Adım katalogu

Hazır adımların tam listesi. Yeni bir senaryo yazmadan önce buraya bak — çoğu durumda Java yazmaya
gerek kalmaz.

Tüm metin parametreleri yer tutucu çözümünden geçer: `${ctx:anahtar}` ve `${config:anahtar}`.

---

## Mobil — genel adımlar

`stepdefinition/common/ElementStepDefinitions.java` · 16 adım

Gherkin'e **locator değil element adı** girer. Element adı `*Locators` sınıfındaki alan adıdır;
sayfa adı ise o sınıftaki `PAGE_NAME` değeridir. Eşleşme toleranslıdır: boşluk, alt çizgi ve
noktalama yok sayılır, büyük/küçük harf önemsizdir.

`* Click to element "VIEWS_MENU" in "Api Demos Page"` = `* Click to element "views menu" in "api_demos_page"`

### Aksiyon

| Adım |
| --- |
| `Click to element "<KEY>" in "<Page>"` |
| `Click to element with text "<text>"` |
| `Write "<text>" to element "<KEY>" in "<Page>"` |
| `Clear text of element "<KEY>" in "<Page>"` |
| `Scroll to element "<KEY>" and click in "<Page>"` |
| `Wait for element "<KEY>" in "<Page>"` |
| `Navigate back` |
| `Hide the keyboard` |

### Doğrulama

| Adım |
| --- |
| `Verify element "<KEY>" exists in "<Page>"` |
| `Verify element "<KEY>" not exists in "<Page>"` |
| `Check if element "<KEY>" has text "<text>" in "<Page>"` |
| `Check if element "<KEY>" contains text "<text>" in "<Page>"` |
| `Check element "<KEY>" is checked in "<Page>"` |
| `Check element "<KEY>" is not checked in "<Page>"` |
| `Toast message "<text>" should be visible` |

### Taşıma

| Adım |
| --- |
| `Save text of element "<KEY>" as "<name>" in "<Page>"` → sonra `${ctx:<name>}` |

### Örnek

```gherkin
Scenario: A planet is selected with generic steps
  * Click to element "VIEWS_MENU" in "Api Demos Page"
  * Scroll to element "SPINNER_OPTION" and click in "Api Demos Page"
  * Click to element "PLANET_DROPDOWN" in "Api Demos Page"
  * Click to element with text "Jupiter"
  * Check if element "PLANET_DROPDOWN_VALUE" has text "Jupiter" in "Api Demos Page"
```

> Gherkin `*` anahtar kelimesini destekler; Given/When/Then de kullanılabilir.

---

## Mobil — iş dili adımları

Çok adımlı akışlar ve formun iç tutarlılığını doğrulayan senaryolar Page Object üzerinden yürür.
Genel adımlar hız ve keşif için, iş dili adımları regresyon ve iletişim içindir.

`stepdefinition/mobile/ApiDemosStepDefinitions.java`

| Adım |
| --- |
| `the Api Demos home screen is visible` |
| `the user opens the Views menu` |
| `the Buttons option should be visible` |
| `the user opens the Spinner screen` |
| `the user selects "<planet>" from the planet dropdown` |
| `the selected planet should be "<planet>"` |
| `the user opens the Switches screen` |
| `the user taps the monitored switch` |
| `the monitored switch should be <on\|off>` |
| `the "<message>" toast should be visible` |

`stepdefinition/mobile/ControlsStepDefinitions.java`

| Adım |
| --- |
| `the Controls screen is open` |
| `the user types "<text>" into the text field` |
| `the text field should contain "<text>"` |
| `the user taps the first checkbox` |
| `the first checkbox should be <checked\|unchecked>` |
| `the user selects the second radio button` |
| `only the second radio button should be selected` |
| `the user taps the first toggle` |
| `the first toggle should be on and labelled "<label>"` |
| `the user selects "<planet>" from the Controls planet dropdown` |
| `the selected Controls planet should be "<planet>"` |
| `the enabled Save button should be clickable` |
| `the disabled Save button should not be clickable` |

---

## API — istek

`stepdefinition/api/ApiRequestStepDefinitions.java`

| Adım | Not |
| --- | --- |
| `the base url is "<url>"` | Yalnızca bu senaryo için; ortam ayarı etkilenmez |
| `the request headers:` | DataTable, 2 kolon |
| `the query params:` | DataTable — encoding Rest Assured'a bırakılır |
| `the path params:` | DataTable — URL'deki `{id}` yerine geçer |
| `the request body:` | DocString — gövde elle yazılır |
| `the request body from json "<classpath>"` | `testdata/json/...` |
| `the request body from csv "<classpath>" row <n>` | Başlık sayılmaz, 1'den başlar |
| `the request body from csv "<classpath>" where "<column>" is "<value>"` | Kolon eşleşmesi |
| `the request body from table:` | DataTable → düz JSON gövde |
| `I send <METHOD> to "<url>"` | GET/POST/PUT/PATCH/DELETE/HEAD/OPTIONS |

Adres üç biçimde verilebilir:

1. Relative path (`/posts/1`) → aktif ortamın `api.base.url` değeri kullanılır
2. `the base url is` ile senaryoya özel adres
3. Doğrudan full URL (`https://baska-servis.com/health`)

---

## API — yanıt

`stepdefinition/api/ApiResponseStepDefinitions.java`

| Adım | Not |
| --- | --- |
| `the response status should be <int>` |  |
| `the response time should be under <int> ms` |  |
| `the response field "<jsonPath>" should be "<value>"` | `[0].postId` gibi JsonPath |
| `the response field "<jsonPath>" should not be null` |  |
| `the response fields should be:` | DataTable, alan → beklenen değer |
| `I save response field "<jsonPath>" as "<name>"` | Sonra `${ctx:<name>}` |
| `I save the response to csv "<file>" with fields "<a,b,c>"` | `target/output/` altına yazar |

### Zincirleme örneği

```gherkin
When I send GET to "/posts/1"
Then the response status should be 200
And I save response field "id" as "postId"
And the request body from table:
  | title  | Updated from context |
  | userId | 7                    |
When I send PUT to "/posts/${ctx:postId}"
Then the response status should be 200
```

---

## Bilerek eklenmeyenler

| İstenen | Neden yok | Yerine |
| --- | --- | --- |
| `${random.email}`, `${today+3d}` | İlk sürümde kapsam dışı; ihtiyaç doğduğunda `Placeholders`'a tek bir kaynak olarak eklenir | Sabit veri veya CSV satırı |
| `${notNull}`, `${anyOf:a,b}` | Bunlar veri değil doğrulama davranışıdır; yer tutucuya gizlenirse hata mesajı anlamsızlaşır | `the response field "<x>" should not be null` |
| `@ByKey("Any element with text '*'")` gibi DSL | Tırnak ayrıştıran parser sessizce yanlış eşleşir | `Click to element with text "<text>"` |
| Tek senaryoda tüm CSV satırlarını dönen adım | Raporda tek satır görünür, hangi satırın patladığı kaybolur | `Scenario Outline` + `Examples` |
