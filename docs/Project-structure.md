# Commencis Interview Lab

Tek modüllü Java 21 / Maven test projesi: **Cucumber + Appium + Rest Assured**, sonuçlar
**Allure HTML** raporuna çıkar. Spring/Lombok yok, kod üretimi yok, static driver yok.

Testlerin kalıcı yeri `.feature` dosyalarıdır. Mülakatta hızlı case yazmak için ayrı bir
JUnit alanı vardır: `interview/InterviewMobile.java` (Appium) ve `interview/InterviewApi.java`
(Rest Assured) — prova bitince silinebilir.

| | |
| --- | --- |
| Java | 21 |
| Build | Maven 3.9.16 (wrapper ile gelir) |
| API | Rest Assured 6.0.1 |
| Mobile | Appium Java Client 10.1.1 + Selenium 4.42.0 |
| BDD | Cucumber 7.29.0 (JUnit Platform Engine + PicoContainer) |
| Rapor | Allure → `target/allure-report/index.html` |

Maven **dependency olmayan**, makineye ayrıca kurulması gereken araçlar: Appium Server 3.4.2,
UiAutomator2 Driver 7.5.2, Node.js >= 20.19.

> `pom.xml` içindeki `appium.java-client.version` (10.1.1) **Java kütüphanesidir**.
> **Appium Server** (3.4.2) ve **UiAutomator2 Driver** (7.5.2) ayrı ürünlerdir, npm ile kurulur.

---

## 1. Proje yapısı

Her klasörün tek bir görevi var. Bir ekran eklemek = **1 locators + 1 page + 1 step definition**
dosyası.

> **Step ≠ Step Definition.** `.feature` içindeki Gherkin cümlesi *step*'tir; onu karşılayan
> `@Given`/`@When`/`@Then` metodu *Step Definition*'dır. Java paketi bu yüzden `stepdefinitions`
> adını taşır, sınıflar `*StepDefinitions` ile biter.

```
src/test/java/com/commencis/interview/
  core/
    Config.java          config.properties okur (-D > ortam değişkeni > dosya)
    Driver.java          senaryo ömürlü Appium oturumu + capability'ler
    TestContext.java     senaryo boyunca taşınan değerler + ${ctx:} / ${config:}
    Redaction.java       rapora giden metinlerdeki secret'ları maskeler
  api/
    ApiClient.java       Rest Assured oturumu: istek kur → gönder → son yanıt (+ Allure eki)
  mobile/
    actions/
      MobileActions.java    TEK düşük seviye Appium katmanı: click/type/wait/scroll/back/keyboard
    locators/
      ApiDemosLocators.java ekranın By sabitleri; başka hiçbir şey yok
      ControlsLocators.java · InterviewLocators.java
    pages/
      BasePage.java         ince taban: Page'lere MobileActions verir, başka iş yapmaz
      ApiDemosPage.java     ekranın iş akışı metotları (assertion yok)
      ControlsPage.java · InterviewPage.java
  stepdefinitions/
    api/ApiStepDefinitions.java            tüm API adımları (istek + doğrulama)
    mobile/ApiDemosStepDefinitions.java    @Given/@When/@Then — sadece Page çağrısı + assertion
    mobile/ControlsStepDefinitions.java
    mobile/InterviewMobileStepDefinitions.java
  hooks/
    Hooks.java           @Before/@After: driver aç/kapat, screenshot, rapor metadata
    Bar2CucumberHooks.java     Bar2 Report plugin üretir — elle değiştirilmez
    Bar2ReportScreenshot.java
  runner/
    CucumberRunnerTest.java    feature'ları çalıştıran tek giriş noktası
  interview/
    InterviewMobile.java mülakat için mobil JUnit alanı — Appium reference testi (silinebilir)
    InterviewApi.java    mülakat için API JUnit alanı — cihaz istemez (silinebilir)
  frameworktest/
    ConfigTest · TestContextTest · RedactionTest            altyapının kendi testleri;
    DynamicLocatorTest                                      cihaz istemez, her koşumda çalışır
    ApiReportCanaryTest   uçtan uca: secret gönderilir ama rapora yazılmaz

src/test/resources/
  config.properties      TEK ayar dosyası
  features/api/posts.feature
  features/mobile/api_demos.feature · controls.feature · interview_mobile.feature
  testdata/create-post.json
  junit-platform.properties   glue / tag / plugin
  allure.properties
```

