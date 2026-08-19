package com.commencis.api;

import com.commencis.core.Config;
import com.commencis.core.Redaction;
import io.qameta.allure.Allure;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import io.restassured.specification.RequestSpecification;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static io.restassured.RestAssured.given;

/**
 * Bir senaryonun (veya bir JUnit testinin) HTTP oturumu: istek kurulur, gonderilir, son yanit
 * saklanir. Assertion yapmaz; dogrulama Step Definition'larin isidir.
 *
 * <p>Durumun omru bilerek ikiye ayrilmistir:
 * <ul>
 *   <li><b>Header'lar ve base url</b> senaryo boyunca yasar; ayni senaryodaki ikinci istek de
 *       ayni kimlikle gider.</li>
 *   <li><b>Govde ve query parametreleri</b> kuruldugu istege aittir, gonderimden sonra
 *       temizlenir; aksi halde sonraki istek istemeden eski govdeyi tasirdi.</li>
 * </ul>
 *
 * <p>Rapor kaniti tek noktada uretilir: {@code ReportFilter} her istegi ve yaniti Allure'a ekler,
 * gizli header/query/govde alanlarini {@link Redaction} ile maskeleyerek. Adim tarafinda ayrica
 * ek cagrisi gerekmez.
 */
public class ApiClient {

    /** http:// gibi bir sema ile basliyorsa adres full URL kabul edilir. */
    private static final Pattern ABSOLUTE_URL = Pattern.compile("^[a-zA-Z][a-zA-Z0-9+.\\-]*://");

    private final Map<String, Object> headers = new LinkedHashMap<>();
    private final Map<String, Object> queryParams = new LinkedHashMap<>();
    private final Map<String, Object> formParams = new LinkedHashMap<>();
    private final Map<String, Object> pathParams = new LinkedHashMap<>();

    private String baseUrl = Config.get("api.base.url");
    private Object body;
    private Response response;
    private RequestSpecification spec;

    // ------------------------------------------------------------------
    // Istek kurulumu
    // ------------------------------------------------------------------

    /** Yalnizca bu oturum icin adresi degistirir; config dosyasi etkilenmez. */
    public void baseUrl(String url) {
        this.baseUrl = url == null ? "" : url.trim();
    }

    public void headers(Map<String, String> values) {
        headers.putAll(values);
    }

    /** Tek header eklemek icin kisa yol; senaryo boyunca sonraki isteklerde de kalir. */
    public void header(String name, String value) {
        headers.put(name, value);
    }

    /** URL'in sonuna ?key=value olarak eklenir. Filtreleme, pagination, search ve sorting gibi islemlerde kullanilir; request body degildir. */
    public void bearerToken(String token) {
        headers.put("Authorization", "Bearer " + token);
    }

    public void queryParams(Map<String, String> values) {
        queryParams.putAll(values);
    }

    /** Degerleri request body'de application/x-www-form-urlencoded formatinda gonderir.
     * OAuth/token veya form endpointlerinde kullanilir; JSON body isteyen endpointlerde kullanilmaz.
     * grant_type=password, scope=openid, username, password, client_id */
    public void formParams(Map<String, String> values) {
        formParams.putAll(values);
    }

    /**
     * URL sablonundaki {ad} yer tutucularini doldurur: adres string birlestirmeyle kurulmaz,
     * encoding'i Rest Assured yapar. Gonderimden sonra temizlenir.
     *
     * <pre>
     * api.pathParams(Map.of("id", id));
     * api.get("/users/{id}");
     * </pre>
     */
    public void pathParams(Map<String, ?> values) {
        pathParams.putAll(values);
    }

    public void body(Object body) {
        this.body = body;
    }

    // ------------------------------------------------------------------
    // Gonderim
    // ------------------------------------------------------------------

    public Response get(String url) {
        return send(Method.GET, url);
    }

    /** Govdesiz POST; form parametreleriyle birlikte kullanilir. */
    public Response post(String url) {
        return send(Method.POST, url);
    }

    public Response post(String url, Object requestBody) {
        body(requestBody);
        return send(Method.POST, url);
    }

    public Response put(String url, Object requestBody) {
        body(requestBody);
        return send(Method.PUT, url);
    }

    public Response patch(String url, Object requestBody) {
        body(requestBody);
        return send(Method.PATCH, url);
    }

    public Response delete(String url) {
        return send(Method.DELETE, url);
    }

