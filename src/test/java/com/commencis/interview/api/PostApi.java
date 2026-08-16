package com.commencis.interview.api;

import com.commencis.interview.util.ConfigReader;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class PostApi extends ApiClient {

    public PostApi(RequestSpecification spec) {
        super(spec);
    }

    public Response getPost(int id) {
        return get(defaultUrl("/posts/" + id));
    }

    public Response createPost(String requestBody) {
        return post(defaultUrl("/posts"), requestBody);
    }

    public Response updatePost(int id, String requestBody) {
        return put(defaultUrl("/posts/" + id), requestBody);
    }

    public Response deletePost(int id) {
        return delete(defaultUrl("/posts/" + id));
    }

    private String defaultUrl(String path) {
        return ConfigReader.require("api.base.url").replaceAll("/+$", "") + path;
    }
}
