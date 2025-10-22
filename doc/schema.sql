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
      id serial  PRIMARY KEY,
      country text not null,
      county text not null,
      city text not null,
      building text,
      address text not null,
      lat double precision,
      lng double precision,
      post_code text,
      props jsonb,
      notes text
);

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
      security_level text not null,
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
      primary_place_id integer,
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





DROP TABLE IF EXISTS workspace_type cascade;
CREATE TABLE workspace_type (
      namespace_id uuid not null,
      id serial,
      name text not null,
      description text,
      number_name text,
      theme text,
      is_default boolean not null default false,
      active boolean not null default true,
      standalone boolean not null default false,
      template jsonb,
      props jsonb,
      created_by integer not null,
      created_date timestamp with time zone not null,
      modified_by integer not null,
      modified_date timestamp with time zone not null,
      PRIMARY KEY (namespace_id, id),
      CONSTRAINT namespace_fk FOREIGN KEY(namespace_id)
                                REFERENCES namespace(id),
      CONSTRAINT created_by_fk FOREIGN KEY(namespace_id, created_by)
                     	  REFERENCES namespace_user(namespace_id, id),
      CONSTRAINT modified_by_fk FOREIGN KEY(namespace_id, modified_by)
                          REFERENCES namespace_user(namespace_id, id)

);
CREATE UNIQUE INDEX workspace_type_u1 ON workspace_type ( namespace_id, name);

DROP TABLE IF EXISTS document_type cascade;
CREATE TABLE document_type (
      namespace_id uuid not null,
      id serial,
      name text not null,
      description text,
      security_class text not null,
      active boolean not null default true,
      props jsonb,
      created_by integer not null,
      created_date timestamp with time zone not null,
      modified_by integer not null,
      modified_date timestamp with time zone not null,
      PRIMARY KEY (namespace_id, id),
      CONSTRAINT namespace_fk FOREIGN KEY(namespace_id)
                                REFERENCES namespace(id),
      CONSTRAINT created_by_fk FOREIGN KEY(namespace_id, created_by)
                     	  REFERENCES namespace_user(namespace_id, id),
      CONSTRAINT modified_by_fk FOREIGN KEY(namespace_id, modified_by)
                          REFERENCES namespace_user(namespace_id, id)
);
CREATE UNIQUE INDEX document_type_u1 ON document_type ( namespace_id, name);

DROP TABLE IF EXISTS work_type cascade;
CREATE TABLE work_type (
      namespace_id uuid not null,
      id serial,
      name text not null,
      description text,
      number_name text,
      theme text,
      category varchar(1) not null,
      template jsonb,
      active boolean not null default true,
      created_by integer not null,
      created_date timestamp with time zone not null,
      modified_by integer not null,
      modified_date timestamp with time zone not null,
      PRIMARY KEY (namespace_id, id),
      CONSTRAINT namespace_fk FOREIGN KEY(namespace_id)
                                REFERENCES namespace(id),
      CONSTRAINT created_by_fk FOREIGN KEY(namespace_id, created_by)
                     	  REFERENCES namespace_user(namespace_id, id),
      CONSTRAINT modified_by_fk FOREIGN KEY(namespace_id, modified_by)
                          REFERENCES namespace_user(namespace_id, id)
);
CREATE UNIQUE INDEX work_type_u1 ON work_type ( namespace_id,category, name);

DROP TABLE IF EXISTS workspace_group cascade;
CREATE TABLE workspace_group (
    namespace_id uuid not null,
    id serial,
    name text not null,
    parent_id integer,
    created_by integer not null,
    created_date timestamp with time zone not null,
    PRIMARY KEY (namespace_id, id),
    CONSTRAINT namespace_fk FOREIGN KEY(namespace_id)
                            REFERENCES namespace(id),
    CONSTRAINT parent_fk FOREIGN KEY(namespace_id, parent_id)
                            REFERENCES workspace_group(namespace_id, id),
    CONSTRAINT created_by_fk FOREIGN KEY(namespace_id, created_by)
                     	  REFERENCES namespace_user(namespace_id, id)
);
CREATE UNIQUE INDEX workspace_group_u1 ON workspace_group ( namespace_id, name);
CREATE INDEX workspace_group_idx1 ON workspace_group ( namespace_id, parent_id);

