package com.commencis.mobile.pages;

import com.commencis.mobile.actions.MobileActions;

public abstract class BasePage {

    protected final MobileActions mobile;

    protected BasePage(MobileActions mobile) {
        this.mobile = mobile;
    }
}
