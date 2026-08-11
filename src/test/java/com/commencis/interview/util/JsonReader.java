package com.commencis.interview.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/** Classpath altindaki JSON dosyasini request body olarak okur. */
public final class JsonReader {

    private JsonReader() {}

    /** Ornek kullanim: JsonReader.read("testdata/create-post.json") */
    public static String read(String classpathFile) {
        try (InputStream stream = JsonReader.class.getClassLoader().getResourceAsStream(classpathFile)) {
            if (stream == null) {
                throw new IllegalArgumentException("JSON dosyasi bulunamadi: " + classpathFile);
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("JSON dosyasi okunamadi: " + classpathFile, e);
        }
    }
}
