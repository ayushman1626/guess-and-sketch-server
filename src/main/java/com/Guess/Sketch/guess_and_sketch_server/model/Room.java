package com.Guess.Sketch.guess_and_sketch_server.model;

import com.Guess.Sketch.guess_and_sketch_server.dto.CreateRoomMessage;
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

    public CreateRoomMessage getPlayerBySession(String sessionId) {
        for (Player player : players) {
            if (player.getSession_id().equals(sessionId)) {
                return new CreateRoomMessage(player.getUsername());
            }
        }
        return null;
    }
}
