package com.Guess.Sketch.guess_and_sketch_server.controller;

import com.Guess.Sketch.guess_and_sketch_server.dto.*;
import com.Guess.Sketch.guess_and_sketch_server.model.Room;
import com.Guess.Sketch.guess_and_sketch_server.service.GameService;
import com.Guess.Sketch.guess_and_sketch_server.service.RoomManager;
import com.Guess.Sketch.guess_and_sketch_server.service.WordService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.springframework.messaging.simp.SimpMessagingTemplate;

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
        Room room = roomManager.createRoom(sessionId, message.getUsername());
        System.out.println("User " + message.getUsername() + " created room " + room.getRoomId());
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
        gameService.handelGuess(sessionId, message);
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
