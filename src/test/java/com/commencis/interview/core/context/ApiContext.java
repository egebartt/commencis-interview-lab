package com.commencis.interview.core.context;

import com.commencis.interview.api.ApiClient;
import com.commencis.interview.api.RequestSpecFactory;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Bir senaryonun API durumu. Yalnizca durum tutar; URL kurma, encoding ve gonderim
 * {@link ApiClient} isidir.
 *
 * <p>Durumun omru bilerek ikiye ayrilmistir:
 * <ul>
 *   <li><b>Header'lar ve base URL</b> senaryo boyunca yasar — ayni senaryodaki ikinci istek de
 *       ayni kimlikle gider.</li>
 *   <li><b>Govde, query ve path parametreleri</b> kuruldugu istege aittir ve gonderimden sonra
 *       temizlenir; aksi halde sonraki istek istemeden eski govdeyi tasir.</li>
 * </ul>
 */
public class ApiContext {

    private final Map<String, Object> headers = new LinkedHashMap<>();
    private final Map<String, Object> queryParams = new LinkedHashMap<>();
    private final Map<String, Object> pathParams = new LinkedHashMap<>();

    private RequestSpecification spec;
    private String baseUrlOverride;
    private Object body;
    private Response response;

    /** Yalnizca bu senaryo icin base URL'i degistirir; ortam ayari etkilenmez. */
    public void baseUrl(String url) {
        this.baseUrlOverride = url;
    }

    public void putHeaders(Map<String, String> values) {
        headers.putAll(values);
    }

    public void putQueryParams(Map<String, String> values) {
        queryParams.putAll(values);
    }

    public void putPathParams(Map<String, String> values) {
        pathParams.putAll(values);
    }

    public void body(Object body) {
        this.body = body;
    }

    public Response send(Method method, String url) {
        try {
            response = client().send(method, url, body, headers, queryParams, pathParams);
            return response;
        } finally {
            body = null;
            queryParams.clear();
            pathParams.clear();
        }
    }

    public Response response() {
        if (response == null) {
            throw new IllegalStateException("Henuz bir istek gonderilmedi. Once istek atan When adimini kullanin.");
        }
        return response;
    }

    /** Spec ilk istekte kurulur; mobil senaryolar bosuna Rest Assured yapilandirmasi uretmez. */
    private ApiClient client() {
        if (spec == null) {
            spec = RequestSpecFactory.create();
        }
        return baseUrlOverride == null ? new ApiClient(spec) : new ApiClient(spec, baseUrlOverride);
    }
}
