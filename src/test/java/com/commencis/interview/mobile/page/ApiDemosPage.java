package com.commencis.interview.mobile.page;

import com.commencis.interview.mobile.element.ElementActions;
import com.commencis.interview.mobile.locator.ApiDemosLocators;
import com.commencis.interview.mobile.locator.DynamicLocators;

/** ApiDemos ana menu ve Views ekranlarinin is akislari. Assertion icermez. */
public class ApiDemosPage extends BasePage {

    public ApiDemosPage(ElementActions ui) {
        super(ui);
    }

    public boolean isHomePageVisible() {
        return ui.isVisible(ApiDemosLocators.ACCESSIBILITY_MENU);
    }

    public void openViews() {
        ui.click(ApiDemosLocators.VIEWS_MENU);
    }

    /** Menu satirini metniyle acar; listedeyse once ona kadar kaydirir. */
    public void openMenuItem(String title) {
        ui.scrollAndClick(DynamicLocators.byText(title));
    }

    public boolean isButtonsOptionVisible() {
        return ui.isVisible(ApiDemosLocators.BUTTONS_OPTION);
    }

    public void openSpinner() {
        ui.scrollAndClick(ApiDemosLocators.SPINNER_OPTION);
    }

    public void openSwitches() {
        ui.scrollAndClick(ApiDemosLocators.SWITCHES_OPTION);
    }

    public void clickMonitoredSwitch() {
        ui.click(ApiDemosLocators.MONITORED_SWITCH);
    }

    public boolean isMonitoredSwitchOn() {
        return ui.isChecked(ApiDemosLocators.MONITORED_SWITCH);
    }

    /** Toast kisa omurludur; varsayilan bekleme yerine kisa bir kontrol yapilir. */
    public boolean isToastDisplayed(String message) {
        return ui.isPresent(DynamicLocators.toast(message), 3);
    }

    public void selectPlanet(String planet) {
        ui.selectOption(ApiDemosLocators.PLANET_DROPDOWN, DynamicLocators.byText(planet));
    }

    public String getSelectedPlanet() {
        return ui.text(ApiDemosLocators.PLANET_DROPDOWN_VALUE);
    }
}
