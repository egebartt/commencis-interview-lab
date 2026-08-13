package com.commencis.interview.page;

import com.commencis.interview.locator.ApiDemosLocators;
import io.appium.java_client.AppiumDriver;

public class ApiDemosPage extends BasePage {

    public ApiDemosPage(AppiumDriver driver) {
        super(driver);
    }

    public boolean isHomePageVisible() {
        return isDisplayed(ApiDemosLocators.ACCESSIBILITY_MENU);
    }

    public void openViews() {
        click(ApiDemosLocators.VIEWS_MENU);
    }

    public void openMenuItem(String title) {
        scrollAndClick(ApiDemosLocators.byText(title));
    }

    public boolean isButtonsOptionVisible() {
        return isDisplayed(ApiDemosLocators.BUTTONS_OPTION);
    }

    public void openSpinner() {
        scrollAndClick(ApiDemosLocators.SPINNER_OPTION);
    }
    public void openSwitches() {
        openMenuItem("Switches");
    }

    public void clickMonitoredSwitch() {
        click(ApiDemosLocators.MONITORED_SWITCH);
    }

    public boolean isMonitoredSwitchOn() {
        return isChecked(ApiDemosLocators.MONITORED_SWITCH);
    }

    public boolean isToastDisplayed(String message) {
        return isPresent(ApiDemosLocators.toastMessage(message), 3);
    }
    public void selectPlanet(String planet) {
        selectOption(ApiDemosLocators.PLANET_DROPDOWN, ApiDemosLocators.byText(planet));
    }

    public String getSelectedPlanet() {
        return getText(ApiDemosLocators.PLANET_DROPDOWN_VALUE);
    }

}
