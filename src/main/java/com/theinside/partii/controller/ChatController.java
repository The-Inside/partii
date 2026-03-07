package com.theinside.partii.controller;

import com.theinside.partii.dto.ChatMessageResponse;
import com.theinside.partii.dto.SendMessageRequest;
import com.theinside.partii.security.SecurityUser;
import com.theinside.partii.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat/{eventId}/send")
    public void sendMessage(
            @DestinationVariable Long eventId,
            @Payload SendMessageRequest request,
            Principal principal
    ) {
        SecurityUser securityUser = extractSecurityUser(principal);
        ChatMessageResponse response = chatService.sendMessage(
                eventId, securityUser.getUserId(), request.content());

        messagingTemplate.convertAndSend(
                "/topic/event/" + eventId + "/messages", response);
    }

    private SecurityUser extractSecurityUser(Principal principal) {
        if (principal instanceof UsernamePasswordAuthenticationToken auth) {
            return (SecurityUser) auth.getPrincipal();
        }
        throw new IllegalStateException("Unexpected principal type");
    }
}
