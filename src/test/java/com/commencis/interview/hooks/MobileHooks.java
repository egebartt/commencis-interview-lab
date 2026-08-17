package com.commencis.interview.hooks;

import com.commencis.interview.core.report.AllureAttachments;
import com.commencis.interview.mobile.driver.DriverManager;
import io.cucumber.java.After;
import io.cucumber.java.AfterStep;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;

/**
 * Mobil senaryolarin yasam dongusu. Kancalar yalnizca @mobile tag'li senaryolarda calisir;
 * @api senaryolari cihaz veya Appium server gerektirmez.
 */
public class MobileHooks {

    /** @After kancalari azalan order ile calisir: ekran goruntusu driver kapanmadan once alinir. */
    private static final int DRIVER_ORDER = 10;

    private final DriverManager drivers;

    public MobileHooks(DriverManager drivers) {
        this.drivers = drivers;
    }

    @Before("@mobile")
    public void startDriver() {
        drivers.start();
    }

    /** Senaryo basarisiz bitse de calisir; driver kapatilmadan birakilmaz. */
    @After(value = "@mobile", order = DRIVER_ORDER)
    public void quitDriver() {
        drivers.quitIfStarted();
    }

    @After(value = "@mobile", order = 10_000)
    public void bar2ScenarioScreenshot(Scenario scenario) {
        Bar2CucumberHooks.captureIfEnabled(drivers.isStarted() ? drivers.driver() : null,
                scenario, Bar2ReportScreenshot.CapturePoint.SCENARIO);
    }

    @AfterStep(value = "@mobile", order = 10_000)
    public void bar2StepScreenshot(Scenario scenario) {
        Bar2CucumberHooks.captureIfEnabled(drivers.isStarted() ? drivers.driver() : null,
                scenario, Bar2ReportScreenshot.CapturePoint.STEP);
    }

    /**
     * Allure ekran goruntusu. Order 9.000: Bar2 capture'indan (10.000) sonra, driver'i kapatan
     * kancadan (10) once calisir; Bar2 ve Allure ayri boru hatlaridir, biri digerini beslemez.
     */
    @After(value = "@mobile", order = 9_000)
    public void allureScreenshot(Scenario scenario) {
        if (drivers.isStarted()) {
            AllureAttachments.attachScreenshot(drivers.driver(),
                    (scenario.isFailed() ? "Failure screenshot - " : "Final screenshot - ") + scenario.getName());
        }
    }
}
