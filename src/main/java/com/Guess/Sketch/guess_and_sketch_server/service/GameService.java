package com.Guess.Sketch.guess_and_sketch_server.service;

import com.Guess.Sketch.guess_and_sketch_server.dto.*;
import com.Guess.Sketch.guess_and_sketch_server.enums.EventType;
import com.Guess.Sketch.guess_and_sketch_server.model.Player;
import com.Guess.Sketch.guess_and_sketch_server.model.Room;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import com.Guess.Sketch.guess_and_sketch_server.enums.GameState;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Service
public class GameService {
    private final RoomManager roomManager;
    private final SimpMessagingTemplate messagingTemplate;
    private final WordService wordService;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());

    public GameService(RoomManager roomManager,
                          SimpMessagingTemplate messagingTemplate,
                          WordService wordService
    ) {
        this.roomManager = roomManager;
        this.messagingTemplate = messagingTemplate;
        this.wordService = wordService;
    }

    public void handelJoinRoom(String sessionId, JoinRoomMessage message) {
        // Join the room using the RoomManager
        Room room = roomManager.joinRoom(
                message.getRoomId(),
                sessionId,
                message.getUsername()
        );

        messagingTemplate.convertAndSend(
                "/topic/room/" + room.getRoomId(),
                new GameEvent(EventType.PLAYER_JOINED, message.getUsername())
        );
        messagingTemplate.convertAndSend(
                "/topic/room/"+ room.getRoomId(),
                new GameEvent(EventType.ROOM_UPDATE,new RoomUpdateDto(room.getRoomId(),room.getPlayers(),room.getDrawEvents()))
        );
        System.out.println("User " + message.getUsername() + " joined room " + message.getRoomId());
    }

    public void handelChat(String sessionId, ChatMessage message){
        String roomId = roomManager.getRoomIdBySession(sessionId);

        Room room = roomManager.getRoomById(roomId);

        String username = room.getPlayerBySession(sessionId).getUsername();

        messagingTemplate.convertAndSend(
                "/topic/room/" + roomId + "/chat",
                 new GameEvent(EventType.CHAT_MESSAGE,new ChatMessage(username, message.getMessage()))
        );

        System.out.println("User " + username + " sent message: " + message.getMessage() + " to room " + roomId);
    }



    public void handelGameStart(String sessionId) {
        /*
        1. Validate creator
        2. Validate minimum players
        3. Reset all scores**
        4. Reset drawer index
        5. Set GameState = WORD_SELECTION
        6. Broadcast GAME_STARTED event
        7. Call startRound()*/

        String roomId = roomManager.getRoomIdBySession(sessionId);
        Room room = roomManager.getRoomById(roomId);
        if (room.getPlayers().size() < 2) return;
        if(!room.getCreatorSessionId().equals(sessionId)) return;
        
        room.getPlayers().forEach(p -> p.setScore(0));
        room.setCurrentRound(1);
        room.setState(GameState.WORD_SELECTION);
        room.setCurrentDrawerIndex(-1);
        messagingTemplate.convertAndSend("/topic/room/" + room.getRoomId(),
                    new GameEvent(EventType.GAME_STARTED,null));
        System.out.println("Game started in room " + roomId + " by user " + room.getPlayerBySession(sessionId).getUsername());
        startRound(room);
    }

    private void startRound(Room room) {
        /*
        * 1. Select next drawer
          2. Reset correctGuessers
          3. Clear draw events
          4. Send word options to drawer
          5. Start timer
          5. Broadcast ROUND_STARTED
          6. Change state to WORD_SELECTION
          * 7. Start round timer (optional - can be handled on client side with a timestamp)*
        * */
        if(room.getPlayers().size() < 2) {
            room.setState(GameState.WAITING);
            messagingTemplate.convertAndSend("/topic/room/" + room.getRoomId(),
                    new GameEvent(EventType.GAME_STOP,new RoomUpdateDto(room.getRoomId(),room.getPlayers(),room.getDrawEvents())));

            return;
        }

        int nextDrawerIndex = (room.getCurrentDrawerIndex() + 1) % room.getPlayers().size();
        
        if (room.getCurrentDrawerIndex() != -1 && nextDrawerIndex == 0) {
            room.setCurrentRound(room.getCurrentRound() + 1);
            if (room.getCurrentRound() > room.getMaxRounds()) {
                room.setState(GameState.WAITING);
                messagingTemplate.convertAndSend("/topic/room/" + room.getRoomId(),
                        new GameEvent(EventType.GAME_STOP, new RoomUpdateDto(room.getRoomId(), room.getPlayers(), room.getDrawEvents())));
                return;
            }
        }

        room.setCurrentDrawerIndex(nextDrawerIndex);

        room.setCorrectGuessers(ConcurrentHashMap.newKeySet());
        room.getDrawEvents().clear();
        room.setCurrentWord(null);

        Player player = room.getPlayers().get(nextDrawerIndex);

        // Send word options to drawer
        SimpMessageHeaderAccessor headerAccessor =
                SimpMessageHeaderAccessor.create();

        headerAccessor.setSessionId(player.getSessionId());
        headerAccessor.setLeaveMutable(true);

        messagingTemplate.convertAndSendToUser(
                player.getSessionId(),
                "/queue/word-options",
                new GameEvent(EventType.WORD_OPTIONS,
                        new WordOptionMessage(wordService.getRandomWords(3))),
                headerAccessor.getMessageHeaders()
        );
        System.out.println("Sent word options to drawer: " + player.getUsername()+" :: " + player.getSessionId());

        room.setRoundEndTime(System.currentTimeMillis()+60000);
        // Broadcast ROUND_STARTED
        messagingTemplate.convertAndSend("/topic/room/" + room.getRoomId(),
                new GameEvent(EventType.ROUND_STARTED, player.getUsername()));

        room.setState(GameState.WORD_SELECTION);
    }

    public void handelSelectWord(String sessionId, String word) {
        /*
        * validate drawer
            store currentWord
            change state to DRAWING
            broadcast ROUND_STARTED
        * */
        String roomId = roomManager.getRoomIdBySession(sessionId);
        Room room = roomManager.getRoomById(roomId);

        if(!room.getState().equals(GameState.WORD_SELECTION)) return;

        Player drawer = room.getPlayers().get(room.getCurrentDrawerIndex());

        if(!drawer.getSessionId().equals(sessionId)) return;

        room.setCurrentWord(word);
        room.setState(GameState.DRAWING);
        messagingTemplate.convertAndSend("/topic/room/"+room.getRoomId(),
                new GameEvent(EventType.WORD_SELECTED,drawer.getUsername()));
            System.out.println("Drawer " + drawer.getUsername() + " selected word: " + word);
            System.out.println("Round started in room " + roomId + " with drawer " + drawer.getUsername() + " and word " + word);

    }



    public void handelDraw(String sessionId, DrawEvent drawEvent) {
        String roomId = roomManager.getRoomIdBySession(sessionId);

        Room room = roomManager.getRoomById(roomId);
        if(!room.getState().equals(GameState.DRAWING)) return;
        Player drawer = room.getPlayers().get(room.getCurrentDrawerIndex());
        if(!drawer.getSessionId().equals(sessionId)) return;
        room.getDrawEvents().add(drawEvent);

        messagingTemplate.convertAndSend(
                "/topic/room/" + roomId + "/draw",
                new GameEvent(EventType.DRAW_EVENT, drawEvent)
        );
    }

    public void handleGuess(String sessionId, GuessMessage message) {

        String roomId = roomManager.getRoomIdBySession(sessionId);
        if(roomId == null) return;

        Room room = roomManager.getRoomById(roomId);
        if(room == null) return;

        if(room.getState() != GameState.DRAWING) return;

        if(System.currentTimeMillis() > room.getRoundEndTime()) return;

        Player player = room.getPlayerEntityBySession(sessionId);
        if(player == null) return;

        Player drawer = room.getPlayers().get(room.getCurrentDrawerIndex());

        if(drawer.getSessionId().equals(sessionId)) return;

        if(room.getCorrectGuessers().contains(sessionId)) return;

        String guess = message.getMessage().trim();

        if(guess.equalsIgnoreCase(room.getCurrentWord())){

            room.getCorrectGuessers().add(sessionId);

            handleScore(room, sessionId);

            messagingTemplate.convertAndSend(
                    "/topic/room/" + roomId,
                    new GameEvent(EventType.PLAYER_GUESSED, player.getUsername())
            );

        } else {

            messagingTemplate.convertAndSend(
                    "/topic/room/" + roomId,
                    new GameEvent(EventType.CHAT_MESSAGE,
                            new ChatMessage(player.getUsername(), guess))
            );
        }

        int guessers = room.getPlayers().size() - 1;

        if(room.getCorrectGuessers().size() == guessers){
            endRound(room);
        }
    }

    private void handleScore(Room room, String sessionId) {
        Player drawer = room.getPlayers().get(room.getCurrentDrawerIndex());
        Player guesser = room.getPlayerEntityBySession(sessionId);

        long remainingTime =
                room.getRoundEndTime() - System.currentTimeMillis();

        if (remainingTime < 0)
            remainingTime = 0;

        int score = (int)(100 * (remainingTime / (double) 60000));// Assuming 60 seconds per round
        int newScore = guesser.getScore() + score;
        guesser.setScore(newScore);

        messagingTemplate.convertAndSend(
                "/topic/room/" + room.getRoomId()+"/score",
                new GameEvent(EventType.SCORE_UPDATE,
                        new ScoreUpdateRes(guesser.getUsername(), score, newScore))
        );
        System.out.println( "Guesser " + guesser.getUsername() + " scored " + score + " points. Total score: " + newScore);
        int drawerScore = drawer.getScore() + score/2;
        drawer.setScore(drawerScore);
        messagingTemplate.convertAndSend(
                "/topic/room/" + room.getRoomId()+"/score",
                new GameEvent(EventType.SCORE_UPDATE,
                        new ScoreUpdateRes(drawer.getUsername(), score/2, drawerScore))
        );
            System.out.println( "Drawer " + drawer.getUsername() + " scored " + score/2 + " points. Total score: " + drawerScore);
    }


    public void endRound(Room room) {
        synchronized(room) {
            if (room.getState() == GameState.ROUND_END || room.getState() == GameState.WAITING) {
                return;
            }
            room.setState(GameState.ROUND_END);
        }

        messagingTemplate.convertAndSend("/topic/room/"+ room.getRoomId(),
                new GameEvent(EventType.ROUND_ENDED,"Round ended! The word was: " + room.getCurrentWord()));

        System.out.println("Round Ended in Romm : "+room.getRoomId());

        room.getDrawEvents().clear();
        
        ScheduledFuture<?> task = scheduler.schedule(() -> startRound(room), 5, TimeUnit.SECONDS);
        room.setRoundStartTask(task);
    }

    public void autoSelectWord(Room room) {
        if(room.getState().equals(GameState.WORD_SELECTION)){
            Player drawer = room.getPlayers().get(room.getCurrentDrawerIndex());
            String word = wordService.getRandomWords(1).get(0);
            handelSelectWord(drawer.getSessionId(), word);
        }

    }
}
