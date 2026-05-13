package org.example.sudobeats.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthSchemaMigration implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        jdbcTemplate.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS password_hash VARCHAR(255)");
        jdbcTemplate.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ");
        jdbcTemplate.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS role VARCHAR(24) DEFAULT 'USER'");
        jdbcTemplate.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS pro BOOLEAN DEFAULT FALSE");
        jdbcTemplate.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS current_streak INTEGER DEFAULT 0");

        if (columnExists("users", "password")) {
            jdbcTemplate.execute("UPDATE users SET password_hash = password WHERE password_hash IS NULL AND password IS NOT NULL");
            jdbcTemplate.execute("ALTER TABLE users ALTER COLUMN password DROP NOT NULL");
        }

        jdbcTemplate.execute("UPDATE users SET updated_at = COALESCE(created_at, NOW()) WHERE updated_at IS NULL");
        jdbcTemplate.execute("UPDATE users SET role = 'USER' WHERE role IS NULL");
        jdbcTemplate.execute("UPDATE users SET pro = FALSE WHERE pro IS NULL");
        jdbcTemplate.execute("UPDATE users SET current_streak = 0 WHERE current_streak IS NULL");

        jdbcTemplate.execute("ALTER TABLE users ALTER COLUMN updated_at SET DEFAULT NOW()");
        jdbcTemplate.execute("ALTER TABLE users ALTER COLUMN updated_at SET NOT NULL");
        jdbcTemplate.execute("ALTER TABLE users ALTER COLUMN role SET DEFAULT 'USER'");
        jdbcTemplate.execute("ALTER TABLE users ALTER COLUMN role SET NOT NULL");
        jdbcTemplate.execute("ALTER TABLE users ALTER COLUMN pro SET DEFAULT FALSE");
        jdbcTemplate.execute("ALTER TABLE users ALTER COLUMN pro SET NOT NULL");
        jdbcTemplate.execute("ALTER TABLE users ALTER COLUMN current_streak SET DEFAULT 0");
        jdbcTemplate.execute("ALTER TABLE users ALTER COLUMN current_streak SET NOT NULL");
    }

    private boolean columnExists(String tableName, String columnName) {
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM information_schema.columns
                WHERE table_schema = current_schema()
                  AND table_name = ?
                  AND column_name = ?
                """,
                Integer.class,
                tableName,
                columnName);
        return count != null && count > 0;
    }
}
