package com.Guess.Sketch.guess_and_sketch_server.dto;

import lombok.*;

public class CreateRoomMessage {
    private String username;

    public CreateRoomMessage() {
    }

    public CreateRoomMessage(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
