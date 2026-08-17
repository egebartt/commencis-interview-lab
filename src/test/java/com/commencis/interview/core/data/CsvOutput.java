package com.commencis.interview.core.data;

import com.commencis.interview.core.config.Config;
import io.restassured.response.Response;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Kosum ciktisini CSV olarak yazar.
 *
 * <p>Varsayilan dizin {@code target/output}; {@code -Dcsv.output.dir=<yol>} ile degistirilir.
 * {@code src/test/resources} altina asla yazilmaz: orasi derleme girdisidir, oraya yazmak hem
 * git'i kirletir hem {@code clean} ile temizlenmez.
 *
 * <p>Baslik satiri dosya bir kez olusturulurken yazilir. Ayni JVM icinde ayni dosyaya paralel
 * append guvenlidir (dosya yolu basina kilit); farkli JVM'lerden ayni dosyaya yazmak desteklenmez.
 */
public final class CsvOutput {

    private static final String OUTPUT_DIRECTORY_KEY = "csv.output.dir";
    private static final String DEFAULT_OUTPUT_DIRECTORY = "target/output";

    private static final ConcurrentHashMap<Path, Object> LOCKS = new ConcurrentHashMap<>();

    private CsvOutput() {
    }

    /** Yanittan istenen alanlari (JSON path) okuyup tek satir ekler. */
    public static Path append(String fileName, Response response, List<String> fields) {
        Map<String, String> row = new LinkedHashMap<>();
        for (String field : fields) {
            Object value = response.path(field);
            row.put(field, value == null ? "" : String.valueOf(value));
        }
        return append(fileName, row);
    }

    public static Path append(String fileName, Map<String, String> row) {
        Path target = resolve(fileName);
        Object lock = LOCKS.computeIfAbsent(target, path -> new Object());
        synchronized (lock) {
            try {
                Files.createDirectories(target.getParent());
                boolean writeHeader = Files.notExists(target) || Files.size(target) == 0;
                try (Writer writer = Files.newBufferedWriter(target, StandardCharsets.UTF_8,
                             StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                     CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT)) {
                    if (writeHeader) {
                        printer.printRecord(new ArrayList<Object>(row.keySet()));
                    }
                    printer.printRecord(new ArrayList<Object>(row.values()));
                }
                return target;
            } catch (IOException e) {
                throw new UncheckedIOException("CSV ciktisi yazilamadi: " + target, e);
            }
        }
    }

    public static Path outputDirectory() {
        String configured = Config.get(OUTPUT_DIRECTORY_KEY);
        return Path.of(configured.isEmpty() ? DEFAULT_OUTPUT_DIRECTORY : configured)
                .toAbsolutePath()
                .normalize();
    }

    /** Dosya adinin cikti dizini disina cikmadigini dogrular. */
    private static Path resolve(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("CSV cikti dosya adi bos olamaz.");
        }
        Path candidate = Path.of(fileName);
        if (candidate.isAbsolute()) {
            throw new IllegalArgumentException("CSV cikti dosya adi mutlak yol olamaz: " + fileName
                    + ". Dizini degistirmek icin -D" + OUTPUT_DIRECTORY_KEY + "=<yol> kullanin.");
        }
        Path directory = outputDirectory();
        Path target = directory.resolve(candidate).normalize();
        if (!target.startsWith(directory)) {
            throw new IllegalArgumentException("CSV ciktisi yalnizca " + directory
                    + " altina yazilabilir, istenen yol disarida: " + fileName);
        }
        return target;
    }
}
