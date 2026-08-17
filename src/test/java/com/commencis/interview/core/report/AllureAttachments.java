package com.commencis.interview.core.report;

import com.commencis.interview.core.security.SensitiveHeaders;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import io.qameta.allure.Allure;
import io.restassured.response.Response;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;

/**
 * Allure raporuna kanit ekler: API alisverisi ve mobil ekran goruntusu.
 *
 * <p>Maskeleme yalnizca <b>rapor kopyasi</b> uzerinde yapilir; gonderilen istek ve alinan
 * {@link Response} degistirilmez.
 */
public final class AllureAttachments {

    private static final int MAX_BODY_LENGTH = 20_000;

    private static final Gson GSON = new GsonBuilder().serializeNulls().setPrettyPrinting().create();

    private AllureAttachments() {
    }

    public static void attachRequest(String method, String uri, Map<String, String> headers, Object body) {
        Allure.addAttachment("API request", "text/plain", requestText(method, uri, headers, body));
    }

    public static void attachResponse(Response response) {
        String summary = "HTTP " + response.statusCode() + " (" + response.time() + " ms)";
        Allure.addAttachment("API response - " + summary, "application/json", bodyText(response.asString()));
    }

    /**
     * Gizli header degerleri, URL'deki kullanici bilgisi, gizli query parametreleri ve JSON
     * govdesindeki gizli alanlar maskelenir.
     */
    public static String requestText(String method, String uri, Map<String, String> headers, Object body) {
        StringBuilder text = new StringBuilder()
                .append(method == null ? "?" : method)
                .append(' ')
                .append(SensitiveHeaders.redactUrl(uri))
                .append(System.lineSeparator());
        if (headers != null) {
            headers.forEach((name, value) -> text.append(name)
                    .append(": ")
                    .append(SensitiveHeaders.isSecret(name) ? SensitiveHeaders.MASK : value)
                    .append(System.lineSeparator()));
        }
        String bodyText = bodyText(body);
        if (!bodyText.isEmpty()) {
            text.append(System.lineSeparator()).append(bodyText);
        }
        return text.toString();
    }

    /**
     * Govdenin rapora yazilacak halini uretir.
     *
     * <p>JSON ise gizli alanlar ic ice nesne ve dizi seviyesinde maskelenir; Map/POJO gecerli
     * JSON olarak yazilir. JSON degilse mevcut kisaltma davranisi korunur.
     *
     * <p>Ikili govdeler icerik olarak yazilmaz: {@code InputStream} okunursa gonderilecek veri
     * tukenir, {@code byte[]} ise JSON'a cevrilirse hem okunaksiz bir sayi dizisine doner hem de
     * icerigi maskelenmeden rapora gecer.
     */
    public static String bodyText(Object body) {
        if (body == null) {
            return "";
        }
        if (body instanceof InputStream) {
            return "<stream govde - rapora yazilmadi>";
        }
        if (body instanceof byte[] bytes) {
            return "<binary govde, " + bytes.length + " bayt - rapora yazilmadi>";
        }
        String json = redactedJson(body);
        return truncate(json != null ? json : String.valueOf(body));
    }

    /** JSON olarak yorumlanamayan govdelerde null doner. */
    private static String redactedJson(Object body) {
        JsonElement tree;
        if (body instanceof CharSequence text) {
            String trimmed = text.toString().trim();
            if (!trimmed.startsWith("{") && !trimmed.startsWith("[")) {
                return null;
            }
            try {
                tree = JsonParser.parseString(trimmed);
            } catch (RuntimeException e) {
                return null;
            }
        } else {
            try {
                tree = GSON.toJsonTree(body);
            } catch (RuntimeException e) {
                return null;
            }
        }
        return GSON.toJson(redact(tree));
    }

    /** Kaynak agac degistirilmez; maskelenmis bir kopya uretilir. */
    private static JsonElement redact(JsonElement element) {
        if (element.isJsonObject()) {
            JsonObject redacted = new JsonObject();
            for (Map.Entry<String, JsonElement> field : element.getAsJsonObject().entrySet()) {
                redacted.add(field.getKey(), SensitiveHeaders.isSecret(field.getKey())
                        ? new JsonPrimitive(SensitiveHeaders.MASK)
                        : redact(field.getValue()));
            }
            return redacted;
        }
        if (element.isJsonArray()) {
            JsonArray redacted = new JsonArray();
            element.getAsJsonArray().forEach(item -> redacted.add(redact(item)));
            return redacted;
        }
        return element;
    }

    /** Driver kapanmadan cagrilmalidir; alinamazsa kosum bozulmaz. */
    public static void attachScreenshot(WebDriver driver, String name) {
        if (!(driver instanceof TakesScreenshot screenshotTaker)) {
            return;
        }
        try {
            byte[] image = screenshotTaker.getScreenshotAs(OutputType.BYTES);
            Allure.addAttachment(name, "image/png", new ByteArrayInputStream(image), ".png");
        } catch (WebDriverException e) {
            System.err.println("[AllureAttachments] ekran goruntusu alinamadi: " + e.getMessage());
        }
    }

    private static String truncate(String value) {
        if (value == null) {
            return "";
        }
        return value.length() <= MAX_BODY_LENGTH
                ? value
                : value.substring(0, MAX_BODY_LENGTH) + System.lineSeparator() + "... (kisaltildi)";
    }
}
