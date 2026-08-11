package com.commencis.interview.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Properties;

/**
 * config.properties dosyasini okur.
 * Oncelik sirasi: -D system property > environment variable > config.properties
 */
public final class ConfigReader {

    private static final Properties PROPERTIES = load();

    private ConfigReader() {}

    private static Properties load() {
        Properties properties = new Properties();
        try (InputStream stream = ConfigReader.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (stream == null) {
                throw new IllegalStateException("config.properties bulunamadi (src/test/resources altinda olmali).");
            }
            try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                properties.load(reader);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("config.properties okunamadi", e);
        }
        return properties;
    }

    /** Degeri dondurur; tanimli degilse bos string. */
    public static String get(String key) {
        String value = System.getProperty(key);
        if (isBlank(value)) {
            // api.auth.token -> API_AUTH_TOKEN
            value = System.getenv(key.replace('.', '_').toUpperCase(Locale.ROOT));
        }
        if (isBlank(value)) {
            value = PROPERTIES.getProperty(key);
        }
        return isBlank(value) ? "" : value.trim();
    }

    /** Zorunlu ayarlar icin; deger yoksa nedenini soyleyerek durur. */
    public static String require(String key) {
        String value = get(key);
        if (value.isEmpty()) {
            throw new IllegalStateException(
                    key + " tanimli degil. config.properties icine yazin veya -D" + key + "=<deger> ile gecin.");
        }
        return value;
    }

    public static int getInt(String key, int defaultValue) {
        String value = get(key);
        if (value.isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalStateException(key + " sayi olmali, gelen deger: " + value, e);
        }
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        String value = get(key);
        return value.isEmpty() ? defaultValue : Boolean.parseBoolean(value);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
