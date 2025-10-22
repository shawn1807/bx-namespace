-- ===========================================================
--  Resource Booking System Schema
--  Postgres version >= 13 recommended
-- ===========================================================

CREATE EXTENSION IF NOT EXISTS btree_gist;
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ===========================================================
-- Resource tables
-- ===========================================================

CREATE TYPE resource_type AS ENUM ('room','equipment','vehicle','person','space');

CREATE TABLE resource (
  id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  namespace_id    uuid NOT NULL,
  name            text NOT NULL,
  type            resource_type NOT NULL,
  capacity        int,
  location        text,
  timezone        text,
  meta            jsonb,
  active          boolean NOT NULL DEFAULT true,
  created_at      timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX resource_namespace_idx ON resource (namespace_id);
CREATE INDEX resource_active_idx ON resource (namespace_id, active);



-- Weekly recurring availability windows
CREATE TABLE resource_weekly_window (
  id             bigserial PRIMARY KEY,
  namespace_id   uuid NOT NULL,
  resource_id    uuid NOT NULL REFERENCES resource(id) ON DELETE CASCADE,
  dow            int NOT NULL CHECK (dow BETWEEN 1 AND 7),
  start_local    time NOT NULL,
  end_local      time NOT NULL CHECK (end_local > start_local),
  created_at     timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX rww_resource_idx ON resource_weekly_window (resource_id, dow);

-- One-time exceptions (blackout periods)
CREATE TABLE resource_exception (
  id             bigserial PRIMARY KEY,
  namespace_id   uuid NOT NULL,
  resource_id    uuid NOT NULL REFERENCES resource(id) ON DELETE CASCADE,
  reason         text,
  start_at       timestamptz NOT NULL,
  end_at         timestamptz NOT NULL CHECK (end_at > start_at),
  created_at     timestamptz NOT NULL DEFAULT now(),
  span           tstzrange GENERATED ALWAYS AS (tstzrange(start_at, end_at, '[)')) STORED
);
CREATE INDEX rexc_resource_span_gist ON resource_exception USING gist (resource_id, span);

-- ===========================================================
-- Booking tables
-- ===========================================================

CREATE TYPE booking_status AS ENUM ('tentative','confirmed','cancelled','completed','no_show');

CREATE TABLE booking (
  id             uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  namespace_id   uuid NOT NULL,
  resource_id    uuid NOT NULL REFERENCES resource(id) ON DELETE RESTRICT,
  user_id        uuid NOT NULL,  -- reference external user directory if needed
  title          text,
  notes          text,
  start_at       timestamptz NOT NULL,
  end_at         timestamptz NOT NULL CHECK (end_at > start_at),
  status         booking_status NOT NULL DEFAULT 'confirmed',
  created_at     timestamptz NOT NULL DEFAULT now(),
  created_by     uuid,
  updated_at     timestamptz NOT NULL DEFAULT now(),
  updated_by     uuid,
  deleted_at     timestamptz,
  slot           tstzrange GENERATED ALWAYS AS (tstzrange(start_at, end_at, '[)')) STORED
);

CREATE INDEX booking_namespace_idx      ON booking (namespace_id);
CREATE INDEX booking_res_start_idx      ON booking (namespace_id, resource_id, start_at);
CREATE INDEX booking_user_start_idx     ON booking (namespace_id, user_id, start_at);
CREATE INDEX booking_status_idx         ON booking (namespace_id, status);

-- Prevent overlapping bookings per resource per namespace
ALTER TABLE booking
  ADD CONSTRAINT booking_no_overlap
  EXCLUDE USING gist (
    namespace_id WITH =,
    resource_id  WITH =,
    slot         WITH &&
  )
  WHERE (deleted_at IS NULL AND status IN ('tentative','confirmed'));

-- ===========================================================
-- Holds & Waitlist
-- ===========================================================

CREATE TABLE booking_hold (
  id             uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  namespace_id   uuid NOT NULL,
  resource_id    uuid NOT NULL REFERENCES resource(id) ON DELETE CASCADE,
  user_id        uuid NOT NULL,
  start_at       timestamptz NOT NULL,
  end_at         timestamptz NOT NULL CHECK (end_at > start_at),
  expires_at     timestamptz NOT NULL,
  created_at     timestamptz NOT NULL DEFAULT now(),
  slot           tstzrange GENERATED ALWAYS AS (tstzrange(start_at, end_at, '[)')) STORED
);

CREATE INDEX hold_expiry_idx        ON booking_hold (expires_at);
CREATE INDEX hold_resource_span_gist ON booking_hold USING gist (namespace_id, resource_id, slot);

CREATE TABLE booking_waitlist (
  id             uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  namespace_id   uuid NOT NULL,
  resource_id    uuid NOT NULL REFERENCES resource(id) ON DELETE CASCADE,
  user_id        uuid NOT NULL,
  desired_start  timestamptz NOT NULL,
  desired_end    timestamptz NOT NULL CHECK (desired_end > desired_start),
  priority       int NOT NULL DEFAULT 100,
  created_at     timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX waitlist_res_time_idx ON booking_waitlist (namespace_id, resource_id, priority, created_at);

-- ===========================================================
-- Audit log
-- ===========================================================

CREATE TABLE audit_log (
  id            bigserial PRIMARY KEY,
  namespace_id  uuid NOT NULL,
  actor_id      uuid,
  entity        text NOT NULL,
  entity_id     uuid NOT NULL,
  action        text NOT NULL,
  at            timestamptz NOT NULL DEFAULT now(),
  diff          jsonb
);

CREATE INDEX audit_ns_entity_time_idx ON audit_log (namespace_id, entity, at DESC);

-- ===========================================================
-- Optional optimization indexes
-- ===========================================================

CREATE INDEX booking_time_idx ON booking USING btree (start_at, end_at);
CREATE INDEX exception_time_idx ON resource_exception USING btree (start_at, end_at);

-- ===========================================================
-- End of schema.sql
-- ===========================================================
