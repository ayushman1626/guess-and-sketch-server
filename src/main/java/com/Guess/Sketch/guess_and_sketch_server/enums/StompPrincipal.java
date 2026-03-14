package com.Guess.Sketch.guess_and_sketch_server.enums;

import java.security.Principal;

public class StompPrincipal implements Principal {
    private final String name;
    public StompPrincipal(String name) { this.name = name; }
    @Override
    public String getName() { return name; }
}
