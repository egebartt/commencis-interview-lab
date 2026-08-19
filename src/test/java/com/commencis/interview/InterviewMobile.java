package com.commencis.interview;

import com.commencis.core.Driver;
import com.commencis.mobile.actions.MobileActions;
import com.commencis.mobile.pages.ApiDemosPage;
import com.commencis.mobile.pages.InterviewPage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.commencis.mobile.actions.MobileActions.byText;
import static com.commencis.mobile.locators.InterviewLocators.CHECKBOX;
import static com.commencis.mobile.locators.InterviewLocators.DISABLED_SAVE_BUTTON;
import static com.commencis.mobile.locators.InterviewLocators.DROPDOWN;
import static com.commencis.mobile.locators.InterviewLocators.DROPDOWN_VALUE;
import static com.commencis.mobile.locators.InterviewLocators.RADIO_1;
import static com.commencis.mobile.locators.InterviewLocators.RADIO_2;
import static com.commencis.mobile.locators.InterviewLocators.SAVE_BUTTON;
import static com.commencis.mobile.locators.InterviewLocators.TEXT_FIELD;
import static com.commencis.mobile.locators.InterviewLocators.TOGGLE;
import static org.junit.jupiter.api.Assertions.*;


@DisplayName("Interview live coding - mobil")
class InterviewMobile {


    private final Driver driver = new Driver();
    private final MobileActions mobile = new MobileActions(driver);

    @AfterEach
    void quitDriver() {
        driver.quit();
    }

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
        String checkedAttribute = mobile.attribute(CHECKBOX, "checked");

        boolean saveTextVisible = mobile.isVisible(byText("Save"));
        boolean missingTextVisible = mobile.isVisible(byText("Yok boyle bir yazi"), 2);

        boolean checkboxBefore = mobile.isChecked(CHECKBOX);
        mobile.click(CHECKBOX);
        boolean checkboxAfter = mobile.isChecked(CHECKBOX);

        mobile.scrollAndClick(RADIO_1);  //
        mobile.scrollAndClick(RADIO_2);
        boolean firstRadioSelected = mobile.isChecked(RADIO_1);
        boolean secondRadioSelected = mobile.isChecked(RADIO_2);

        mobile.click(TOGGLE);
        boolean toggleOn = mobile.isChecked(TOGGLE);
        String toggleLabel = mobile.text(TOGGLE);


        String planetBefore = mobile.text(DROPDOWN_VALUE);
        mobile.selectByText(DROPDOWN, "Jupiter");
        String planetAfter = mobile.text(DROPDOWN_VALUE);
        mobile.selectByScrollingToText(DROPDOWN, "Saturn"); //
        String planetAfterScroll = mobile.text(DROPDOWN_VALUE);

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

    @Test
    @DisplayName("Mobil - Views menusu acilir")
    void opensViewsMenu() {
        InterviewPage interviewPage = new InterviewPage(mobile);

        assertTrue(interviewPage.isHomeVisible(), "Interview ana ekrani gorunmedi");
        interviewPage.openViews();

        assertTrue(interviewPage.isButtonsVisible(), "Interview Views ekraninda Buttons gorunmedi");
    }

    @Test
    @DisplayName("Mobil - Spinner'dan gezegen secilir")
    void selectsPlanetFromSpinner() {
        ApiDemosPage apiDemos = new ApiDemosPage(mobile);

        apiDemos.openViews();
        apiDemos.openSpinner();
        apiDemos.selectPlanet("Jupiter");

        assertEquals("Jupiter", apiDemos.selectedPlanet());
    }
}
