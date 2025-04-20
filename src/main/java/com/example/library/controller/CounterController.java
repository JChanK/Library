package com.example.library.controller;

import com.example.library.annotation.CountVisit;
import com.example.library.service.VisitCounterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/counter")
public class CounterController {
    private final VisitCounterService visitCounterService;

    public CounterController(VisitCounterService visitCounterService) {
        this.visitCounterService = visitCounterService;
    }

    @GetMapping("/count")
    @CountVisit
    @Operation(summary = "Получить счетчик посещений")
    public ResponseEntity<Integer> getVisitCount(
            @RequestParam @Parameter(description = "URL для проверки") String url) {
        return ResponseEntity.ok(visitCounterService.getCounter(url));
    }

    @GetMapping("/all")
    @CountVisit
    @Operation(summary = "Получить все счетчики")
    public ResponseEntity<Map<String, Integer>> getAllCounters() {
        return ResponseEntity.ok(visitCounterService.getAllCounters());
    }
}