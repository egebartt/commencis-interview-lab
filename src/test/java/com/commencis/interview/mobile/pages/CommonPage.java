package com.commencis.interview.mobile.pages;

import com.commencis.interview.mobile.actions.ElementActions;

/**
 * Belirli bir ekrana ait olmayan, hedefi senaryodan gelen aksiyonlar.
 *
 * <p>Generic Cucumber adimlarinin ({@code Click to element "X" in "Y"}) Page katmanindaki
 * karsiligi. Ad cozumu {@link PageElementCatalog}, tiklama {@link ElementActions} tarafindan
 * yapilir; boylece generic yol ile Page Object yolu ayni koda iner.
 *
 * <p>Adi {@code By}'a cevirmek bilerek burada olur: Step Definition locator tipini hic gormez.
 */
public class CommonPage extends BasePage {

    public CommonPage(ElementActions element) {
        super(element);
    }

    /** Parametreler feature'daki okuma sirasiyla ayni: element adi, sonra sayfa adi. */
    public void clickElement(String elementName, String pageName) {
        element.click(PageElementCatalog.find(elementName, pageName));
    }
}
