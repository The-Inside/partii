package com.theinside.partii.service;

import com.theinside.partii.dto.DeleteAccountRequest;
import com.theinside.partii.dto.ExportDataResponse;

/**
 * Service interface for account management operations.
 * Handles account deletion, deactivation, and GDPR data export.
 */
public interface AccountService {

    /**
     * Delete a user account after a grace period.
     *
     * Process:
     * 1. Cancel all organized events
     * 2. Anonymize attendee records
     * 3. Schedule deletion after 30-day grace period
     * 4. Delete verification documents immediately
     * 5. After grace period, delete all personal data
     *
     * @param userId The user ID to delete
     * @param request Deletion confirmation request
     * @throws IllegalArgumentException if confirmation is invalid
     */
    void deleteAccount(Long userId, DeleteAccountRequest request);

    /**
     * Deactivate a user account (temporary).
     * Account can be reactivated by logging in.
     *
     * @param userId The user ID to deactivate
     */
    void deactivateAccount(Long userId);

    /**
     * Reactivate a previously deactivated account.
     *
     * @param userId The user ID to reactivate
     */
    void reactivateAccount(Long userId);

    /**
     * Check if an account is deactivated.
     *
     * @param userId The user ID to check
     * @return true if account is deactivated
     */
    boolean isDeactivated(Long userId);

    /**
     * Export user's personal data for GDPR compliance.
     * Returns a downloadable file containing all user data.
     *
     * @param userId The user ID to export data for
     * @return Export data with download URL
     */
    ExportDataResponse exportUserData(Long userId);

    /**
     * Purge all data for accounts that have completed the grace period.
     * Called by a scheduled task daily.
     *
     * @return Number of accounts purged
     */
    long purgeExpiredAccounts();

    /**
     * Update account deletion status to reflect when grace period ends.
     * Called daily to check for accounts ready for purging.
     *
     * @return Number of accounts scheduled for deletion
     */
    long updateAccountDeletionSchedules();

    /**
     * Get the grace period in days before permanent deletion.
     * Default: 30 days
     *
     * @return Grace period in days
     */
    int getGracePeriodDays();
}
