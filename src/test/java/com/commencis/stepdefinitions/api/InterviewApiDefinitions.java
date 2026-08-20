package com.commencis.stepdefinitions.api;

import com.commencis.api.ApiClient;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class InterviewApiDefinitions {

    private final ApiClient api;

    private Response response;

    private String random;

    private String firstName;
    private String lastName;
    private String email;
    private String password;

    private String updatedFirstName;
    private String updatedLastName;

    public InterviewApiDefinitions(ApiClient api) {
        this.api = api;
    }


    @Given("a random Contact List user is prepared")
    public void aRandomContactListUserIsPrepared() {
        api.baseUrl("https://thinking-tester-contact-list.herokuapp.com");
        random = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 8);

        firstName = "Test" + random;
        lastName = "User" + random;
        email = "commencis." + random + "@example.com";
        password = "commencis123";
    }


    @When("the user is created")
    public void theUserIsCreated() {
        Map<String, String> createBody = Map.of(
                "firstName", firstName,
                "lastName", lastName,
                "email", email,
                "password", password
        );

        response = api.post("/users", createBody);
        response.prettyPrint();
    }


    @Then("the user should be created successfully")
    public void theUserShouldBeCreatedSuccessfully() {
        assertAll(
                () -> assertEquals(201, response.statusCode()),
                () -> assertEquals(firstName, response.path("user.firstName")),
                () -> assertEquals(lastName, response.path("user.lastName")),
                () -> assertEquals(email, response.path("user.email")),
                () -> assertNotNull(response.path("user._id")),
                () -> assertNotNull(response.path("token"))
        );

        String token = response.path("token");
        api.bearerToken(token);
    }


    @When("the current user is requested")
    public void theCurrentUserIsRequested() {
        response = api.get("/users/me");
        response.prettyPrint();
    }


    @Then("the created user information should be returned")
    public void theCreatedUserInformationShouldBeReturned() {
        assertAll(
                () -> assertEquals(200, response.statusCode()),
                () -> assertEquals(firstName, response.path("firstName")),
                () -> assertEquals(lastName, response.path("lastName")),
                () -> assertEquals(email, response.path("email"))
        );
    }


    @When("the user's first name and last name are changed to random values")
    public void theUsersFirstNameAndLastNameAreChangedToRandomValues() {
        updatedFirstName = "Updated" + random;
        updatedLastName = "Commencis" + random;

        Map<String, String> updateBody = Map.of(
                "firstName", updatedFirstName,
                "lastName", updatedLastName
        );

        response = api.patch("/users/me", updateBody);
        response.prettyPrint();
    }


    @Then("the user should be updated successfully")
    public void theUserShouldBeUpdatedSuccessfully() {
        assertAll(
                () -> assertEquals(200, response.statusCode()),
                () -> assertEquals(
                        updatedFirstName,
                        response.path("firstName")
                ),
                () -> assertEquals(
                        updatedLastName,
                        response.path("lastName")
                )
        );
    }


    @Then("the updated user information should be returned")
    public void theUpdatedUserInformationShouldBeReturned() {
        assertAll(
                () -> assertEquals(200, response.statusCode()),
                () -> assertEquals(
                        updatedFirstName,
                        response.path("firstName")
                ),
                () -> assertEquals(
                        updatedLastName,
                        response.path("lastName")
                ),
                () -> assertEquals(
                        email,
                        response.path("email")
                )
        );
    }

    @When("the current user is deleted")
    public void theCurrentUserIsDeleted() {
        response = api.delete("/users/me");
        response.prettyPrint();
    }

    @Then("the user should be deleted successfully")
    public void theUserShouldBeDeletedSuccessfully() {
        assertEquals(200, response.statusCode());
    }

    @Then("the deleted user should no longer be accessible")
    public void deletedUserShouldNoLongerBeAccessible() {
        response = api.get("/users/me");
        response.prettyPrint();
        assertEquals(401, response.statusCode(), "Deleted user should not be accessible");
    }

}