-- V4: Create contribution_items table
-- Tracks items and services that attendees can contribute to events

CREATE TABLE IF NOT EXISTS contribution_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(50),
    type VARCHAR(10) NOT NULL,
    quantity INTEGER DEFAULT 1,
    time_commitment INTEGER,
    estimated_cost DECIMAL(12, 2),
    priority VARCHAR(15) NOT NULL DEFAULT 'NICE_TO_HAVE',
    notes VARCHAR(500),
    status VARCHAR(15) NOT NULL DEFAULT 'AVAILABLE',
    assigned_to BIGINT REFERENCES users(id) ON DELETE SET NULL,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    claimed_at TIMESTAMP WITH TIME ZONE,
    confirmed_at TIMESTAMP WITH TIME ZONE,

    CONSTRAINT chk_contribution_type CHECK (type IN ('MATERIAL', 'SERVICE')),
    CONSTRAINT chk_contribution_priority CHECK (priority IN ('MUST_HAVE', 'NICE_TO_HAVE')),
    CONSTRAINT chk_contribution_status CHECK (status IN ('AVAILABLE', 'CLAIMED', 'CONFIRMED')),
    CONSTRAINT chk_contribution_quantity CHECK (quantity >= 1),
    CONSTRAINT chk_contribution_time CHECK (time_commitment IS NULL OR time_commitment >= 0),
    CONSTRAINT chk_contribution_cost CHECK (estimated_cost IS NULL OR estimated_cost >= 0)
);

-- Indexes for common queries
CREATE INDEX IF NOT EXISTS idx_contributions_event ON contribution_items(event_id);
CREATE INDEX IF NOT EXISTS idx_contributions_assigned_to ON contribution_items(assigned_to);
CREATE INDEX IF NOT EXISTS idx_contributions_status ON contribution_items(status);
CREATE INDEX IF NOT EXISTS idx_contributions_category ON contribution_items(category);
CREATE INDEX IF NOT EXISTS idx_contributions_priority ON contribution_items(priority);

-- Composite index for available items
CREATE INDEX IF NOT EXISTS idx_contributions_available ON contribution_items(event_id, status)
    WHERE status = 'AVAILABLE';

-- Index for unclaimed must-have items (for alerts)
CREATE INDEX IF NOT EXISTS idx_contributions_unclaimed_must_have ON contribution_items(event_id, priority)
    WHERE priority = 'MUST_HAVE' AND status = 'AVAILABLE';

-- Comments
COMMENT ON TABLE contribution_items IS 'Items and services that attendees can contribute to events';
COMMENT ON COLUMN contribution_items.type IS 'MATERIAL for physical items, SERVICE for tasks/help';
COMMENT ON COLUMN contribution_items.priority IS 'MUST_HAVE for essential, NICE_TO_HAVE for optional';
COMMENT ON COLUMN contribution_items.status IS 'AVAILABLE, CLAIMED (pending confirmation), CONFIRMED';
