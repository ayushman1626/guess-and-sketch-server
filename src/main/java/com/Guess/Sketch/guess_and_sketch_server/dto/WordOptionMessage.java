package com.Guess.Sketch.guess_and_sketch_server.dto;

import java.util.List;

public class WordOptionMessage {

    private List<String> words;

    public WordOptionMessage(List<String> words) {
        this.words = words;
    }

    public List<String> getWords() {
        return words;
    }

}