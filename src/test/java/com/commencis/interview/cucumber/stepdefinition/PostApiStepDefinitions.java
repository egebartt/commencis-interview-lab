package com.commencis.interview.cucumber.stepdefinition;

import com.commencis.interview.cucumber.ApiTestContext;
import com.commencis.interview.util.JsonReader;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Posts API senaryolarinin adimlari.
 * Istekleri PostApi atar, durum ApiTestContext'te tutulur; burada Rest Assured
 * yapilandirmasi veya test mantigi tekrar yazilmaz.
 */
public class PostApiStepDefinitions {

    private final ApiTestContext context;

    public PostApiStepDefinitions(ApiTestContext context) {
        this.context = context;
    }

    @Given("the request body is loaded from {string}")
    public void loadRequestBody(String classpathFile) {
        context.setRequestBody(JsonReader.read(classpathFile));
    }

    @When("post {int} is requested")
    public void requestPost(int id) {
        context.setResponse(context.postApi().getPost(id));
    }

    @When("the post is created")
    public void createPost() {
        context.setResponse(context.postApi().createPost(context.getRequestBody()));
    }

    @Then("the response status should be {int}")
    public void checkStatus(int expectedStatus) {
        context.getResponse().then().statusCode(expectedStatus);
    }

    @Then("the response field {string} should be {string}")
    public void checkField(String field, String expectedValue) {
        Object actual = context.getResponse().path(field);
        assertEquals(expectedValue, String.valueOf(actual), "'" + field + "' alani beklenen degeri tasimiyor");
    }

    @Then("the response field {string} should not be null")
    public void checkFieldNotNull(String field) {
        assertNotNull(context.getResponse().path(field), "'" + field + "' alani dolu olmali");
    }
}
