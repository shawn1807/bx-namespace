
-- POST: one row per post
CREATE TABLE post (
  id                     uuid NOT NULL,
  namespace_id           uuid                NOT NULL,
  author_id              integer             NOT NULL, -- FK -> namespace_user(namespace_id,id)
  title                  text,
  body                   text                NOT NULL,
  props                  jsonb               NOT NULL DEFAULT '{}'::jsonb,
  active                 boolean             NOT NULL DEFAULT false,
  -- denormalized counters (kept consistent by triggers/app)
  comment_count          integer             NOT NULL DEFAULT 0,
  reshare_count          integer             NOT NULL DEFAULT 0,
  -- search
  body_tsv               tsvector,
  -- lifecycle
  expiration_date          timestamptz         NOT NULL DEFAULT now(),
  -- audit
  created_by             integer             NOT NULL,
  created_date           timestamptz         NOT NULL DEFAULT now(),
  modified_by            integer             NOT NULL,
  modified_date          timestamptz         NOT NULL DEFAULT now(),
  PRIMARY KEY (namespace_id, id),
  CONSTRAINT post_author_fk   FOREIGN KEY (namespace_id, author_id)  REFERENCES namespace_user(namespace_id, id),
  CONSTRAINT post_cby_fk      FOREIGN KEY (namespace_id, created_by) REFERENCES namespace_user(namespace_id, id),
  CONSTRAINT post_mby_fk      FOREIGN KEY (namespace_id, modified_by)REFERENCES namespace_user(namespace_id, id),
  CONSTRAINT post_ws_vis_ck   CHECK ((visibility <> 'workspace') OR (workspace_id IS NOT NULL))
);

-- GIN search over title/body/rich
CREATE INDEX post_gin_tsv ON post USING GIN (body_tsv);
CREATE INDEX post_namespace_created_brin ON post USING BRIN (namespace_id, created_date);
CREATE INDEX post_workspace_idx  ON post (namespace_id, workspace_id) WHERE visibility = 'workspace';
CREATE INDEX post_author_idx     ON post (namespace_id, author_id);

-- auto-maintain tsvector (you can also use a trigger if you prefer)
CREATE OR REPLACE FUNCTION post_tsv_update() RETURNS trigger AS $$
BEGIN
  NEW.body_tsv :=
    setweight(to_tsvector('simple', coalesce(NEW.title,'')), 'A') ||
    setweight(to_tsvector('simple', coalesce(NEW.body,'')),  'B') ||
    setweight(to_tsvector('simple', coalesce(jsonb_extract_path_text(NEW.rich,'plain')::text,'')), 'C');
  RETURN NEW;
END $$ LANGUAGE plpgsql;

CREATE TRIGGER post_tsv_trg
BEFORE INSERT OR UPDATE OF title, body, rich ON post
FOR EACH ROW EXECUTE FUNCTION post_tsv_update();


-- POST VERSION: keep edit history (diff or full copy)
CREATE TABLE post_version (
  namespace_id     uuid        NOT NULL,
  post_id          uuid      NOT NULL,
  version_no       integer     NOT NULL,
  title            text,
  body             text        NOT NULL,
  rich             jsonb,
  edited_by        integer     NOT NULL,
  edited_date      timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (namespace_id, post_id, version_no),
  CONSTRAINT pv_post_fk FOREIGN KEY (namespace_id, post_id) REFERENCES post(namespace_id, id) ON DELETE CASCADE,
  CONSTRAINT pv_editor_fk FOREIGN KEY (namespace_id, edited_by) REFERENCES namespace_user(namespace_id, id)
);

CREATE INDEX pv_recent_idx ON post_version (namespace_id, post_id, version_no DESC);


