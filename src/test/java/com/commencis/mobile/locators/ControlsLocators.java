package com.commencis.mobile.locators;

import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;


public final class ControlsLocators {

    private static final String ID_PREFIX = "io.appium.android.apis:id/";

    public static final By TEXT_FIELD = AppiumBy.id(ID_PREFIX + "edit");

    /** Ekranda iki Save butonu var: bu etkin olan. */
    public static final By SAVE_BUTTON = AppiumBy.id(ID_PREFIX + "button");

    /** Ikinci Save butonu bilerek devre disidir; negatif dogrulama icin kullanilir. */
    public static final By DISABLED_SAVE_BUTTON = AppiumBy.id(ID_PREFIX + "button_disabled");

    public static final By CHECKBOX_1 = AppiumBy.id(ID_PREFIX + "check1");

    public static final By RADIO_1 = AppiumBy.id(ID_PREFIX + "radio1");
    public static final By RADIO_2 = AppiumBy.id(ID_PREFIX + "radio2");

    public static final By TOGGLE_1 = AppiumBy.id(ID_PREFIX + "toggle1");

    public static final By PLANET_SPINNER = AppiumBy.id(ID_PREFIX + "spinner1");

    /** android:id/text1 ekranda baska yerlerde de gectigi icin childSelector ile spinner1'e kapsanir. */
    public static final By PLANET_SPINNER_VALUE = AppiumBy.androidUIAutomator(
            "new UiSelector().resourceId(\"" + ID_PREFIX + "spinner1\")"
                    + ".childSelector(new UiSelector().resourceId(\"android:id/text1\"))");

    private ControlsLocators() {
    }
}
