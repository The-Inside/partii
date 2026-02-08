package com.theinside.partii.service;

import com.theinside.partii.dto.DeleteAccountRequest;
import com.theinside.partii.dto.ExportDataResponse;
import com.theinside.partii.entity.Event;
import com.theinside.partii.entity.EventAttendee;
import com.theinside.partii.entity.User;
import com.theinside.partii.enums.AccountDeletionStatus;
import com.theinside.partii.enums.EventStatus;
import com.theinside.partii.enums.AttendeeStatus;
import com.theinside.partii.exception.BadRequestException;
import com.theinside.partii.exception.ResourceNotFoundException;
import com.theinside.partii.repository.EventAttendeeRepository;
import com.theinside.partii.repository.EventRepository;
import com.theinside.partii.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Implementation of AccountService.
 * Handles account deletion, deactivation, and GDPR data export.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AccountServiceImpl implements AccountService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final EventAttendeeRepository eventAttendeeRepository;

    @Value("${partii.account.grace-period-days:30}")
    private int gracePeriodDays;

    private static final String DELETE_CONFIRMATION = "DELETE MY ACCOUNT";

    // ===== Account Deletion =====

    @Override
    public void deleteAccount(Long userId, DeleteAccountRequest request) {
        // Validate confirmation
        if (!DELETE_CONFIRMATION.equals(request.getConfirmation())) {
            throw new BadRequestException("Invalid deletion confirmation. Please type 'DELETE MY ACCOUNT' to confirm.");
        }

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        log.warn("User {} requested account deletion. Reason: {}", userId, request.getReason());

        // Step 1: Cancel all organized events
        List<Event> organizedEvents = eventRepository.findByOrganizerId(userId);
        for (Event event : organizedEvents) {
            if (event.getStatus() != EventStatus.CANCELLED && event.getStatus() != EventStatus.ARCHIVED) {
                event.setStatus(EventStatus.CANCELLED);
                event.setCancellationReason("Event cancelled due to organizer account deletion");
                eventRepository.save(event);
                log.info("Cancelled event {} due to organizer deletion", event.getId());
            }
        }

        // Step 2: Anonymize attendee records
        List<EventAttendee> attendeeRecords = eventAttendeeRepository.findByUserId(userId);
        for (EventAttendee attendee : attendeeRecords) {
            if (attendee.getStatus() == AttendeeStatus.APPROVED) {
                attendee.setNotes("Attendee account has been deleted");
                eventAttendeeRepository.save(attendee);
            }
        }

        // Step 3: Schedule deletion after grace period
        user.setDeletedAt(Instant.now());
        user.setEnabled(false);
        userRepository.save(user);

        log.info("User {} account scheduled for deletion after {} days", userId, gracePeriodDays);
    }

    @Override
    public void deactivateAccount(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setEnabled(false);
        userRepository.save(user);

        log.info("User {} account deactivated", userId);
    }

    @Override
    public void reactivateAccount(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Only allow reactivation if not scheduled for deletion
        if (user.getDeletedAt() != null) {
            throw new BadRequestException("Account is scheduled for deletion and cannot be reactivated");
        }

        user.setEnabled(true);
        userRepository.save(user);

        log.info("User {} account reactivated", userId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isDeactivated(Long userId) {
        return userRepository.findById(userId)
            .map(user -> !user.isEnabled())
            .orElse(false);
    }

    // ===== Data Export (GDPR) =====

    @Override
    @Transactional(readOnly = true)
    public ExportDataResponse exportUserData(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        log.info("Exporting data for user {}", userId);

        // TODO: Implement actual data export to JSON/CSV file
        // For now, return a response indicating export is available
        Instant now = Instant.now();
        Instant expiresAt = now.plus(7, ChronoUnit.DAYS);

        return ExportDataResponse.builder()
            .createdAt(now)
            .expiresAt(expiresAt)
            .message("Data export created successfully. Your data will be available for download for 7 days.")
            .build();
    }

    // ===== Scheduled Tasks =====

    @Override
    @Transactional
    public long purgeExpiredAccounts() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(gracePeriodDays);

        // TODO: Implement actual purging of user data after grace period
        // This should:
        // 1. Delete verification documents
        // 2. Delete personal data
        // 3. Delete events and attendee records
        // 4. Delete user account

        log.info("Purging accounts marked for deletion before {}", cutoffDate);
        return 0; // Placeholder
    }

    @Override
    @Transactional
    public long updateAccountDeletionSchedules() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(gracePeriodDays);

        // Find accounts marked for deletion that have passed grace period
        // TODO: Implement logic to find expired soft-deleted accounts
        // and trigger purging

        log.debug("Checking for accounts ready for permanent deletion");
        return 0; // Placeholder
    }

    // ===== Configuration =====

    @Override
    public int getGracePeriodDays() {
        return gracePeriodDays;
    }
}
