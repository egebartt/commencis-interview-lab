package com.commencis.interview.api;

import com.commencis.interview.util.ConfigReader;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.LogConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

import java.util.Set;

/**
 * Ortak RequestSpecification uretimi.
 *
 * <p>JUnit testleri (BaseApiTest) ve Cucumber step definition'lari ayni Rest Assured
 * yapilandirmasini kullansin, ayarlar iki yerde kopyalanmasin diye tek noktada tutulur.
 */
public final class RequestSpecFactory {

    private static final Set<String> SECRET_HEADERS = Set.of("Authorization", "Cookie", "Proxy-Authorization", "X-Api-Key");

    private RequestSpecFactory() {
    }

    /** Her cagrida temiz bir spec uretir; test/senaryo arasinda durum tasinmaz. */
    public static RequestSpecification create() {
        int timeoutMillis = ConfigReader.getInt("api.timeout.seconds", 20) * 1000;

        RestAssuredConfig config = RestAssuredConfig.newConfig()
                // Sadece assertion basarisiz olursa request/response yazilir; secret header'lar maskelenir.
                .logConfig(LogConfig.logConfig()
                        .enableLoggingOfRequestAndResponseIfValidationFails(LogDetail.ALL)
                        .blacklistHeaders(SECRET_HEADERS))
                .httpClient(HttpClientConfig.httpClientConfig()
                        .setParam("http.connection.timeout", timeoutMillis)
                        .setParam("http.socket.timeout", timeoutMillis));

        RequestSpecBuilder builder = new RequestSpecBuilder()
                .setBaseUri(ConfigReader.require("api.base.url"))
                .setConfig(config)
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON);

        if (ConfigReader.getBoolean("api.log.response", false)) {
            builder.addFilter(new ResponseLoggingFilter(LogDetail.BODY));
        }

        // Token yalnizca verilmisse eklenir; repository'de bos durur.
        String token = ConfigReader.get("api.auth.token");
        if (!token.isEmpty()) {
            builder.addHeader("Authorization", "Bearer " + token);
        }

        return builder.build();
    }
}
