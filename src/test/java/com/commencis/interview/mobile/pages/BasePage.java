package com.commencis.interview.mobile.pages;

import com.commencis.interview.mobile.actions.MobileActions;

public abstract class BasePage {

    protected final MobileActions mobile;

    protected BasePage(MobileActions mobile) {
        this.mobile = mobile;
    }
}
