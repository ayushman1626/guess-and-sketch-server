package com.Guess.Sketch.guess_and_sketch_server.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class GameEndedRes {
    String reason;
    String winner;
    Map<String,Integer> finalScores;

    public GameEndedRes() {
    }

    public GameEndedRes(String reason, String winner, Map<String,Integer> finalScores) {
        this.reason = reason;
        this.winner = winner;
        this.finalScores = finalScores;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getWinner() {
        return winner;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }

    public Map<String, Integer> getFinalScores() {
        return finalScores;
    }

    public void setFinalScores(Map<String, Integer> finalScores) {
        this.finalScores = finalScores;
    }
}