DROP TABLE IF EXISTS workspace cascade;
CREATE TABLE workspace (
      namespace_id uuid not null,
      id uuid,
      name text not null,
      description text,
      workspace_type_id Integer not null,
      workspace_group_id integer,
      primary_place_id integer,
      assignee_id integer not null,
      is_private boolean not null default false,
      priority integer not null,
      start_date date,
      due_date date,
      status text not null,
      closed boolean not null default false,
      closed_by integer,
      closed_date timestamp with time zone,
      props jsonb,
      created_by integer not null,
      created_date timestamp with time zone not null,
      modified_by integer not null,
      modified_date timestamp with time zone not null,
      PRIMARY KEY (namespace_id, id),
      CONSTRAINT place_fk FOREIGN KEY(primary_place_id)
                                REFERENCES place(id),
      CONSTRAINT namespace_fk FOREIGN KEY(namespace_id)
                                REFERENCES namespace(id),
      CONSTRAINT assignee_id_fk FOREIGN KEY(namespace_id, assignee_id)
                                REFERENCES namespace_user(namespace_id, id),
      CONSTRAINT closed_by_fk FOREIGN KEY(namespace_id, closed_by)
                                REFERENCES namespace_user(namespace_id, id),
      CONSTRAINT created_by_fk FOREIGN KEY(namespace_id, created_by)
                                REFERENCES namespace_user(namespace_id, id),
      CONSTRAINT modified_by_fk FOREIGN KEY(namespace_id, modified_by)
                                REFERENCES namespace_user(namespace_id, id),
      CONSTRAINT workspace_type_fk FOREIGN KEY(namespace_id, workspace_type_id)
                                REFERENCES workspace_type(namespace_id, id),
      CONSTRAINT workspace_group_fk FOREIGN KEY(namespace_id, workspace_group_id)
                                REFERENCES workspace_group(namespace_id, id)
);
CREATE UNIQUE INDEX workspace_u1 ON workspace ( namespace_id, name);
CREATE INDEX workspace_idx1 ON workspace ( namespace_id, workspace_type_id);
--CREATE INDEX workspace_idx2 ON workspace ( namespace_id, last_activity_date desc);

DROP TABLE IF EXISTS workspace_user cascade;
CREATE TABLE workspace_user (
   namespace_id uuid NOT NULL,
   id serial,
   workspace_id uuid NOT NULL,
   user_id integer NOT NULL,
   role_id text not null,
   preference jsonb,
   created_by integer not null,
   created_date timestamp with time zone not null,
   modified_by integer not null,
   modified_date timestamp with time zone not null,
   PRIMARY KEY (namespace_id, id),
   CONSTRAINT namespace_fk FOREIGN KEY(namespace_id)
                        	  REFERENCES namespace(id),
   CONSTRAINT workspace_fk FOREIGN KEY(namespace_id, workspace_id)
                        	  REFERENCES workspace(namespace_id, id),
   CONSTRAINT user_fk FOREIGN KEY(namespace_id, user_id)
                        	  REFERENCES namespace_user(namespace_id, id),
   CONSTRAINT created_by_fk FOREIGN KEY(namespace_id, created_by)
                        	  REFERENCES namespace_user(namespace_id, id),
   CONSTRAINT modified_by_fk FOREIGN KEY(namespace_id, modified_by)
                        	  REFERENCES namespace_user(namespace_id, id)
);
CREATE  UNIQUE INDEX workspace_user_u1 ON workspace_user (namespace_id, workspace_id, user_id);

