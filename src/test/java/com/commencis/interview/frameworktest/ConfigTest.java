package com.commencis.interview.frameworktest;

import com.commencis.interview.core.config.Config;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Ayar katmanlarinin oncelik sirasini dogrular.
 * {@code config.layer} anahtari uc katman dosyasinda da farkli degerle bulunur.
 */
@Tag("unit")
@DisplayName("Config layer precedence")
class ConfigTest {

    @Test
    @DisplayName("-D degeri tum katmanlari ezer")
    void systemPropertyWinsOverAllLayers() {
        System.setProperty("config.layer", "system");
        try {
            assertEquals("system", Config.get("config.layer"));
        } finally {
            System.clearProperty("config.layer");
        }
    }

    @Test
    @DisplayName("Cihaz katmani ortam ve varsayilanlari ezer")
    void deviceLayerWinsOverEnvironmentAndDefaults() {
        assertEquals("device", Config.get("config.layer"));
    }

    @Test
    @DisplayName("Yalnizca ortam dosyasindaki anahtar okunur")
    void environmentLayerIsRead() {
        assertEquals("https://jsonplaceholder.typicode.com", Config.get("api.base.url"));
    }

    @Test
    @DisplayName("Yalnizca varsayilan dosyadaki anahtar okunur")
    void defaultsLayerIsRead() {
        assertEquals(20, Config.getInt("api.timeout.seconds", 0));
    }

    @Test
    @DisplayName("Aktif katman adlari acilir")
    void activeLayerNamesAreExposed() {
        assertEquals("test", Config.environmentName());
        assertEquals("android-emulator", Config.deviceName());
    }

    @Test
    @DisplayName("Tanimsiz anahtar bos string doner")
    void unknownKeyIsEmpty() {
        assertEquals("", Config.get("there.is.no.such.key"));
    }

    @Test
    @DisplayName("require eksik anahtarda ne yapilacagini soyler")
    void requireExplainsMissingKey() {
        IllegalStateException error =
                assertThrows(IllegalStateException.class, () -> Config.require("there.is.no.such.key"));

        assertTrue(error.getMessage().contains("-Dthere.is.no.such.key"), error.getMessage());
    }

    @Test
    @DisplayName("Katman adi path disina cikamaz")
    void unsafeLayerNameIsRejected() {
        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> Config.requireSafeLayerName("environment", "../../etc/passwd"));

        assertTrue(error.getMessage().contains("environment"), error.getMessage());
    }

    @Test
    @DisplayName("Katman adi kucuk harfe cevrilir")
    void layerNameIsNormalized() {
        assertEquals("prep", Config.requireSafeLayerName("environment", "  PREP  "));
    }

    @Test
    @DisplayName("Bos katman adi reddedilir")
    void blankLayerNameIsRejected() {
        assertThrows(IllegalStateException.class, () -> Config.requireSafeLayerName("device", "  "));
    }
}
