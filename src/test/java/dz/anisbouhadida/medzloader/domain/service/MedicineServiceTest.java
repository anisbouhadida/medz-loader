package dz.anisbouhadida.medzloader.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dz.anisbouhadida.medzloader.domain.spi.MedicinePort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/// Unit tests for [MedicineService].
///
/// Verifies that the service correctly delegates all calls to the [MedicinePort] SPI
/// and propagates its return values unchanged.
@DisplayName("MedicineService")
@ExtendWith(MockitoExtension.class)
class MedicineServiceTest {

  @Mock private MedicinePort medicinePort;

  @InjectMocks private MedicineService medicineService;

  private static final String REGISTRATION_NUMBER = "035/01 A 003/17/23";
  private static final String CODE = "01 A 003";
  private static final String INN = "PARACETAMOL";
  private static final String BRAND_NAME = "DOLIPRANE";
  private static final String LABORATORY_HOLDER = "SANOFI";

  @Nested
  @DisplayName("getMedicineVersionByRegistrationNumber()")
  class GetMedicineVersionByRegistrationNumberTests {

    @Test
    @DisplayName("should delegate to MedicinePort and return the version")
    void getMedicineVersionByRegistrationNumber_should_returnVersion_when_medicineExists() {
      // Arrange
      when(medicinePort.getMedicineVersionByRegistrationNumber(
              REGISTRATION_NUMBER, CODE, INN, BRAND_NAME, LABORATORY_HOLDER))
          .thenReturn(3);

      // Act
      int version =
          medicineService.getMedicineVersionByRegistrationNumber(
              REGISTRATION_NUMBER, CODE, INN, BRAND_NAME, LABORATORY_HOLDER);

      // Assert
      assertThat(version).isEqualTo(3);
      verify(medicinePort)
          .getMedicineVersionByRegistrationNumber(
              REGISTRATION_NUMBER, CODE, INN, BRAND_NAME, LABORATORY_HOLDER);
    }

    @Test
    @DisplayName("should return 0 when the port returns 0 (medicine not found)")
    void getMedicineVersionByRegistrationNumber_should_returnZero_when_medicineNotFound() {
      // Arrange
      when(medicinePort.getMedicineVersionByRegistrationNumber(
              REGISTRATION_NUMBER, CODE, INN, BRAND_NAME, LABORATORY_HOLDER))
          .thenReturn(0);

      // Act
      int version =
          medicineService.getMedicineVersionByRegistrationNumber(
              REGISTRATION_NUMBER, CODE, INN, BRAND_NAME, LABORATORY_HOLDER);

      // Assert
      assertThat(version).isZero();
    }

    @Test
    @DisplayName("should propagate a high version number unchanged")
    void getMedicineVersionByRegistrationNumber_should_propagateHighVersion_when_portReturnsIt() {
      // Arrange
      when(medicinePort.getMedicineVersionByRegistrationNumber(
              REGISTRATION_NUMBER, CODE, INN, BRAND_NAME, LABORATORY_HOLDER))
          .thenReturn(Integer.MAX_VALUE);

      // Act
      int version =
          medicineService.getMedicineVersionByRegistrationNumber(
              REGISTRATION_NUMBER, CODE, INN, BRAND_NAME, LABORATORY_HOLDER);

      // Assert
      assertThat(version).isEqualTo(Integer.MAX_VALUE);
    }

    @Test
    @DisplayName("should pass all arguments to the port without alteration")
    void getMedicineVersionByRegistrationNumber_should_forwardAllArguments_toPort() {
      // Arrange
      when(medicinePort.getMedicineVersionByRegistrationNumber(
              REGISTRATION_NUMBER, CODE, INN, BRAND_NAME, LABORATORY_HOLDER))
          .thenReturn(1);

      // Act
      medicineService.getMedicineVersionByRegistrationNumber(
          REGISTRATION_NUMBER, CODE, INN, BRAND_NAME, LABORATORY_HOLDER);

      // Assert
      verify(medicinePort)
          .getMedicineVersionByRegistrationNumber(
              REGISTRATION_NUMBER, CODE, INN, BRAND_NAME, LABORATORY_HOLDER);
    }

    @Test
    @DisplayName("should handle null arguments by forwarding them to the port")
    void getMedicineVersionByRegistrationNumber_should_forwardNullArguments_toPort() {
      // Arrange
      when(medicinePort.getMedicineVersionByRegistrationNumber(null, null, null, null, null))
          .thenReturn(0);

      // Act
      int version =
          medicineService.getMedicineVersionByRegistrationNumber(null, null, null, null, null);

      // Assert
      assertThat(version).isZero();
      verify(medicinePort).getMedicineVersionByRegistrationNumber(null, null, null, null, null);
    }
  }
}
