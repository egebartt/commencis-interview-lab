## Appium Mobile Automation Cheat Sheet
![Appium mimarisi](images/appium-architecture.png)

# Appium cheat sheet

Sık kullanılan komutlar ve locator örnekleri. Bu notlar önceden `config.properties` içindeydi;
ayar dosyası ayar tutsun diye buraya taşındı.

## Komutlar (PowerShell)

| Amaç                        | Komut                                                                                                                                                        |
|-----------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Appium server               | `appium.cmd`                                                                                                                                                 |
| Appium + Inspector plugin   | `appium.cmd --use-plugins=inspector`                                                                                                                         |
| Bağlı cihazlar              | `adb devices -l`                                                                                                                                             |
| Emulator listesi            | `emulator -list-avds`                                                                                                                                        |
| Emulator başlat             | `emulator -avd Pixel8_API35`                                                                                                                                 |
| Ekrandaki package/activity  | `adb shell dumpsys window \| Select-String mCurrentFocus`                                                                                                    |
| Web inspector url           | `http://127.0.0.1:4723/inspector`                                                                                                                            |
| Server ayakta mı            | `Invoke-RestMethod http://127.0.0.1:4723/status`                                                                                                             |
| 4723 portunu kullanan süreç | `Get-Process -Id (Get-NetTCPConnection -LocalPort 4723 -State Listen).OwningProcess`                                                                         |
| 4723 portunu kapat          | `Get-NetTCPConnection -LocalPort 4723 -State Listen \| Select-Object -ExpandProperty OwningProcess -Unique \| ForEach-Object { Stop-Process -Id $_ -Force }` |
| Allure Rapor temizliği için | `./mvnw.cmd clean`                                                                                                                                   |
| Allure Raporu yeniden üret  | `.\mvnw.cmd allure:report`                                                                                                                                   |
| Allure Raporu tarayıcıda aç | `Start-Process .\target\allure-report\index.html`                                                                                                           |

Inspector web arayüzü: <http://127.0.0.1:4723/inspector> · Remote Host `127.0.0.1`, Port `4723`,
Path `/`

## Capability JSON karşılığı

`config.properties` içindeki anahtarlar Appium'a şu capability'ler olarak gider
(`core/Driver.java` kurar; `platformName` ve `automationName` platforma göre sabittir):

```json
{
  "platformName": "Android",
  "appium:automationName": "UiAutomator2",
  "appium:udid": "emulator-5554",
  "appium:appPackage": "io.appium.android.apis",
  "appium:appActivity": ".ApiDemos",
  "appium:noReset": true
}
```

## Locator öncelik sırası

| # | Strateji | Ne zaman |
| --- | --- | --- |
| 1 | `AppiumBy.accessibilityId()` → content-desc | İlk tercih |
| 2 | `AppiumBy.id()` → resource-id | id kadar iyi |
| 3 | `AppiumBy.androidUIAutomator()` → text/özellik | Kesinlikle öğren |
| 4 | `AppiumBy.className()` | Tek başına pek kullanma |
| 5 | `AppiumBy.xpath()` | Son çare |

## UiAutomator selector örnekleri

```java
AppiumBy.androidUIAutomator("new UiSelector().text(\"Alarm\")");
AppiumBy.androidUIAutomator("new UiSelector().textContains(\"Alarm\")");
AppiumBy.androidUIAutomator("new UiSelector().textStartsWith(\"Al\")");
AppiumBy.androidUIAutomator("new UiSelector().textMatches(\"(?i).*alarm.*\")"); //(text ignorecase regex) (?i)
AppiumBy.androidUIAutomator("new UiSelector().description(\"Alarm\")"); //(text content-desc)
AppiumBy.androidUIAutomator("new UiSelector().descriptionContains(\"Alarm\")"); //(text content-desc contains)
AppiumBy.androidUIAutomator("new UiSelector().className(\"android.widget.Button\").clickable(true)");
AppiumBy.androidUIAutomator("new UiSelector().checkable(true).checked(true)");

// Iç içe kapsama: aynı id birden fazla yerde geçiyorsa
AppiumBy.androidUIAutomator(
        "new UiSelector().resourceId(\"io.appium.android.apis:id/spinner2\")"
                + ".childSelector(new UiSelector().resourceId(\"android:id/text1\"))");
```

