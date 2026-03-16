package com.Guess.Sketch.guess_and_sketch_server.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }

    @GetMapping("/")
    public Map<String, String> root() {
        return Map.of(
                "service", "guess-and-sketch-server",
                "status", "running"
        );
    }
}
