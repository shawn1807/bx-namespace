
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
      image_url text,
      region text,
      phone text,
      profile jsonb,
      preference jsonb,
      active boolean default true,
      expiration_date date,
      created_by uuid not null,
      created_date timestamp with time zone not null,
      modified_by uuid not null,
      modified_date timestamp with time zone not null,
      CONSTRAINT created_by_fk FOREIGN KEY(created_by)
                     	  REFERENCES base_principal(id),
      CONSTRAINT modified_by_fk FOREIGN KEY(modified_by)
                          REFERENCES base_principal(id)
);
CREATE  INDEX user_base_idx1 ON user_base (display_name);
CREATE  INDEX user_base_idx2 ON user_base (email);
CREATE  INDEX user_base_idx3 ON user_base (phone);

DROP TABLE IF EXISTS namespace cascade;
CREATE TABLE namespace (
      id UUID  PRIMARY KEY,
      name text NOT NULL,
      uri text not null,
      owner_id UUID not null,
      contact_email text not null,
      primary_workspace_id uuid ,
      bucket text not null,
      props jsonb,
      active boolean default true,
      expiration_date date,
      description text,
      status text,
      website text,
      logo_image_url text,
      background_image_url text,
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
      namespace_id UUID NOT NULL,
      id SERIAL,
      plan_id integer not null,
      plan_namespace_id uuid not null,
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
      namespace_id uuid NOT NULL,
      id text,
      provider_id uuid NOT NULL,
      plan text not null,
      activation_date date,
      expiration_date date,
      created_by uuid not null,
      created_date timestamp with time zone not null,
      PRIMARY KEY (namespace_id, id)
);
