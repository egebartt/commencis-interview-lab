package com.commencis.interview.stepdefinitions.api;

import com.commencis.interview.api.ApiClient;
import com.commencis.interview.core.TestContext;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.http.Method;
import io.restassured.response.Response;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class ApiStepDefinitions {

    private final ApiClient api;
    private final TestContext context;

    public ApiStepDefinitions(ApiClient api, TestContext context) {
        this.api = api;
        this.context = context;
    }

    // ------------------------------------------------------------------
    // Istek
    // ------------------------------------------------------------------

    /** Yalnizca bu senaryo icin adresi degistirir; config dosyasi etkilenmez. */
    @Given("the base url is {string}")
    public void setBaseUrl(String baseUrl) {
        api.baseUrl(context.resolve(baseUrl));
    }

    @Given("the request headers:")
    public void setHeaders(DataTable table) {
        api.headers(context.resolve(table.asMap(String.class, String.class)));
    }

    @Given("the query params:")
    public void setQueryParams(DataTable table) {
        api.queryParams(context.resolve(table.asMap(String.class, String.class)));
    }

    /** Govde dogrudan senaryoya yazilir (DocString). */
    @Given("the request body:")
    public void setBody(String body) {
        api.body(context.resolve(body));
    }

    /** Govde classpath altindaki bir dosyadan gelir: {@code testdata/create-post.json}. */
    @Given("the request body from file {string}")
    public void setBodyFromFile(String classpathFile) {
        api.body(context.resolve(readClasspathFile(context.resolve(classpathFile))));
    }

    @When("I send {word} to {string}")
    public void sendRequest(String method, String url) {
        api.send(methodOf(method), context.resolve(url));
    }

    // ------------------------------------------------------------------
    // Yanit
    // ------------------------------------------------------------------

    /** Hata mesajina yanit govdesi konmaz: hata mesaji maskelenmeden konsola ve failsafe raporuna duser.
     * Govdenin maskelenmis hali Allure'daki "API response" ekinde durur. */
    @Then("the response status should be {int}")
    public void checkStatus(int expectedStatus) {
        assertEquals(expectedStatus, api.response().statusCode(),
                "HTTP status beklenenden farkli. Yanit icin Allure 'API response' ekine bakin.");
    }

    @Then("the response time should be under {int} ms")
    public void checkResponseTime(int maximumMillis) {
        long actual = api.response().time();
        assertTrue(actual < maximumMillis, "Yanit " + actual + " ms surdu, sinir " + maximumMillis + " ms");
    }

    @Then("the response field {string} should be {string}")
    public void checkField(String field, String expectedValue) {
        assertEquals(context.resolve(expectedValue), fieldValue(context.resolve(field)),
                "'" + field + "' alani beklenen degeri tasimiyor");
    }

    @Then("the response field {string} should not be null")
    public void checkFieldNotNull(String field) {
        assertNotNull(api.response().path(context.resolve(field)), "'" + field + "' alani dolu olmali");
    }

    @Then("the response fields should be:")
    public void checkFields(DataTable table) {
        Map<String, String> expected = context.resolve(table.asMap(String.class, String.class));
        expected.forEach((field, expectedValue) ->
                assertEquals(expectedValue, fieldValue(field), "'" + field + "' alani beklenen degeri tasimiyor"));
    }

    /** Zincirleme icin: sonraki adimlarda ${ctx:<ad>} olarak kullanilir. */
    @Then("I save response field {string} as {string}")
    public void saveResponseField(String field, String name) {
        String resolvedField = context.resolve(field);
        Object value = api.response().path(resolvedField);
        assertNotNull(value, "'" + resolvedField + "' alani bos oldugu icin saklanamaz");
        context.put(context.resolve(name), value);
    }

    // ------------------------------------------------------------------
    // Yardimcilar
    // ------------------------------------------------------------------

    /** Rest Assured'in {@code <T> T path(...)} imzasi String.valueOf(...) icine dogrudan verilirse
     * T, char[] olarak cikarilir ve calisma aninda ClassCastException uretir. Once Object'e atanir. */
    private String fieldValue(String field) {
        Response response = api.response();
        Object value = response.path(field);
        return String.valueOf(value);
    }

    private static Method methodOf(String method) {
        try {
            return Method.valueOf(method.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Desteklenmeyen HTTP metodu: '" + method
                    + "'. Kullanilabilir metotlar: GET, POST, PUT, PATCH, DELETE, HEAD, OPTIONS", e);
        }
    }

    private static String readClasspathFile(String classpathFile) {
        try (InputStream stream = ApiStepDefinitions.class.getClassLoader().getResourceAsStream(classpathFile)) {
            if (stream == null) {
                throw new IllegalArgumentException("Dosya bulunamadi: src/test/resources/" + classpathFile);
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Dosya okunamadi: " + classpathFile, e);
        }
    }
}
