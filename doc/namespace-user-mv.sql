-- Materialized View for NamespaceUser with all necessary joins
-- This view denormalizes namespace_user data with user_base and namespace for efficient querying

DROP MATERIALIZED VIEW IF EXISTS namespace_user_mv CASCADE;

CREATE MATERIALIZED VIEW namespace_user_mv AS
SELECT
    -- namespace_user core fields
    nu.id,
    nu.namespace_id,
    nu.principal_id,
    r.name as role,
    nu.display_name,
    nu.type,
    nu.security_level,
    nu.permissions,
    nu.entry_id,
    nu.active,
    nu.activation_date,
    nu.expiration_date,
    nu.approved_by,
    nu.approved_date,
    nu.created_by,
    nu.created_date,
    nu.modified_by,
    nu.modified_date,

    -- user_base fields
    ub.first_name,
    ub.last_name,
    ub.email,
    ub.phone,
    ub.image_url,
    ub.profile,
    ub.preference,
    ub.active as user_active,
    ub.expiration_date as user_expiration_date
FROM namespace_user nu
-- Join user_base
INNER JOIN user_base ub
    ON nu.principal_id = ub.id
LEFT JOIN namespace_role r
    ON nu.role_id = r.id

;
-- Create indexes for common query patterns
CREATE UNIQUE INDEX namespace_user_mv_id_idx ON namespace_user_mv (id);
CREATE INDEX namespace_user_mv_namespace_id_idx ON namespace_user_mv (namespace_id);
CREATE INDEX namespace_user_mv_principal_id_idx ON namespace_user_mv (principal_id);
CREATE INDEX namespace_user_mv_display_name_idx ON namespace_user_mv (display_name);
CREATE INDEX namespace_user_mv_email_idx ON namespace_user_mv (email);
CREATE INDEX namespace_user_mv_type_idx ON namespace_user_mv (type);
CREATE INDEX namespace_user_mv_active_idx ON namespace_user_mv (active);
CREATE INDEX namespace_user_mv_namespace_active_idx ON namespace_user_mv (namespace_id, active);

-- Refresh the materialized view concurrently (requires unique index on id)
-- To refresh: REFRESH MATERIALIZED VIEW CONCURRENTLY namespace_user_mv;
