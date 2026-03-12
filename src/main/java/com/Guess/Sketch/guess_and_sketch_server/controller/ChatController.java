package com.Guess.Sketch.guess_and_sketch_server.controller;

import com.Guess.Sketch.guess_and_sketch_server.dto.ChatMessage;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {
    @MessageMapping("/chat")
    @SendTo("/topic/messages")
    public ChatMessage sendMessage(ChatMessage message) {
        System.out.println("Received message from " + message.getSender() + ": " + message.getMessage());
        return message;
    }
}
