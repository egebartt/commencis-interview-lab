package com.commencis.interview.frameworktest;

import com.commencis.interview.core.context.ScenarioContext;
import com.commencis.interview.core.data.Placeholders;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("unit")
@DisplayName("Placeholder resolution")
class PlaceholdersTest {

    private final ScenarioContext context = new ScenarioContext();
    private final Placeholders placeholders = new Placeholders(context);

    @Test
    @DisplayName("${ctx:...} context'teki degeri koyar")
    void resolvesContextValue() {
        context.put("postId", 101);

        assertEquals("/posts/101", placeholders.resolve("/posts/${ctx:postId}"));
    }

    @Test
    @DisplayName("${config:...} aktif ayari koyar")
    void resolvesConfigValue() {
        assertEquals("https://jsonplaceholder.typicode.com",
                placeholders.resolve("${config:api.base.url}"));
    }

    @Test
    @DisplayName("Bir metindeki birden fazla token cozulur")
    void resolvesMultipleTokens() {
        context.put("id", 7);
        context.put("name", "commencis");

        assertEquals("7-commencis", placeholders.resolve("${ctx:id}-${ctx:name}"));
    }

    @Test
    @DisplayName("Token icermeyen metin degismez")
    void leavesPlainTextUntouched() {
        assertEquals("/posts/1", placeholders.resolve("/posts/1"));
    }

    @Test
    @DisplayName("Tablo anahtar ve degerleri birlikte cozulur")
    void resolvesMapKeysAndValues() {
        context.put("headerName", "X-Request-Id");
        context.put("headerValue", "abc");
        Map<String, String> table = new LinkedHashMap<>();
        table.put("${ctx:headerName}", "${ctx:headerValue}");

        assertEquals(Map.of("X-Request-Id", "abc"), placeholders.resolve(table));
    }

    @Test
    @DisplayName("Context'te olmayan anahtar sessizce bos gecilmez")
    void missingContextKeyFails() {
        IllegalStateException error =
                assertThrows(IllegalStateException.class, () -> placeholders.resolve("${ctx:missing}"));

        assertTrue(error.getMessage().contains("missing"), error.getMessage());
    }

    @Test
    @DisplayName("Bos ayar degeri hata verir")
    void blankConfigValueFails() {
        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> placeholders.resolve("${config:there.is.no.such.key}"));

        assertTrue(error.getMessage().contains("there.is.no.such.key"), error.getMessage());
    }

    @Test
    @DisplayName("Bilinmeyen kaynak reddedilir")
    void unknownSourceFails() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> placeholders.resolve("${random:email}"));

        assertTrue(error.getMessage().contains("ctx, config"), error.getMessage());
    }

    @Test
    @DisplayName("Kaynak belirtilmemis token reddedilir")
    void tokenWithoutSourceFails() {
        assertThrows(IllegalArgumentException.class, () -> placeholders.resolve("${postId}"));
    }
}
