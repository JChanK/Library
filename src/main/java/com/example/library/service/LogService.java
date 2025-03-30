package com.example.library.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class LogService {
    private static final String LOG_FILE_PATH = "./logs/library-app.log";
    private static final String PERFORMANCE_FILE_PATH = "./logs/performance.log";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter
            .ofPattern("yyyy-MM-dd");

    public ResponseEntity<Resource> getLogFileByDate(LocalDate date) throws IOException {
        Path path = Paths.get(LOG_FILE_PATH);
        if (!Files.exists(path)) {
            return ResponseEntity.notFound().build();
        }

        String dateString = date.format(DATE_FORMAT);
        List<String> filteredLines = filterLinesByDate(path, dateString);

        if (filteredLines.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return createTempFileResponse(filteredLines, "logs-" + dateString + ".log");
    }

    public ResponseEntity<Resource> getPerformanceLogsByDate(LocalDate date)
            throws IOException {
        Path path = Paths.get(PERFORMANCE_FILE_PATH);
        if (!Files.exists(path)) {
            return ResponseEntity.notFound().build();
        }

        String dateString = date.format(DATE_FORMAT);
        List<String> filteredLines = filterLinesByDate(path, dateString);

        if (filteredLines.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return createTempFileResponse(filteredLines, "performance-"
                + dateString + ".log");
    }

    private List<String> filterLinesByDate(Path path, String dateString) throws IOException {
        return Files.lines(path, StandardCharsets.UTF_8)
                .filter(line -> line.startsWith(dateString))
                .collect(Collectors.toList());
    }

    private ResponseEntity<Resource> createTempFileResponse(List<String> lines,
                                                            String filename) throws IOException {
        Path tempFile = Files.createTempFile(filename.replace(".log", ""), ".log");
        Files.write(tempFile, lines, StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header("Content-Type", "text/plain")
                .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                .body(new org.springframework.core.io.UrlResource(tempFile.toUri()));
    }
}