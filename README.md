# Commencis Interview Lab

Tek modüllü Java 21 / Maven test projesi. **API testleri Rest Assured**, **mobile testleri Appium** ile
yazılır, sonuçlar **Allure HTML** raporuna çıkar. Spring/Lombok yok.

Testler **JUnit 5 `@Test`** ile yazılır. **Cucumber** ikinci bir giriş noktası olarak hazırdır ve aynı
Page aksiyonlarını çağırır — ayrı bir otomasyon katmanı değildir (bkz. §9).

| | |
| --- | --- |
| Java | 21 |
| Build | Maven 3.9.16 (wrapper ile gelir, ayrıca kurmaya gerek yok) |
| API | Rest Assured 6.0.1 |
| Mobile | Appium Java Client 10.1.1 + Selenium 4.42.0 |
| Test | JUnit 5 + Maven Failsafe |
| BDD | Cucumber 7.29.0 (JUnit Platform Engine + PicoContainer) |
| Rapor | Allure → `target/allure-report/index.html` |

Maven **dependency olmayan**, makineye ayrıca kurulması gereken araçlar:

| Araç | Sürüm |
| --- | --- |
| Appium Server | 3.4.2 |
| UiAutomator2 Driver | 7.5.2 |
| Node.js | >= 20.19 |
| npm | >= 10 |

> **Sürüm karışıklığına dikkat:** `pom.xml` içindeki `appium.java-client.version` (10.1.1) **Appium
> Java Client** sürümüdür — yani testlerin kullandığı Java kütüphanesi. **Appium Server** (3.4.2) ve
> **UiAutomator2 Driver** (7.5.2) ayrı ürünlerdir, Maven dependency değildir ve npm ile makineye
> kurulur. Bu üçü birbirinden bağımsız versiyonlanır.

---

## 1. Proje yapısı

```
src/test/java/com/commencis/interview/
  base/
    BaseMobileTest.java     driver'ı açar/kapatır (@BeforeEach / @AfterEach)
    BaseApiTest.java        her test için temiz RequestSpecification üretir
  platform/
    MobilePlatform.java     ANDROID / IOS — tek kaynak: mobile.platform
  driver/
    MobileDriver.java       capability'leri kurar, AndroidDriver veya IOSDriver döner
  locator/
    ApiDemosLocators.java   PAGE_NAME + public static final By locator'lar
    PlatformBy.java         aynı elemanın Android/iOS locator'ı farklıysa seçer
  page/
    BasePage.java           ortak bekleme / tıklama / kaydırma / liste / context
    ApiDemosPage.java       sadece ekran aksiyonları, locator tutmaz
  api/
    RequestSpecFactory.java ortak Rest Assured yapılandırması
    PostApi.java            /posts endpoint çağrıları (assertion yok)
  test/
    mobile/ApiDemosTest.java
    api/PostApiTest.java
  cucumber/
    CucumberRunnerTest.java     feature'ları çalıştıran tek runner
    MobileHooks.java            @mobile senaryolarında driver aç/kapat + fail screenshot
    MobileTestContext.java      senaryo ömürlü driver sahibi
    ApiTestContext.java         senaryo ömürlü PostApi + response
    stepdefinition/             Page ve PostApi'ye delege eden adımlar
  util/
    ConfigReader.java       config.properties okur
    JsonReader.java         JSON dosyasını String olarak okur

src/test/resources/
  config.properties
  junit-platform.properties   Cucumber glue / tag / plugin ayarları
  features/                   .feature dosyaları
  testdata/create-post.json
```

**Locator ve aksiyon ayrımı:** `ApiDemosLocators` sadece locator'ları ve ekran adını (`PAGE_NAME`)
tutar. `ApiDemosPage` sadece aksiyonları tutar, hiç locator tanımlamaz. Böylece bir locator
değiştiğinde aksiyon koduna, bir akış değiştiğinde locator dosyasına dokunulmaz.

**JUnit ve Cucumber ortak nokta:** ikisi de aynı `ApiDemosPage` / `PostApi` metotlarını çağırır.
Step definition'lar `driver.findElement` veya bekleme mantığı içermez.

