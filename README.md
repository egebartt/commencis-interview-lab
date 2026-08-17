# Commencis Interview Lab

Tek modüllü Java 21 / Maven test projesi. **Cucumber + Appium + Rest Assured** mimarisi; sonuçlar
**Allure HTML** raporuna çıkar. Spring/Lombok yok.

Test senaryolarının kalıcı yeri **`.feature` dosyalarıdır**. JUnit `@Test` yalnızca `live/` altında,
canlı kodlama sırasında hızlı case yazmak için bir adapter olarak durur; ikisi de aynı altyapıyı
kullanır.

```
live/LiveCodingTest  ─┐
                      ├─  Config · ApiClient · DriverManager · ElementActions · LocatorRegistry
Cucumber steps       ─┘
```

| | |
| --- | --- |
| Java | 21 |
| Build | Maven 3.9.16 (wrapper ile gelir) |
| API | Rest Assured 6.0.1 |
| Mobile | Appium Java Client 10.1.1 + Selenium 4.42.0 |
| BDD | Cucumber 7.29.0 (JUnit Platform Engine + PicoContainer) |
| CSV | Apache Commons CSV 1.12.0 |
| Rapor | Allure → `target/allure-report/index.html` |

Maven **dependency olmayan**, makineye ayrıca kurulması gereken araçlar:

| Araç | Sürüm |
| --- | --- |
| Appium Server | 3.4.2 |
| UiAutomator2 Driver | 7.5.2 |
| Node.js | >= 20.19 |
| npm | >= 10 |

> `pom.xml` içindeki `appium.java-client.version` (10.1.1) **Appium Java Client** sürümüdür — yani
> testlerin kullandığı Java kütüphanesi. **Appium Server** (3.4.2) ve **UiAutomator2 Driver** (7.5.2)
> ayrı ürünlerdir, npm ile kurulur ve bağımsız versiyonlanır.

---

## 1. Proje yapısı

```
src/test/java/com/commencis/interview/
  core/
    config/
      Config.java              katmanlı ayar okuma (-D > env > device > environment > default)
      MobilePlatform.java      ANDROID / IOS — tek kaynak: mobile.platform
    context/
      ScenarioContext.java     senaryo boyunca taşınan değerler (${ctx:...})
      ApiContext.java          kurulan istek + son yanıt (yalnızca durum tutar)
    data/
      JsonData.java            classpath JSON dosyasını okur
      CsvData.java             CSV okur, satır seçer, düz satırı JSON gövdeye çevirir
      CsvOutput.java           yanıtı CSV'ye yazar (target/output)
      Placeholders.java        ${ctx:key} ve ${config:key} çözümü
    report/
      AllureAttachments.java   request/response/screenshot ekleri (maskeli)
      AllureEnvironment.java   rapordaki Environment paneli
    security/
      SensitiveHeaders.java    gizli header listesi — log ve rapor aynı kaynağı kullanır
  api/
    ApiClient.java             tek send() + get/post/put/patch/delete, query/path param, assertion yok
    RequestSpecFactory.java    ortak Rest Assured yapılandırması
    ApiRequestException.java   geçerli Response alınamadığında hata tipini söyler
  mobile/
    driver/
      DriverManager.java       senaryo/test ömürlü driver sahibi (strict + lazy mod)
      MobileDriver.java        capability'leri kurar, AndroidDriver veya IOSDriver döner
    element/
      ElementActions.java      tüm düşük seviye aksiyonlar (By ile ve sayfa adı + element adı ile)
    locator/
      Locators.java            işaretleyici arayüz
      LocatorRegistry.java     "Api Demos Page" + "VIEWS_MENU" → By
      DynamicLocators.java     metinden üretilen locator'lar
      PlatformBy.java          aynı elemanın Android/iOS locator'ı farklıysa seçer
      ApiDemosLocators.java    PAGE_NAME + public static final By
      ControlsLocators.java
    page/
      BasePage.java            ElementActions'ı taşır; tek base page
      ApiDemosPage.java        iş akışı metotları, locator tutmaz
      ControlsPage.java
  hooks/
    MobileHooks.java           @mobile driver aç/kapat + Bar2 + Allure screenshot
    ReportHooks.java           koşum metadata'sı
    Bar2CucumberHooks.java     Bar2 Report plugin tarafından üretilir — elle değiştirilmez
    Bar2ReportScreenshot.java
  stepdefinition/
    common/ElementStepDefinitions.java    16 genel mobil adım (sayfa adı + element adı)
    api/ApiRequestStepDefinitions.java    url, header, query, path, body, gönderim
    api/ApiResponseStepDefinitions.java   status, alan, süre, context'e kaydet, CSV'ye yaz
    mobile/ApiDemosStepDefinitions.java   iş dili adımları
    mobile/ControlsStepDefinitions.java
  live/
    BaseTest.java              tek JUnit base — driver lazy açılır
    LiveCodingTest.java        mülakat çalışma alanı (@Tag("api") / @Tag("mobile"))
  frameworktest/               framework'ün kendi birim testleri (@Tag("unit"))
    ConfigTest · CsvDataTest · CsvOutputTest · LocatorRegistryTest
    PlaceholdersTest · SensitiveHeadersTest · ApiClientParamsTest · ApiErrorReportingTest
  runner/
    CucumberRunnerTest.java    feature'ları çalıştıran tek runner (@Suite, içinde @Test yok)

src/test/resources/
  config/
    config.properties          ortam/cihaz bağımsız varsayılanlar
    env/{test,prep}.properties         -Denvironment=<ad>
    device/{android-emulator,android-real,ios-simulator}.properties   -Ddevice=<ad>
  features/
    api/posts.feature
    mobile/api_demos.feature · mobile/controls.feature
  testdata/
    json/create-post.json
    csv/posts.csv
  junit-platform.properties    Cucumber glue / tag / plugin ayarları
  allure.properties
```

