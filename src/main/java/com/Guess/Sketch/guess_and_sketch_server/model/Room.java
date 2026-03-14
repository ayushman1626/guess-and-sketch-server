package com.Guess.Sketch.guess_and_sketch_server.model;

import com.Guess.Sketch.guess_and_sketch_server.dto.CreateRoomMessage;
import com.Guess.Sketch.guess_and_sketch_server.dto.DrawEvent;
import com.Guess.Sketch.guess_and_sketch_server.enums.GameState;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Builder
@Data
public class Room {

    private String roomId;
    private List<Player> players;
    private String currentWord;
    private int currentDrawerIndex = -1;
    private String creatorSessionId;
    private GameState state = GameState.WAITING;
    private Set<String> correctGuessers = new HashSet<>();
    private List<DrawEvent> drawEvents = new ArrayList<>();
    private long roundEndTime;
    private static final int MAX_PLAYERS = 10;

    public Room(String roomId, List<Player> players, String creatorSessionId) {
        this.roomId = roomId;
        this.players = players;
        this.creatorSessionId = creatorSessionId;
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

    public String getCurrentWord() {
        return currentWord;
    }

    public void setCurrentWord(String currentWord) {
        this.currentWord = currentWord;
    }

    public int getCurrentDrawerIndex() {
        return currentDrawerIndex;
    }

    public void setCurrentDrawerIndex(int currentDrawerIndex) {
        this.currentDrawerIndex = currentDrawerIndex;
    }

    public String getCreatorSessionId() {
        return creatorSessionId;
    }

    public void setCreatorSessionId(String creatorSessionId) {
        this.creatorSessionId = creatorSessionId;
    }

    public GameState getState() {
        return state;
    }

    public void setState(GameState state) {
        this.state = state;
    }

    public Set<String> getCorrectGuessers() {
        return correctGuessers;
    }

    public void setCorrectGuessers(Set<String> correctGuessers) {
        this.correctGuessers = correctGuessers;
    }

    public List<DrawEvent> getDrawEvents() {
        return drawEvents;
    }

    public void setDrawEvents(List<DrawEvent> drawEvents) {
        this.drawEvents = drawEvents;
    }

    public long getRoundEndTime() {
        return roundEndTime;
    }

    public void setRoundEndTime(long roundEndTime) {
        this.roundEndTime = roundEndTime;
    }

    public CreateRoomMessage getPlayerBySession(String sessionId) {
        for (Player player : players) {
            if (player.getSessionId().equals(sessionId)) {
                return new CreateRoomMessage(player.getUsername());
            }
        }
        return null;
    }
}
