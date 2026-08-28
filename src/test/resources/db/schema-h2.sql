DROP TABLE IF EXISTS expression_calib;
DROP TABLE IF EXISTS roadmap;
DROP TABLE IF EXISTS mission_log;
DROP TABLE IF EXISTS story_session;
DROP TABLE IF EXISTS story_node;
DROP TABLE IF EXISTS story;
DROP TABLE IF EXISTS child;
DROP TABLE IF EXISTS member;

CREATE TABLE member
(
    member_id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    login_id             VARCHAR(50)  NOT NULL,
    password             VARCHAR(255) NOT NULL,
    parent_pin           VARCHAR(255),
    name                 VARCHAR(50)  NOT NULL,
    email                VARCHAR(255) NOT NULL,
    phone                VARCHAR(20),
    relation             VARCHAR(10),
    agree_service        TINYINT      NOT NULL DEFAULT 0,
    agree_privacy        TINYINT      NOT NULL DEFAULT 0,
    agree_sensitive      TINYINT      NOT NULL DEFAULT 0,
    agreed_at            TIMESTAMP,
    notify_weekly_report TINYINT      NOT NULL DEFAULT 1,
    notify_reminder      TINYINT      NOT NULL DEFAULT 1,
    agree_marketing      TINYINT      NOT NULL DEFAULT 0,
    created_at           TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at           TIMESTAMP,
    CONSTRAINT uq_member_login_id UNIQUE (login_id),
    CONSTRAINT uq_member_email UNIQUE (email)
);

CREATE TABLE child
(
    child_id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id          BIGINT      NOT NULL,
    name               VARCHAR(50) NOT NULL,
    birth_date         DATE        NOT NULL,
    gender             CHAR(1),
    disorder_type      VARCHAR(20),
    severity           VARCHAR(10),
    character_type     VARCHAR(20),
    character_nickname VARCHAR(50),
    created_at         TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at         TIMESTAMP,
    CONSTRAINT uq_child_member UNIQUE (member_id),
    CONSTRAINT fk_child_member FOREIGN KEY (member_id) REFERENCES member (member_id)
);

CREATE TABLE story
(
    story_id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    child_id       BIGINT,
    title          VARCHAR(100) NOT NULL,
    situation_type VARCHAR(30),
    emotion        VARCHAR(12),
    is_generated   BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_story_child FOREIGN KEY (child_id) REFERENCES child (child_id) ON DELETE CASCADE
);

CREATE TABLE story_node
(
    node_id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    story_id      BIGINT      NOT NULL,
    node_order    INT         NOT NULL,
    stage_type    VARCHAR(12) NOT NULL,
    narration     CLOB        NOT NULL,
    question_text VARCHAR(255),
    choice_data   CLOB,
    CONSTRAINT fk_node_story FOREIGN KEY (story_id) REFERENCES story (story_id) ON DELETE CASCADE,
    CONSTRAINT uq_node_order UNIQUE (story_id, node_order)
);

CREATE TABLE story_session
(
    session_id  BIGINT AUTO_INCREMENT PRIMARY KEY,
    child_id    BIGINT      NOT NULL,
    story_id    BIGINT      NOT NULL,
    roadmap_id  BIGINT,
    daily_input CLOB,
    started_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ended_at    TIMESTAMP,
    status      VARCHAR(12) NOT NULL DEFAULT 'INCOMPLETE',
    prepared    BOOLEAN     NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_session_child FOREIGN KEY (child_id) REFERENCES child (child_id) ON DELETE CASCADE,
    CONSTRAINT fk_session_story FOREIGN KEY (story_id) REFERENCES story (story_id)
);

CREATE TABLE mission_log
(
    log_id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id     BIGINT      NOT NULL,
    node_id        BIGINT,
    mission_type   VARCHAR(12) NOT NULL,
    target_value   VARCHAR(20) NOT NULL,
    response_value VARCHAR(50),
    is_success     BOOLEAN     NOT NULL,
    created_at     TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_log_node FOREIGN KEY (node_id) REFERENCES story_node (node_id) ON DELETE SET NULL,
    CONSTRAINT fk_log_session FOREIGN KEY (session_id) REFERENCES story_session (session_id) ON DELETE CASCADE
);

CREATE TABLE roadmap
(
    roadmap_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    child_id     BIGINT      NOT NULL,
    roadmap_type VARCHAR(10) NOT NULL,
    step_data    CLOB        NOT NULL,
    is_active    BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_roadmap_child FOREIGN KEY (child_id) REFERENCES child (child_id) ON DELETE CASCADE
);

CREATE TABLE expression_calib
(
    calib_id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    child_id      BIGINT      NOT NULL,
    emotion_type  VARCHAR(10) NOT NULL,
    landmark_data CLOB        NOT NULL,
    media_url     VARCHAR(500),
    created_at    TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_calib_child FOREIGN KEY (child_id) REFERENCES child (child_id) ON DELETE CASCADE,
    CONSTRAINT uq_calib_child_emotion UNIQUE (child_id, emotion_type)
);

INSERT INTO member (member_id, login_id, password, name, email)
VALUES (1, 'test.parent1@kkeudeok.local', 'TEST-DATA-NOT-A-REAL-PASSWORD-HASH', '김보호',
        'test.parent1@kkeudeok.local');

INSERT INTO child (child_id, member_id, name, birth_date, gender, disorder_type, severity, character_type)
VALUES (1, 1, '김지우', '2020-04-17', 'F', '자폐', '경도', 'tori');
