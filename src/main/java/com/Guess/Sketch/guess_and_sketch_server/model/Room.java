package com.Guess.Sketch.guess_and_sketch_server.model;

import lombok.*;

import java.util.List;

public class Room {
    private String roomId;
    private List<Player> players;

    public Room(String roomId, List<Player> players) {
        this.roomId = roomId;
        this.players = players;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }
}
