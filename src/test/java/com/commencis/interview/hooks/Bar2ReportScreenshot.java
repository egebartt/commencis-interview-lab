package com.commencis.interview.hooks;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Browser screenshot capture for a Selenium project, driven by Bar2's Protocol v2 contract.
 *
 * <p><b>This file is generated into your project by the Bar2 Report plugin.</b> It is deliberately a
 * single self-contained class with no dependencies of its own — not Gson, not SLF4J — so it cannot
 * collide with the versions your suite already ships. Regenerating it from the plugin overwrites it.
 *
 * <p>It knows nothing about any test framework. What a step is called, whether it failed and which
 * file it came from are passed IN, by a small hook class that does speak one framework —
 * {@code Bar2GaugeHooks} for Gauge, {@code Bar2CucumberHooks} for Cucumber-JVM. That split is why one
 * recording implementation, with one decision table and one conformance suite behind it, can serve
 * frameworks whose hook APIs have nothing in common.
 *
 * <p>It deliberately declares no hooks of its own, so it cannot conflict with hooks you already have
 * and never needs to guess how your driver is stored.
 *
 * <p><b>Both calls are required.</b> The step call produces step-anchored evidence and the failing
 * step's image; the scenario call is the only place the outcome is known — it carries the final image
 * for LAST_STEP and, even when it captures nothing, the record without which Bar2 must discard every
 * candidate image the scenario produced.
 *
 * <p><b>Hook ordering matters.</b> Gauge runs after-hooks in reverse method-name order, and tagged
 * hooks before untagged ones (verified against gauge-java 0.12.0). If your own teardown quits the
 * browser first, the final image of a <i>passing</i> scenario is lost. Failing scenarios are
 * unaffected — their image is taken at the failing step, precisely so failure evidence does not
 * depend on hook order.
 *
 * <p>Two invariants hold everywhere below: this class never fails a test (every throwable is
 * swallowed and diagnosed), and it never produces wrong evidence (when in doubt it captures nothing
 * and says why). Missing beats misleading.
 */
public final class Bar2ReportScreenshot {

    /** The lifecycle position the calling hook is reporting. */
    public enum CapturePoint {
        STEP,
        SCENARIO
    }

    private Bar2ReportScreenshot() {
    }

    // ---------------------------------------------------------------------------------------------
    // Entry point
    // ---------------------------------------------------------------------------------------------

    /**
     * Records one executed step.
     *
     * <p>Called by a framework hook class, which is what turns that framework's own idea of "a step
     * just finished" into these four plain values. Silent and free when the run was not launched by
     * Bar2, or was launched with capture off.
     *
     * @param driver       the WebDriver for the current thread. May be null - a scenario that failed
     *                     before the browser started is a normal state, not an error.
     * @param scenarioName the scenario this step belongs to.
     * @param sourcePath   the feature/spec file the scenario came from.
     * @param stepText     the step as executed, with its parameters resolved.
     * @param stepFailed   whether THIS step failed.
     */
    public static void step(
            WebDriver driver, String scenarioName, String sourcePath, String stepText, boolean stepFailed
    ) {
        try {
            Env env = Env.current();
            if (!env.active) {
                return;
            }
            recordStep(env, driver, scenarioName, sourcePath, stepText, stepFailed);
        } catch (Throwable t) {
            diagnose(WRITE_FAILED, "hook raised " + t.getClass().getSimpleName());
        }
    }

