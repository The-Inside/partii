-- V1: Create users table
-- Partii Event Collaboration Platform
-- This migration creates the core users table for authentication and profiles

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255),
    display_name VARCHAR(100) NOT NULL,
    provider VARCHAR(50) NOT NULL,
    provider_id VARCHAR(255) NOT NULL,
    legal_name VARCHAR(100),
    bio TEXT,
    general_location VARCHAR(255) NOT NULL DEFAULT '',
    primary_address VARCHAR(500) NOT NULL DEFAULT '',
    phone_number VARCHAR(20) NOT NULL DEFAULT '',
    dob DATE,
    account_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    is_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    is_admin BOOLEAN NOT NULL DEFAULT FALSE,
    profile_completed BOOLEAN NOT NULL DEFAULT FALSE,
    total_ratings INTEGER NOT NULL DEFAULT 0,
    average_rating INTEGER NOT NULL DEFAULT 0,
    events_attended INTEGER NOT NULL DEFAULT 0,
    events_organized INTEGER NOT NULL DEFAULT 0,
    active_events_count INTEGER NOT NULL DEFAULT 0,
    profile_picture_url VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP WITH TIME ZONE
);

-- Indexes for common queries
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_provider ON users(provider, provider_id);
CREATE INDEX IF NOT EXISTS idx_users_account_status ON users(account_status);
CREATE INDEX IF NOT EXISTS idx_users_created_at ON users(created_at);

-- Comments for documentation
COMMENT ON TABLE users IS 'Core user table for authentication and profile data';
COMMENT ON COLUMN users.provider IS 'Authentication provider: local, google, github';
COMMENT ON COLUMN users.account_status IS 'PENDING, VERIFIED, REJECTED, SUSPENDED';
