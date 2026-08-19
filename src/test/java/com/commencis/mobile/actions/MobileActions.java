package com.commencis.mobile.actions;

import com.commencis.core.Config;
import com.commencis.core.Driver;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.HidesKeyboard;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Tek dusuk seviye Appium katmani: bekleme, tiklama, yazma, kaydirma, swipe, klavye ve geri tusu.
 * Yalnizca Page siniflarindan cagrilir; Step Definition bu sinifi gormez.
 */
public class MobileActions {

    private static final int MAX_SCROLL_ATTEMPT = 5;

    private final Driver driver;

    /** PicoContainer bu constructor'i kullanir; senaryonun Driver'i enjekte edilir. */
    public MobileActions(Driver driver) {
        this.driver = driver;
    }

    /** Element tiklanabilir olana kadar bekleyip tiklar. */
    public void click(By locator) {
        wait(defaultTimeout()).until(ExpectedConditions.elementToBeClickable(locator)).click();
    }

    /** Alani temizlemeden yazar: mevcut metnin sonuna ekler. */
    public void sendKeys(By locator, String text) {
        waitVisible(locator).sendKeys(text);
    }

    /** Alani temizleyip yeni metni yazar; dolu olabilecek alanlarda bunu kullanin. */
    public void clearSendKeys(By locator, String text) {
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

    /** Secenegin degeri senaryodan gelen dropdown'lar icin: secenek locator'i calisma aninda uretilir */
    public void selectByText(By dropdown, String optionText) {
        select(dropdown, byText(optionText));
    }

    /** Uzun dropdown listelerinde secenek ekranin altinda kalabilir: acar, kaydirir, tiklar. */
    public void selectByScrollingToText(By dropdown, String optionText) {
        click(dropdown);
        scrollAndClickText(optionText);
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

    /** Ekranin ortasindan sola kaydirir: carousel, onboarding, tab gecisi. */
    public void swipeLeft() {
        swipe("left");
    }

    public void swipeRight() {
        swipe("right");
    }

    /** Tek bir elementin uzerinde swipe: liste satirini kaydirip aksiyon acmak gibi. */
    public void swipeLeft(By locator) {
        swipeElement(locator, "left");
    }

    public void swipeRight(By locator) {
        swipeElement(locator, "right");
    }

    /** Uzun basma; context menu acan satirlar icin. */
    public void longPress(By locator) {
        String elementId = elementId(waitVisible(locator));
        if (!Config.isAndroid()) {
            appium().executeScript("mobile: touchAndHold", Map.of("elementId", elementId, "duration", 1));
            return;
        }
        appium().executeScript("mobile: longClickGesture", Map.of("elementId", elementId, "duration", 1_000));
    }

    /** Locator'i olmayan bir noktaya dokunur: harita, canvas, kapatma alani. */
    public void tapAt(int x, int y) {
        appium().executeScript(Config.isAndroid() ? "mobile: clickGesture" : "mobile: tap",
                Map.of("x", x, "y", y));
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

    public void back() {
        appium().navigate().back();
    }

    public String text(By locator) {
        return waitVisible(locator).getText();
    }

    /** Ayni locator'a uyan tum elementlerin metni; liste/sonuc dogrulamalari icin. */
    public List<String> texts(By locator) {
        waitVisible(locator);
        return appium().findElements(locator).stream().map(WebElement::getText).toList();
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

    public WebElement waitVisible(By locator) {
        return wait(defaultTimeout()).until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public void waitInvisible(By locator) {
        wait(defaultTimeout()).until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    /** Ekran locator'i degil, platforma gore degisen teknik kalip; bu yuzden Locators'ta degil burada durur. */
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
        appium().executeScript("mobile: scrollGesture", screenGesture(direction));
    }

    /** Scroll icerigi tarar, swipe tek bir hamledir; sayfa/kart gecislerinde swipe kullanilir. */
    private void swipe(String direction) {
        if (!Config.isAndroid()) {
            appium().executeScript("mobile: swipe", Map.of("direction", direction));
            return;
        }
        appium().executeScript("mobile: swipeGesture", screenGesture(direction));
    }

    private void swipeElement(By locator, String direction) {
        String elementId = elementId(waitVisible(locator));
        if (!Config.isAndroid()) {
            appium().executeScript("mobile: swipe", Map.of("elementId", elementId, "direction", direction));
            return;
        }
        appium().executeScript("mobile: swipeGesture",
                Map.of("elementId", elementId, "direction", direction, "percent", 0.75));
    }

    /** UiAutomator2 gesture'lari hedef alani piksel olarak ister: ekranin ortasindaki %80. */
    private Map<String, Object> screenGesture(String direction) {
        Dimension screen = appium().manage().window().getSize();
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("left", (int) (screen.getWidth() * 0.1));
        arguments.put("top", (int) (screen.getHeight() * 0.1));
        arguments.put("width", (int) (screen.getWidth() * 0.8));
        arguments.put("height", (int) (screen.getHeight() * 0.8));
        arguments.put("direction", direction);
        arguments.put("percent", 0.75);
        return arguments;
    }

    /** mobile: komutlari WebElement degil, elementin oturum icindeki id'sini bekler. */
    private static String elementId(WebElement element) {
        return ((RemoteWebElement) element).getId();
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
