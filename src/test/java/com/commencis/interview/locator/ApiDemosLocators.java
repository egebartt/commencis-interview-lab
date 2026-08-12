package com.commencis.interview.locator;

import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;

/**
 * ApiDemos ekranina ait locator'lar.
 * Locator onceligi: accessibilityId > id > UiAutomator selector > XPath.
 */
public final class ApiDemosLocators {

    public static final String PAGE_NAME = "Api Demos Page";

    public static final By ACCESSIBILITY_MENU = PlatformBy.of(AppiumBy.accessibilityId("Accessibility"), null);
    public static final By VIEWS_MENU = PlatformBy.of(AppiumBy.accessibilityId("Views"), null);
    public static final By BUTTONS_OPTION = PlatformBy.of(AppiumBy.accessibilityId("Buttons"), null);
    public static final By SPINNER_OPTION = PlatformBy.of(AppiumBy.accessibilityId("Spinner"), null);

    // Views/Spinner ekraninda iki dropdown var: spinner1 = "Color:", spinner2 = "Planet:".
    public static final By PLANET_DROPDOWN = PlatformBy.of(AppiumBy.id("io.appium.android.apis:id/spinner2"), null);

    /**
     * Dropdown'da secili gorunen deger. Spinner container'inin kendi text'i bostur; secili
     * deger icindeki TextView'da durur. android:id/text1 iki spinner'da da ayni oldugu icin
     * childSelector ile spinner2'ye kapsanir.
     */
    public static final By PLANET_DROPDOWN_VALUE = PlatformBy.of(
            AppiumBy.androidUIAutomator(
                    "new UiSelector().resourceId(\"io.appium.android.apis:id/spinner2\")"
                            + ".childSelector(new UiSelector().resourceId(\"android:id/text1\"))"), null);


    /** Metne gore dinamik locator uretir. */
    public static By byText(String text) {
        return PlatformBy.of(
                AppiumBy.androidUIAutomator("new UiSelector().text(\"" + text + "\")"), null);
    }
}
