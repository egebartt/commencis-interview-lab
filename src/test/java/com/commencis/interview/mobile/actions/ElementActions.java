package com.commencis.interview.mobile.actions;

import com.commencis.interview.core.Config;
import com.commencis.interview.core.Driver;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.HidesKeyboard;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Tek dusuk seviye Appium katmani: bekleme, tiklama, yazma, kaydirma, durum okuma.
 *
 * <p>"Bir elemana teknik olarak nasil dokunulur" sorusunun tek cevabi burasidir. Page Object'ler
 * yalnizca "bu is ekranda nasil yapilir"i yazar ({@code openViews()}), Step Definition'lar da
 * yalnizca Page cagirir. Boylece WebDriverWait / ExpectedConditions / gesture kodu tek dosyada
 * kalir ve ayni bekleme mantigi her ekranda tekrar yazilmaz.
 *
 * <p>Driver constructor'dan gelir ve disari acilmaz: hicbir Page veya Step Definition
 * {@link AppiumDriver}'a dogrudan erisemez. PicoContainer her senaryo icin bir tane uretir ve
 * ayni ornegi butun Page'lere verir.
 */
public class ElementActions {

    private static final int MAX_SCROLL_ATTEMPT = 5;

    private final Driver driver;

    /** PicoContainer bu constructor'i kullanir; senaryonun Driver'i enjekte edilir. */
    public ElementActions(Driver driver) {
        this.driver = driver;
    }

    // ------------------------------------------------------------------
    // Aksiyon
    // ------------------------------------------------------------------

    /** Element tiklanabilir olana kadar bekleyip tiklar. */
    public void click(By locator) {
        wait(defaultTimeout()).until(ExpectedConditions.elementToBeClickable(locator)).click();
    }

    /** Alani temizleyip yeni metni yazar. */
    public void type(By locator, String text) {
        WebElement element = waitVisible(locator);
        element.clear();
        element.sendKeys(text);
    }

    public void clear(By locator) {
        waitVisible(locator).clear();
    }

    /** Dropdown'i acar ve acilan listeden secenegi tiklar. */
    public void select(By dropdown, By option) {
        click(dropdown);
        click(option);
    }

    /**
     * Secenegin degeri senaryodan gelen dropdown'lar icin: secenek locator'i calisma aninda
     * uretilir, boylece Page tarafinda selector kurma kodu tekrarlanmaz.
     */
    public void selectByText(By dropdown, String optionText) {
        select(dropdown, byText(optionText));
    }

    /** Element gorunene kadar kaydirip tiklar; listede asagida kalan satirlar icin. */
    public void scrollAndClick(By locator) {
        scrollUntilVisible(locator).click();
    }

    /** Metniyle bulunan satirlar icin (menu listesi); gerekirse satira kadar kaydirir. */
    public void scrollAndClickText(String text) {
        scrollAndClick(byText(text));
    }

    public void scrollDown() {
        scroll("down");
    }

    public void scrollUp() {
        scroll("up");
    }

    public void hideKeyboard() {
        if (appium() instanceof HidesKeyboard keyboard) {
            try {
                keyboard.hideKeyboard();
            } catch (WebDriverException ignored) {
                // Klavye zaten kapali olabilir.
            }
        }
    }

    /** Cihazin geri aksiyonu. */
    public void back() {
        appium().navigate().back();
    }

    // ------------------------------------------------------------------
    // Okuma
    // ------------------------------------------------------------------

    public String text(By locator) {
        return waitVisible(locator).getText();
    }

    public String attribute(By locator, String name) {
        return waitVisible(locator).getAttribute(name);
    }

    /** Element varsayilan sure icinde gorunurse true. */
    public boolean isVisible(By locator) {
        return isVisible(locator, defaultTimeout());
    }

