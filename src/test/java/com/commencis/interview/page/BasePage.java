package com.commencis.interview.page;

import com.commencis.interview.platform.MobilePlatform;
import com.commencis.interview.util.ConfigReader;
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

public abstract class BasePage {

    private static final int MAX_SCROLL_ATTEMPT = 5;

    private final AppiumDriver driver;
    private final WebDriverWait wait;

    /** Page siniflarinin kullanacagi driver ve explicit wait'i hazirlar. */
    protected BasePage(AppiumDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(
                driver,
                Duration.ofSeconds(ConfigReader.getInt("mobile.explicit.wait.seconds", 15))
        );
    }

    /** Element tiklanabilir olana kadar bekleyip tiklar. */
    protected void click(By locator) {
        wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
    }

    /** Alani gorunur olunca temizleyip yeni metni yazar. */
    protected void clearAndType(By locator, String text) {
        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        element.clear();
        element.sendKeys(text);
    }

    /** Gorunur elementin metnini dondurur. */
    protected String getText(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator)).getText();
    }

    /** Gorunur elementin istenen attribute degerini dondurur. */
    protected String getAttribute(By locator, String attribute) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator)).getAttribute(attribute);
    }

    /** Element varsayilan bekleme suresinde gorunurse true dondurur. */
    protected boolean isDisplayed(By locator) {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    /** Element verilen surede UI hiyerarsisinde bulunursa true dondurur. */
    protected boolean isPresent(By locator, int seconds) {
        try {
            waitOf(seconds).until(ExpectedConditions.presenceOfElementLocated(locator));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    /** Checkbox, radio veya switch elementinin secili durumunu dondurur. */
    protected boolean isChecked(By locator) {
        String value = MobilePlatform.current().isAndroid()
                ? getAttribute(locator, "checked")
                : getAttribute(locator, "value");
        return "true".equalsIgnoreCase(value) || "1".equals(value);
    }

    /** Gorunur element etkilesime aciksa true dondurur. */
    protected boolean isEnabled(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator)).isEnabled();
    }

    /** Aciksa mobil klavyeyi kapatir. */
    protected void hideKeyboard() {
        if (driver instanceof HidesKeyboard keyboard) {
            try {
                keyboard.hideKeyboard();
            } catch (WebDriverException ignored) {
                // Klavye zaten kapali olabilir.
            }
        }
    }

    /** Dropdown'i acip verilen secenegi secer. */
    protected void selectOption(By dropdown, By option) {
        click(dropdown);
        click(option);
    }

    /** Hedef element gorunene kadar asagi kaydirip elementi dondurur. */
    protected WebElement scrollUntilVisible(By locator) {
        for (int attempt = 0; attempt < MAX_SCROLL_ATTEMPT; attempt++) {
            WebElement displayedElement = findDisplayed(locator);
            if (displayedElement != null) {
                return displayedElement;
            }
            scrollDown();
        }
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    /** Hedef element gorunene kadar asagi kaydirip tiklar. */
    protected void scrollAndClick(By locator) {
        scrollUntilVisible(locator).click();
    }

    /** Cihazin geri aksiyonunu calistirir. */
    public void goBack() {
        driver.navigate().back();
    }

    /** Verilen sureye sahip yeni bir explicit wait dondurur. */
    private WebDriverWait waitOf(int seconds) {
        return new WebDriverWait(driver, Duration.ofSeconds(seconds));
    }

    /** Locator'a uyan gorunur ilk elementi beklemeden dondurur. */
    private WebElement findDisplayed(By locator) {
        for (WebElement element : driver.findElements(locator)) {
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

    /** Aktif platformun native gesture komutuyla asagi kaydirir. */
    private void scrollDown() {
        if (MobilePlatform.current().isAndroid()) {
            driver.executeScript("mobile: scrollGesture", androidScrollArguments());
        } else {
            driver.executeScript("mobile: scroll", Map.of("direction", "down"));
        }
    }

    /** Android scroll gesture icin ekranin orta alanini hedefleyen argumanlari dondurur. */
    private Map<String, Object> androidScrollArguments() {
        Dimension screen = driver.manage().window().getSize();
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("left", (int) (screen.getWidth() * 0.1));
        arguments.put("top", (int) (screen.getHeight() * 0.1));
        arguments.put("width", (int) (screen.getWidth() * 0.8));
        arguments.put("height", (int) (screen.getHeight() * 0.8));
        arguments.put("direction", "down");
        arguments.put("percent", 0.75);
        return arguments;
    }
}