### Akış

```
Mobil:   .feature → stepdefinitions/mobile/ → mobile/pages/ → MobileActions → Driver → Appium
                                              ↑ locator'ı yalnızca Page okur: *Locators
API:     .feature → ApiStepDefinitions → ApiClient → Rest Assured → HTTP
Mülakat: @Test    → InterviewMobile → MobileActions veya Page   (aynı altyapı)
         @Test    → InterviewApi    → ApiClient
```

Her katmanın cevapladığı soru:

| Katman | Sorusu |
| --- | --- |
| `.feature` (step) | Senaryo ne anlatıyor? |
| `stepdefinitions/` | Bu cümle hangi iş akışını çağırır, sonuç doğru mu? |
| `mobile/pages/*Page` | Bu iş ekranda nasıl yapılır? |
| `mobile/locators/*Locators` | O eleman ekranda nerede? |
| `mobile/actions/MobileActions` | Elemana teknik olarak nasıl tıklanır/beklenir? |
| `api/ApiClient` | İstek nasıl kurulur ve gönderilir? (assertion yok) |
| `Hooks` + `Driver` | Oturum ne zaman açılıp kapanır? |

### Cucumber'ın "BaseTest"i nerede?

Cucumber'da kalıtım yerine **hook + scenario-scoped injection** kullanılır. Karşılıkları:

| JUnit dünyası | Cucumber karşılığı |
| --- | --- |
| `@BeforeEach` driver açar | `Hooks.startDriver()` — `@Before("@mobile")` |
| `@AfterEach` driver kapatır | `Hooks.quitDriver()` — `@After("@mobile")` |
| Base sınıfın `driver` alanı | `core/Driver` — PicoContainer her senaryoya bir tane verir |
| Base sınıfın helper metotları | `mobile/actions/MobileActions` |

PicoContainer zinciri kurar; hiçbir yerde `new` veya static yoktur:

```
Hooks(Driver)  ─────────────────────────────────────────────────┐
                                                                ├─ aynı senaryoda AYNI Driver
ApiDemosStepDefinitions(ApiDemosPage) → ApiDemosPage(MobileActions) → MobileActions(Driver) ─┘
```

`@api` senaryosu cihaz açmaz: driver yalnızca `@mobile` tag'inde başlatılır ve `ApiClient` ilk
istekte kurulur.

---

## 2. Koşum

Tek komut kalıbı var: **`.\mvnw.cmd clean verify`**. Ne çalışacağını tag belirler.

| Komut | Ne çalışır | Cihaz |
| --- | --- | --- |
| `.\mvnw.cmd clean verify "-Dcucumber.filter.tags=@api"` | API senaryoları | Hayır |
| `.\mvnw.cmd clean verify "-Dcucumber.filter.tags=@mobile and @interview"` | Mülakatın mobil senaryosu | **Evet** |
| `.\mvnw.cmd clean verify "-Dit.test=InterviewApi#interViewApi"` | Mülakatın API testi | Hayır |
| `.\mvnw.cmd clean verify "-Dit.test=InterviewMobile#interViewMobile"` | Mülakatın mobil testi | **Evet** |
| `.\mvnw.cmd clean verify "-Dit.test=InterviewMobile#mobileReferences"` | Referans testi — ApiDemos APK ister | **Evet** |

Varsayılan tag filtresi `junit-platform.properties` içinde `@mobile or @api`; filtresiz
`clean verify` ApiDemos senaryolarını da çalıştırmaya kalkar ve repodaki APK o uygulama
olmadığı için düşer. Yukarıdaki tag'li komutları kullan.

PowerShell'de `-D...` içeren argümanları **tırnak içine al**.

Ayar geçmek (dosyayı değiştirmeden):

```powershell
.\mvnw.cmd clean verify "-Dcucumber.filter.tags=@mobile" -Dandroid.udid=emulator-5556
```

Adımların glue ile eşleştiğini cihaz açmadan kontrol etmek:

```powershell
.\mvnw.cmd clean verify "-Dcucumber.filter.tags=@mobile" "-Dcucumber.execution.dry-run=true"
```

> Dry-run adımları çalıştırmaz, hook tetiklemez — yalnızca **glue eşleşmesini** kanıtlar.

