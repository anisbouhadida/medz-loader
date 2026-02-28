-- ============================================================
-- NUCLEAR RESET (DEV / TEST ONLY)
-- Drops all application + Spring Batch tables quickly
-- ============================================================

DROP TABLE IF EXISTS
    medicine,
    medicine_status_history,
    nomenclature_event,
    non_renewal_event,
    withdrawal_event,
    medicine_event_history,
    BATCH_STEP_EXECUTION_CONTEXT,
    BATCH_STEP_EXECUTION,
    BATCH_JOB_EXECUTION_CONTEXT,
    BATCH_JOB_EXECUTION_PARAMS,
    BATCH_JOB_EXECUTION,
    BATCH_JOB_INSTANCE
    CASCADE;

-- Drop Spring Batch sequences if present
DROP SEQUENCE IF EXISTS
    BATCH_STEP_EXECUTION_SEQ,
    BATCH_JOB_EXECUTION_SEQ,
    BATCH_JOB_SEQ
    CASCADE;

-- Drop possible leftover enum types from old schema
DROP TYPE IF EXISTS
    medicine_type,
    medicine_origin,
    medicine_status,
    medicine_event_type
    CASCADE;