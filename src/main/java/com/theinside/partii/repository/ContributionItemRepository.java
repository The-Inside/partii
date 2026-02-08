package com.theinside.partii.repository;

import com.theinside.partii.entity.ContributionItem;
import com.theinside.partii.enums.ContributionStatus;
import com.theinside.partii.enums.ContributionType;
import com.theinside.partii.enums.Priority;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Repository for ContributionItem entity with custom query methods.
 */
@Repository
public interface ContributionItemRepository extends JpaRepository<ContributionItem, UUID> {

    // ===== Basic Queries =====

    List<ContributionItem> findByEventId(UUID eventId);

    Page<ContributionItem> findByEventId(UUID eventId, Pageable pageable);

    List<ContributionItem> findByAssignedToId(Long userId);

    Page<ContributionItem> findByAssignedToId(Long userId, Pageable pageable);

    // ===== Status Queries =====

    List<ContributionItem> findByEventIdAndStatus(UUID eventId, ContributionStatus status);

    List<ContributionItem> findByEventIdAndStatusIn(UUID eventId, List<ContributionStatus> statuses);

    @Query("""
        SELECT ci FROM ContributionItem ci
        WHERE ci.event.id = :eventId
        AND ci.status = 'AVAILABLE'
        ORDER BY ci.priority ASC, ci.createdAt ASC
        """)
    List<ContributionItem> findAvailableByEventId(@Param("eventId") UUID eventId);

    // ===== Category Queries =====

    List<ContributionItem> findByEventIdAndCategory(UUID eventId, String category);

    @Query("SELECT DISTINCT ci.category FROM ContributionItem ci WHERE ci.event.id = :eventId AND ci.category IS NOT NULL")
    List<String> findDistinctCategoriesByEventId(@Param("eventId") UUID eventId);

    // ===== Type Queries =====

    List<ContributionItem> findByEventIdAndType(UUID eventId, ContributionType type);

    // ===== Priority Queries =====

    List<ContributionItem> findByEventIdAndPriority(UUID eventId, Priority priority);

    @Query("""
        SELECT ci FROM ContributionItem ci
        WHERE ci.event.id = :eventId
        AND ci.priority = 'MUST_HAVE'
        AND ci.status = 'AVAILABLE'
        ORDER BY ci.createdAt ASC
        """)
    List<ContributionItem> findUnclaimedMustHaveItems(@Param("eventId") UUID eventId);

    // ===== Count Queries =====

    long countByEventId(UUID eventId);

    long countByEventIdAndStatus(UUID eventId, ContributionStatus status);

    long countByEventIdAndCompleted(UUID eventId, boolean completed);

    @Query("""
        SELECT COUNT(ci) FROM ContributionItem ci
        WHERE ci.event.id = :eventId
        AND ci.priority = 'MUST_HAVE'
        AND ci.status = 'AVAILABLE'
        """)
    long countUnclaimedMustHaveItems(@Param("eventId") UUID eventId);

    // ===== User's Contributions =====

    @Query("""
        SELECT ci FROM ContributionItem ci
        JOIN FETCH ci.event e
        WHERE ci.assignedTo.id = :userId
        AND ci.status IN ('CLAIMED', 'CONFIRMED')
        AND e.status IN ('ACTIVE', 'FULL')
        ORDER BY e.eventDate ASC
        """)
    List<ContributionItem> findActiveContributionsByUser(@Param("userId") Long userId);

    @Query("""
        SELECT ci FROM ContributionItem ci
        WHERE ci.event.id = :eventId
        AND ci.assignedTo.id = :userId
        """)
    List<ContributionItem> findByEventIdAndAssignedTo(
        @Param("eventId") UUID eventId,
        @Param("userId") Long userId
    );

    // ===== Cost Queries =====

    @Query("SELECT SUM(ci.estimatedCost) FROM ContributionItem ci WHERE ci.event.id = :eventId")
    BigDecimal sumEstimatedCostByEventId(@Param("eventId") UUID eventId);

    @Query("""
        SELECT SUM(ci.estimatedCost) FROM ContributionItem ci
        WHERE ci.event.id = :eventId
        AND ci.status IN ('CLAIMED', 'CONFIRMED')
        """)
    BigDecimal sumClaimedCostByEventId(@Param("eventId") UUID eventId);

    // ===== Completion Queries =====

    List<ContributionItem> findByEventIdAndCompletedTrue(UUID eventId);

    List<ContributionItem> findByEventIdAndCompletedFalse(UUID eventId);

    @Query("""
        SELECT ci FROM ContributionItem ci
        WHERE ci.event.id = :eventId
        AND ci.status = 'CONFIRMED'
        AND ci.completed = false
        ORDER BY ci.priority ASC
        """)
    List<ContributionItem> findPendingCompletionByEventId(@Param("eventId") UUID eventId);

    // ===== Alert Queries =====

    /**
     * Find MUST_HAVE items that are not yet claimed or confirmed,
     * for events happening within the specified number of days.
     */
    @Query("""
        SELECT ci FROM ContributionItem ci
        JOIN ci.event e
        WHERE ci.priority = 'MUST_HAVE'
        AND ci.status = 'AVAILABLE'
        AND e.status = 'ACTIVE'
        AND e.eventDate <= :deadline
        ORDER BY e.eventDate ASC
        """)
    List<ContributionItem> findUnclaimedMustHaveItemsBeforeDeadline(
        @Param("deadline") java.time.LocalDateTime deadline
    );

    // ===== Deletion =====

    void deleteByEventId(UUID eventId);
}
