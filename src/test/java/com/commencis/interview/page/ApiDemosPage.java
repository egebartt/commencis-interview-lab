package com.commencis.interview.page;

import com.commencis.interview.locator.ApiDemosLocators;
import io.appium.java_client.AppiumDriver;

/**
 * ApiDemos uygulamasinin ekran aksiyonlari.
 * Locator'lar {@link ApiDemosLocators} icinde tutulur.
 */
public class ApiDemosPage extends BasePage {

    public ApiDemosPage(AppiumDriver driver) {
        super(driver, ApiDemosLocators.PAGE_NAME);
    }

    /** Ana menu acik mi? */
    public boolean isHomePageVisible() {
        return isDisplayed(ApiDemosLocators.ACCESSIBILITY_MENU);
    }

    public void openViews() {
        click(ApiDemosLocators.VIEWS_MENU);
    }

    public boolean isButtonsOptionVisible() {
        return isDisplayed(ApiDemosLocators.BUTTONS_OPTION);
    }

    /** Views listesinde asagi kaydirarak Spinner secenegini acar. */
    public void openSpinner() {
        scrollUntilVisible(ApiDemosLocators.SPINNER_OPTION).click();
    }

    /** Dropdown'i acip verilen gezegeni secer (Views > Spinner ekrani). */
    public void selectPlanet(String planet) {
        openDropdownAndSelect(ApiDemosLocators.PLANET_DROPDOWN, ApiDemosLocators.byText(planet));
    }

    public String getSelectedPlanet() {
        return getText(ApiDemosLocators.PLANET_DROPDOWN_VALUE);
    }

    /** Cihazin geri tusu. */
    public void goBack() {
        driver.navigate().back();
    }
}
