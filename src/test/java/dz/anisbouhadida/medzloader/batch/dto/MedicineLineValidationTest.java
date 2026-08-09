package dz.anisbouhadida.medzloader.batch.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("MedicineLine validation")
class MedicineLineValidationTest {

  @ParameterizedTest(name = "should accept localized type {0}")
  @ValueSource(strings = {"Gé", "Ré"})
  void shouldAcceptLocalizedMedicineTypes(String type) {
    assertThat(validate(withdrawalLine(type, "I", "REG", "FORM"))).isEmpty();
  }

  @Test
  @DisplayName("should accept a blank origin")
  void shouldAcceptBlankOrigin() {
    assertThat(validate(withdrawalLine("GE", "", "REG", "FORM"))).isEmpty();
  }

  @ParameterizedTest(name = "should reject unknown type {0}")
  @ValueSource(strings = {"B", "FPREMIX", "1"})
  void shouldRejectUnknownMedicineTypes(String type) {
    assertThat(validate(withdrawalLine(type, "I", "REG", "FORM")))
        .extracting(violation -> violation.getPropertyPath().toString())
        .containsExactly("type");
  }

  @Test
  @DisplayName("should reject whitespace-only mandatory identity fields")
  void shouldRejectWhitespaceOnlyRegistrationNumber() {
    assertThat(validate(withdrawalLine("GE", "I", "   ", "FORM")))
        .extracting(violation -> violation.getPropertyPath().toString())
        .containsExactly("registrationNumber");
  }

  @Test
  @DisplayName("should retain mandatory form validation for non-renewals")
  void shouldRejectBlankNonRenewalForm() {
    var line =
        new NonRenewalLine(
            1,
            "REG",
            "CODE",
            "INN",
            "BRAND",
            "   ",
            "DOSAGE",
            "PACKAGING",
            "LIST",
            "P1",
            "P2",
            "OBS",
            "LAB",
            "DZ",
            "",
            "",
            "GE",
            "F");

    assertThat(validate(line))
        .extracting(violation -> violation.getPropertyPath().toString())
        .containsExactly("form");
  }

  private static jakarta.validation.ConstraintViolation<?>[] validate(MedicineLine line) {
    try (var factory = Validation.buildDefaultValidatorFactory()) {
      return factory
          .getValidator()
          .validate(line)
          .toArray(jakarta.validation.ConstraintViolation[]::new);
    }
  }

  private static WithdrawalLine withdrawalLine(
      String type, String status, String registrationNumber, String form) {
    return new WithdrawalLine(
        1,
        registrationNumber,
        "CODE",
        "INN",
        "BRAND",
        form,
        "DOSAGE",
        "PACKAGING",
        "LIST",
        "P1",
        "P2",
        "LAB",
        "DZ",
        "",
        type,
        status,
        "",
        "");
  }
}
