package dz.anisbouhadida.medzloader.domain.service;

import dz.anisbouhadida.medzloader.domain.api.MedicineApi;
import dz.anisbouhadida.medzloader.domain.spi.MedicinePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/// Domain service implementing [MedicineApi].
///
/// Delegates every operation to the [MedicinePort] SPI so the domain layer
/// remains independent of any specific persistence technology.
///
/// @author Anis Bouhadida
/// @since 0.2.0
@Service
@RequiredArgsConstructor
public class MedicineService implements MedicineApi {

  private final MedicinePort medicinePort;

  @Override
  public int getMedicineVersionByRegistrationNumber(
      String registrationNumber,
      String code,
      String internationalCommonDenomination,
      String brandName,
      String laboratoryHolder) {
    return this.medicinePort.getMedicineVersionByRegistrationNumber(
        registrationNumber, code, internationalCommonDenomination, brandName, laboratoryHolder);
  }
}
