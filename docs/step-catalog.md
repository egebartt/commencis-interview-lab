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

## Mobil adım yazma kuralı

Yeni ekran = 3 küçük dosya:

```
mobile/pages/LoginLocators.java                    →  static final By LOGIN = ...   (public değil)
mobile/pages/LoginPage.java                        →  public void login(String phone, String password)
stepdefinitions/mobile/LoginStepDefinitions.java   →  @When("the user logs in with {string} and {string}")
```

`MobileActions` içindeki teknik metotlar (`click`, `type`, `isVisible`, `scrollAndClick`,
`isChecked`, `selectByText`, `back`, `hideKeyboard`) yalnızca Page sınıflarından çağrılır; Step
Definition ve feature katmanına sızmaz. `back()` / `scrollDown()` gibi cihaz aksiyonları Page'de
iş diline sarılır (`returnToMenu()`), Step Definition'dan doğrudan çağrılmaz.

Değeri çalışma anında gelen locator'lar (dropdown seçeneği, liste satırı) sabit olarak yazılmaz;
Page `mobile.selectByText(...)` / `mobile.scrollAndClickText(...)` çağırır, locator'ı
`MobileActions.byText` üretir.

Feature'a locator veya element adı girmez. `* Click to element "X" in "Y"` tarzı generic adım
bilerek **yoktur**: senaryoyu iş dilinden UI script'ine çevirir ve string olduğu için yazım hatası
derlemede değil koşumda ortaya çıkar.
