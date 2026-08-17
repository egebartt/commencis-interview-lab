package com.commencis.interview.mobile.locator;

import com.commencis.interview.core.config.MobilePlatform;
import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;

/**
 * Degeri calisma aninda gelen locator'lar. Sabit elementler {@code *Locators} siniflarinda durur.
 *
 * <p>Gelen metin selector ifadesinin icine gomuldugu icin escape edilir: tirnak veya ters bolu
 * iceren bir deger (ornegin {@code He said "hi"}) aksi halde ifadeyi bozar ve locator sessizce
 * yanlis elemani bulur.
 */
public final class DynamicLocators {

    private DynamicLocators() {
    }

    public static By byText(String text) {
        return MobilePlatform.current().isAndroid()
                ? AppiumBy.androidUIAutomator("new UiSelector().text(\"" + uiAutomatorLiteral(text) + "\")")
                : AppiumBy.iOSNsPredicateString("label == '" + predicateLiteral(text) + "'");
    }

    public static By byTextContains(String text) {
        return MobilePlatform.current().isAndroid()
                ? AppiumBy.androidUIAutomator("new UiSelector().textContains(\"" + uiAutomatorLiteral(text) + "\")")
                : AppiumBy.iOSNsPredicateString("label CONTAINS '" + predicateLiteral(text) + "'");
    }

    public static By byAccessibilityId(String id) {
        return AppiumBy.accessibilityId(id);
    }

    /** Toast yalnizca Android'de ayri bir widget'tir; iOS'ta karsiligi yoktur. */
    public static By toast(String message) {
        if (!MobilePlatform.current().isAndroid()) {
            throw new UnsupportedOperationException("Toast dogrulamasi yalnizca Android icin tanimli.");
        }
        return AppiumBy.xpath("//android.widget.Toast[@text=" + xpathLiteral(message) + "]");
    }

    /** UiSelector cift tirnakli Java string sozdizimi kullanir. */
    static String uiAutomatorLiteral(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    /** NSPredicate tek tirnakli string; ters bolu ve tek tirnak kacisla yazilir. */
    static String predicateLiteral(String value) {
        return value.replace("\\", "\\\\").replace("'", "\\'");
    }

    /**
     * XPath'te kacis karakteri yoktur; tirnaklarin ikisi de geciyorsa deger concat() ile parcalanir.
     * Donen deger tirnaklari kendi icinde tasir.
     */
    static String xpathLiteral(String value) {
        if (!value.contains("'")) {
            return "'" + value + "'";
        }
        if (!value.contains("\"")) {
            return "\"" + value + "\"";
        }
        StringBuilder literal = new StringBuilder("concat(");
        String[] parts = value.split("'", -1);
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) {
                literal.append(", \"'\", ");
            }
            literal.append('\'').append(parts[i]).append('\'');
        }
        return literal.append(')').toString();
    }
}
