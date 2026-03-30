package com.Guess.Sketch.guess_and_sketch_server.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimiterService {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public Bucket resolveBucket(String key) {
        return buckets.computeIfAbsent(key, this::createBucket);
    }

    private Bucket createBucket(String key) {
        return Bucket.builder()
                .addLimit(getBandwidth(key))
                .build();
    }

    private Bandwidth getBandwidth(String key) {

        if (key == null) {
            return defaultLimit();
        }

        // Extract endpoint (simple approach)
        String endpoint = extractEndpoint(key);

        switch (endpoint) {
            case "/guess":
            case "/chat":
                return Bandwidth.simple(5, Duration.ofSeconds(5));

            case "/joinRoom":
            case "/createRoom":
                return Bandwidth.simple(1, Duration.ofSeconds(5));

            case "/draw":
                return Bandwidth.simple(30, Duration.ofSeconds(1));

            case "/startGame":
            case "/selectWord":
                return Bandwidth.simple(1, Duration.ofSeconds(3));

            case "/requestWord":
                return Bandwidth.simple(5, Duration.ofSeconds(5));

            default:
                return defaultLimit();
        }
    }

    private Bandwidth defaultLimit() {
        return Bandwidth.simple(2, Duration.ofSeconds(5));
    }

    // 🔹 Helps avoid unsafe contains()
    private String extractEndpoint(String key) {
        // Example key: "user123:/guess"
        int index = key.indexOf(":");
        if (index != -1) {
            return key.substring(index + 1);
        }
        return key; // fallback
    }
}