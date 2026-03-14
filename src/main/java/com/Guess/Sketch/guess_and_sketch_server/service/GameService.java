package com.Guess.Sketch.guess_and_sketch_server.service;

import com.Guess.Sketch.guess_and_sketch_server.dto.*;
import com.Guess.Sketch.guess_and_sketch_server.enums.EventType;
import com.Guess.Sketch.guess_and_sketch_server.model.Player;
import com.Guess.Sketch.guess_and_sketch_server.model.Room;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import com.Guess.Sketch.guess_and_sketch_server.enums.GameState;

import java.util.HashSet;

@Service
public class GameService {
    private final RoomManager roomManager;
    private final SimpMessagingTemplate messagingTemplate;
    private final WordService wordService;

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
                message.getUsername() + " joined the room"
        );

        System.out.println("User " + message.getUsername() + " joined room " + message.getRoomId());
    }

    public void handelChat(String sessionId, ChatMessage message){
        String roomId = roomManager.getRoomIdBySession(sessionId);

        Room room = roomManager.getRoomById(roomId);

        String username = room.getPlayerBySession(sessionId).getUsername();

        messagingTemplate.convertAndSend(
                "/topic/room/" + roomId + "/chat",
                 new GameEvent(EventType.CHAT_MESSAGE,username + ": " + message.getMessage())
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
        room.setState(GameState.WORD_SELECTION);
        room.setCurrentDrawerIndex(-1);
        messagingTemplate.convertAndSend("/topic/room/" + room.getRoomId(),
                    new GameEvent(EventType.GAME_STARTED, "Game has started!"));
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
        int nextDrawerIndex = (room.getCurrentDrawerIndex() + 1) % room.getPlayers().size();

        room.setCurrentDrawerIndex(nextDrawerIndex);

        room.setCorrectGuessers(new HashSet<>());
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
                new GameEvent(EventType.ROUND_STARTED, player.getUsername() + " is drawer!"));

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
                new GameEvent(EventType.WORD_SELECTED,drawer.getUsername()+" started Drawing !"));
        

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

    public void handelGuess(String sessionId, GuessMessage message) {
        String roomId = roomManager.getRoomIdBySession(sessionId);

        Room room = roomManager.getRoomById(roomId);

        if(!room.getState().equals(GameState.DRAWING)) return;

        Player drawer = room.getPlayers().get(room.getCurrentDrawerIndex());

        if(drawer.getSessionId().equals(sessionId)) return;

        if(room.getCorrectGuessers().contains(sessionId)) return;

        String username = room.getPlayerBySession(sessionId).getUsername();
        if(message.getMessage().equalsIgnoreCase(room.getCurrentWord())){
            room.getCorrectGuessers().add(sessionId);

            messagingTemplate.convertAndSend(
                    "/topic/room/" + roomId,
                    new GameEvent(EventType.PLAYER_GUESSED, username)
            );
        }else{
            messagingTemplate.convertAndSend(
                    "/topic/room/"+ roomId,
                    new GameEvent(EventType.CHAT_MESSAGE, username + ": " + message.getMessage()));
        }
        if(room.getCorrectGuessers().size() == room.getPlayers().size() - 1){
            endRound(room);
        }
    }


    public void endRound(Room room) {
        /*
        1. Change state → ROUND_END
        2. Reveal the correct word
        3. Stop drawing phase
        4. Reset round-specific data
        5. Clear canvas
        6. Start next round or end game after a delay if max rounds reached
         *Remaining
         *  calculate scores (optional - can be done at the end of the game)*
         *  Broadcast ROUND_ENDED with correct word and scores (optional)*
         *  Start next round after a delay (optional - can be handled on client side with a timer)*
         *
        */
        room.setState(GameState.ROUND_END);

        messagingTemplate.convertAndSend("/topic/room/"+ room.getRoomId(),
                new GameEvent(EventType.ROUND_ENDED,"Round ended! The word was: " + room.getCurrentWord()));

        room.getDrawEvents().clear();
        // Start next round after a delay (handled on client side with a timer)*
        new Thread(() -> {
            try {
                Thread.sleep(5000); // 5 second delay before next round
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            startRound(room);
        }).start();//Needs Attention
    }

    public void autoSelectWord(Room room) {
        if(room.getState().equals(GameState.WORD_SELECTION)){
            Player drawer = room.getPlayers().get(room.getCurrentDrawerIndex());
            String word = wordService.getRandomWords(1).get(0);
            handelSelectWord(drawer.getSessionId(), word);
        }

    }
}
