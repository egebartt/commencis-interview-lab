package com.commencis.interview.stepdefinition.api;

import com.commencis.interview.core.context.ApiContext;
import com.commencis.interview.core.data.CsvData;
import com.commencis.interview.core.data.JsonData;
import com.commencis.interview.core.data.Placeholders;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.restassured.http.Method;

import java.util.Locale;
import java.util.Map;

/**
 * Istegin kurulmasi ve gonderilmesi. Endpoint'e ozel sinif gerekmez; adres, govde ve
 * parametreler senaryodan gelir.
 *
 * <p>Tum metin parametreleri {@link Placeholders} uzerinden gecer: {@code ${ctx:postId}} ve
 * {@code ${config:api.base.url}} her adimda kullanilabilir.
 */
public class ApiRequestStepDefinitions {

    private final ApiContext api;
    private final Placeholders placeholders;

    public ApiRequestStepDefinitions(ApiContext api, Placeholders placeholders) {
        this.api = api;
        this.placeholders = placeholders;
    }

    /** Yalnizca bu senaryo icin adresi degistirir; ortam ayari etkilenmez. */
    @Given("the base url is {string}")
    public void setBaseUrl(String baseUrl) {
        api.baseUrl(placeholders.resolve(baseUrl));
    }

    @Given("the request headers:")
    public void setHeaders(DataTable table) {
        api.putHeaders(placeholders.resolve(table.asMap(String.class, String.class)));
    }

    @Given("the query params:")
    public void setQueryParams(DataTable table) {
        api.putQueryParams(placeholders.resolve(table.asMap(String.class, String.class)));
    }

    @Given("the path params:")
    public void setPathParams(DataTable table) {
        api.putPathParams(placeholders.resolve(table.asMap(String.class, String.class)));
    }

    /** Govde dogrudan senaryoya yazilir (DocString). */
    @Given("the request body:")
    public void setBodyFromDocString(String body) {
        api.body(placeholders.resolve(body));
    }

    @Given("the request body from json {string}")
    public void setBodyFromJsonFile(String classpathFile) {
        api.body(placeholders.resolve(JsonData.read(placeholders.resolve(classpathFile))));
    }

    @Given("the request body from csv {string} row {int}")
    public void setBodyFromCsvRow(String classpathFile, int rowNumber) {
        api.body(CsvData.rowAsJson(placeholders.resolve(classpathFile), rowNumber));
    }

    @Given("the request body from csv {string} where {string} is {string}")
    public void setBodyFromCsvMatch(String classpathFile, String column, String value) {
        api.body(CsvData.rowAsJson(placeholders.resolve(classpathFile),
                placeholders.resolve(column), placeholders.resolve(value)));
    }

    /** Duz alanlar icin tablo; ic ice yapi gerekiyorsa JSON dosyasi veya DocString kullanilir. */
    @Given("the request body from table:")
    public void setBodyFromTable(DataTable table) {
        Map<String, String> row = placeholders.resolve(table.asMap(String.class, String.class));
        api.body(CsvData.toJson(row));
    }

    @When("I send {word} to {string}")
    public void sendRequest(String method, String url) {
        api.send(methodOf(method), placeholders.resolve(url));
    }

    private static Method methodOf(String method) {
        try {
            return Method.valueOf(method.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Desteklenmeyen HTTP metodu: '" + method
                    + "'. Kullanilabilir metotlar: GET, POST, PUT, PATCH, DELETE, HEAD, OPTIONS", e);
        }
    }
}
