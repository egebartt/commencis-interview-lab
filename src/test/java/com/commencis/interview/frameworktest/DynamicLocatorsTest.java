package com.commencis.interview.frameworktest;

import com.commencis.interview.mobile.locator.DynamicLocators;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Calisma aninda gelen metnin selector ifadesini bozmadigini dogrular.
 * Cihaz gerekmez: yalnizca uretilen locator ifadesine bakilir.
 */
@Tag("unit")
@DisplayName("Dynamic locator escaping")
class DynamicLocatorsTest {

    private static final String PLATFORM_KEY = "mobile.platform";

    @AfterEach
    void restorePlatform() {
        System.clearProperty(PLATFORM_KEY);
    }

    private static void useIos() {
        System.setProperty(PLATFORM_KEY, "ios");
    }

    @Test
    @DisplayName("UiSelector icindeki cift tirnak kacisla yazilir")
    void androidEscapesDoubleQuote() {
        String locator = DynamicLocators.byText("He said \"hi\"").toString();

        assertTrue(locator.contains("\\\"hi\\\""), locator);
    }

    @Test
    @DisplayName("UiSelector icindeki ters bolu kacisla yazilir")
    void androidEscapesBackslash() {
        String locator = DynamicLocators.byText("C:\\temp").toString();

        assertTrue(locator.contains("C:\\\\temp"), locator);
    }

    @Test
    @DisplayName("UiSelector icindeki tek tirnak sorun cikarmaz")
    void androidKeepsSingleQuote() {
        String locator = DynamicLocators.byTextContains("John's phone").toString();

        assertTrue(locator.contains("John's phone"), locator);
    }

    @Test
    @DisplayName("iOS predicate icindeki tek tirnak kacisla yazilir")
    void iosEscapesSingleQuote() {
        useIos();

        String locator = DynamicLocators.byText("John's phone").toString();

        assertTrue(locator.contains("John\\'s phone"), locator);
    }

    @Test
    @DisplayName("iOS predicate icindeki ters bolu kacisla yazilir")
    void iosEscapesBackslash() {
        useIos();

        String locator = DynamicLocators.byTextContains("a\\b").toString();

        assertTrue(locator.contains("a\\\\b"), locator);
    }

    @Test
    @DisplayName("Toast xpath'inde tek tirnak cift tirnakli literal olur")
    void toastUsesDoubleQuotedLiteralForSingleQuote() {
        String locator = DynamicLocators.toast("John's phone").toString();

        assertTrue(locator.contains("@text=\"John's phone\""), locator);
    }

    @Test
    @DisplayName("Toast xpath'inde her iki tirnak varsa concat kullanilir")
    void toastUsesConcatWhenBothQuotesArePresent() {
        String locator = DynamicLocators.toast("He said \"it's on\"").toString();

        assertTrue(locator.contains("concat("), locator);
        assertTrue(locator.contains("\"'\""), locator);
    }

    @Test
    @DisplayName("Toast yalnizca Android icin tanimlidir")
    void toastIsAndroidOnly() {
        useIos();

        assertThrows(UnsupportedOperationException.class, () -> DynamicLocators.toast("anything"));
    }
}
