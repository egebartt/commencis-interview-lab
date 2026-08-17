package com.commencis.interview.mobile.element;

import com.commencis.interview.core.config.Config;
import com.commencis.interview.core.config.MobilePlatform;
import com.commencis.interview.mobile.driver.DriverManager;
import com.commencis.interview.mobile.locator.LocatorRegistry;
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
import java.util.function.Supplier;

/**
 * Tum mobil etkilesimlerin tek dusuk seviye katmani.
 *
 * <p>Iki cagri bicimi vardir ve ayni koda inerler:
 * <ul>
 *   <li>{@code click(By)} — Page Object'ler kendi locator'lariyla cagirir</li>
 *   <li>{@code click("Api Demos Page", "VIEWS_MENU")} — generic Cucumber adimlari isimle cagirir</li>
 * </ul>
 *
 * <p>Driver referansi degil, driver'i uretecek fonksiyon tutulur: strict {@link DriverManager}
 * ile Cucumber, lazy {@link DriverManager} ile JUnit ayni sinifi kullanabilir.
 */
public class ElementActions {

    private static final int MAX_SCROLL_ATTEMPT = 5;

    private final Supplier<AppiumDriver> driverSupplier;

    /** PicoContainer bu constructor'i kullanir. */
    public ElementActions(DriverManager drivers) {
        this(drivers::driver);
    }

    private ElementActions(Supplier<AppiumDriver> driverSupplier) {
        this.driverSupplier = driverSupplier;
    }

    /** Elinde hazir bir driver varken (JUnit) kullanilir. */
    public static ElementActions on(AppiumDriver driver) {
        return new ElementActions(() -> driver);
    }

    public AppiumDriver driver() {
        return driverSupplier.get();
    }

    // ------------------------------------------------------------------
    // Sayfa adi + element adi ile cagri
    // ------------------------------------------------------------------

    public void click(String pageName, String elementKey) {
        click(LocatorRegistry.find(pageName, elementKey));
    }

    public void type(String pageName, String elementKey, String text) {
        type(LocatorRegistry.find(pageName, elementKey), text);
    }

    public void clear(String pageName, String elementKey) {
        clear(LocatorRegistry.find(pageName, elementKey));
    }

    public void waitForVisible(String pageName, String elementKey) {
        waitForVisible(LocatorRegistry.find(pageName, elementKey));
    }

    public void waitForInvisible(String pageName, String elementKey) {
        waitForInvisible(LocatorRegistry.find(pageName, elementKey));
    }

    public boolean isVisible(String pageName, String elementKey) {
        return isVisible(LocatorRegistry.find(pageName, elementKey));
    }

    public String text(String pageName, String elementKey) {
        return text(LocatorRegistry.find(pageName, elementKey));
    }

    public boolean isChecked(String pageName, String elementKey) {
        return isChecked(LocatorRegistry.find(pageName, elementKey));
    }

    public boolean isEnabled(String pageName, String elementKey) {
        return isEnabled(LocatorRegistry.find(pageName, elementKey));
    }

    public void scrollAndClick(String pageName, String elementKey) {
        scrollAndClick(LocatorRegistry.find(pageName, elementKey));
    }

    // ------------------------------------------------------------------
    // Locator ile cagri
    // ------------------------------------------------------------------

    /** Element tiklanabilir olana kadar bekleyip tiklar. */
    public void click(By locator) {
        defaultWait().until(ExpectedConditions.elementToBeClickable(locator)).click();
    }

    /** Alani gorunur olunca temizleyip yeni metni yazar. */
    public void type(By locator, String text) {
        WebElement element = waitForVisible(locator);
        element.clear();
        element.sendKeys(text);
    }

    public void clear(By locator) {
        waitForVisible(locator).clear();
    }

    public WebElement waitForVisible(By locator) {
        return defaultWait().until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public void waitForInvisible(By locator) {
        defaultWait().until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    /** Element varsayilan bekleme suresinde gorunurse true dondurur. */
    public boolean isVisible(By locator) {
        try {
            waitForVisible(locator);
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    /** Element verilen surede UI hiyerarsisinde bulunursa true dondurur. */
    public boolean isPresent(By locator, int seconds) {
        try {
            waitOf(seconds).until(ExpectedConditions.presenceOfElementLocated(locator));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    public String text(By locator) {
        return waitForVisible(locator).getText();
    }

    public String attribute(By locator, String attribute) {
        return waitForVisible(locator).getAttribute(attribute);
    }

    /** Checkbox, radio veya switch elementinin secili durumu; attribute adi platforma gore degisir. */
    public boolean isChecked(By locator) {
        String value = MobilePlatform.current().isAndroid()
                ? attribute(locator, "checked")
                : attribute(locator, "value");
        return "true".equalsIgnoreCase(value) || "1".equals(value);
    }

    public boolean isEnabled(By locator) {
        return waitForVisible(locator).isEnabled();
    }

    public void selectOption(By dropdown, By option) {
        click(dropdown);
        click(option);
    }

    public WebElement scrollUntilVisible(By locator) {
        for (int attempt = 0; attempt < MAX_SCROLL_ATTEMPT; attempt++) {
            WebElement displayed = findDisplayed(locator);
            if (displayed != null) {
                return displayed;
            }
            scrollDown();
        }
        return waitForVisible(locator);
    }

    public void scrollAndClick(By locator) {
        scrollUntilVisible(locator).click();
    }

    public void hideKeyboard() {
        if (driver() instanceof HidesKeyboard keyboard) {
            try {
                keyboard.hideKeyboard();
            } catch (WebDriverException ignored) {
                // Klavye zaten kapali olabilir.
            }
        }
    }

    public void back() {
        driver().navigate().back();
    }

    /** Aktif platformun native gesture komutuyla asagi kaydirir. */
    public void scrollDown() {
        if (MobilePlatform.current().isAndroid()) {
            driver().executeScript("mobile: scrollGesture", androidScrollArguments("down"));
        } else {
            driver().executeScript("mobile: scroll", Map.of("direction", "down"));
        }
    }

    public void scrollUp() {
        if (MobilePlatform.current().isAndroid()) {
            driver().executeScript("mobile: scrollGesture", androidScrollArguments("up"));
        } else {
            driver().executeScript("mobile: scroll", Map.of("direction", "up"));
        }
    }

    private WebDriverWait defaultWait() {
        return waitOf(Config.getInt("mobile.explicit.wait.seconds", 15));
    }

    private WebDriverWait waitOf(int seconds) {
        return new WebDriverWait(driver(), Duration.ofSeconds(seconds));
    }

    /** Locator'a uyan gorunur ilk elementi beklemeden dondurur. */
    private WebElement findDisplayed(By locator) {
        for (WebElement element : driver().findElements(locator)) {
            try {
                if (element.isDisplayed()) {
                    return element;
                }
            } catch (StaleElementReferenceException ignored) {
                // Scroll sirasinda yenilenen element bir sonraki denemede tekrar aranir.
            }
        }
        return null;
    }

    /** Android scroll gesture icin ekranin orta alanini hedefleyen argumanlar. */
    private Map<String, Object> androidScrollArguments(String direction) {
        Dimension screen = driver().manage().window().getSize();
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("left", (int) (screen.getWidth() * 0.1));
        arguments.put("top", (int) (screen.getHeight() * 0.1));
        arguments.put("width", (int) (screen.getWidth() * 0.8));
        arguments.put("height", (int) (screen.getHeight() * 0.8));
        arguments.put("direction", direction);
        arguments.put("percent", 0.75);
        return arguments;
    }
}