-- COMMENTS (flat + parent_id for threads)
CREATE TABLE post_comment (
  namespace_id     uuid        NOT NULL,
  id               bigserial,
  post_id          bigint      NOT NULL,
  parent_id        bigint,           -- null = top-level comment
  author_id        integer     NOT NULL,
  body             text        NOT NULL,
  rich             jsonb,
  props            jsonb       NOT NULL DEFAULT '{}'::jsonb,
  reaction_count   integer     NOT NULL DEFAULT 0,
  deleted          boolean     NOT NULL DEFAULT false,
  created_by       integer     NOT NULL,
  created_date     timestamptz NOT NULL DEFAULT now(),
  modified_by      integer     NOT NULL,
  modified_date    timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (namespace_id, id),
  CONSTRAINT pc_post_fk   FOREIGN KEY (namespace_id, post_id) REFERENCES post(namespace_id, id) ON DELETE CASCADE,
  CONSTRAINT pc_parent_fk FOREIGN KEY (namespace_id, parent_id) REFERENCES post_comment(namespace_id, id) ON DELETE CASCADE,
  CONSTRAINT pc_author_fk FOREIGN KEY (namespace_id, author_id) REFERENCES namespace_user(namespace_id, id),
  CONSTRAINT pc_cby_fk    FOREIGN KEY (namespace_id, created_by) REFERENCES namespace_user(namespace_id, id),
  CONSTRAINT pc_mby_fk    FOREIGN KEY (namespace_id, modified_by) REFERENCES namespace_user(namespace_id, id)
);

CREATE INDEX pc_post_idx     ON post_comment (namespace_id, post_id, created_date);
CREATE INDEX pc_parent_idx   ON post_comment (namespace_id, parent_id);
CREATE INDEX pc_author_idx   ON post_comment (namespace_id, author_id);


-- REACTIONS (1 per user per target)
CREATE TABLE post_reaction (
  namespace_id   uuid           NOT NULL,
  post_id        bigint,
  comment_id     bigint,
  user_id        integer        NOT NULL,
  type           reaction_type  NOT NULL,
  created_date   timestamptz    NOT NULL DEFAULT now(),
  PRIMARY KEY (namespace_id, post_id, comment_id, user_id, type),
  CONSTRAINT pr_target_ck CHECK (
    (post_id IS NOT NULL AND comment_id IS NULL) OR
    (post_id IS NULL AND comment_id IS NOT NULL)
  ),
  CONSTRAINT pr_post_fk    FOREIGN KEY (namespace_id, post_id)    REFERENCES post(namespace_id, id) ON DELETE CASCADE,
  CONSTRAINT pr_comment_fk FOREIGN KEY (namespace_id, comment_id) REFERENCES post_comment(namespace_id, id) ON DELETE CASCADE,
  CONSTRAINT pr_user_fk    FOREIGN KEY (namespace_id, user_id)    REFERENCES namespace_user(namespace_id, id)
);

CREATE INDEX pr_post_idx    ON post_reaction (namespace_id, post_id);
CREATE INDEX pr_comment_idx ON post_reaction (namespace_id, comment_id);


-- ATTACHMENTS / EMBEDS (files, links, images, etc.)
CREATE TABLE post_media (
  namespace_id   uuid        NOT NULL,
  id             bigserial,
  post_id        bigint      NOT NULL,
  uploader_id    integer     NOT NULL,
  media_type     text        NOT NULL, -- 'image','file','link','video',...
  url            text        NOT NULL,
  meta           jsonb       NOT NULL DEFAULT '{}'::jsonb, -- width/height/size/mime/etc.
  created_date   timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (namespace_id, id),
  CONSTRAINT pm_post_fk FOREIGN KEY (namespace_id, post_id)     REFERENCES post(namespace_id, id) ON DELETE CASCADE,
  CONSTRAINT pm_user_fk FOREIGN KEY (namespace_id, uploader_id) REFERENCES namespace_user(namespace_id, id)
);

CREATE INDEX pm_post_idx ON post_media (namespace_id, post_id);


-- TAGS (freeform or controlled)
CREATE TABLE tag (
  namespace_id uuid   NOT NULL,
  id           bigserial,
  name         citext NOT NULL,
  PRIMARY KEY (namespace_id, id),
  CONSTRAINT tag_unique UNIQUE (namespace_id, name)
);

