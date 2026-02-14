package com.theinside.partii.enums;

/**
 * Tracks the account deletion status during the grace period.
 */
public enum AccountDeletionStatus {
    /**
     * Account is active and not scheduled for deletion.
     */
    ACTIVE,

    /**
     * Account is deactivated but can be reactivated.
     */
    DEACTIVATED,

    /**
     * Account is scheduled for deletion after the grace period.
     */
    SCHEDULED_FOR_DELETION
}
