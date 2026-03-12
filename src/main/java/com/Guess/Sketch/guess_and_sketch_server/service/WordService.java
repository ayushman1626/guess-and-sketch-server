package com.Guess.Sketch.guess_and_sketch_server.service;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

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

            System.out.println("Loaded words: " + words.size());

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    public List<String> getRandomWords(int count) {

        List<String> copy = new ArrayList<>(words);

        Collections.shuffle(copy);

        return copy.subList(0, count);
    }

}