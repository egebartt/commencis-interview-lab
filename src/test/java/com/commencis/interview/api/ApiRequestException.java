package com.commencis.interview.api;

import io.restassured.http.Method;

import javax.net.ssl.SSLException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.PortUnreachableException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

public class ApiRequestException extends RuntimeException {

    /**
     * DNS_ERROR ->  Host adi cozulemedi
     * CONNECTION_ERROR -> TCP baglantisi kurulamadi (connection refused, route yok)
     * TIMEOUT_ERROR -> Baglanti kurulurken veya yanit beklenirken sure asildi
     * TLS_ERROR -> TLS el sikismasi basarisiz (sertifika, protokol uyusmazligi
     * REQUEST_ERROR -> Istek hic gonderilemedi veya hata siniflandirilamadi
     * */
    public enum Category {
        DNS_ERROR,
        CONNECTION_ERROR,
        TIMEOUT_ERROR,
        TLS_ERROR,
        REQUEST_ERROR
    }

    private final Category category;

    private ApiRequestException(Category category, String message, Throwable cause) {
        super(message, cause);
        this.category = category;
    }

    public Category category() {
        return category;
    }

    public static ApiRequestException from(Method method, String url, Throwable cause) {
        Throwable known = firstKnownCause(cause);
        Category category = known == null ? Category.REQUEST_ERROR : categoryOf(known);
        Throwable reported = known == null ? rootCause(cause) : known;
        return new ApiRequestException(category, message(category, method, url, describe(reported)), cause);
    }

    public static ApiRequestException configuration(Method method, String url, String detail) {
        return new ApiRequestException(Category.REQUEST_ERROR, message(Category.REQUEST_ERROR, method, url, detail), null);
    }

    private static String message(Category category, Method method, String url, String detail) {
        return category + " - " + method + " " + url + " failed: " + detail;
    }

    private static Throwable firstKnownCause(Throwable cause) {
        for (Throwable current : chain(cause)) {
            if (categoryOf(current) != null) {
                return current;
            }
        }
        return null;
    }

    private static Throwable rootCause(Throwable cause) {
        Throwable last = cause;
        for (Throwable current : chain(cause)) {
            last = current;
        }
        return last;
    }

    private static List<Throwable> chain(Throwable cause) {
        Set<Throwable> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        List<Throwable> chain = new ArrayList<>();
        for (Throwable current = cause; current != null && seen.add(current); current = current.getCause()) {
            chain.add(current);
        }
        return chain;
    }

    private static Category categoryOf(Throwable throwable) {
        if (throwable instanceof UnknownHostException) {
            return Category.DNS_ERROR;
        }
        if (throwable instanceof SocketTimeoutException) {
            return Category.TIMEOUT_ERROR;
        }
        if (throwable instanceof ConnectException
                || throwable instanceof NoRouteToHostException
                || throwable instanceof PortUnreachableException) {
            return Category.CONNECTION_ERROR;
        }
        if (throwable instanceof SSLException) {
            return Category.TLS_ERROR;
        }
        String type = throwable.getClass().getName();
        if (type.endsWith("ConnectTimeoutException") || type.endsWith("ConnectionPoolTimeoutException")) {
            return Category.TIMEOUT_ERROR;
        }
        return null;
    }

    private static String describe(Throwable throwable) {
        if (throwable == null) {
            return "Unknown error";
        }
        String message = throwable.getMessage();
        return message == null || message.isBlank()
                ? throwable.getClass().getSimpleName()
                : throwable.getClass().getSimpleName() + ": " + message;
    }
}
