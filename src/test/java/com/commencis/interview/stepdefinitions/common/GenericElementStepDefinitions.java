package com.commencis.interview.stepdefinitions.common;

import com.commencis.interview.mobile.pages.CommonPage;
import io.cucumber.java.en.When;

/**
 * Sayfaya ozel adim yazmadan tek bir elemana dokunmak icin generic Step Definition.
 *
 * <pre>
 * * Click to element "VIEWS_MENU" in "Api Demos Page"
 * </pre>
 *
 * <p>Gherkin'e locator degil <b>element adi</b> girer. Bu sinif locator tipini hic gormez:
 * ad cozumu {@code CommonPage} icinde, selector'in kendisi ilgili Page dosyasinda durur.
 *
 * <p>Bu yol <b>istisna icindir</b>: hizli deneme, kesif, tek seferlik teknik adim. Kalici
 * senaryolar is dili adimlariyla yazilir ({@code When the user opens the Views menu}), cunku
 * generic adimlar feature dosyasini UI script'ine cevirir.
 *
 * <p>Feature'daki {@code *} bir wildcard degil, Given/When/Then yerine kullanilan notr Gherkin
 * anahtar kelimesidir; Java tarafinda karsiligi normal bir Step Definition'dir.
 */
public class GenericElementStepDefinitions {

    private final CommonPage commonPage;

    public GenericElementStepDefinitions(CommonPage commonPage) {
        this.commonPage = commonPage;
    }

    @When("Click to element {string} in {string}")
    public void clickElement(String elementName, String pageName) {
        commonPage.clickElement(elementName, pageName);
    }
}
