ALTER TABLE user_table
MODIFY COLUMN is_active BOOLEAN
GENERATED ALWAYS AS (is_email_verified AND is_admin_approved) STORED;



