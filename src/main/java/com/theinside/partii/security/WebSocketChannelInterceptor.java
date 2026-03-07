package com.theinside.partii.security;

import com.theinside.partii.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketChannelInterceptor implements ChannelInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtDecoder jwtDecoder;
    private final UserRepository userRepository;

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
                log.debug("WebSocket CONNECT missing Authorization header");
                throw new IllegalArgumentException("Missing or invalid Authorization header");
            }

            try {
                String token = authHeader.substring(BEARER_PREFIX.length());
                Jwt jwt = jwtDecoder.decode(token);
                String email = jwt.getSubject();

                userRepository.findByEmail(email).ifPresentOrElse(user -> {
                    if (!user.isEnabled()) {
                        throw new IllegalArgumentException("User account is disabled");
                    }

                    SecurityUser securityUser = new SecurityUser(user);
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    securityUser,
                                    null,
                                    securityUser.getAuthorities()
                            );
                    accessor.setUser(authentication);
                    log.debug("WebSocket authenticated user: {}", email);
                }, () -> {
                    throw new IllegalArgumentException("User not found");
                });

            } catch (JwtException e) {
                log.debug("WebSocket JWT validation failed: {}", e.getMessage());
                throw new IllegalArgumentException("Invalid JWT token");
            }
        }

        return message;
    }
}
