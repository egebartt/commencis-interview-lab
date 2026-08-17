package com.commencis.interview.core.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * Ayarlarin tek okuma noktasi.
 *
 * <p>Cozumleme sirasi (ustteki kazanir):
 * <pre>
 * 1) -Dapi.base.url=...            komut satiri
 * 2) API_BASE_URL                  ortam degiskeni (nokta -> alt cizgi, buyuk harf)
 * 3) config/device/&lt;device&gt;.properties
 * 4) config/env/&lt;environment&gt;.properties
 * 5) config/config.properties      ortak varsayilanlar
 * </pre>
 *
 * <p>{@code environment} ve {@code device} degerleri yalnizca ilk uc kaynaktan (-D, ortam
 * degiskeni, varsayilanlar) cozulur; aksi halde hangi dosyanin yuklenecegi kendi icerigine
 * bagli olurdu.
 */
public final class Config {

    private static final String DEFAULTS_RESOURCE = "config/config.properties";
    private static final String ENVIRONMENT_DIRECTORY = "config/env";
    private static final String DEVICE_DIRECTORY = "config/device";

    /** Dosya adi olarak kullanildigi icin path disina cikilmasi engellenir. */
    private static final Pattern SAFE_LAYER_NAME = Pattern.compile("[A-Za-z0-9_-]+");

    private static final Properties DEFAULTS = read(DEFAULTS_RESOURCE);

    private static final String ENVIRONMENT_NAME = layerName("environment");
    private static final String DEVICE_NAME = layerName("device");

    private static final Properties ENVIRONMENT_PROPERTIES =
            read(ENVIRONMENT_DIRECTORY + "/" + ENVIRONMENT_NAME + ".properties");
    private static final Properties DEVICE_PROPERTIES =
            read(DEVICE_DIRECTORY + "/" + DEVICE_NAME + ".properties");

    private Config() {
    }

    /** Aktif ortam adi; {@code -Denvironment=prep} ile degisir. */
    public static String environmentName() {
        return ENVIRONMENT_NAME;
    }

    /** Aktif cihaz profili adi; {@code -Ddevice=android-real} ile degisir. */
    public static String deviceName() {
        return DEVICE_NAME;
    }

    /** Degeri dondurur; tanimli degilse bos string. */
    public static String get(String key) {
        String value = System.getProperty(key);
        if (isBlank(value)) {
            value = System.getenv(key.replace('.', '_').toUpperCase(Locale.ROOT));
        }
        if (isBlank(value)) {
            value = DEVICE_PROPERTIES.getProperty(key);
        }
        if (isBlank(value)) {
            value = ENVIRONMENT_PROPERTIES.getProperty(key);
        }
        if (isBlank(value)) {
            value = DEFAULTS.getProperty(key);
        }
        return isBlank(value) ? "" : value.trim();
    }

    /** Zorunlu ayarlar icin; deger yoksa nedenini soyleyerek durur. */
    public static String require(String key) {
        String value = get(key);
        if (value.isEmpty()) {
            throw new IllegalStateException(key + " tanimli degil. config/config.properties, "
                    + ENVIRONMENT_DIRECTORY + "/" + ENVIRONMENT_NAME + ".properties veya "
                    + DEVICE_DIRECTORY + "/" + DEVICE_NAME + ".properties icine yazin, "
                    + "ya da -D" + key + "=<deger> ile gecin.");
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

    /**
     * Katman adini dosya adi olarak dogrular.
     *
     * <p>Deger dogrudan bir kaynak yoluna girdigi icin serbest birakilamaz: {@code ../../} gibi
     * bir deger classpath disina cikmayi dener.
     */
    public static String requireSafeLayerName(String key, String value) {
        if (isBlank(value)) {
            throw new IllegalStateException(key + " tanimli degil. " + DEFAULTS_RESOURCE
                    + " icine varsayilanini yazin veya -D" + key + "=<ad> ile gecin.");
        }
        String name = value.trim().toLowerCase(Locale.ROOT);
        if (!SAFE_LAYER_NAME.matcher(name).matches()) {
            throw new IllegalStateException(key + " yalnizca harf, rakam, '-' ve '_' icerebilir. "
                    + "Gelen deger: '" + value.trim() + "'");
        }
        return name;
    }

    /** {@code environment} / {@code device} secicisini cozer; kendi katmanlarini okumaz. */
    private static String layerName(String key) {
        String value = System.getProperty(key);
        if (isBlank(value)) {
            value = System.getenv(key.toUpperCase(Locale.ROOT));
        }
        if (isBlank(value)) {
            value = DEFAULTS.getProperty(key);
        }
        return requireSafeLayerName(key, value);
    }

    private static Properties read(String resource) {
        Properties properties = new Properties();
        try (InputStream stream = Config.class.getClassLoader().getResourceAsStream(resource)) {
            if (stream == null) {
                throw new IllegalStateException("Ayar dosyasi bulunamadi: src/test/resources/" + resource);
            }
            try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                properties.load(reader);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Ayar dosyasi okunamadi: " + resource, e);
        }
        return properties;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
