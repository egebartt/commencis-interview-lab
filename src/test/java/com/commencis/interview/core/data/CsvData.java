package com.commencis.interview.core.data;

import com.google.gson.JsonObject;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Classpath altindaki CSV test verisini okur ({@code src/test/resources/testdata/csv/...}).
 *
 * <p>Ayrıştırma Apache Commons CSV ile yapilir: tirnak icindeki virgul, gomulu satir sonu ve
 * escape edilmis tirnak dogru okunur.
 *
 * <p>Sinir: CSV duz bir tablodur. Ic ice nesne veya dizi iceren govdeler icin JSON dosyasi
 * ({@link JsonData}) veya feature icindeki DocString kullanilir.
 */
public final class CsvData {

    private static final Pattern INTEGER = Pattern.compile("-?\\d+");
    private static final Pattern DECIMAL = Pattern.compile("-?\\d*\\.\\d+");

    private static final CSVFormat FORMAT = CSVFormat.DEFAULT.builder()
            .setHeader()
            .setSkipHeaderRecord(true)
            .setIgnoreEmptyLines(true)
            .setTrim(true)
            .build();

    private CsvData() {
    }

    /** Dosyadaki tum satirlari kolon adi -> deger olarak dondurur. */
    public static List<Map<String, String>> rows(String classpathFile) {
        try (InputStream stream = CsvData.class.getClassLoader().getResourceAsStream(classpathFile)) {
            if (stream == null) {
                throw new IllegalArgumentException("CSV dosyasi bulunamadi: src/test/resources/" + classpathFile);
            }
            try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
                 CSVParser parser = CSVParser.parse(reader, FORMAT)) {
                List<Map<String, String>> rows = new ArrayList<>();
                for (CSVRecord record : parser) {
                    rows.add(new LinkedHashMap<>(record.toMap()));
                }
                return rows;
            }
        } catch (IOException e) {
            throw new UncheckedIOException("CSV dosyasi okunamadi: " + classpathFile, e);
        }
    }

    /** Baslik satiri sayilmaz: {@code row(file, 1)} ilk veri satiridir. */
    public static Map<String, String> row(String classpathFile, int rowNumber) {
        List<Map<String, String>> rows = rows(classpathFile);
        if (rowNumber < 1 || rowNumber > rows.size()) {
            throw new IllegalArgumentException(classpathFile + " icinde " + rowNumber
                    + ". veri satiri yok; dosyada " + rows.size() + " veri satiri var.");
        }
        return rows.get(rowNumber - 1);
    }

    public static Map<String, String> row(String classpathFile, String column, String value) {
        List<Map<String, String>> rows = rows(classpathFile);
        if (!rows.isEmpty() && !rows.get(0).containsKey(column)) {
            throw new IllegalArgumentException(classpathFile + " icinde '" + column + "' kolonu yok. "
                    + "Mevcut kolonlar: " + rows.get(0).keySet());
        }
        return rows.stream()
                .filter(row -> value.equals(row.get(column)))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(classpathFile + " icinde '" + column
                        + "' = '" + value + "' olan satir yok."));
    }

    public static String rowAsJson(String classpathFile, int rowNumber) {
        return toJson(row(classpathFile, rowNumber));
    }

    public static String rowAsJson(String classpathFile, String column, String value) {
        return toJson(row(classpathFile, column, value));
    }

    /**
     * Duz bir satiri JSON govdeye cevirir.
     *
     * <p>CSV'de her deger metindir; sayi, boolean ve null gorunumlu degerler tipli yazilir,
     * digerleri string kalir. Sayi gibi gorunen bir degerin string gonderilmesi gerekiyorsa
     * CSV degil JSON dosyasi/DocString kullanilir.
     */
    public static String toJson(Map<String, String> row) {
        JsonObject json = new JsonObject();
        row.forEach((column, value) -> {
            if (value == null || value.isEmpty() || "null".equalsIgnoreCase(value)) {
                json.add(column, com.google.gson.JsonNull.INSTANCE);
            } else if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
                json.addProperty(column, Boolean.parseBoolean(value.toLowerCase(Locale.ROOT)));
            } else if (INTEGER.matcher(value).matches()) {
                json.addProperty(column, Long.valueOf(value));
            } else if (DECIMAL.matcher(value).matches()) {
                json.addProperty(column, Double.valueOf(value));
            } else {
                json.addProperty(column, value);
            }
        });
        return json.toString();
    }
}
