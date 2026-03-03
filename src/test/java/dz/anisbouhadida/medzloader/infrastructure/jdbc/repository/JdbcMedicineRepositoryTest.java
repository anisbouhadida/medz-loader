package dz.anisbouhadida.medzloader.infrastructure.jdbc.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/// Unit tests for [JdbcMedicineRepository].
///
/// Verifies the query dispatching and the `Optional` wrapping logic,
/// including the [EmptyResultDataAccessException] → [Optional#empty()] conversion.
@DisplayName("JdbcMedicineRepository")
@ExtendWith(MockitoExtension.class)
class JdbcMedicineRepositoryTest {

  @Mock private NamedParameterJdbcTemplate jdbcTemplate;

  private JdbcMedicineRepository repository;

  private static final String REGISTRATION_NUMBER = "035/01 A 003/17/23";
  private static final String CODE = "01 A 003";
  private static final String INN = "PARACETAMOL";
  private static final String BRAND_NAME = "DOLIPRANE";
  private static final String LABORATORY_HOLDER = "SANOFI";

  @BeforeEach
  void setUp() {
    repository = new JdbcMedicineRepository(jdbcTemplate);
  }

  @Nested
  @DisplayName("findVersionByRegistrationNumber()")
  class FindVersionByRegistrationNumberTests {

    @Test
    @DisplayName("should return an Optional containing the version when a matching row is found")
    void findVersionByRegistrationNumber_should_returnOptionalWithVersion_when_rowExists() {
      // Arrange
      when(jdbcTemplate.queryForObject(
              any(String.class), any(MapSqlParameterSource.class), eq(Integer.class)))
          .thenReturn(7);

      // Act
      Optional<Integer> result =
          repository.findVersionByRegistrationNumber(
              REGISTRATION_NUMBER, CODE, INN, BRAND_NAME, LABORATORY_HOLDER);

      // Assert
      assertThat(result).isPresent().contains(7);
    }

    @Test
    @DisplayName("should return Optional.empty() when EmptyResultDataAccessException is thrown")
    void findVersionByRegistrationNumber_should_returnEmpty_when_noRowFound() {
      // Arrange
      when(jdbcTemplate.queryForObject(
              any(String.class), any(MapSqlParameterSource.class), eq(Integer.class)))
          .thenThrow(new EmptyResultDataAccessException(1));

      // Act
      Optional<Integer> result =
          repository.findVersionByRegistrationNumber(
              REGISTRATION_NUMBER, CODE, INN, BRAND_NAME, LABORATORY_HOLDER);

      // Assert
      assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("should return Optional.empty() when the query returns null")
    void findVersionByRegistrationNumber_should_returnEmpty_when_queryReturnsNull() {
      // Arrange
      when(jdbcTemplate.queryForObject(
              any(String.class), any(MapSqlParameterSource.class), eq(Integer.class)))
          .thenReturn(null);

      // Act
      Optional<Integer> result =
          repository.findVersionByRegistrationNumber(
              REGISTRATION_NUMBER, CODE, INN, BRAND_NAME, LABORATORY_HOLDER);

      // Assert
      assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("should return Optional.of(0) when the stored version is 0")
    void findVersionByRegistrationNumber_should_returnOptionalOfZero_when_versionIsZero() {
      // Arrange
      when(jdbcTemplate.queryForObject(
              any(String.class), any(MapSqlParameterSource.class), eq(Integer.class)))
          .thenReturn(0);

      // Act
      Optional<Integer> result =
          repository.findVersionByRegistrationNumber(
              REGISTRATION_NUMBER, CODE, INN, BRAND_NAME, LABORATORY_HOLDER);

      // Assert
      assertThat(result).isPresent().contains(0);
    }

    @Test
    @DisplayName("should return Optional containing a high version number")
    void findVersionByRegistrationNumber_should_returnHighVersion_when_manyUpdatesOccurred() {
      // Arrange
      when(jdbcTemplate.queryForObject(
              any(String.class), any(MapSqlParameterSource.class), eq(Integer.class)))
          .thenReturn(Integer.MAX_VALUE);

      // Act
      Optional<Integer> result =
          repository.findVersionByRegistrationNumber(
              REGISTRATION_NUMBER, CODE, INN, BRAND_NAME, LABORATORY_HOLDER);

      // Assert
      assertThat(result).isPresent().contains(Integer.MAX_VALUE);
    }

    @Test
    @DisplayName("should return Optional.empty() when all arguments are null and no row is found")
    void findVersionByRegistrationNumber_should_returnEmpty_when_nullArgumentsAndNoRow() {
      // Arrange
      when(jdbcTemplate.queryForObject(
              any(String.class), any(MapSqlParameterSource.class), eq(Integer.class)))
          .thenThrow(new EmptyResultDataAccessException(1));

      // Act
      Optional<Integer> result =
          repository.findVersionByRegistrationNumber(null, null, null, null, null);

      // Assert
      assertThat(result).isEmpty();
    }
  }
}
