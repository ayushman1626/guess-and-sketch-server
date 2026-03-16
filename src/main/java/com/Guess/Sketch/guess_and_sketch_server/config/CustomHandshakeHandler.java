package com.Guess.Sketch.guess_and_sketch_server.config;

import com.Guess.Sketch.guess_and_sketch_server.enums.StompPrincipal;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

@Component
public class CustomHandshakeHandler extends DefaultHandshakeHandler {
    @Override
    protected Principal determineUser(ServerHttpRequest request,
                                      WebSocketHandler wsHandler,
                                      Map<String, Object> attributes) {
        // Sec-WebSocket-Key is only present for native WebSocket upgrades.
        // SockJS fallback transports (XHR, long-polling) do NOT send it,
        // so we fall back to a random UUID to avoid null session identity.
        String sessionId = request.getHeaders().getFirst("Sec-WebSocket-Key");
        if (sessionId == null || sessionId.isBlank()) {
            sessionId = UUID.randomUUID().toString();
        }
        return new StompPrincipal(sessionId);
    }
}
