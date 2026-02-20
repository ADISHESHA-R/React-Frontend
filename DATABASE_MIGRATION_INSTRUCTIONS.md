# Database Migration Instructions for Email OTP Feature

## Problem
The application is failing because the `email_verified` columns don't exist in the database. Hibernate's auto-migration failed because it tried to add NOT NULL columns to existing tables with data.

## Solution
Run the SQL migration script to manually add the required columns.

## Steps to Run Migration

### Option 1: Using Render Database Console (Recommended)

1. **Go to Render Dashboard**
   - Navigate to https://dashboard.render.com
   - Find your PostgreSQL database service (usually named `shopping-db` or similar)

2. **Open Database Console**
   - Click on your database service
   - Look for "Connect" or "Info" tab
   - Click on "Connect" or use the "psql" connection option
   - Or use the "Query" / "SQL Editor" if available

3. **Run the Migration Script**
   - Open the file: `shopping/database-migration.sql`
   - Copy the entire SQL script
   - Paste it into the database console
   - Execute the script

4. **Verify Migration**
   - The script includes verification queries at the end
   - Check that all columns were created successfully

5. **Restart Application**
   - After migration completes, restart your Render web service
   - The application should now start successfully

### Option 2: Using psql Command Line

If you have psql installed locally and have the database connection string:

```bash
# Get connection string from Render dashboard
# Format: postgresql://user:password@host:port/database

psql "your-connection-string-here" -f shopping/database-migration.sql
```

### Option 3: Using Render Shell (if available)

1. Go to your database service in Render
2. Click on "Shell" tab (if available)
3. Run:
   ```bash
   psql $DATABASE_URL -f /path/to/database-migration.sql
   ```

## What the Migration Does

1. **Adds `email` column** to `users` table (nullable)
2. **Adds `email_verified` column** to `users` table (nullable, default false)
3. **Adds `email_verified` column** to `sellers` table (nullable, default false)
4. **Sets default values** for existing rows (false = not verified)
5. **Creates `email_otps` table** for storing OTP codes
6. **Creates indexes** for better query performance

## Verification

After running the migration, you should see:
- ✅ `users` table has `email` and `email_verified` columns
- ✅ `sellers` table has `email_verified` column
- ✅ `email_otps` table exists

## Troubleshooting

### If migration fails:
- Check that you have proper database permissions
- Ensure you're connected to the correct database
- Verify table names match (should be `users` and `sellers`)

### If columns already exist:
- The script uses `IF NOT EXISTS`, so it's safe to run multiple times
- If you get "column already exists" errors, that's okay - the migration is already done

### After migration:
- Restart your Render web service
- Check application logs for any remaining errors
- Test the signup endpoint to verify email OTP is working

## Important Notes

- ⚠️ **Backup your database** before running migrations (if possible)
- ✅ The migration is **safe to run multiple times** (uses IF NOT EXISTS)
- ✅ **Existing data is preserved** - no data will be lost
- ✅ **Existing users/sellers** will have `email_verified = false` by default

## Next Steps After Migration

1. ✅ Run the migration script
2. ✅ Restart your Render application
3. ✅ Test signup endpoint: `POST /api/v1/auth/signup`
4. ✅ Check email inbox for OTP
5. ✅ Test verify endpoint: `POST /api/v1/auth/verify-email`

---

**Migration Script Location:** `shopping/database-migration.sql`
