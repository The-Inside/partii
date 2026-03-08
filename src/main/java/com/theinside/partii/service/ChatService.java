package com.theinside.partii.service;

import com.theinside.partii.dto.ChatMessageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ChatService {

    ChatMessageResponse sendMessage(Long eventId, Long userId, String content);

    Page<ChatMessageResponse> getMessages(Long eventId, Long userId, Pageable pageable);

    void deleteMessage(Long eventId, Long messageId, Long organizerId);
}
