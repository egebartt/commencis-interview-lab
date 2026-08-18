package com.commencis.interview.mobile.pages;

import com.commencis.interview.mobile.actions.ElementActions;
import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ApiDemos ana menu ve Views ekranlarinin is akislari.
 *
 * <p>Ekranin locator'lari bu dosyada durur: elemanin nerede oldugu ile o elemanla ne yapildigi
 * ayni yerde okunur, degistiginde tek dosya guncellenir. Locator'lar {@code private}'tir; disari
 * sizmaz, Step Definition onlari goremez.
 *
 * <p>Assertion icermez: "ekranda bu is nasil yapilir" sorusunu cevaplar, "dogru mu" sorusunu
 * Step Definition cevaplar. Bu yuzden dogrulama icin boolean/String dondurur.
 *
 * <p>Locator onceligi: accessibilityId &gt; id &gt; UiAutomator selector &gt; XPath.
 *
 * <p>Ayni elemanin Android ve iOS locator'i farkliysa platforma gore secilir:
 * <pre>
 * private static final By LOGIN = Config.isAndroid()
 *         ? AppiumBy.id("com.example:id/loginButton")
 *         : AppiumBy.accessibilityId("loginButton");
 * </pre>
 * ApiDemos yalnizca Android uygulamasi oldugu icin burada tek locator yazilir.
 */
public class ApiDemosPage extends BasePage {

    /** Toast kisa omurludur; varsayilan bekleme yerine kisa bir kontrol yapilir. */
    private static final int TOAST_TIMEOUT_SECONDS = 3;

    /** Generic adimlarda ({@code ... in "Api Demos Page"}) bu sayfayi bulmak icin okunabilir ad. */
    static final String PAGE_NAME = "Api Demos Page";

    /** Uygulamanin id on eki; her locator'da tekrar yazilmasin diye ayrildi. */
    private static final String ID_PREFIX = "io.appium.android.apis:id/";

    private static final By ACCESSIBILITY_MENU = AppiumBy.accessibilityId("Accessibility");
    private static final By VIEWS_MENU = AppiumBy.accessibilityId("Views");
    private static final By BUTTONS_OPTION = AppiumBy.accessibilityId("Buttons");
    private static final By SPINNER_OPTION = AppiumBy.accessibilityId("Spinner");
    private static final By SWITCHES_OPTION = AppiumBy.accessibilityId("Switches");

    private static final By MONITORED_SWITCH = AppiumBy.id(ID_PREFIX + "monitored_switch");

    /** Views &gt; Spinner ekraninda iki dropdown var: spinner1 = "Color:", spinner2 = "Planet:". */
    private static final By PLANET_DROPDOWN = AppiumBy.id(ID_PREFIX + "spinner2");

    /**
     * Dropdown'da secili gorunen deger. Spinner container'inin kendi text'i bostur; secili deger
     * icindeki TextView'da durur. android:id/text1 iki spinner'da da ayni oldugu icin
     * childSelector ile spinner2'ye kapsanir.
     */
    private static final By PLANET_DROPDOWN_VALUE = AppiumBy.androidUIAutomator(
            "new UiSelector().resourceId(\"" + ID_PREFIX + "spinner2\")"
                    + ".childSelector(new UiSelector().resourceId(\"android:id/text1\"))");

    /** PicoContainer bu constructor'i kullanir; senaryonun ElementActions'i enjekte edilir. */
    public ApiDemosPage(ElementActions element) {
        super(element);
    }

    /**
     * Generic adima ({@code Click to element "VIEWS_MENU" in "Api Demos Page"}) acilan elemanlar.
     *
     * <p>Locator'in sahibi yine bu sinif: burada yalnizca ayni sabitler adlariyla listelenir,
     * selector ikinci kez yazilmaz. Package-private oldugu icin yalnizca
     * {@link PageElementCatalog} okur.
     */
    static Map<String, By> namedElements() {
        Map<String, By> elements = new LinkedHashMap<>();
        elements.put("ACCESSIBILITY_MENU", ACCESSIBILITY_MENU);
        elements.put("VIEWS_MENU", VIEWS_MENU);
        elements.put("BUTTONS_OPTION", BUTTONS_OPTION);
        elements.put("SPINNER_OPTION", SPINNER_OPTION);
        elements.put("SWITCHES_OPTION", SWITCHES_OPTION);
        elements.put("MONITORED_SWITCH", MONITORED_SWITCH);
        elements.put("PLANET_DROPDOWN", PLANET_DROPDOWN);
        return elements;
    }

    public boolean isHomeVisible() {
        return element.isVisible(ACCESSIBILITY_MENU);
    }

    public void openViews() {
        element.click(VIEWS_MENU);
    }

    /** Menu satirini metniyle acar; listede asagidaysa once ona kadar kaydirir. */
    public void openMenuItem(String title) {
        element.scrollAndClickText(title);
    }

    public boolean isButtonsOptionVisible() {
        return element.isVisible(BUTTONS_OPTION);
    }

    public void openSpinner() {
        element.scrollAndClick(SPINNER_OPTION);
    }

    public void openSwitches() {
        element.scrollAndClick(SWITCHES_OPTION);
    }

    public void tapMonitoredSwitch() {
        element.click(MONITORED_SWITCH);
    }

    public boolean isMonitoredSwitchOn() {
        return element.isChecked(MONITORED_SWITCH);
    }

    public boolean isToastVisible(String message) {
        return element.isToastVisible(message, TOAST_TIMEOUT_SECONDS);
    }

    public void selectPlanet(String planet) {
        element.selectByText(PLANET_DROPDOWN, planet);
    }

    public String selectedPlanet() {
        return element.text(PLANET_DROPDOWN_VALUE);
    }
}
