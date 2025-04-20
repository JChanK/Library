package com.example.library.service;

import com.example.library.dto.LogTaskResponse;
import com.example.library.dto.LogTaskStatus;
import com.example.library.exception.ResourceNotFoundException;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class AsyncLogService {
    private static final Logger logger = LoggerFactory.getLogger(AsyncLogService.class);
    private static final int PROCESSING_DELAY_MS = 10000;

    private final LogService logService;
    private final Map<UUID, LogTaskResponse> tasks = new ConcurrentHashMap<>();

    public AsyncLogService(LogService logService) {
        this.logService = logService;
    }

    public UUID startAsyncProcessing(LocalDate date, boolean isPerformanceLog) {
        UUID taskId = UUID.randomUUID();
        tasks.put(taskId, new LogTaskResponse(
                taskId,
                LogTaskStatus.PROCESSING,
                date,
                isPerformanceLog
        ));

        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(PROCESSING_DELAY_MS);

                ResponseEntity<Resource> response = isPerformanceLog
                        ? logService.getPerformanceLogsByDate(date)
                        : logService.getLogFileByDate(date);

                if (response.getBody() instanceof LogService.AutoDeletingTempFileResource) {
                    tasks.put(taskId, new LogTaskResponse(
                            taskId,
                            LogTaskStatus.COMPLETED,
                            date,
                            isPerformanceLog
                    ));
                } else {
                    logger.warn("Task {} did not return a valid resource", taskId);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                tasks.put(taskId, new LogTaskResponse(
                        taskId,
                        LogTaskStatus.FAILED,
                        date,
                        isPerformanceLog
                ));
            } catch (Exception e) {
                logger.warn("Task {} failed: {}", taskId, e.getMessage());
                tasks.put(taskId, new LogTaskResponse(
                        taskId,
                        LogTaskStatus.FAILED,
                        date,
                        isPerformanceLog
                ));
            }
        });

        return taskId;
    }

    public LogTaskStatus getTaskStatus(UUID taskId) {
        LogTaskResponse task = tasks.get(taskId);
        if (task == null) {
            throw new ResourceNotFoundException("Task not found");
        }
        return task.status();
    }

    public ResponseEntity<Resource> getTaskResult(UUID taskId) throws IOException {
        LogTaskResponse task = tasks.get(taskId);
        if (task == null || task.status() != LogTaskStatus.COMPLETED) {
            throw new ResourceNotFoundException("Result not available");
        }

        return task.isPerformanceLog()
                ?
                logService.getPerformanceLogsByDate(task.logDate()) :
                logService.getLogFileByDate(task.logDate());
    }
}