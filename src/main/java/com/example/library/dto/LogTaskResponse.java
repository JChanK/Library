package com.example.library.dto;

import java.time.LocalDate;
import java.util.UUID;

public record LogTaskResponse(
        UUID taskId,
        LogTaskStatus status,
        LocalDate logDate,
        boolean isPerformanceLog
) {}