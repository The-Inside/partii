package com.theinside.partii.enums;

/**
 * Tracks the payment status for an attendee's contribution to event costs.
 */
public enum PaymentStatus {
    /**
     * No payment has been made.
     */
    UNPAID,

    /**
     * Partial payment received.
     */
    PARTIAL,

    /**
     * Full payment received.
     */
    PAID
}
