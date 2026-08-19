package com.commencis.mobile.locators;

import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;

/**
 * Mulakat dosyalarinin ({@code InterviewMobile}, {@code InterviewPage}) kullandigi locator'lar.
 * Ekran degisirse yalnizca bu dosya duzenlenir; test ve page kodu ayni kalir.
 *
 * <p>Oncelik sirasi: accessibilityId &gt; id &gt; androidUIAutomator &gt; className &gt; xpath
 *
 * <pre>
 * AppiumBy.accessibilityId("Views")                                content-desc
 * AppiumBy.id("io.appium.android.apis:id/edit")                    resource-id
 * AppiumBy.androidUIAutomator("new UiSelector().text(\"Save\")")   text / ozellik
 * AppiumBy.className("android.widget.CheckBox")                    tur
 * AppiumBy.xpath("//*[contains(@text,'Save')]")                    son care
 * </pre>
 */
public final class InterviewLocators {

    private static final String ID_PREFIX = "io.appium.android.apis:id/";

    // Ana ekran ve menu
    public static final By HOME_ANCHOR = AppiumBy.accessibilityId("Accessibility");
    public static final By VIEWS_MENU = AppiumBy.accessibilityId("Views");
    public static final By BUTTONS_OPTION = AppiumBy.accessibilityId("Buttons");

    // Ekran: ApiDemos > Views > Controls > 1. Light Theme
    public static final By TEXT_FIELD = AppiumBy.id(ID_PREFIX + "edit");
    public static final By CHECKBOX = AppiumBy.id(ID_PREFIX + "check1");
    public static final By RADIO_1 = AppiumBy.id(ID_PREFIX + "radio1");
    public static final By RADIO_2 = AppiumBy.id(ID_PREFIX + "radio2");
    public static final By TOGGLE = AppiumBy.id(ID_PREFIX + "toggle1");
    public static final By DROPDOWN = AppiumBy.id(ID_PREFIX + "spinner1");
    public static final By SAVE_BUTTON = AppiumBy.id(ID_PREFIX + "button");
    public static final By DISABLED_SAVE_BUTTON = AppiumBy.id(ID_PREFIX + "button_disabled");

    /** Secili deger dropdown'in kendi text'inde degil, icindeki TextView'da durur. */
    public static final By DROPDOWN_VALUE = AppiumBy.androidUIAutomator(
            "new UiSelector().resourceId(\"" + ID_PREFIX + "spinner1\")"
                    + ".childSelector(new UiSelector().resourceId(\"android:id/text1\"))");

    private InterviewLocators() {
    }
}
