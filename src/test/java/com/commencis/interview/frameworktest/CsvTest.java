package com.commencis.interview.frameworktest;

import com.commencis.interview.core.Csv;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link Csv} yaz/oku turu ve hata durumlari. Cihaz veya ag gerektirmez; dosyalar gecici
 * dizine yazilir, testdata klasoru kirlenmez.
 */
@DisplayName("CSV veri dosyasi")
class CsvTest {

    @TempDir
    Path directory;

    private static Map<String, String> row(String key1, String value1, String key2, String value2) {
        Map<String, String> row = new LinkedHashMap<>();
        row.put(key1, value1);
        row.put(key2, value2);
        return row;
    }

    @Test
    @DisplayName("Yazilan satirlar ayni sirayla geri okunur")
    void writesAndReadsBackEveryRow() {
        Path file = directory.resolve("data.csv");
        List<Map<String, String>> rows = List.of(
                row("username", "user_1", "password", "Pa55w0rd"),
                row("username", "user_2", "password", "S3cret"));

        Csv.write(file, rows);

        assertEquals(rows, Csv.read(file));
    }

    @Test
    @DisplayName("Kolon adlari veriden gelir, sabit degildir")
    void columnNamesComeFromTheData() {
        Path file = directory.resolve("orders.csv");

        Csv.write(file, List.of(row("orderId", "42", "amount", "199.90")));

        Map<String, String> saved = Csv.read(file).get(0);
        assertEquals("42", saved.get("orderId"));
        assertEquals("199.90", saved.get("amount"));
    }

    @Test
    @DisplayName("Ust dizin yoksa olusturulur")
    void createsMissingParentDirectory() {
        Path file = directory.resolve("yeni/klasor/data.csv");

        Csv.write(file, List.of(Map.of("id", "42")));

        assertEquals("42", Csv.read(file).get(0).get("id"));
    }

    @Test
    @DisplayName("Virgul iceren deger sessizce bozulmaz, hata verir")
    void rejectsValueContainingSeparator() {
        Path file = directory.resolve("bozuk.csv");

        assertThrows(IllegalArgumentException.class,
                () -> Csv.write(file, List.of(Map.of("adres", "Katar Cad., Sariyer"))));
    }

    @Test
    @DisplayName("Yalnizca baslik satiri varsa okuma hata verir")
    void failsWhenDataRowIsMissing() throws IOException {
        Path file = directory.resolve("yalniz-baslik.csv");
        Files.writeString(file, "username,password" + System.lineSeparator(), StandardCharsets.UTF_8);

        assertThrows(IllegalStateException.class, () -> Csv.read(file));
    }

    @Test
    @DisplayName("Baslik ve deger sayisi tutmuyorsa okuma hata verir")
    void failsWhenColumnCountsDiffer() throws IOException {
        Path file = directory.resolve("eksik-kolon.csv");
        Files.writeString(file, String.join(System.lineSeparator(),
                "username,password", "sadece-kullanici") + System.lineSeparator(), StandardCharsets.UTF_8);

        assertThrows(IllegalStateException.class, () -> Csv.read(file));
    }
}
