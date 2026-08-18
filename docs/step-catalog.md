# Adım katalogu

Hazır adımların tam listesi. Yeni senaryo yazmadan önce buraya bak — API tarafında çoğu durumda
Java yazmaya gerek kalmaz.

Terim ayrımı: `.feature` içindeki Gherkin cümlesi **step**, onu karşılayan `@Given`/`@When`/`@Then`
metodu **Step Definition**'dır. Aşağıdaki tablolarda soldaki sütun step, başlıktaki dosya o
step'lerin Step Definition'larıdır.

Tüm metin parametreleri yer tutucu çözümünden geçer: `${ctx:anahtar}` ve `${config:anahtar}`.

---

## API — `stepdefinitions/api/ApiStepDefinitions.java`

Endpoint başına sınıf yoktur; adres, gövde ve parametreler senaryodan gelir.

### İstek

| Adım | Not |
| --- | --- |
| `Given the base url is "<url>"` | Yalnızca bu senaryo için; config dosyası etkilenmez |
| `Given the request headers:` | DataTable, 2 kolon — senaryo boyunca yaşar |
| `Given the query params:` | DataTable — encoding Rest Assured'a bırakılır |
| `Given the request body:` | DocString — gövde senaryonun içine yazılır |
| `Given the request body from file "<classpath>"` | `testdata/create-post.json` |
| `When I send <GET\|POST\|PUT\|PATCH\|DELETE> to "<url>"` | Relative path veya full URL |

Gövde ve query parametreleri **gönderimden sonra temizlenir**; header'lar ve base url senaryo
boyunca kalır.

### Doğrulama

| Adım | Not |
| --- | --- |
| `Then the response status should be <int>` | |
| `Then the response time should be under <int> ms` | |
| `Then the response field "<jsonPath>" should be "<value>"` | `id`, `[0].postId`, `data.user.name` |
| `Then the response field "<jsonPath>" should not be null` | |
| `Then the response fields should be:` | DataTable — alan/beklenen değer |
| `Then I save response field "<jsonPath>" as "<name>"` | Sonra `${ctx:<name>}` |

### Örnek — zincirleme

```gherkin
Scenario: A value from the first response is used in the second request
  When I send GET to "/posts/1"
  Then the response status should be 200
  And I save response field "id" as "postId"
  Given the request body:
    """
    { "title": "Updated from context", "userId": 7 }
    """
  When I send PUT to "/posts/${ctx:postId}"
  Then the response status should be 200
```

### Veri odaklı koşum

Ayrı bir CSV/Excel katmanı yoktur; Cucumber'ın kendi `Examples` tablosu kullanılır:

```gherkin
Scenario Outline: Posts are fetched for several ids
  When I send GET to "/posts/<id>"
  Then the response field "userId" should be "<userId>"

  Examples:
    | id | userId |
    | 1  | 1      |
    | 12 | 2      |
```

---

## Mobil — `stepdefinitions/mobile/ApiDemosStepDefinitions.java`

| Adım |
| --- |
| `Given the Api Demos home screen is visible` |
| `When the user opens the Views menu` |
| `Then the Buttons option should be visible` |
| `When the user opens the Spinner screen` |
| `When the user selects "<planet>" from the planet dropdown` |
| `Then the selected planet should be "<planet>"` |
| `When the user opens the Switches screen` |
| `When the user taps the monitored switch` |
| `Then the monitored switch should be <on\|off>` |
| `Then the "<message>" toast should be visible` |

## Mobil — `stepdefinitions/mobile/ControlsStepDefinitions.java`

| Adım |
| --- |
| `Given the Controls screen is open` |
| `When the user types "<text>" into the text field` |
| `Then the text field should contain "<text>"` |
| `When the user taps the first checkbox` |
| `Then the first checkbox should be <checked\|unchecked>` |
| `When the user selects the second radio button` |
| `Then only the second radio button should be selected` |
| `When the user taps the first toggle` |
| `Then the first toggle should be on and labelled "<label>"` |
| `When the user selects "<planet>" from the Controls planet dropdown` |
| `Then the selected Controls planet should be "<planet>"` |
| `Then the enabled Save button should be clickable` |
| `Then the disabled Save button should not be clickable` |

---

## Generic adım — `stepdefinitions/common/GenericElementStepDefinitions.java`

Sayfaya özel adım yazmadan tek bir elemana dokunmak gerektiğinde:

| Adım |
| --- |
| `* Click to element "<KEY>" in "<PAGE_NAME>"` |

```gherkin
Given the Api Demos home screen is visible
* Click to element "VIEWS_MENU" in "Api Demos Page"
Then the Buttons option should be visible
```

- `<KEY>` = Page'in `namedElements()` metodunda kayıtlı ad, `<PAGE_NAME>` = o Page'in `PAGE_NAME` değeri.
- Akış: `GenericElementStepDefinitions → CommonPage → PageElementCatalog → ElementActions`.
- Gherkin'deki `*` wildcard değil; Given/When/Then yerine kullanılan nötr anahtar kelimedir.
- **Locator'ın sahibi yine Page'dir.** Selector yalnızca Page içindeki `private static final By`
  sabitinde tanımlıdır, katalogda ikinci kez yazılmaz. Katalog sadece key'i çözer.
- Generic kullanıma açılan elemanın key'i Page'in `namedElements()` metodunda string olarak
  açıkça kaydedilir. Reflection veya dosya/package taraması yok.
- Yeni sayfa eklerken iki kayıt: (1) elemanlar Page'in `namedElements()` metoduna,
  (2) Page'in kendisi `PageElementCatalog.index()` içine.
- Eksik veya yanlış key derleme zamanında değil **koşumda** anlaşılır.
- Step Definition `By` tipini hiç görmez; ad çözümü `CommonPage` içinde yapılır.

**Bu ana yol değildir.** Hız ve keşif içindir: senaryo iş anlatmaz, UI script'ine döner. Kalıcı
senaryolar Page Object üzerinden iş dili adımlarıyla yazılır.

---

## Mobil adım yazma kuralı

Yeni ekran = 2 küçük dosya:

```
mobile/pages/LoginPage.java                        →  private static final By LOGIN = ...
                                                      public void login(String phone, String password)
stepdefinitions/mobile/LoginStepDefinitions.java   →  @When("the user logs in with {string} and {string}")
```

`ElementActions` içindeki teknik metotlar (`click`, `type`, `isVisible`, `scrollAndClick`,
`isChecked`, `selectByText`, `byText`, `toast`) yalnızca Page sınıflarından çağrılır; Step
Definition ve feature katmanına sızmaz.

Değeri çalışma anında gelen locator'lar (dropdown seçeneği, liste satırı) katalogdan geçmez;
Page `element.selectByText(...)` / `element.scrollAndClickText(...)` çağırır, locator'ı
`ElementActions` üretir.
