package com.example.library.controller;

import com.example.library.service.LogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/logs")
@Tag(name = "Log Controller", description = "API для работы с логами")
public class LogController {

    private final LogService logService;

    @Autowired
    public LogController(LogService logService) {
        this.logService = logService;
    }

    @GetMapping("/app")
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
            LocalDate date) {
        try {
            return logService.getLogFileByDate(date);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/performance")
    @Operation(
            summary = "Получить логи производительности",
            description = "Возвращает логи производительности за указанную дату",
            responses = {   @ApiResponse(responseCode = "200",
                    description = "Логи успешно получены"),
                            @ApiResponse(responseCode = "404",
                                    description = "Логи не найдены")
            }
    )
    public ResponseEntity<Resource> getPerformanceLogs(
            @RequestParam @Parameter(description = "Дата логов в формате YYYY-MM-DD")
            LocalDate date) {
        try {
            return logService.getPerformanceLogsByDate(date);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}