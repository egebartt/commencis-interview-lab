package com.commencis.hooks;

import com.commencis.core.Config;
import com.commencis.core.Driver;
import com.commencis.core.Redaction;
import io.cucumber.java.After;
import io.cucumber.java.AfterStep;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.qameta.allure.Allure;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriverException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Kosumun yasam dongusu. Cucumber tarafinda "BaseTest"in karsiligi budur: kalitim yerine
 * hook + senaryo omurlu {@link Driver}.
 *
 * <p>Driver yalnizca {@code @mobile} tag'li senaryolarda acilir; {@code @api} senaryolari cihaz
 * veya Appium server istemez.
 */
public class Hooks {

    /** @After kancalari azalan order ile calisir: ekran goruntusu driver kapanmadan once alinir. */
    private static final int DRIVER_ORDER = 10;
    private static final int SCREENSHOT_ORDER = 9_000;
    private static final int BAR2_ORDER = 10_000;

    private static final AtomicBoolean ENVIRONMENT_WRITTEN = new AtomicBoolean(false);

    private final Driver driver;

    /** PicoContainer, ayni Driver ornegini Page'lere ve bu sinifa verir. */
    public Hooks(Driver driver) {
        this.driver = driver;
    }

    @Before(order = 0)
    public void writeReportEnvironment() {
        writeAllureEnvironmentOnce();
    }

    @Before("@mobile")
    public void startDriver() {
        driver.start();
    }

    /** Senaryo basarisiz bitse de calisir; driver kapatilmadan birakilmaz. */
    @After(value = "@mobile", order = DRIVER_ORDER)
    public void quitDriver() {
        driver.quit();
    }

    /** Rapora kanit: basarisiz senaryoda hata ekrani, basarilida son ekran. */
    @After(value = "@mobile", order = SCREENSHOT_ORDER)
    public void attachScreenshot(Scenario scenario) {
        if (!driver.isStarted()) {
            return;
        }
        try {
            byte[] screenshot = ((TakesScreenshot) driver.get()).getScreenshotAs(OutputType.BYTES);
            Allure.addAttachment(scenario.isFailed() ? "Failure screenshot" : "Final screenshot",
                    new ByteArrayInputStream(screenshot));
        } catch (WebDriverException e) {
            // Oturum zaten dusmus olabilir; rapor eki testin sonucunu degistirmemeli.
            System.err.println("[Hooks] Ekran goruntusu alinamadi: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // Bar2 Report plugin entegrasyonu (uretilen dosyalar elle degistirilmez)
    // ------------------------------------------------------------------

    @After(value = "@mobile", order = BAR2_ORDER)
    public void bar2ScenarioScreenshot(Scenario scenario) {
        Bar2CucumberHooks.captureIfEnabled(driver.isStarted() ? driver.get() : null,
                scenario, Bar2ReportScreenshot.CapturePoint.SCENARIO);
    }

    @AfterStep(value = "@mobile", order = BAR2_ORDER)
    public void bar2StepScreenshot(Scenario scenario) {
        Bar2CucumberHooks.captureIfEnabled(driver.isStarted() ? driver.get() : null,
                scenario, Bar2ReportScreenshot.CapturePoint.STEP);
    }

    // ------------------------------------------------------------------
    // Rapor metadata'si
    // ------------------------------------------------------------------

    /**
     * Allure raporundaki "Environment" panelini besler: kosumun hangi platform ve adreslerle
     * yapildigi rapordan gorulur. Kosum basina bir kez yazilir.
     *
     * <p>Yalnizca secret icermeyen metadata yazilir; token buraya girmez.
     */
    private static void writeAllureEnvironmentOnce() {
        if (!ENVIRONMENT_WRITTEN.compareAndSet(false, true)) {
            return;
        }
        Properties values = new Properties();
        values.setProperty("platform", Config.get("platform"));
        // Her iki adres de kullanici bilgisi tasiyabilir (cloud Appium: https://user:key@host).
        values.setProperty("api.base.url", Redaction.maskUrl(Config.get("api.base.url")));
        values.setProperty("appium.url", Redaction.maskUrl(Config.get("appium.url")));

        String results = System.getProperty("allure.results.directory", "target/allure-results");
        Path target = Path.of(results, "environment.properties");
        try {
            Files.createDirectories(target.getParent());
            try (Writer writer = Files.newBufferedWriter(target, StandardCharsets.UTF_8)) {
                values.store(writer, "Commencis test run");
            }
        } catch (IOException e) {
            // Rapor metadata'si testin sonucunu degistirmemeli.
            System.err.println("[Hooks] " + target + " yazilamadi: " + e.getMessage());
        }
    }
}
