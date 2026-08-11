package com.commencis.interview.locator;

import com.commencis.interview.platform.MobilePlatform;
import org.openqa.selenium.By;

/**
 * Ayni elemanin Android ve iOS locator'i farkliysa aktif platforma uygun olani secer.
 *
 * <pre>
 * public static final By PAY_BUTTON = PlatformBy.of(
 *         AppiumBy.id("com.example:id/payButton"),
 *         AppiumBy.accessibilityId("payButton"));
 * </pre>
 *
 * <p>Locator alanlari {@code public static final} oldugu icin secim class yuklenirken bir kez
 * yapilir; ayni JVM icinde paralel Android + iOS kosumu desteklenmez.
 *
 * <p>Iki platformda ayni locator kullaniliyorsa bu class'a gerek yoktur, locator dogrudan yazilir.
 */
public final class PlatformBy {

    private PlatformBy() {
    }

    /**
     * Iki platform locator'i da zorunludur. Bilinmeyen bir platform locator'i bos selector ile
     * doldurulup sessizce yanlis elemani secmesin diye null kabul edilmez.
     */
    public static By of(By android, By ios) {
        if (android == null || ios == null) {
            throw new IllegalArgumentException(
                    "Android ve iOS locator'larinin ikisi de verilmelidir. Platform locator'i "
                            + "henuz bilinmiyorsa PlatformBy kullanmayin, tahmini selector yazmayin.");
        }
        return MobilePlatform.current().isAndroid() ? android : ios;
    }
}
