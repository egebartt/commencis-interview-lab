package com.commencis.core;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.ios.options.XCUITestOptions;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Duration;
import java.util.function.Consumer;

public class Driver {

    private AppiumDriver driver;

    /** Oturumu acar; zaten aciksa tekrar acmaz. */
    public void start() {
        if (driver == null) {
            driver = Config.isAndroid() ? androidDriver() : iosDriver();
        }
    }

    /** Aktif driver. Oturum acilmamissa ilk cagride acilir. */
    public AppiumDriver get() {
        start();
        return driver;
    }

    public boolean isStarted() {
        return driver != null;
    }

    /** Senaryo/test bitiminde cagrilir; oturum acilmadiysa hicbir sey yapmaz. */
    public void quit() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }

    private static AndroidDriver androidDriver() {
        UiAutomator2Options options = new UiAutomator2Options();
        options.setUdid(require("android.udid", "Bagli cihazlari 'adb devices -l' ile listeleyin"));
        setIfPresent("android.device.name", options::setDeviceName);
        setIfPresent("android.platform.version", options::setPlatformVersion);

        // android.app doluysa APK kurulur; bos ise cihazdaki uygulama package/activity ile acilir.
        String app = Config.get("android.app");
        if (!app.isEmpty()) {
            options.setApp(app);
        } else {
            options.setAppPackage(Config.require("android.app.package"));
            options.setAppActivity(Config.require("android.app.activity"));
        }
        setIfPresent("android.app.wait.package", options::setAppWaitPackage);
        setIfPresent("android.app.wait.activity", options::setAppWaitActivity);

        options.setNoReset(Config.getBoolean("android.no.reset", false));
        options.setAutoGrantPermissions(Config.getBoolean("android.auto.grant.permissions", true));
        options.setNewCommandTimeout(Duration.ofSeconds(Config.getInt("appium.command.timeout", 120)));

        return new AndroidDriver(serverUrl(), options);
    }

    /** XCUITest calistiran Appium server macOS + Xcode ister; appium.url uzak bir mac olabilir. */
    private static IOSDriver iosDriver() {
        XCUITestOptions options = new XCUITestOptions();
        options.setUdid(require("ios.udid", "Cihazlari 'xcrun xctrace list devices' ile listeleyin"));
        setIfPresent("ios.device.name", options::setDeviceName);
        setIfPresent("ios.platform.version", options::setPlatformVersion);

        String app = Config.get("ios.app");
        if (!app.isEmpty()) {
            options.setApp(app);
        } else {
            options.setBundleId(Config.require("ios.bundle.id"));
        }

        options.setNoReset(Config.getBoolean("ios.no.reset", false));
        options.setNewCommandTimeout(Duration.ofSeconds(Config.getInt("appium.command.timeout", 120)));

        return new IOSDriver(serverUrl(), options);
    }

    /** Appium server bu proje tarafindan baslatilmaz, yalnizca adresi okunur. */
    private static URL serverUrl() {
        String url = Config.require("appium.url");
        try {
            return new URI(url).toURL();
        } catch (URISyntaxException | MalformedURLException e) {
            throw new IllegalStateException("appium.url gecerli bir adres degil: " + url, e);
        }
    }

    private static String require(String key, String hint) {
        String value = Config.get(key);
        if (value.isEmpty()) {
            throw new IllegalStateException(key + " bos. " + hint
                    + ", degeri config.properties icine yazin veya -D" + key + "=<deger> ile gecin.");
        }
        return value;
    }

    /** Opsiyonel capability: yalnizca deger verilmisse gonderilir. */
    private static void setIfPresent(String key, Consumer<String> setter) {
        String value = Config.get(key);
        if (!value.isEmpty()) {
            setter.accept(value);
        }
    }
}
