package com.Guess.Sketch.guess_and_sketch_server.config;

import com.Guess.Sketch.guess_and_sketch_server.service.RateLimiterService;
import io.github.bucket4j.Bucket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

@Component
public class RateLimitInterceptor implements ChannelInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RateLimitInterceptor.class);
    @Autowired
    private RateLimiterService rateLimiter;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) return message;

        // Only limit SEND messages
        if (StompCommand.SEND.equals(accessor.getCommand())) {

            String sessionId = accessor.getSessionId();
            String destination = accessor.getDestination();

            if (sessionId == null || destination == null) {
                return message;
            }

            //Normalize destination (/app/guess → /guess)
            String endpoint = normalizeDestination(destination);

            String key = sessionId + ":" + endpoint;

            log.info("⏱️ Checking rate limit for: " + key+" available tokens: "+rateLimiter.resolveBucket(key).getAvailableTokens());

            Bucket bucket = rateLimiter.resolveBucket(key);

            //** Needed to change. Handle with response
            if (!bucket.tryConsume(1)) {

                log.info("🚫 Rate limit hit: " + key);

                // ❗ Option 1: Block silently
                return null;

                // ❗ Option 2 (better UX): send error to client (advanced)
                // throw new MessagingException("Rate limit exceeded");
            }
        }

        return message;
    }

    private String normalizeDestination(String destination) {
        // Example: /app/guess → /guess
        if (destination.startsWith("/app")) {
            return destination.substring(4);
        }
        return destination;
    }
}
