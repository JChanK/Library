package com.example.library.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
        try {
            Files.write(tempFile, lines, StandardCharsets.UTF_8);

            Resource resource = new TempFileResource(tempFile);
            return ResponseEntity.ok()
                    .header("Content-Type", "text/plain")
                    .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                    .body(resource);
        } catch (IOException e) {
            Files.deleteIfExists(tempFile);
            throw e;
        }
    }

    private static class TempFileResource extends org.springframework.core.io.AbstractResource {
        private final Path path;

        public TempFileResource(Path path) {
            this.path = path;
        }

        @Override
        public String getDescription() {
            return "Temporary file resource [" + path + "]";
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return new FileInputStream(path.toFile()) {
                @Override
                public void close() throws IOException {
                    super.close();
                    Files.deleteIfExists(path);
                }
            };
        }

        @Override
        public boolean exists() {
            return Files.exists(path);
        }

        @Override
        public long contentLength() throws IOException {
            return Files.size(path);
        }
    }
}

