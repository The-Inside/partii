package com.theinside.partii.repository;

import com.theinside.partii.entity.EventAttendee;
import com.theinside.partii.enums.AttendeeStatus;
import com.theinside.partii.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for EventAttendee entity with custom query methods.
 */
@Repository
public interface EventAttendeeRepository extends JpaRepository<EventAttendee, UUID> {

    // ===== Basic Queries =====

    List<EventAttendee> findByEventId(UUID eventId);

    Page<EventAttendee> findByEventId(UUID eventId, Pageable pageable);

    List<EventAttendee> findByUserId(Long userId);

    Page<EventAttendee> findByUserId(Long userId, Pageable pageable);

    Optional<EventAttendee> findByEventIdAndUserId(UUID eventId, Long userId);

    boolean existsByEventIdAndUserId(UUID eventId, Long userId);

    // ===== Status Queries =====

    List<EventAttendee> findByEventIdAndStatus(UUID eventId, AttendeeStatus status);

    Page<EventAttendee> findByEventIdAndStatus(UUID eventId, AttendeeStatus status, Pageable pageable);

    List<EventAttendee> findByUserIdAndStatus(Long userId, AttendeeStatus status);

    @Query("""
        SELECT ea FROM EventAttendee ea
        WHERE ea.event.id = :eventId
        AND ea.status IN :statuses
        ORDER BY ea.joinedAt ASC
        """)
    List<EventAttendee> findByEventIdAndStatusIn(
        @Param("eventId") UUID eventId,
        @Param("statuses") List<AttendeeStatus> statuses
    );

    // ===== Count Queries =====

    long countByEventId(UUID eventId);

    long countByEventIdAndStatus(UUID eventId, AttendeeStatus status);

    @Query("SELECT COUNT(ea) FROM EventAttendee ea WHERE ea.event.id = :eventId AND ea.status = 'APPROVED'")
    long countApprovedAttendees(@Param("eventId") UUID eventId);

    // ===== Payment Queries =====

    List<EventAttendee> findByEventIdAndPaymentStatus(UUID eventId, PaymentStatus paymentStatus);

    @Query("""
        SELECT ea FROM EventAttendee ea
        WHERE ea.event.id = :eventId
        AND ea.status = 'APPROVED'
        AND ea.paymentStatus != 'PAID'
        ORDER BY ea.joinedAt ASC
        """)
    List<EventAttendee> findUnpaidAttendees(@Param("eventId") UUID eventId);

    @Query("""
        SELECT SUM(ea.amountPaid) FROM EventAttendee ea
        WHERE ea.event.id = :eventId
        AND ea.status = 'APPROVED'
        """)
    BigDecimal sumAmountPaidByEvent(@Param("eventId") UUID eventId);

    @Query("""
        SELECT SUM(ea.paymentAmount) FROM EventAttendee ea
        WHERE ea.event.id = :eventId
        AND ea.status = 'APPROVED'
        """)
    BigDecimal sumPaymentAmountByEvent(@Param("eventId") UUID eventId);

    // ===== Waitlist Queries =====

    @Query("""
        SELECT ea FROM EventAttendee ea
        WHERE ea.event.id = :eventId
        AND ea.status = 'WAITLIST'
        ORDER BY ea.joinedAt ASC
        """)
    List<EventAttendee> findWaitlistByEventId(@Param("eventId") UUID eventId);

    @Query("""
        SELECT ea FROM EventAttendee ea
        WHERE ea.event.id = :eventId
        AND ea.status = 'WAITLIST'
        ORDER BY ea.joinedAt ASC
        LIMIT 1
        """)
    Optional<EventAttendee> findFirstInWaitlist(@Param("eventId") UUID eventId);

    // ===== User Event Participation =====

    /**
     * Find all events a user is actively participating in.
     */
    @Query("""
        SELECT ea FROM EventAttendee ea
        JOIN FETCH ea.event e
        WHERE ea.user.id = :userId
        AND ea.status = 'APPROVED'
        AND e.status IN ('ACTIVE', 'FULL')
        ORDER BY e.eventDate ASC
        """)
    List<EventAttendee> findActiveParticipationsByUser(@Param("userId") Long userId);

    /**
     * Find all pending join requests for a user.
     */
    @Query("""
        SELECT ea FROM EventAttendee ea
        JOIN FETCH ea.event e
        WHERE ea.user.id = :userId
        AND ea.status = 'PENDING'
        ORDER BY ea.joinedAt DESC
        """)
    List<EventAttendee> findPendingRequestsByUser(@Param("userId") Long userId);

    /**
     * Find all past events a user attended.
     */
    @Query("""
        SELECT ea FROM EventAttendee ea
        JOIN FETCH ea.event e
        WHERE ea.user.id = :userId
        AND ea.status = 'APPROVED'
        AND e.status IN ('PAST', 'ARCHIVED')
        ORDER BY e.eventDate DESC
        """)
    Page<EventAttendee> findPastParticipationsByUser(@Param("userId") Long userId, Pageable pageable);

    // ===== Organizer Queries =====

    /**
     * Find all pending requests for events organized by a user.
     */
    @Query("""
        SELECT ea FROM EventAttendee ea
        JOIN FETCH ea.event e
        JOIN FETCH ea.user u
        WHERE e.organizer.id = :organizerId
        AND ea.status = 'PENDING'
        ORDER BY ea.joinedAt ASC
        """)
    List<EventAttendee> findPendingRequestsForOrganizer(@Param("organizerId") Long organizerId);

    /**
     * Count pending requests for all events organized by a user.
     */
    @Query("""
        SELECT COUNT(ea) FROM EventAttendee ea
        WHERE ea.event.organizer.id = :organizerId
        AND ea.status = 'PENDING'
        """)
    long countPendingRequestsForOrganizer(@Param("organizerId") Long organizerId);

    // ===== Deletion =====

    void deleteByEventId(UUID eventId);

    void deleteByEventIdAndUserId(UUID eventId, Long userId);
}
