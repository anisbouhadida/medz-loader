package dz.anisbouhadida.medzloader.batch.support.audit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

@DisplayName("MedicineImportIssueRecorder")
class MedicineImportIssueRecorderTest {

  @Test
  @DisplayName("should persist all issue details")
  void shouldPersistIssueDetails() {
    var jdbcTemplate = org.mockito.Mockito.mock(NamedParameterJdbcTemplate.class);
    var recorder = new MedicineImportIssueRecorder(jdbcTemplate);
    var issue =
        new MedicineImportIssue(
            "2026-08",
            42L,
            "retraits.csv",
            7,
            "REG",
            "CODE",
            "BRAND",
            "LAB",
            MedicineImportIssueDisposition.FILTERED,
            "type",
            "FPREMIX",
            "unsupported medicine type");

    recorder.record(issue);

    var captor = ArgumentCaptor.forClass(SqlParameterSource.class);
    verify(jdbcTemplate).update(anyString(), captor.capture());
    assertThat(captor.getValue().getValue("release")).isEqualTo("2026-08");
    assertThat(captor.getValue().getValue("jobExecutionId")).isEqualTo(42L);
    assertThat(captor.getValue().getValue("sourceFile")).isEqualTo("retraits.csv");
    assertThat(captor.getValue().getValue("disposition")).isEqualTo("FILTERED");
    assertThat(captor.getValue().getValue("fieldName")).isEqualTo("type");
    assertThat(captor.getValue().getValue("rejectedValue")).isEqualTo("FPREMIX");
  }
}
