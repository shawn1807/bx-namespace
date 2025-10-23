-- Locale and Regional Settings Schema
-- Adds currency, language, timezone, and formatting preferences to namespace and user_base

-- ISO 4217 Currency Reference Table
DROP TABLE IF EXISTS iso_currency CASCADE;
CREATE TABLE iso_currency (
    code           CHAR(3) PRIMARY KEY,      -- ISO 4217 alpha code (USD, EUR, TWD, etc.)
    numeric_code   CHAR(3),                  -- ISO 4217 numeric code
    name           TEXT NOT NULL,            -- English name
    symbol         TEXT,                     -- Currency symbol ($, €, NT$, etc.)
    decimal_places SMALLINT DEFAULT 2,       -- Number of decimal places
    active         BOOLEAN DEFAULT true,

    created_date   TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Common currencies
INSERT INTO iso_currency (code, numeric_code, name, symbol, decimal_places) VALUES
('USD', '840', 'US Dollar', '$', 2),
('EUR', '978', 'Euro', '€', 2),
('GBP', '826', 'Pound Sterling', '£', 2),
('JPY', '392', 'Japanese Yen', '¥', 0),
('CNY', '156', 'Chinese Yuan', '¥', 2),
('TWD', '901', 'New Taiwan Dollar', 'NT$', 2),
('HKD', '344', 'Hong Kong Dollar', 'HK$', 2),
('SGD', '702', 'Singapore Dollar', 'S$', 2),
('KRW', '410', 'South Korean Won', '₩', 0),
('THB', '764', 'Thai Baht', '฿', 2),
('AUD', '036', 'Australian Dollar', 'A$', 2),
('CAD', '124', 'Canadian Dollar', 'C$', 2),
('CHF', '756', 'Swiss Franc', 'Fr.', 2),
('INR', '356', 'Indian Rupee', '₹', 2);

CREATE INDEX iso_currency_active_idx ON iso_currency (active);

-- Add locale/regional settings to namespace table
ALTER TABLE namespace ADD COLUMN IF NOT EXISTS currency_code CHAR(3) REFERENCES iso_currency(code);
ALTER TABLE namespace ADD COLUMN IF NOT EXISTS language_tag TEXT;         -- IETF BCP47, e.g. 'zh-TW', 'en-US'
ALTER TABLE namespace ADD COLUMN IF NOT EXISTS timezone_id TEXT;          -- IANA TZ, e.g. 'Asia/Taipei', 'America/New_York'
ALTER TABLE namespace ADD COLUMN IF NOT EXISTS date_pattern TEXT;         -- e.g. 'yyyy-MM-dd' (LDML/Java)
ALTER TABLE namespace ADD COLUMN IF NOT EXISTS time_pattern TEXT;         -- e.g. 'HH:mm'
ALTER TABLE namespace ADD COLUMN IF NOT EXISTS datetime_pattern TEXT;     -- e.g. 'yyyy-MM-dd HH:mm'

-- Add locale/regional settings to user_base table (nullable - falls back to namespace)
ALTER TABLE user_base ADD COLUMN IF NOT EXISTS currency_code CHAR(3) REFERENCES iso_currency(code);
ALTER TABLE user_base ADD COLUMN IF NOT EXISTS language_tag TEXT;
ALTER TABLE user_base ADD COLUMN IF NOT EXISTS timezone_id TEXT;
ALTER TABLE user_base ADD COLUMN IF NOT EXISTS date_pattern TEXT;
ALTER TABLE user_base ADD COLUMN IF NOT EXISTS time_pattern TEXT;
ALTER TABLE user_base ADD COLUMN IF NOT EXISTS datetime_pattern TEXT;

-- Indexes for locale settings
CREATE INDEX namespace_currency_idx ON namespace (currency_code);
CREATE INDEX namespace_language_idx ON namespace (language_tag);
CREATE INDEX namespace_timezone_idx ON namespace (timezone_id);

CREATE INDEX user_base_currency_idx ON user_base (currency_code);
CREATE INDEX user_base_language_idx ON user_base (language_tag);
CREATE INDEX user_base_timezone_idx ON user_base (timezone_id);

-- Default values for common configurations
COMMENT ON COLUMN namespace.currency_code IS 'ISO 4217 currency code (USD, EUR, TWD, etc.)';
COMMENT ON COLUMN namespace.language_tag IS 'IETF BCP47 language tag (en-US, zh-TW, ja-JP, etc.)';
COMMENT ON COLUMN namespace.timezone_id IS 'IANA timezone identifier (America/New_York, Asia/Taipei, etc.)';
COMMENT ON COLUMN namespace.date_pattern IS 'Java DateTimeFormatter pattern for dates (yyyy-MM-dd, MM/dd/yyyy, etc.)';
COMMENT ON COLUMN namespace.time_pattern IS 'Java DateTimeFormatter pattern for times (HH:mm, hh:mm a, etc.)';
COMMENT ON COLUMN namespace.datetime_pattern IS 'Java DateTimeFormatter pattern for date-times';

COMMENT ON COLUMN user_base.currency_code IS 'User override for currency (falls back to namespace if NULL)';
COMMENT ON COLUMN user_base.language_tag IS 'User override for language (falls back to namespace if NULL)';
COMMENT ON COLUMN user_base.timezone_id IS 'User override for timezone (falls back to namespace if NULL)';

-- Function to get effective locale settings (with fallback)
CREATE OR REPLACE FUNCTION get_effective_locale_settings(
    p_user_id UUID,
    p_namespace_id UUID
)
RETURNS TABLE (
    currency_code CHAR(3),
    language_tag TEXT,
    timezone_id TEXT,
    date_pattern TEXT,
    time_pattern TEXT,
    datetime_pattern TEXT
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        COALESCE(ub.currency_code, ns.currency_code) as currency_code,
        COALESCE(ub.language_tag, ns.language_tag) as language_tag,
        COALESCE(ub.timezone_id, ns.timezone_id) as timezone_id,
        COALESCE(ub.date_pattern, ns.date_pattern) as date_pattern,
        COALESCE(ub.time_pattern, ns.time_pattern) as time_pattern,
        COALESCE(ub.datetime_pattern, ns.datetime_pattern) as datetime_pattern
    FROM user_base ub
    CROSS JOIN namespace ns
    WHERE ub.id = p_user_id
      AND ns.id = p_namespace_id;
END;
$$ LANGUAGE plpgsql STABLE;

-- Example default patterns
-- US: date_pattern='MM/dd/yyyy', time_pattern='hh:mm a', datetime_pattern='MM/dd/yyyy hh:mm a'
-- Taiwan: date_pattern='yyyy/MM/dd', time_pattern='HH:mm', datetime_pattern='yyyy/MM/dd HH:mm'
-- Japan: date_pattern='yyyy年MM月dd日', time_pattern='HH:mm', datetime_pattern='yyyy年MM月dd日 HH:mm'
-- Europe: date_pattern='dd/MM/yyyy', time_pattern='HH:mm', datetime_pattern='dd/MM/yyyy HH:mm'