### Katman sırası

```
.feature  →  stepdefinition/  →  page/ (iş akışı)  →  ElementActions  →  driver
.feature  →  stepdefinition/common (genel adım)    →  ElementActions  →  driver
.feature  →  stepdefinition/api   →  ApiContext    →  ApiClient       →  HTTP
@Test     →  live/BaseTest        →  ElementActions / ApiClient
```

### Neden `BaseTest` sadece JUnit'te var

Cucumber'da yaşam döngüsünün karşılığı `@Before/@After` hook'ları, durumun karşılığı da
senaryo ömürlü context nesneleridir; kalıtım yerine PicoContainer injection kullanılır. `BaseTest`
sadece `live/` altındaki JUnit adapter'ı içindir ve driver'ı **lazy** açar — bu yüzden aynı taban
hem API hem mobil hızlı testlere hizmet eder, API testi Appium aramaz.

### `DriverManager`'ın iki modu

| Mod | Kim kullanır | Davranış |
| --- | --- | --- |
| strict (public no-arg constructor) | Cucumber / PicoContainer | Driver yalnızca `MobileHooks` içindeki `@Before("@mobile")` ile açılır. Tag'i eksik bir senaryo mobil adım çağırırsa sessizce cihaz açmak yerine açık hata verir. |
| lazy (`DriverManager.lazy()`) | `live/BaseTest` | İlk UI erişiminde driver açılır. |

### Locator ve aksiyon ayrımı

`*Locators` sınıfları yalnızca `PAGE_NAME` ve `public static final By` alanları tutar; aksiyon
içermez. `ElementActions` yalnızca aksiyon tutar; locator tanımlamaz. `LocatorRegistry` ikisini
isimle birleştirir.

Kayıt **açık listedir** (`LocatorRegistry.PAGES`), dosya sistemi taranmaz — koşum jar/CI ortamında
da aynı davranır. Liste derleme zamanı sabitleriyle kurulduğu için bir sayfanın locator alanları
**yalnızca o sayfa ilk kez sorgulandığında** okunur; bozuk tek bir locator sınıfı tüm koşumu
düşürmez ve hata mesajı hangi sayfa olduğunu söyler.

---

## 2. Kurulum (Windows 11 / PowerShell)

```powershell
$env:JAVA_HOME = "C:\Users\bartu\.jdks\ms-21.0.12"
```

