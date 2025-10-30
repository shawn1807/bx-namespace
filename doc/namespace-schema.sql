
DROP TABLE IF EXISTS base_principal cascade;
CREATE TABLE base_principal (
      id UUID  PRIMARY KEY,
      name text NOT NULL,
      type text NOT NULL
);
CREATE  INDEX principal_idx1 ON base_principal (type);

DROP TABLE IF EXISTS user_base cascade;
CREATE TABLE user_base (
      id UUID  PRIMARY KEY references base_principal,
      display_name text NOT NULL,
      first_name text,
      last_name text,
      email text NOT NULL,
      phone text,
      active boolean default true,
      image_url text,
      preferences jsonb,
      language_tag TEXT;
      timezone_id TEXT;
      date_pattern TEXT;
      datetime_pattern TEXT;
      created_date timestamp with time zone not null,
      modified_date timestamp with time zone not null
);
CREATE  INDEX user_base_idx1 ON user_base (display_name);
CREATE  INDEX user_base_idx2 ON user_base (email);
CREATE  INDEX user_base_idx3 ON user_base (phone);
CREATE INDEX user_base_language_idx ON user_base (language_tag);
CREATE INDEX user_base_timezone_idx ON user_base (timezone_id);
COMMENT ON COLUMN user_base.currency_code IS 'User override for currency (falls back to namespace if NULL)';
COMMENT ON COLUMN user_base.language_tag IS 'User override for language (falls back to namespace if NULL)';
COMMENT ON COLUMN user_base.timezone_id IS 'User override for timezone (falls back to namespace if NULL)';

DROP TABLE IF EXISTS namespace cascade;
CREATE TABLE namespace (
      id UUID  PRIMARY KEY,
      name text NOT NULL,
      uri text not null,
      owner_id UUID not null,
      contact_email text not null,
      bucket text not null,
      props jsonb,
      active boolean default true,
      status text,
      expiration_date date,
      description text,
      website text,
      logo_image_url text,
      background_image_url text,
      language_tag TEXT;
      timezone_id TEXT;
      date_pattern TEXT;
      datetime_pattern TEXT;
      access_level text not null,
      created_by UUID not null,
      created_date timestamp with time zone not null,
      modified_by UUID not null,
      modified_date timestamp with time zone not null,
      CONSTRAINT owner_id_fk FOREIGN KEY(owner_id)
                     	  REFERENCES base_principal(id),
      CONSTRAINT created_by_fk FOREIGN KEY(created_by)
                     	  REFERENCES base_principal(id),
      CONSTRAINT modified_by_fk FOREIGN KEY(modified_by)
                          REFERENCES base_principal(id)
);
CREATE UNIQUE INDEX namespace_u1 ON namespace (name);
CREATE UNIQUE INDEX namespace_u2 ON namespace (uri);
CREATE INDEX namespace_language_idx ON namespace (language_tag);
CREATE INDEX namespace_timezone_idx ON namespace (timezone_id);
COMMENT ON COLUMN namespace.currency_code IS 'ISO 4217 currency code (USD, EUR, TWD, etc.)';
COMMENT ON COLUMN namespace.language_tag IS 'IETF BCP47 language tag (en-US, zh-TW, ja-JP, etc.)';
COMMENT ON COLUMN namespace.timezone_id IS 'IANA timezone identifier (America/New_York, Asia/Taipei, etc.)';
COMMENT ON COLUMN namespace.date_pattern IS 'Java DateTimeFormatter pattern for dates (yyyy-MM-dd, MM/dd/yyyy, etc.)';
COMMENT ON COLUMN namespace.datetime_pattern IS 'Java DateTimeFormatter pattern for date-times';

DROP TABLE IF EXISTS auth_provider cascade;
CREATE TABLE auth_provider (
    id serial primary key,
    type text not null,
    name text not null,
    description text,
    props jsonb
);
CREATE UNIQUE INDEX auth_provider_u1 ON auth_provider (name);

DROP TABLE IF EXISTS login cascade;
CREATE TABLE login (
      id serial  PRIMARY KEY,
      user_id UUID NOT NULL,
      provider text NOT NULL,
      auth_id text NOT NULL,
      active boolean default true,
      props jsonb,
      created_by UUID not null,
      created_date timestamp with time zone not null,
      CONSTRAINT user_fk FOREIGN KEY(user_id)
         	  REFERENCES base_principal(id),
      CONSTRAINT created_by_fk FOREIGN KEY(created_by)
                     	  REFERENCES base_principal(id)
);
CREATE UNIQUE INDEX login_u1 ON login (provider, auth_id);
CREATE INDEX login_u2 ON login (user_id);