**Bilinen sınırlama:** `cucumber.filter.tags` hiçbir senaryoyla eşleşmezse Cucumber senaryoları
*skip* eder, "sıfır test" üretmez; build yeşil kalır. Tag değiştirdiğinde çıktıdaki **Skipped
sayısına bak**.

---

## 3. Ayarlar — tek dosya

Her şey `src/test/resources/config.properties` içinde. Ortam/cihaz başına ayrı dosya **yoktur**:
değişen değer komut satırından geçirilir, böylece "hangi dosya yüklendi" sorusu hiç doğmaz.

```
Öncelik:  -Dkey=değer  >  ORTAM_DEĞİŞKENİ  >  config.properties
```

Ortam değişkeni adı: nokta → alt çizgi, büyük harf (`api.token` → `API_TOKEN`).

| Anahtar | Açıklama |
| --- | --- |
| `platform` | `android` veya `ios`. Hem driver'ı hem platforma bağlı locator seçimini belirler |
| `api.base.url` | Opsiyonel: doluysa relative path (`/posts/1`), boşsa senaryoda full URL |
| `api.timeout` | Bağlantı/okuma zaman aşımı (saniye) |
| `api.token` | **Boş bırakılır** — `-Dapi.token=...` veya `API_TOKEN` ile geçilir |
| `appium.url` | Dışarıdan başlatılan Appium server adresi |
| `element.timeout` | Tüm explicit wait'lerin üst sınırı (saniye) |
| `android.udid` | `adb devices -l` çıktısındaki seri numara |
| `android.app` | Doluysa APK kurulur; relative yol proje köküne göre çözülür. Boşsa `android.app.package` + `android.app.activity` |
| `ios.udid` / `ios.bundle.id` | `xcrun xctrace list devices` çıktısı / bundle id |

Token, parola gibi değerler repository'ye yazılmaz. Rapora giden kopyada maskeleme tek yerden
yapılır (`core/Redaction.java`) ve isim karşılaştırması normalize edilir — `access_token`,
`accessToken` ve `Access-Token` aynı sayılır:

| Nerede | Ne maskelenir |
| --- | --- |
| Allure request eki | Gizli header değerleri, URL'deki kullanıcı bilgisi (`user:pass@host`), gizli query parametreleri (URL-encode edilmiş anahtar dahil), JSON gövdedeki gizli alanlar |
| Allure response eki | JSON gövdedeki gizli alanlar |
| `Map` / POJO gövde | `toString()` ile yazılmaz; önce JSON'a çevrilip maskelenir (serialize edilemezse hiç yazılmaz) |
| Gizli adlı nesne (`"auth": {...}`) | Tamamı maskelenir, içine inilmez — `"value"` gibi genel adlı bir alt alan açıkta kalmasın |
| Environment paneli | `api.base.url` ve `appium.url` içindeki kullanıcı bilgisi |
| `byte[]` / `InputStream` gövde | İçerik rapora **hiç yazılmaz** (stream okumak gönderilecek veriyi tüketirdi) |
| Assertion hata mesajı | Gövde mesaja konmaz; mesaj Allure ekine yönlendirir |

**Gönderilen istek ve alınan `Response` değişmez**, yalnızca rapor kopyası maskelenir.
`RedactionTest` fonksiyonları tek tek doğrular; `ApiReportCanaryTest` gerçek bir HTTP isteği atıp
hem secret'ın gönderildiğini hem `target/allure-results` altındaki eklerde bulunmadığını doğrular.

---

## 4. Yeni test eklemek

### 1) Locators — `mobile/locators/`

Locator'lar Page'den **ayrı bir pakette** durur: Page yalnızca davranış listesi olarak okunur,
ekranın elemanı değiştiğinde tek dosya güncellenir.

```java
public final class LoginLocators {               // Page ayri pakette, public olmali

    private static final String ID = "com.example:id/";

    public static final By PHONE    = AppiumBy.id(ID + "phone");
    public static final By PASSWORD = AppiumBy.id(ID + "password");
    public static final By LOGIN    = AppiumBy.id(ID + "loginButton");

    private LoginLocators() {
    }
}
```

Sınıf ve alanlar `public` değildir; Page'ler aynı pakette olduğu için erişir, `stepdefinitions`
başka pakette olduğu için **derleyici seviyesinde** erişemez. Ayrı bir `locators` paketi açmak
sınıfı `public` yapmayı zorunlu kılardı ve bu sınırı konvansiyona düşürürdü.

