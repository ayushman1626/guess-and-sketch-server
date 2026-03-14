package com.Guess.Sketch.guess_and_sketch_server.dto;

import com.Guess.Sketch.guess_and_sketch_server.enums.EventType;

public class GameEvent {

    private EventType type;

    private Object payload;

    public GameEvent(EventType type, Object payload) {
        this.type = type;
        this.payload = payload;
    }

    public EventType getType() {
        return type;
    }

    public Object getPayload() {
        return payload;
    }
}
