package com.commencis.core;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Senaryo boyunca tasinan degerler ve feature dosyasindaki {@code ${...}} yer tutuculari.
 *
 * <pre>
 * ${ctx:postId}           onceki bir adimda kaydedilen deger
 * ${config:api.base.url}  config.properties'ten gelen ayar
 * </pre>
 *
 * <p>PicoContainer her senaryo icin yeni ornek uretir; static alan yoktur, senaryolar
 * birbirinin verisini gormez.
 */
public class TestContext {

    private static final Pattern TOKEN = Pattern.compile("\\$\\{(ctx|config):([^}]+)}");

    private final Map<String, Object> values = new LinkedHashMap<>();

    public void put(String key, Object value) {
        values.put(key, value);
    }

    public String get(String key) {
        if (!values.containsKey(key)) {
            throw new IllegalStateException("Context'te '" + key + "' yok. Once bir adimda kaydedin. "
                    + "Mevcut anahtarlar: " + values.keySet());
        }
        return String.valueOf(values.get(key));
    }

    /** Metindeki yer tutuculari cozer; yer tutucu yoksa metni oldugu gibi dondurur. */
    public String resolve(String text) {
        if (text == null || !text.contains("${")) {
            return text;
        }
        Matcher matcher = TOKEN.matcher(text);
        StringBuilder resolved = new StringBuilder();
        while (matcher.find()) {
            String key = matcher.group(2).trim();
            String value = "ctx".equals(matcher.group(1)) ? get(key) : Config.require(key);
            matcher.appendReplacement(resolved, Matcher.quoteReplacement(value));
        }
        matcher.appendTail(resolved);

        // Cozulmemis bir yer tutucu sessizce gecerse istek yanlis govdeyle gider.
        String result = resolved.toString();
        if (result.contains("${")) {
            throw new IllegalArgumentException("Cozulemeyen yer tutucu: " + result
                    + ". Kullanilabilir bicimler: ${ctx:anahtar}, ${config:anahtar}");
        }
        return result;
    }

    /** Header/query tablolarinda hem anahtar hem deger cozulur. */
    public Map<String, String> resolve(Map<String, String> table) {
        Map<String, String> resolved = new LinkedHashMap<>();
        table.forEach((key, value) -> resolved.put(resolve(key), resolve(value)));
        return resolved;
    }
}
