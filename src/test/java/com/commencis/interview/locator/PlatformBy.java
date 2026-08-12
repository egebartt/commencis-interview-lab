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

    /**
     * Aktif platformun locator'ini secer. Pasif platformun locator'i henuz bilinmiyorsa null
     * olabilir; aktif platform icin null verilirse acik hata uretilir.
     */
    public static By of(By android, By ios) {
        MobilePlatform platform = MobilePlatform.current();
        By selected = platform.isAndroid() ? android : ios;

        if (selected == null) {
            throw new UnsupportedOperationException(platform + " platformu icin locator henuz tanimlanmadi.");
        }
        return selected;
    }
}