Aynı elemanın Android/iOS locator'ı farklıysa platforma göre seçilir:

```java
static final By LOGIN = Config.isAndroid()
        ? AppiumBy.id("com.example:id/loginButton")
        : AppiumBy.accessibilityId("loginButton");
```

Locator seçim önceliği: **accessibilityId > id > UiAutomator selector > XPath**.
Değeri çalışma anında gelen elemanlar (dropdown seçeneği, liste satırı) sabit locator olarak
yazılmaz; `mobile.selectByText(...)` / `mobile.scrollAndClickText(...)` çağrılır, locator'ı
`MobileActions` üretir.

### 2) Page — `mobile/pages/`

```java
public class LoginPage extends BasePage {

    public LoginPage(MobileActions mobile) {   // PicoContainer bu constructor'ı kullanır
        super(mobile);
    }

    public void login(String phone, String password) {
        mobile.clearSendKeys(PHONE, phone);
        mobile.clearSendKeys(PASSWORD, password);
        mobile.click(LOGIN);
    }

    public boolean isLoginButtonVisible() {
        return mobile.isVisible(LOGIN);
    }
}
```

Locator'lar tek satırlık static import ile gelir: `import static ...pages.LoginLocators.LOGIN;`

Page **assertion yapmaz**: "ekranda bu iş nasıl yapılır"ı bilir, "doğru mu"yu Step Definition
söyler. `back()`, `scrollDown()`, `hideKeyboard()` gibi teknik aksiyonlar da Step Definition'dan
doğrudan çağrılmaz; Page içinde iş diline sarılır:

```java
public void returnToMenu() {
    mobile.back();
}
```

### 3) Step Definition — `stepdefinitions/mobile/`

```java
public class LoginStepDefinitions {

    private final LoginPage loginPage;

    public LoginStepDefinitions(LoginPage loginPage) {
        this.loginPage = loginPage;
    }

    @When("the user logs in with {string} and {string}")
    public void login(String phone, String password) {
        loginPage.login(phone, password);
    }

    @Then("the login button should be visible")
    public void loginButtonIsVisible() {
        assertTrue(loginPage.isLoginButtonVisible(), "Login butonu görünmedi");
    }
}
```

Step Definition'da **locator ve driver bulunmaz**: yalnızca Page çağrısı ve assertion.

### 4) Feature — `resources/features/`

```gherkin
@mobile
Feature: Login

  Scenario: The user logs in
    When the user logs in with "5551112233" and "secret"
    Then the login button should be visible
```

Sorumluluk sınırı için §1'deki katman tablosuna bak.

API tarafında endpoint başına sınıf yazılmaz: adres, gövde ve parametreler senaryodan gelir.
Hazır adımlar: [docs/step-catalog.md](step-catalog.md)
Response okuma ve doğrulama: [docs/api-cheatsheet.md](api-cheatsheet.md)

### Neden generic adım yok

`* Click to element "VIEWS_MENU" in "Api Demos Page"` gibi bir adım bilerek **yoktur**. Feature
dosyasını iş dilinden UI script'ine çevirir, element ve sayfa adı string olduğu için yazım hatası
derlemede değil koşumda patlar, ve karşılığında Page Object'in zaten verdiğinden fazlasını vermez.
Kalıcı senaryolar iş dili adımlarıyla yazılır: `When the user opens the Views menu`.

---

## 5. Rapor

```powershell
.\mvnw.cmd clean verify
start target\allure-report\index.html
```

Rapor `post-integration-test` fazında üretilir; **test fail olsa da HTML oluşur**, fail eden test
build'i yine kırar. Raporda ne var:

- Her HTTP isteği/yanıtı (maskeli), süre ve status
- Mobil senaryoların son/hata ekran görüntüsü
- Environment paneli: `platform`, `api.base.url`, `appium.url`

---

## 6. Kurulum (Windows 11 / PowerShell)

```powershell
$env:JAVA_HOME = "C:\Users\bartu\.jdks\ms-21.0.12"
```

