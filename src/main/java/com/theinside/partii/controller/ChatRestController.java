package com.theinside.partii.controller;

import com.theinside.partii.dto.ChatMessageResponse;
import com.theinside.partii.security.SecurityUser;
import com.theinside.partii.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/partii/api/v1/events/{eventId}/chat")
@RequiredArgsConstructor
public class ChatRestController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @GetMapping("/messages")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<ChatMessageResponse>> getMessages(
            @PathVariable Long eventId,
            @AuthenticationPrincipal SecurityUser user,
            @PageableDefault(size = 50) Pageable pageable
    ) {
        log.debug("Fetching chat history for event {} by user {}", eventId, user.getUserId());
        Page<ChatMessageResponse> messages = chatService.getMessages(
                eventId, user.getUserId(), pageable);
        return ResponseEntity.ok(messages);
    }

    @DeleteMapping("/messages/{messageId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteMessage(
            @PathVariable Long eventId,
            @PathVariable Long messageId,
            @AuthenticationPrincipal SecurityUser user
    ) {
        log.info("Organizer {} deleting message {} in event {}", user.getUserId(), messageId, eventId);
        chatService.deleteMessage(eventId, messageId, user.getUserId());

        messagingTemplate.convertAndSend(
                "/topic/event/" + eventId + "/deletions", messageId);

        return ResponseEntity.noContent().build();
    }
}