DROP TABLE IF EXISTS document cascade;
CREATE TABLE document (
      namespace_id uuid NOT NULL,
      id uuid,
      workspace_id uuid,
      name text not null,
      security_class text not null,
      type_id Integer,
      description text not null,
      notes text,
      props jsonb,
      archived boolean default false,
      created_by integer not null,
      created_date timestamp with time zone not null,
      modified_by integer not null,
      modified_date timestamp with time zone not null,
      PRIMARY KEY (namespace_id, id),
      CONSTRAINT namespace_fk FOREIGN KEY(namespace_id)
                          REFERENCES namespace(id),
      CONSTRAINT workspace_fk FOREIGN KEY(namespace_id, workspace_id)
                          REFERENCES workspace(namespace_id, id),
      CONSTRAINT document_type_fk FOREIGN KEY(namespace_id,type_id)
                          REFERENCES document_type(namespace_id, id),
      CONSTRAINT created_by_fk FOREIGN KEY(namespace_id, created_by)
                          REFERENCES namespace_user(namespace_id, id),
      CONSTRAINT modified_by_fk FOREIGN KEY(namespace_id, modified_by)
                          REFERENCES namespace_user(namespace_id, id)
);
CREATE UNIQUE INDEX document_u1 ON document ( namespace_id, name);
CREATE INDEX document_idx1 ON document (namespace_id, type_id);
CREATE INDEX document_idx2 ON document (namespace_id, workspace_id);

-- One table to rule them all
CREATE TABLE objective (
  namespace_id uuid NOT NULL,
  id               serial,
  workspace_id     uuid NOT NULL,
  parent_id        integer,                           -- parent objective (usually goal)
  type             CHAR(1) NOT NULL CHECK (type IN ('G','M')),
  title            text NOT NULL,
  description      text,
  -- Timebox:
  start_date       date,
  due_date         date,
  -- State/progress:
  status           text NOT NULL DEFAULT 'active',   -- active/paused/done/missed
  progress_pct     numeric(5,2) NOT NULL DEFAULT 0,  -- 0–100
  -- Fast counters for dashboards:
  tasks_total      int NOT NULL DEFAULT 0,
  tasks_done       int NOT NULL DEFAULT 0,
  props            jsonb NOT NULL DEFAULT '{}',
  created_by       integer not null,
  created_date     timestamptz NOT NULL DEFAULT now(),
  modified_date    timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (namespace_id, id),
  CONSTRAINT namespace_fk FOREIGN KEY(namespace_id)
                          REFERENCES namespace(id),
  CONSTRAINT workspace_fk FOREIGN KEY (namespace_id, workspace_id)
                          REFERENCES workspace(namespace_id, id),
  CONSTRAINT parent_fk FOREIGN KEY (namespace_id, parent_id)
                          REFERENCES objective(namespace_id, id) ON DELETE CASCADE,
  CONSTRAINT created_by_fk FOREIGN KEY (namespace_id, created_by)
                          REFERENCES namespace_user(namespace_id, id)
);

-- Indices for speed
CREATE INDEX obj_project_idx        ON objective(namespace_id, workspace_id, type);
CREATE INDEX obj_due_open_idx       ON objective(namespace_id, type, status, due_date);
CREATE INDEX obj_parent_idx         ON objective(namespace_id, parent_id);
CREATE INDEX obj_title_trgm_idx     ON objective USING gin (title gin_trgm_ops);
CREATE INDEX obj_props_gin_idx      ON objective USING gin (props);


-- Tight partials for common pages
CREATE INDEX obj_goals_active_idx     ON objective(namespace_id, workspace_id, due_date)
  WHERE type='G' AND status IN ('active','paused');

CREATE INDEX obj_milestones_open_idx  ON objective(namespace_id, workspace_id, due_date)
  WHERE type='M' AND status IN ('active');



