package com.commencis.interview.mobile.page;

import com.commencis.interview.mobile.element.ElementActions;

/**
 * Page Object'lerin ortak tabani.
 *
 * <p>Aksiyonlar {@link ElementActions} icindedir; BasePage yalnizca onu tasir. Boylece generic
 * adimlar, Page Object'ler ve JUnit testleri ayni tiklama/bekleme koduna iner.
 */
public abstract class BasePage {

    protected final ElementActions ui;

    protected BasePage(ElementActions ui) {
        this.ui = ui;
    }

    /** Cihazin geri aksiyonu; her ekranda gecerli oldugu icin burada durur. */
    public void goBack() {
        ui.back();
    }
}
