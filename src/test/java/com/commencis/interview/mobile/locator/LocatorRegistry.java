package com.commencis.interview.mobile.locator;

import org.openqa.selenium.By;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * "Sayfa adi + element adi" ile locator cozer; generic Cucumber adimlarinin dayandigi katman.
 *
 * <pre>
 * LocatorRegistry.find("Api Demos Page", "VIEWS_MENU")
 * </pre>
 *
 * <p>Sayfalar {@link #PAGES} icinde acikca kaydedilir; dosya sistemi taranmaz, boylece kosum
 * jar/CI ortaminda da ayni sekilde calisir ve IDE "kim kullaniyor" aramasi bozulmaz.
 *
 * <p>Kayit listesi derleme zamani sabitleriyle kurulur ({@code PAGE_NAME} inline edilir,
 * {@code X.class} sinif yuklemez): bir sayfanin locator alanlari yalnizca o sayfa ilk kez
 * sorgulandiginda okunur. Boylece bozuk tek bir locator sinifi tum kosumu dusurmez.
 *
 * <p>Isim eslesmesi tolerelidir: bosluk, alt cizgi ve noktalama yok sayilir, buyuk/kucuk harf
 * onemsizdir. {@code "Api Demos Page"}, {@code "api_demos_page"} ve {@code "ApiDemosPage"} aynidir.
 */
public final class LocatorRegistry {

    /** Yeni sayfa eklemek: buraya tek satir. */
    private static final List<Page> PAGES = List.of(
            new Page(ApiDemosLocators.PAGE_NAME, ApiDemosLocators.class),
            new Page(ControlsLocators.PAGE_NAME, ControlsLocators.class));

    private static final Map<String, Page> INDEX = index();
    private static final Map<Class<?>, PageLocators> CACHE = new ConcurrentHashMap<>();

    private LocatorRegistry() {
    }

    public static By find(String pageName, String elementKey) {
        Page page = INDEX.get(normalize(pageName));
        if (page == null) {
            throw new IllegalArgumentException("'" + pageName + "' adinda bir sayfa kayitli degil. "
                    + "Kayitli sayfalar: " + registeredPageNames());
        }
        PageLocators locators = CACHE.computeIfAbsent(page.type(), type -> read(page));
        By locator = locators.byKey().get(normalize(elementKey));
        if (locator == null) {
            throw new IllegalArgumentException("'" + page.name() + "' sayfasinda '" + elementKey
                    + "' adinda locator yok. Mevcut locator'lar: " + locators.keys());
        }
        return locator;
    }

    /** Hata mesajlarinda ve dokumantasyonda kullanilir. */
    public static Set<String> registeredPageNames() {
        Set<String> names = new LinkedHashSet<>();
        PAGES.forEach(page -> names.add(page.name()));
        return names;
    }

    private static Map<String, Page> index() {
        Map<String, Page> index = new LinkedHashMap<>();
        for (Page page : PAGES) {
            Page existing = index.put(normalize(page.name()), page);
            if (existing != null) {
                throw new IllegalStateException("Iki sayfa ayni ada cozunuyor: '" + existing.name()
                        + "' (" + existing.type().getSimpleName() + ") ve '" + page.name()
                        + "' (" + page.type().getSimpleName() + "). PAGE_NAME degerlerini ayirin.");
            }
        }
        return index;
    }

    /** Bu cagri ilgili locator sinifini yukler; hata olursa hangi sayfa oldugu mesajda gecer. */
    private static PageLocators read(Page page) {
        Map<String, By> byKey = new LinkedHashMap<>();
        Set<String> keys = new LinkedHashSet<>();
        try {
            for (Field field : page.type().getDeclaredFields()) {
                if (!By.class.isAssignableFrom(field.getType()) || !isConstant(field)) {
                    continue;
                }
                By locator = (By) field.get(null);
                if (locator == null) {
                    continue;
                }
                By existing = byKey.put(normalize(field.getName()), locator);
                if (existing != null) {
                    throw new IllegalStateException("'" + page.name() + "' sayfasinda iki locator ayni ada "
                            + "cozunuyor: " + field.getName());
                }
                keys.add(field.getName());
            }
        } catch (IllegalAccessException | ExceptionInInitializerError | NoClassDefFoundError e) {
            throw new IllegalStateException("'" + page.name() + "' sayfasinin locator'lari yuklenemedi ("
                    + page.type().getName() + ").", e);
        }
        if (byKey.isEmpty()) {
            throw new IllegalStateException("'" + page.name() + "' sayfasinda public static final By alani yok ("
                    + page.type().getName() + ").");
        }
        return new PageLocators(byKey, keys);
    }

    private static boolean isConstant(Field field) {
        int modifiers = field.getModifiers();
        return Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers);
    }

    /** Bosluk, alt cizgi ve noktalama atilir; buyuk harfe cevrilir. */
    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.replaceAll("[^A-Za-z0-9]", "").toUpperCase(Locale.ROOT);
    }

    private record Page(String name, Class<? extends Locators> type) {
    }

    private record PageLocators(Map<String, By> byKey, Set<String> keys) {
    }
}