**BaseMobileTest ve MobileDriver ayrımı:** `BaseMobileTest` *ne zaman* driver açılıp kapanacağını
bilir (test yaşam döngüsü). `MobileDriver` *nasıl* açılacağını bilir (capability'ler). Böylece
capability değiştirmek için teste, test akışını değiştirmek için driver koduna dokunmazsın.

**RequestSpecFactory, BaseApiTest ve PostApi ayrımı:** `RequestSpecFactory` bağlantı ayarını
(base url, timeout, header, secret maskeleme) kurar; `BaseApiTest` (JUnit) ve `ApiTestContext`
(Cucumber) aynı factory'yi çağırır, Rest Assured yapılandırması iki yerde kopyalanmaz. `PostApi`
sadece istek atar ve `Response` döner — **assertion yapmaz**. Doğrulama testte/adımda kalır; bu
sayede aynı `PostApi` hem 200 hem 404 senaryosuna hizmet eder.

---

## 2. Kurulum (Windows 11 / PowerShell)

```powershell
$env:JAVA_HOME = "C:\Users\bartu\.jdks\ms-21.0.12"
```

```powershell
cd C:\Users\bartu\IdeaProjects\commencis-interview-lab
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

Kalıcı yapmak için (sonra **yeni** terminal aç):

```powershell
[Environment]::SetEnvironmentVariable("ANDROID_HOME", "$env:LOCALAPPDATA\Android\Sdk", "User")
```

### Appium

PowerShell'de `npm` ve `appium` yerine **`npm.cmd`** ve **`appium.cmd`** kullan — `npm.ps1` ve
`appium.ps1` shim'leri execution policy nedeniyle bloklanabilir.

Sürümler yukarıdaki tabloyla aynı olsun diye komutlara açıkça yazılır:

```powershell
npm.cmd install -g appium@3.4.2
```

```powershell
appium.cmd driver install uiautomator2@7.5.2
```

```powershell
appium.cmd driver doctor uiautomator2
```

### Appium Server'ı başlat

Server bu proje tarafından başlatılmaz; ayrı bir terminalde açık kalmalı:

```powershell
appium.cmd
```

Kontrol:

```powershell
Invoke-RestMethod http://127.0.0.1:4723/status
```

### Emulator veya gerçek cihaz

```powershell
emulator -list-avds
```

```powershell
emulator -avd Pixel_7_API_34
```

**udid'i bul** — `adb devices -l` çıktısındaki ilk sütun udid'dir (örn. `emulator-5554`):

```powershell
adb devices -l
```

### Gerçek Redmi cihaz hazırlığı

1. **Ayarlar → Telefon hakkında** → **MIUI sürümü**ne 7 kez dokun (Geliştirici seçenekleri açılır).
2. **Ayarlar → Ek ayarlar → Geliştirici seçenekleri**:
   - **USB hata ayıklama** açık
   - **USB ile yükleme** açık
   - **USB hata ayıklama (Güvenlik ayarları)** açık — Xiaomi'de UiAutomator2'nin tuşa basabilmesi
     için gerekli, Mi hesabı ister
   - **USB yapılandırması**: *Dosya aktarımı (MTP)*
3. Kabloyu tak, telefondaki **"USB hata ayıklamaya izin ver?"** uyarısını onayla ve
   *Bu bilgisayardan her zaman izin ver* işaretle. Onaylamazsan `adb` cihazı `unauthorized` görür.

| `adb devices` çıktısı | Yapılacak |
| --- | --- |
| `unauthorized` | Telefondaki onay penceresini kabul et. Çıkmıyorsa: `adb kill-server`, kabloyu çıkar-tak. |
| `offline` | Kabloyu/portu değiştir. |
| boş liste | OEM USB sürücüsünü kur, USB modunu MTP yap. |

### ApiDemos APK

APK repository'ye konmaz. Resmî sürüm sayfasından indir:
<https://github.com/appium/android-apidemos/releases>

`android.app.path` boş bırakılırsa cihazda **kurulu** uygulama `android.app.package` +
`android.app.activity` ile açılır. APK'dan kurmak istersen `android.app.path` doldur.

---

## 3. Testleri çalıştırma

**Maven profile hangi test grubunun çalışacağını belirler. config.properties ise testin hangi
ortam/cihaz/endpoint üzerinde çalışacağını belirler.** İkisi farklı iştir.

Dört koşum biçimi vardır:

| Komut | Ne çalışır | Cihaz gerekir mi |
| --- | --- | --- |
| `.\mvnw.cmd clean verify` | JUnit API testleri (profilsiz = `-Papi` ile aynı) | Hayır |
| `.\mvnw.cmd clean verify -Papi` | JUnit API testleri (`@Tag("api")`) | Hayır |
| `.\mvnw.cmd clean verify -Pmobile` | JUnit mobile testleri (`@Tag("mobile")`) | **Evet** |
| `.\mvnw.cmd clean verify -Pcucumber` | Sadece `CucumberRunnerTest` → varsayılan `@api and @smoke` | Hayır |

`CucumberRunnerTest` adı `**/*Test.java` desenine uyduğu için `-Papi`, `-Pmobile` ve profilsiz
koşumlarda `pom.xml` içinde **açıkça hariç tutulur**. Yani Cucumber yalnızca `-Pcucumber` ile çalışır.

API testleri:

```powershell
.\mvnw.cmd clean verify -Papi
```

Mobile testleri:

```powershell
.\mvnw.cmd clean verify -Pmobile -Dandroid.udid=emulator-5554
```

APK'dan kurarak:

```powershell
.\mvnw.cmd clean verify -Pmobile -Dandroid.udid=emulator-5554 -Dandroid.app.path=C:\apps\ApiDemos-debug.apk
```

Tek test sınıfı veya tek metot (PowerShell'de **tırnak içine al**, yoksa Maven argümanı bölüyor):

```powershell
.\mvnw.cmd clean verify -Papi "-Dit.test=PostApiTest"
```

```powershell
.\mvnw.cmd clean verify -Papi "-Dit.test=PostApiTest#getPostReturnsRecord"
```

Profile verilmezse sadece API testleri çalışır — çıplak `verify` komutu asla cihaz/Appium aramaz.
Yanlış bir `-Dit.test` değeri veya sıfır test seçimi build'i **kırar** (`failIfNoSpecifiedTests`).

---

## 3.1 Cucumber koşumu

Varsayılan `-Pcucumber` koşumu **cihaz gerektirmez**: `junit-platform.properties` içindeki
`cucumber.filter.tags=@api and @smoke` yalnızca API senaryolarını seçer, mobile senaryo skip edilir
ve driver hiç açılmaz.

```powershell
.\mvnw.cmd clean verify -Pcucumber
```

Mobile senaryolar (Appium + bağlı cihaz gerekir):

```powershell
.\mvnw.cmd clean verify -Pcucumber "-Dcucumber.filter.tags=@mobile and @smoke" -Dandroid.udid=emulator-5554
```

Tag ifadesi serbesttir; `-D` her zaman properties dosyasını ezer:

```powershell
.\mvnw.cmd clean verify -Pcucumber "-Dcucumber.filter.tags=@smoke and not @wip"
```

Adımların glue ile eşleştiğini cihaz açmadan kontrol etmek için dry-run:

```powershell
.\mvnw.cmd clean verify -Pcucumber "-Dcucumber.execution.dry-run=true" "-Dcucumber.filter.tags=@mobile and @smoke"
```

Dry-run adımları çalıştırmaz ve hook'ları tetiklemez — driver açılmaz.

### Bilinen sınırlama: yanlış tag build'i kırmaz

`cucumber.filter.tags` değeri hiçbir senaryoyla eşleşmezse Cucumber senaryoları **skip** eder,
"sıfır test" üretmez. Failsafe test bulduğu için `failIfNoTests` devreye girmez ve **build yeşil
kalır**. Örnek: `-Dcucumber.filter.tags=@yokboylebirtag` → `Tests run: 3, Skipped: 3`, BUILD SUCCESS.

Bunun için özel bir guard eklenmedi. Tag'i değiştirdiğinde koşum çıktısındaki **Skipped sayısına
bak**; hepsi skip ise tag ifadesi yanlıştır.

### StepDefinition'ı tek başına çalıştıramazsın

`@Given` / `@When` / `@Then` işaretli metotlar test değildir; IDE onları tek başına çalıştıramaz
(driver açılmaz, hook'lar tetiklenmez). Bir senaryoyu tek tek denemek için `.feature` dosyasındaki
senaryonun yanındaki çalıştır düğmesini (IntelliJ Cucumber for Java plugin) veya yukarıdaki tag
override komutunu kullan.

---

## 4. Ayarlar (config.properties)

Öncelik sırası: `-Dkey=deger` > environment variable > `config.properties`.

```
android.udid=            ->  -Dandroid.udid=emulator-5554   veya   ANDROID_UDID=emulator-5554
```

Önemli anahtarlar:

| Anahtar | Açıklama |
| --- | --- |
| `mobile.platform` | Yalnızca `android` veya `ios`. `auto` **desteklenmez** — cihaz taranmaz |
| `appium.server.url` | Dışarıdan başlatılan server adresi (uzak bir makine de olabilir) |
| `mobile.explicit.wait.seconds` | Tüm explicit wait'lerin üst sınırı |
| `android.udid` | `adb devices -l` çıktısından |
| `android.app.path` | Doluysa APK kurulur, boşsa package/activity kullanılır |
| `android.no.reset` | Varsayılan `false` |
| `ios.udid` | `xcrun xctrace list devices` çıktısından |
| `ios.app.path` | Doluysa uygulama kurulur, boşsa `ios.bundle.id` zorunlu |
| `ios.bundle.id` | Cihazdaki uygulamayı açmak için |
| `api.base.url` | Test edilen API |
| `api.auth.token` | **Boş bırakılır**, gerekirse `-Dapi.auth.token=...` |

`mobile.platform` hem driver'ı hem locator seçimini besleyen tek kaynaktır; ikisi tanım gereği
tutarlıdır. Bilinmeyen bir değer verilirse koşum açık hatayla durur:
`mobile.platform 'android' veya 'ios' olmali, gelen deger: 'windows'.`

**Not:** Önceki sürümde property isimlerini sabit olarak tutan bir `ConfigKeys` sınıfı vardı. Bu
boyuttaki bir projede araya bir sınıf koymak okumayı kolaylaştırmıyor, o yüzden anahtarlar doğrudan
string olarak kullanılıyor.

Token gibi gizli değerler repository'ye yazılmaz. Log'a da düşmez: assertion başarısız olduğunda
basılan request/response çıktısında `Authorization`, `Cookie`, `Proxy-Authorization` ve `X-Api-Key`
maskelenir.

---

## 5. Capability nedir?

Capability, Appium'a **hangi cihazda, hangi uygulamayı, nasıl** çalıştıracağını söyleyen ayarlardır.
Eskiden `DesiredCapabilities` ile map olarak verilirdi; artık tip güvenli **`UiAutomator2Options`**
kullanılır:

```java
UiAutomator2Options options = new UiAutomator2Options();
options.setUdid("emulator-5554");
options.setAppPackage("io.appium.android.apis");
options.setAppActivity(".ApiDemos");
options.setNoReset(false);
```

iOS tarafında karşılığı **`XCUITestOptions`**'tır (`setUdid`, `setBundleId`, `setApp`, `setNoReset`).
Hangisinin kurulacağını `mobile.platform` belirler.

Hepsi `config.properties`'ten okunur, kodda sabit değer yoktur → bkz. `MobileDriver`.

---

## 6. Locator seçimi

Locator'lar `ApiDemosLocators` içinde `public static final By` olarak durur; `ApiDemosPage` ve
testler ham locator tanımlamaz. Öncelik sırası:

| # | Strateji | Örnek |
| --- | --- | --- |
| 1 | accessibilityId | `AppiumBy.accessibilityId("Views")` |
| 2 | id | `AppiumBy.id("io.appium.android.apis:id/spinner1")` |
| 3 | UiAutomator selector | `AppiumBy.androidUIAutomator("new UiSelector().text(\"Jupiter\")")` |
| 4 | XPath | son çare — yavaş ve kırılgan |

**Locator'lar kullandığın ApiDemos APK sürümüne göre değişebilir; Appium Inspector ile doğrula.**
Inspector: <https://github.com/appium/appium-inspector/releases> → Remote Host `127.0.0.1`,
Port `4723`, Path `/` → capability'leri gir → Start Session.

Native dropdown'da Selenium `Select` çalışmaz. Yaklaşım: dropdown'a tıkla, sonra açılan listedeki
seçeneğe tıkla (`BasePage.openDropdownAndSelect`).

Kaydırma ve swipe için eski `TouchAction` API'si kullanılmaz. `BasePage` her platformda o driver'ın
kendi native gesture komutunu çağırır: Android'de UiAutomator2'nin `mobile: scrollGesture` /
`mobile: swipeGesture`, iOS'ta XCUITest'in `mobile: scroll` / `mobile: swipe` komutları.
`scrollDown()` gibi public metotlar iki platformda da aynıdır.

### Aynı elemanın iki platformda farklı locator'ı

`PlatformBy` aktif platforma göre seçim yapar:

```java
public static final By PAY_BUTTON = PlatformBy.of(
        AppiumBy.id("com.example:id/payButton"),
        AppiumBy.accessibilityId("payButton"));
```

Sözleşme: **bir JVM koşumu tek platform çalıştırır.** Locator'lar `static final` olduğu için seçim
class yüklenirken bir kez yapılır; aynı JVM içinde paralel Android + iOS koşumu desteklenmez.

`ApiDemosLocators` bunu kullanmaz: ApiDemos Android-only bir demo uygulamasıdır, iOS build'i yoktur.
**Bilinmeyen bir platform locator'ı için tahmini selector veya boş `By.id("")` yazılmaz** —
`PlatformBy.of` null locator kabul etmez.

---

## 7. API testleri

`PostApi` dört endpoint metodu içerir: `getPost`, `createPost`, `updatePost`, `deletePost`.
`PostApiTest` içindeki örnekler: GET alan doğrulama, JSON dosyasından POST, PUT, DELETE,
404 negatif testi ve bir yanıttan alınan değeri sonraki istekte kullanma.

### JSON body okuma

```java
String body = JsonReader.read("testdata/create-post.json");
postApi.createPost(body).then().statusCode(201);
```

### DTO nedir, neden yok?

- **Request DTO**: Java nesnesini JSON request body'ye çevirmek için kullanılır.
- **Response DTO**: JSON response'u Java nesnesine map etmek için kullanılır.

Bu projede JSON dosyası + JsonPath tercih edildi; böylece hangi alanın gittiğini/geldiğini
dosyada birebir görüyorsun. Gerçek bir projede şema sabitse DTO eklemek daha bakımlıdır.

### JSONPlaceholder gerçekten kayıt tutmaz

`api.base.url` varsayılanı JSONPlaceholder'dır: sahte bir servistir. `POST /posts` **201** döner ve
gönderdiğin gövdeyi `id: 101` ile echo eder, ama **hiçbir şey saklanmaz** — ardından
`GET /posts/101` **404** verir. Testler bu yüzden yalnızca yanıt sözleşmesini doğrular.

---

## 8. Rapor

```
target/allure-results        ham sonuçlar
target/allure-report/index.html    HTML rapor (tek dosya)
```

```powershell
start .\target\allure-report\index.html
```

Rapor `post-integration-test` aşamasında üretilir; Failsafe'in `verify` goal'ü build'i kırmadan
önce çalışır, **yani test fail olsa da HTML oluşur.** Fail eden test build'i yine kırar.

İlk çalıştırma internet ister: allure-maven, raporu üretmek için Node runtime'ı ve Allure paketini
`.allure/` klasörüne indirir (~35 sn, sadece bir kez).

`target/allure-results` klasörü istenirse Bar2 Report Plugin ile de denenebilir; ancak bu projenin
temel çıktısı yukarıdaki Allure HTML dosyasıdır. Bar2 zorunlu bir bağımlılık değildir.

---

## 9. iOS durumu

`mobile.platform=ios` verildiğinde `MobileDriver` artık `XCUITestOptions` + `IOSDriver` ile gerçek
bir oturum açmayı dener; kod yerinde ve derleniyor.

**İstemcinin işletim sistemi kontrol edilmez.** XCUITest çalıştıran **Appium server** macOS + Xcode
gerektirir, ama `appium.server.url` uzaktaki bir macOS makineyi gösterebilir — Windows'tan böyle bir
server'a bağlanmak normal bir kullanımdır. Bu yüzden OS'a bakıp iOS'u bloklayan bir kontrol yoktur.

**iOS E2E: NOT RUN.** iOS kod yolu bugüne kadar yalnızca derlendi ve dispatch/konfigürasyon
seviyesinde çalıştırıldı (`ios.udid` boşken açık hata verdiği doğrulandı). Gerçek bir XCUITest
oturumu hiç açılmadı. Özellikle şunlar **doğrulanmamıştır**:

- `mobile: scroll` / `mobile: swipe` yön semantiğinin Android tarafıyla örtüşmesi
- XCUITest `mobile: scroll`, elementId verilmediğinde **active application element** üzerinde çalışır;
  iç içe scroll alanlarındaki davranış test edilmedi
- `getAttribute("label")` / `getAttribute("value")` ile metin okuma

`ApiDemos` bir Android demo uygulamasıdır; iOS build'i yoktur, bu yüzden depoda çalışan bir iOS
senaryosu bulunmaz.

---

## 10. Yeni test eklemek

**API:** `PostApi`'ye endpoint metodu ekle (sadece istek, assertion yok) → `PostApiTest`'e
`@Test` ekle. Sınıf adı `*Test` ile bitmeli ve `@Tag("api")` taşımalı.

**Mobile:** Locator'ı Appium Inspector ile bul → `ApiDemosLocators`'a `public static final By`
olarak ekle → `ApiDemosPage`'e aksiyon metodu yaz → `ApiDemosTest`'e `@Test` ekle (`@Tag("mobile")`).

Tag veya `*Test` son eki eksikse test sessizce çalışmaz — sadece bu iki kural akışı belirler.

**Cucumber senaryosu:** `src/test/resources/features/` altına `.feature` ekle ve tag'le
(`@api @smoke` veya `@mobile @smoke`) → `cucumber/stepdefinition/` altındaki ilgili sınıfa adımı
yaz. Step definition işi Page veya `PostApi` metoduna delege etmeli; **locator, bekleme, driver
kullanımı ve request kurulumu orada tekrarlanmaz.** Given/When/Then seviyesinde ince sonuç
assertion'ları (status kodu, alan değeri, görünürlük) step definition içinde yer alabilir.

Mobile senaryolar `@mobile` tag'ini taşımalıdır: driver'ı açan hook `@Before("@mobile")` ile
sınırlıdır, tag yoksa driver açılmaz ve adım `Mobil driver acilmadi...` hatası verir.

---

## 11. Interview APK workflow

Mülakatta bir APK ve senaryo verildiğinde izlenecek sıra. Kod değişikliği gerekmez; uygulama
`config.properties` veya `-D` ile seçilir.

### 1) Uygulamayı tanıt

**APK dosyası verildiyse** — Appium APK'yı cihaza kurar:

```powershell
.\mvnw.cmd clean verify -Pmobile -Dandroid.udid=emulator-5554 -Dandroid.app.path=C:\apps\verilen.apk
```

**Uygulama emülatörde zaten kuruluysa** — `android.app.path` boş bırakılır, package/activity kullanılır:

```powershell
.\mvnw.cmd clean verify -Pmobile -Dandroid.udid=emulator-5554 -Dandroid.app.package=com.ornek.app -Dandroid.app.activity=.MainActivity
```

Uygulama splash ekranından sonra **başka bir activity'ye** geçiyorsa Appium yanlış ekranı bekleyip
zaman aşımına düşebilir. Bu durumda opsiyonel iki alanı doldur:

```
android.app.wait.package=com.ornek.app
android.app.wait.activity=.HomeActivity
```

Boş bırakılırsa hiç uygulanmaz, davranış değişmez.

### 2) Cihazı doğrula

```powershell
adb devices -l
```

Çıktıdaki ilk sütun `android.udid` değeridir (örn. `emulator-5554`).

### 3) Appium 3 server'ı başlat

Ayrı bir terminalde açık kalmalı:

```powershell
appium.cmd
```

```powershell
Invoke-RestMethod http://127.0.0.1:4723/status
```

### 4) Locator'ları Appium Inspector ile bul

| Alan | Değer |
| --- | --- |
| Remote Host | `127.0.0.1` |
| Remote Port | `4723` |
| Remote Path | `/` |

Capability olarak testin kullandığı değerleri gir, **Start Session** de, ekrandan elemana tıklayıp
sağdaki `content-desc` / `resource-id` değerlerini oku. Öncelik: accessibilityId → id →
UiAutomator selector → XPath (bkz. §6).

### 5) Senaryoyu ekle

Locator'ı `ApiDemosLocators` benzeri bir locator class'ına `public static final By` olarak ekle,
aksiyon metodunu ilgili page class'ına yaz, testi `@Tag("mobile")` ile `*Test` sınıfına koy
(bkz. §10).

### 6) Çalıştır

Tüm mobile testleri:

```powershell
.\mvnw.cmd clean verify -Pmobile -Dandroid.udid=emulator-5554
```

Tek bir test class'ı:

```powershell
.\mvnw.cmd clean verify -Pmobile -Dandroid.udid=emulator-5554 "-Dit.test=ApiDemosTest"
```

Tek bir metot:

```powershell
.\mvnw.cmd clean verify -Pmobile -Dandroid.udid=emulator-5554 "-Dit.test=ApiDemosTest#opensViewsAndGoesBack"
```

Rapor: `target/allure-report/index.html`

### Mevcut doğrulama durumu

Neyin gerçekten koşulduğunu ve neyin koşulmadığını ayırmak için:

| Kapsam | Durum |
| --- | --- |
| JUnit API testleri (`-Papi`, profilsiz) | **PASS** — 6/6 |
| Cucumber API senaryoları (`-Pcucumber`) | **PASS** — 3 senaryo bulundu, 2 API senaryosu geçti, 1 mobile senaryo skip |
| Cucumber glue eşleşmesi (`@mobile`, dry-run) | **PASS** — undefined step yok, driver açılmadı |
| Platform seçimi ve driver dispatch | **PASS** — android / ios / geçersiz değer için açık hata |
| **Android E2E** | **NOT RUN** |
| **iOS E2E** | **NOT RUN** |

**Android E2E: NOT RUN.** Mobile kodun platform dispatch'i ve config guard'ları gerçekten
çalıştırıldı (`mobile.platform` çözümü, eksik `android.udid`/`ios.udid` hataları, `@Before("@mobile")`
hook'unun tetiklenmesi), ancak **gerçek bir Appium session hiç açılmadı** — bağlı cihaz veya
emülatör üzerinde koşum yapılmadı. Dolayısıyla `ApiDemosLocators` locator'ları, dropdown akışı,
`scrollUntilVisible` görünürlük kontrolü ve `mobile: scrollGesture` / `mobile: swipeGesture`
çağrıları **doğrulanmamıştır**. İlk gerçek koşumda locator'ların Appium Inspector ile teyit edilmesi
gerekir. `BasePage`'deki liste ve WebView metotları henüz hiçbir test tarafından çağrılmıyor;
fail-screenshot hook'unun PNG üretmesi de cihaz gerektirdiği için test edilmedi.

**iOS E2E: NOT RUN** (bkz. §9).

Derleme başarısı veya Cucumber dry-run sonucu, gerçek cihaz E2E kanıtı değildir.
