package com.commencis.interview.mobile.pages;

import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;

/**
 * Api Demos ana menu ve Views ekranlarinin locator'lari.
 * Sinif ve alanlar public degildir: yalnizca ayni paketteki Page'ler okuyabilir.
 */
final class ApiDemosLocators {

    private static final String ID_PREFIX = "io.appium.android.apis:id/";

    static final By ACCESSIBILITY_MENU = AppiumBy.accessibilityId("Accessibility");
    static final By VIEWS_MENU = AppiumBy.accessibilityId("Views");
    static final By BUTTONS_OPTION = AppiumBy.accessibilityId("Buttons");
    static final By SPINNER_OPTION = AppiumBy.accessibilityId("Spinner");
    static final By SWITCHES_OPTION = AppiumBy.accessibilityId("Switches");

    static final By MONITORED_SWITCH = AppiumBy.id(ID_PREFIX + "monitored_switch");

    /** Views &gt; Spinner ekraninda iki dropdown var: spinner1 = "Color:", spinner2 = "Planet:". */
    static final By PLANET_DROPDOWN = AppiumBy.id(ID_PREFIX + "spinner2");

    /** Secili deger spinner'in kendi text'inde degil icindeki TextView'da durur. */
    static final By PLANET_DROPDOWN_VALUE = AppiumBy.androidUIAutomator(
            "new UiSelector().resourceId(\"" + ID_PREFIX + "spinner2\")"
                    + ".childSelector(new UiSelector().resourceId(\"android:id/text1\"))");

    private ApiDemosLocators() {
    }
}
