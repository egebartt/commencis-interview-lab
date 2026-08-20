package com.commencis.mobile.locators;

import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;


public final class InterviewLocators {

    private static final String ID_PREFIX = "com.saucelabs.mydemoapp.android:id/";

    public static final By CATALOG_PRODUCTS = AppiumBy.id(ID_PREFIX + "productRV");
    public static final By CATALOG_PRODUCT_TITLE = AppiumBy.id(ID_PREFIX + "titleTV");
    public static final By CATALOG_PRODUCT_PRICE = AppiumBy.id(ID_PREFIX + "priceTV");

    public static final By SORT_BUTTON =
            AppiumBy.accessibilityId("Shows current sorting order and displays available sorting options");
    public static final By SORT_PRICE_ASCENDING = AppiumBy.id(ID_PREFIX + "priceAscCL");

    public static final By PRODUCT_DETAIL_TITLE = AppiumBy.id(ID_PREFIX + "productTV");
    public static final By PRODUCT_DETAIL_ADD_TO_CART = AppiumBy.accessibilityId("Tap to add product to cart");

    public static final By MENU_BUTTON = AppiumBy.accessibilityId("View menu");
    public static final By MENU_LIST = AppiumBy.id(ID_PREFIX + "menuRV");
    public static final By MENU_WEBVIEW_ITEM = AppiumBy.androidUIAutomator("new UiSelector().text(\"WebView\")");

    public static final By WEBVIEW_URL_INPUT = AppiumBy.id(ID_PREFIX + "urlET");
    public static final By WEBVIEW_GO_BUTTON = AppiumBy.accessibilityId("Tap to view content of given url");
    public static final By WEBVIEW = AppiumBy.id(ID_PREFIX + "webView");

    /** Katalogdaki urunler ayni resourceId'yi paylasir; sirasi calisma aninda instance ile secilir. */
    public static By catalogProduct(int order) {
        return AppiumBy.androidUIAutomator("new UiSelector().resourceId(\"" + ID_PREFIX + "productIV\")"
                + ".instance(" + (order - 1) + ")");
    }

    private InterviewLocators() {
    }
}
