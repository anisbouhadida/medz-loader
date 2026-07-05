package dz.anisbouhadida.medzloader.batch.support.classifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayName("MedicineCsvFileType")
class MedicineCsvFileTypeTest {

  @ParameterizedTest
  @CsvSource({
    "nomenclature.csv, NOMENCLATURE",
    "retraits_2026.csv, WITHDRAWAL",
    "non_renouveles.csv, NON_RENEWAL"
  })
  void fromFilename_shouldClassifyRecognizedFiles(
      String filename, MedicineCsvFileType expected) {
    assertThat(MedicineCsvFileType.fromFilename(filename)).isEqualTo(expected);
  }

  @Test
  void fromFilename_shouldRejectUnknownFiles() {
    assertThatThrownBy(() -> MedicineCsvFileType.fromFilename("other.csv"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Unknown medicine CSV file type: other.csv");
  }
}
