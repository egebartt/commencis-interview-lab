package com.commencis.interview.hooks;

import com.commencis.interview.context.MobileTestContext;
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

    private final MobileTestContext context;

    public MobileHooks(MobileTestContext context) {
        this.context = context;
    }

    @Before("@mobile")
    public void startDriver() {
        context.startDriver();
    }

    /** Senaryo basarisiz bitse de calisir; driver kapatilmadan birakilmaz. */
    @After(value = "@mobile", order = DRIVER_ORDER)
    public void quitDriver() {
        context.quitDriver();
    }

    @After(value = "@mobile", order = 10_000)
    public void bar2ScenarioScreenshot(Scenario scenario) {
        Bar2CucumberHooks.captureIfEnabled(context.isDriverStarted() ? context.getDriver() : null,
                scenario, Bar2ReportScreenshot.CapturePoint.SCENARIO);
    }

    @AfterStep(value = "@mobile", order = 10_000)
    public void bar2StepScreenshot(Scenario scenario) {
        Bar2CucumberHooks.captureIfEnabled(context.isDriverStarted() ? context.getDriver() : null,
                scenario, Bar2ReportScreenshot.CapturePoint.STEP);
    }


}
