package com.example.library.service;

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
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.core.io.Resource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class LogService {
    private static final String LOG_FILE_PATH = "./logs/library-app.log";
    private static final String PERFORMANCE_FILE_PATH = "./logs/performance.log";
    private static final String TEMP_DIR_NAME = "library-temp-logs";
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

    public ResponseEntity<Resource> getPerformanceLogsByDate(LocalDate date) throws IOException {
        Path path = Paths.get(PERFORMANCE_FILE_PATH);
        if (!Files.exists(path)) {
            return ResponseEntity.notFound().build();
        }

        String dateString = date.format(DATE_FORMAT);
        List<String> filteredLines = filterLinesByDate(path, dateString);

        if (filteredLines.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return createTempFileResponse(filteredLines, "performance-" + dateString + ".log");
    }

    private List<String> filterLinesByDate(Path path, String dateString) throws IOException {
        return Files.lines(path, StandardCharsets.UTF_8)
                .filter(line -> line.startsWith(dateString))
                .collect(Collectors.toList());
    }

    private ResponseEntity<Resource> createTempFileResponse(List<String> lines, String filename) throws IOException {
        // Создаем безопасную временную директорию
        Path tempDir = createSecureTempDirectory();

        // Создаем временный файл с безопасными правами
        Path tempFile = createSecureTempFile(tempDir, filename);

        try {
            // Записываем данные в файл
            Files.write(tempFile, lines, StandardCharsets.UTF_8);

            // Создаем ресурс с автоматическим удалением файла после использования
            Resource resource = new AutoDeletingTempFileResource(tempFile);

            return ResponseEntity.ok()
                    .header("Content-Type", "text/plain")
                    .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                    .body(resource);
        } catch (IOException e) {
            // В случае ошибки удаляем файл
            Files.deleteIfExists(tempFile);
            throw e;
        }
    }

    private Path createSecureTempDirectory() throws IOException {
        Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"), TEMP_DIR_NAME);

        // Создаем директорию, если она не существует
        if (!Files.exists(tempDir)) {
            Files.createDirectories(tempDir);

            // Устанавливаем безопасные права (если поддерживается)
            try {
                Set<PosixFilePermission> perms = PosixFilePermissions.fromString("rwx------");
                Files.setPosixFilePermissions(tempDir, perms);
            } catch (UnsupportedOperationException e) {
                // Игнорируем, если файловая система не поддерживает POSIX
            }
        }
        return tempDir;
    }

    private Path createSecureTempFile(Path directory, String filename) throws IOException {
        // Очищаем имя файла от потенциально опасных символов
        String safeFilename = filename.replaceAll("[^a-zA-Z0-9.-]", "_");

        try {
            // Пытаемся создать файл с POSIX правами (для Unix-систем)
            Set<PosixFilePermission> perms = PosixFilePermissions.fromString("rw-------");
            FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions.asFileAttribute(perms);
            return Files.createTempFile(directory, safeFilename.replace(".log", ""), ".log", attr);
        } catch (UnsupportedOperationException e) {
            // Если POSIX не поддерживается (например, Windows), создаем без атрибутов
            return Files.createTempFile(directory, safeFilename.replace(".log", ""), ".log");
        }
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
        public InputStream getInputStream() throws IOException {
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

        public void close() throws IOException {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } finally {
                try {
                    Files.deleteIfExists(filePath);
                } catch (IOException e) {
                    System.err.println("Failed to delete temp file: " + filePath);
                    // Можно добавить логирование через logger вместо System.err
                }
            }
        }
    }
}