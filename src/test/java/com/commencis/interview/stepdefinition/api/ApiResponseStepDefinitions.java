package com.commencis.interview.stepdefinition.api;

import com.commencis.interview.core.context.ApiContext;
import com.commencis.interview.core.context.ScenarioContext;
import com.commencis.interview.core.data.CsvOutput;
import com.commencis.interview.core.data.Placeholders;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.restassured.response.Response;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Yanit dogrulamalari ve ciktinin saklanmasi. */
public class ApiResponseStepDefinitions {

    private final ApiContext api;
    private final ScenarioContext context;
    private final Placeholders placeholders;

    public ApiResponseStepDefinitions(ApiContext api, ScenarioContext context, Placeholders placeholders) {
        this.api = api;
        this.context = context;
        this.placeholders = placeholders;
    }

    /**
     * Hata mesajina yanit govdesi konmaz: govde maskelenmemis haliyle failsafe raporuna ve
     * konsola duserdi. Beklenen/gelen status'u JUnit zaten yaziyor, govdenin maskelenmis hali
     * Allure'daki "API response" ekinde duruyor.
     */
    @Then("the response status should be {int}")
    public void checkStatus(int expectedStatus) {
        Response response = api.response();
        assertEquals(expectedStatus, response.statusCode(),
                "HTTP status beklenenden farkli. Maskelenmis yanit icin Allure 'API response' ekine bakin.");
    }

    @Then("the response time should be under {int} ms")
    public void checkResponseTime(int maximumMillis) {
        long actual = api.response().time();
        assertTrue(actual < maximumMillis, "Yanit " + actual + " ms surdu, sinir " + maximumMillis + " ms");
    }

    @Then("the response field {string} should be {string}")
    public void checkField(String field, String expectedValue) {
        assertEquals(placeholders.resolve(expectedValue), actualField(placeholders.resolve(field)),
                "'" + field + "' alani beklenen degeri tasimiyor");
    }

    @Then("the response field {string} should not be null")
    public void checkFieldNotNull(String field) {
        assertNotNull(api.response().path(placeholders.resolve(field)), "'" + field + "' alani dolu olmali");
    }

    @Then("the response fields should be:")
    public void checkFields(DataTable table) {
        Map<String, String> expected = placeholders.resolve(table.asMap(String.class, String.class));
        expected.forEach((field, expectedValue) ->
                assertEquals(expectedValue, actualField(field),
                        "'" + field + "' alani beklenen degeri tasimiyor"));
    }

    /**
     * Rest Assured'in {@code <T> T path(...)} imzasi String.valueOf(...) icine dogrudan
     * verilirse T, char[] olarak cikarilir ve calisma aninda ClassCastException uretir.
     * Once Object'e atanir.
     */
    private String actualField(String field) {
        Object value = api.response().path(field);
        return String.valueOf(value);
    }

    /** Zincirleme icin: sonraki adimlarda ${ctx:<name>} olarak kullanilir. */
    @Then("I save response field {string} as {string}")
    public void saveResponseField(String field, String name) {
        String resolvedField = placeholders.resolve(field);
        Object value = api.response().path(resolvedField);
        assertNotNull(value, "'" + resolvedField + "' alani bos oldugu icin saklanamaz");
        context.put(placeholders.resolve(name), value);
    }

    /** Cikti target/output altina yazilir; kaynak dizinine hicbir zaman dokunulmaz. */
    @Then("I save the response to csv {string} with fields {string}")
    public void saveResponseToCsv(String fileName, String fields) {
        List<String> fieldNames = Arrays.stream(placeholders.resolve(fields).split(","))
                .map(String::trim)
                .filter(field -> !field.isEmpty())
                .toList();
        Path written = CsvOutput.append(placeholders.resolve(fileName), api.response(), fieldNames);
        context.put("lastCsvOutput", written.toString());
    }
}
