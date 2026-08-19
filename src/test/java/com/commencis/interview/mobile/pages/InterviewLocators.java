package com.commencis.interview.mobile.pages;

import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;

/** Mulakat provasinda kullanilan ornek ekranin locator'lari. */
final class InterviewLocators {

    static final By HOME_ANCHOR = AppiumBy.accessibilityId("Accessibility");
    static final By VIEWS_MENU = AppiumBy.accessibilityId("Views");
    static final By BUTTONS_OPTION = AppiumBy.accessibilityId("Buttons");

    private InterviewLocators() {
    }
}
