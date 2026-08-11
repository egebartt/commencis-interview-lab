package com.commencis.interview.driver;

import com.commencis.interview.platform.MobilePlatform;
import com.commencis.interview.util.ConfigReader;
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

/** Appium capability'lerini config.properties'ten okuyup driver olusturur. */
public final class MobileDriver {

    private MobileDriver() {
    }

    /** mobile.platform degerine gore Android veya iOS driver olusturur. */
    public static AppiumDriver create() {
        return switch (MobilePlatform.current()) {
            case ANDROID -> createAndroidDriver();
            case IOS -> createIosDriver();
        };
    }

    /** Android cihaz icin UiAutomator2 driver olusturur. */
    public static AndroidDriver createAndroidDriver() {
        String udid = ConfigReader.get("android.udid");
        if (udid.isEmpty()) {
            throw new IllegalStateException("android.udid bos. Bagli cihazlari 'adb devices -l' ile listeleyin, "
                            + "seri numarasini config.properties icine yazin veya -Dandroid.udid=emulator-5554 verin.");
        }

        // Capability = Appium'a "hangi cihazda, hangi uygulamayi, nasil calistir" bilgisini veren ayarlar.
        UiAutomator2Options options = new UiAutomator2Options();
        options.setAutomationName(ConfigReader.get("android.automation.name"));
        options.setUdid(udid);
        setIfPresent("android.device.name", options::setDeviceName);
        setIfPresent("android.platform.version", options::setPlatformVersion);

        // appPath doluysa APK kurulur; bossa cihazdaki uygulama appPackage/appActivity ile acilir.
        String appPath = ConfigReader.get("android.app.path");
        if (!appPath.isEmpty()) {
            options.setApp(appPath);
        } else {
            options.setAppPackage(ConfigReader.require("android.app.package"));
            options.setAppActivity(ConfigReader.require("android.app.activity"));
        }

        // Opsiyonel: uygulama baska bir package/activity ile aciliyorsa Appium'un neyi bekleyecegi.
        setIfPresent("android.app.wait.package", options::setAppWaitPackage);
        setIfPresent("android.app.wait.activity", options::setAppWaitActivity);

        options.setNoReset(ConfigReader.getBoolean("android.no.reset", false));
        options.setAutoGrantPermissions(ConfigReader.getBoolean("android.auto.grant.permissions", true));
        options.setNewCommandTimeout(Duration.ofSeconds(ConfigReader.getInt("android.new.command.timeout", 120)));

        return new AndroidDriver(serverUrl(), options);
    }

    /**
     * iOS cihaz/simulator icin XCUITest driver olusturur.
     *
     * <p>XCUITest calistiran Appium server macOS + Xcode gerektirir. Bu istemcinin isletim sistemi
     * kontrol edilmez; appium.server.url uzaktaki bir macOS makineyi gosterebilir.
     */
    public static IOSDriver createIosDriver() {
        String udid = ConfigReader.get("ios.udid");
        if (udid.isEmpty()) {
            throw new IllegalStateException("ios.udid bos. Cihazlari 'xcrun xctrace list devices' ile listeleyin, "
                            + "udid degerini config.properties icine yazin veya -Dios.udid=<udid> verin.");
        }

        XCUITestOptions options = new XCUITestOptions();
        // XCUITestOptions automationName'i zaten XCUITest'e ayarlar; deger bossa uzerine yazilmaz.
        setIfPresent("ios.automation.name", options::setAutomationName);
        options.setUdid(udid);
        setIfPresent("ios.device.name", options::setDeviceName);
        setIfPresent("ios.platform.version", options::setPlatformVersion);

        // appPath doluysa uygulama kurulur; bossa cihazdaki uygulama bundleId ile acilir.
        String appPath = ConfigReader.get("ios.app.path");
        if (!appPath.isEmpty()) {
            options.setApp(appPath);
        } else {
            options.setBundleId(ConfigReader.require("ios.bundle.id"));
        }

        options.setNoReset(ConfigReader.getBoolean("ios.no.reset", false));
        options.setNewCommandTimeout(Duration.ofSeconds(ConfigReader.getInt("ios.new.command.timeout", 120)));

        return new IOSDriver(serverUrl(), options);
    }

    /** Appium server bu proje tarafindan baslatilmaz; sadece adresi okunur. */
    private static URL serverUrl() {
        String url = ConfigReader.require("appium.server.url");
        try {
            return new URI(url).toURL();
        } catch (URISyntaxException | MalformedURLException e) {
            throw new IllegalStateException("appium.server.url gecerli bir adres degil: " + url, e);
        }
    }

    private static void setIfPresent(String key, java.util.function.Consumer<String> setter) {
        String value = ConfigReader.get(key);
        if (!value.isEmpty()) {
            setter.accept(value);
        }
    }
}
