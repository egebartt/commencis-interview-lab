package com.commencis.frameworktest;

import com.commencis.mobile.actions.MobileActions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@code MobileActions.byText} / {@code toast} icin escape testi: escape edilmezse locator
 * sessizce yanlis elemani bulur. Ikisi de saf fonksiyondur, cihaz veya driver gerekmez.
 */
@DisplayName("Dynamic locator escaping")
class DynamicLocatorTest {

    @Test
    @DisplayName("byText duz metni UiSelector'a koyar")
    void buildsPlainTextSelector() {
        By locator = MobileActions.byText("Jupiter");

        assertTrue(locator.toString().contains("text(\"Jupiter\")"), locator.toString());
    }

    @Test
    @DisplayName("byText cift tirnak ve ters bolu iceren metni escape eder")
    void escapesQuotesInText() {
        By locator = MobileActions.byText("He said \"hi\"");

        assertTrue(locator.toString().contains("\\\"hi\\\""), locator.toString());
    }

    @Test
    @DisplayName("toast tek tirnakli metinde xpath'i bozmaz")
    void toastHandlesApostrophe() {
        By plain = MobileActions.toast("Monitored switch is on");
        By withApostrophe = MobileActions.toast("It's on");

        assertTrue(plain.toString().contains("'Monitored switch is on'"), plain.toString());
        assertTrue(withApostrophe.toString().contains("\"It's on\""), withApostrophe.toString());
    }
}
