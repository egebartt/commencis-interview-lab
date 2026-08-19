package com.commencis.frameworktest;

import com.commencis.core.TestContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Yer tutucu cozumu. Sessizce cozulmeyen bir yer tutucu, istegin yanlis govdeyle gitmesi
 * demektir; bu yuzden "cozemedim" durumu hata firlatmali.
 */
@DisplayName("TestContext")
class TestContextTest {

    @Test
    @DisplayName("${ctx:...} onceki adimda kaydedilen degeri koyar")
    void resolvesContextValue() {
        TestContext context = new TestContext();
        context.put("postId", 101);

        assertEquals("/posts/101", context.resolve("/posts/${ctx:postId}"));
    }

    @Test
    @DisplayName("${config:...} ayar dosyasindaki degeri koyar")
    void resolvesConfigValue() {
        TestContext context = new TestContext();

        assertEquals("android", context.resolve("${config:platform}"));
    }

    @Test
    @DisplayName("Yer tutucu yoksa metin degismez")
    void leavesPlainTextUntouched() {
        TestContext context = new TestContext();

        assertEquals("/posts/1", context.resolve("/posts/1"));
    }

    @Test
    @DisplayName("Tabloda hem anahtar hem deger cozulur")
    void resolvesTableValues() {
        TestContext context = new TestContext();
        context.put("requestId", "abc-123");

        Map<String, String> table = new LinkedHashMap<>();
        table.put("X-Request-Id", "${ctx:requestId}");

        assertEquals("abc-123", context.resolve(table).get("X-Request-Id"));
    }

    @Test
    @DisplayName("Kaydedilmemis anahtar mevcut anahtarlari gosterir")
    void unknownContextKeyListsAvailableKeys() {
        TestContext context = new TestContext();
        context.put("postId", 1);

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> context.resolve("${ctx:userId}"));

        assertTrue(error.getMessage().contains("userId"), error.getMessage());
        assertTrue(error.getMessage().contains("postId"), error.getMessage());
    }

    @Test
    @DisplayName("Desteklenmeyen yer tutucu sessizce gecmez")
    void unsupportedPlaceholderFails() {
        TestContext context = new TestContext();

        assertThrows(IllegalArgumentException.class, () -> context.resolve("${env:HOME}"));
        assertThrows(IllegalArgumentException.class, () -> context.resolve("${postId}"));
    }
}
