package dz.anisbouhadida.medzloader.domain.model;

import java.nio.file.Path;

/// Metadata for one successfully loaded medicine release.
///
/// @param releaseVersion source release in `YYYY-MM` format
/// @param sourceDirectory source directory relative to the configured input directory
/// @param jobExecutionId Spring Batch job execution identifier
/// @param loadStepExecutionId Spring Batch load-step execution identifier
/// @param readCount number of rows read
/// @param writeCount number of events written
/// @param filterCount number of rows filtered by processing
/// @param skipCount total number of read, process, and write skips
public record DataSyncHistory(
    String releaseVersion,
    Path sourceDirectory,
    long jobExecutionId,
    long loadStepExecutionId,
    long readCount,
    long writeCount,
    long filterCount,
    long skipCount) {}
