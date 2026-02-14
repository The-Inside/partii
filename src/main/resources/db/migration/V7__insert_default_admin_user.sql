-- V7: Insert default admin user
-- Creates a default admin user for system management
-- Default password: admin123 (should be changed after first login)
-- Password hash generated using BCrypt with strength 10
-- Account status mapping: 0=PENDING, 1=VERIFIED, 2=REJECTED, 3=SUSPENDED

INSERT INTO users (
    email,
    password,
    display_name,
    provider,
    provider_id,
    legal_name,
    bio,
    general_location,
    primary_address,
    phone_number,
    dob,
    account_status,
    is_verified,
    is_enabled,
    is_admin,
    profile_completed,
    total_ratings,
    average_rating,
    events_attended,
    events_organized,
    active_events_count,
    profile_picture_url,
    created_at,
    updated_at,
    deleted_at
) VALUES (
    'admin@partii.com',
    -- BCrypt hash for 'admin123' - CHANGE THIS AFTER FIRST LOGIN!
    '$2b$12$pU3jzOaUBfcpV6UDGK80FugGIkKS.PjUBOcL5MGn0oIrrxuWukyqu',  -- BCrypt with 12 rounds
    'System Administrator',
    'local',
    'admin_local',
    'Partii Admin',
    'Default system administrator account.',
    'System',
    'Partii Headquarters',
    '+1234567890',
    '1990-01-01',
    1,  -- VERIFIED = 1
    true,
    true,
    true,  -- is_admin = true
    true,
    0,
    0,
    0,
    0,
    0,
    null,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    null
) ON CONFLICT (email) DO NOTHING;

-- Add comment for security reminder
COMMENT ON TABLE users IS 'Core user table - Default admin: admin@partii.com / admin123 (CHANGE IMMEDIATELY!)';
