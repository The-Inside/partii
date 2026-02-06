package com.theinside.partii.enums;

/**
 * Determines who can discover and view an event.
 */
public enum EventVisibility {
    /**
     * Event is discoverable via search and feed.
     */
    PUBLIC,

    /**
     * Event is only accessible via direct link or code.
     */
    PRIVATE
}