Makinede ne var ne yok görmek için (hiçbir şey kurmaz, sadece rapor eder):

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\preflight.ps1
```

### Android SDK

1. Android Studio kur → **Settings → Languages & Frameworks → Android SDK**
2. **SDK Platforms**: en az bir platform (örn. Android 14 / API 34)
3. **SDK Tools**: **Android SDK Platform-Tools** + **Android Emulator**

```powershell
$env:ANDROID_HOME = "$env:LOCALAPPDATA\Android\Sdk"; $env:PATH = "$env:ANDROID_HOME\platform-tools;$env:ANDROID_HOME\emulator;$env:PATH"
```

### Appium

PowerShell'de `npm` ve `appium` yerine **`npm.cmd`** ve **`appium.cmd`** kullan — `.ps1` shim'leri
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

### Emulator veya gerçek cihaz

```powershell
emulator -list-avds
```

```powershell
adb devices -l
```

Çıktıdaki ilk sütun `android.udid` değeridir (örn. `emulator-5554`).

Gerçek Redmi/Xiaomi cihaz için: **Ayarlar → Telefon hakkında → MIUI sürümü**ne 7 kez dokun, sonra
**Geliştirici seçenekleri**nde *USB hata ayıklama*, *USB ile yükleme* ve *USB hata ayıklama (Güvenlik
ayarları)* açık olmalı; USB modu *MTP*. Telefondaki "USB hata ayıklamaya izin ver?" uyarısını
onaylamazsan `adb` cihazı `unauthorized` görür.

### ApiDemos APK

APK repository'ye konmaz: <https://github.com/appium/android-apidemos/releases>

`android.app.path` doluysa APK kurulur; boş bırakılırsa cihazdaki uygulama `android.app.package` +
`android.app.activity` ile açılır.

---

## 3. Koşum

**Profil hangi test grubunun çalışacağını belirler; `-Denvironment` / `-Ddevice` ise testin hangi
ortam ve cihaz üzerinde çalışacağını belirler.** İkisi farklı iştir.

| Komut | Ne çalışır | Cihaz |
| --- | --- | --- |
| `.\mvnw.cmd clean verify -Pcucumber` | Cucumber, varsayılan `@api and @smoke` | Hayır |
| `.\mvnw.cmd clean verify -Pcucumber "-Dcucumber.filter.tags=@mobile and @smoke"` | Cucumber mobil senaryolar | **Evet** |
| `.\mvnw.cmd clean verify -Papi` | `@Tag("api")` + `@Tag("unit")` — live adapter + framework testleri | Hayır |
| `.\mvnw.cmd clean verify -Pmobile` | `@Tag("mobile")` + `@Tag("unit")` | **Evet** |

`CucumberRunnerTest` adı `**/*Test.java` desenine uyduğu için `-Papi` / `-Pmobile` koşumlarında
`pom.xml` içinde **açıkça hariç tutulur**; Cucumber yalnızca `-Pcucumber` ile çalışır.

Ortam ve cihaz seçimi:

```powershell
.\mvnw.cmd clean verify -Pcucumber -Denvironment=prep -Ddevice=android-real "-Dcucumber.filter.tags=@mobile and @smoke"
```

Canlı kodlama sırasında tek sınıf / tek metot (PowerShell'de **tırnak içine al**):

```powershell
.\mvnw.cmd clean verify -Pmobile "-Dit.test=LiveCodingTest"
```

```powershell
.\mvnw.cmd clean verify -Papi "-Dit.test=LiveCodingTest#getsExistingPost"
```

> Failsafe `-Dtest` değil **`-Dit.test`** ister. Tek test denemek için IntelliJ'in çalıştır düğmesi
> daha hızlıdır; terminal koşumu asıl olarak rapor üretmek için kullanılır.

Adımların glue ile eşleştiğini cihaz açmadan kontrol etmek için dry-run:

```powershell
.\mvnw.cmd clean verify -Pcucumber "-Dcucumber.filter.tags=@mobile" "-Dcucumber.execution.dry-run=true"
```

Dry-run adımları çalıştırmaz ve hook'ları tetiklemez — driver açılmaz. Bu yüzden **yalnızca glue
eşleşmesini kanıtlar, E2E kanıtı değildir.**

### Bilinen sınırlama: yanlış tag build'i kırmaz

`cucumber.filter.tags` hiçbir senaryoyla eşleşmezse Cucumber senaryoları **skip** eder, "sıfır test"
üretmez; Failsafe test bulduğu için build yeşil kalır. Tag'i değiştirdiğinde çıktıdaki **Skipped
sayısına bak**.

---

## 4. Ayarlar

Çözümleme sırası (üstteki kazanır):

```
1) -Dapi.base.url=...                       komut satırı
2) API_BASE_URL                             ortam değişkeni (nokta → alt çizgi, büyük harf)
3) config/device/<device>.properties         -Ddevice=android-real
4) config/env/<environment>.properties       -Denvironment=prep
5) config/config.properties                  ortak varsayılanlar
```

`environment` ve `device` yalnızca ilk üç kaynaktan çözülür — aksi halde hangi dosyanın yükleneceği
kendi içeriğine bağlı olurdu. Değer doğrudan bir kaynak yoluna girdiği için `[A-Za-z0-9_-]+` ile
doğrulanır: `../../` gibi bir değer reddedilir.

Yeni ortam eklemek = `config/env/<ad>.properties` dosyası açmak. Kod değişikliği gerekmez.

| Anahtar | Nerede | Açıklama |
| --- | --- | --- |
| `environment` / `device` | config.properties | Hangi katman dosyalarının yükleneceği |
| `mobile.platform` | config.properties | Yalnızca `android` veya `ios`; cihaz taranmaz |
| `appium.server.url` | config.properties | Dışarıdan başlatılan server adresi |
| `mobile.explicit.wait.seconds` | config.properties | Tüm explicit wait'lerin üst sınırı |
| `api.base.url` | env | **Opsiyonel:** doluysa relative path, boşsa senaryoda full URL |
| `api.auth.token` | env | **Boş bırakılır**, `-Dapi.auth.token=...` veya `API_AUTH_TOKEN` ile geçilir |
| `api.log.request` / `api.log.response` | config.properties | Rest Assured'ın konsol log'u. **Varsayılan `false`** — açılırsa gövde **maskelenmeden** stdout'a ve `failsafe-reports` XML'ine düşer. Rapor için gerekmez; Allure eki zaten maskelenmiş gövdeyi taşır |
| `android.udid` / `android.app.path` | device | `adb devices -l` çıktısı / kurulacak APK |
| `ios.udid` / `ios.bundle.id` | device | `xcrun xctrace list devices` çıktısı |
| `csv.output.dir` | -D | CSV çıktı dizini (varsayılan `target/output`) |

Gizli değerler repository'ye yazılmaz ve tek bir isim listesinden maskelenir (`SensitiveHeaders`).
Karşılaştırma normalize edilir — `access_token`, `accessToken` ve `Access-Token` aynı sayılır.
Maskeleme **yalnızca rapor kopyasında** yapılır; gönderilen istek ve alınan `Response` değişmez.

| Nerede | Ne maskelenir |
| --- | --- |
| Allure request eki | Gizli header değerleri, URL'deki kullanıcı bilgisi, gizli query parametrelerinin değerleri, JSON gövdedeki gizli alanlar (iç içe nesne ve dizi dahil) |
| Allure response eki | JSON gövdedeki gizli alanlar |
| Environment paneli | `api.base.url` ve `appium.server.url` içindeki kullanıcı bilgisi |
| `byte[]` ve `InputStream` gövdeler | İçerik rapora **hiç yazılmaz**, yerine boyut/tip bilgisi konur. `InputStream` rapor için okunmaz — okumak gönderilecek veriyi tüketirdi |
| Assertion hata mesajları | Yanıt gövdesi **mesaja konmaz**; mesaj Allure'daki maskelenmiş "API response" ekine yönlendirir. Hata mesajı failsafe raporuna maskelenmeden geçtiği için gövde orada durmaz |
| Rest Assured'ın kendi konsol log'u | **Maskelenmez** — bu yüzden `api.log.request` / `api.log.response` varsayılan olarak kapalıdır ve doğrulama hatasında otomatik log (`enableLoggingOfRequestAndResponseIfValidationFails`) açılmaz. Hata kanıtı maskelenmiş Allure ekleridir |

---

## 5. Adım katalogu

Ayrıntılı liste: [docs/step-catalog.md](docs/step-catalog.md)

### Mobil — iki yazım stili

**Genel adımlar** (hız ve keşif için): Gherkin'e locator değil **element adı** girer.

```gherkin
* Click to element "VIEWS_MENU" in "Api Demos Page"
* Verify element "BUTTONS_OPTION" exists in "Api Demos Page"
```

Yeni bir ekran için adım yazmak gerekmez; locator'ı `*Locators` sınıfına eklemek ve sayfayı
`LocatorRegistry.PAGES` listesine tek satırla kaydetmek yeterlidir.

**İş dili adımları** (regresyon ve iletişim için): çok adımlı akışlar ve formun iç tutarlılığını
doğrulayan senaryolar Page Object üzerinden yürür.

```gherkin
When the user selects "Jupiter" from the planet dropdown
Then only the second radio button should be selected
```

### API — endpoint başına sınıf yok

```gherkin
Given the base url is "https://baska-servis.com"      # opsiyonel, sadece bu senaryo
And the request headers:
  | X-Request-Id | commencis-interview |
And the query params:
  | postId | 1 |
And the request body from csv "testdata/csv/posts.csv" where "case" is "happy_path"
When I send POST to "/posts"
Then the response status should be 201
And I save response field "id" as "postId"
And I save the response to csv "created-posts.csv" with fields "id,title,userId"
```

Gövde dört kaynaktan gelebilir: **DocString** (elle yazma), **JSON dosyası**, **CSV satırı**,
**DataTable**.

### Yer tutucular

| Token | Kaynak |
| --- | --- |
| `${ctx:postId}` | Önceki adımda context'e kaydedilen değer |
| `${config:api.base.url}` | Aktif ortam/cihaz katmanından gelen ayar |

Başka kaynak yoktur. Bilinmeyen bir önek veya boş değer sessizce geçilmez, hata verir. `notNull`
gibi doğrulamalar yer tutucu değil ayrı adımdır (`the response field "id" should not be null`).

---

## 6. Test verisi

| | Yer | Neden |
| --- | --- | --- |
| Girdi CSV / JSON | `src/test/resources/testdata/` | Classpath'ten okunur, git'te versiyonlanır |
| Çıktı CSV | `target/output/` | `resources` derleme girdisidir; oraya yazmak git'i kirletir ve `clean` ile temizlenmez |

CSV ayrıştırma Apache Commons CSV ile yapılır: tırnak içindeki virgül, gömülü satır sonu ve escape
edilmiş tırnak doğru okunur. Çıktıda başlık bir kez yazılır, değerler escape edilir, aynı JVM içinde
aynı dosyaya paralel append güvenlidir.

**Sınır:** CSV düz bir tablodur. `CsvData.toJson` sayı, boolean ve null görünümlü değerleri tipli
yazar, gerisi string kalır. İç içe nesne veya dizi içeren gövdeler için JSON dosyası veya DocString
kullanılır.

---

## 7. Rapor

```powershell
start .\target\allure-report\index.html
```

Rapor `post-integration-test` aşamasında üretilir; Failsafe'in `verify` goal'ü build'i kırmadan önce
çalışır, **yani test fail olsa da HTML oluşur.** Fail eden test build'i yine kırar.

Raporda bulunanlar:

- **Environment paneli** — `environment`, `device`, `mobile.platform`, `api.base.url`,
  `appium.server.url`. Koşum başında `target/allure-results/environment.properties` olarak üretilir
  (statik kaynak olamaz, `clean` siler). Token/key/secret **yazılmaz**.
- **API request / response ekleri** — method, **gönderilen gerçek URI** (çözülmüş base URL, yerleşmiş
  path parametreleri, encode edilmiş query string), maskeli header'lar, gövde, status ve süre.
  Kanıt `ApiReportingFilter` içinde, yani ortak HTTP katmanında üretilir: Cucumber adımları ve
  `live/LiveCodingTest` aynı eki alır, istek ikinci kez gönderilmez.
- **Mobil ekran görüntüsü** — `@After(order = 9000)`; Bar2 capture'ından (10.000) sonra, driver'ı
  kapatan hook'tan (10) önce çalışır. Bar2 ve Allure ayrı boru hatlarıdır, biri diğerini beslemez.

İlk çalıştırma internet ister: allure-maven, Node runtime'ı ve Allure paketini `.allure/` klasörüne
indirir (~35 sn, bir kez).

---

## 8. Yeni test eklemek

**API senaryosu:** `features/api/` altına `.feature` ekle, `@api` ile tag'le. Çoğu durumda Java
yazmana gerek yok — adım katalogu adres, gövde, parametre ve doğrulamayı karşılar. Aynı endpoint 3+
senaryoda tekrar ederse `api/service/` altına ince bir sarmalayıcı eklenebilir.

**Mobil senaryo:**
1. Locator'ı Appium Inspector ile bul
2. `*Locators` sınıfına `public static final By` olarak ekle
3. Yeni bir sayfa ise `LocatorRegistry.PAGES` listesine tek satır ekle
4. Genel adımlarla yaz; akış çok adımlıysa Page Object'e metot ekleyip iş dili adımı yaz

Mobil senaryolar `@mobile` tag'ini taşımalıdır: driver'ı açan hook `@Before("@mobile")` ile
sınırlıdır.

**Framework değişikliği:** `frameworktest/` altına `@Tag("unit")` birim testi ekle. Bu testler
`-Papi` ve `-Pmobile` koşumlarında da çalışır (`test.tags = api | unit` / `mobile | unit`).

**Canlı kodlama:** `live/LiveCodingTest` içine `@Test` ekle, `@Tag("api")` veya `@Tag("mobile")` ver.

---

## 9. Locator seçimi

| # | Strateji | Örnek |
| --- | --- | --- |
| 1 | accessibilityId | `AppiumBy.accessibilityId("Views")` |
| 2 | id | `AppiumBy.id("io.appium.android.apis:id/spinner1")` |
| 3 | UiAutomator selector | `AppiumBy.androidUIAutomator("new UiSelector().text(\"Jupiter\")")` |
| 4 | XPath | son çare — yavaş ve kırılgan |

**Locator'lar kullandığın APK sürümüne göre değişebilir; Appium Inspector ile doğrula.**
Remote Host `127.0.0.1`, Port `4723`, Path `/`.

Native dropdown'da Selenium `Select` çalışmaz: dropdown'a tıkla, sonra açılan listedeki seçeneğe
tıkla (`ElementActions.selectOption`). Kaydırma için eski `TouchAction` API'si kullanılmaz;
Android'de `mobile: scrollGesture`, iOS'ta `mobile: scroll` çağrılır.

### Aynı elemanın iki platformda farklı locator'ı

```java
public static final By PAY_BUTTON = PlatformBy.of(
        AppiumBy.id("com.example:id/payButton"),
        AppiumBy.accessibilityId("payButton"));
```

**Sözleşme: bir JVM koşumu tek platform çalıştırır.** Locator'lar `static final` olduğu için seçim
class yüklenirken bir kez yapılır; aynı JVM içinde paralel Android + iOS koşumu desteklenmez. Ayrı
Maven/JVM koşumları kullanılır.

---

## 10. Capability

Capability, Appium'a **hangi cihazda, hangi uygulamayı, nasıl** çalıştıracağını söyleyen ayarlardır.
Tip güvenli `UiAutomator2Options` / `XCUITestOptions` kullanılır; hepsi cihaz profili dosyasından
okunur, kodda sabit değer yoktur (bkz. `MobileDriver`).

---

## 11. iOS durumu

`mobile.platform=ios` verildiğinde `MobileDriver` gerçek bir XCUITest oturumu açmayı dener; kod
yerinde ve derleniyor. **İstemcinin işletim sistemi kontrol edilmez** — `appium.server.url` uzaktaki
bir macOS makineyi gösterebilir.

**iOS E2E: NOT RUN.** Gerçek bir XCUITest oturumu hiç açılmadı. `ApiDemos` bir Android demo
uygulamasıdır, iOS build'i yoktur.

---

## 12. Doğrulama durumu

Son koşum: 16.08.2026

| Kapsam | Komut | Durum |
| --- | --- | --- |
| Framework birim testleri | `-Papi` (`@Tag("unit")`) | **PASS** — 83 test |
| JUnit live adapter (API) | `-Papi` (`@Tag("api")`) | **PASS** — 5 test |
| Cucumber API senaryoları | `-Pcucumber "-Dcucumber.filter.tags=@api"` | **PASS** — 11 senaryo |
| Cucumber mobil glue eşleşmesi | dry-run `@mobile` | **PASS** — 9 senaryo, undefined step yok |
| Allure HTML + Environment paneli | `-Pcucumber` ve `-Papi` | **PASS** — her iki koşum tipinde de üretiliyor |
| Allure API request/response ekleri | `-Pcucumber` (12+12) ve `-Papi` (5+5) | **PASS** — gerçek URI + query görünüyor |
| Sızıntı kanaryası — header, URL userinfo, gizli query parametresi, iç içe JSON gövde, `byte[]` gövde, başarısız doğrulama log'u | `ReportRedactionCanaryTest` + `ValidationFailureLogTest` + artifact taraması | **PASS** — `allure-results`, `allure-report`, `failsafe-reports`, `output` temiz |
| CSV çıktısı | `-Pcucumber` | **PASS** — `target/output/created-posts.csv` |
| **Cucumber mobil E2E (gerçek cihaz)** | `-Pcucumber "-Dcucumber.filter.tags=@mobile and @smoke"` | **NOT RUN** |
| **JUnit live adapter (mobil)** | `-Pmobile "-Dit.test=LiveCodingTest"` | **NOT RUN** |
| **Mobil failure screenshot** | — | **NOT RUN** — hook ve kod yolu yerinde, kontrollü bir failing senaryo ile doğrulanmadı |
| **iOS E2E** | — | **NOT RUN** |

**Derleme başarısı veya Cucumber dry-run sonucu, gerçek cihaz E2E kanıtı değildir.**
