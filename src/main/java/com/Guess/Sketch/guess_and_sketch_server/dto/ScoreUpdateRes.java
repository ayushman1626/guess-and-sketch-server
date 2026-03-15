package com.Guess.Sketch.guess_and_sketch_server.dto;


public class ScoreUpdateRes {
    private String username;
    private int score;
    private int totalScore;

    public ScoreUpdateRes() {
    }

    public ScoreUpdateRes(String username, int score, int totalScore) {
        this.username = username;
        this.score = score;
        this.totalScore = totalScore;
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

    public int getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }
}
