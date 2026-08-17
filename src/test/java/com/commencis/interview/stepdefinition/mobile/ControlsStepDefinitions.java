package com.commencis.interview.stepdefinition.mobile;

import com.commencis.interview.mobile.page.ApiDemosPage;
import com.commencis.interview.mobile.page.ControlsPage;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Views > Controls form ekraninin is dili adimlari.
 *
 * <p>Bu ekran tek bir formun ic tutarliligini dogrular (radio grubunda tek secim, toggle
 * etiketi gibi); generic adimlarla yazmak akisi okunmaz hale getirirdi, bu yuzden Page Object
 * uzerinden yurutulur.
 */
public class ControlsStepDefinitions {

    private final ApiDemosPage menu;
    private final ControlsPage controls;

    public ControlsStepDefinitions(ApiDemosPage menu, ControlsPage controls) {
        this.menu = menu;
        this.controls = controls;
    }

    @Given("the Controls screen is open")
    public void openControlsScreen() {
        menu.openMenuItem("Views");
        menu.openMenuItem("Controls");
        menu.openMenuItem("1. Light Theme");

        assertTrue(controls.isFormVisible(), "Controls formu acilmadi");
    }

    @When("the user types {string} into the text field")
    public void typeIntoTextField(String text) {
        controls.enterText(text);
    }

    @Then("the text field should contain {string}")
    public void textFieldShouldContain(String expected) {
        assertEquals(expected, controls.getEnteredText(), "Metin alanina yazilan deger tutmadi");
    }

    @When("the user taps the first checkbox")
    public void tapFirstCheckbox() {
        controls.clickFirstCheckbox();
    }

    @Then("the first checkbox should be {word}")
    public void firstCheckboxShouldBe(String expectedState) {
        assertEquals(checkedState(expectedState), controls.isFirstCheckboxChecked(),
                "Checkbox 1 beklenen durumda degil: " + expectedState);
    }

    @When("the user selects the second radio button")
    public void selectSecondRadioButton() {
        controls.selectSecondRadioButton();
    }

    /** Radio grubunda tek secim olmasi bu ekranin asil dogrulamasidir. */
    @Then("only the second radio button should be selected")
    public void onlySecondRadioButtonIsSelected() {
        assertTrue(controls.isSecondRadioButtonSelected(), "RadioButton 2 secilmedi");
        assertFalse(controls.isFirstRadioButtonSelected(), "Radio grubunda yalnizca bir secim olmaliydi");
    }

    @When("the user taps the first toggle")
    public void tapFirstToggle() {
        controls.clickFirstToggle();
    }

    @Then("the first toggle should be on and labelled {string}")
    public void firstToggleIsOn(String expectedLabel) {
        assertTrue(controls.isFirstToggleOn(), "Toggle ON konumuna gecmedi");
        assertEquals(expectedLabel, controls.getFirstToggleLabel(), "Toggle etiketi beklenenden farkli");
    }

    @When("the user selects {string} from the Controls planet dropdown")
    public void selectPlanet(String planet) {
        controls.selectPlanet(planet);
    }

    @Then("the selected Controls planet should be {string}")
    public void selectedPlanetShouldBe(String expected) {
        assertEquals(expected, controls.getSelectedPlanet(), "Secilen gezegen dropdown'da gorunmedi");
    }

    @Then("the enabled Save button should be clickable")
    public void enabledSaveButtonIsClickable() {
        assertTrue(controls.isSaveButtonEnabled(), "Etkin Save butonu devre disi gorundu");
    }

    @Then("the disabled Save button should not be clickable")
    public void disabledSaveButtonIsNotClickable() {
        assertFalse(controls.isDisabledSaveButtonEnabled(), "Devre disi Save butonu etkin gorundu");
    }

    private static boolean checkedState(String state) {
        return switch (state) {
            case "checked" -> true;
            case "unchecked" -> false;
            default -> throw new IllegalArgumentException("Durum 'checked' veya 'unchecked' olmali: " + state);
        };
    }
}
