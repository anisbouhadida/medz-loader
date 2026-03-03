package dz.anisbouhadida.medzloader.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import dz.anisbouhadida.medzloader.domain.model.enums.MedicineOrigin;
import dz.anisbouhadida.medzloader.domain.model.enums.MedicineType;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Medicine")
class MedicineTest {

  @Test
  @DisplayName("should store all fields correctly")
  void constructor_should_storeAllFields_when_fullyPopulated() {
    var registrationDate = LocalDateTime.of(2020, 6, 15, 0, 0, 0);

    var medicine =
        new Medicine(
            "035/01 A 003/17/23",
            "01 A 003",
            "PARACETAMOL",
            "DOLIPRANE",
            "COMPRIMÉ",
            "500MG",
            "B/16",
            "LISTE I",
            "120.00",
            "150.00",
            "SANOFI",
            "FRANCE",
            registrationDate,
            MedicineType.GE,
            MedicineOrigin.MANUFACTURED,
            1);

    assertAll(
        () -> assertThat(medicine.registrationNumber()).isEqualTo("035/01 A 003/17/23"),
        () -> assertThat(medicine.code()).isEqualTo("01 A 003"),
        () -> assertThat(medicine.internationalCommonDenomination()).isEqualTo("PARACETAMOL"),
        () -> assertThat(medicine.brandName()).isEqualTo("DOLIPRANE"),
        () -> assertThat(medicine.form()).isEqualTo("COMPRIMÉ"),
        () -> assertThat(medicine.dosage()).isEqualTo("500MG"),
        () -> assertThat(medicine.packaging()).isEqualTo("B/16"),
        () -> assertThat(medicine.list()).isEqualTo("LISTE I"),
        () -> assertThat(medicine.p1()).isEqualTo("120.00"),
        () -> assertThat(medicine.p2()).isEqualTo("150.00"),
        () -> assertThat(medicine.laboratoryHolder()).isEqualTo("SANOFI"),
        () -> assertThat(medicine.laboratoryCountry()).isEqualTo("FRANCE"),
        () -> assertThat(medicine.initialRegistrationDate()).isEqualTo(registrationDate),
        () -> assertThat(medicine.type()).isEqualTo(MedicineType.GE),
        () -> assertThat(medicine.origin()).isEqualTo(MedicineOrigin.MANUFACTURED),
        () -> assertThat(medicine.version()).isEqualTo(1));
  }

  @Test
  @DisplayName("should accept null for optional fields")
  void constructor_should_acceptNulls_when_optionalFieldsAreNull() {
    var medicine =
        new Medicine(
            "035/01 A 003/17/23",
            "01 A 003",
            "PARACETAMOL",
            "DOLIPRANE",
            null,
            null,
            null,
            null,
            null,
            null,
            "SANOFI",
            "FRANCE",
            null,
            null,
            null,
            0);

    assertAll(
        () -> assertThat(medicine.form()).isNull(),
        () -> assertThat(medicine.dosage()).isNull(),
        () -> assertThat(medicine.packaging()).isNull(),
        () -> assertThat(medicine.list()).isNull(),
        () -> assertThat(medicine.p1()).isNull(),
        () -> assertThat(medicine.p2()).isNull(),
        () -> assertThat(medicine.initialRegistrationDate()).isNull(),
        () -> assertThat(medicine.type()).isNull(),
        () -> assertThat(medicine.origin()).isNull());
  }

  @Test
  @DisplayName("should implement equals based on all fields")
  void equals_should_returnTrue_when_allFieldsMatch() {
    var date = LocalDateTime.of(2020, 1, 1, 0, 0, 0);
    var med1 =
        new Medicine(
            "REG",
            "CODE",
            "INN",
            "BRAND",
            "FORM",
            "DOSE",
            "PACK",
            "LIST",
            "P1",
            "P2",
            "LAB",
            "COUNTRY",
            date,
            MedicineType.RE,
            MedicineOrigin.IMPORTED,
            0);
    var med2 =
        new Medicine(
            "REG",
            "CODE",
            "INN",
            "BRAND",
            "FORM",
            "DOSE",
            "PACK",
            "LIST",
            "P1",
            "P2",
            "LAB",
            "COUNTRY",
            date,
            MedicineType.RE,
            MedicineOrigin.IMPORTED,
            0);

    assertThat(med1).isEqualTo(med2);
  }

  @Test
  @DisplayName("should implement hashCode consistently with equals")
  void hashCode_should_beConsistent_when_equalObjects() {
    var date = LocalDateTime.of(2020, 1, 1, 0, 0, 0);
    var med1 =
        new Medicine(
            "REG",
            "CODE",
            "INN",
            "BRAND",
            "FORM",
            "DOSE",
            "PACK",
            "LIST",
            "P1",
            "P2",
            "LAB",
            "COUNTRY",
            date,
            MedicineType.BIO,
            MedicineOrigin.MANUFACTURED,
            2);
    var med2 =
        new Medicine(
            "REG",
            "CODE",
            "INN",
            "BRAND",
            "FORM",
            "DOSE",
            "PACK",
            "LIST",
            "P1",
            "P2",
            "LAB",
            "COUNTRY",
            date,
            MedicineType.BIO,
            MedicineOrigin.MANUFACTURED,
            2);

    assertThat(med1).hasSameHashCodeAs(med2);
  }
}
