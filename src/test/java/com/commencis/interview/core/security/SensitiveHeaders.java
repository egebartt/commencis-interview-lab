package com.commencis.interview.core.security;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Gizli deger tasiyan isimlerin tek kaynagi: header, query parametresi ve JSON alani ayni listeye
 * bakar, boylece bir deger bir yerde maskelenip digerinde acik yazilmaz.
 *
 * <p>Karsilastirma normalize edilir: harf/rakam disi karakterler atilir ve kucuk harfe cevrilir.
 * Boylece {@code Authorization}, {@code authorization}, {@code access_token}, {@code accessToken}
 * ve {@code access-token} ayni sekilde taninir.
 */
public final class SensitiveHeaders {

    public static final String MASK = "***";

    /** Rest Assured'in blacklistHeaders API'sine verilen kanonik header isimleri. */
    private static final Set<String> CANONICAL_HEADER_NAMES = Set.of(
            "Authorization",
            "Cookie",
            "Set-Cookie",
            "Proxy-Authorization",
            "Api-Key",
            "X-Api-Key",
            "Client-Key",
            "X-Client-Key",
            "Secret-Key",
            "Client-Secret",
            "X-Client-Secret");

    /** Header isimlerine ek olarak query parametrelerinde ve JSON govdesinde aranan adlar. */
    private static final Set<String> EXTRA_SECRET_NAMES = Set.of(
            "token",
            "access_token",
            "refresh_token",
            "id_token",
            "api_key",
            "apikey",
            "client_secret",
            "password",
            "passwd",
            "secret",
            "signature");

    private static final Set<String> NORMALIZED_SECRET_NAMES =
            java.util.stream.Stream.concat(CANONICAL_HEADER_NAMES.stream(), EXTRA_SECRET_NAMES.stream())
                    .map(SensitiveHeaders::normalize)
                    .collect(Collectors.toUnmodifiableSet());

    private SensitiveHeaders() {
    }

    public static Set<String> canonicalNames() {
        return CANONICAL_HEADER_NAMES;
    }

    /** Header adi, query parametresi adi veya JSON alan adi icin kullanilir. */
    public static boolean isSecret(String name) {
        return name != null && NORMALIZED_SECRET_NAMES.contains(normalize(name));
    }

    /** Gizli header degerlerini maskelenmis bir kopya dondurur; girdi degistirilmez. */
    public static Map<String, String> redact(Map<String, String> headers) {
        Map<String, String> redacted = new LinkedHashMap<>();
        if (headers != null) {
            headers.forEach((name, value) -> redacted.put(name, isSecret(name) ? MASK : value));
        }
        return redacted;
    }

    /**
     * Adresi rapora yazilabilir hale getirir: kullanici bilgisi ({@code https://user:pass@host})
     * ve gizli query parametrelerinin degerleri maskelenir.
     *
     * <p>Adres okunabilir kalsin diye yalnizca degerler degisir; parametre sirasi, adlari ve
     * encoding'i korunur.
     */
    public static String redactUrl(String url) {
        if (url == null || url.isBlank()) {
            return url;
        }
        return redactQuery(redactUserInfo(url));
    }

    private static String redactUserInfo(String url) {
        int schemeEnd = url.indexOf("://");
        if (schemeEnd < 0) {
            return url;
        }
        int authorityStart = schemeEnd + 3;
        int authorityEnd = url.indexOf('/', authorityStart);
        String authority = authorityEnd < 0 ? url.substring(authorityStart) : url.substring(authorityStart, authorityEnd);
        int userInfoEnd = authority.indexOf('@');
        if (userInfoEnd < 0) {
            return url;
        }
        String pathAndBeyond = authorityEnd < 0 ? "" : url.substring(authorityEnd);
        return url.substring(0, authorityStart) + MASK + authority.substring(userInfoEnd) + pathAndBeyond;
    }

    private static String redactQuery(String url) {
        int queryStart = url.indexOf('?');
        if (queryStart < 0) {
            return url;
        }
        StringBuilder redacted = new StringBuilder(url.substring(0, queryStart + 1));
        String[] pairs = url.substring(queryStart + 1).split("&", -1);
        for (int index = 0; index < pairs.length; index++) {
            if (index > 0) {
                redacted.append('&');
            }
            String pair = pairs[index];
            int separator = pair.indexOf('=');
            if (separator < 0) {
                redacted.append(pair);
                continue;
            }
            String name = pair.substring(0, separator);
            redacted.append(name).append('=');
            redacted.append(isSecret(decode(name)) ? MASK : pair.substring(separator + 1));
        }
        return redacted.toString();
    }

    /** Parametre adi encode edilmis olabilir; karsilastirma icin cozulur, ciktiya yazilmaz. */
    private static String decode(String value) {
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return value;
        }
    }

    private static String normalize(String name) {
        return name.replaceAll("[^A-Za-z0-9]", "").toLowerCase(Locale.ROOT);
    }
}
