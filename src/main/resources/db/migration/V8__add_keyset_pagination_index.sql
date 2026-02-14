-- V8: Add composite index for keyset pagination
-- Enables O(1) cursor-based pagination for public event discovery

CREATE INDEX IF NOT EXISTS idx_events_keyset_public
    ON events(event_date ASC, id ASC)
    WHERE visibility = 'PUBLIC' AND status IN ('ACTIVE', 'FULL');

CREATE INDEX IF NOT EXISTS idx_events_keyset_admin
    ON events(created_at DESC, id ASC);
