package com.commencis.interview.core.report;

import com.commencis.interview.core.config.Config;
import com.commencis.interview.core.security.SensitiveHeaders;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Allure raporunun "Environment" panelini besleyen dosyayi yazar.
 *
 * <p>Dosya {@code target/allure-results} altina yazilir ve {@code clean} ile silinir; bu yuzden
 * statik bir kaynak degil, kosum basinda uretilen bir cikti olmak zorundadir.
 *
 * <p>Yalnizca secret icermeyen metadata yazilir. Token, key ve parola hicbir kosulda buraya
 * girmez; base URL'deki kullanici bilgisi de {@link SensitiveHeaders#redactUrl} ile temizlenir.
 */
public final class AllureEnvironment {

    private static final AtomicBoolean WRITTEN = new AtomicBoolean(false);

    private AllureEnvironment() {
    }

    /** Kosum basina bir kez yazar; birden fazla hook'tan cagrilabilir. */
    public static void writeOnce() {
        if (!WRITTEN.compareAndSet(false, true)) {
            return;
        }
        Map<String, String> values = new LinkedHashMap<>();
        values.put("environment", Config.environmentName());
        values.put("device", Config.deviceName());
        values.put("mobile.platform", Config.get("mobile.platform"));
        // Her iki adres de kullanici bilgisi tasiyabilir (cloud Appium: https://user:key@host).
        values.put("api.base.url", SensitiveHeaders.redactUrl(Config.get("api.base.url")));
        values.put("appium.server.url", SensitiveHeaders.redactUrl(Config.get("appium.server.url")));

        Properties properties = new Properties();
        values.forEach((key, value) -> properties.setProperty(key, value == null ? "" : value));

        Path target = resultsDirectory().resolve("environment.properties");
        try {
            Files.createDirectories(target.getParent());
            try (Writer writer = Files.newBufferedWriter(target, StandardCharsets.UTF_8)) {
                properties.store(writer, "Commencis test run");
            }
        } catch (IOException e) {
            // Rapor metadata'si testin sonucunu degistirmemeli; yazamazsak kosum devam eder.
            System.err.println("[AllureEnvironment] " + target + " yazilamadi: " + e.getMessage());
        }
    }

    private static Path resultsDirectory() {
        String configured = System.getProperty("allure.results.directory");
        return Path.of(configured == null || configured.isBlank() ? "target/allure-results" : configured);
    }
}
