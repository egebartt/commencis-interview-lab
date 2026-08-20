package com.commencis.mobile.pages;

import com.commencis.mobile.actions.MobileActions;

import java.util.List;

import static com.commencis.mobile.locators.InterviewLocators.CATALOG_PRODUCTS;
import static com.commencis.mobile.locators.InterviewLocators.CATALOG_PRODUCT_PRICE;
import static com.commencis.mobile.locators.InterviewLocators.CATALOG_PRODUCT_TITLE;
import static com.commencis.mobile.locators.InterviewLocators.MENU_BUTTON;
import static com.commencis.mobile.locators.InterviewLocators.MENU_LIST;
import static com.commencis.mobile.locators.InterviewLocators.MENU_WEBVIEW_ITEM;
import static com.commencis.mobile.locators.InterviewLocators.PRODUCT_DETAIL_ADD_TO_CART;
import static com.commencis.mobile.locators.InterviewLocators.PRODUCT_DETAIL_TITLE;
import static com.commencis.mobile.locators.InterviewLocators.SORT_BUTTON;
import static com.commencis.mobile.locators.InterviewLocators.SORT_PRICE_ASCENDING;
import static com.commencis.mobile.locators.InterviewLocators.WEBVIEW;
import static com.commencis.mobile.locators.InterviewLocators.WEBVIEW_GO_BUTTON;
import static com.commencis.mobile.locators.InterviewLocators.WEBVIEW_URL_INPUT;
import static com.commencis.mobile.locators.InterviewLocators.catalogProduct;

public class InterviewPage extends BasePage {

    private static final int WEBVIEW_LOAD_TIMEOUT = 60;

    public InterviewPage(MobileActions mobile) {
        super(mobile);
    }

    public boolean isCatalogVisible() {
        return mobile.isVisible(CATALOG_PRODUCTS);
    }

    public void sortByPriceAscending() {
        mobile.click(SORT_BUTTON);
        mobile.click(SORT_PRICE_ASCENDING);
    }

    public List<Double> visibleProductPrices() {
        return mobile.texts(CATALOG_PRODUCT_PRICE).stream()
                .map(price -> Double.parseDouble(price.replaceAll("[^0-9.]", "")))
                .toList();
    }

    public String catalogProductTitle(int order) {
        return mobile.texts(CATALOG_PRODUCT_TITLE).get(order - 1);
    }

    public void openProduct(int order) {
        mobile.click(catalogProduct(order));
    }

    public boolean isProductDetailVisible() {
        return mobile.isVisible(PRODUCT_DETAIL_ADD_TO_CART);
    }

    public String openedProductTitle() {
        return mobile.text(PRODUCT_DETAIL_TITLE);
    }

    public void openMenu() {
        mobile.click(MENU_BUTTON);
    }

    public boolean isMenuVisible() {
        return mobile.isVisible(MENU_LIST);
    }

    public void openWebViewFromMenu() {
        mobile.click(MENU_WEBVIEW_ITEM);
    }

    public void goToUrl(String url) {
        mobile.clearSendKeys(WEBVIEW_URL_INPUT, url);
        mobile.hideKeyboard();
        mobile.click(WEBVIEW_GO_BUTTON);
    }

    public boolean isWebViewVisible() {
        return mobile.isVisible(WEBVIEW, WEBVIEW_LOAD_TIMEOUT);
    }

    public String webViewContext() {
        return mobile.webViewContext();
    }
}
