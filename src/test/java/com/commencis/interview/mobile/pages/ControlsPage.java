package com.commencis.interview.mobile.pages;

import com.commencis.interview.mobile.actions.MobileActions;

import static com.commencis.interview.mobile.locators.ControlsLocators.CHECKBOX_1;
import static com.commencis.interview.mobile.locators.ControlsLocators.DISABLED_SAVE_BUTTON;
import static com.commencis.interview.mobile.locators.ControlsLocators.PLANET_SPINNER;
import static com.commencis.interview.mobile.locators.ControlsLocators.PLANET_SPINNER_VALUE;
import static com.commencis.interview.mobile.locators.ControlsLocators.RADIO_1;
import static com.commencis.interview.mobile.locators.ControlsLocators.RADIO_2;
import static com.commencis.interview.mobile.locators.ControlsLocators.SAVE_BUTTON;
import static com.commencis.interview.mobile.locators.ControlsLocators.TEXT_FIELD;
import static com.commencis.interview.mobile.locators.ControlsLocators.TOGGLE_1;

public class ControlsPage extends BasePage {

    public ControlsPage(MobileActions mobile) {
        super(mobile);
    }

    public boolean isFormVisible() {
        return mobile.isVisible(TEXT_FIELD);
    }

    /** Alani temizleyip yazar ve klavyeyi kapatir; klavye alttaki kontrolleri ortmesin. */
    public void enterText(String text) {
        mobile.type(TEXT_FIELD, text);
        mobile.hideKeyboard();
    }

    public String enteredText() {
        return mobile.text(TEXT_FIELD);
    }

    /** Tiklamak durumu tersine cevirir, mutlak deger atamaz. */
    public void tapFirstCheckbox() {
        mobile.click(CHECKBOX_1);
    }

    public boolean isFirstCheckboxChecked() {
        return mobile.isChecked(CHECKBOX_1);
    }

    public void selectFirstRadioButton() {
        mobile.click(RADIO_1);
    }

    public void selectSecondRadioButton() {
        mobile.click(RADIO_2);
    }

    public boolean isFirstRadioButtonSelected() {
        return mobile.isChecked(RADIO_1);
    }

    public boolean isSecondRadioButtonSelected() {
        return mobile.isChecked(RADIO_2);
    }

    public void tapFirstToggle() {
        mobile.click(TOGGLE_1);
    }

    public boolean isFirstToggleOn() {
        return mobile.isChecked(TOGGLE_1);
    }

    /** Toggle'in uzerinde yazan metin: "ON" veya "OFF". */
    public String firstToggleLabel() {
        return mobile.text(TOGGLE_1);
    }

    public void selectPlanet(String planet) {
        mobile.selectByText(PLANET_SPINNER, planet);
    }

    public String selectedPlanet() {
        return mobile.text(PLANET_SPINNER_VALUE);
    }

    public boolean isSaveButtonEnabled() {
        return mobile.isEnabled(SAVE_BUTTON);
    }

    /** Ikinci Save butonu bilerek devre disidir; negatif dogrulama icin kullanilir. */
    public boolean isDisabledSaveButtonEnabled() {
        return mobile.isEnabled(DISABLED_SAVE_BUTTON);
    }
}
