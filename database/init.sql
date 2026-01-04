-- Sports Match Tracker Database Schema
-- SQLite initialization script

-- Users table
CREATE TABLE IF NOT EXISTS uzytkownik (
    email TEXT PRIMARY KEY,
    nick TEXT NOT NULL,
    haslo TEXT NOT NULL
);

-- Sample data for testing
INSERT OR IGNORE INTO uzytkownik (email, nick, haslo) VALUES 
    ('test@example.com', 'macbak', 'password123'),
    ('admin@example.com', 'admin', 'admin123');
