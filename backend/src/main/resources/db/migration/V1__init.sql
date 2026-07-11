CREATE EXTENSION IF NOT EXISTS postgis;



CREATE TABLE users (
    id              UUID PRIMARY KEY,
    email           VARCHAR(254) NOT NULL UNIQUE,
    password_hash   VARCHAR(256) NOT NULL,
    phone           VARCHAR(32),
    role            VARCHAR(16)  NOT NULL DEFAULT 'USER',
    status          VARCHAR(16)  NOT NULL DEFAULT 'ACTIVE',
    email_verified  BOOLEAN      NOT NULL DEFAULT FALSE,
    last_active_at  TIMESTAMPTZ,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE refresh_tokens (
    id          UUID PRIMARY KEY,
    user_id     UUID         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    token_hash  VARCHAR(64)  NOT NULL UNIQUE,
    device_info VARCHAR(256),
    expires_at  TIMESTAMPTZ  NOT NULL,
    revoked     BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX idx_refresh_tokens_user ON refresh_tokens (user_id);

CREATE TABLE email_verification_tokens (
    id         UUID PRIMARY KEY,
    user_id    UUID        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ NOT NULL,
    used_at    TIMESTAMPTZ
);

CREATE TABLE password_reset_tokens (
    id         UUID PRIMARY KEY,
    user_id    UUID        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ NOT NULL,
    used_at    TIMESTAMPTZ
);



CREATE TABLE profiles (
    user_id         UUID PRIMARY KEY REFERENCES users (id) ON DELETE CASCADE,
    display_name    VARCHAR(40) NOT NULL,
    birth_date      DATE        NOT NULL,
    gender          VARCHAR(16) NOT NULL,
    interested_in   TEXT[]      NOT NULL,
    bio             TEXT,
    location        GEOMETRY(Point, 4326),
    city            VARCHAR(120),
    min_age_pref    INT         NOT NULL DEFAULT 18,
    max_age_pref    INT         NOT NULL DEFAULT 100,
    max_distance_km INT         NOT NULL DEFAULT 50,
    is_incognito    BOOLEAN     NOT NULL DEFAULT FALSE,
    is_discoverable BOOLEAN     NOT NULL DEFAULT TRUE,
    is_verified     BOOLEAN     NOT NULL DEFAULT FALSE,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_age_prefs CHECK (min_age_pref >= 18 AND max_age_pref <= 100 AND min_age_pref <= max_age_pref)
);
CREATE INDEX idx_profiles_location ON profiles USING GIST (location);



CREATE TABLE photos (
    id              UUID PRIMARY KEY,
    user_id         UUID         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    position        INT          NOT NULL,
    storage_key     VARCHAR(256) NOT NULL,
    thumb_key       VARCHAR(256),
    thumb_small_key VARCHAR(256),
    status          VARCHAR(16)  NOT NULL DEFAULT 'PENDING',
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX idx_photos_user ON photos (user_id, position);
CREATE INDEX idx_photos_pending ON photos (created_at) WHERE status = 'PENDING';



CREATE TABLE swipes (
    id           UUID PRIMARY KEY,
    from_user_id UUID        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    to_user_id   UUID        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    action       VARCHAR(16) NOT NULL,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_swipes_pair UNIQUE (from_user_id, to_user_id),
    CONSTRAINT chk_swipes_not_self CHECK (from_user_id <> to_user_id)
);
CREATE INDEX idx_swipes_to_user ON swipes (to_user_id, action);
CREATE INDEX idx_swipes_from_created ON swipes (from_user_id, created_at DESC);

CREATE TABLE matches (
    id           UUID PRIMARY KEY,
    user_a_id    UUID        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    user_b_id    UUID        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    matched_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    unmatched_at TIMESTAMPTZ,
    unmatched_by UUID,
    CONSTRAINT uq_matches_pair UNIQUE (user_a_id, user_b_id),
    CONSTRAINT chk_matches_order CHECK (user_a_id < user_b_id)
);
CREATE INDEX idx_matches_user_a ON matches (user_a_id) WHERE unmatched_at IS NULL;
CREATE INDEX idx_matches_user_b ON matches (user_b_id) WHERE unmatched_at IS NULL;



CREATE TABLE blocked_users (
    blocker_id UUID        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    blocked_id UUID        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (blocker_id, blocked_id)
);
CREATE INDEX idx_blocked_users_blocked ON blocked_users (blocked_id);


CREATE TABLE notifications (
    id         UUID PRIMARY KEY,
    user_id    UUID        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    type       VARCHAR(32) NOT NULL,
    payload    JSONB       NOT NULL DEFAULT '{}'::jsonb,
    is_read    BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_notifications_user_created ON notifications (user_id, created_at DESC);
CREATE INDEX idx_notifications_unread ON notifications (user_id) WHERE is_read = FALSE;



CREATE TABLE reports (
    id             UUID PRIMARY KEY,
    reporter_id    UUID         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    target_user_id UUID         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    reason         VARCHAR(32)  NOT NULL,
    description    VARCHAR(1000),
    status         VARCHAR(16)  NOT NULL DEFAULT 'PENDING',
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    reviewed_by    UUID,
    reviewed_at    TIMESTAMPTZ
);
CREATE INDEX idx_reports_status ON reports (status, created_at);

CREATE TABLE moderation_actions (
    id             UUID PRIMARY KEY,
    moderator_id   UUID        NOT NULL REFERENCES users (id),
    target_user_id UUID        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    action         VARCHAR(32) NOT NULL,
    reason         VARCHAR(500),
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_moderation_actions_target ON moderation_actions (target_user_id, created_at DESC);



CREATE TABLE feature_flags (
    flag_key    VARCHAR(64) PRIMARY KEY,
    enabled     BOOLEAN      NOT NULL DEFAULT FALSE,
    description VARCHAR(300),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

INSERT INTO feature_flags (flag_key, enabled, description)
VALUES ('likes_you_screen', TRUE, 'Экран «Кто лайкнул тебя» с блюр-превью'),
       ('icebreakers', TRUE, 'Стартовые фразы для нового мэтча'),
       ('undo_swipe', TRUE, 'Отмена последнего свайпа');
