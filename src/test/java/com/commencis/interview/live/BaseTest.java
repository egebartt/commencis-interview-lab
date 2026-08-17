package com.commencis.interview.live;

import com.commencis.interview.api.ApiClient;
import com.commencis.interview.api.RequestSpecFactory;
import com.commencis.interview.core.report.AllureEnvironment;
import com.commencis.interview.mobile.driver.DriverManager;
import com.commencis.interview.mobile.element.ElementActions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/**
 * JUnit live-coding adapter'inin tek tabani.
 *
 * <p>Bu sinif mulakatta hizlica {@code @Test} yazabilmek icindir; kalici test senaryolarinin
 * yeri feature dosyalaridir. Cucumber tarafi bu sinifi kullanmaz, Hooks + context ile calisir.
 *
 * <p>Driver lazy acilir: {@link #ui()} ilk kez cagrilana kadar Appium'a baglanilmaz, bu yuzden
 * ayni taban API testleri icin de kullanilabilir.
 */
public abstract class BaseTest {

    private final DriverManager drivers = DriverManager.lazy();

    private ApiClient api;
    private ElementActions ui;

    /** Rest Assured istemcisi; base URL aktif ortam dosyasindan gelir. */
    protected ApiClient api() {
        if (api == null) {
            api = new ApiClient(RequestSpecFactory.create());
        }
        return api;
    }

    /** Mobil aksiyonlar; ilk cagride driver acilir. */
    protected ElementActions ui() {
        if (ui == null) {
            ui = new ElementActions(drivers);
        }
        return ui;
    }

    /** Cucumber tarafindaki ReportHooks ile ayni idempotent cagri; JUnit'ten kosulunca da uretilir. */
    @BeforeEach
    void writeReportEnvironment() {
        AllureEnvironment.writeOnce();
    }

    @AfterEach
    void quitDriver() {
        drivers.quitIfStarted();
    }
}
