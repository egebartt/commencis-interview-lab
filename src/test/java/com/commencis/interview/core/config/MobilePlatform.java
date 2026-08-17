package com.commencis.interview.core.config;

import java.util.Locale;

/**
 * Testin hangi mobil platformda kosacagini belirler.
 *
 * <p>Tek kaynak {@code mobile.platform} ayaridir; bagli cihazlar taranarak tahmin yapilmaz.
 * Sozlesme: bir JVM kosumu tek platform calistirir.
 */
public enum MobilePlatform {

    ANDROID,
    IOS;

    private static final String CONFIG_KEY = "mobile.platform";

    public static MobilePlatform current() {
        String value = Config.require(CONFIG_KEY);
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