    /**
     * Records the end of a scenario - the only point at which its outcome is known.
     *
     * <p>Required even when it captures nothing: without this record Bar2 cannot learn the outcome and
     * has to discard every candidate image the scenario produced.
     *
     * @param scenarioFailed whether the scenario failed.
     */
    public static void scenarioEnd(
            WebDriver driver, String scenarioName, String sourcePath, boolean scenarioFailed
    ) {
        try {
            Env env = Env.current();
            if (!env.active) {
                return;
            }
            recordScenarioEnd(env, driver, scenarioName, sourcePath, scenarioFailed);
        } catch (Throwable t) {
            diagnose(WRITE_FAILED, "hook raised " + t.getClass().getSimpleName());
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Recording
    // ---------------------------------------------------------------------------------------------

    private static final ThreadLocal<State> STATE = new ThreadLocal<State>();

    static void recordStep(
            Env env, WebDriver driver, String scenarioName, String sourcePath,
            String stepText, boolean stepFailed
    ) {
        try {
            State current = ensure(scenarioName, sourcePath);
            // Counted for every executed leaf step, captured or not: Bar2 maps this index onto the
            // executed leaf steps of the report, so a gap would shift every later badge.
            current.executedLeafSteps++;

            Decision decision = decide(
                    env.policy, CapturePoint.STEP, stepFailed,
                    current.stepEventsRecorded, current.failedStepCaptured
            );
            if (decision == Decision.SKIP) {
                return;
            }
            String file = captureToFile(env, driver, current, current.executedLeafSteps);
            if (file == null) {
                // Nothing was captured, so there is no evidence to announce — and in particular the
                // failing-step flag stays down, so the scenario hook still takes the fallback image.
                return;
            }
            Event event = newEvent(env, current, Event.TYPE_STEP_SCREENSHOT);
            event.stepIndex = current.executedLeafSteps;
            event.stepText = stepText;
            event.stepStatus = stepFailed ? Event.STATUS_FAILED : Event.STATUS_PASSED;
            event.candidate = decision == Decision.CAPTURE_CANDIDATE;
            event.file = file;
            current.buffered.add(event.toJson());
            current.stepEventsRecorded = true;
            if (stepFailed) {
                current.failedStepCaptured = true;
            }
        } catch (Throwable t) {
            diagnose(WRITE_FAILED, "step hook: " + t.getClass().getSimpleName());
        }
    }

    static void recordScenarioEnd(
            Env env, WebDriver driver, String scenarioName, String sourcePath, boolean scenarioFailed
    ) {
        try {
            State current = ensure(scenarioName, sourcePath);
            Decision decision = decide(
                    env.policy, CapturePoint.SCENARIO, scenarioFailed,
                    current.stepEventsRecorded, current.failedStepCaptured
            );
            if (decision == Decision.SKIP) {
                return;
            }
            String file = null;
            if (decision == Decision.CAPTURE_DEFINITE) {
                file = captureToFile(env, driver, current, current.executedLeafSteps + 1);
            }
            // Written even when the capture failed: without this record Bar2 cannot learn the outcome
            // and drops every candidate the scenario produced.
            Event event = newEvent(env, current, Event.TYPE_SCENARIO_END);
            event.stepIndex = current.executedLeafSteps;
            event.stepStatus = scenarioFailed ? Event.STATUS_FAILED : Event.STATUS_PASSED;
            event.scenarioFailed = Boolean.valueOf(scenarioFailed);
            event.file = file;
            current.buffered.add(event.toJson());

            appendManifest(env.manifestFile, current.buffered);
        } catch (Throwable t) {
            diagnose(WRITE_FAILED, "scenario hook: " + t.getClass().getSimpleName());
        } finally {
            STATE.remove();
        }
    }

    /**
     * State is per thread and is rebuilt whenever the scenario identity changes. That is what keeps a
     * scenario killed by a timeout from leaking its step counter into the next scenario on the same
     * pooled thread, and it is why no "before" hook is required.
     */
    private static State ensure(String scenarioName, String sourcePath) {
        State current = STATE.get();
        if (current == null || !current.matches(scenarioName, sourcePath)) {
            current = new State(scenarioName, sourcePath);
            STATE.set(current);
        }
        return current;
    }

    private static Event newEvent(Env env, State current, String type) {
        Event event = new Event();
        event.runId = env.runId;
        event.framework = env.framework;
        event.eventType = type;
        event.failedMode = env.policy.failedMode.name();
        event.passedMode = env.policy.passedMode.name();
        event.scenarioName = current.scenarioName;
        event.sourcePath = current.sourcePath;
        event.capturedAtMillis = System.currentTimeMillis();
        return event;
    }

    private static final class State {
        final String scenarioName;
        final String sourcePath;
        final List<String> buffered = new ArrayList<String>();
        int executedLeafSteps;
        boolean stepEventsRecorded;
        /** Set only when a *failing* step's image was actually written — see {@link #decideScenario}. */
        boolean failedStepCaptured;

        State(String scenarioName, String sourcePath) {
            this.scenarioName = scenarioName;
            this.sourcePath = sourcePath;
        }

        boolean matches(String otherName, String otherPath) {
            return equal(scenarioName, otherName) && equal(sourcePath, otherPath);
        }

        private static boolean equal(String a, String b) {
            return a == null ? b == null : a.equals(b);
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Decision table — mirrored from com.bar2.core.capture.CaptureDecisionTable
    // ---------------------------------------------------------------------------------------------

    /** Capture depth spoken by Protocol v2. Names match the BAR2_SCREENSHOT_*_MODE wire values. */
    public enum Depth {
        NONE,
        LAST_STEP,
        EVERY_STEP;

        /** Unknown or blank values normalize to {@link #NONE}: an unreadable policy must not capture. */
        public static Depth fromWireName(String value) {
            if (value != null) {
                String trimmed = value.trim();
                for (Depth depth : values()) {
                    if (depth.name().equals(trimmed)) {
                        return depth;
                    }
                }
            }
            return NONE;
        }
    }

    /** What this class must do at a single lifecycle point. */
    public enum Decision {
        /** Do nothing: the driver must not be touched and no event may be written. */
        SKIP,
        /** Take a screenshot and record it as durable. */
        CAPTURE_DEFINITE,
        /** Take a screenshot, but mark it candidate: the scenario outcome may still drop it. */
        CAPTURE_CANDIDATE,
        /** Take no screenshot, but still write SCENARIO_END so candidates can be resolved. */
        FINALIZE_ONLY
    }

    /** Per-outcome capture policy carried by Protocol v2. The two depths may legitimately differ. */
    public static final class Policy {
        public final boolean enabled;
        public final Depth failedMode;
        public final Depth passedMode;

        public Policy(boolean enabled, Depth failedMode, Depth passedMode) {
            this.enabled = enabled;
            this.failedMode = failedMode == null ? Depth.NONE : failedMode;
            this.passedMode = passedMode == null ? Depth.NONE : passedMode;
        }

        public static Policy fromWire(boolean enabled, String failedMode, String passedMode) {
            return new Policy(enabled, Depth.fromWireName(failedMode), Depth.fromWireName(passedMode));
        }

        /** True when no lifecycle point could ever capture, so the calls can short-circuit entirely. */
        public boolean isInert() {
            return !enabled || (failedMode == Depth.NONE && passedMode == Depth.NONE);
        }

        @Override
        public String toString() {
            return "Policy{enabled=" + enabled + ", failed=" + failedMode + ", passed=" + passedMode + "}";
        }
    }

    /**
     * @param failed at {@link CapturePoint#STEP} whether <em>this step</em> failed; at
     *               {@link CapturePoint#SCENARIO} whether the scenario failed.
     * @param stepEventsRecorded whether any step event was already written for this scenario. This
     *               answers "are there candidates to resolve", nothing more.
     * @param failedStepCaptured whether an image of a <em>failing</em> step was actually written.
     *               Deliberately separate from {@code stepEventsRecorded}: a passing step's image also
     *               records an event, and treating the two as one let a passing image suppress the
     *               failure fallback.
     *
     * <p>Both are consulted only at {@link CapturePoint#SCENARIO}; the STEP answer must not depend on
     * either.
     */
    public static Decision decide(
            Policy policy, CapturePoint point, boolean failed,
            boolean stepEventsRecorded, boolean failedStepCaptured
    ) {
        if (policy == null || !policy.enabled) {
            return Decision.SKIP;
        }
        return point == CapturePoint.STEP
                ? decideStep(policy, failed)
                : decideScenario(policy, failed, stepEventsRecorded, failedStepCaptured);
    }

    private static Decision decideStep(Policy policy, boolean stepFailed) {
        // A failed step settles the scenario outcome, so the failed policy applies exactly.
        //
        // LAST_STEP captures HERE rather than waiting for the scenario hook. The failing step is the
        // moment the evidence exists: by scenario end the page may have navigated, an alert may have
        // been dismissed, or another after-hook may already have quit the driver — Gauge runs
        // after-hooks in reverse method-name order, and tagged hooks before untagged ones, so a
        // project's own teardown can legitimately run first. Capturing at the failing step makes
        // failure evidence independent of hook ordering and anchors the image to the failing step.
        if (stepFailed) {
            return policy.failedMode == Depth.NONE ? Decision.SKIP : Decision.CAPTURE_DEFINITE;
        }
        boolean failedWants = policy.failedMode == Depth.EVERY_STEP;
        boolean passedWants = policy.passedMode == Depth.EVERY_STEP;
        if (failedWants && passedWants) {
            // Whatever happens next, the image is kept.
            return Decision.CAPTURE_DEFINITE;
        }
        return (failedWants || passedWants) ? Decision.CAPTURE_CANDIDATE : Decision.SKIP;
    }

    private static Decision decideScenario(
            Policy policy, boolean scenarioFailed, boolean stepEventsRecorded, boolean failedStepCaptured
    ) {
        if (scenarioFailed && policy.failedMode != Depth.NONE) {
            // A failed scenario is owed exactly one image of the failure, at whatever depth.
            //
            // If the failing step already produced it, capturing again would be a second driver
            // round-trip for a worse image — the browser has moved on. If it did not, this is the last
            // moment an image can be taken, and there are three real ways to get here: the step's
            // capture failed (dead session, open alert), nothing ever reported failing (a hook or setup
            // failure), or the depth never captured steps at all. Keyed on the failing step's image
            // rather than on any step event, so a passing step's image cannot suppress this.
            return failedStepCaptured ? Decision.FINALIZE_ONLY : Decision.CAPTURE_DEFINITE;
        }
        if (!scenarioFailed && policy.passedMode == Depth.LAST_STEP) {
            // The single final image a passing scenario asked for.
            return Decision.CAPTURE_DEFINITE;
        }
        // Nothing more is wanted: step images already carry the evidence, or none was requested. A
        // final record is still mandatory when step events exist, because Bar2 discards every step
        // event of a scenario whose outcome it never learned.
        return stepEventsRecorded ? Decision.FINALIZE_ONLY : Decision.SKIP;
    }

    // ---------------------------------------------------------------------------------------------
    // Protocol v2 environment
    // ---------------------------------------------------------------------------------------------

    static final String ENV_ENABLED = "BAR2_SCREENSHOT_ENABLED";
    static final String ENV_PROTOCOL = "BAR2_SCREENSHOT_PROTOCOL";
    static final String ENV_FAILED_MODE = "BAR2_SCREENSHOT_FAILED_MODE";
    static final String ENV_PASSED_MODE = "BAR2_SCREENSHOT_PASSED_MODE";
    static final String ENV_OUTPUT_DIR = "BAR2_SCREENSHOT_OUTPUT_DIR";
    static final String ENV_MANIFEST = "BAR2_SCREENSHOT_MANIFEST";
    static final String ENV_RUN_ID = "BAR2_RUN_ID";
    static final String ENV_FRAMEWORK = "BAR2_FRAMEWORK";

    /** The only protocol revision this file speaks. A newer plugin must not be answered in v2 dialect. */
    static final int PROTOCOL_VERSION = 2;

    /**
     * The contract Bar2 sets on the child process, read once per JVM.
     *
     * <p>An absent contract means the suite is being run by hand rather than by Bar2 — a completely
     * normal state, so this stays inert and silent. A contract that is present but unusable is a real
     * problem and is diagnosed.
     */
    static final class Env {

        private static final Env CURRENT = fromEnvironment();

        final boolean active;
        final Policy policy;
        final String runId;
        final String framework;
        final File manifestFile;
        final File outputDir;
        /** Path prefix, relative to the manifest's directory, that Bar2 resolves image paths against. */
        final String imagePathPrefix;

        private Env(
                boolean active, Policy policy, String runId, String framework,
                File manifestFile, File outputDir, String imagePathPrefix
        ) {
            this.active = active;
            this.policy = policy;
            this.runId = runId;
            this.framework = framework;
            this.manifestFile = manifestFile;
            this.outputDir = outputDir;
            this.imagePathPrefix = imagePathPrefix;
        }

        static Env current() {
            return CURRENT;
        }

        static Env inert() {
            return new Env(false, new Policy(false, null, null), null, null, null, null, "");
        }

        private static Env fromEnvironment() {
            try {
                return parse(
                        read(ENV_ENABLED), read(ENV_PROTOCOL), read(ENV_FAILED_MODE), read(ENV_PASSED_MODE),
                        read(ENV_OUTPUT_DIR), read(ENV_MANIFEST), read(ENV_RUN_ID), read(ENV_FRAMEWORK)
                );
            } catch (Throwable t) {
                return inert();
            }
        }

        /** Visible for tests: the same parsing without touching the real environment. */
        static Env parse(
                String enabled, String protocol, String failedMode, String passedMode,
                String outputDir, String manifest, String runId, String framework
        ) {
            if (isBlank(enabled) && isBlank(manifest) && isBlank(outputDir)) {
                // Not launched by Bar2. Say nothing.
                return inert();
            }
            if (!"true".equalsIgnoreCase(trim(enabled))) {
                // Bar2 launched this run with capture switched off. Nothing to diagnose.
                return inert();
            }
            if (!String.valueOf(PROTOCOL_VERSION).equals(trim(protocol))) {
                diagnose(NO_PROTOCOL, "this adapter speaks protocol v" + PROTOCOL_VERSION
                        + " but the run declared v" + trim(protocol));
                return inert();
            }
            if (isBlank(manifest) || isBlank(outputDir)) {
                diagnose(NO_PROTOCOL, "manifest or output directory was not supplied");
                return inert();
            }

            File manifestFile = new File(trim(manifest));
            File images = new File(trim(outputDir));
            File root = manifestFile.getParentFile();
            if (root == null) {
                diagnose(NO_PROTOCOL, "manifest path has no parent directory");
                return inert();
            }

            String prefix = relativize(root, images);
            if (prefix == null) {
                // Bar2 resolves recorded paths against the manifest's directory, so an image written
                // outside it could never be promoted. Refusing to start beats unreachable evidence.
                diagnose(NO_PROTOCOL, "output directory is not inside the manifest directory");
                return inert();
            }

            Policy policy = Policy.fromWire(true, failedMode, passedMode);
            if (policy.isInert()) {
                return inert();
            }
            return new Env(true, policy, trim(runId), trim(framework), manifestFile, images, prefix);
        }

        /**
         * @return {@code images} expressed relative to {@code root} with forward slashes and a trailing
         *         slash, or {@code null} when it is not inside {@code root}.
         */
        private static String relativize(File root, File images) {
            try {
                String rootPath = root.getCanonicalPath();
                String imagePath = images.getCanonicalPath();
                if (imagePath.equals(rootPath)) {
                    return "";
                }
                String prefix = rootPath.endsWith(File.separator) ? rootPath : rootPath + File.separator;
                if (!imagePath.startsWith(prefix)) {
                    return null;
                }
                String relative = imagePath.substring(prefix.length()).replace('\\', '/');
                return relative.isEmpty() ? "" : relative + "/";
            } catch (Throwable t) {
                return null;
            }
        }

        private static String read(String key) {
            try {
                return System.getenv(key);
            } catch (Throwable t) {
                return null;
            }
        }

        private static boolean isBlank(String value) {
            return value == null || value.trim().isEmpty();
        }

        private static String trim(String value) {
            return value == null ? "" : value.trim();
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Image capture
    // ---------------------------------------------------------------------------------------------

    /** @return the recorded path relative to the manifest directory, or {@code null} on any failure. */
    private static String captureToFile(Env env, WebDriver driver, State current, int sequence) {
        byte[] png = screenshot(driver);
        if (png == null || png.length == 0) {
            return null;
        }
        String name = fileName(current.scenarioName, sequence);
        File target = new File(env.outputDir, name);
        try {
            File parent = target.getParentFile();
            if (parent != null && !parent.isDirectory()) {
                parent.mkdirs();
            }
            OutputStream out = new FileOutputStream(target);
            try {
                out.write(png);
                out.flush();
            } finally {
                out.close();
            }
        } catch (Throwable t) {
            diagnose(WRITE_FAILED, "could not write " + target + ": " + t.getClass().getSimpleName());
            return null;
        }
        return env.imagePathPrefix + name;
    }

    /**
     * The browser viewport — not the whole monitor, which is what the framework's own screenshot gives.
     * Every failure path returns null: never a fallback image, never an exception into your test.
     */
    private static byte[] screenshot(WebDriver driver) {
        if (driver == null) {
            diagnose(NO_DRIVER, "the hook passed no driver; nothing was captured");
            return null;
        }
        if (!(driver instanceof TakesScreenshot)) {
            diagnose(NOT_SCREENSHOTTABLE, driver.getClass().getName() + " does not implement TakesScreenshot");
            return null;
        }
        try {
            byte[] png = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            return (png == null || png.length == 0) ? null : png;
        } catch (Throwable t) {
            // Dead session, open alert, browser already closed: normal states, not test failures.
            diagnose(DRIVER_FAILED, "getScreenshotAs threw " + t.getClass().getSimpleName());
            return null;
        }
    }

    /**
     * Readable enough to debug in the staging folder, unique across threads and across the sibling JVMs
     * Gauge may fork onto the same output directory.
     */
    static String fileName(String scenarioName, int sequence) {
        StringBuilder prefix = new StringBuilder(48);
        String source = scenarioName == null ? "" : scenarioName;
        for (int i = 0; i < source.length() && prefix.length() < 40; i++) {
            char c = source.charAt(i);
            boolean safe = (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9')
                    || c == '.' || c == '-' || c == '_';
            prefix.append(safe ? c : '_');
        }
        if (prefix.length() == 0) {
            prefix.append("scenario");
        }
        String unique = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        return prefix + "-" + sequence + "-" + unique + ".png";
    }

    // ---------------------------------------------------------------------------------------------
    // Manifest
    // ---------------------------------------------------------------------------------------------

    private static final Charset UTF_8 = Charset.forName("UTF-8");

    /** Soft cap per write call; a chunk is always cut after a newline, never inside a line. */
    private static final int CHUNK_BYTES = 32 * 1024;

    private static final Object WRITE_LOCK = new Object();

    /**
     * Appends this scenario's buffered JSONL events. Best-effort; never throws.
     *
     * <p>One flush per scenario, not one per event: an EVERY_STEP suite of 200 scenarios x 20 steps
     * would otherwise mean ~4000 open/close cycles plus lock contention under parallel execution.
     * Nothing is lost by buffering — Bar2 discards every step event of a scenario that never reported
     * its outcome, so a scenario killed mid-flight had no usable evidence either way.
     *
     * <p>Gauge can fork several JVMs sharing one manifest path (verified: {@code gauge run -p -n 2}
     * with multithreading off), so appends are chunked at line boundaries: another process's chunk can
     * land between our lines but never inside one.
     */
    static void appendManifest(File manifestFile, List<String> lines) {
        if (manifestFile == null || lines == null || lines.isEmpty()) {
            return;
        }
        try {
            synchronized (WRITE_LOCK) {
                File parent = manifestFile.getParentFile();
                if (parent != null && !parent.isDirectory()) {
                    parent.mkdirs();
                }
                OutputStream out = new FileOutputStream(manifestFile, true);
                try {
                    StringBuilder chunk = new StringBuilder(CHUNK_BYTES + 1024);
                    for (int i = 0; i < lines.size(); i++) {
                        chunk.append(lines.get(i)).append('\n');
                        if (chunk.length() >= CHUNK_BYTES) {
                            out.write(chunk.toString().getBytes(UTF_8));
                            chunk.setLength(0);
                        }
                    }
                    if (chunk.length() > 0) {
                        out.write(chunk.toString().getBytes(UTF_8));
                    }
                    out.flush();
                } finally {
                    out.close();
                }
            }
        } catch (Throwable t) {
            diagnose(WRITE_FAILED, "could not append to " + manifestFile + ": " + t.getClass().getSimpleName());
        }
    }

    /**
     * One sidecar manifest record.
     *
     * <p>Field names are the wire contract: Bar2 deserializes these lines straight into its own event
     * model, so a rename here silently drops data there. Serialization is hand-written because this
     * file must not put a JSON library on your classpath.
     */
    static final class Event {

        static final String TYPE_STEP_SCREENSHOT = "STEP_SCREENSHOT";
        static final String TYPE_SCENARIO_END = "SCENARIO_END";

        static final String STATUS_PASSED = "PASSED";
        static final String STATUS_FAILED = "FAILED";

        int protocolVersion = PROTOCOL_VERSION;
        String runId;
        String framework;
        String eventType;
        String failedMode;
        String passedMode;
        String scenarioName;
        String sourcePath;
        int stepIndex;
        String stepText;
        String stepStatus;
        Boolean scenarioFailed;
        boolean candidate;
        String file;
        long capturedAtMillis;

        String toJson() {
            StringBuilder out = new StringBuilder(256);
            out.append('{');
            out.append('"').append("protocolVersion").append("\":").append(protocolVersion);
            string(out, "runId", runId);
            string(out, "framework", framework);
            string(out, "eventType", eventType);
            string(out, "failedMode", failedMode);
            string(out, "passedMode", passedMode);
            string(out, "scenarioName", scenarioName);
            string(out, "sourcePath", sourcePath);
            out.append(",\"stepIndex\":").append(stepIndex);
            string(out, "stepText", stepText);
            string(out, "stepStatus", stepStatus);
            if (scenarioFailed != null) {
                out.append(",\"scenarioFailed\":").append(scenarioFailed.booleanValue());
            }
            out.append(",\"candidate\":").append(candidate);
            string(out, "file", file);
            out.append(",\"capturedAtMillis\":").append(capturedAtMillis);
            out.append('}');
            return out.toString();
        }

        /** Null values are omitted rather than written as {@code null}: Bar2's defaults are correct. */
        private static void string(StringBuilder out, String name, String value) {
            if (value == null) {
                return;
            }
            out.append(",\"").append(name).append("\":");
            escape(out, value);
        }

        static void escape(StringBuilder out, String value) {
            out.append('"');
            for (int i = 0; i < value.length(); i++) {
                char c = value.charAt(i);
                switch (c) {
                    case '"':
                        out.append("\\\"");
                        break;
                    case '\\':
                        out.append("\\\\");
                        break;
                    case '\n':
                        out.append("\\n");
                        break;
                    case '\r':
                        out.append("\\r");
                        break;
                    case '\t':
                        out.append("\\t");
                        break;
                    case '\b':
                        out.append("\\b");
                        break;
                    case '\f':
                        out.append("\\f");
                        break;
                    default:
                        if (c < 0x20) {
                            out.append(String.format("\\u%04x", (int) c));
                        } else {
                            out.append(c);
                        }
                }
            }
            out.append('"');
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Diagnostics
    // ---------------------------------------------------------------------------------------------

    /** No usable driver was available, so nothing was captured. */
    static final String NO_DRIVER = "NO_DRIVER";

    /** The driver does not implement TakesScreenshot. */
    static final String NOT_SCREENSHOTTABLE = "NOT_SCREENSHOTTABLE";

    /** The driver threw while producing the image (dead session, alert open, browser closed). */
    static final String DRIVER_FAILED = "DRIVER_FAILED";

    /** The image or manifest could not be written to the run workspace. */
    static final String WRITE_FAILED = "WRITE_FAILED";

    /** The Bar2 environment contract was present but unusable, so this stayed inert. */
    static final String NO_PROTOCOL = "NO_PROTOCOL";

    private static final Set<String> REPORTED = Collections.synchronizedSet(new LinkedHashSet<String>());

    /**
     * Prints each distinct reason to stderr once per JVM; Bar2 reads the child process output and
     * turns it into an explanation. Rate limiting is per reason, not per occurrence: a 200-step suite
     * with no driver must not print 200 lines.
     *
     * @return true the first time this reason is seen.
     */
    static boolean diagnose(String reason, String detail) {
        boolean first;
        synchronized (REPORTED) {
            first = REPORTED.add(reason);
        }
        if (first) {
            String message = "[bar2] screenshot skipped: " + reason
                    + (detail == null || detail.isEmpty() ? "" : " (" + detail + ")");
            try {
                System.err.println(message);
            } catch (Throwable ignored) {
                // Even diagnostics must never fail a test.
            }
        }
        return first;
    }

    /** Test-only query: whether this reason has already been printed. */
    static boolean wasDiagnosed(String reason) {
        return REPORTED.contains(reason);
    }

    /** Test-only reset; production code never clears the set. */
    static void resetDiagnosticsForTest() {
        REPORTED.clear();
    }

    /** Test-only reset of the per-thread scenario state. */
    static void resetStateForTest() {
        STATE.remove();
    }

}
