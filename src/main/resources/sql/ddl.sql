-- =====================================================
-- ENUM TYPES
-- =====================================================

DO $$
BEGIN
CREATE TYPE medicine_type AS ENUM ('GE', 'RE', 'BIO');
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$
BEGIN
CREATE TYPE medicine_origin AS ENUM ('MANUFACTURED', 'IMPORTED');
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$
BEGIN
CREATE TYPE medicine_status AS ENUM ('ACTIVE', 'WITHDRAWN', 'MARKED_NOT_RENEWED');
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$
BEGIN
CREATE TYPE medicine_event_type AS ENUM ('UPSERT', 'WITHDRAWAL', 'NON_RENEWAL');
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;


-- =====================================================
-- MAIN TABLE
-- =====================================================

CREATE TABLE IF NOT EXISTS medicine (

    -- Surrogate primary key
                                        medicine_id BIGSERIAL PRIMARY KEY,

    -- External identifiers
                                        registration_number TEXT NOT NULL,

                                        code TEXT,
                                        icd TEXT,
                                        brand_name TEXT,
                                        form TEXT,
                                        dosage TEXT,
                                        packaging TEXT,
                                        list TEXT,
                                        p1 TEXT,
                                        p2 TEXT,

                                        laboratory_holder TEXT,
                                        laboratory_country TEXT,

                                        initial_registration_date TIMESTAMPTZ,

                                        type   medicine_type,
                                        origin medicine_origin,

                                        version INTEGER NOT NULL DEFAULT 0,
                                        last_updated TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT chk_registration_number_not_blank
    CHECK (btrim(registration_number) <> '')
    );


-- =====================================================
-- BUSINESS KEY UNIQUENESS
-- Prevent duplicates for the same medicine definition
-- =====================================================

CREATE UNIQUE INDEX IF NOT EXISTS uq_medicine_business_key
    ON medicine (
    registration_number,
    code,
    icd,
    brand_name,
    laboratory_holder
    )
    NULLS NOT DISTINCT;


-- =====================================================
-- INDEXES FOR COMMON ACCESS PATTERNS
-- =====================================================

-- Lookup by registration number
CREATE INDEX IF NOT EXISTS idx_medicine_registration_number
    ON medicine (registration_number);

-- Lookup by laboratory
CREATE INDEX IF NOT EXISTS idx_medicine_laboratory
    ON medicine (laboratory_holder);

-- Filter by type/origin
CREATE INDEX IF NOT EXISTS idx_medicine_type_origin
    ON medicine (type, origin);

-- Last updated queries
CREATE INDEX IF NOT EXISTS idx_medicine_last_updated
    ON medicine (last_updated DESC);


-- =====================================================
-- STATUS HISTORY
-- =====================================================

CREATE TABLE IF NOT EXISTS medicine_status_history (

                                                       medicine_id BIGINT NOT NULL
                                                       REFERENCES medicine(medicine_id) ON DELETE CASCADE,

    status medicine_status NOT NULL,

    status_timestamp TIMESTAMPTZ NOT NULL DEFAULT now(),

    PRIMARY KEY (medicine_id, status)
    );


CREATE INDEX IF NOT EXISTS idx_msh_status_timestamp
    ON medicine_status_history (status_timestamp DESC);

CREATE INDEX IF NOT EXISTS idx_msh_medicine_timestamp
    ON medicine_status_history (medicine_id, status_timestamp DESC);



-- =====================================================
-- NOMENCLATURE EVENT
-- One row per medicine
-- =====================================================

CREATE TABLE IF NOT EXISTS nomenclature_event (

                                                  medicine_id BIGINT PRIMARY KEY
                                                  REFERENCES medicine(medicine_id) ON DELETE CASCADE,

    final_registration_date TIMESTAMPTZ,
    stability_duration TEXT,
    observations TEXT,

    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
    );

CREATE INDEX IF NOT EXISTS idx_nomenclature_updated
    ON nomenclature_event (updated_at DESC);



-- =====================================================
-- NON RENEWAL EVENT
-- =====================================================

CREATE TABLE IF NOT EXISTS non_renewal_event (

                                                 medicine_id BIGINT PRIMARY KEY
                                                 REFERENCES medicine(medicine_id) ON DELETE CASCADE,

    final_registration_date TIMESTAMPTZ,
    observations TEXT,

    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
    );

CREATE INDEX IF NOT EXISTS idx_nonrenewal_updated
    ON non_renewal_event (updated_at DESC);



-- =====================================================
-- WITHDRAWAL EVENT
-- =====================================================

CREATE TABLE IF NOT EXISTS withdrawal_event (

                                                medicine_id BIGINT PRIMARY KEY
                                                REFERENCES medicine(medicine_id) ON DELETE CASCADE,

    withdrawal_date TIMESTAMPTZ,
    withdrawal_reason TEXT,

    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
    );

CREATE INDEX IF NOT EXISTS idx_withdrawal_updated
    ON withdrawal_event (updated_at DESC);



-- =====================================================
-- GENERIC EVENT HISTORY
-- One row per medicine + event type
-- =====================================================

CREATE TABLE IF NOT EXISTS medicine_event_history (

                                                      medicine_id BIGINT NOT NULL
                                                      REFERENCES medicine(medicine_id) ON DELETE CASCADE,

    event_type medicine_event_type NOT NULL,

    event_date TIMESTAMPTZ NOT NULL DEFAULT now(),

    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    PRIMARY KEY (medicine_id, event_type)
    );


CREATE INDEX IF NOT EXISTS idx_meh_updated
    ON medicine_event_history (updated_at DESC);

CREATE INDEX IF NOT EXISTS idx_meh_medicine_updated
    ON medicine_event_history (medicine_id, updated_at DESC);


-- =====================================================
-- SUCCESSFUL RELEASE SYNCHRONIZATION HISTORY
-- One row per completed release load-step execution
-- =====================================================

CREATE TABLE IF NOT EXISTS data_sync_history (

    sync_id BIGSERIAL PRIMARY KEY,

    release_version VARCHAR(7) NOT NULL,
    source_directory TEXT NOT NULL,
    loaded_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    job_execution_id BIGINT NOT NULL,
    load_step_execution_id BIGINT NOT NULL,

    read_count BIGINT NOT NULL,
    write_count BIGINT NOT NULL,
    filter_count BIGINT NOT NULL,
    skip_count BIGINT NOT NULL,

    CONSTRAINT chk_data_sync_release_version
        CHECK (release_version ~ '^[0-9]{4}-(0[1-9]|1[0-2])$'),

    CONSTRAINT uq_data_sync_load_step_execution
        UNIQUE (load_step_execution_id)
    );

CREATE INDEX IF NOT EXISTS idx_data_sync_latest_release
    ON data_sync_history (release_version DESC, loaded_at DESC);
