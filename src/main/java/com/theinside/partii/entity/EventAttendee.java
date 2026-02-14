package com.theinside.partii.entity;

import com.theinside.partii.enums.AttendeeStatus;
import com.theinside.partii.enums.PaymentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Represents a user's participation in an event.
 * Tracks join request status and payment information.
 */
@Entity
@Table(
    name = "event_attendees",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_event_attendee_event_user",
            columnNames = {"event_id", "user_id"}
        )
    },
    indexes = {
        @Index(name = "idx_event_attendees_event", columnList = "event_id"),
        @Index(name = "idx_event_attendees_user", columnList = "user_id"),
        @Index(name = "idx_event_attendees_status", columnList = "status"),
        @Index(name = "idx_event_attendees_payment", columnList = "payment_status")
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventAttendee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull(message = "Attendee status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    @Builder.Default
    private AttendeeStatus status = AttendeeStatus.PENDING;

    /**
     * The amount this attendee is expected to pay.
     */
    @DecimalMin(value = "0.0", message = "Payment amount cannot be negative")
    @Digits(integer = 10, fraction = 2, message = "Invalid payment amount format")
    @Column(name = "payment_amount", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal paymentAmount = BigDecimal.ZERO;

    @NotNull(message = "Payment status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 10)
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.UNPAID;

    /**
     * The amount this attendee has actually paid.
     */
    @DecimalMin(value = "0.0", message = "Amount paid cannot be negative")
    @Digits(integer = 10, fraction = 2, message = "Invalid amount paid format")
    @Column(name = "amount_paid", precision = 12, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal amountPaid = BigDecimal.ZERO;

    /**
     * When the join request was submitted.
     */
    @Column(name = "joined_at", nullable = false, updatable = false)
    private Instant joinedAt;

    /**
     * When the organizer approved the request (null if not approved).
     */
    @Column(name = "approved_at")
    private Instant approvedAt;

    /**
     * Notes about the attendee or their payment (visible to organizer).
     */
    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    @Column(length = 500)
    private String notes;

    @PrePersist
    protected void onCreate() {
        joinedAt = Instant.now();
    }

    /**
     * Approves the attendee's join request.
     */
    public void approve() {
        this.status = AttendeeStatus.APPROVED;
        this.approvedAt = Instant.now();
    }

    /**
     * Declines the attendee's join request.
     */
    public void decline() {
        this.status = AttendeeStatus.DECLINED;
    }

    /**
     * Moves the attendee to the waitlist.
     */
    public void waitlist() {
        this.status = AttendeeStatus.WAITLIST;
    }

    /**
     * Removes an approved attendee from the event.
     */
    public void remove() {
        this.status = AttendeeStatus.REMOVED;
    }

    /**
     * Records a payment from the attendee.
     * @param amount The amount paid
     */
    public void recordPayment(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        this.amountPaid = this.amountPaid.add(amount);

        if (this.paymentAmount.compareTo(BigDecimal.ZERO) > 0) {
            if (this.amountPaid.compareTo(this.paymentAmount) >= 0) {
                this.paymentStatus = PaymentStatus.PAID;
            } else if (this.amountPaid.compareTo(BigDecimal.ZERO) > 0) {
                this.paymentStatus = PaymentStatus.PARTIAL;
            }
        }
    }

    /**
     * Checks if the attendee has been approved.
     */
    public boolean isApproved() {
        return status == AttendeeStatus.APPROVED;
    }

    /**
     * Checks if the attendee is actively participating (approved and not removed).
     */
    public boolean isActiveParticipant() {
        return status == AttendeeStatus.APPROVED;
    }

    /**
     * Calculates the remaining payment amount.
     */
    public BigDecimal getRemainingPayment() {
        if (paymentAmount == null || amountPaid == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal remaining = paymentAmount.subtract(amountPaid);
        return remaining.max(BigDecimal.ZERO);
    }
}
