package com.commencis.interview.test.mobile;

import com.commencis.interview.base.BaseMobileTest;
import com.commencis.interview.page.ApiDemosPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ApiDemos uygulamasi uzerinde ornek Android testleri.
 * Locator'lar kullanilan APK surumune gore Appium Inspector ile dogrulanmalidir.
 */
@Tag("mobile")
@DisplayName("ApiDemos")
class ApiDemosTest extends BaseMobileTest {

    @Test
    @DisplayName("Views ekrani acilir ve geri donulur")
    void opensViewsAndGoesBack() {
        ApiDemosPage page = new ApiDemosPage(driver);

        assertTrue(page.isHomePageVisible(), "Ana menu gorunmedi");

        page.openViews();
        assertTrue(page.isButtonsOptionVisible(), "Views ekraninda Buttons gorunmedi");

        page.goBack();
        assertTrue(page.isHomePageVisible(), "Geri donuste ana menu gorunmedi");
    }

    @Test
    @DisplayName("Views > Spinner dropdown'undan gezegen secilir")
    void selectsPlanetFromDropdown() {
        ApiDemosPage page = new ApiDemosPage(driver);

        page.openViews();
        page.openSpinner();
        page.selectPlanet("Jupiter");

        assertEquals("Jupiter", page.getSelectedPlanet(), "Secilen gezegen dropdown'da gorunmedi");
    }
}
