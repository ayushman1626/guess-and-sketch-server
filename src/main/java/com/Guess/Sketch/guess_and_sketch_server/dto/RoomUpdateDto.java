package com.Guess.Sketch.guess_and_sketch_server.dto;

import com.Guess.Sketch.guess_and_sketch_server.model.Player;
import lombok.Data;

import java.util.List;

@Data
public class RoomUpdateDto {
    String roomId;
    List<Player> players;
    List<DrawEvent> drawEvents;

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

    public List<DrawEvent> getDrawEvents() {
        return drawEvents;
    }

    public void setDrawEvents(List<DrawEvent> drawEvents) {
        this.drawEvents = drawEvents;
    }

    public RoomUpdateDto(String roomId, List<Player> players, List<DrawEvent> drawEvents) {
        this.roomId = roomId;
        this.players = players;
        this.drawEvents = drawEvents;
    }
}
