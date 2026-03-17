package com.Guess.Sketch.guess_and_sketch_server.dto;

import java.util.List;

public class RoundScoreRes {
    private List<ScoreUpdateRes> playerScores;

    public RoundScoreRes() {
    }

    public RoundScoreRes(List<ScoreUpdateRes> playerScores) {
        this.playerScores = playerScores;
    }

    public List<ScoreUpdateRes> getPlayerScores() {
        return playerScores;
    }

    public void setPlayerScores(List<ScoreUpdateRes> playerScores) {
        this.playerScores = playerScores;
    }
}
