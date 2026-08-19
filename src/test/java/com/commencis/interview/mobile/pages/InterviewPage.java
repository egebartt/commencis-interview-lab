package com.commencis.interview.mobile.pages;

import com.commencis.interview.mobile.actions.MobileActions;

import static com.commencis.interview.mobile.locators.InterviewLocators.BUTTONS_OPTION;
import static com.commencis.interview.mobile.locators.InterviewLocators.HOME_ANCHOR;
import static com.commencis.interview.mobile.locators.InterviewLocators.VIEWS_MENU;

public class InterviewPage extends BasePage {

    public InterviewPage(MobileActions mobile) {
        super(mobile);
    }

    public boolean isHomeVisible() {
        return mobile.isVisible(HOME_ANCHOR);
    }

    public void openViews() {
        mobile.click(VIEWS_MENU);
    }

    public boolean isButtonsVisible() {
        return mobile.isVisible(BUTTONS_OPTION);
    }
}