    /** Negatif dogrulamalarda varsayilan sureyi beklememek icin sure verilebilir. */
    public boolean isVisible(By locator, int seconds) {
        try {
            wait(seconds).until(ExpectedConditions.visibilityOfElementLocated(locator));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    /** Element UI hiyerarsisinde var mi; gorunur olmasi sart degil (toast gibi kisa omurluler). */
    public boolean isPresent(By locator, int seconds) {
        try {
            wait(seconds).until(ExpectedConditions.presenceOfElementLocated(locator));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    /**
     * Mesaji senaryodan gelen Toast dogrulamasi.
     *
     * <p>Toast kisa omurludur ve "gorunur" sayilmayabilir; bu yuzden varsayilan bekleme yerine
     * kisa sureli bir varlik kontrolu yapilir.
     */
    public boolean isToastVisible(String message, int seconds) {
        return isPresent(toast(message), seconds);
    }

    /** Checkbox / radio / switch secili mi; attribute adi platforma gore degisir. */
    public boolean isChecked(By locator) {
        String value = Config.isAndroid() ? attribute(locator, "checked") : attribute(locator, "value");
        return "true".equalsIgnoreCase(value) || "1".equals(value);
    }

    public boolean isEnabled(By locator) {
        return waitVisible(locator).isEnabled();
    }

    // ------------------------------------------------------------------
    // Bekleme
    // ------------------------------------------------------------------

    public WebElement waitVisible(By locator) {
        return wait(defaultTimeout()).until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public void waitInvisible(By locator) {
        wait(defaultTimeout()).until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    // ------------------------------------------------------------------
    // Calisma aninda uretilen locator'lar
    // ------------------------------------------------------------------

    /**
     * Degeri senaryodan gelen elementler icin (dropdown secenegi, liste satiri). Ekranin sabit
     * locator'lari kendi Page sinifinda durur; burada yalnizca calisma aninda uretilenler vardir.
     *
     * <p>Gelen metin selector ifadesinin icine gomuldugu icin escape edilir; aksi halde tirnak
     * iceren bir deger ifadeyi bozar ve locator sessizce yanlis elemani bulur.
     */
    public static By byText(String text) {
        return Config.isAndroid()
                ? AppiumBy.androidUIAutomator("new UiSelector().text(\"" + escapeUiAutomator(text) + "\")")
                : AppiumBy.iOSNsPredicateString("label == '" + escapePredicate(text) + "'");
    }

    /** Toast yalnizca Android'de ayri bir widget'tir; iOS'ta karsiligi yoktur. */
    public static By toast(String message) {
        if (!Config.isAndroid()) {
            throw new UnsupportedOperationException("Toast dogrulamasi yalnizca Android icin tanimli.");
        }
        // XPath'te kacis karakteri yoktur: metin tek tirnak iceriyorsa cift tirnakla sarilir.
        String literal = message.contains("'") ? "\"" + message + "\"" : "'" + message + "'";
        return AppiumBy.xpath("//android.widget.Toast[@text=" + literal + "]");
    }

    // ------------------------------------------------------------------
    // Ic yardimcilar
    // ------------------------------------------------------------------

    /** Driver disari acilmaz: dusuk seviye cagrilar bu sinifin icinde kalir. */
    private AppiumDriver appium() {
        return driver.get();
    }

    private int defaultTimeout() {
        return Config.getInt("element.timeout", 15);
    }

    private WebDriverWait wait(int seconds) {
        return new WebDriverWait(appium(), Duration.ofSeconds(seconds));
    }

    private WebElement scrollUntilVisible(By locator) {
        for (int attempt = 0; attempt < MAX_SCROLL_ATTEMPT; attempt++) {
            WebElement displayed = findDisplayed(locator);
            if (displayed != null) {
                return displayed;
            }
            scrollDown();
        }
        // Son deneme: element hala yoksa anlamli TimeoutException uretilsin.
        return waitVisible(locator);
    }

    /** Locator'a uyan gorunur ilk elementi beklemeden dondurur, yoksa null. */
    private WebElement findDisplayed(By locator) {
        for (WebElement element : appium().findElements(locator)) {
            try {
                if (element.isDisplayed()) {
                    return element;
                }
            } catch (StaleElementReferenceException ignored) {
                // Kaydirma sirasinda yenilenen element bir sonraki denemede tekrar aranir.
            }
        }
        return null;
    }

    /** Ekranin orta alanini hedefleyen native gesture; platforma gore komut degisir. */
    private void scroll(String direction) {
        if (!Config.isAndroid()) {
            appium().executeScript("mobile: scroll", Map.of("direction", direction));
            return;
        }
        Dimension screen = appium().manage().window().getSize();
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("left", (int) (screen.getWidth() * 0.1));
        arguments.put("top", (int) (screen.getHeight() * 0.1));
        arguments.put("width", (int) (screen.getWidth() * 0.8));
        arguments.put("height", (int) (screen.getHeight() * 0.8));
        arguments.put("direction", direction);
        arguments.put("percent", 0.75);
        appium().executeScript("mobile: scrollGesture", arguments);
    }

    /** UiSelector cift tirnakli Java string sozdizimi kullanir. */
    private static String escapeUiAutomator(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    /** NSPredicate tek tirnakli string kullanir. */
    private static String escapePredicate(String value) {
        return value.replace("\\", "\\\\").replace("'", "\\'");
    }
}
