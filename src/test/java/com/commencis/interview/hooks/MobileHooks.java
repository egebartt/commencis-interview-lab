package com.commencis.interview.hooks;

import com.commencis.interview.context.MobileTestContext;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

/**
 * Mobil senaryolarin yasam dongusu. Kancalar yalnizca @mobile tag'li senaryolarda calisir;
 * @api senaryolari cihaz veya Appium server gerektirmez.
 */
public class MobileHooks {

    /** @After kancalari azalan order ile calisir: ekran goruntusu driver kapanmadan once alinir. */
    private static final int SCREENSHOT_ORDER = 20;
    private static final int DRIVER_ORDER = 10;

    private final MobileTestContext context;

    public MobileHooks(MobileTestContext context) {
        this.context = context;
    }

    @Before("@mobile")
    public void startDriver() {
        context.startDriver();
    }

    /**
     * Yalnizca senaryo basarisiz bittiginde ve driver gercekten acikken PNG ekler.
     * Ekran goruntusu alinamazsa asil test hatasi maskelenmemeli; bu yuzden hata
     * yeniden firlatilmaz, senaryo log'una yazilir.
     */
    @After(value = "@mobile", order = SCREENSHOT_ORDER)
    public void attachScreenshotOnFailure(Scenario scenario) {
        if (!scenario.isFailed() || !context.isDriverStarted()) {
            return;
        }
        try {
            if (context.getDriver() instanceof TakesScreenshot takesScreenshot) {
                scenario.attach(takesScreenshot.getScreenshotAs(OutputType.BYTES), "image/png", scenario.getName());
            }
        } catch (Exception e) {
            scenario.log("Ekran goruntusu alinamadi, asil hata degismedi: " + e);
        }
    }

    /** Senaryo basarisiz bitse de calisir; driver kapatilmadan birakilmaz. */
    @After(value = "@mobile", order = DRIVER_ORDER)
    public void quitDriver() {
        context.quitDriver();
    }

}
