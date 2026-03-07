package com.theinside.partii.service;

import com.theinside.partii.dto.ChatMessageResponse;
import com.theinside.partii.entity.ChatMessage;
import com.theinside.partii.entity.Event;
import com.theinside.partii.entity.User;
import com.theinside.partii.enums.AttendeeStatus;
import com.theinside.partii.exception.NotFoundException;
import com.theinside.partii.exception.UnauthorizedException;
import com.theinside.partii.repository.ChatMessageRepository;
import com.theinside.partii.repository.EventAttendeeRepository;
import com.theinside.partii.repository.EventRepository;
import com.theinside.partii.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class ChatServiceImpl implements ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final EventRepository eventRepository;
    private final EventAttendeeRepository eventAttendeeRepository;
    private final UserRepository userRepository;

    @Override
    public ChatMessageResponse sendMessage(Long eventId, Long userId, String content) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        User sender = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        validateChatAccess(event, userId);

        ChatMessage message = ChatMessage.builder()
                .event(event)
                .sender(sender)
                .content(content)
                .build();

        ChatMessage saved = chatMessageRepository.save(message);
        log.debug("Chat message {} sent by user {} in event {}", saved.getId(), userId, eventId);

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ChatMessageResponse> getMessages(Long eventId, Long userId, Pageable pageable) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        validateChatAccess(event, userId);

        return chatMessageRepository.findByEventIdAndDeletedFalseOrderBySentAtDesc(eventId, pageable)
                .map(this::toResponse);
    }

    @Override
    public void deleteMessage(Long eventId, Long messageId, Long organizerId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        if (!event.getOrganizer().getId().equals(organizerId)) {
            throw new UnauthorizedException("Only the organizer can delete messages");
        }

        ChatMessage message = chatMessageRepository.findByIdAndEventId(messageId, eventId)
                .orElseThrow(() -> new NotFoundException("Message not found"));

        message.softDelete();
        chatMessageRepository.save(message);
        log.info("Chat message {} soft-deleted by organizer {} in event {}", messageId, organizerId, eventId);
    }

    private void validateChatAccess(Event event, Long userId) {
        if (event.getOrganizer().getId().equals(userId)) {
            return;
        }

        boolean isApproved = eventAttendeeRepository.existsByEventIdAndUserIdAndStatus(
                event.getId(), userId, AttendeeStatus.APPROVED);

        if (!isApproved) {
            throw new UnauthorizedException("You must be an approved attendee to access this chat");
        }
    }

    private ChatMessageResponse toResponse(ChatMessage message) {
        User sender = message.getSender();
        return new ChatMessageResponse(
                message.getId(),
                message.getEvent().getId(),
                sender.getId(),
                sender.getDisplayName(),
                sender.getProfilePictureUrl(),
                message.isDeleted() ? null : message.getContent(),
                message.getSentAt(),
                message.isDeleted()
        );
    }
}
