package dz.anisbouhadida.medzloader.infrastructure.jdbc.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dz.anisbouhadida.medzloader.infrastructure.jdbc.repository.MedicineRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/// Unit tests for [MedicineAdapter].
///
/// Verifies that the adapter correctly bridges the
// [dz.anisbouhadida.medzloader.domain.spi.MedicinePort]
/// SPI to [MedicineRepository], and that the `Optional`-to-primitive
/// conversion (empty → 0) is applied correctly.
@DisplayName("MedicineAdapter")
@ExtendWith(MockitoExtension.class)
class MedicineAdapterTest {

  @Mock private MedicineRepository medicineRepository;

  @InjectMocks private MedicineAdapter medicineAdapter;

  private static final String REGISTRATION_NUMBER = "035/01 A 003/17/23";
  private static final String CODE = "01 A 003";
  private static final String INN = "PARACETAMOL";
  private static final String BRAND_NAME = "DOLIPRANE";
  private static final String LABORATORY_HOLDER = "SANOFI";

  @Nested
  @DisplayName("getMedicineVersionByRegistrationNumber()")
  class GetMedicineVersionByRegistrationNumberTests {

    @Test
    @DisplayName("should return the version when the repository finds a matching medicine")
    void getMedicineVersionByRegistrationNumber_should_returnVersion_when_medicineFound() {
      // Arrange
      when(medicineRepository.findVersionByRegistrationNumber(
              REGISTRATION_NUMBER, CODE, INN, BRAND_NAME, LABORATORY_HOLDER))
          .thenReturn(Optional.of(5));

      // Act
      int version =
          medicineAdapter.getMedicineVersionByRegistrationNumber(
              REGISTRATION_NUMBER, CODE, INN, BRAND_NAME, LABORATORY_HOLDER);

      // Assert
      assertThat(version).isEqualTo(5);
      verify(medicineRepository)
          .findVersionByRegistrationNumber(
              REGISTRATION_NUMBER, CODE, INN, BRAND_NAME, LABORATORY_HOLDER);
    }

    @Test
    @DisplayName(
        "should return 0 when the repository returns an empty Optional (medicine not found)")
    void getMedicineVersionByRegistrationNumber_should_returnZero_when_medicineNotFound() {
      // Arrange
      when(medicineRepository.findVersionByRegistrationNumber(
              REGISTRATION_NUMBER, CODE, INN, BRAND_NAME, LABORATORY_HOLDER))
          .thenReturn(Optional.empty());

      // Act
      int version =
          medicineAdapter.getMedicineVersionByRegistrationNumber(
              REGISTRATION_NUMBER, CODE, INN, BRAND_NAME, LABORATORY_HOLDER);

      // Assert
      assertThat(version).isZero();
    }

    @Test
    @DisplayName("should return 0 when the repository returns an Optional containing 0")
    void getMedicineVersionByRegistrationNumber_should_returnZero_when_repositoryReturnsZero() {
      // Arrange
      when(medicineRepository.findVersionByRegistrationNumber(
              REGISTRATION_NUMBER, CODE, INN, BRAND_NAME, LABORATORY_HOLDER))
          .thenReturn(Optional.of(0));

      // Act
      int version =
          medicineAdapter.getMedicineVersionByRegistrationNumber(
              REGISTRATION_NUMBER, CODE, INN, BRAND_NAME, LABORATORY_HOLDER);

      // Assert
      assertThat(version).isZero();
    }

    @Test
    @DisplayName("should propagate a high version number from the repository")
    void
        getMedicineVersionByRegistrationNumber_should_returnHighVersion_when_repositoryReturnsIt() {
      // Arrange
      when(medicineRepository.findVersionByRegistrationNumber(
              REGISTRATION_NUMBER, CODE, INN, BRAND_NAME, LABORATORY_HOLDER))
          .thenReturn(Optional.of(Integer.MAX_VALUE));

      // Act
      int version =
          medicineAdapter.getMedicineVersionByRegistrationNumber(
              REGISTRATION_NUMBER, CODE, INN, BRAND_NAME, LABORATORY_HOLDER);

      // Assert
      assertThat(version).isEqualTo(Integer.MAX_VALUE);
    }

    @Test
    @DisplayName("should pass all arguments to the repository without alteration")
    void getMedicineVersionByRegistrationNumber_should_forwardAllArguments_toRepository() {
      // Arrange
      when(medicineRepository.findVersionByRegistrationNumber(
              REGISTRATION_NUMBER, CODE, INN, BRAND_NAME, LABORATORY_HOLDER))
          .thenReturn(Optional.of(1));

      // Act
      medicineAdapter.getMedicineVersionByRegistrationNumber(
          REGISTRATION_NUMBER, CODE, INN, BRAND_NAME, LABORATORY_HOLDER);

      // Assert
      verify(medicineRepository)
          .findVersionByRegistrationNumber(
              REGISTRATION_NUMBER, CODE, INN, BRAND_NAME, LABORATORY_HOLDER);
    }

    @Test
    @DisplayName("should handle null arguments by forwarding them to the repository")
    void getMedicineVersionByRegistrationNumber_should_forwardNullArguments_toRepository() {
      // Arrange
      when(medicineRepository.findVersionByRegistrationNumber(null, null, null, null, null))
          .thenReturn(Optional.empty());

      // Act
      int version =
          medicineAdapter.getMedicineVersionByRegistrationNumber(null, null, null, null, null);

      // Assert
      assertThat(version).isZero();
      verify(medicineRepository).findVersionByRegistrationNumber(null, null, null, null, null);
    }
  }
}
