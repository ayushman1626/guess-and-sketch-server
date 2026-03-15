package com.Guess.Sketch.guess_and_sketch_server.model;

public class Player {

    private String sessionId;
    private String username;
    private int score = 0;

    public Player(String session_id, String username) {
        this.sessionId = session_id;
        this.username = username;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
