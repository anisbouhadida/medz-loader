INSERT INTO data_sync_history (
    release_version,
    source_directory,
    job_execution_id,
    load_step_execution_id,
    read_count,
    write_count,
    filter_count,
    skip_count
) VALUES (
    :releaseVersion,
    :sourceDirectory,
    :jobExecutionId,
    :loadStepExecutionId,
    :readCount,
    :writeCount,
    :filterCount,
    :skipCount
)
ON CONFLICT (load_step_execution_id) DO NOTHING;
