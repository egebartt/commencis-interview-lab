package com.commencis.interview.core.data;

import com.commencis.interview.core.config.Config;
import com.commencis.interview.core.context.ScenarioContext;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Feature dosyasindaki {@code ${...}} yer tutucularini cozer.
 *
 * <p>Desteklenen iki kaynak vardir; baskasi eklenmemistir ki adimin ne okudugu tahmin
 * gerektirmesin:
 * <pre>
 * ${ctx:postId}          onceki adimda context'e kaydedilen deger
 * ${config:api.base.url} aktif ortam/cihaz katmanindan gelen ayar
 * </pre>
 *
 * <p>Bilinmeyen bir onek veya bos deger sessizce gecilmez; hata firlatilir.
 */
public class Placeholders {

    private static final Pattern TOKEN = Pattern.compile("\\$\\{([^}]*)}");

    private final ScenarioContext context;

    public Placeholders(ScenarioContext context) {
        this.context = context;
    }

    public String resolve(String text) {
        if (text == null || text.indexOf("${") < 0) {
            return text;
        }
        Matcher matcher = TOKEN.matcher(text);
        StringBuilder resolved = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(resolved, Matcher.quoteReplacement(value(matcher.group(1).trim())));
        }
        matcher.appendTail(resolved);
        return resolved.toString();
    }

    /** Header/query tablolarinda hem anahtar hem deger cozulur. */
    public Map<String, String> resolve(Map<String, String> values) {
        Map<String, String> resolved = new LinkedHashMap<>();
        values.forEach((key, value) -> resolved.put(resolve(key), resolve(value)));
        return resolved;
    }

    private String value(String token) {
        int separator = token.indexOf(':');
        if (separator < 0) {
            throw new IllegalArgumentException("Desteklenmeyen yer tutucu: ${" + token + "}. "
                    + "Kullanilabilir bicimler: ${ctx:anahtar}, ${config:anahtar}");
        }
        String source = token.substring(0, separator).trim();
        String key = token.substring(separator + 1).trim();
        if (key.isEmpty()) {
            throw new IllegalArgumentException("Yer tutucuda anahtar bos: ${" + token + "}");
        }
        return switch (source) {
            case "ctx" -> context.getString(key);
            case "config" -> requireConfig(key);
            default -> throw new IllegalArgumentException("Bilinmeyen yer tutucu kaynagi '" + source
                    + "'. Kullanilabilir kaynaklar: ctx, config");
        };
    }

    private static String requireConfig(String key) {
        String value = Config.get(key);
        if (value.isEmpty()) {
            throw new IllegalStateException("${config:" + key + "} bos. Ayari config/config.properties, "
                    + "aktif ortam/cihaz dosyasina yazin veya -D" + key + "=<deger> ile gecin.");
        }
        return value;
    }
}
