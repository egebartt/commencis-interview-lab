package com.commencis.interview.frameworktest;

import com.commencis.interview.mobile.actions.ElementActions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@code ElementActions.byText} / {@code toast} icin escape testi.
 *
 * <p>Metin selector ifadesinin icine gomuldugu icin escape edilmezse locator sessizce yanlis
 * elemani bulur veya hicbir sey bulmaz — kosumda "element yok" gibi gorunur, sebebi gorunmez.
 *
 * <p>Ikisi de static ve saf fonksiyondur: driver, cihaz veya Appium oturumu gerekmez.
 */
@DisplayName("Dynamic locator escaping")
class DynamicLocatorTest {

    @Test
    @DisplayName("byText duz metni UiSelector'a koyar")
    void buildsPlainTextSelector() {
        By locator = ElementActions.byText("Jupiter");

        assertTrue(locator.toString().contains("text(\"Jupiter\")"), locator.toString());
    }

    @Test
    @DisplayName("byText cift tirnak ve ters bolu iceren metni escape eder")
    void escapesQuotesInText() {
        By locator = ElementActions.byText("He said \"hi\"");

        assertTrue(locator.toString().contains("\\\"hi\\\""), locator.toString());
    }

    @Test
    @DisplayName("toast tek tirnakli metinde xpath'i bozmaz")
    void toastHandlesApostrophe() {
        By plain = ElementActions.toast("Monitored switch is on");
        By withApostrophe = ElementActions.toast("It's on");

        assertTrue(plain.toString().contains("'Monitored switch is on'"), plain.toString());
        assertTrue(withApostrophe.toString().contains("\"It's on\""), withApostrophe.toString());
    }
}