DROP TABLE IF EXISTS work cascade;
CREATE TABLE work (
      namespace_id uuid NOT NULL,
      id uuid,
      workspace_id uuid NOT NULL,
      parent_id uuid,
      category varchar(1) not null,
      type_id Integer not null,
      tracking_id text NOT NULL,
      title text NOT NULL,
      description text ,
      status text,
      recurring boolean not null default false,
      reuse boolean not null default false,
      priority integer not null,
      severity text not null,
      place_id Integer,
      start_date date,
      start_time time,
      due_date date,
      actual_start_date date,
      waiting boolean default false,
      closed boolean default false,
      assignee_id integer,
      assigned_by integer,
      assigned_date timestamp with time zone,
      completed boolean default false,
      completed_by integer,
      completed_date timestamp with time zone,
      estimated_hours real,
      created_by integer not null,
      created_date timestamp with time zone not null,
      modified_by integer not null,
      modified_date timestamp with time zone not null,
      PRIMARY KEY (namespace_id, id),
      CONSTRAINT workspace_fk FOREIGN KEY(namespace_id, workspace_id)
                          	  REFERENCES workspace(namespace_id, id),
      CONSTRAINT parent_fk FOREIGN KEY(namespace_id, parent_id)
                          	  REFERENCES work(namespace_id, id),
      CONSTRAINT namespace_fk FOREIGN KEY(namespace_id)
                           	  REFERENCES namespace(id),
      CONSTRAINT type_fk FOREIGN KEY(namespace_id, type_id)
                           	  REFERENCES work_type(namespace_id, id),
      CONSTRAINT assignee_fk FOREIGN KEY(namespace_id, assignee_id)
                          REFERENCES namespace_user(namespace_id, id),
      CONSTRAINT assigned_by_fk FOREIGN KEY(namespace_id, assigned_by)
                          REFERENCES namespace_user(namespace_id, id),
      CONSTRAINT completed_by_fk FOREIGN KEY(namespace_id, completed_by)
                          REFERENCES namespace_user(namespace_id, id),
      CONSTRAINT created_by_fk FOREIGN KEY(namespace_id, created_by)
                          REFERENCES namespace_user(namespace_id, id),
      CONSTRAINT modified_by_fk FOREIGN KEY(namespace_id, modified_by)
                          REFERENCES namespace_user(namespace_id, id)
);
CREATE UNIQUE INDEX work_tracking_id   ON work(namespace_id, tracking_id);
CREATE INDEX work_idx1            ON work(namespace_id, type_id);
CREATE INDEX work_idx2            ON work(namespace_id, due_date, priority);
CREATE INDEX work_idx3            ON work(namespace_id, status);
CREATE INDEX work_idx4            ON work(namespace_id, parent_id);
CREATE INDEX work_idx5            ON work(namespace_id, workspace_id);
CREATE INDEX work_assignee_idx    ON work(namespace_id, assignee_id, status);
CREATE INDEX work_title_trgm_idx  ON work USING gin (title gin_trgm_ops);
CREATE INDEX work_updated_idx     ON work(namespace_id, modified_date DESC);


DROP TABLE IF EXISTS entry_assignment cascade;
CREATE TABLE entry_assignment (
      namespace_id uuid NOT NULL,
      id BIGSERIAL,
      entry_id UUID NOT NULL,
      user_id integer not null,
      assigned_by integer not null,
      assigned_date timestamp with time zone not null,
      PRIMARY KEY (namespace_id, id),
      CONSTRAINT namespace_fk FOREIGN KEY(namespace_id)
                     	  REFERENCES namespace(id),
      CONSTRAINT assigned_by_fk FOREIGN KEY(namespace_id, assigned_by)
                     	  REFERENCES namespace_user(namespace_id, id),
      CONSTRAINT user_id_fk FOREIGN KEY(namespace_id, user_id)
                     	  REFERENCES namespace_user(namespace_id, id)
);
CREATE UNIQUE INDEX entry_assignment_u1 ON entry_assignment(namespace_id, entry_id, user_id);
CREATE INDEX entry_assignment_idx1 ON entry_assignment(namespace_id, entry_id);

