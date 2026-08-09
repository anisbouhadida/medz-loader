package dz.anisbouhadida.medzloader.batch.support.audit;

import static dz.anisbouhadida.medzloader.batch.support.utils.MedicineSqlUtils.loadSql;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

/// Persists field-level medicine import issues for later diagnosis.
@Component
@RequiredArgsConstructor
public class MedicineImportIssueRecorder {

  private static final String INSERT_SQL = loadSql("sql/write/medicine-import-issue-insert.sql");

  private final NamedParameterJdbcTemplate jdbcTemplate;

  /// Persists an issue in the current chunk transaction.
  ///
  /// @param issue issue to persist
  public void record(MedicineImportIssue issue) {
    var parameters =
        new MapSqlParameterSource()
            .addValue("release", issue.release())
            .addValue("jobExecutionId", issue.jobExecutionId())
            .addValue("sourceFile", issue.sourceFile())
            .addValue("sourceItemId", issue.sourceItemId())
            .addValue("registrationNumber", issue.registrationNumber())
            .addValue("code", issue.code())
            .addValue("brandName", issue.brandName())
            .addValue("laboratoryHolder", issue.laboratoryHolder())
            .addValue("disposition", issue.disposition().name())
            .addValue("fieldName", issue.fieldName())
            .addValue("rejectedValue", issue.rejectedValue())
            .addValue("message", issue.message());
    jdbcTemplate.update(INSERT_SQL, parameters);
  }
}
