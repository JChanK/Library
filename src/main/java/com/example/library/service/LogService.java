package com.example.library.service;

import com.example.library.exception.LogProcessingException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class LogService {
    private static final Logger logger = LoggerFactory.getLogger(LogService.class);
    private static final String LOG_FILE_PATH = "./logs/library-app.log";
    private static final String PERFORMANCE_FILE_PATH = "./logs/performance.log";
    private static final String TEMP_DIR_NAME = "library-temp-logs";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public ResponseEntity<Resource> getLogFileByDate(LocalDate date) throws IOException {
        Path path = Paths.get(LOG_FILE_PATH);
        if (!Files.exists(path)) {
            logger.warn("Log file not found at path: {}", LOG_FILE_PATH);
            return ResponseEntity.notFound().build();
        }

        String dateString = date.format(DATE_FORMAT);
        List<String> filteredLines = filterLinesByDate(path, dateString);

        if (filteredLines.isEmpty()) {
            logger.info("No log entries found for date: {}", dateString);
            return ResponseEntity.notFound().build();
        }

        return createTempFileResponse(filteredLines, "logs-" + dateString + ".log");
    }

    public ResponseEntity<Resource> getPerformanceLogsByDate(LocalDate date) throws IOException {
        Path path = Paths.get(PERFORMANCE_FILE_PATH);
        if (!Files.exists(path)) {
            logger.warn("Performance log file not found at path: {}", PERFORMANCE_FILE_PATH);
            return ResponseEntity.notFound().build();
        }

        String dateString = date.format(DATE_FORMAT);
        List<String> filteredLines = filterLinesByDate(path, dateString);

        if (filteredLines.isEmpty()) {
            logger.info("No performance log entries found for date: {}", dateString);
            return ResponseEntity.notFound().build();
        }

        return createTempFileResponse(filteredLines, "performance-" + dateString + ".log");
    }

    private static List<String> filterLinesByDate(Path path, String dateString) throws IOException {
        return Files.lines(path, StandardCharsets.UTF_8)
                .filter(line -> line.startsWith(dateString))
                .collect(Collectors.toList());
    }

    private ResponseEntity<Resource> createTempFileResponse(List<String> lines, String filename)
            throws IOException {
        Path tempDir = createSecureTempDirectory();
        Path tempFile = createSecureTempFile(tempDir, filename);

        try {
            Files.write(tempFile, lines, StandardCharsets.UTF_8);
            Resource resource = new AutoDeletingTempFileResource(tempFile);

            return ResponseEntity.ok()
                    .header("Content-Type", "text/plain")
                    .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                    .body(resource);
        } catch (IOException e) {
            try {
                Files.deleteIfExists(tempFile);
            } catch (IOException deleteEx) {
                logger.error("Failed to delete temp file after error: {}", tempFile, deleteEx);
            }
            throw new LogProcessingException("Failed to create temp file response", e);
        }
    }

    private static Path createSecureTempDirectory() throws IOException {
        Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"), TEMP_DIR_NAME);

        if (!Files.exists(tempDir)) {
            Files.createDirectories(tempDir);
            try {
                Set<PosixFilePermission> perms = PosixFilePermissions.fromString("rwx------");
                Files.setPosixFilePermissions(tempDir, perms);
            } catch (UnsupportedOperationException e) {
                logger.debug("POSIX permissions not supported on this filesystem");
            }
        }
        return tempDir;
    }

    private static Path createSecureTempFile(Path directory, String filename) throws IOException {
        String safeFilename = filename.replaceAll("[^a-zA-Z0-9.-]", "_");

        try {
            Set<PosixFilePermission> perms = PosixFilePermissions.fromString("rw-------");
            FileAttribute<Set<PosixFilePermission>> attr =
                    PosixFilePermissions.asFileAttribute(perms);
            return Files.createTempFile(directory, safeFilename.replace(".log", ""), ".log", attr);
        } catch (UnsupportedOperationException e) {
            return Files.createTempFile(directory, safeFilename.replace(".log", ""), ".log");
        }
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o != null && getClass() == o.getClass());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    private static class AutoDeletingTempFileResource extends InputStreamResource {
        private final Path filePath;
        private final InputStream inputStream;

        public AutoDeletingTempFileResource(Path filePath) throws IOException {
            super(Files.newInputStream(filePath));
            this.filePath = filePath;
            this.inputStream = Files.newInputStream(filePath);
        }

        @Override
        public InputStream getInputStream() {
            return inputStream;
        }

        @Override
        public String getFilename() {
            return filePath.getFileName().toString();
        }

        @Override
        public long contentLength() throws IOException {
            return Files.size(filePath);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            if (!super.equals(o)) {
                return false;
            }
            AutoDeletingTempFileResource that = (AutoDeletingTempFileResource) o;
            return Objects.equals(filePath, that.filePath);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), filePath);
        }

        public void close() throws IOException {
            try {
                inputStream.close();
            } finally {
                try {
                    Files.deleteIfExists(filePath);
                } catch (IOException e) {
                    logger.error("Failed to delete temp file: {}", filePath, e);
                }
            }
        }
    }
}