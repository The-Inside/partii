package com.theinside.partii.enums;

/**
 * Represents the status of an attendee's participation in an event.
 */
public enum AttendeeStatus {
    /**
     * Join request submitted, awaiting organizer approval.
     */
    PENDING,

    /**
     * Organizer approved the join request.
     */
    APPROVED,

    /**
     * Event is full, user is on the waitlist.
     */
    WAITLIST,

    /**
     * Organizer declined the join request.
     */
    DECLINED,

    /**
     * Attendee was removed by the organizer after being approved.
     */
    REMOVED
}
