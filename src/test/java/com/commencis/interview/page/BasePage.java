package com.commencis.interview.page;

import com.commencis.interview.platform.MobilePlatform;
import com.commencis.interview.util.ConfigReader;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.HidesKeyboard;
import io.appium.java_client.remote.SupportsContextSwitching;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Tum page class'larinin kullandigi ortak bekleme, tiklama ve kaydirma islemleri. */
public abstract class BasePage {

    private static final int MAX_SCROLL_ATTEMPT = 5;

    protected final AppiumDriver driver;
    protected final WebDriverWait wait;

    /** Hata mesajlarinda hangi ekranda olundugunu gostermek icin tutulur. */
    private final String pageName;

    protected BasePage(AppiumDriver driver, String pageName) {
        this.driver = driver;
        this.pageName = pageName;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(ConfigReader.getInt("mobile.explicit.wait.seconds", 15)));
    }

    /** Page'in ekran adi; locator class'indaki PAGE_NAME degerinden gelir. */
    public String getPageName() {
        return pageName;
    }

    // ------------------------------------------------------------------
    // Bekleme
    // ------------------------------------------------------------------

    /** Element gorunur olana kadar bekler ve dondurur. */
    protected WebElement waitForVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    /** Element tiklanabilir olana kadar bekler ve dondurur. */
    protected WebElement waitForClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    /** Element gorunmez olana kadar bekler; loading gostergesi gibi gecici elemanlar icin. */
    protected void waitForInvisible(By locator) {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    // ------------------------------------------------------------------
    // Etkilesim
    // ------------------------------------------------------------------

    /** Element tiklanabilir olana kadar bekler ve tiklar. */
    protected void click(By locator) {
        waitForClickable(locator).click();
    }

    /**
     * Element verilen sure icinde tiklanabilir olursa tiklar.
     * Opsiyonel popup/banner gibi her zaman cikmayan elemanlar icindir; tiklandiysa true doner.
     */
    protected boolean clickIfVisible(By locator, int seconds) {
        try {
            waitOf(seconds).until(ExpectedConditions.elementToBeClickable(locator)).click();
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    /** Alani temizlemeden yazar. */
    protected void type(By locator, String text) {
        waitForVisible(locator).sendKeys(text);
    }

    /** Alani temizler, sonra yazar. */
    protected void clearAndType(By locator, String text) {
        WebElement element = waitForVisible(locator);
        element.clear();
        element.sendKeys(text);
    }

    protected String getText(By locator) {
        return waitForVisible(locator).getText();
    }

    /**
     * Elementin attribute degerini dondurur.
     * iOS'ta gorunen metin cogunlukla getText() yerine "label" veya "value" attribute'unda olur.
     */
    protected String getAttribute(By locator, String attribute) {
        return waitForVisible(locator).getAttribute(attribute);
    }

    /** Element varsayilan bekleme suresi icinde gorunurse true, gorunmezse false doner. */
    protected boolean isDisplayed(By locator) {
        return isDisplayed(locator, defaultWaitSeconds());
    }

    /** Element verilen sure icinde gorunurse true, gorunmezse false doner. */
    protected boolean isDisplayed(By locator, int seconds) {
        try {
            waitOf(seconds).until(ExpectedConditions.visibilityOfElementLocated(locator));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    /** Locator'a uyan tum elemanlari dondurur; hicbiri yoksa bos liste. Bekleme yapmaz. */
    protected List<WebElement> findElements(By locator) {
        return driver.findElements(locator);
    }

    /** Klavyeyi kapatir. */
    protected void hideKeyboard() {
        if (driver instanceof HidesKeyboard keyboard) {
            keyboard.hideKeyboard();
        }
    }

    // ------------------------------------------------------------------
    // Liste islemleri
    // ------------------------------------------------------------------

    /**
     * Listede metni verilen degere birebir esit olan elemana tiklar.
     *
     * <p>Karsilastirma iki tarafta da trim edilerek yapilir, icerme (contains) kullanilmaz:
     * "Jupiter" araninca "Jupiter 2" secilmesin diye. Bulunamazsa listedeki tum metinleri
     * gostererek hata verir; sessizce baska elemana tiklamaz.
     */
    protected void selectListItemByText(By locator, String expectedText) {
        waitForVisible(locator);
        String expected = expectedText.trim();
        List<WebElement> items = findElements(locator);
        for (WebElement item : items) {
            if (expected.equals(item.getText().trim())) {
                item.click();
                return;
            }
        }
        throw new NoSuchElementException(String.format(
                "'%s' listede bulunamadi @ %s%nLocator: %s%nListedeki metinler:%n%s",
                expectedText, pageName, locator, formatItemTexts(items)));
    }

    /**
     * Listede verilen index'teki elemana tiklar.
     * Index liste disindaysa son elemana dusulmez; acik hata verilir.
     */
    protected void selectListItemByIndex(By locator, int index) {
        waitForVisible(locator);
        List<WebElement> items = findElements(locator);
        if (index < 0 || index >= items.size()) {
            throw new IndexOutOfBoundsException(String.format(
                    "Liste index'i gecersiz @ %s. Istenen index: %d, liste boyutu: %d.%nLocator: %s",
                    pageName, index, items.size(), locator));
        }
        items.get(index).click();
    }

    /**
     * Native dropdown'i acar ve secenegi tiklar.
     * Native mobilde Selenium Select calismaz; dropdown acilip acilan liste elemanina tiklanir.
     */
    protected void openDropdownAndSelect(By dropdown, By option) {
        click(dropdown);
        click(option);
    }

    // ------------------------------------------------------------------
    // Kaydirma
    // ------------------------------------------------------------------

    protected void scrollDown() {
        scroll("down");
    }

    protected void scrollUp() {
        scroll("up");
    }

    protected void swipeLeft() {
        swipe("left");
    }

    protected void swipeRight() {
        swipe("right");
    }

    /**
     * Element ekranda gorunur olana kadar asagi kaydirir.
     *
     * <p>Sayfa kaynaginda bulunup ekranda gorunmeyen eleman kaydirmayi durdurmaz; bu yuzden
     * varlik degil gorunurluk kontrol edilir. Dongu icinde uzun bekleme yapilmaz, son deneme
     * sonrasinda standart bekleme ile bir kez daha denenir.
     */
    protected WebElement scrollUntilVisible(By locator) {
        for (int attempt = 0; attempt < MAX_SCROLL_ATTEMPT; attempt++) {
            WebElement displayed = findDisplayed(locator);
            if (displayed != null) {
                return displayed;
            }
            scrollDown();
        }
        // Eleman kaydirma sonrasi gecikmeli render ediliyor olabilir; son sans.
        return waitForVisible(locator);
    }

    // ------------------------------------------------------------------
    // Context (WebView / native)
    // ------------------------------------------------------------------

    /**
     * Native app'ten WebView context'ine gecer.
     *
     * <p>Hybrid uygulamalarda WebView context'i sayfa yuklendikten birkac saniye sonra olusabilir;
     * bu yuzden liste tek seferde okunmaz, standart bekleme suresi boyunca yeniden sorgulanir.
     */
    protected void switchToWebView() {
        SupportsContextSwitching contextSwitching = contextSwitching();
        String webViewContext;
        try {
            webViewContext = wait.until(ignored -> findWebViewContext(contextSwitching));
        } catch (TimeoutException e) {
            throw new NoSuchElementException(String.format(
                    "WebView context bekleme suresi icinde olusmadi @ %s. Mevcut context'ler: %s",
                    pageName, contextSwitching.getContextHandles()), e);
        }
        contextSwitching.context(webViewContext);
    }

    /** Native app context'ine geri doner. */
    protected void switchToNativeContext() {
        contextSwitching().context("NATIVE_APP");
    }

    // ------------------------------------------------------------------
    // Private yardimcilar
    // ------------------------------------------------------------------

    private int defaultWaitSeconds() {
        return ConfigReader.getInt("mobile.explicit.wait.seconds", 15);
    }

    private WebDriverWait waitOf(int seconds) {
        return new WebDriverWait(driver, Duration.ofSeconds(seconds));
    }

    /** Locator'a uyan ve ekranda gorunen ilk elemani dondurur; yoksa null. Bekleme yapmaz. */
    private WebElement findDisplayed(By locator) {
        for (WebElement element : driver.findElements(locator)) {
            try {
                if (element.isDisplayed()) {
                    return element;
                }
            } catch (StaleElementReferenceException e) {
                // Liste kaydirma sirasinda yenilendi. Hata gizlenmiyor: bir sonraki denemede
                // yeniden aranir, hicbir denemede bulunamazsa scrollUntilVisible sonunda rapor edilir.
            }
        }
        return null;
    }

    /**
     * Kaydirma. Her platform kendi driver'inin native gesture komutunu kullanir:
     * Android'de UiAutomator2 'mobile: scrollGesture', iOS'ta XCUITest 'mobile: scroll'.
     */
    private void scroll(String direction) {
        if (MobilePlatform.current().isAndroid()) {
            driver.executeScript("mobile: scrollGesture", androidGestureArgs(direction));
        } else {
            driver.executeScript("mobile: scroll", Map.of("direction", direction));
        }
    }

    /** Kaydirma ile ayni platform ayrimi: 'mobile: swipeGesture' (Android) / 'mobile: swipe' (iOS). */
    private void swipe(String direction) {
        if (MobilePlatform.current().isAndroid()) {
            driver.executeScript("mobile: swipeGesture", androidGestureArgs(direction));
        } else {
            driver.executeScript("mobile: swipe", Map.of("direction", direction));
        }
    }

    /** UiAutomator2 gesture komutlari hedef alan ister; ekranin ortadaki %80'i kullanilir. */
    private Map<String, Object> androidGestureArgs(String direction) {
        Dimension screen = driver.manage().window().getSize();
        Map<String, Object> args = new HashMap<>();
        args.put("left", (int) (screen.getWidth() * 0.1));
        args.put("top", (int) (screen.getHeight() * 0.1));
        args.put("width", (int) (screen.getWidth() * 0.8));
        args.put("height", (int) (screen.getHeight() * 0.8));
        args.put("direction", direction);
        args.put("percent", 0.75);
        return args;
    }

    /** Polling icin: WebView context'i varsa dondurur, yoksa null (WebDriverWait tekrar dener). */
    private String findWebViewContext(SupportsContextSwitching contextSwitching) {
        return contextSwitching.getContextHandles().stream()
                .filter(context -> context.toLowerCase(Locale.ROOT).contains("webview"))
                .findFirst()
                .orElse(null);
    }

    private SupportsContextSwitching contextSwitching() {
        if (driver instanceof SupportsContextSwitching contextSwitching) {
            return contextSwitching;
        }
        throw new UnsupportedOperationException(
                "Driver context degistirmeyi desteklemiyor: " + driver.getClass().getName());
    }

    /** Liste elemanlarinin metinlerini numarali, okunabilir bicimde dondurur. */
    private String formatItemTexts(List<WebElement> items) {
        if (items.isEmpty()) {
            return "  (liste bos)";
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            String text;
            try {
                text = items.get(i).getText();
            } catch (StaleElementReferenceException e) {
                text = "[eleman yenilendi, metin okunamadi]";
            }
            builder.append(String.format("  %2d. \"%s\"%n", i + 1, text));
        }
        return builder.toString().stripTrailing();
    }
}
