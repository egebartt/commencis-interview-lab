package com.commencis.interview.mobile.pages;

import com.commencis.interview.mobile.actions.ElementActions;
import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Views &gt; Controls &gt; 1. Light Theme form ekraninin is akislari. Assertion icermez.
 *
 * <p>Bu ekrandaki her kontrolun kendi resource-id'si vardir, bu yuzden hepsinde id kullanilir.
 * Menu satirlarinda durum farklidir: orada tum satirlar android:id/text1 id'sini paylasir,
 * ayirt edici olan content-desc'tir (bkz. {@link ApiDemosPage}).
 */
public class ControlsPage extends BasePage {

    /** Generic adimlarda bu sayfayi bulmak icin okunabilir ad. */
    static final String PAGE_NAME = "Controls Page";

    private static final String ID_PREFIX = "io.appium.android.apis:id/";

    private static final By TEXT_FIELD = AppiumBy.id(ID_PREFIX + "edit");

    /** Ekranda iki Save butonu var: bu etkin olan. */
    private static final By SAVE_BUTTON = AppiumBy.id(ID_PREFIX + "button");
    /** Ikinci Save butonu bilerek devre disidir; negatif dogrulama icin kullanilir. */
    private static final By DISABLED_SAVE_BUTTON = AppiumBy.id(ID_PREFIX + "button_disabled");

    private static final By CHECKBOX_1 = AppiumBy.id(ID_PREFIX + "check1");
    private static final By CHECKBOX_2 = AppiumBy.id(ID_PREFIX + "check2");
    private static final By STAR_CHECKBOX = AppiumBy.id(ID_PREFIX + "star");

    private static final By RADIO_1 = AppiumBy.id(ID_PREFIX + "radio1");
    private static final By RADIO_2 = AppiumBy.id(ID_PREFIX + "radio2");

    private static final By TOGGLE_1 = AppiumBy.id(ID_PREFIX + "toggle1");
    private static final By TOGGLE_2 = AppiumBy.id(ID_PREFIX + "toggle2");

    private static final By PLANET_SPINNER = AppiumBy.id(ID_PREFIX + "spinner1");

    /**
     * Dropdown'da secili gorunen deger. android:id/text1 ekranda baska yerlerde de gectigi icin
     * childSelector ile spinner1'e kapsanir.
     */
    private static final By PLANET_SPINNER_VALUE = AppiumBy.androidUIAutomator(
            "new UiSelector().resourceId(\"" + ID_PREFIX + "spinner1\")"
                    + ".childSelector(new UiSelector().resourceId(\"android:id/text1\"))");

    public ControlsPage(ElementActions element) {
        super(element);
    }

    /** Generic adima acilan elemanlar; selector ikinci kez yazilmaz, sabitler adlariyla listelenir. */
    static Map<String, By> namedElements() {
        Map<String, By> elements = new LinkedHashMap<>();
        elements.put("TEXT_FIELD", TEXT_FIELD);
        elements.put("SAVE_BUTTON", SAVE_BUTTON);
        elements.put("DISABLED_SAVE_BUTTON", DISABLED_SAVE_BUTTON);
        elements.put("CHECKBOX_1", CHECKBOX_1);
        elements.put("CHECKBOX_2", CHECKBOX_2);
        elements.put("STAR_CHECKBOX", STAR_CHECKBOX);
        elements.put("RADIO_1", RADIO_1);
        elements.put("RADIO_2", RADIO_2);
        elements.put("TOGGLE_1", TOGGLE_1);
        elements.put("TOGGLE_2", TOGGLE_2);
        elements.put("PLANET_SPINNER", PLANET_SPINNER);
        return elements;
    }

    public boolean isFormVisible() {
        return element.isVisible(TEXT_FIELD);
    }

    /** Alani temizleyip yazar ve klavyeyi kapatir; klavye alttaki kontrolleri ortmesin. */
    public void enterText(String text) {
        element.type(TEXT_FIELD, text);
        element.hideKeyboard();
    }

    public String enteredText() {
        return element.text(TEXT_FIELD);
    }

    /** Tiklamak durumu tersine cevirir, mutlak deger atamaz. */
    public void tapFirstCheckbox() {
        element.click(CHECKBOX_1);
    }

    public boolean isFirstCheckboxChecked() {
        return element.isChecked(CHECKBOX_1);
    }

    public void selectFirstRadioButton() {
        element.click(RADIO_1);
    }

    public void selectSecondRadioButton() {
        element.click(RADIO_2);
    }

    public boolean isFirstRadioButtonSelected() {
        return element.isChecked(RADIO_1);
    }

    public boolean isSecondRadioButtonSelected() {
        return element.isChecked(RADIO_2);
    }

    public void tapFirstToggle() {
        element.click(TOGGLE_1);
    }

    public boolean isFirstToggleOn() {
        return element.isChecked(TOGGLE_1);
    }

    /** Toggle'in uzerinde yazan metin: "ON" veya "OFF". */
    public String firstToggleLabel() {
        return element.text(TOGGLE_1);
    }

    public void selectPlanet(String planet) {
        element.selectByText(PLANET_SPINNER, planet);
    }

    public String selectedPlanet() {
        return element.text(PLANET_SPINNER_VALUE);
    }

    public boolean isSaveButtonEnabled() {
        return element.isEnabled(SAVE_BUTTON);
    }

    /** Ikinci Save butonu bilerek devre disidir; negatif dogrulama icin kullanilir. */
    public boolean isDisabledSaveButtonEnabled() {
        return element.isEnabled(DISABLED_SAVE_BUTTON);
    }
}
