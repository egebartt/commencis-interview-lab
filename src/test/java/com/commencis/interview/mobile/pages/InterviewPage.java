package com.commencis.interview.mobile.pages;

import com.commencis.interview.mobile.actions.ElementActions;
import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;

/**
 * Mulakat provasinda kullanilan ornek Page Object: kendi locator'larini tutar, aksiyon yapar,
 * assertion yapmaz.
 *
 * <p>Generic katalogda kayitli degildir; yalnizca kendi Step Definition'i ve
 * {@code InterviewLive} uzerinden kullanilir.
 */
public class InterviewPage extends BasePage {

    private static final By HOME_ANCHOR = AppiumBy.accessibilityId("Accessibility");
    private static final By VIEWS_MENU = AppiumBy.accessibilityId("Views");
    private static final By BUTTONS_OPTION = AppiumBy.accessibilityId("Buttons");

    public InterviewPage(ElementActions element) {
        super(element);
    }

    public boolean isHomeVisible() {
        return element.isVisible(HOME_ANCHOR);
    }

    public void openViews() {
        element.click(VIEWS_MENU);
    }

    public boolean isButtonsVisible() {
        return element.isVisible(BUTTONS_OPTION);
    }
}
