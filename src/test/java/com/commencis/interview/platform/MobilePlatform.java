package com.commencis.interview.platform;

import com.commencis.interview.util.ConfigReader;

import java.util.Locale;

/**
 * Testin hangi mobil platformda kosacagini belirler.
 *
 * <p>Tek kaynak config.properties icindeki {@code mobile.platform} degeridir; bagli cihazlar
 * taranarak platform tahmini yapilmaz. Boylece driver ve locator secimi ayni degeri kullanir.
 *
 * <p>Sozlesme: bir JVM kosumu tek platform calistirir.
 */
public enum MobilePlatform {

    ANDROID,
    IOS;

    private static final String CONFIG_KEY = "mobile.platform";

    /** config.properties'teki mobile.platform degerini cozer. */
    public static MobilePlatform current() {
        String value = ConfigReader.require(CONFIG_KEY);
        return switch (value.toLowerCase(Locale.ROOT)) {
            case "android" -> ANDROID;
            case "ios" -> IOS;
            default -> throw new IllegalArgumentException(
                    CONFIG_KEY + " 'android' veya 'ios' olmali, gelen deger: '" + value + "'.");
        };
    }

    public boolean isAndroid() {
        return this == ANDROID;
    }
}