    /**
     * Kurulan istegi gonderir.
     *
     * <p>Query parametreleri URL'e elle eklenmez; encoding Rest Assured'a birakilir.
     */
    public Response send(Method method, String url) {
        RequestSpecification request = given().spec(spec());
        if (!headers.isEmpty()) {
            request.headers(headers);
        }
        if (!queryParams.isEmpty()) {
            request.queryParams(queryParams);
        }
        if (!pathParams.isEmpty()) {
            request.pathParams(pathParams);
        }
        if (!formParams.isEmpty()) {
            request.contentType(ContentType.URLENC).formParams(formParams);
        }
        if (body != null) {
            request.body(body);
        }
        try {
            response = request.request(method, resolveUrl(url));
            return response;
        } finally {
            body = null;
            queryParams.clear();
            formParams.clear();
            pathParams.clear();
        }
    }

    /** Son yanit; dogrulama adimlari bunu okur. */
    public Response response() {
        if (response == null) {
            throw new IllegalStateException("Henuz istek gonderilmedi. Once istek atan When adimini kullanin.");
        }
        return response;
    }

    // ------------------------------------------------------------------
    // Yapilandirma
    // ------------------------------------------------------------------

    /** Spec ilk istekte kurulur; mobil senaryolar bosuna Rest Assured yapilandirmasi uretmez. */
    private RequestSpecification spec() {
        if (spec == null) {
            int timeoutMillis = Config.getInt("api.timeout", 20) * 1000;

            RequestSpecBuilder builder = new RequestSpecBuilder()
                    .setConfig(RestAssuredConfig.newConfig()
                            .httpClient(HttpClientConfig.httpClientConfig()
                                    .setParam("http.connection.timeout", timeoutMillis)
                                    .setParam("http.socket.timeout", timeoutMillis)))
                    .setContentType(ContentType.JSON)
                    .setAccept(ContentType.JSON)
                    .addFilter(new ReportFilter());

            // Token repository'ye yazilmaz: -Dapi.token=... veya API_TOKEN ile gelir.
            String token = Config.get("api.token");
            if (!token.isEmpty()) {
                builder.addHeader("Authorization", "Bearer " + token);
            }

            spec = builder.build();
        }
        return spec;
    }

    private String resolveUrl(String url) {
        String path = url == null ? "" : url.trim();
        if (path.isEmpty()) {
            throw new IllegalArgumentException("URL verilmedi.");
        }
        if (ABSOLUTE_URL.matcher(path).find()) {
            return path;
        }
        if (baseUrl.isEmpty()) {
            throw new IllegalStateException("Relative path verildi ama api.base.url tanimli degil: " + path
                    + ". Ya testte full URL kullanin ya da api.base.url ayarlayin.");
        }
        return baseUrl.replaceAll("/+$", "") + (path.startsWith("/") ? path : "/" + path);
    }

    // ------------------------------------------------------------------
    // Rapor
    // ------------------------------------------------------------------

    /**
     * Istek ve yaniti Allure'a ekler. Maskeleme {@link Redaction} uzerinden yapilir ve yalnizca
     * rapor kopyasini etkiler; gonderilen istek ile alinan {@link Response} degistirilmez.
     *
     * <p>URI filtre zincirinde okundugu icin base url cozulmus, path parametreleri yerlesmis ve
     * query parametreleri encode edilmis haliyle gorunur.
     */
    private static final class ReportFilter implements Filter {

        private static final int MAX_BODY_LENGTH = 20_000;

        @Override
        public Response filter(FilterableRequestSpecification request,
                               FilterableResponseSpecification responseSpec,
                               FilterContext context) {
            Allure.addAttachment("API request", "text/plain", requestText(request));

            Response response = context.next(request, responseSpec);

            Allure.addAttachment("API response - HTTP " + response.statusCode()
                            + " (" + response.time() + " ms)",
                    "application/json", trim(Redaction.maskJson(response.asString())));
            return response;
        }

        private static String requestText(FilterableRequestSpecification request) {
            StringBuilder text = new StringBuilder()
                    .append(request.getMethod())
                    .append(' ')
                    .append(Redaction.maskUrl(request.getURI()))
                    .append(System.lineSeparator());
            for (Header header : request.getHeaders()) {
                text.append(header.getName())
                        .append(": ")
                        .append(Redaction.maskHeader(header.getName(), header.getValue()))
                        .append(System.lineSeparator());
            }
            Object body = request.getBody();
            if (body != null) {
                text.append(System.lineSeparator()).append(trim(Redaction.maskBody(body)));
            }
            return text.toString();
        }

        /** Cok buyuk govde raporu sisirmesin. */
        private static String trim(String body) {
            if (body == null) {
                return "";
            }
            return body.length() <= MAX_BODY_LENGTH
                    ? body
                    : body.substring(0, MAX_BODY_LENGTH) + System.lineSeparator() + "... (kisaltildi)";
        }
    }
}