Makinede ne var ne yok görmek için (hiçbir şey kurmaz, sadece rapor eder):

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\preflight.ps1
```

### Android SDK

1. Android Studio → **Settings → Languages & Frameworks → Android SDK**
2. **SDK Platforms**: en az bir platform (örn. Android 14 / API 34)
3. **SDK Tools**: **Android SDK Platform-Tools** + **Android Emulator**

```powershell
$env:ANDROID_HOME = "$env:LOCALAPPDATA\Android\Sdk"; $env:PATH = "$env:ANDROID_HOME\platform-tools;$env:ANDROID_HOME\emulator;$env:PATH"
```

### Appium

PowerShell'de `npm`/`appium` yerine **`npm.cmd`** ve **`appium.cmd`** kullan — `.ps1` shim'leri
execution policy nedeniyle bloklanabilir.

```powershell
npm.cmd install -g appium@3.4.2
```

```powershell
appium.cmd driver install uiautomator2@7.5.2
```

Server bu proje tarafından başlatılmaz; ayrı bir terminalde açık kalmalı:

```powershell
appium.cmd
```

```powershell
Invoke-RestMethod http://127.0.0.1:4723/status
```

### Cihaz

```powershell
emulator -list-avds
```

```powershell
adb devices -l
```

Çıktıdaki ilk sütun `android.udid` değeridir (örn. `emulator-5554`).

Gerçek Redmi/Xiaomi cihaz için: **Ayarlar → Telefon hakkında → MIUI sürümü**ne 7 kez dokun, sonra
**Geliştirici seçenekleri**nde *USB hata ayıklama*, *USB ile yükleme* ve *USB hata ayıklama (Güvenlik
ayarları)* açık olmalı; USB modu *MTP*.

### APK

Mülakat senaryosunun uygulaması (Sauce Labs My Demo App) repo içinde: `apps/mda-2.2.0-25.apk`.
`android.app=apps/mda-2.2.0-25.apk` relative yolu `core/Driver.java` tarafından proje köküne
göre çözülür, indirme veya yol düzenleme gerekmez.

Referans testlerinin kullandığı ApiDemos uygulaması repoda değildir; gerekirse
<https://github.com/appium/android-apidemos/releases> adresinden indirilip `android.app`
anahtarına yazılır.

Komut kopyaları: [appium-cheatsheet.md](appium-cheatsheet.md)

---

## 7. Mülakat dosyası

`interview/` altında iki dosya vardır ve projede hiçbir şey onlara bağlı değildir; prova bitince
silinir. Sınıf adları bilerek `*Test` ile bitmez, böylece varsayılan koşumda çalışmazlar:

| Dosya | İçerik |
| --- | --- |
| `InterviewMobile.java` | Appium: `mobileReferences()` tek ekranda text kontrolü, tıklama, dropdown, radio, checkbox, toggle ve swipe'ı bir arada gösterir. Locator'lar sınıfın başındadır — mülakatta değişen tek yer orasıdır. |
| `InterviewApi.java` | Rest Assured: `apiReferences()` header/query/path/form, JsonPath ve assertion kalıplarını gösterir. Cihaz istemez. |

```powershell
.\mvnw.cmd clean verify "-Dit.test=InterviewMobile#mobileReferences"
.\mvnw.cmd clean verify "-Dit.test=InterviewApi#apiReferences"
```

IntelliJ'de tek testi sağ tık → Run ile çalıştırmak daha hızlıdır; terminal koşumu asıl olarak
rapor üretmek içindir.

---

## 8. Doğrulama durumu

| Ne | Durum |
| --- | --- |
| `mvnw clean verify` | ✅ 46 test, 0 fail — 27 framework testi + 12 API senaryosu geçti, 7 mobil senaryo tag filtresi nedeniyle **skipped** |
| Allure HTML | ✅ `target/allure-report/index.html` üretildi |
| `mvnw clean verify "-Dit.test=InterviewApi#<api metotları>"` | ✅ 3 test, 0 fail |
| Glue eşleşmesi (dry-run, `@api or @mobile`) | ✅ 19 senaryo, 0 skipped, undefined step yok |
| `mvnw verify "-Dit.test=InterviewMobile#mobileReferences"` | ✅ 1 test, 0 fail — emulator-5554 + Appium 3.6.0 üzerinde koşuldu |
| Mobil `.feature` senaryoları uçtan uca | ⛔ Ayrıca koşulmadı |

> Skipped ≠ passed: tag filtresi dışında kalan senaryolar çalışmaz, build yine yeşil kalır.
