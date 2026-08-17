package com.commencis.interview.mobile.driver;

import io.appium.java_client.AppiumDriver;

/**
 * Bir senaryonun/testin driver sahibi. Iki modu vardir:
 *
 * <ul>
 *   <li><b>strict</b> (public no-arg constructor, PicoContainer bunu kullanir): driver yalnizca
 *       {@link #start()} ile acilir. {@code @api} senaryosu yanlislikla mobil bir adim cagirirsa
 *       sessizce cihaz acmak yerine acik hata verir.</li>
 *   <li><b>lazy</b> ({@link #lazy()}, JUnit live-coding icin): ilk UI erisiminde driver acilir,
 *       API testleri hicbir zaman cihaz istemez.</li>
 * </ul>
 */
public class DriverManager {

    private final boolean lazy;

    private AppiumDriver driver;

    /** PicoContainer'in cagirdigi constructor: Cucumber tarafinda strict davranis. */
    public DriverManager() {
        this(false);
    }

    private DriverManager(boolean lazy) {
        this.lazy = lazy;
    }

    /** JUnit live-coding adapter'i icin: ilk erisimde driver'i kendisi acar. */
    public static DriverManager lazy() {
        return new DriverManager(true);
    }

    /** Driver'i acar; zaten aciksa tekrar acmaz. */
    public void start() {
        if (driver == null) {
            driver = MobileDriver.create();
        }
    }

    public boolean isStarted() {
        return driver != null;
    }

    public AppiumDriver driver() {
        if (driver == null) {
            if (!lazy) {
                throw new IllegalStateException("Mobil driver acilmadi. Senaryonun @mobile tag'i var mi "
                        + "kontrol edin; strict modda driver'i yalnizca MobileHooks'taki @Before(\"@mobile\") acar.");
            }
            start();
        }
        return driver;
    }

    public void quitIfStarted() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }
}
