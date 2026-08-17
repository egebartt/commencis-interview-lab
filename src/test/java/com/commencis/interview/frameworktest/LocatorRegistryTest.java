package com.commencis.interview.frameworktest;

import com.commencis.interview.mobile.locator.ApiDemosLocators;
import com.commencis.interview.mobile.locator.LocatorRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Generic adimlarin dayandigi isim cozumlemesini sabitler.
 * Cihaz gerekmez: yalnizca locator tanimlari okunur, Appium'a baglanilmaz.
 */
@Tag("unit")
@DisplayName("Locator registry")
class LocatorRegistryTest {

    @Test
    @DisplayName("Sayfa adi ve element adiyla locator bulunur")
    void findsLocatorByPageAndKey() {
        assertSame(ApiDemosLocators.VIEWS_MENU, LocatorRegistry.find("Api Demos Page", "VIEWS_MENU"));
    }

    @Test
    @DisplayName("Bosluk, alt cizgi ve buyuk/kucuk harf farki onemsizdir")
    void nameMatchingIsTolerant() {
        assertSame(ApiDemosLocators.VIEWS_MENU, LocatorRegistry.find("api_demos_page", "views menu"));
        assertSame(ApiDemosLocators.VIEWS_MENU, LocatorRegistry.find("ApiDemosPage", "viewsMenu"));
    }

    @Test
    @DisplayName("Noktalama iceren sayfa adi da cozulur")
    void resolvesPageNameWithPunctuation() {
        assertNotNull(LocatorRegistry.find("Views > Controls > 1. Light Theme", "TEXT_FIELD"));
    }

    @Test
    @DisplayName("Olmayan sayfa kayitli sayfalari listeler")
    void unknownPageListsRegisteredPages() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> LocatorRegistry.find("Login Page", "USERNAME"));

        assertTrue(error.getMessage().contains("Api Demos Page"), error.getMessage());
    }

    @Test
    @DisplayName("Olmayan element o sayfanin locator'larini listeler")
    void unknownKeyListsAvailableKeys() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> LocatorRegistry.find("Api Demos Page", "NO_SUCH_ELEMENT"));

        assertTrue(error.getMessage().contains("VIEWS_MENU"), error.getMessage());
    }

    @Test
    @DisplayName("Kayitli sayfa adlari acilir")
    void registeredPageNamesAreExposed() {
        assertEquals(2, LocatorRegistry.registeredPageNames().size());
        assertTrue(LocatorRegistry.registeredPageNames().contains(ApiDemosLocators.PAGE_NAME));
    }
}