## XPath örnekleri (son çare)

```java
AppiumBy.xpath("//*[@text='Alarm']");
AppiumBy.xpath("//*[contains(@text,'Alarm')]");
AppiumBy.xpath("//*[@text='Premium Paket']/parent::android.view.ViewGroup//android.widget.Button[@text='Seç']");
AppiumBy.xpath("//android.widget.Toast[@text='Monitored switch is on']");
```

## MobileActions metotları (mülakatta kullanılacak liste)

Hepsi `By` alır, bekleme içeridedir; ayrıca `WebDriverWait` yazmaya gerek yoktur.
Çalışan örnek: `interview/InterviewMobile.java` → `mobileReferences()`.

| İşlem | Metot |
| --- | --- |
| Tıklama | `mobile.click(locator)` |
| Ekranda değilse kaydırıp tıklama | `mobile.scrollAndClick(locator)` · `mobile.scrollAndClickText("Views")` |
| Yazma / temizleme | `mobile.clearSendKeys(locator, "metin")` · `mobile.clear(locator)` |
| Temizlemeden ekleme | `mobile.sendKeys(locator, "metin")` |
| Klavyeyi kapatma | `mobile.hideKeyboard()` |
| Text okuma | `mobile.text(locator)` · `mobile.texts(locator)` (liste) |
| Attribute okuma | `mobile.attribute(locator, "checked")` — `text`, `content-desc`, `enabled`… |
| Ekranda bu yazı var mı | `mobile.isVisible(MobileActions.byText("Save"))` |
| Görünürlük / negatif kontrol | `mobile.isVisible(locator)` · `mobile.isVisible(locator, 2)` · `mobile.isPresent(locator, 2)` |
| Checkbox / radio / switch seçili mi | `mobile.isChecked(locator)` |
| Buton aktif mi | `mobile.isEnabled(locator)` |
| Dropdown: aç + seç | `mobile.selectByText(dropdown, "Jupiter")` · `mobile.select(dropdown, option)` |
| Dropdown: seçenek listenin altındaysa | `mobile.selectByScrollingToText(dropdown, "Saturn")` |
| Sağa / sola swipe | `mobile.swipeLeft()` · `mobile.swipeRight()` · element üzerinde: `mobile.swipeLeft(locator)` |
| Yukarı / aşağı kaydırma | `mobile.scrollDown()` · `mobile.scrollUp()` |
| Uzun basma | `mobile.longPress(locator)` |
| Koordinata dokunma | `mobile.tapAt(540, 1200)` |
| Geri tuşu | `mobile.back()` |
| Toast (yalnız Android) | `mobile.isToastVisible("Kaydedildi", 3)` |
| Açık bekleme | `mobile.waitVisible(locator)` · `mobile.waitInvisible(locator)` |

Checkbox/radio/toggle'da `click` durumu **tersine çevirir**, mutlak değer atamaz: önce
`isChecked` ile bak, gerekirse tıkla.

## Bu projede nereye yazılır

Sabit locator'lar ekranın kendi `mobile/pages/*Locators.java` sınıfında `static final By` olarak
durur. Sınıf `public` değildir: aynı paketteki Page'ler okur, `stepdefinitions` paketi göremez.
Değeri çalışma anında gelen locator'lar (`byText`, `toast`) `mobile/actions/MobileActions`
içindeki static üreticilerdir — ekran locator'ı değil, platforma göre değişen teknik kalıptır.

Kural: locator'ı yalnızca Page okur; `click`/`wait`/`scroll`/`back` gibi teknik dokunuşlar
`MobileActions` içinde toplanır; Step Definition katmanında ne locator ne driver bulunur.
