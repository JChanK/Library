package com.example.library.service;

import com.example.library.dto.LogTaskResponse;
import com.example.library.dto.LogTaskStatus;
import com.example.library.exception.ResourceNotFoundException;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
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
    private final Map<Integer, LogTaskResponse> tasks = new ConcurrentHashMap<>();
    private final AtomicInteger taskIdCounter = new AtomicInteger(0);

    public AsyncLogService(LogService logService) {
        this.logService = logService;
    }

    public int startAsyncProcessing(LocalDate date) {
        int taskId = taskIdCounter.incrementAndGet();
        tasks.put(taskId, new LogTaskResponse(
                taskId,
                LogTaskStatus.PROCESSING,
                date
        ));

        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(PROCESSING_DELAY_MS);

                ResponseEntity<Resource> response = logService.getLogFileByDate(date);

                if (response.getBody() instanceof LogService.AutoDeletingTempFileResource) {
                    tasks.put(taskId, new LogTaskResponse(
                            taskId,
                            LogTaskStatus.COMPLETED,
                            date
                    ));
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("Task {} was interrupted: {}", taskId, e.getMessage());
                tasks.put(taskId, new LogTaskResponse(
                        taskId,
                        LogTaskStatus.FAILED,
                        date
                ));
            } catch (Exception e) {
                logger.error("Error processing task {}: {}", taskId, e.getMessage());
                tasks.put(taskId, new LogTaskResponse(
                        taskId,
                        LogTaskStatus.FAILED,
                        date
                ));
            }
        });

        return taskId;
    }

    public LogTaskStatus getTaskStatus(int taskId) {
        LogTaskResponse task = tasks.get(taskId);
        if (task == null) {
            throw new ResourceNotFoundException("Task not found");
        }
        return task.status();
    }

    public ResponseEntity<Resource> getTaskResult(int taskId) throws IOException {
        LogTaskResponse task = tasks.get(taskId);
        if (task == null || task.status() != LogTaskStatus.COMPLETED) {
            throw new ResourceNotFoundException("Result not available");
        }
        return logService.getLogFileByDate(task.logDate());
    }
}