package dz.anisbouhadida.medzloader.infrastructure.jdbc.repository;

import static dz.anisbouhadida.medzloader.batch.support.utils.MedicineSqlUtils.loadSql;

import dz.anisbouhadida.medzloader.domain.model.DataSyncHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/// Appends successful medicine release synchronizations to the business history table.
@Repository
@RequiredArgsConstructor
public class DataSyncHistoryRepository {

  private static final String INSERT_SQL = loadSql("sql/write/insert-data-sync-history.sql");

  private final NamedParameterJdbcTemplate jdbcTemplate;

  /// Appends a synchronization unless this load-step execution was already recorded.
  ///
  /// @param history successful release metadata
  public void append(DataSyncHistory history) {
    var parameters =
        new MapSqlParameterSource()
            .addValue("releaseVersion", history.releaseVersion())
            .addValue("sourceDirectory", history.sourceDirectory().toString())
            .addValue("jobExecutionId", history.jobExecutionId())
            .addValue("loadStepExecutionId", history.loadStepExecutionId())
            .addValue("readCount", history.readCount())
            .addValue("writeCount", history.writeCount())
            .addValue("filterCount", history.filterCount())
            .addValue("skipCount", history.skipCount());
    jdbcTemplate.update(INSERT_SQL, parameters);
  }
}
