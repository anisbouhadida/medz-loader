package dz.anisbouhadida.medzloader.batch.support.mapper;

import dz.anisbouhadida.medzloader.domain.model.enums.MedicineOrigin;
import dz.anisbouhadida.medzloader.domain.model.enums.MedicineType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/// Note: In these tests, we intentionally do not use the MapStruct-generated implementation.
/// Instead, we create an anonymous subclass of the interface to exercise the default methods
// directly.
/// The abstract mapping methods are irrelevant for these tests and can safely return null, allowing
// us to test the default methods in isolation.
@DisplayName("MedicineLineMapper — default methods")
class MedicineLineMapperDefaultMethodsTest {

  private MedicineLineMapper mapper;

  @BeforeEach
  void setUp() {

    mapper =
        new MedicineLineMapper() {
          @Override
          public dz.anisbouhadida.medzloader.domain.model.Medicine toMedicine(
              dz.anisbouhadida.medzloader.batch.dto.MedicineLine line,
              dz.anisbouhadida.medzloader.domain.api.MedicineApi medicineApi) {
            return null;
          }

          @Override
          public dz.anisbouhadida.medzloader.domain.model.NomenclatureEvent toMedicineEvent(
              dz.anisbouhadida.medzloader.batch.dto.NomenclatureLine line,
              dz.anisbouhadida.medzloader.domain.api.MedicineApi medicineApi) {
            return null;
          }

          @Override
          public dz.anisbouhadida.medzloader.domain.model.WithdrawalEvent toMedicineEvent(
              dz.anisbouhadida.medzloader.batch.dto.WithdrawalLine line,
              dz.anisbouhadida.medzloader.domain.api.MedicineApi medicineApi) {
            return null;
          }

          @Override
          public dz.anisbouhadida.medzloader.domain.model.NonRenewalEvent toMedicineEvent(
              dz.anisbouhadida.medzloader.batch.dto.NonRenewalLine line,
              dz.anisbouhadida.medzloader.domain.api.MedicineApi medicineApi) {
            return null;
          }
        };
  }

  @Nested
  @DisplayName("parseDate()")
  @SuppressWarnings("removal")
  class ParseDateTests {

    @Test
    @DisplayName("should parse a valid yyyy-MM-dd HH:mm:ss string")
    void parseDate_should_returnLocalDateTime_when_validFormat() {
      var result = mapper.parseDate("2024-03-15 10:30:00");

      assertThat(result).isEqualTo(LocalDateTime.of(2024, 3, 15, 10, 30, 0));
    }

    @Test
    @DisplayName("should return null for null input")
    void parseDate_should_returnNull_when_inputIsNull() {
      assertThat(mapper.parseDate(null)).isNull();
    }

    @Test
    @DisplayName("should return null for blank input")
    void parseDate_should_returnNull_when_inputIsBlank() {
      assertThat(mapper.parseDate("   ")).isNull();
    }

    @Test
    @DisplayName("should return null for empty input")
    void parseDate_should_returnNull_when_inputIsEmpty() {
      assertThat(mapper.parseDate("")).isNull();
    }

    @Test
    @DisplayName("should return null for malformed date")
    void parseDate_should_returnNull_when_formatIsMalformed() {
      assertThat(mapper.parseDate("15/03/2024")).isNull();
    }

    @Test
    @DisplayName("should return null for incomplete date")
    void parseDate_should_returnNull_when_dateIsIncomplete() {
      assertThat(mapper.parseDate("2024-03-15")).isNull();
    }

    @Test
    @DisplayName("should parse midnight correctly")
    void parseDate_should_parseCorrectly_when_timeIsMidnight() {
      var result = mapper.parseDate("2024-01-01 00:00:00");

      assertThat(result).isEqualTo(LocalDateTime.of(2024, 1, 1, 0, 0, 0));
    }
  }

  @Nested
  @DisplayName("toMedicineType()")
  class ToMedicineTypeTests {

    @ParameterizedTest(name = "should return GE for input \"{0}\"")
    @ValueSource(strings = {"GE", "G"})
    void toMedicineType_should_returnGE_when_inputIsGEorG(String input) {
      assertThat(mapper.toMedicineType(input)).isEqualTo(MedicineType.GE);
    }

    @ParameterizedTest(name = "should return RE for input \"{0}\"")
    @ValueSource(strings = {"RE", "R"})
    void toMedicineType_should_returnRE_when_inputIsREorR(String input) {
      assertThat(mapper.toMedicineType(input)).isEqualTo(MedicineType.RE);
    }

    @Test
    @DisplayName("should return BIO for input \"BIO\"")
    void toMedicineType_should_returnBIO_when_inputIsBIO() {
      assertThat(mapper.toMedicineType("BIO")).isEqualTo(MedicineType.BIO);
    }

    @ParameterizedTest(name = "should return null for unrecognised input \"{0}\"")
    @ValueSource(strings = {"", "UNKNOWN", "ge", "re", "bio", "X"})
    void toMedicineType_should_returnNull_when_inputIsUnrecognised(String input) {
      assertThat(mapper.toMedicineType(input)).isNull();
    }

    @Test
    @DisplayName("should return null for null input")
    void toMedicineType_should_returnNull_when_inputIsNull() {
      assertThat(mapper.toMedicineType(null)).isNull();
    }
  }

  @Nested
  @DisplayName("toMedicineOrigin()")
  class ToMedicineOriginTests {

    @Test
    @DisplayName("should return MANUFACTURED for input \"F\"")
    void toMedicineOrigin_should_returnManufactured_when_inputIsF() {
      assertThat(mapper.toMedicineOrigin("F")).isEqualTo(MedicineOrigin.MANUFACTURED);
    }

    @ParameterizedTest(name = "should return IMPORTED for input \"{0}\"")
    @ValueSource(strings = {"I", "i"})
    void toMedicineOrigin_should_returnImported_when_inputIsI(String input) {
      assertThat(mapper.toMedicineOrigin(input)).isEqualTo(MedicineOrigin.IMPORTED);
    }

    @ParameterizedTest(name = "should return null for unrecognised input \"{0}\"")
    @ValueSource(strings = {"", "f", "X", "UNKNOWN"})
    void toMedicineOrigin_should_returnNull_when_inputIsUnrecognised(String input) {
      assertThat(mapper.toMedicineOrigin(input)).isNull();
    }

    @Test
    @DisplayName("should return null for null input")
    void toMedicineOrigin_should_returnNull_when_inputIsNull() {
      assertThat(mapper.toMedicineOrigin(null)).isNull();
    }
  }
}
