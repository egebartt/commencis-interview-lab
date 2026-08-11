package com.commencis.interview.locator;

import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;

/**
 * ApiDemos ekranina ait locator'lar.
 * Locator onceligi: accessibilityId > id > UiAutomator selector > XPath.
 */
public final class ApiDemosLocators {

    public static final String PAGE_NAME = "Api Demos Page";

    public static final By ACCESSIBILITY_MENU = AppiumBy.accessibilityId("Accessibility");
    public static final By VIEWS_MENU = AppiumBy.accessibilityId("Views");
    public static final By BUTTONS_OPTION = AppiumBy.accessibilityId("Buttons");
    public static final By SPINNER_OPTION = AppiumBy.accessibilityId("Spinner");
    public static final By PLANET_DROPDOWN = AppiumBy.id("io.appium.android.apis:id/spinner1");

    private ApiDemosLocators() {
    }

    /** Metne gore dinamik locator uretir. */
    public static By byText(String text) {
        return AppiumBy.androidUIAutomator("new UiSelector().text(\"" + text + "\")");
    }
}
