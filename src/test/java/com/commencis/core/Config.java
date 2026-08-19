package com.commencis.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Properties;


public final class Config {

    private static final String FILE = "config.properties";

    private static final Properties PROPERTIES = load();

    private Config() {
    }

    /** Deger tanimli degilse bos string doner. */
    public static String get(String key) {
        String value = System.getProperty(key);
        if (isBlank(value)) {
            value = System.getenv(key.replace('.', '_').toUpperCase(Locale.ROOT));
        }
        if (isBlank(value)) {
            value = PROPERTIES.getProperty(key);
        }
        return isBlank(value) ? "" : value.trim();
    }

    /** Zorunlu ayarlar icin: deger yoksa nereye yazilacagini soyleyerek durur. */
    public static String require(String key) {
        String value = get(key);
        if (value.isEmpty()) {
            throw new IllegalStateException(key + " tanimli degil. src/test/resources/" + FILE
                    + " icine yazin veya -D" + key + "=<deger> ile gecin.");
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

    public static boolean isAndroid() {
        String platform = require("platform").toLowerCase(Locale.ROOT);
        return switch (platform) {
            case "android" -> true;
            case "ios" -> false;
            default -> throw new IllegalStateException(
                    "platform 'android' veya 'ios' olmali, gelen deger: '" + platform + "'");
        };
    }

    private static Properties load() {
        Properties properties = new Properties();
        try (InputStream stream = Config.class.getClassLoader().getResourceAsStream(FILE)) {
            if (stream == null) {
                throw new IllegalStateException("Ayar dosyasi bulunamadi: src/test/resources/" + FILE);
            }
            try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                properties.load(reader);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Ayar dosyasi okunamadi: " + FILE, e);
        }
        return properties;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