DROP TABLE IF EXISTS workspace_activity cascade;
CREATE TABLE workspace_activity (
      namespace_id uuid NOT NULL,
      id bigserial,
      workspace_id uuid NOT NULL,
      action text not null,
      status text not null,
      created_by integer not null,
      created_date timestamp with time zone not null,
      PRIMARY KEY (namespace_id, id),
      CONSTRAINT namespace_fk FOREIGN KEY(namespace_id)
                     	  REFERENCES namespace(id),
      CONSTRAINT workspace_fk FOREIGN KEY(namespace_id, workspace_id)
                     	  REFERENCES workspace(namespace_id, id),
      CONSTRAINT created_by_fk FOREIGN KEY(namespace_id, created_by)
                     	  REFERENCES namespace_user(namespace_id, id)
);
CREATE INDEX workspace_activity_idx1 ON workspace_activity(namespace_id, workspace_id);



DROP TABLE IF EXISTS work_hour cascade;
CREATE TABLE work_hour (
      namespace_id uuid NOT NULL,
      id bigserial,
      work_id uuid NOT NULL,
      type text not null,
      user_id integer not null,
      description text not null,
      work_date date not null,
      hours real default 0,
      comment text not null,
      created_by integer not null,
      created_date timestamp with time zone not null,
      modified_by integer not null,
      modified_date timestamp with time zone not null,
      PRIMARY KEY (namespace_id, id),
      CONSTRAINT namespace_fk FOREIGN KEY(namespace_id)
                     	  REFERENCES namespace(id),
      CONSTRAINT work_fk FOREIGN KEY(namespace_id, work_id)
                     	  REFERENCES work(namespace_id, id),
      CONSTRAINT user_fk FOREIGN KEY(namespace_id, user_id)
                     	  REFERENCES namespace_user(namespace_id, id),
      CONSTRAINT created_by_fk FOREIGN KEY(namespace_id, created_by)
                     	  REFERENCES namespace_user(namespace_id, id),
      CONSTRAINT modified_by_fk FOREIGN KEY(namespace_id, modified_by)
                     	  REFERENCES namespace_user(namespace_id, id)
);
CREATE INDEX work_hour_idx1 ON work_hour(namespace_id, type);
CREATE INDEX work_hour_idx2 ON work_hour(namespace_id, user_id);


DROP TABLE IF EXISTS event_audit cascade;
CREATE TABLE event_audit (
      namespace_id uuid not null,
      id BIGSERIAL,
      entry_id bigint,
      action text not null,
      params text,
      txid text not null,
      created_by integer not null,
      created_date timestamp with time zone not null,
      PRIMARY KEY (namespace_id, id),
      CONSTRAINT created_by_fk FOREIGN KEY(namespace_id, created_by)
                          REFERENCES namespace_user(namespace_id, id)
);
CREATE INDEX event_audit_idx1 ON event_audit(namespace_id, entry_id);
CREATE INDEX event_audit_idx2 ON event_audit(namespace_id, created_by);

DROP TABLE IF EXISTS message_template CASCADE;
create table message_template(
   namespace_id uuid not null,
   id SERIAL,
   name text not null,
   type text not null,
   subject text not null,
   notification text not null,
   email_body text not null,
   created_date timestamp with time zone  NOT NULL,
   created_by integer  NOT NULL,
   PRIMARY KEY (namespace_id, id),
   CONSTRAINT namespace_fk FOREIGN KEY(namespace_id)
                          REFERENCES namespace(id),
   CONSTRAINT created_by_fk FOREIGN KEY(namespace_id, created_by)
                          REFERENCES namespace_user(namespace_id, id)
);
create unique index message_template_u1 on message_template(namespace_id, name);

