package com.commencis.interview.mobile.pages;

import com.commencis.interview.mobile.actions.ElementActions;

/**
 * Page Object'lerin ince tabani. Tek isi, senaryonun {@link ElementActions} ornegini alt
 * siniflara vermektir.
 *
 * <p>Dusuk seviye Appium kodu burada <b>degildir</b>: click/wait/type/scroll {@link ElementActions}
 * icinde yasar. Base sinif buyudukce her ekran istemeden ayni siniftan devraldigi genis bir API
 * tasir; ayirmak "davranis" (Page) ile "teknik dokunus" (ElementActions) sinirini gorunur tutar.
 *
 * <p>Alan {@code protected}'tir: {@code element.click(...)} cagrilari Page icinde kalir, Step
 * Definition ve feature tarafina sizmaz.
 */
public abstract class BasePage {

    protected final ElementActions element;

    protected BasePage(ElementActions element) {
        this.element = element;
    }
}
