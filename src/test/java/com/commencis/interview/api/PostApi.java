package com.commencis.interview.api;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;

/**
 * /posts endpoint'ine istek atar.
 * Assertion yapmaz; Response dondurur, dogrulamayi test yapar.
 */
public class PostApi {

    private final RequestSpecification spec;

    public PostApi(RequestSpecification spec) {
        this.spec = spec;
    }

    public Response getPost(int id) {
        return given()
                .spec(spec)
                .get("/posts/{id}", id);
    }

    public Response createPost(String requestBody) {
        return given()
                .spec(spec)
                .body(requestBody)
                .post("/posts");
    }

    public Response updatePost(int id, String requestBody) {
        return given()
                .spec(spec)
                .body(requestBody)
                .put("/posts/{id}", id);
    }

    public Response deletePost(int id) {
        return given()
                .spec(spec)
                .delete("/posts/{id}", id);
    }
}
