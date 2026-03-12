package com.Guess.Sketch.guess_and_sketch_server.service;

import com.Guess.Sketch.guess_and_sketch_server.model.Player;
import com.Guess.Sketch.guess_and_sketch_server.model.Room;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class RoomManager {
    private final ConcurrentMap<String, Room> rooms = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, String> sessionToRoom = new ConcurrentHashMap<>();


    public Room joinRoom(String roomId, String sessionId, String username) {
        //Throw Exception if room doesn't exist
        Room room = rooms.get(roomId);
        if (room == null) {
            throw new IllegalArgumentException("Room not found");
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
        Room room = new Room(roomId, new ArrayList<>());

        System.out.println("Created room with ID: " + roomId);

        //Add room to map
        rooms.put(roomId, room);

        //Add player to room
        Player player = new Player(sessionId, username);
        room.getPlayers().add(player);

        //Map session to room
        sessionToRoom.put(sessionId, roomId);

        return room;
    }
}
