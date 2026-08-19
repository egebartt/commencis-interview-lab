package com.commencis.hooks;

import io.cucumber.java.Scenario;
import org.openqa.selenium.WebDriver;

import java.net.URI;

/**
 * The Cucumber-JVM half of Bar2 screenshot capture: it reads Cucumber's {@link Scenario} and
 * hands the plain values to {@link Bar2ReportScreenshot}, which does the deciding and the writing.
 *
 * <p><b>This file is generated into your project by the Bar2 Report plugin</b>, next to
 * {@code Bar2ReportScreenshot}. Everything framework-specific about capture lives here, which is the
 * whole reason it is a separate file — the recording class it calls has no Cucumber types in it
 * and works the same for a Gauge suite through {@code Bar2GaugeHooks}.
 *
 * <p>Call it from your own hooks; it deliberately declares none, so it cannot conflict with
 * hooks you already have and never needs to guess how your driver is stored — which matters in
 * a suite that keeps the driver in a scenario-scoped object graph rather than a static field:
 *
 * <pre>
 * &#64;AfterStep(order = 10_000)
 * public void bar2StepScreenshot(Scenario scenario) {
 *     Bar2CucumberHooks.captureIfEnabled(
 *             getDriver(), scenario, Bar2ReportScreenshot.CapturePoint.STEP);
 * }
 *
 * &#64;After(order = 10_000)
 * public void bar2ScenarioScreenshot(Scenario scenario) {
 *     Bar2CucumberHooks.captureIfEnabled(
 *             getDriver(), scenario, Bar2ReportScreenshot.CapturePoint.SCENARIO);
 * }
 * </pre>
 *
 * <p><b>Both calls are required.</b> The step call produces step-anchored evidence and the
 * failing step's image; the scenario call is the only place the outcome is known — it carries
 * the final image for LAST_STEP and, even when it captures nothing, the record without which
 * Bar2 must discard every candidate image the scenario produced.
 *
 * <p><b>Hook ordering matters.</b> Cucumber runs {@code @After} hooks in DESCENDING order value,
 * so the high {@code order} above runs the capture BEFORE a default-order teardown. Without it,
 * a suite that quits the browser in its own {@code @After} loses the final image of every
 * <i>passing</i> scenario. Failing scenarios are unaffected — their image is taken at the
 * failing step, precisely so failure evidence does not depend on hook order.
 */
public final class Bar2CucumberHooks {

    private Bar2CucumberHooks() {
    }

    /**
     * Records this lifecycle point, capturing an image when the run's policy asks for one.
     *
     * @param driver   the WebDriver for the current scenario. May be null — a scenario that
     *                 failed before the browser started is a normal state, not an error.
     * @param scenario the Cucumber scenario handed to your hook.
     * @param point    which hook is calling.
     */
    public static void captureIfEnabled(
            WebDriver driver, Scenario scenario, Bar2ReportScreenshot.CapturePoint point
    ) {
        boolean failed = scenario != null && scenario.isFailed();
        if (point == Bar2ReportScreenshot.CapturePoint.STEP) {
            Bar2ReportScreenshot.step(
                    driver,
                    scenarioName(scenario),
                    sourcePath(scenario),
                    // Cucumber does not hand the step text to @AfterStep. Bar2 binds the image
                    // by executed-step position instead, and refuses the binding rather than
                    // guessing when the position cannot be verified.
                    null,
                    failed);
        } else {
            Bar2ReportScreenshot.scenarioEnd(
                    driver,
                    scenarioName(scenario),
                    sourcePath(scenario),
                    failed);
        }
    }

    private static String scenarioName(Scenario scenario) {
        return scenario == null ? null : scenario.getName();
    }

    /** The feature file behind this scenario, with the URI scheme removed. */
    private static String sourcePath(Scenario scenario) {
        URI uri = scenario == null ? null : scenario.getUri();
        if (uri == null) {
            return null;
        }
        String path = uri.getPath();
        return path == null || path.isEmpty() ? uri.getSchemeSpecificPart() : path;
    }
}
