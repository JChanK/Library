package com.example.library.dto;

import java.time.LocalDate;

public record LogTaskResponse(
        int taskId,
        LogTaskStatus status,
        LocalDate logDate
) {}