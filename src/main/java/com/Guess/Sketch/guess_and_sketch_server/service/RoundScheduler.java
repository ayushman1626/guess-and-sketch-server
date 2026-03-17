package com.Guess.Sketch.guess_and_sketch_server.service;

import com.Guess.Sketch.guess_and_sketch_server.controller.GameController;
import com.Guess.Sketch.guess_and_sketch_server.enums.GameState;
import com.Guess.Sketch.guess_and_sketch_server.model.Room;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;


@Component
public class RoundScheduler {

    private final RoomManager roomManager;
    private final GameService gameService;
    private static final Logger log = LoggerFactory.getLogger(GameController.class);
    public RoundScheduler(RoomManager roomManager, GameService gameService) {
        this.roomManager = roomManager;
        this.gameService = gameService;
    }

    @Scheduled(fixedRate = 1000)
    public void checkRounds() {

        for (Room room : roomManager.getRooms()) {

            if (room.getState() == GameState.DRAWING || room.getState() == GameState.WORD_SELECTION) {

                if (System.currentTimeMillis() > room.getRoundEndTime()) {
                    log.info("Round timer expired for room {}", room.getRoomId());
                    gameService.endRound(room);
                } else if(room.getCorrectGuessers().size() >= Math.max(1, room.getPlayers().size() - 1)) {
                    log.info("All players guessed correctly in room {}", room.getRoomId());
                    gameService.endRound(room);
                }

            }
            if(room.getState() == GameState.WORD_SELECTION
                    && System.currentTimeMillis() > room.getRoundEndTime() - 45000) {
                log.info("Auto-selecting word for room {}", room.getRoomId());
                gameService.autoSelectWord(room);
            }

        }

    }

}
