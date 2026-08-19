package com.commencis.interview.core;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Iki islem: bir tabloyu CSV'ye yaz, geri oku. Neyin yazildigini bilmez; kolon adlari ilk
 * satirin anahtarlarindan gelir, yani her tur veri ayni iki metotla tasinir.
 *
 * <p>Amaci veri devretmek: bir testin urettigi deger (API'den donen kayit gibi) dosyaya
 * yazilir, baska bir test veya kosum ayni dosyadan okur. Elde hazir bir CSV varsa veri odakli
 * kosum icin de ayni {@code read} kullanilir.
 *
 * <p>Ayrac duz virguldur; tirnak ve kacis yoktur. Deger virgul icerirse dosya sessizce
 * bozulmaz, hata verir.
 */
public final class Csv {

    private static final String SEPARATOR = ",";

    private Csv() {
    }

    /** Kolon adlari ilk satirin anahtarlarindan alinir. Dosya varsa uzerine yazilir. */
    public static void write(Path file, List<Map<String, String>> rows) {
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("Yazilacak satir yok: " + file);
        }
        List<String> headers = new ArrayList<>(rows.get(0).keySet());
        List<String> lines = new ArrayList<>();
        lines.add(join(headers, file));

        for (Map<String, String> row : rows) {
            List<String> values = new ArrayList<>();
            for (String header : headers) {
                String value = row.get(header);
                if (value == null) {
                    throw new IllegalArgumentException("'" + header + "' alani bu satirda yok: " + file);
                }
                values.add(value);
            }
            lines.add(join(values, file));
        }

        try {
            Path parent = file.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("CSV yazilamadi: " + file, e);
        }
    }

    /** Ilk satir baslik kabul edilir; her veri satiri baslik-deger eslesmesi olarak doner. */
    public static List<Map<String, String>> read(Path file) {
        List<String> lines;
        try {
            lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("CSV okunamadi: " + file, e);
        }

        List<String> filled = lines.stream().filter(line -> !line.isBlank()).toList();
        if (filled.size() < 2) {
            throw new IllegalStateException("CSV bos veya yalnizca baslik satiri var: " + file);
        }

        String[] headers = filled.get(0).split(SEPARATOR, -1);
        List<Map<String, String>> rows = new ArrayList<>();
        for (int line = 1; line < filled.size(); line++) {
            String[] values = filled.get(line).split(SEPARATOR, -1);
            if (values.length != headers.length) {
                throw new IllegalStateException(file + " satir " + (line + 1) + ": baslik sayisi "
                        + headers.length + ", deger sayisi " + values.length);
            }
            Map<String, String> row = new LinkedHashMap<>();
            for (int column = 0; column < headers.length; column++) {
                row.put(headers[column].trim(), values[column].trim());
            }
            rows.add(row);
        }
        return rows;
    }

    private static String join(List<String> cells, Path file) {
        for (String cell : cells) {
            if (cell.contains(SEPARATOR)) {
                throw new IllegalArgumentException("Deger virgul iceriyor, bu basit CSV yazimi yetmez: '"
                        + cell + "' (" + file + ")");
            }
        }
        return String.join(SEPARATOR, cells);
    }
}
