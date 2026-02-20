-- ============================================
-- Database Migration Script for Email OTP Feature
-- ============================================
-- Run this script in your PostgreSQL database to add email verification columns
-- Execute this in Render's PostgreSQL database console or via psql
-- ============================================

-- Add email column to users table (if it doesn't exist)
ALTER TABLE users ADD COLUMN IF NOT EXISTS email VARCHAR(255);

-- Add email_verified column to users table (nullable)
ALTER TABLE users ADD COLUMN IF NOT EXISTS email_verified BOOLEAN;

-- Add email_verified column to sellers table (nullable)
ALTER TABLE sellers ADD COLUMN IF NOT EXISTS email_verified BOOLEAN;

-- Set default value for existing rows (set to false for unverified users)
UPDATE users SET email_verified = false WHERE email_verified IS NULL;
UPDATE sellers SET email_verified = false WHERE email_verified IS NULL;

-- Create email_otps table (if it doesn't exist - Hibernate should create this, but adding for safety)
CREATE TABLE IF NOT EXISTS email_otps (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    otp VARCHAR(6) NOT NULL,
    user_type VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used BOOLEAN NOT NULL DEFAULT false
);

-- Create index on email and user_type for faster lookups
CREATE INDEX IF NOT EXISTS idx_email_otp_email_user_type ON email_otps(email, user_type);
CREATE INDEX IF NOT EXISTS idx_email_otp_expires_at ON email_otps(expires_at);

-- Verify the changes
SELECT 
    'users table columns:' as info,
    column_name, 
    data_type, 
    is_nullable
FROM information_schema.columns 
WHERE table_name = 'users' 
AND column_name IN ('email', 'email_verified')
ORDER BY column_name;

SELECT 
    'sellers table columns:' as info,
    column_name, 
    data_type, 
    is_nullable
FROM information_schema.columns 
WHERE table_name = 'sellers' 
AND column_name = 'email_verified'
ORDER BY column_name;

SELECT 
    'email_otps table exists:' as info,
    EXISTS (
        SELECT 1 
        FROM information_schema.tables 
        WHERE table_name = 'email_otps'
    ) as table_exists;

-- ============================================
-- Migration Complete!
-- ============================================
-- After running this script, restart your application
-- The application should now work correctly with email OTP verification
-- ============================================
