package com.theinside.partii.dto;

import java.time.Instant;

public record ChatMessageResponse(
    Long id,
    Long eventId,
    Long senderId,
    String senderDisplayName,
    String senderProfilePictureUrl,
    String content,
    Instant sentAt,
    boolean deleted
) {}