DROP TABLE IF EXISTS subscription_plan cascade;
CREATE TABLE subscription_plan (
      namespace_id uuid NOT NULL,
      id SERIAL,
      name text NOT NULL,
      description text NOT NULL,
      duration_unit text NOT NULL,
      duration integer NOT NULL,
      max_user integer not null default 3,
      PRIMARY KEY (namespace_id, id),
      CONSTRAINT namespace_fk FOREIGN KEY(namespace_id)
                              REFERENCES namespace(id)
);
CREATE  UNIQUE INDEX subscription_plan_u1 ON subscription_plan (namespace_id, name);


DROP TABLE IF EXISTS subscription cascade;
CREATE TABLE subscription (
      id SERIAL NOT NULL,
      namespace_id UUID NOT NULL,
      plan_id integer not null,
      recurring boolean NOT NULL default true,
      activation_date date,
      expiration_date date,
      created_by UUID not null,
      created_date timestamp with time zone not null,
      modified_by UUID not null,
      modified_date timestamp with time zone not null,
      PRIMARY KEY (namespace_id, id),
      CONSTRAINT plan_fk FOREIGN KEY(plan_namespace_id, plan_id)
                           	  REFERENCES subscription_plan(namespace_id, id),
      CONSTRAINT created_by_fk FOREIGN KEY(created_by)
                              REFERENCES base_principal(id),
      CONSTRAINT modified_by_fk FOREIGN KEY(modified_by)
                          REFERENCES base_principal(id)
);
CREATE  UNIQUE INDEX subscription_u1 ON subscription (namespace_id);

DROP TABLE IF EXISTS subscription_history cascade;
CREATE TABLE subscription_history (
      id serial not null,
      namespace_id uuid NOT NULL,
      provider_id uuid NOT NULL,
      plan text not null,
      activation_date date,
      expiration_date date,
      created_by uuid not null,
      created_date timestamp with time zone not null,
      PRIMARY KEY (namespace_id, id)
);

CREATE EXTENSION IF NOT EXISTS btree_gin;
CREATE EXTENSION IF NOT EXISTS pg_trgm;

DROP TABLE IF EXISTS app_module cascade;
CREATE TABLE app_module (
      id serial  PRIMARY KEY,
      name text not null,
      version text not null,
      priority integer default 0,
      build integer default 0,
      build_package text not null,
      modified_date  timestamp with time zone not null
);

DROP TABLE IF EXISTS upgrade_history cascade;
CREATE TABLE upgrade_history (
      id serial  PRIMARY KEY,
      module text not null,
      version text not null,
      build integer  not null,
      description  text not null,
      upgrade_date  timestamp with time zone not null
);

DROP TABLE IF EXISTS place cascade;
CREATE TABLE place (
      id uuid  NOT NULL,
      namespace_id UUID not null,
      type             text NOT NULL,                   -- country|region|city|neighborhood|poi|campus|building|floor|room|route|area|water|...
      iso_country      char(2),                         -- ISO-3166-1 alpha-2 (for countries or inferred)
      country text not null,
      county text not null,
      city text not null,
      building text,
      address text not null,
      post_code text,
        -- geo
      lat double precision,
      lng double precision,
      center           geography(Point, 4326),          -- WGS84, for fast distance/radius sorts
      props jsonb,
      notes text,
      created_at       timestamptz NOT NULL DEFAULT now(),
      updated_at       timestamptz NOT NULL DEFAULT now(),
      PRIMARY KEY (namespace_id, id),
      CONSTRAINT namespace_fk FOREIGN KEY(namespace_id)
                              	  REFERENCES namespace(id)
);
-- Props search (if you filter on jsonb keys)
CREATE INDEX location_props_gin_idx    ON location USING gin (props);

-- Geo: center for distance & radius, bbox for coarse filters
CREATE INDEX location_center_gist_idx  ON location USING gist (center);
CREATE INDEX location_bbox_gist_idx    ON location USING gist (bbox);
CREATE INDEX location_type_idx         ON location (type);
CREATE INDEX location_iso_idx          ON location (iso_country);
CREATE INDEX location_post_code_idx          ON location (post_code);
CREATE INDEX location_namespace_idx    ON location (namespace_id);


DROP TABLE IF EXISTS namespace_role cascade;
CREATE TABLE namespace_role (
   namespace_id uuid NOT NULL,
   id serial,
   name text not null,
   description text,
   permissions jsonb not null,
   created_by integer not null,
   created_date timestamp with time zone not null,
   modified_by integer not null,
   modified_date timestamp with time zone not null,
   PRIMARY KEY (namespace_id, id),
   CONSTRAINT namespace_fk FOREIGN KEY(namespace_id)
                        	  REFERENCES namespace(id)
);
CREATE UNIQUE INDEX namespace_role_u1 ON namespace_role (namespace_id, name);

