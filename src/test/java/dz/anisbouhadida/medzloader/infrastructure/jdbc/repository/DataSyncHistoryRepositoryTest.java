package dz.anisbouhadida.medzloader.infrastructure.jdbc.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;

import dz.anisbouhadida.medzloader.domain.model.DataSyncHistory;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@ExtendWith(MockitoExtension.class)
@DisplayName("DataSyncHistoryRepository")
class DataSyncHistoryRepositoryTest {

  @Mock private NamedParameterJdbcTemplate jdbcTemplate;

  @Test
  void append_shouldBindEveryHistoryField() {
    var repository = new DataSyncHistoryRepository(jdbcTemplate);
    var history =
        new DataSyncHistory(
            "2026-04", Path.of("2026-04"), 41L, 42L, 100L, 90L, 8L, 2L);

    repository.append(history);

    var parameters = ArgumentCaptor.forClass(MapSqlParameterSource.class);
    verify(jdbcTemplate).update(anyString(), parameters.capture());
    assertThat(parameters.getValue().getValue("releaseVersion")).isEqualTo("2026-04");
    assertThat(parameters.getValue().getValue("sourceDirectory")).isEqualTo("2026-04");
    assertThat(parameters.getValue().getValue("jobExecutionId")).isEqualTo(41L);
    assertThat(parameters.getValue().getValue("loadStepExecutionId")).isEqualTo(42L);
    assertThat(parameters.getValue().getValue("readCount")).isEqualTo(100L);
    assertThat(parameters.getValue().getValue("writeCount")).isEqualTo(90L);
    assertThat(parameters.getValue().getValue("filterCount")).isEqualTo(8L);
    assertThat(parameters.getValue().getValue("skipCount")).isEqualTo(2L);
  }
}
