package com.commencis.interview;

import com.commencis.core.Driver;
import com.commencis.mobile.actions.MobileActions;
import com.commencis.mobile.pages.InterviewPage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.commencis.mobile.actions.MobileActions.byText;
import static com.commencis.mobile.locators.ControlsLocators.CHECKBOX_1;
import static com.commencis.mobile.locators.ControlsLocators.DISABLED_SAVE_BUTTON;
import static com.commencis.mobile.locators.ControlsLocators.PLANET_SPINNER;
import static com.commencis.mobile.locators.ControlsLocators.PLANET_SPINNER_VALUE;
import static com.commencis.mobile.locators.ControlsLocators.RADIO_1;
import static com.commencis.mobile.locators.ControlsLocators.RADIO_2;
import static com.commencis.mobile.locators.ControlsLocators.SAVE_BUTTON;
import static com.commencis.mobile.locators.ControlsLocators.TEXT_FIELD;
import static com.commencis.mobile.locators.ControlsLocators.TOGGLE_1;
import static org.junit.jupiter.api.Assertions.*;


@DisplayName("Interview live coding - mobil")
class InterviewMobile {

    private static final int THIRD_PRODUCT = 3;
    private static final String COMMENCIS_URL = "https://www.commencis.com";
    private static final String WEBVIEW_CONTEXT = "WEBVIEW_com.saucelabs.mydemoapp.android";

    private final Driver driver = new Driver();
    private final MobileActions mobile = new MobileActions(driver);

    @AfterEach
    void quitDriver() {
        driver.quit();
    }


    @Test
    @DisplayName("Interview Mobil - saucelabs Scenario")
    void interViewMobile() {

        /**
         * Mobile (android):
         *
         * apk: GitHub - saucelabs/my-demo-app-android
         * adimlar:
         * 1. app'i ac
         * 2. ana ekran geldigini verify et
         * 3. reorder butona bas
         * 4. price - ascending sirala
         * 5. ekranda gorunen 4 urune gore price-ascending oldugunu verify et
         * 6. sag ustten 3. urune tikla
         * 7. tiklanan urunun acildigini verify et
         * 8. side bar menuye bas
         * 9. Webview'i sec
         * 10. https://www.commencis.com adresine git
         * 11. contexti ve sayfayi verify et
         * 12. sol ustteki menu butona bas
         * 13. menu buton acildigini dogrula
         */

        InterviewPage interviewPage = new InterviewPage(mobile);

        assertTrue(interviewPage.isCatalogVisible(), "Katalog ana ekrani gorunmedi");
        interviewPage.sortByPriceAscending();

        List<Double> prices = interviewPage.visibleProductPrices();
        assertAll(
                () -> assertEquals(4, prices.size(), "Ekranda 4 urun gorunmedi: " + prices),
                () -> assertEquals(prices.stream().sorted().toList(), prices,
                        "Urunler ucuzdan pahaliya siralanmadi: " + prices)
        );

        String thirdProductTitle = interviewPage.catalogProductTitle(THIRD_PRODUCT);
        interviewPage.openProduct(THIRD_PRODUCT);

        assertAll(
                () -> assertTrue(interviewPage.isProductDetailVisible(), "Urun detay ekrani acilmadi"),
                () -> assertEquals(thirdProductTitle, interviewPage.openedProductTitle(), "Tiklanan urun acilmadi")
        );

        interviewPage.openMenu();
        interviewPage.openWebViewFromMenu();

        interviewPage.goToUrl(COMMENCIS_URL);

        assertAll(
                () -> assertTrue(interviewPage.isWebViewVisible(), "WebView sayfasi yuklenmedi"),
                () -> assertEquals(WEBVIEW_CONTEXT, interviewPage.webViewContext(), "WebView context acilmadi")
        );

        interviewPage.openMenu();
        assertTrue(interviewPage.isMenuVisible(), "Menu acilmadi");

    }


