package com.commencis.interview.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.io.File;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Set;

/** Reduction classı = API isteğini değiştirmeden, rapora/loga yazılan kopyadaki token, password, Authorization, cookie gibi değerleri *** yaparak değerleri gizleyip maskeler */
public final class Redaction {

    private static final boolean MASKING_ENABLED = true;
    public static final String MASK = "***";

    /** Normalize edilmis (harf/rakam disi karakterler atilmis, kucuk harf) gizli isimler. */
    private static final Set<String> SECRET_NAMES = Set.of(
            "authorization", "auth", "cookie", "setcookie",
            "apikey", "xapikey", "clientkey", "xclientkey",
            "secretkey", "secret", "clientsecret", "xclientsecret",
            "token", "xauthtoken", "authtoken", "accesstoken", "refreshtoken", "idtoken",
            "password", "passwd", "pwd", "pass", "credential", "credentials",
            "sessionid", "jsessionid", "otp", "pin", "privatekey", "signature");

    private static final Gson GSON = new GsonBuilder().serializeNulls().setPrettyPrinting().create();

    private Redaction() {
    }

    /** Header adi, query parametresi veya JSON alani gizli mi. */
    public static boolean isSecret(String name) {
        if (name == null) {
            return false;
        }
        return SECRET_NAMES.contains(name.replaceAll("[^A-Za-z0-9]", "").toLowerCase(Locale.ROOT));
    }

    public static String maskHeader(String name, String value) {
        if (!MASKING_ENABLED) {
            return value;
        }
        return isSecret(name) ? MASK : value;
    }

    /** URL'deki kullanici bilgisini ({@code https://user:pass@host}) ve gizli query parametrelerinin degerlerini maskeler. */
    public static String maskUrl(String url) {
        if (!MASKING_ENABLED) {
            return url;
        }

        if (url == null || url.isBlank()) {
            return url;
        }
        String masked = url.replaceAll("://[^/@\\s]+@", "://" + MASK + "@");

        int queryStart = masked.indexOf('?');
        if (queryStart < 0) {
            return masked;
        }
        return masked.substring(0, queryStart + 1) + maskQueryString(masked.substring(queryStart + 1));
    }

    /** Body içeriğini rapora güvenli şekilde yazılabilir hale getirir; hassas alanları maskeler, binary/stream içeriklerini rapora basmaz.*/
    public static String maskBody(Object body) {
        if (body == null) {
            return "";
        }
        if (body instanceof byte[] bytes) {
            return "<binary body, " + bytes.length + " bytes>";
        }
        if (body instanceof InputStream) {
            return "<stream body, rapor icin okunmadi>";
        }
        if (body instanceof File file) {
            return "<file body: " + file.getName() + ">";
        }
        if (body instanceof CharSequence text) {
            return maskJson(text.toString());
        }
        try {
            return maskJson(GSON.toJson(body));
        } catch (RuntimeException e) {
            return "<" + body.getClass().getSimpleName() + " body, guvenli sekilde yazilamadi>";
        }
    }

    /** JSON govdedeki gizli alanlari maskeler (ic ice nesne ve dizi dahil). Govde JSON degilse form-encoded kabul edilip ayni kurallarla maskelenir. */
    public static String maskJson(String body) {
        if (!MASKING_ENABLED) {
            return body == null ? "" : body;
        }
        if (body == null || body.isBlank()) {
            return body == null ? "" : body;
        }
        try {
            JsonElement root = JsonParser.parseString(body);
            if (root.isJsonObject() || root.isJsonArray()) {
                maskElement(root);
                return GSON.toJson(root);
            }
        } catch (JsonSyntaxException ignored) {
            // JSON degil; asagida form-encoded olarak denenir.
        }
        return maskQueryString(body);
    }

    /** Gizli anahtarin degeri, tipi ne olursa olsun (nesne ve dizi dahil) tamamen maskelenir. */
    private static void maskElement(JsonElement element) {
        if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            for (String key : new ArrayList<>(object.keySet())) {
                if (isSecret(key)) {
                    object.addProperty(key, MASK);
                } else {
                    maskElement(object.get(key));
                }
            }
        } else if (element.isJsonArray()) {
            element.getAsJsonArray().forEach(Redaction::maskElement);
        }
    }

    /** {@code a=1&token=xyz} bicimindeki metinde gizli anahtarlarin degerini maskeler. */
    private static String maskQueryString(String text) {
        String[] pairs = text.split("&", -1);
        StringBuilder masked = new StringBuilder();
        for (int i = 0; i < pairs.length; i++) {
            if (i > 0) {
                masked.append('&');
            }
            int separator = pairs[i].indexOf('=');
            if (separator > 0 && isSecret(decode(pairs[i].substring(0, separator)))) {
                masked.append(pairs[i], 0, separator + 1).append(MASK);
            } else {
                masked.append(pairs[i]);
            }
        }
        return masked.toString();
    }

    private static String decode(String value) {
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            // Bozuk encoding: ham deger uzerinden kontrol edilir.
            return value;
        }
    }
}
