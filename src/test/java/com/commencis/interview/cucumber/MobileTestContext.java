package com.commencis.interview.cucumber;

import com.commencis.interview.driver.MobileDriver;
import io.appium.java_client.AppiumDriver;

/**
 * Bir senaryo boyunca yasayan mobil durum. PicoContainer her senaryo icin yeni ornek uretir,
 * bu yuzden static driver alani tutulmaz.
 *
 * <p>Driver constructor'da acilmaz; yalnizca {@link MobileHooks} icindeki
 * {@code @Before("@mobile")} kancasi acar. Boylece @api senaryolarinda cihaz aranmaz.
 */
public class MobileTestContext {

    private AppiumDriver driver;

    public void startDriver() {
        driver = MobileDriver.create();
    }

    /** Driver acildi mi? Ekran goruntusu gibi opsiyonel islemler once bunu sorar. */
    public boolean isDriverStarted() {
        return driver != null;
    }

    public AppiumDriver getDriver() {
        if (driver == null) {
            throw new IllegalStateException(
                    "Mobil driver acilmadi. Senaryonun @mobile tag'i var mi kontrol edin; "
                            + "driver yalnizca @mobile senaryolarinda acilir.");
        }
        return driver;
    }

    public void quitDriver() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }
}
