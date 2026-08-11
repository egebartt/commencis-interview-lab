package com.commencis.interview.cucumber;

import com.commencis.interview.api.PostApi;
import com.commencis.interview.api.RequestSpecFactory;
import io.restassured.response.Response;

/**
 * Bir senaryo boyunca yasayan API durumu. PicoContainer her senaryo icin yeni ornek uretir,
 * bu yuzden static response alani tutulmaz.
 *
 * <p>API client'i burada kurulur ki step definition'lar Rest Assured yapilandirmasini
 * tekrar yazmasin; spec {@link RequestSpecFactory} uzerinden JUnit testleriyle ayni gelir.
 */
public class ApiTestContext {

    private final PostApi postApi = new PostApi(RequestSpecFactory.create());

    private String requestBody;
    private Response response;

    public PostApi postApi() {
        return postApi;
    }

    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody;
    }

    public String getRequestBody() {
        if (requestBody == null) {
            throw new IllegalStateException(
                    "Istek govdesi yuklenmedi. Once govdeyi okuyan Given adimini kullanin.");
        }
        return requestBody;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

    public Response getResponse() {
        if (response == null) {
            throw new IllegalStateException(
                    "Henuz bir istek gonderilmedi. Once istek atan When adimini kullanin.");
        }
        return response;
    }
}
