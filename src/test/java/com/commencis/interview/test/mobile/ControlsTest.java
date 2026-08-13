package com.commencis.interview.test.mobile;

import com.commencis.interview.base.BaseMobileTest;
import com.commencis.interview.page.ApiDemosPage;
import com.commencis.interview.page.ControlsPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Views > Controls > 1. Light Theme ekrani uzerinde form kontrolleri testleri.
 *
 * <p>Ekrana ulasmak icin menude asagi kaydirmak gerekir; navigasyon
 * {@link ApiDemosPage#openMenuItem(String)} ile yapilir.
 */
@Tag("mobile")
@DisplayName("Views > Controls")
class ControlsTest extends BaseMobileTest {

    /**
     * Her test ayni ekrandan basladigi icin navigasyon tek yerde toplanir.
     *
     * <p>@BeforeEach yerine yardimci metot tercih edildi: donus degeri Page nesnesidir,
     * boylece testler alan (field) paylasmaz ve her test kendi baslangicini acikca gosterir.
     */
    private ControlsPage openControlsScreen() {
        ApiDemosPage menu = new ApiDemosPage(driver);
        menu.openMenuItem("Views");
        menu.openMenuItem("Controls");
        menu.openMenuItem("1. Light Theme");

        ControlsPage page = new ControlsPage(driver);
        assertTrue(page.isFormVisible(), "Controls formu acilmadi");
        return page;
    }

    @Test
    @DisplayName("Metin girisi, checkbox, radio ve toggle birlikte calisir")
    void fillsFormControls() {
        ControlsPage page = openControlsScreen();

        page.enterText("Commencis");

        // Tiklamak durumu tersine cevirir; baslangicta isaretsiz oldugu dogrulanir ki
        // test, onceki bir kosumdan kalan durumu dogru sanmasin.
        assertFalse(page.isFirstCheckboxChecked(), "Checkbox 1 baslangicta isaretli gelmemeliydi");
        page.clickFirstCheckbox();

        page.selectSecondRadioButton();
        page.clickFirstToggle();

        assertAll("Form kontrolleri",
                () -> assertEquals("Commencis", page.getEnteredText(), "Metin alanina yazilan deger tutmadi"),
                () -> assertTrue(page.isFirstCheckboxChecked(), "Checkbox 1 isaretlenmedi"),
                () -> assertTrue(page.isSecondRadioButtonSelected(), "RadioButton 2 secilmedi"),
                () -> assertFalse(page.isFirstRadioButtonSelected(), "Radio grubunda yalnizca bir secim olmaliydi"),
                () -> assertTrue(page.isFirstToggleOn(), "Toggle ON konumuna gecmedi"),
                () -> assertEquals("ON", page.getFirstToggleLabel(), "Toggle etiketi ON olmadi"));
    }

    @Test
    @DisplayName("Dropdown'dan secilen gezegen alanda gorunur")
    void selectsPlanetFromDropdown() {
        ControlsPage page = openControlsScreen();

        assertEquals("Mercury", page.getSelectedPlanet(), "Dropdown varsayilan degeri beklenenden farkli");

        page.selectPlanet("Jupiter");

        assertEquals("Jupiter", page.getSelectedPlanet(), "Secilen gezegen dropdown'da gorunmedi");
    }

    @Test
    @DisplayName("Devre disi Save butonu etkin degildir")
    void disabledSaveButtonIsNotEnabled() {
        ControlsPage page = openControlsScreen();

        assertAll("Save butonlari",
                () -> assertTrue(page.isSaveButtonEnabled(), "Etkin Save butonu devre disi gorundu"),
                () -> assertFalse(page.isDisabledSaveButtonEnabled(), "Devre disi Save butonu etkin gorundu"));
    }
}
