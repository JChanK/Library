package com.example.library.controller;

import com.example.library.annotation.CountVisit;
import com.example.library.dto.LogTaskStatus;
import com.example.library.exception.ResourceNotFoundException;
import com.example.library.service.AsyncLogService;
import com.example.library.service.LogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/logs")
@Tag(name = "Log Controller", description = "API для работы с логами")
public class LogController {

    private final LogService logService;
    private final AsyncLogService asyncLogService;

    @Autowired
    public LogController(LogService logService, AsyncLogService asyncLogService) {
        this.logService = logService;
        this.asyncLogService = asyncLogService;
    }

    @GetMapping("/app")
    @CountVisit
    @Operation(
            summary = "Получить лог-файл приложения",
            description = "Возвращает лог-файл за указанную дату",
            responses = {   @ApiResponse(responseCode = "200",
                    description = "Лог-файл успешно получен"),
                            @ApiResponse(responseCode = "404",
                                    description = "Лог-файл не найден")
            }
    )
    public ResponseEntity<Resource> getAppLogFile(
            @RequestParam @Parameter(description = "Дата логов в формате YYYY-MM-DD")
            LocalDate date) throws IOException, ResourceNotFoundException {
        return logService.getLogFileByDate(date);
    }

    @GetMapping("/async/start")
    @CountVisit
    @Operation(summary = "Start async log processing")
    public ResponseEntity<Map<String, UUID>> startAsyncProcessing(
            @RequestParam LocalDate date,
            @RequestParam(required = false, defaultValue = "false") boolean isPerformanceLog) {

        UUID taskId = asyncLogService.startAsyncProcessing(date, isPerformanceLog);
        return ResponseEntity.ok(Collections.singletonMap("taskId", taskId));
    }

    @GetMapping("/async/status/{taskId}")
    @CountVisit
    @Operation(summary = "Get task status")
    public ResponseEntity<LogTaskStatus> getTaskStatus(
            @PathVariable UUID taskId) {

        return ResponseEntity.ok(asyncLogService.getTaskStatus(taskId));
    }

    @GetMapping("/async/result/{taskId}")
    @CountVisit
    @Operation(summary = "Get processed log file")
    public ResponseEntity<Resource> getTaskResult(
            @PathVariable UUID taskId) throws IOException {

        return asyncLogService.getTaskResult(taskId);
    }
}