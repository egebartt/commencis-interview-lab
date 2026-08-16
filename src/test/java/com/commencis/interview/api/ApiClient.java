package com.commencis.interview.api;

import com.commencis.interview.util.ConfigReader;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.Map;
import java.util.regex.Pattern;

import static io.restassured.RestAssured.given;


public class ApiClient {

    /** http://, https:// gibi bir sema ile basliyorsa URL full kabul edilir. */
    private static final Pattern ABSOLUTE_URL = Pattern.compile("^[a-zA-Z][a-zA-Z0-9+.\\-]*://");

    private final RequestSpecification spec;
    private final String baseUrl;

    /** Base URL {@code api.base.url} ayarindan okunur; tanimli degilse full URL kullanilir. */
    public ApiClient(RequestSpecification spec) {
        this(spec, ConfigReader.get("api.base.url"));
    }

    /** Base URL'i testten vermek icin; bos verilirse yalnizca full URL kullanilabilir. */
    public ApiClient(RequestSpecification spec, String baseUrl) {
        this.spec = spec;
        this.baseUrl = baseUrl == null ? "" : baseUrl.trim().replaceAll("/+$", "");
    }

    public Response get(String url) {
        return send(Method.GET, url, null, Map.of());
    }

    public Response get(String url, Map<String, ?> headers) {
        return send(Method.GET, url, null, headers);
    }

    public Response post(String url, Object body) {
        return send(Method.POST, url, body, Map.of());
    }

    public Response post(String url, Object body, Map<String, ?> headers) {
        return send(Method.POST, url, body, headers);
    }

    public Response put(String url, Object body) {
        return send(Method.PUT, url, body, Map.of());
    }

    public Response put(String url, Object body, Map<String, ?> headers) {
        return send(Method.PUT, url, body, headers);
    }

    public Response patch(String url, Object body) {
        return send(Method.PATCH, url, body, Map.of());
    }

    public Response patch(String url, Object body, Map<String, ?> headers) {
        return send(Method.PATCH, url, body, headers);
    }

    public Response delete(String url) {
        return send(Method.DELETE, url, null, Map.of());
    }

    public Response delete(String url, Map<String, ?> headers) {
        return send(Method.DELETE, url, null, headers);
    }

    public Response send(Method method, String url, Object body, Map<String, ?> headers) {
        String targetUrl = resolveUrl(method, url);
        RequestSpecification request = given().spec(spec);
        if (headers != null && !headers.isEmpty()) {
            request.headers(headers);
        }
        if (body != null) {
            request.body(body);
        }
        try {
            return request.request(method, targetUrl);
        } catch (Exception e) {
            // HTTP yaniti alinan istekler buraya dusmez; yalnizca gecerli bir Response uretemeden basarisiz olan istekler siniflandirilir.
            throw ApiRequestException.from(method, targetUrl, e);
        }
    }

    private String resolveUrl(Method method, String url) {
        String path = url == null ? "" : url.trim();
        if (path.isEmpty()) {
            throw ApiRequestException.configuration(method, "<bos>", "URL verilmedi.");
        }
        if (ABSOLUTE_URL.matcher(path).find()) {
            return path;
        }
        if (baseUrl.isEmpty()) {
            throw ApiRequestException.configuration(method, path,
                    "Relative path verildi ama api.base.url tanimli degil. Ya testte full URL kullanin "
                            + "ya da api.base.url ayarlayin (config.properties veya -Dapi.base.url=<adres>).");
        }
        return baseUrl + (path.startsWith("/") ? path : "/" + path);
    }
}
