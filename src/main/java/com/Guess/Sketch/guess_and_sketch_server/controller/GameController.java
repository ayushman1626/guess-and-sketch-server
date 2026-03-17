package com.Guess.Sketch.guess_and_sketch_server.controller;

import com.Guess.Sketch.guess_and_sketch_server.dto.*;
import com.Guess.Sketch.guess_and_sketch_server.model.Room;
import com.Guess.Sketch.guess_and_sketch_server.service.GameService;
import com.Guess.Sketch.guess_and_sketch_server.service.RoomManager;
import com.Guess.Sketch.guess_and_sketch_server.service.WordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;


@Controller
public class GameController {
    private final RoomManager roomManager;
    private final SimpMessagingTemplate messagingTemplate;
    private final WordService wordService;
    private final GameService gameService;

    public GameController(RoomManager roomManager,
                          SimpMessagingTemplate messagingTemplate,
                          WordService wordService,
                            GameService gameService
    ) {
        this.roomManager = roomManager;
        this.messagingTemplate = messagingTemplate;
        this.wordService = wordService;
        this.gameService = gameService;
    }

    private static final Logger log = LoggerFactory.getLogger(GameController.class);

    @MessageMapping("/joinRoom")
    public void joinRoom(JoinRoomMessage message,
                         SimpMessageHeaderAccessor headerAccessor) {

        // Get the session ID from the header
        String sessionId = headerAccessor.getSessionId();
        // Join the room using the GameService
        gameService.handelJoinRoom(sessionId, message);
    }

    @MessageMapping("/createRoom")
    @SendToUser("/queue/room-created")
    public String createRoom(CreateRoomMessage message,
                             SimpMessageHeaderAccessor headerAccessor) {

        String sessionId = headerAccessor.getSessionId();
        // Sanitize username: strip HTML tags and enforce max length
        String username = message.getUsername();
        if (username != null) {
            username = username.replaceAll("<[^>]*>", "").trim();
            if (username.length() > 20) username = username.substring(0, 20);
        }
        if (username == null || username.isEmpty()) {
            log.warn("Rejected createRoom with empty username from session {}", sessionId);
            return null;
        }
        message.setUsername(username);

        Room room = roomManager.createRoom(sessionId, username);
        log.info("User {} created room {}", username, room.getRoomId());
        return room.getRoomId();
    }
    // Needed Fix - (controller voilations - > accessed game data) - (game data should be handled by service)



    @MessageMapping("/chat")
    public void chat(ChatMessage message,
                     SimpMessageHeaderAccessor headerAccessor) {

        String sessionId = headerAccessor.getSessionId();
        gameService.handelChat(sessionId, message);
    }

    @MessageMapping("/draw")
    public void draw(DrawEvent drawEvent,
                     SimpMessageHeaderAccessor headerAccessor) {

        String sessionId = headerAccessor.getSessionId();
        gameService.handelDraw(sessionId, drawEvent);
    }

    @MessageMapping("/guess")
    public void guess(GuessMessage message,
                      SimpMessageHeaderAccessor headerAccessor){

        String sessionId = headerAccessor.getSessionId();
        gameService.handleGuess(sessionId, message);
    }

    @MessageMapping("/requestWords")
    @SendToUser("/queue/word-options")
    public WordOptionMessage getWords(
            SimpMessageHeaderAccessor headerAccessor){
        String session_id = headerAccessor.getSessionId();

        String roomId = roomManager.getRoomIdBySession(session_id);

        Room room = roomManager.getRoomById(roomId);

        return new WordOptionMessage(wordService.getRandomWords(3));
    }
    //Needed Fix - (controller voilations - > accessed game data) - (game data should be handled by service)

    @MessageMapping("/startGame")
    public void startGame(SimpMessageHeaderAccessor headerAccessor){
        String sessionId = headerAccessor.getSessionId();
        gameService.handelGameStart(sessionId);
    }

    @MessageMapping("/selectWord")
    public void selectWord(SelectWordMessage message, SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        gameService.handelSelectWord(sessionId, message.getWord());
    }

}
