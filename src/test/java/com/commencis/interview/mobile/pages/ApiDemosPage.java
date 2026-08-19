package com.commencis.interview.mobile.pages;

import com.commencis.interview.mobile.actions.MobileActions;

import static com.commencis.interview.mobile.locators.ApiDemosLocators.ACCESSIBILITY_MENU;
import static com.commencis.interview.mobile.locators.ApiDemosLocators.BUTTONS_OPTION;
import static com.commencis.interview.mobile.locators.ApiDemosLocators.MONITORED_SWITCH;
import static com.commencis.interview.mobile.locators.ApiDemosLocators.PLANET_DROPDOWN;
import static com.commencis.interview.mobile.locators.ApiDemosLocators.PLANET_DROPDOWN_VALUE;
import static com.commencis.interview.mobile.locators.ApiDemosLocators.SPINNER_OPTION;
import static com.commencis.interview.mobile.locators.ApiDemosLocators.SWITCHES_OPTION;
import static com.commencis.interview.mobile.locators.ApiDemosLocators.VIEWS_MENU;

public class ApiDemosPage extends BasePage {

    /** Toast kisa omurludur; varsayilan bekleme yerine kisa bir kontrol yapilir. */
    private static final int TOAST_TIMEOUT_SECONDS = 3;

    /** PicoContainer bu constructor'i kullanir; senaryonun MobileActions'i enjekte edilir. */
    public ApiDemosPage(MobileActions mobile) {
        super(mobile);
    }

    public boolean isHomeVisible() {
        return mobile.isVisible(ACCESSIBILITY_MENU);
    }

    public void openViews() {
        mobile.click(VIEWS_MENU);
    }

    /** Menu satirini metniyle acar; listede asagidaysa once ona kadar kaydirir. */
    public void openMenuItem(String title) {
        mobile.scrollAndClickText(title);
    }

    public boolean isButtonsOptionVisible() {
        return mobile.isVisible(BUTTONS_OPTION);
    }

    public void openSpinner() {
        mobile.scrollAndClick(SPINNER_OPTION);
    }

    public void openSwitches() {
        mobile.scrollAndClick(SWITCHES_OPTION);
    }

    public void tapMonitoredSwitch() {
        mobile.click(MONITORED_SWITCH);
    }

    public boolean isMonitoredSwitchOn() {
        return mobile.isChecked(MONITORED_SWITCH);
    }

    public boolean isToastVisible(String message) {
        return mobile.isToastVisible(message, TOAST_TIMEOUT_SECONDS);
    }

    public void selectPlanet(String planet) {
        mobile.selectByText(PLANET_DROPDOWN, planet);
    }

    public String selectedPlanet() {
        return mobile.text(PLANET_DROPDOWN_VALUE);
    }
}
