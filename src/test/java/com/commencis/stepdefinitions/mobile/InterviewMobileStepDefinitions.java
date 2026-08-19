package com.commencis.stepdefinitions.mobile;

import com.commencis.mobile.pages.InterviewPage;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** Interview mobile feature'ini Page Object'e baglar; driver ve locator kullanmaz. */
public class InterviewMobileStepDefinitions {

    private final InterviewPage interviewPage;

    public InterviewMobileStepDefinitions(InterviewPage interviewPage) {
        this.interviewPage = interviewPage;
    }

    @Given("the Interview home screen is visible")
    public void homeScreenIsVisible() {
        assertTrue(interviewPage.isHomeVisible(), "Interview ana ekrani gorunmedi");
    }

    @When("the user opens Views on the Interview page")
    public void openViews() {
        interviewPage.openViews();
    }

    @Then("the Interview Buttons option should be visible")
    public void buttonsOptionIsVisible() {
        assertTrue(interviewPage.isButtonsVisible(), "Interview Views ekraninda Buttons gorunmedi");
    }
}
