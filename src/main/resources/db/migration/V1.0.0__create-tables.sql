CREATE TABLE process_definition (
    id VARCHAR(255) NOT NULL,
    process_id VARCHAR(255),
    process_name VARCHAR(255),
    process_version VARCHAR(255),
    resource_id VARCHAR(255),
    lock INTEGER DEFAULT 0,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    resource_version BIGINT,
    CONSTRAINT process_definition_pkey PRIMARY KEY (id),
    CONSTRAINT process_definition_process UNIQUE (process_id, process_version)
);

CREATE TABLE process_instance (
    id VARCHAR(255) NOT NULL,
    process_definition_id VARCHAR(255),
    process_definition_resource_id VARCHAR(255),
    process_id VARCHAR(255),
    process_version VARCHAR(255),
    values BYTEA,
    status VARCHAR(255),
    lock INTEGER DEFAULT 0,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT process_instance_pkey PRIMARY KEY (id)
);

CREATE TABLE resource_data (
    id VARCHAR(255) NOT NULL,
    data BYTEA,
    CONSTRAINT resource_data_pkey PRIMARY KEY (id)
);

CREATE TABLE resource (
    checksum BIGINT,
    version BIGINT,
    deployment_request_id VARCHAR(255),
    filename VARCHAR(255),
    id VARCHAR(255) NOT NULL,
    process_definition_id VARCHAR(255),
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT resource_pkey PRIMARY KEY (id),
    CONSTRAINT resource_pd_version UNIQUE (process_definition_id, version)
);

CREATE TABLE job (
    id VARCHAR(255) NOT NULL,
    lock INTEGER DEFAULT 0,
    type VARCHAR(255),
    worker VARCHAR(255),
    retry_count INTEGER,
    lock_to TIMESTAMP WITHOUT TIME ZONE,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    take_from TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(255),
    custom_headers BYTEA,
    variables BYTEA,
    output BYTEA,
    process_definition_version BIGINT,
    element_id VARCHAR(255),
    process_definition_id VARCHAR(255),
    process_id VARCHAR(255),
    process_instance_id VARCHAR(255),
    process_version VARCHAR(255),
    error_code VARCHAR(255),
    error_message VARCHAR(255),
    state VARCHAR(255),
    lock_key VARCHAR(255),
    token_type VARCHAR(32),
    token BYTEA,
    CONSTRAINT job_pkey PRIMARY KEY (id)
);

CREATE TABLE gateway_instance (
    scope_id VARCHAR(255),
    process_instance_id VARCHAR(255),
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    element_id VARCHAR(255),
    from_element_id VARCHAR(255),
    token BYTEA
);

CREATE TABLE stream (
    id BIGSERIAL PRIMARY KEY,
    attempt_no INTEGER NOT NULL DEFAULT 0,
    parent_id VARCHAR(32) NOT NULL,
    type VARCHAR(32) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    value BYTEA
);

-- PARTITION BY RANGE (created_at)

-- CREATE UNIQUE INDEX idx_stream_parent_id ON stream(parent_id);

--     dedup_key VARCHAR(32),
-- CREATE UNIQUE INDEX IF NOT EXISTS idx_stream_dedup ON stream(dedup_key) WHERE dedup_key IS NOT NULL;

-- CREATE INDEX idx_stream_created_at_brin ON stream USING BRIN (created_at);

CREATE TABLE stream_attempts (
    id BIGSERIAL PRIMARY KEY,
    event_id BIGINT NOT NULL REFERENCES stream(id) ON DELETE CASCADE,
    attempt_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    status VARCHAR(2) NOT NULL,
    processor_id VARCHAR(255),
    error TEXT
);
CREATE INDEX idx_stream_eventid_attempts ON stream_attempts(event_id, attempt_at DESC);

CREATE FUNCTION stream_notify() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
    PERFORM pg_notify('execute', NEW.id::text);
    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_stream_notify AFTER INSERT ON stream FOR EACH ROW EXECUTE FUNCTION stream_notify();