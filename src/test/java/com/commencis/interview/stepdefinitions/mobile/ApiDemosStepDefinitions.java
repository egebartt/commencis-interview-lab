package com.commencis.interview.stepdefinitions.mobile;

import com.commencis.interview.mobile.pages.ApiDemosPage;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ApiDemos senaryolarinin Step Definition'lari.
 *
 * <p>Locator, bekleme veya driver kullanimi yoktur: yalnizca Page cagrisi ve dogrulama.
 * Page, PicoContainer tarafindan enjekte edilir; senaryo icinde elle olusturulmaz.
 */
public class ApiDemosStepDefinitions {

    private final ApiDemosPage apiDemos;

    public ApiDemosStepDefinitions(ApiDemosPage apiDemos) {
        this.apiDemos = apiDemos;
    }

    @Given("the Api Demos home screen is visible")
    public void homeScreenIsVisible() {
        assertTrue(apiDemos.isHomeVisible(), "Ana menu gorunmedi");
    }

    @When("the user opens the Views menu")
    public void openViewsMenu() {
        apiDemos.openViews();
    }

    @Then("the Buttons option should be visible")
    public void buttonsOptionIsVisible() {
        assertTrue(apiDemos.isButtonsOptionVisible(), "Views ekraninda Buttons gorunmedi");
    }

    @When("the user opens the Spinner screen")
    public void openSpinnerScreen() {
        apiDemos.openSpinner();
    }

    @When("the user selects {string} from the planet dropdown")
    public void selectPlanet(String planet) {
        apiDemos.selectPlanet(planet);
    }

    @Then("the selected planet should be {string}")
    public void selectedPlanetShouldBe(String expectedPlanet) {
        assertEquals(expectedPlanet, apiDemos.selectedPlanet(), "Secilen gezegen dropdown'da gorunmedi");
    }

    @When("the user opens the Switches screen")
    public void openSwitchesScreen() {
        apiDemos.openSwitches();
    }

    @When("the user taps the monitored switch")
    public void tapMonitoredSwitch() {
        apiDemos.tapMonitoredSwitch();
    }

    @Then("the monitored switch should be {word}")
    public void monitoredSwitchShouldBe(String expectedState) {
        boolean expectedOn = switch (expectedState) {
            case "on" -> true;
            case "off" -> false;
            default -> throw new IllegalArgumentException("Switch durumu 'on' veya 'off' olmali: " + expectedState);
        };
        assertEquals(expectedOn, apiDemos.isMonitoredSwitchOn(),
                "Monitored switch beklenen durumda degil: " + expectedState);
    }

    @Then("the {string} toast should be visible")
    public void toastShouldBeVisible(String message) {
        assertTrue(apiDemos.isToastVisible(message), "Toast mesaji gorunmedi: " + message);
    }
}
