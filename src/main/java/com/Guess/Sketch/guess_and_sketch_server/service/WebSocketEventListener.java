package com.Guess.Sketch.guess_and_sketch_server.service;

import com.Guess.Sketch.guess_and_sketch_server.controller.GameController;
import com.Guess.Sketch.guess_and_sketch_server.dto.GameEvent;
import com.Guess.Sketch.guess_and_sketch_server.enums.EventType;
import com.Guess.Sketch.guess_and_sketch_server.model.Player;
import com.Guess.Sketch.guess_and_sketch_server.model.Room;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import lombok.extern.slf4j.Slf4j;


@Component
public class WebSocketEventListener {

    private final RoomManager roomManager;
    private final GameService gameService;
    private final SimpMessagingTemplate messagingTemplate;
    private static final Logger log = LoggerFactory.getLogger(GameController.class);
    public WebSocketEventListener(RoomManager roomManager
    , GameService gameService, SimpMessagingTemplate messagingTemplate) {
        this.roomManager = roomManager;
        this.gameService = gameService;
        this.messagingTemplate = messagingTemplate;
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {

        String sessionId = event.getSessionId();
        log.info("Session Disconnected: {}", sessionId);

        String roomId = roomManager.getRoomIdBySession(sessionId);
        if(roomId == null) return;

        Room room = roomManager.getRoomById(roomId);
        if(room == null) return;

        Player player = room.getPlayerEntityBySession(sessionId);
        if(player == null) return;

        String username = player.getUsername();
        log.info("User {} (session: {}) disconnected from room {}", username, sessionId, roomId);

        boolean wasDrawer = false;

        int drawerIndex = room.getCurrentDrawerIndex();
        if(!room.getPlayers().isEmpty() && drawerIndex >= 0 && drawerIndex < room.getPlayers().size()){
            Player drawer = room.getPlayers().get(drawerIndex);
            wasDrawer = drawer.getSessionId().equals(sessionId);
        }

        //
        room.removePlayer(sessionId);

        //
        roomManager.removeSession(sessionId);

        //
        messagingTemplate.convertAndSend(
                "/topic/room/" + room.getRoomId(),new GameEvent(EventType.PLAYER_LEFT, username)
        );

        if(room.getPlayers().isEmpty()){
            roomManager.closeRoom(room);
            log.info("Room Closed: {}", room.getRoomId());
            return;
        }

        if(room.getCurrentDrawerIndex() >= room.getPlayers().size())
            room.setCurrentDrawerIndex(0);

        if(wasDrawer)
            gameService.endRound(room);

    }
}
