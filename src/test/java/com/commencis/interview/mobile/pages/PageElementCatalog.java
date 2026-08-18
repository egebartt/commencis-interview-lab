package com.commencis.interview.mobile.pages;

import org.openqa.selenium.By;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Generic adimlarin ("element adi" + "sayfa adi") locator karsiligini bulur.
 *
 * <pre>
 * PageElementCatalog.find("VIEWS_MENU", "Api Demos Page")  -&gt;  ApiDemosPage'in VIEWS_MENU'su
 * </pre>
 *
 * <p><b>Locator'in sahibi bu sinif degildir.</b> Selector'lar ilgili Page dosyasinda
 * {@code private static final By} olarak durur ve burada tekrar yazilmaz; katalog yalnizca
 * ad cozumleme yapar.
 *
 * <p>Kayit iki yerde <b>acikca</b> yapilir: generic kullanima acilan key'ler Page'in
 * package-private {@code namedElements()} metodunda listelenir, yeni bir Page ise ayrica
 * {@link #index()} icinde register edilir. Yani selector tekrarlanmaz ama key adi ikinci kez
 * string olarak yazilir ve eksik kayit derleme zamaninda degil kosumda ortaya cikar.
 * Karsiliginda dosya sistemi taranmaz, reflection ile private alan zorlanmaz ve kosum jar/CI
 * ortaminda da ayni sekilde calisir.
 *
 * <p><b>Ana yol bu degildir.</b> Normal senaryolar Page Object uzerinden yurur ve locator'a
 * derleme zamaninda erisir. Bu katalog yalnizca feature icinde hizlica tek bir elemana dokunmak
 * gerektiginde devreye girer.
 */
public final class PageElementCatalog {

    /** Yeni sayfa acmak: buraya tek satir. */
    private static final Map<String, Map<String, By>> PAGES = index();

    private PageElementCatalog() {
    }

    /** Parametre sirasi feature'daki okuma sirasiyla ayni: element adi, sonra sayfa adi. */
    public static By find(String key, String pageName) {
        Map<String, By> elements = PAGES.get(trim(pageName));
        if (elements == null) {
            throw new IllegalArgumentException("'" + pageName + "' adinda bir sayfa kayitli degil. "
                    + "Kayitli sayfalar: " + pageNames());
        }
        By locator = elements.get(trim(key));
        if (locator == null) {
            throw new IllegalArgumentException("'" + pageName + "' sayfasinda '" + key
                    + "' adinda element yok. Kullanilabilir key'ler: " + elements.keySet());
        }
        return locator;
    }

    /** Hata mesajlarinda ve dokumantasyonda kullanilir. */
    public static Set<String> pageNames() {
        return new LinkedHashSet<>(PAGES.keySet());
    }

    private static Map<String, Map<String, By>> index() {
        Map<String, Map<String, By>> pages = new LinkedHashMap<>();
        register(pages, ApiDemosPage.PAGE_NAME, ApiDemosPage.namedElements());
        register(pages, ControlsPage.PAGE_NAME, ControlsPage.namedElements());
        return Collections.unmodifiableMap(pages);
    }

    private static void register(Map<String, Map<String, By>> pages, String pageName, Map<String, By> elements) {
        if (elements.isEmpty()) {
            throw new IllegalStateException("'" + pageName + "' sayfasi hicbir element kaydetmiyor.");
        }
        Map<String, By> existing = pages.put(trim(pageName), Collections.unmodifiableMap(elements));
        if (existing != null) {
            throw new IllegalStateException("Iki sayfa ayni PAGE_NAME degerini kullaniyor: '"
                    + pageName + "'. Adlari ayirin.");
        }
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
