package com.Guess.Sketch.guess_and_sketch_server.model;

import com.Guess.Sketch.guess_and_sketch_server.dto.CreateRoomMessage;
import com.Guess.Sketch.guess_and_sketch_server.dto.DrawEvent;
import com.Guess.Sketch.guess_and_sketch_server.enums.GameState;
import lombok.*;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;

@Data
public class Room {

    private String roomId;
    private List<Player> players = new CopyOnWriteArrayList<>();
    private String currentWord;
    private int currentDrawerIndex = -1;
    private String creatorSessionId;
    private GameState state = GameState.WAITING;
    private Set<String> correctGuessers = ConcurrentHashMap.newKeySet();
    private List<DrawEvent> drawEvents = new CopyOnWriteArrayList<>();
    private long roundEndTime;
    private int currentRound = 1;
    private int maxRounds = 3;
    private ScheduledFuture<?> roundStartTask;
    private static final int MAX_PLAYERS = 10;

    public Room(String roomId, List<Player> players, String creatorSessionId) {
        this.roomId = roomId;
        this.players = new CopyOnWriteArrayList<>(players);
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

    public int getCurrentRound() {
        return currentRound;
    }

    public void setCurrentRound(int currentRound) {
        this.currentRound = currentRound;
    }

    public int getMaxRounds() {
        return maxRounds;
    }

    public void setMaxRounds(int maxRounds) {
        this.maxRounds = maxRounds;
    }

    public ScheduledFuture<?> getRoundStartTask() {
        return roundStartTask;
    }

    public void setRoundStartTask(ScheduledFuture<?> roundStartTask) {
        this.roundStartTask = roundStartTask;
    }

    public CreateRoomMessage getPlayerBySession(String sessionId) {
        for (Player player : players) {
            if (player.getSessionId().equals(sessionId)) {
                return new CreateRoomMessage(player.getUsername());
            }
        }
        return null;
    }
    public boolean isFull() {
        return players.size() >= MAX_PLAYERS;
    }
    public Player getPlayerEntityBySession(String sessionId) {
        for (Player player : players) {
            if (player.getSessionId().equals(sessionId)) {
                return player;
            }
        }
        return null;
    }

    public void removePlayer(String sessionId) {
        players.removeIf(player -> player.getSessionId().equals(sessionId));
    }
}
