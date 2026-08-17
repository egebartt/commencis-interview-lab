package com.commencis.interview.mobile.page;

import com.commencis.interview.mobile.element.ElementActions;
import com.commencis.interview.mobile.locator.ControlsLocators;

/**
 * Views > Controls > 1. Light Theme ekraninin is akislari.
 * Metotlar assertion icermez; dogrulamayi cagiran taraf yapar.
 */
public class ControlsPage extends BasePage {

    public ControlsPage(ElementActions ui) {
        super(ui);
    }

    public boolean isFormVisible() {
        return ui.isVisible(ControlsLocators.TEXT_FIELD);
    }

    /** Alani temizleyip yazar ve klavyeyi kapatir; klavye alttaki kontrolleri ortmesin. */
    public void enterText(String text) {
        ui.type(ControlsLocators.TEXT_FIELD, text);
        ui.hideKeyboard();
    }

    public String getEnteredText() {
        return ui.text(ControlsLocators.TEXT_FIELD);
    }

    /** Tiklamak durumu tersine cevirir, mutlak deger atamaz. */
    public void clickFirstCheckbox() {
        ui.click(ControlsLocators.CHECKBOX_1);
    }

    public boolean isFirstCheckboxChecked() {
        return ui.isChecked(ControlsLocators.CHECKBOX_1);
    }

    public void clickStarCheckbox() {
        ui.click(ControlsLocators.STAR_CHECKBOX);
    }

    public boolean isStarChecked() {
        return ui.isChecked(ControlsLocators.STAR_CHECKBOX);
    }

    public void selectFirstRadioButton() {
        ui.click(ControlsLocators.RADIO_1);
    }

    public void selectSecondRadioButton() {
        ui.click(ControlsLocators.RADIO_2);
    }

    public boolean isFirstRadioButtonSelected() {
        return ui.isChecked(ControlsLocators.RADIO_1);
    }

    public boolean isSecondRadioButtonSelected() {
        return ui.isChecked(ControlsLocators.RADIO_2);
    }

    public void clickFirstToggle() {
        ui.click(ControlsLocators.TOGGLE_1);
    }

    public boolean isFirstToggleOn() {
        return ui.isChecked(ControlsLocators.TOGGLE_1);
    }

    /** Toggle'in uzerinde yazan metin: "ON" veya "OFF". */
    public String getFirstToggleLabel() {
        return ui.text(ControlsLocators.TOGGLE_1);
    }

    public void selectPlanet(String planet) {
        ui.selectOption(ControlsLocators.PLANET_SPINNER, ControlsLocators.planetOption(planet));
    }

    public String getSelectedPlanet() {
        return ui.text(ControlsLocators.PLANET_SPINNER_VALUE);
    }

    public boolean isSaveButtonEnabled() {
        return ui.isEnabled(ControlsLocators.SAVE_BUTTON);
    }

    /** Ikinci Save butonu bilerek devre disidir; negatif dogrulama icin kullanilir. */
    public boolean isDisabledSaveButtonEnabled() {
        return ui.isEnabled(ControlsLocators.DISABLED_SAVE_BUTTON);
    }
}
