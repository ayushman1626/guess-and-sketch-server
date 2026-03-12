package com.Guess.Sketch.guess_and_sketch_server.controller;

import com.Guess.Sketch.guess_and_sketch_server.dto.ChatMessage;
import com.Guess.Sketch.guess_and_sketch_server.dto.CreateRoomMessage;
import com.Guess.Sketch.guess_and_sketch_server.dto.JoinRoomMessage;
import com.Guess.Sketch.guess_and_sketch_server.model.Room;
import com.Guess.Sketch.guess_and_sketch_server.service.RoomManager;
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

    public GameController(RoomManager roomManager, SimpMessagingTemplate messagingTemplate) {
        this.roomManager = roomManager;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/joinRoom")
    public void joinRoom(JoinRoomMessage message,
                         SimpMessageHeaderAccessor headerAccessor) {

        // Get the session ID from the header
        String sessionId = headerAccessor.getSessionId();

        // Join the room using the RoomManager
        Room room = roomManager.joinRoom(
                message.getRoomId(),
                sessionId,
                message.getUsername()
        );

        messagingTemplate.convertAndSend(
                "/topic/room/" + room.getRoomId(),
                message.getUsername() + " joined the room"
        );
        System.out.println(
                "User " + message.getUsername() + " joined room " + message.getRoomId()
        );
    }

    @MessageMapping("/createRoom")
    @SendToUser("/queue/room-created")
    public String createRoom(CreateRoomMessage message,
                             SimpMessageHeaderAccessor headerAccessor) {

        String sessionId = headerAccessor.getSessionId();

        Room room = roomManager.createRoom(
                sessionId,
                message.getUsername()
        );

        System.out.println(
                "User " + message.getUsername() + " created room " + room.getRoomId()
        );

        return room.getRoomId();
    }

    @MessageMapping("/chat")
    public void chat(ChatMessage message,
                     SimpMessageHeaderAccessor headerAccessor) {

        String sessionId = headerAccessor.getSessionId();

        String roomId = roomManager.getRoomIdBySession(sessionId);

        Room room = roomManager.getRoomById(roomId);

        String username = room.getPlayerBySession(sessionId).getUsername();

        messagingTemplate.convertAndSend(
                "/topic/room/" + roomId + "/chat",
                username + ": " + message.getMessage()
        );
    }
}
