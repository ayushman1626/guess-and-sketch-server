package com.Guess.Sketch.guess_and_sketch_server.model;

import lombok.*;

public class Player {

    private String session_id;
    private String username;

    public Player(String session_id, String username) {
        this.session_id = session_id;
        this.username = username;
    }

    public String getSession_id() {
        return session_id;
    }

    public void setSession_id(String session_id) {
        this.session_id = session_id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
