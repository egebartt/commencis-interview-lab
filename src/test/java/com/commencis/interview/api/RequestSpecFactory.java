package com.commencis.interview.api;

import com.commencis.interview.util.ConfigReader;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.HeaderConfig;
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

    private static final Set<String> SECRET_HEADERS = Set.of(
            "Authorization",
            "Cookie",
            "Proxy-Authorization",
            "Api-Key",
            "X-Api-Key",
            "Client-Key",
            "X-Client-Key",
            "Secret-Key",
            "Client-Secret",
            "X-Client-Secret");

    private RequestSpecFactory() {
    }

    /** Her cagrida temiz bir spec uretir; test/senaryo arasinda durum tasinmaz. */
    public static RequestSpecification create() {
        int timeoutMillis = ConfigReader.getInt("api.timeout.seconds", 20) * 1000;

        RestAssuredConfig config = RestAssuredConfig.newConfig()
                // Global spec ile istek bazli header birlikte verilirse son deger kazanir.
                .headerConfig(HeaderConfig.headerConfig().overwriteHeadersWithName(
                        "Authorization",
                        "Api-Key",
                        "X-Api-Key",
                        "Client-Key",
                        "X-Client-Key",
                        "Secret-Key",
                        "Client-Secret",
                        "X-Client-Secret"))
                // Sadece assertion basarisiz olursa request/response yazilir; secret header'lar maskelenir.
                .logConfig(LogConfig.logConfig()
                        .enableLoggingOfRequestAndResponseIfValidationFails(LogDetail.ALL)
                        .blacklistHeaders(SECRET_HEADERS))
                .httpClient(HttpClientConfig.httpClientConfig()
                        .setParam("http.connection.timeout", timeoutMillis)
                        .setParam("http.socket.timeout", timeoutMillis));

        RequestSpecBuilder builder = new RequestSpecBuilder()
                .setConfig(config)
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON);

        // Opsiyoneldir: doluysa relative path'ler ("/posts/1") bu adrese gore cozulur, bos ise
        // testte full URL verilir. Zorunlu ayar degildir; ApiClient iki kullanimi da destekler.
        String baseUrl = ConfigReader.get("api.base.url");
        if (!baseUrl.isEmpty()) {
            builder.setBaseUri(baseUrl.replaceAll("/+$", ""));
        }

        // Gonderilen istegi yazar. builder.log(...) kullanilir, cunku RequestLoggingFilter blacklist'i
        // constructor'dan alir; bu metot onu yukaridaki LogConfig'ten okuyup verir, yani secret
        // header'lar burada da maskelenir. Filtre elle kurulursa maskeleme calismaz.
        // setConfig'ten sonra cagrilmalidir.
        if (ConfigReader.getBoolean("api.log.request", false)) {
            builder.log(LogDetail.ALL);
        }
        // Yanit yalnizca BODY olarak yazilir: ResponseLoggingFilter blacklist parametresi almadigi
        // icin LogDetail.ALL yapilirsa response header'lari maskelenmeden basilir.
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