CREATE TABLE post_tag (
  namespace_id uuid   NOT NULL,
  post_id      bigint NOT NULL,
  tag_id       bigint NOT NULL,
  PRIMARY KEY (namespace_id, post_id, tag_id),
  CONSTRAINT pt_post_fk FOREIGN KEY (namespace_id, post_id) REFERENCES post(namespace_id, id) ON DELETE CASCADE,
  CONSTRAINT pt_tag_fk  FOREIGN KEY (namespace_id, tag_id)  REFERENCES tag(namespace_id, id)  ON DELETE CASCADE
);

CREATE INDEX pt_tag_lookup ON post_tag (namespace_id, tag_id);


-- MENTIONS (users mentioned in post/comment)
CREATE TABLE mention (
  namespace_id uuid     NOT NULL,
  id           bigserial,
  post_id      bigint,
  comment_id   bigint,
  mentioned_id integer  NOT NULL,
  created_date timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (namespace_id, id),
  CONSTRAINT m_target_ck CHECK (
    (post_id IS NOT NULL AND comment_id IS NULL) OR
    (post_id IS NULL AND comment_id IS NOT NULL)
  ),
  CONSTRAINT m_post_fk    FOREIGN KEY (namespace_id, post_id)    REFERENCES post(namespace_id, id) ON DELETE CASCADE,
  CONSTRAINT m_comment_fk FOREIGN KEY (namespace_id, comment_id) REFERENCES post_comment(namespace_id, id) ON DELETE CASCADE,
  CONSTRAINT m_user_fk    FOREIGN KEY (namespace_id, mentioned_id) REFERENCES namespace_user(namespace_id, id)
);

CREATE INDEX mention_user_idx ON mention (namespace_id, mentioned_id, created_date);


-- CUSTOM AUDIENCE TARGETS (when visibility='custom')
-- You can target users, groups, roles, or workspaces via a polymorphic scheme.
CREATE TYPE audience_target AS ENUM ('user','group','role','workspace');

CREATE TABLE post_audience (
  namespace_id   uuid            NOT NULL,
  post_id        bigint          NOT NULL,
  target_type    audience_target NOT NULL,
  target_id      text            NOT NULL,  -- store the id in string; enforce with app logic or FK tables by type
  PRIMARY KEY (namespace_id, post_id, target_type, target_id),
  CONSTRAINT pa_post_fk FOREIGN KEY (namespace_id, post_id) REFERENCES post(namespace_id, id) ON DELETE CASCADE
);

CREATE INDEX pa_lookup_idx ON post_audience (namespace_id, target_type, target_id);


-- BOOKMARKS / SAVES
CREATE TABLE post_bookmark (
  namespace_id uuid        NOT NULL,
  post_id      bigint      NOT NULL,
  user_id      integer     NOT NULL,
  created_date timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (namespace_id, post_id, user_id),
  CONSTRAINT pb_post_fk FOREIGN KEY (namespace_id, post_id) REFERENCES post(namespace_id, id) ON DELETE CASCADE,
  CONSTRAINT pb_user_fk FOREIGN KEY (namespace_id, user_id) REFERENCES namespace_user(namespace_id, id)
);


-- OPTIONAL: cross references to system objects (tasks/docs/etc.)
CREATE TABLE post_ref (
  namespace_id uuid       NOT NULL,
  post_id      bigint     NOT NULL,
  ref_type     text       NOT NULL,         -- 'task','doc','file','event',...
  ref_id       text       NOT NULL,         -- store external ID; you can add per-type FKs in app/service
  meta         jsonb      NOT NULL DEFAULT '{}'::jsonb,
  PRIMARY KEY (namespace_id, post_id, ref_type, ref_id),
  CONSTRAINT pref_post_fk FOREIGN KEY (namespace_id, post_id) REFERENCES post(namespace_id, id) ON DELETE CASCADE
);
