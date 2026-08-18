package com.commencis.interview.frameworktest;

import com.commencis.interview.core.Config;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Ayar cozumleme sirasi. Yanlis sirada okumak sessizce yanlis ortama/cihaza istek gonderir,
 * bu yuzden -D onceliginin testi var.
 */
@DisplayName("Config")
class ConfigTest {

    @Test
    @DisplayName("-D degeri dosyadaki degeri ezer")
    void systemPropertyWinsOverFile() {
        System.setProperty("api.timeout", "99");
        try {
            assertEquals(99, Config.getInt("api.timeout", 20));
        } finally {
            System.clearProperty("api.timeout");
        }
    }

    @Test
    @DisplayName("-D yoksa dosyadaki deger okunur")
    void fileValueIsUsedAsFallback() {
        assertEquals("android", Config.get("platform"));
        assertTrue(Config.get("api.base.url").startsWith("http"));
    }

    @Test
    @DisplayName("Tanimsiz anahtar bos string doner, varsayilanlar korunur")
    void missingKeyFallsBackToDefault() {
        assertEquals("", Config.get("bu.anahtar.yok"));
        assertEquals(7, Config.getInt("bu.anahtar.yok", 7));
        assertTrue(Config.getBoolean("bu.anahtar.yok", true));
    }

    @Test
    @DisplayName("require eksik ayarda nereye yazilacagini soyler")
    void requireExplainsMissingKey() {
        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> Config.require("bu.anahtar.yok"));

        assertTrue(error.getMessage().contains("bu.anahtar.yok"), error.getMessage());
        assertTrue(error.getMessage().contains("config.properties"), error.getMessage());
    }

    @Test
    @DisplayName("Sayi bekleyen ayar metin gelirse acik hata verir")
    void getIntRejectsNonNumericValue() {
        System.setProperty("api.timeout", "yirmi");
        try {
            assertThrows(IllegalStateException.class, () -> Config.getInt("api.timeout", 20));
        } finally {
            System.clearProperty("api.timeout");
        }
    }

    @Test
    @DisplayName("platform yalnizca android veya ios olabilir")
    void isAndroidValidatesPlatform() {
        System.setProperty("platform", "ios");
        try {
            assertFalse(Config.isAndroid());
        } finally {
            System.clearProperty("platform");
        }

        System.setProperty("platform", "windows-phone");
        try {
            IllegalStateException error = assertThrows(IllegalStateException.class, Config::isAndroid);
            assertTrue(error.getMessage().contains("windows-phone"), error.getMessage());
        } finally {
            System.clearProperty("platform");
        }

        assertTrue(Config.isAndroid());
    }
}
