package com.example.db2accounting.controller;

import com.example.db2accounting.service.DemoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class DemoController {

    private final DemoService demoService;

    public DemoController(DemoService demoService) {
        this.demoService = demoService;
    }

    @GetMapping("/client-info")
    public Map<String, String> getClientInfo() {
        return demoService.getCurrentClientInfo();
    }

    @GetMapping("/demo")
    public Map<String, Object> demo() {
        return Map.of("data", demoService.fetchSomeData());
    }

    @GetMapping("/performance-test")
    public Map<String, Object> performanceTest(@RequestParam(defaultValue = "100") int iterations) {
        return Map.of(
            "iterations", iterations,
            "durationMs", demoService.performanceTest(iterations)
        );
    }

    @GetMapping("/hold-lock")
    public Map<String, Object> holdLock(@RequestParam(defaultValue = "10") int seconds) throws Exception {
        long start = System.currentTimeMillis();
        demoService.holdLock(seconds);
        return Map.of(
            "holdSeconds", seconds,
            "actualMs", System.currentTimeMillis() - start
        );
    }
}
