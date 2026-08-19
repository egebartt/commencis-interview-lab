package com.commencis.interview.mobile.locators;

import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;


public final class ApiDemosLocators {
    private static final String ID_PREFIX = "io.appium.android.apis:id/";

    public static final By ACCESSIBILITY_MENU = AppiumBy.accessibilityId("Accessibility");
    public static final By VIEWS_MENU = AppiumBy.accessibilityId("Views");
    public static final By BUTTONS_OPTION = AppiumBy.accessibilityId("Buttons");
    public static final By SPINNER_OPTION = AppiumBy.accessibilityId("Spinner");
    public static final By SWITCHES_OPTION = AppiumBy.accessibilityId("Switches");

    public static final By MONITORED_SWITCH = AppiumBy.id(ID_PREFIX + "monitored_switch");

    /** Views &gt; Spinner ekraninda iki dropdown var: spinner1 = "Color:", spinner2 = "Planet:". */
    public static final By PLANET_DROPDOWN = AppiumBy.id(ID_PREFIX + "spinner2");

    /** Secili deger spinner'in kendi text'inde degil icindeki TextView'da durur. */
    public static final By PLANET_DROPDOWN_VALUE = AppiumBy.androidUIAutomator(
            "new UiSelector().resourceId(\"" + ID_PREFIX + "spinner2\")"
                    + ".childSelector(new UiSelector().resourceId(\"android:id/text1\"))");

    private ApiDemosLocators() {
    }
}
