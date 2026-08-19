package com.commencis.interview.mobile.pages;

import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;

/**
 * Views &gt; Controls &gt; 1. Light Theme form ekraninin locator'lari.
 * Bu ekrandaki her kontrolun kendi resource-id'si oldugu icin hepsinde id kullanilir.
 */
final class ControlsLocators {

    private static final String ID_PREFIX = "io.appium.android.apis:id/";

    static final By TEXT_FIELD = AppiumBy.id(ID_PREFIX + "edit");

    /** Ekranda iki Save butonu var: bu etkin olan. */
    static final By SAVE_BUTTON = AppiumBy.id(ID_PREFIX + "button");

    /** Ikinci Save butonu bilerek devre disidir; negatif dogrulama icin kullanilir. */
    static final By DISABLED_SAVE_BUTTON = AppiumBy.id(ID_PREFIX + "button_disabled");

    static final By CHECKBOX_1 = AppiumBy.id(ID_PREFIX + "check1");

    static final By RADIO_1 = AppiumBy.id(ID_PREFIX + "radio1");
    static final By RADIO_2 = AppiumBy.id(ID_PREFIX + "radio2");

    static final By TOGGLE_1 = AppiumBy.id(ID_PREFIX + "toggle1");

    static final By PLANET_SPINNER = AppiumBy.id(ID_PREFIX + "spinner1");

    /** android:id/text1 ekranda baska yerlerde de gectigi icin childSelector ile spinner1'e kapsanir. */
    static final By PLANET_SPINNER_VALUE = AppiumBy.androidUIAutomator(
            "new UiSelector().resourceId(\"" + ID_PREFIX + "spinner1\")"
                    + ".childSelector(new UiSelector().resourceId(\"android:id/text1\"))");

    private ControlsLocators() {
    }
}
