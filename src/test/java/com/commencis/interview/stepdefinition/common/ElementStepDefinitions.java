package com.commencis.interview.stepdefinition.common;

import com.commencis.interview.core.context.ScenarioContext;
import com.commencis.interview.core.data.Placeholders;
import com.commencis.interview.mobile.element.ElementActions;
import com.commencis.interview.mobile.locator.DynamicLocators;
import com.commencis.interview.mobile.locator.LocatorRegistry;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Sayfa adi + element adi ile calisan genel mobil adimlar.
 *
 * <pre>
 * * Click to element "VIEWS_MENU" in "Api Demos Page"
 * </pre>
 *
 * <p>Gherkin'e locator degil <b>element adi</b> girer; locator'in kendisi {@code *Locators}
 * sinifinda kalir ve {@link LocatorRegistry} uzerinden cozulur. Yeni bir ekran icin adim yazmak
 * gerekmez, locator eklemek yeterlidir.
 *
 * <p>Cok adimli is akislari icin Page Object tabanli adimlar kullanilir; bu katman hiz ve
 * kesif icindir.
 */
public class ElementStepDefinitions {

    private static final int TOAST_TIMEOUT_SECONDS = 3;

    private final ElementActions ui;
    private final ScenarioContext context;
    private final Placeholders placeholders;

    public ElementStepDefinitions(ElementActions ui, ScenarioContext context, Placeholders placeholders) {
        this.ui = ui;
        this.context = context;
        this.placeholders = placeholders;
    }

    @When("Click to element {string} in {string}")
    public void clickElement(String key, String page) {
        ui.click(page, key);
    }

    /** Degeri senaryodan gelen elementler icin; locator sinifinda karsiligi yoktur. */
    @When("Click to element with text {string}")
    public void clickElementWithText(String text) {
        ui.click(DynamicLocators.byText(placeholders.resolve(text)));
    }

    @When("Write {string} to element {string} in {string}")
    public void writeToElement(String text, String key, String page) {
        ui.type(page, key, placeholders.resolve(text));
    }

    @When("Clear text of element {string} in {string}")
    public void clearElement(String key, String page) {
        ui.clear(page, key);
    }

    @When("Wait for element {string} in {string}")
    public void waitForElement(String key, String page) {
        ui.waitForVisible(page, key);
    }

    @When("Scroll to element {string} and click in {string}")
    public void scrollToElementAndClick(String key, String page) {
        ui.scrollAndClick(page, key);
    }

    @When("Navigate back")
    public void navigateBack() {
        ui.back();
    }

    @When("Hide the keyboard")
    public void hideKeyboard() {
        ui.hideKeyboard();
    }

    @Then("Verify element {string} exists in {string}")
    public void verifyElementExists(String key, String page) {
        assertTrue(ui.isVisible(page, key), "'" + key + "' elementi '" + page + "' ekraninda gorunmedi");
    }

    @Then("Verify element {string} not exists in {string}")
    public void verifyElementNotExists(String key, String page) {
        assertFalse(ui.isVisible(page, key), "'" + key + "' elementi '" + page + "' ekraninda gorunmemeliydi");
    }

    @Then("Check if element {string} has text {string} in {string}")
    public void checkElementText(String key, String expectedText, String page) {
        assertEquals(placeholders.resolve(expectedText), ui.text(page, key),
                "'" + key + "' elementinin metni beklenenden farkli");
    }

    @Then("Check if element {string} contains text {string} in {string}")
    public void checkElementContainsText(String key, String expectedText, String page) {
        String resolved = placeholders.resolve(expectedText);
        String actual = ui.text(page, key);
        assertTrue(actual.contains(resolved),
                "'" + key + "' elementi '" + resolved + "' icermiyor, gorulen metin: '" + actual + "'");
    }

    @Then("Check element {string} is checked in {string}")
    public void checkElementIsChecked(String key, String page) {
        assertTrue(ui.isChecked(page, key), "'" + key + "' elementi secili degil");
    }

    @Then("Check element {string} is not checked in {string}")
    public void checkElementIsNotChecked(String key, String page) {
        assertFalse(ui.isChecked(page, key), "'" + key + "' elementi secili olmamaliydi");
    }

    /** Sonraki adimlarda ${ctx:<name>} olarak kullanilir. */
    @Then("Save text of element {string} as {string} in {string}")
    public void saveElementText(String key, String name, String page) {
        context.put(placeholders.resolve(name), ui.text(page, key));
    }

    @Then("Toast message {string} should be visible")
    public void toastShouldBeVisible(String message) {
        String resolved = placeholders.resolve(message);
        assertTrue(ui.isPresent(DynamicLocators.toast(resolved), TOAST_TIMEOUT_SECONDS),
                "Toast mesaji gorunmedi: " + resolved);
    }
}
