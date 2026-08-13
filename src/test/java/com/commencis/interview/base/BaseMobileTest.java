package com.commencis.interview.base;

import com.commencis.interview.driver.MobileDriver;
import io.appium.java_client.AppiumDriver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public abstract class BaseMobileTest {

    protected AppiumDriver driver;

    @BeforeEach
    protected void setUp() {
        driver = MobileDriver.create();
    }

    @AfterEach
    protected void tearDown() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }
}
