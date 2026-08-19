package com.commencis.interview.mobile.pages;

import com.commencis.interview.mobile.actions.MobileActions;

/**
 * Page Object'lerin ince tabani: tek isi senaryonun {@link MobileActions} ornegini alt siniflara
 * vermektir. Bilerek buyutulmez; teknik metotlar {@link MobileActions} icinde kalir.
 */
public abstract class BasePage {

    protected final MobileActions mobile;

    protected BasePage(MobileActions mobile) {
        this.mobile = mobile;
    }
}
