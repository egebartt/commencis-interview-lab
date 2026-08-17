package com.commencis.interview.frameworktest;

import com.commencis.interview.core.data.CsvOutput;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** CSV ciktisinin basligi, escaping'i ve yazma sinirini sabitler. */
@Tag("unit")
@DisplayName("CSV output")
class CsvOutputTest {

    private static String uniqueFileName() {
        return "frameworktest-" + UUID.randomUUID() + ".csv";
    }

    @Test
    @DisplayName("Baslik bir kez yazilir, sonraki satirlar eklenir")
    void writesHeaderOnceAndAppendsRows() throws IOException {
        String file = uniqueFileName();

        CsvOutput.append(file, Map.of("id", "1"));
        Path path = CsvOutput.append(file, Map.of("id", "2"));

        assertEquals(List.of("id", "1", "2"), Files.readAllLines(path, StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("Virgul ve tirnak iceren degerler escape edilir")
    void escapesCommasAndQuotes() throws IOException {
        Map<String, String> row = new LinkedHashMap<>();
        row.put("title", "a, b");
        row.put("note", "say \"hi\"");

        Path path = CsvOutput.append(uniqueFileName(), row);

        String content = Files.readString(path, StandardCharsets.UTF_8);
        assertTrue(content.contains("\"a, b\""), content);
        assertTrue(content.contains("\"say \"\"hi\"\"\""), content);
    }

    @Test
    @DisplayName("Cikti varsayilan olarak target/output altina yazilir")
    void writesUnderOutputDirectory() {
        Path path = CsvOutput.append(uniqueFileName(), Map.of("id", "1"));

        assertTrue(path.startsWith(CsvOutput.outputDirectory()), path.toString());
        assertTrue(path.toString().replace('\\', '/').contains("/target/output/"), path.toString());
    }

    @Test
    @DisplayName("Cikti dizininin disina yazilamaz")
    void rejectsPathTraversal() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> CsvOutput.append("../../src/test/resources/testdata/csv/posts.csv", Map.of("id", "1")));

        assertTrue(error.getMessage().contains("yalnizca"), error.getMessage());
    }

    @Test
    @DisplayName("Mutlak yol reddedilir")
    void rejectsAbsolutePath() {
        String absolute = Path.of("target", "escape.csv").toAbsolutePath().toString();

        assertThrows(IllegalArgumentException.class, () -> CsvOutput.append(absolute, Map.of("id", "1")));
    }

    @Test
    @DisplayName("Bos dosya adi reddedilir")
    void rejectsBlankFileName() {
        assertThrows(IllegalArgumentException.class, () -> CsvOutput.append("  ", Map.of("id", "1")));
    }
}
