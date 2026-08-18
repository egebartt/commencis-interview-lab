package com.commencis.interview.frameworktest;

import com.commencis.interview.mobile.pages.PageElementCatalog;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Generic adimlarin dayandigi ad cozumleyici. String tabanli oldugu icin yanlis isim derleme
 * zamaninda degil kosumda ortaya cikar; hata mesajinin yol gostermesi bu yuzden onemli.
 *
 * <p>Test locator sabitlerini ismen kullanamaz: selector'lar Page siniflarinda
 * {@code private}'tir. Bu yuzden dogrulama selector'in icerigi ve kayitli ornegin kararliligi
 * uzerinden yapilir.
 */
@DisplayName("PageElementCatalog")
class PageElementCatalogTest {

    private static final String API_DEMOS = "Api Demos Page";
    private static final String CONTROLS = "Controls Page";

    @Test
    @DisplayName("Sayfa adi + element adi dogru locator'i dondurur")
    void resolvesLocator() {
        assertTrue(PageElementCatalog.find("VIEWS_MENU", API_DEMOS).toString().contains("Views"),
                PageElementCatalog.find("VIEWS_MENU", API_DEMOS).toString());
        assertTrue(PageElementCatalog.find("TEXT_FIELD", CONTROLS).toString()
                        .contains("io.appium.android.apis:id/edit"),
                PageElementCatalog.find("TEXT_FIELD", CONTROLS).toString());
    }

    @Test
    @DisplayName("Ayni key her cagride ayni kayitli By ornegini dondurur")
    void returnsStableRegisteredInstance() {
        By first = PageElementCatalog.find("VIEWS_MENU", API_DEMOS);
        By second = PageElementCatalog.find("VIEWS_MENU", API_DEMOS);

        assertSame(first, second);
    }

    @Test
    @DisplayName("Bastaki/sondaki bosluk yok sayilir")
    void trimsInput() {
        assertSame(PageElementCatalog.find("VIEWS_MENU", API_DEMOS),
                PageElementCatalog.find("  VIEWS_MENU ", "  Api Demos Page  "));
    }

    @Test
    @DisplayName("Bilinmeyen sayfa kayitli sayfalari gosterir")
    void unknownPageListsRegisteredPages() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> PageElementCatalog.find("VIEWS_MENU", "Checkout Page"));

        assertTrue(error.getMessage().contains("Checkout Page"), error.getMessage());
        assertTrue(error.getMessage().contains(API_DEMOS), error.getMessage());
    }

    @Test
    @DisplayName("Bilinmeyen element adi o sayfadaki key'leri gosterir")
    void unknownKeyListsAvailableKeys() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> PageElementCatalog.find("VIEWS_MENUU", API_DEMOS));

        assertTrue(error.getMessage().contains("VIEWS_MENUU"), error.getMessage());
        assertTrue(error.getMessage().contains("VIEWS_MENU"), error.getMessage());
    }

    @Test
    @DisplayName("Kaydedilmemis ic sabitler element olarak cozulmez")
    void ignoresUnregisteredFields() {
        assertThrows(IllegalArgumentException.class,
                () -> PageElementCatalog.find("PAGE_NAME", API_DEMOS));
        assertThrows(IllegalArgumentException.class,
                () -> PageElementCatalog.find("ID_PREFIX", API_DEMOS));
    }

    @Test
    @DisplayName("Kayitli sayfalar listelenebilir")
    void listsPages() {
        assertEquals(2, PageElementCatalog.pageNames().size());
        assertTrue(PageElementCatalog.pageNames().contains(CONTROLS));
    }
}
