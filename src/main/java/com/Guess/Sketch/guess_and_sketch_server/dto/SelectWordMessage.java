package com.Guess.Sketch.guess_and_sketch_server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SelectWordMessage {
    private String word;

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }
}
