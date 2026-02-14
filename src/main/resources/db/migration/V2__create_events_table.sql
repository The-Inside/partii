-- V2: Create events table
-- Core event entity for the event collaboration platform

CREATE TABLE IF NOT EXISTS events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organizer_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(100) NOT NULL,
    description VARCHAR(2000),
    event_type VARCHAR(20) NOT NULL,
    location_address VARCHAR(500),
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    event_date TIMESTAMP NOT NULL,
    image_url VARCHAR(500),
    estimated_budget DECIMAL(12, 2),
    currency VARCHAR(3) DEFAULT 'GHS',
    max_attendees INTEGER NOT NULL DEFAULT 10,
    current_attendees INTEGER NOT NULL DEFAULT 0,
    age_restriction INTEGER,
    payment_deadline TIMESTAMP,
    join_deadline TIMESTAMP,
    visibility VARCHAR(10) NOT NULL DEFAULT 'PUBLIC',
    status VARCHAR(15) NOT NULL DEFAULT 'DRAFT',
    private_link_code VARCHAR(10) UNIQUE,
    link_expiration TIMESTAMP,
    cancellation_reason VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_events_visibility CHECK (visibility IN ('PUBLIC', 'PRIVATE')),
    CONSTRAINT chk_events_status CHECK (status IN ('DRAFT', 'ACTIVE', 'FULL', 'PAST', 'CANCELLED', 'ARCHIVED')),
    CONSTRAINT chk_events_event_type CHECK (event_type IN ('PARTY', 'DINNER', 'TRIP', 'SPORTS', 'GAME_NIGHT', 'CONCERT', 'FESTIVAL', 'BIRTHDAY', 'WEDDING', 'GRADUATION', 'NETWORKING', 'WORKSHOP', 'OTHER')),
    CONSTRAINT chk_events_max_attendees CHECK (max_attendees >= 2 AND max_attendees <= 10000),
    CONSTRAINT chk_events_current_attendees CHECK (current_attendees >= 0),
    CONSTRAINT chk_events_latitude CHECK (latitude IS NULL OR (latitude >= -90 AND latitude <= 90)),
    CONSTRAINT chk_events_longitude CHECK (longitude IS NULL OR (longitude >= -180 AND longitude <= 180))
);

-- Indexes for common queries
CREATE INDEX IF NOT EXISTS idx_events_organizer ON events(organizer_id);
CREATE INDEX IF NOT EXISTS idx_events_status ON events(status);
CREATE INDEX IF NOT EXISTS idx_events_visibility ON events(visibility);
CREATE INDEX IF NOT EXISTS idx_events_event_date ON events(event_date);
CREATE INDEX IF NOT EXISTS idx_events_location ON events(latitude, longitude);
CREATE INDEX IF NOT EXISTS idx_events_private_link ON events(private_link_code);
CREATE INDEX IF NOT EXISTS idx_events_created_at ON events(created_at);

-- Composite index for public event discovery
CREATE INDEX IF NOT EXISTS idx_events_public_active ON events(visibility, status, event_date)
    WHERE visibility = 'PUBLIC' AND status IN ('ACTIVE', 'FULL');

-- Comments
COMMENT ON TABLE events IS 'Core events table for organizing and discovering events';
COMMENT ON COLUMN events.private_link_code IS '6-character alphanumeric code for private event access';
COMMENT ON COLUMN events.latitude IS 'Location latitude for nearby event search';
COMMENT ON COLUMN events.longitude IS 'Location longitude for nearby event search';
