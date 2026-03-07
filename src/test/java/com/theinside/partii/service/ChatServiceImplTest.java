package com.theinside.partii.service;

import com.theinside.partii.dto.ChatMessageResponse;
import com.theinside.partii.entity.ChatMessage;
import com.theinside.partii.entity.Event;
import com.theinside.partii.entity.User;
import com.theinside.partii.enums.AttendeeStatus;
import com.theinside.partii.enums.EventStatus;
import com.theinside.partii.exception.NotFoundException;
import com.theinside.partii.exception.UnauthorizedException;
import com.theinside.partii.repository.ChatMessageRepository;
import com.theinside.partii.repository.EventAttendeeRepository;
import com.theinside.partii.repository.EventRepository;
import com.theinside.partii.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceImplTest {

    @Mock EventRepository eventRepository;
    @Mock ChatMessageRepository chatMessageRepository;
    @Mock EventAttendeeRepository eventAttendeeRepository;
    @Mock UserRepository userRepository;
    @InjectMocks ChatServiceImpl service;

    private User organizer;
    private User approvedAttendee;
    private User pendingUser;
    private Event event;

    @BeforeEach
    void setUp() {
        organizer = User.builder().id(1L).displayName("Organizer").profilePictureUrl("pic1.jpg").build();
        approvedAttendee = User.builder().id(2L).displayName("Attendee").profilePictureUrl("pic2.jpg").build();
        pendingUser = User.builder().id(3L).displayName("Pending").build();
        event = Event.builder().id(1L).organizer(organizer).status(EventStatus.ACTIVE).build();
    }

    // ===== sendMessage =====

    @Test
    void sendMessage_asOrganizer_success() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(userRepository.findById(1L)).thenReturn(Optional.of(organizer));
        when(chatMessageRepository.save(any(ChatMessage.class)))
                .thenAnswer(invocation -> {
                    ChatMessage msg = invocation.getArgument(0);
                    msg.setId(10L);
                    msg.setSentAt(Instant.now());
                    return msg;
                });

        ChatMessageResponse response = service.sendMessage(1L, 1L, "Hello everyone!");

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.eventId()).isEqualTo(1L);
        assertThat(response.senderId()).isEqualTo(1L);
        assertThat(response.senderDisplayName()).isEqualTo("Organizer");
        assertThat(response.content()).isEqualTo("Hello everyone!");
        assertThat(response.deleted()).isFalse();
        verify(chatMessageRepository).save(any(ChatMessage.class));
    }

    @Test
    void sendMessage_asApprovedAttendee_success() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(userRepository.findById(2L)).thenReturn(Optional.of(approvedAttendee));
        when(eventAttendeeRepository.existsByEventIdAndUserIdAndStatus(1L, 2L, AttendeeStatus.APPROVED))
                .thenReturn(true);
        when(chatMessageRepository.save(any(ChatMessage.class)))
                .thenAnswer(invocation -> {
                    ChatMessage msg = invocation.getArgument(0);
                    msg.setId(11L);
                    msg.setSentAt(Instant.now());
                    return msg;
                });

        ChatMessageResponse response = service.sendMessage(1L, 2L, "Hi!");

        assertThat(response.senderId()).isEqualTo(2L);
        assertThat(response.content()).isEqualTo("Hi!");
        verify(eventAttendeeRepository).existsByEventIdAndUserIdAndStatus(1L, 2L, AttendeeStatus.APPROVED);
    }

    @Test
    void sendMessage_asPendingUser_throwsUnauthorized() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(userRepository.findById(3L)).thenReturn(Optional.of(pendingUser));
        when(eventAttendeeRepository.existsByEventIdAndUserIdAndStatus(1L, 3L, AttendeeStatus.APPROVED))
                .thenReturn(false);

        assertThatThrownBy(() -> service.sendMessage(1L, 3L, "Can I chat?"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("approved attendee");
    }

    @Test
    void sendMessage_eventNotFound_throwsNotFound() {
        when(eventRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.sendMessage(99L, 1L, "Hello"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Event not found");
    }

    // ===== getMessages =====

    @Test
    void getMessages_asApprovedAttendee_returnsPage() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(eventAttendeeRepository.existsByEventIdAndUserIdAndStatus(1L, 2L, AttendeeStatus.APPROVED))
                .thenReturn(true);

        ChatMessage msg = ChatMessage.builder()
                .id(10L).event(event).sender(organizer)
                .content("Test message").sentAt(Instant.now()).deleted(false).build();

        Pageable pageable = PageRequest.of(0, 50);
        when(chatMessageRepository.findByEventIdAndDeletedFalseOrderBySentAtDesc(1L, pageable))
                .thenReturn(new PageImpl<>(List.of(msg)));

        Page<ChatMessageResponse> result = service.getMessages(1L, 2L, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().content()).isEqualTo("Test message");
    }

    @Test
    void getMessages_asUnauthorizedUser_throwsUnauthorized() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(eventAttendeeRepository.existsByEventIdAndUserIdAndStatus(1L, 3L, AttendeeStatus.APPROVED))
                .thenReturn(false);

        Pageable pageable = PageRequest.of(0, 50);

        assertThatThrownBy(() -> service.getMessages(1L, 3L, pageable))
                .isInstanceOf(UnauthorizedException.class);
    }

    // ===== deleteMessage =====

    @Test
    void deleteMessage_asOrganizer_success() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        ChatMessage msg = ChatMessage.builder()
                .id(10L).event(event).sender(approvedAttendee)
                .content("Some message").sentAt(Instant.now()).deleted(false).build();
        when(chatMessageRepository.findByIdAndEventId(10L, 1L)).thenReturn(Optional.of(msg));
        when(chatMessageRepository.save(any(ChatMessage.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.deleteMessage(1L, 10L, 1L);

        assertThat(msg.isDeleted()).isTrue();
        assertThat(msg.getDeletedAt()).isNotNull();
        verify(chatMessageRepository).save(msg);
    }

    @Test
    void deleteMessage_asNonOrganizer_throwsUnauthorized() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> service.deleteMessage(1L, 10L, 2L))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("organizer");
    }

    @Test
    void deleteMessage_messageNotFound_throwsNotFound() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(chatMessageRepository.findByIdAndEventId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteMessage(1L, 99L, 1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Message not found");
    }
}