    // EXAMPLE FOR REMEMBER APPIUM :) (NOT WORKING)
    @Test
    @DisplayName("Mobil - Appium reference: text, tiklama, dropdown, radio, checkbox")
    void mobileReferences() {

        mobile.scrollAndClickText("Views");
        mobile.scrollAndClickText("Controls");
        mobile.scrollAndClickText("1. Light Theme");

        assertTrue(mobile.isVisible(TEXT_FIELD), "Controls formu acilmadi");

        mobile.clearSendKeys(TEXT_FIELD, "Comm"); //
        mobile.sendKeys(TEXT_FIELD, "encis");  //
        mobile.hideKeyboard();

        String fieldText = mobile.text(TEXT_FIELD);
        String radioLabel = mobile.text(RADIO_2);
        String checkedAttribute = mobile.attribute(CHECKBOX_1, "checked");

        boolean saveTextVisible = mobile.isVisible(byText("Save"));
        boolean missingTextVisible = mobile.isVisible(byText("Yok boyle bir yazi"), 2);

        boolean checkboxBefore = mobile.isChecked(CHECKBOX_1);
        mobile.click(CHECKBOX_1);
        boolean checkboxAfter = mobile.isChecked(CHECKBOX_1);

        mobile.scrollAndClick(RADIO_1);  //
        mobile.scrollAndClick(RADIO_2);
        boolean firstRadioSelected = mobile.isChecked(RADIO_1);
        boolean secondRadioSelected = mobile.isChecked(RADIO_2);

        mobile.click(TOGGLE_1);
        boolean toggleOn = mobile.isChecked(TOGGLE_1);
        String toggleLabel = mobile.text(TOGGLE_1);


        String planetBefore = mobile.text(PLANET_SPINNER_VALUE);
        mobile.selectByText(PLANET_SPINNER, "Jupiter");
        String planetAfter = mobile.text(PLANET_SPINNER_VALUE);
        mobile.selectByScrollingToText(PLANET_SPINNER, "Saturn"); //
        String planetAfterScroll = mobile.text(PLANET_SPINNER_VALUE);

        boolean saveEnabled = mobile.isEnabled(SAVE_BUTTON);
        boolean disabledSaveEnabled = mobile.isEnabled(DISABLED_SAVE_BUTTON);

        mobile.swipeLeft();
        mobile.swipeRight();
        mobile.scrollDown();
        mobile.scrollUp();
        // mobile.swipeLeft(SATIR);
        // mobile.longPress(SATIR);
        // mobile.tapAt(540, 1200);
        // mobile.back();
        // mobile.isToastVisible("Kaydedildi", 3);

        assertTrue(mobile.isVisible(TEXT_FIELD), "Gesture sonrasi form kayboldu");

        assertAll(
                () -> assertEquals("Commencis", fieldText, "Yazilan metin alanda gorunmedi"),
                () -> assertTrue(radioLabel.contains("Radio"), "Radio etiketi beklenenden farkli: " + radioLabel),
                () -> assertFalse(fieldText.isBlank()),
                () -> assertTrue(saveTextVisible, "Save yazisi ekranda bulunamadi"),
                () -> assertFalse(missingTextVisible, "Olmayan metin gorunur raporlandi"),

                () -> assertEquals("false", checkedAttribute, "Checkbox basta isaretsiz olmaliydi"),
                () -> assertFalse(checkboxBefore, "Checkbox basta isaretsiz olmaliydi"),
                () -> assertTrue(checkboxAfter, "Checkbox tiklamadan sonra isaretlenmedi"),
                () -> assertNotEquals(checkboxBefore, checkboxAfter, "Tiklama durumu degistirmedi"),

                () -> assertTrue(secondRadioSelected, "RadioButton 2 secilmedi"),
                () -> assertFalse(firstRadioSelected, "Radio grubunda yalnizca bir secim olmaliydi"),

                () -> assertTrue(toggleOn, "Toggle ON konumuna gecmedi"),
                () -> assertEquals("ON", toggleLabel, "Toggle etiketi durumla eslesmedi"),

                () -> assertEquals("Mercury", planetBefore, "Dropdown varsayilan degeri degismis"),
                () -> assertEquals("Jupiter", planetAfter, "Secilen deger dropdown'da gorunmedi"),
                () -> assertEquals("Saturn", planetAfterScroll, "Kaydirarak secim dropdown'a yansimadi"),

                () -> assertTrue(saveEnabled, "Etkin Save butonu devre disi gorundu"),
                () -> assertFalse(disabledSaveEnabled, "Devre disi Save butonu etkin gorundu")
        );
    }
}
