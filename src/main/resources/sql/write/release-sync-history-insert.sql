INSERT INTO release_sync_history (
    release,
    job_execution_id,
    read_count,
    write_count,
    filter_count
)
VALUES (
    :release,
    :jobExecutionId,
    :readCount,
    :writeCount,
    :filterCount
);
