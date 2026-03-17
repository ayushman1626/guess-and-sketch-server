package com.Guess.Sketch.guess_and_sketch_server.service;

import com.Guess.Sketch.guess_and_sketch_server.controller.GameController;
import com.Guess.Sketch.guess_and_sketch_server.model.Player;
import com.Guess.Sketch.guess_and_sketch_server.model.Room;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class RoomManager {
    private final ConcurrentMap<String, Room> rooms = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, String> sessionToRoom = new ConcurrentHashMap<>();
    private static final Logger log = LoggerFactory.getLogger(GameController.class);

    public Room joinRoom(String roomId, String sessionId, String username) {
        //Throw Exception if room doesn't exist
        Room room = rooms.get(roomId);
        if (room == null) {
            log.warn("Attempt to join non-existent room: {}", roomId);
            throw new IllegalArgumentException("Room not found");
        }
        if(room.isFull()) {
            log.warn("Attempt to join full room: {}", roomId);
            return null;
        }
        //Add player to room
        Player player = new Player(sessionId, username);
        room.getPlayers().add(player);
        //Map session to room
        sessionToRoom.put(sessionId, roomId);
        return room;
    }

    public Room createRoom(String sessionId, String username) {
        //generate 6 digit unique room id
        String roomId;
        do {
            roomId = String.valueOf((int)(Math.random() * 900000) + 100000);
        } while (rooms.containsKey(roomId));

        //Create room
        Room room =  new Room(roomId,new ArrayList<>(),sessionId);

        log.info("Created room with ID: {} by user {}", roomId, username);

        //Add room to map
        rooms.put(roomId, room);

        //Add player to room
        Player player = new Player(sessionId, username);
        room.getPlayers().add(player);

        //Map session to room
        sessionToRoom.put(sessionId, roomId);

        return room;
    }

    public String getRoomIdBySession(String sessionId) {
        return sessionToRoom.get(sessionId);
    }

    public Room getRoomById(String roomId) {
        return rooms.get(roomId);
    }

    public List<Room> getRooms() {
        return new ArrayList<>(rooms.values());
    }

    public void removeSession(String sessionId) {
        String roomId = sessionToRoom.remove(sessionId);
        if (roomId != null) {
            log.debug("Session {} removed from room {}", sessionId, roomId);
        }
    }

    public void closeRoom(Room room) {
        if(room == null) return;
        if(room.getRoundStartTask() != null) {
            room.getRoundStartTask().cancel(false);
        }
        rooms.remove(room.getRoomId());
        log.info("Closed room: {}", room.getRoomId());
    }
}
