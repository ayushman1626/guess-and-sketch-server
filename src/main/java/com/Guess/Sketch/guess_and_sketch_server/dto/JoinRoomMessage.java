package com.Guess.Sketch.guess_and_sketch_server.dto;

import lombok.*;


public class JoinRoomMessage {
    private String roomId;
    private String username;

    public JoinRoomMessage() {
    }

    public JoinRoomMessage(String roomId, String username) {
        this.roomId = roomId;
        this.username = username;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

}
