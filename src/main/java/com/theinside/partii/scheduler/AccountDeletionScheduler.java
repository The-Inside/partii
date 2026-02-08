package com.theinside.partii.scheduler;

import com.theinside.partii.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler for account deletion tasks.
 * Handles purging expired accounts and updating deletion schedules.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AccountDeletionScheduler {

    private final AccountService accountService;

    /**
     * Purge accounts that have completed their grace period.
     * Runs daily at midnight UTC.
     */
    @Scheduled(cron = "0 0 0 * * ?", zone = "UTC")
    public void purgeExpiredAccounts() {
        try {
            log.info("Starting scheduled task: purge expired accounts");
            long purgedCount = accountService.purgeExpiredAccounts();
            log.info("Purged {} expired accounts", purgedCount);
        } catch (Exception e) {
            log.error("Error during account purging", e);
        }
    }

    /**
     * Update account deletion schedules.
     * Runs daily at 1 AM UTC (1 hour after purging).
     */
    @Scheduled(cron = "0 0 1 * * ?", zone = "UTC")
    public void updateDeletionSchedules() {
        try {
            log.info("Starting scheduled task: update account deletion schedules");
            long updatedCount = accountService.updateAccountDeletionSchedules();
            log.info("Updated {} account deletion schedules", updatedCount);
        } catch (Exception e) {
            log.error("Error during deletion schedule update", e);
        }
    }
}