DROP TABLE IF EXISTS email_message cascade;
CREATE TABLE email_message (
      namespace_id uuid not null,
      id BIGSERIAL,
      workspace_id uuid,
      email_to  text,
      cc  text,
      status text,
      retry integer not null default 0,
      delivery_date timestamp with time zone,
      expiration_date timestamp with time zone not null,
      template_id integer not null,
      params jsonb not null,
      PRIMARY KEY (namespace_id, id),
      CONSTRAINT workspace_fk FOREIGN KEY(namespace_id, workspace_id)
                          REFERENCES workspace(namespace_id, id),
      CONSTRAINT template_fk FOREIGN KEY(namespace_id, template_id)
                          REFERENCES message_template(namespace_id, id)
);

DROP TABLE IF EXISTS number CASCADE;
create table number(
   namespace_id uuid not null,
   id SERIAL,
   type text not null,
   name text not null,
   prefix text not null,
   length int null,
   suffix text,
   created_date timestamp with time zone NOT NULL,
   created_by integer  NOT NULL,
   PRIMARY KEY (namespace_id, id),
   CONSTRAINT namespace_fk FOREIGN KEY(namespace_id)
                          REFERENCES namespace(id),
   CONSTRAINT created_by_fk FOREIGN KEY(namespace_id, created_by)
                          REFERENCES namespace_user(namespace_id, id)
);

DROP TABLE IF EXISTS number_sequence CASCADE;
create table number_sequence(
   namespace_id uuid not null,
   id SERIAL,
   prefix text not null,
   current_seq integer not null,
   version integer default 0,
   PRIMARY KEY (namespace_id, id)
);
create unique index number_sequence_u1 on number_sequence(namespace_id, prefix);

DROP TABLE IF EXISTS number_history CASCADE;
create table number_history(
   namespace_id uuid not null,
   id BIGSERIAL,
   number text not null,
   created_date timestamp with time zone  NOT NULL,
   created_by integer  NOT NULL,
   PRIMARY KEY (namespace_id, id),
   CONSTRAINT created_by_fk FOREIGN KEY(namespace_id, created_by)
                          REFERENCES namespace_user(namespace_id, id)
);
create unique index history_u1 on number_history(namespace_id, number);

DROP TABLE IF EXISTS message_template CASCADE;
create table message_template(
   namespace_id uuid not null,
   id SERIAL,
   name text not null,
   type text not null,
   subject text not null,
   notification text not null,
   email_body text not null,
   created_date timestamp with time zone  NOT NULL,
   created_by integer  NOT NULL,
   PRIMARY KEY (namespace_id, id),
   CONSTRAINT namespace_fk FOREIGN KEY(namespace_id)
                          REFERENCES namespace(id),
   CONSTRAINT created_by_fk FOREIGN KEY(namespace_id, created_by)
                          REFERENCES namespace_user(namespace_id, id)
);
create unique index message_template_u1 on message_template(namespace_id, name);


DROP TABLE IF EXISTS template CASCADE;
create table template(
   namespace_id uuid not null,
   id SERIAL,
   type text not null,
   name text not null,
   description text not null,
   is_private boolean not null default false,
   template jsonb not null,
   created_by integer  NOT NULL,
   created_date timestamp with time zone  NOT NULL,
   modified_by integer  NOT NULL,
   modified_date timestamp with time zone  NOT NULL,
   PRIMARY KEY (namespace_id, id),
   CONSTRAINT namespace_fk FOREIGN KEY(namespace_id)
                          REFERENCES namespace(id),
   CONSTRAINT created_by_fk FOREIGN KEY(namespace_id, created_by)
                          REFERENCES namespace_user(namespace_id, id),
   CONSTRAINT modified_by_fk FOREIGN KEY(namespace_id, modified_by)
                          REFERENCES namespace_user(namespace_id, id)
);
CREATE UNIQUE INDEX template_u1 ON template (namespace_id, type, name);
CREATE INDEX template_idx1 ON template (namespace_id, type);


