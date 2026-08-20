# Kurulum ve Koşum

Bu dosya projeyi ilk kez indiren biri için yazıldı. Proje yapısı ve mimari anlatımı
[docs/Project-structure.md](docs/Project-structure.md) içinde.

Mülakat senaryoları iki yerde birden yazılı: **JUnit** (`interview/`) ve **Cucumber**
(`features/`). İkisi de aynı Page Object katmanını kullanır, ikisi de geçiyor.

APK repo içinde (`apps/`), yol relative verildiği için **config'de değiştirilecek bir şey yok**.

---

## 1. Gereksinimler

| Ne | Sürüm | Ne için |
| --- | --- | --- |
| JDK | 21 | Proje Java 21 ile derleniyor |
| Node.js | 20+ | Appium npm ile kuruluyor |
| Appium server | 3.x | Sadece mobil koşum |
| Appium `uiautomator2` driver | 8.x | Sadece mobil koşum |
| Android SDK Platform-Tools | — | `adb` |
| Android emülatör veya gerçek cihaz | API 30+ | Sadece mobil koşum |

**API tarafı için sadece JDK yeterli** — cihaz, emülatör, Appium gerekmez.

**Maven kurmaya gerek yok**; repo içindeki `mvnw.cmd` wrapper'ı kullanılıyor.

Doğrulandığı ortam: JDK 21.0.12, Node 22.17, Appium 3.6.0, uiautomator2 8.2.2,
Android 15 (API 35) emülatör, Windows 11.

### Appium kurulumu

```powershell
npm.cmd install -g appium
```

```powershell
appium.cmd driver install uiautomator2
```

> PowerShell'de `appium` yerine `appium.cmd` kullan; `.ps1` shim'i execution policy nedeniyle
> bloklanabiliyor.

Makinede ne eksik olduğunu görmek için (hiçbir şey kurmaz, sadece rapor eder):

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\preflight.ps1
```

---

## 2. Config

Tek ayar dosyası: `src/test/resources/config.properties`. Klonlayıp koşmak için **dokunmana
gerek yok**; APK repo içinde ve yolu relative:

```properties
android.app=apps/mda-2.2.0-25.apk
android.udid=emulator-5554
```

Relative yol proje köküne göre çözülür (`core/Driver.java`), yani makineden bağımsızdır.
Mutlak yol da verilebilir.

Cihazın seri numarası farklıysa tek değişecek şey `android.udid`:

```powershell
adb devices -l
```

Dosyayı hiç değiştirmeden komut satırından geçmek de mümkün:

```powershell
.\mvnw.cmd clean verify "-Dcucumber.filter.tags=@mobile and @interview" "-Dandroid.udid=emulator-5556"
```

Öncelik sırası: `-Dkey=değer` > ortam değişkeni > `config.properties`

Uygulama `SplashActivity` ile açılıp hemen `MainActivity`'ye geçtiği için config'de şu iki satır
var; Appium varsayılan davranışta *"SplashActivity never started"* hatası veriyor:

```properties
android.app.wait.package=com.saucelabs.mydemoapp.android
android.app.wait.activity=*
```

---

## 3. Mobil koşumdan önce

Emülatörü aç ve göründüğünü doğrula:

```powershell
adb devices -l
```

Appium server'ı **ayrı bir terminalde** başlat, açık bırak — proje server başlatmaz:

```powershell
appium.cmd
```

```powershell
Invoke-RestMethod http://127.0.0.1:4723/status
```

---

## 4. Koşum komutları

Tümü proje kökünden, PowerShell'de. `-D` içeren argümanları **tırnak içine al**.

### API — cihaz gerekmez

```powershell
.\mvnw.cmd clean verify "-Dcucumber.filter.tags=@api"
```

```powershell
.\mvnw.cmd clean verify "-Dit.test=InterviewApi#interViewApi"
```

### Mobil — emülatör + Appium açık olmalı

```powershell
.\mvnw.cmd clean verify "-Dcucumber.filter.tags=@mobile and @interview"
```

```powershell
.\mvnw.cmd clean verify "-Dit.test=InterviewMobile#interViewMobile"
```

### IntelliJ'den

Feature dosyasındaki senaryonun ya da test metodunun yanındaki yeşil butona basmak yeterli;
ek VM option gerekmiyor.

> **Çıktıdaki `Tests run:` sayısına bak.** `-Dit.test=...` içindeki **metot adı** yanlış
> yazılırsa Failsafe hata vermez; `Tests run: 0` yazıp **BUILD SUCCESS** döner. Sınıf adı yanlışsa
> build kırılır, metot adı için bu koruma yok.

---

## 5. Hangi dosyada ne var

| Senaryo | JUnit | Cucumber |
| --- | --- | --- |
| **API** — user oluştur / oku / güncelle / sil | [`InterviewApi.java`](src/test/java/com/commencis/interview/InterviewApi.java) → `contactListUserLifecycle()` | [`InterviewApi.feature`](src/test/resources/features/api/InterviewApi.feature) |
| **Mobil** — fiyata göre sırala, 3. ürün, WebView | [`InterviewMobile.java`](src/test/java/com/commencis/interview/InterviewMobile.java) → `interViewMobile()` | [`interview_mobile.feature`](src/test/resources/features/mobile/interview_mobile.feature) |

Destekleyen katmanlar:

```
mobile/locators/InterviewLocators.java     locator'lar (kodda inline locator yok)
mobile/pages/InterviewPage.java            Page Object
mobile/actions/MobileActions.java          tek Appium katmanı (bekleme, tıklama, context)
stepdefinitions/                           Cucumber adımları → Page Object
api/ApiClient.java                         tek Rest Assured katmanı
core/Driver.java                           Appium session ve capability'ler
```

Aynı dosyalardaki `mobileReferences()` / `apiReferences()` metotları mülakat senaryosuna dahil
değildir; Appium ve Rest Assured kalıplarını gösteren referans testleridir. `mobileReferences()`
ApiDemos uygulamasını hedefler, bu repodaki APK ile çalışmaz.

---

## 6. Rapor

Koşum sonrası Allure HTML otomatik üretilir:

```
target/allure-report/index.html
```

Mobil senaryolarda ekran görüntüsü, API senaryolarında request/response ekleri rapora düşer.
Token gibi gizli değerler rapor kopyasında maskelenir.

---

## 7. İki teknik not

**Locale.** Makinenin JVM locale'i `tr_TR` ise `"I".toLowerCase()` noktasız `ı` üretir; Rest
Assured içeride locale vermeden `toLowerCase()` çağırdığı için non-2xx yanıtlar `Response`
yerine exception olarak döner ve 401 bekleyen adım patlar. `ApiClient` static block'unda locale
sabitlendi, koşum şekli fark etmeksizin çalışıyor.

**WebView context.** Mobil senaryodaki WebView adımı, context'in açıldığını
`WEBVIEW_com.saucelabs.mydemoapp.android` handle'ını doğrulayarak kontrol eder. Sayfa içeriğine
girmek için context değiştirmek (`MobileActions.switchToWebViewContext()`) cihazdaki
Chrome/WebView sürümüne uyan bir Chromedriver ister; senaryo bu bağımlılığı gerektirmeyecek
şekilde yazıldı. Metotlar `MobileActions` içinde duruyor, ihtiyaç olursa kullanılabilir.
