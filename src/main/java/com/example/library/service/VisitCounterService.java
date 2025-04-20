package com.example.library.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Service;

@Service
public class VisitCounterService {
    private final Map<String, AtomicInteger> urlCounterMap = new ConcurrentHashMap<>();

    public void incrementCounter(String url) {
        urlCounterMap.computeIfAbsent(url, k -> new AtomicInteger(0)).incrementAndGet();
    }

    public int getCounter(String url) {
        return urlCounterMap.getOrDefault(url, new AtomicInteger(0)).get();
    }

    public Map<String, Integer> getAllCounters() {
        Map<String, Integer> result = new ConcurrentHashMap<>();
        urlCounterMap.forEach((url, counter) -> result.put(url, counter.get()));
        return result;
    }
}