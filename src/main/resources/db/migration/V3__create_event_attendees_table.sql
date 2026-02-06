-- V3: Create event_attendees table
-- Tracks user participation in events including join requests and payments

CREATE TABLE IF NOT EXISTS event_attendees (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    status VARCHAR(15) NOT NULL DEFAULT 'PENDING',
    payment_amount DECIMAL(12, 2) DEFAULT 0,
    payment_status VARCHAR(10) NOT NULL DEFAULT 'UNPAID',
    amount_paid DECIMAL(12, 2) NOT NULL DEFAULT 0,
    joined_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    approved_at TIMESTAMP WITH TIME ZONE,
    notes VARCHAR(500),

    CONSTRAINT uk_event_attendee_event_user UNIQUE (event_id, user_id),
    CONSTRAINT chk_attendee_status CHECK (status IN ('PENDING', 'APPROVED', 'WAITLIST', 'DECLINED', 'REMOVED')),
    CONSTRAINT chk_payment_status CHECK (payment_status IN ('UNPAID', 'PARTIAL', 'PAID')),
    CONSTRAINT chk_payment_amount CHECK (payment_amount >= 0),
    CONSTRAINT chk_amount_paid CHECK (amount_paid >= 0)
);

-- Indexes for common queries
CREATE INDEX IF NOT EXISTS idx_event_attendees_event ON event_attendees(event_id);
CREATE INDEX IF NOT EXISTS idx_event_attendees_user ON event_attendees(user_id);
CREATE INDEX IF NOT EXISTS idx_event_attendees_status ON event_attendees(status);
CREATE INDEX IF NOT EXISTS idx_event_attendees_payment ON event_attendees(payment_status);
CREATE INDEX IF NOT EXISTS idx_event_attendees_joined_at ON event_attendees(joined_at);

-- Composite index for approved attendees
CREATE INDEX IF NOT EXISTS idx_event_attendees_approved ON event_attendees(event_id, status)
    WHERE status = 'APPROVED';

-- Composite index for waitlist ordering
CREATE INDEX IF NOT EXISTS idx_event_attendees_waitlist ON event_attendees(event_id, joined_at)
    WHERE status = 'WAITLIST';

-- Comments
COMMENT ON TABLE event_attendees IS 'Tracks user participation and payment status for events';
COMMENT ON COLUMN event_attendees.status IS 'PENDING, APPROVED, WAITLIST, DECLINED, REMOVED';
COMMENT ON COLUMN event_attendees.payment_status IS 'UNPAID, PARTIAL, PAID';
