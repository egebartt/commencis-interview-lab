package com.commencis.interview.stepdefinition.mobile;

import com.commencis.interview.context.MobileTestContext;
import com.commencis.interview.page.ApiDemosPage;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ApiDemos senaryolarinin adimlari; JUnit testleriyle ayni Page aksiyonlarini cagirir.
 * Burada locator, bekleme veya driver kullanimi bulunmaz; yalnizca delegasyon ve
 * ince sonuc assertion'lari yer alir.
 */
public class ApiDemosStepDefinitions {

    private final MobileTestContext context;

    /** Driver hook'ta acildigi icin Page constructor'da degil, ilk kullanimda olusturulur. */
    private ApiDemosPage apiDemosPage;

    public ApiDemosStepDefinitions(MobileTestContext context) {
        this.context = context;
    }

    @Given("the Api Demos home screen is visible")
    public void homeScreenIsVisible() {
        assertTrue(page().isHomePageVisible(), "Ana menu gorunmedi");
    }

    @When("the user opens the Views menu")
    public void openViewsMenu() {
        page().openViews();
    }

    @Then("the Buttons option should be visible")
    public void buttonsOptionIsVisible() {
        assertTrue(page().isButtonsOptionVisible(), "Views ekraninda Buttons gorunmedi");
    }

    private ApiDemosPage page() {
        if (apiDemosPage == null) {
            apiDemosPage = new ApiDemosPage(context.getDriver());
        }
        return apiDemosPage;
    }
}
