package dz.anisbouhadida.medzloader.infrastructure.jdbc.adapter;

import dz.anisbouhadida.medzloader.domain.spi.MedicinePort;
import dz.anisbouhadida.medzloader.infrastructure.jdbc.repository.MedicineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/// JDBC adapter implementing [MedicinePort].
///
/// Bridges the domain SPI to the [MedicineRepository] infrastructure
/// repository, translating the `Optional`-based repository result into the
/// primitive `int` expected by the port contract (`0` when absent).
///
/// @author Anis Bouhadida
/// @since 0.2.0
@Component
@RequiredArgsConstructor
public class MedicineAdapter implements MedicinePort {

  private final MedicineRepository medicineRepository;

  @Override
  public int getMedicineVersionByRegistrationNumber(
      String registrationNumber,
      String code,
      String internationalCommonDenomination,
      String brandName,
      String laboratoryHolder) {
    return this.medicineRepository
        .findVersionByRegistrationNumber(
            registrationNumber, code, internationalCommonDenomination, brandName, laboratoryHolder)
        .orElse(0);
  }
}
