package com.Guess.Sketch.guess_and_sketch_server.service;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

@Slf4j
@Service
public class WordService {

    private List<String> words = new ArrayList<>();

    private Random random = new Random();

    @PostConstruct
    public void loadWords() {

        try {

            InputStream inputStream =
                    getClass().getResourceAsStream("/words.txt");

            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(inputStream));

            String line;

            while ((line = reader.readLine()) != null) {

                words.add(line.trim());
            }

            log.info("Loaded words: {}", words.size());

        } catch (Exception e) {

            log.error("Failed to load words", e);
        }
    }

    public List<String> getRandomWords(int count) {

        List<String> copy = new ArrayList<>(words);

        Collections.shuffle(copy);

        return copy.subList(0, count);
    }

}