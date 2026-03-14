package com.Guess.Sketch.guess_and_sketch_server.service;

import com.Guess.Sketch.guess_and_sketch_server.enums.GameState;
import com.Guess.Sketch.guess_and_sketch_server.model.Room;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RoundScheduler {

    private final RoomManager roomManager;
    private final GameService gameService;

    public RoundScheduler(RoomManager roomManager, GameService gameService) {
        this.roomManager = roomManager;
        this.gameService = gameService;
    }

    @Scheduled(fixedRate = 1000)
    public void checkRounds() {

        for (Room room : roomManager.getRooms()) {

            if (room.getState() == GameState.DRAWING || room.getState() == GameState.WORD_SELECTION) {

                if (System.currentTimeMillis() > room.getRoundEndTime()) {

                    gameService.endRound(room);

                }
                if(room.getCorrectGuessers().size() ==
                        room.getPlayers().size() - 1){

                    gameService.endRound(room);
                }

            }
            if(room.getState() == GameState.WORD_SELECTION
                    && System.currentTimeMillis() > room.getRoundEndTime() - 50000) {
                gameService.autoSelectWord(room);
            }

        }

    }

}
