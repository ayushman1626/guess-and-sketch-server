package com.Guess.Sketch.guess_and_sketch_server.service;

import com.Guess.Sketch.guess_and_sketch_server.dto.GameEvent;
import com.Guess.Sketch.guess_and_sketch_server.enums.EventType;
import com.Guess.Sketch.guess_and_sketch_server.model.Player;
import com.Guess.Sketch.guess_and_sketch_server.model.Room;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class WebSocketEventListener {

    private final RoomManager roomManager;
    private final GameService gameService;
    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketEventListener(RoomManager roomManager
    , GameService gameService, SimpMessagingTemplate messagingTemplate) {
        this.roomManager = roomManager;
        this.gameService = gameService;
        this.messagingTemplate = messagingTemplate;
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {

        String sessionId = event.getSessionId();

        String roomId = roomManager.getRoomIdBySession(sessionId);
        if(roomId == null) return;

        Room room = roomManager.getRoomById(roomId);
        if(room == null) return;

        Player player = room.getPlayerEntityBySession(sessionId);
        if(player == null) return;

        String username = player.getUsername();

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
            System.out.println("Room Closed" + room.getRoomId());
            return;
        }

        if(room.getCurrentDrawerIndex() >= room.getPlayers().size())
            room.setCurrentDrawerIndex(0);

        if(wasDrawer)
            gameService.endRound(room);

    }
}
