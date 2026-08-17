package com.commencis.interview.frameworktest;

import com.commencis.interview.core.data.CsvData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** CSV okumanin tirnak/virgul/escape davranisini sabitler. */
@Tag("unit")
@DisplayName("CSV input")
class CsvDataTest {

    private static final String FILE = "testdata/csv/posts.csv";

    @Test
    @DisplayName("Baslik satiri veri olarak sayilmaz")
    void readsDataRowsWithoutHeader() {
        List<Map<String, String>> rows = CsvData.rows(FILE);

        assertEquals(3, rows.size());
        assertEquals(List.of("case", "userId", "title", "body"), List.copyOf(rows.get(0).keySet()));
    }

    @Test
    @DisplayName("Tirnak icindeki virgul kolonu bolmez")
    void quotedCommaStaysInOneColumn() {
        Map<String, String> row = CsvData.row(FILE, "case", "happy_path");

        assertEquals("Body with a comma, quoted so it stays one column", row.get("body"));
    }

    @Test
    @DisplayName("Escape edilmis tirnak degerin icinde kalir")
    void escapedQuotesAreRead() {
        Map<String, String> row = CsvData.row(FILE, "case", "long_title");

        assertEquals("Title that contains \"escaped quotes\" inside", row.get("title"));
    }

    @Test
    @DisplayName("Satir numarasi 1'den baslar")
    void selectsRowByNumber() {
        assertEquals("happy_path", CsvData.row(FILE, 1).get("case"));
    }

    @Test
    @DisplayName("Olmayan satir numarasi dosyadaki satir sayisini soyler")
    void unknownRowNumberIsExplained() {
        IllegalArgumentException error =
                assertThrows(IllegalArgumentException.class, () -> CsvData.row(FILE, 99));

        assertTrue(error.getMessage().contains("3 veri satiri"), error.getMessage());
    }

    @Test
    @DisplayName("Olmayan kolon mevcut kolonlari listeler")
    void unknownColumnListsAvailableColumns() {
        IllegalArgumentException error =
                assertThrows(IllegalArgumentException.class, () -> CsvData.row(FILE, "nope", "x"));

        assertTrue(error.getMessage().contains("title"), error.getMessage());
    }

    @Test
    @DisplayName("Eslesmeyen deger hata verir")
    void unknownValueFails() {
        assertThrows(IllegalArgumentException.class, () -> CsvData.row(FILE, "case", "no_such_case"));
    }

    @Test
    @DisplayName("Olmayan dosya aranan yolu soyler")
    void missingFileIsExplained() {
        IllegalArgumentException error =
                assertThrows(IllegalArgumentException.class, () -> CsvData.rows("testdata/csv/none.csv"));

        assertTrue(error.getMessage().contains("src/test/resources/testdata/csv/none.csv"), error.getMessage());
    }

    @Test
    @DisplayName("JSON'a cevirirken sayi, bos ve metin ayirt edilir")
    void toJsonInfersValueTypes() {
        String json = CsvData.rowAsJson(FILE, "case", "missing_title");

        assertTrue(json.contains("\"userId\":7"), json);
        assertTrue(json.contains("\"title\":null"), json);
        assertTrue(json.contains("\"body\":\"Body without a title\""), json);
    }
}