DROP TABLE IF EXISTS namespace_user cascade;
CREATE TABLE namespace_user (
      namespace_id uuid not null,
      id SERIAL,
      principal_id uuid NOT NULL,
      role_id integer,
      display_name text NOT NULL,
      type char(1) NOT NULL,
      permissions jsonb not null,
      entry_id UUID,
      active boolean NOT NULL default false,
      activation_date date,
      expiration_date date,
      approved_by uuid,
      approved_date timestamp with time zone,
      created_by uuid not null,
      created_date timestamp with time zone not null,
      modified_by uuid not null,
      modified_date timestamp with time zone not null,
      PRIMARY KEY (namespace_id, id),
      CONSTRAINT namespace_fk FOREIGN KEY(namespace_id)
                REFERENCES namespace(id),
      CONSTRAINT role_fk FOREIGN KEY(namespace_id, role_id)
                REFERENCES namespace_role(namespace_id, id),
      CONSTRAINT principal_id_fk FOREIGN KEY(principal_id)
                              REFERENCES base_principal(id),
      CONSTRAINT approved_by_fk FOREIGN KEY(approved_by)
                              REFERENCES base_principal(id),
      CONSTRAINT created_by_fk FOREIGN KEY(created_by)
                              REFERENCES base_principal(id),
      CONSTRAINT modified_by_fk FOREIGN KEY(modified_by)
                              REFERENCES base_principal(id)
);
CREATE  UNIQUE INDEX namespace_user_u1 ON namespace_user (namespace_id, principal_id);




DROP TABLE IF EXISTS entity_type cascade;
CREATE TABLE entity_type (
      namespace_id uuid not null,
      id SERIAL,
      name text NOT NULL,
      created_by integer not null,
      created_date timestamp with time zone not null,
      PRIMARY KEY (namespace_id, id),
      CONSTRAINT namespace_fk FOREIGN KEY(namespace_id)
                                       	  REFERENCES namespace(id),
      CONSTRAINT created_by_fk FOREIGN KEY(namespace_id, created_by)
                     	  REFERENCES namespace_user(namespace_id, id)
);
CREATE  UNIQUE INDEX entity_type_u1 ON entity_type (namespace_id, name);

DROP TABLE IF EXISTS entity cascade;
CREATE TABLE entity (
      namespace_id uuid NOT NULL,
      id uuid,
      name text NOT NULL,
      primary_place_id UUID,
      type_id integer NOT NULL,
      clazz text,
      parent_id uuid,
      email text not null,
      phone text,
      profile jsonb,
      active boolean default true,
      created_by integer not null,
      created_date timestamp with time zone not null,
      modified_by integer not null,
      modified_date timestamp with time zone not null,
      PRIMARY KEY (namespace_id, id),
      CONSTRAINT namespace_fk FOREIGN KEY(namespace_id)
                                 	  REFERENCES namespace(id),
      CONSTRAINT type_fk FOREIGN KEY(namespace_id, type_id)
                                 	  REFERENCES entity_type(namespace_id, id),
      CONSTRAINT parent_fk FOREIGN KEY(namespace_id, parent_id)
                                 	  REFERENCES entity(namespace_id, id),
      CONSTRAINT created_by_fk FOREIGN KEY(namespace_id, created_by)
                     	  REFERENCES namespace_user(namespace_id, id),
      CONSTRAINT modified_by_fk FOREIGN KEY(namespace_id, modified_by)
                          REFERENCES namespace_user(namespace_id, id)
);
CREATE  UNIQUE INDEX entity_u1 ON entity (namespace_id,type_id, name);


DROP TABLE IF EXISTS event_audit cascade;
CREATE TABLE event_audit (
      id BIGSERIAL NOT NULL,
      namespace_id uuid not null,
      entry_id UUID,
      action text not null,
      params jsonb,
      txid text not null,
      created_by integer not null,
      created_date timestamp with time zone not null,
      PRIMARY KEY (namespace_id, id),
      CONSTRAINT created_by_fk FOREIGN KEY(namespace_id, created_by)
                          REFERENCES namespace_user(namespace_id, id)
);
CREATE INDEX event_audit_idx1 ON event_audit(namespace_id, entry_id);
CREATE INDEX event_audit_idx2 ON event_audit(namespace_id, created_by);