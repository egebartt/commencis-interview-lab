package com.commencis.interview.core.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/** Classpath altindaki JSON dosyasini request body olarak okur. */
public final class JsonData {

    private JsonData() {
    }

    /** Ornek kullanim: JsonData.read("testdata/json/create-post.json") */
    public static String read(String classpathFile) {
        try (InputStream stream = JsonData.class.getClassLoader().getResourceAsStream(classpathFile)) {
            if (stream == null) {
                throw new IllegalArgumentException("JSON dosyasi bulunamadi: src/test/resources/" + classpathFile);
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("JSON dosyasi okunamadi: " + classpathFile, e);
        }
    }
}
