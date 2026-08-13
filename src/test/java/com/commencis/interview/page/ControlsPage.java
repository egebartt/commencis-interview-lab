package com.commencis.interview.page;

import com.commencis.interview.locator.ControlsLocators;
import io.appium.java_client.AppiumDriver;

/**
 * Views > Controls > 1. Light Theme ekraninin aksiyonlari.
 *
 * <p>Bu ekran tek basina metin girisi, checkbox, radio grubu, toggle ve dropdown icerir.
 * Locator'lar {@link ControlsLocators} icinde tutulur; burada locator tanimi yapilmaz.
 *
 * <p>Metotlar assertion icermez. Dogrulamayi cagiran test yapar; Page yalnizca "ne yapilir"
 * ve "ekranda ne goruluyor" sorularina cevap verir.
 */
public class ControlsPage extends BasePage {

    public ControlsPage(AppiumDriver driver) {
        super(driver);
    }

    /** Form ekrani acildi mi? */
    public boolean isFormVisible() {
        return isDisplayed(ControlsLocators.TEXT_FIELD);
    }

    // ------------------------------------------------------------------
    // Metin alani
    // ------------------------------------------------------------------

    /** Alani temizleyip yazar ve klavyeyi kapatir; klavye alttaki kontrolleri ortmesin. */
    public void enterText(String text) {
        clearAndType(ControlsLocators.TEXT_FIELD, text);
        hideKeyboard();
    }

    public String getEnteredText() {
        return getText(ControlsLocators.TEXT_FIELD);
    }

    // ------------------------------------------------------------------
    // Checkbox
    // ------------------------------------------------------------------

    /** Checkbox 1'e tiklar. Tiklamak durumu tersine cevirir, mutlak deger atamaz. */
    public void clickFirstCheckbox() {
        click(ControlsLocators.CHECKBOX_1);
    }

    public boolean isFirstCheckboxChecked() {
        return isChecked(ControlsLocators.CHECKBOX_1);
    }

    public void clickStarCheckbox() {
        click(ControlsLocators.STAR_CHECKBOX);
    }

    public boolean isStarChecked() {
        return isChecked(ControlsLocators.STAR_CHECKBOX);
    }

    // ------------------------------------------------------------------
    // Radio grubu
    // ------------------------------------------------------------------

    public void selectFirstRadioButton() {
        click(ControlsLocators.RADIO_1);
    }

    public void selectSecondRadioButton() {
        click(ControlsLocators.RADIO_2);
    }

    public boolean isFirstRadioButtonSelected() {
        return isChecked(ControlsLocators.RADIO_1);
    }

    public boolean isSecondRadioButtonSelected() {
        return isChecked(ControlsLocators.RADIO_2);
    }

    // ------------------------------------------------------------------
    // Toggle
    // ------------------------------------------------------------------

    public void clickFirstToggle() {
        click(ControlsLocators.TOGGLE_1);
    }

    public boolean isFirstToggleOn() {
        return isChecked(ControlsLocators.TOGGLE_1);
    }

    /** Toggle'in uzerinde yazan metin: "ON" veya "OFF". */
    public String getFirstToggleLabel() {
        return getText(ControlsLocators.TOGGLE_1);
    }

    // ------------------------------------------------------------------
    // Dropdown
    // ------------------------------------------------------------------

    /** Dropdown'i acip verilen gezegeni secer. */
    public void selectPlanet(String planet) {
        selectOption(ControlsLocators.PLANET_SPINNER, ControlsLocators.planetOption(planet));
    }

    public String getSelectedPlanet() {
        return getText(ControlsLocators.PLANET_SPINNER_VALUE);
    }

    // ------------------------------------------------------------------
    // Buton durumlari
    // ------------------------------------------------------------------

    public boolean isSaveButtonEnabled() {
        return isEnabled(ControlsLocators.SAVE_BUTTON);
    }

    /** Ikinci Save butonu bilerek devre disidir; testte negatif dogrulama icin kullanilir. */
    public boolean isDisabledSaveButtonEnabled() {
        return isEnabled(ControlsLocators.DISABLED_SAVE_BUTTON);
    }
}
