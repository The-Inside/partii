package com.theinside.partii.enums;

/**
 * Represents the lifecycle status of an event.
 */
public enum EventStatus {
    /**
     * Event is being created, not yet visible to others.
     */
    DRAFT,

    /**
     * Event is live and accepting join requests.
     */
    ACTIVE,

    /**
     * Event has reached maximum capacity.
     */
    FULL,

    /**
     * Event date has passed.
     */
    PAST,

    /**
     * Event was cancelled by the organizer.
     */
    CANCELLED,

    /**
     * Event has been archived (30+ days after completion).
     */
    ARCHIVED
}
