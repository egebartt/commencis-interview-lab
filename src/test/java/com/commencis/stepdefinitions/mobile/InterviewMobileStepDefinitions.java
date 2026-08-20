package com.commencis.stepdefinitions.mobile;

import com.commencis.mobile.pages.InterviewPage;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Interview mobile feature'ini Page Object'e baglar; driver ve locator kullanmaz. */
public class InterviewMobileStepDefinitions {

    private static final String WEBVIEW_CONTEXT = "WEBVIEW_com.saucelabs.mydemoapp.android";

    private final InterviewPage interviewPage;

    private String selectedProductTitle;

    public InterviewMobileStepDefinitions(InterviewPage interviewPage) {
        this.interviewPage = interviewPage;
    }

    @Given("the catalog screen is visible")
    public void catalogScreenIsVisible() {
        assertTrue(interviewPage.isCatalogVisible(), "Katalog ana ekrani gorunmedi");
    }

    @When("the products are sorted by price ascending")
    public void sortProductsByPriceAscending() {
        interviewPage.sortByPriceAscending();
    }

    @Then("the visible products should be listed from the cheapest to the most expensive")
    public void productsAreSortedByPriceAscending() {
        List<Double> prices = interviewPage.visibleProductPrices();

        assertEquals(4, prices.size(), "Ekranda 4 urun gorunmedi: " + prices);
        assertEquals(prices.stream().sorted().toList(), prices,
                "Urunler ucuzdan pahaliya siralanmadi: " + prices);
    }

    @When("the {int}rd product is opened")
    public void openProduct(int order) {
        selectedProductTitle = interviewPage.catalogProductTitle(order);
        interviewPage.openProduct(order);
    }

    @Then("the opened product should be the selected one")
    public void openedProductIsTheSelectedOne() {
        assertTrue(interviewPage.isProductDetailVisible(), "Urun detay ekrani acilmadi");
        assertEquals(selectedProductTitle, interviewPage.openedProductTitle(), "Tiklanan urun acilmadi");
    }

    @When("the WebView screen is opened from the menu")
    public void openWebViewFromMenu() {
        interviewPage.openMenu();
        interviewPage.openWebViewFromMenu();
    }

    @And("{string} is loaded in the webview")
    public void loadUrlInWebView(String url) {
        interviewPage.goToUrl(url);
    }

    @Then("the webview context and page should be opened")
    public void webViewContextAndPageAreOpened() {
        assertTrue(interviewPage.isWebViewVisible(), "WebView sayfasi yuklenmedi");
        assertEquals(WEBVIEW_CONTEXT, interviewPage.webViewContext(), "WebView context acilmadi");
    }

    @When("the menu is opened")
    public void openMenu() {
        interviewPage.openMenu();
    }

    @Then("the menu should be displayed")
    public void menuIsDisplayed() {
        assertTrue(interviewPage.isMenuVisible(), "Menu acilmadi");
    }
}
