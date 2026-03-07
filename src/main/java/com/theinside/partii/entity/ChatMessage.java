package com.theinside.partii.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "chat_messages", indexes = {
    @Index(name = "idx_chat_messages_event", columnList = "event_id"),
    @Index(name = "idx_chat_messages_sender", columnList = "sender_id"),
    @Index(name = "idx_chat_messages_sent_at", columnList = "event_id, sent_at DESC")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @NotBlank(message = "Message content is required")
    @Size(max = 2000, message = "Message cannot exceed 2000 characters")
    @Column(nullable = false, length = 2000)
    private String content;

    @Column(name = "sent_at", nullable = false, updatable = false)
    private Instant sentAt;

    @Builder.Default
    @Column(nullable = false)
    private boolean deleted = false;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @PrePersist
    protected void onCreate() {
        sentAt = Instant.now();
    }

    public void softDelete() {
        this.deleted = true;
        this.deletedAt = Instant.now();
    }
}
