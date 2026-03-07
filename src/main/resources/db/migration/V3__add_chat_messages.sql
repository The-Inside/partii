-- V3: Add chat_messages table for per-event group chat

CREATE TABLE chat_messages (
    id BIGSERIAL PRIMARY KEY,
    event_id BIGINT NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    sender_id BIGINT NOT NULL REFERENCES users(id),
    content VARCHAR(2000) NOT NULL,
    sent_at TIMESTAMP WITH TIME ZONE NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT chat_messages_content_length CHECK (char_length(content) >= 1)
);

CREATE INDEX idx_chat_messages_event ON chat_messages(event_id);
CREATE INDEX idx_chat_messages_sender ON chat_messages(sender_id);
CREATE INDEX idx_chat_messages_sent_at ON chat_messages(event_id, sent_at DESC);
