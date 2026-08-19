package com.commencis.mobile.locators;

import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;


public final class InterviewLocators {

    public static final By HOME_ANCHOR = AppiumBy.accessibilityId("Accessibility");
    public static final By VIEWS_MENU = AppiumBy.accessibilityId("Views");
    public static final By BUTTONS_OPTION = AppiumBy.accessibilityId("Buttons");

    private InterviewLocators() {
    }
}
