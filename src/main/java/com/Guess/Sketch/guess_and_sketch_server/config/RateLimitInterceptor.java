package com.Guess.Sketch.guess_and_sketch_server.config;

import com.Guess.Sketch.guess_and_sketch_server.dto.GameEvent;
import com.Guess.Sketch.guess_and_sketch_server.enums.EventType;
import com.Guess.Sketch.guess_and_sketch_server.service.RateLimiterService;
import io.github.bucket4j.Bucket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

@Component
public class RateLimitInterceptor implements ChannelInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RateLimitInterceptor.class);

    private final RateLimiterService rateLimiter;
    private final SimpMessagingTemplate messagingTemplate;

    public RateLimitInterceptor(RateLimiterService rateLimiter,
                                @Lazy SimpMessagingTemplate messagingTemplate) {  //**Needs fix, circular dependency with web socket config
        this.rateLimiter = rateLimiter;
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) {
            return message;
        }

        // Only limit SEND messages
        if (StompCommand.SEND.equals(accessor.getCommand())) {

            String sessionId = accessor.getSessionId();
            String destination = accessor.getDestination();

            if (sessionId == null || destination == null) {
                return message;
            }

            // Normalize destination (/app/guess → /guess)
            String endpoint = normalizeDestination(destination);
            String key = sessionId + ":" + endpoint;

            Bucket bucket = rateLimiter.resolveBucket(key);

            log.info("⏱️ Checking rate limit for: {} | Available tokens: {}",
                    key, bucket.getAvailableTokens());

            if (!bucket.tryConsume(1)) {

                log.warn("🚫 Rate limit hit: {}", key);

                sendRateLimitError(sessionId, endpoint);

                //IMPORTANT: block the message
                return null;
            }
        }

        return message;
    }

    private void sendRateLimitError(String sessionId, String endpoint) {

        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create();
        headerAccessor.setSessionId(sessionId);
        headerAccessor.setLeaveMutable(true);

        GameEvent event = new GameEvent(
                EventType.RATELIMIT_EXCEEDED,
                "Rate limit exceeded for endpoint: " + endpoint
        );

        messagingTemplate.convertAndSendToUser(
                sessionId,
                "/queue/errors",
                event,
                headerAccessor.getMessageHeaders()
        );
    }

    private String normalizeDestination(String destination) {
        return destination.startsWith("/app")
                ? destination.substring(4)
                : destination;
    }
}