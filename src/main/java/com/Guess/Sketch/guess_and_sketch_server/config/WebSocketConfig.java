package com.Guess.Sketch.guess_and_sketch_server.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }

    // This class is a configuration class for WebSocket in a Spring Boot application. It enables WebSocket message handling and configures the message broker. The registerStompEndpoints method registers a STOMP endpoint at "/ws" and allows cross-origin requests from any origin. The configureMessageBroker method sets up a simple in-memory message broker with a destination prefix of "/topic" for outgoing messages and "/app" for incoming messages. This configuration allows clients to connect to the WebSocket endpoint and send/receive messages using the specified prefixes.
}
