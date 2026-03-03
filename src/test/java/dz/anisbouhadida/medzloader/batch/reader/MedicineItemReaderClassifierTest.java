package dz.anisbouhadida.medzloader.batch.reader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import dz.anisbouhadida.medzloader.batch.dto.NomenclatureLine;
import dz.anisbouhadida.medzloader.batch.dto.NonRenewalLine;
import dz.anisbouhadida.medzloader.batch.dto.WithdrawalLine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;

@DisplayName("MedicineItemReaderClassifier")
@ExtendWith(MockitoExtension.class)
class MedicineItemReaderClassifierTest {

  @Mock private MedicineItemReaderFactory readerFactory;

  private MedicineItemReaderClassifier classifier;

  @BeforeEach
  void setUp() {
    classifier = new MedicineItemReaderClassifier(readerFactory);
  }

  @Nested
  @DisplayName("classify()")
  class ClassifyTests {

    @Mock private FlatFileItemReader<NomenclatureLine> nomenclatureReader;

    @Mock private FlatFileItemReader<WithdrawalLine> withdrawalReader;

    @Mock private FlatFileItemReader<NonRenewalLine> nonRenewalReader;

    @ParameterizedTest(name = "should return nomenclature reader for filename \"{0}\"")
    @ValueSource(
        strings = {"nomenclature.csv", "2024-08/nomenclature.csv", "input/nomenclature_v2.csv"})
    void classify_should_returnNomenclatureReader_when_filenameContainsNomenclature(
        String resourceName) {
      when(readerFactory.createNomenclatureReader()).thenReturn(nomenclatureReader);

      var result = classifier.classify(resourceName);

      assertThat(result).isSameAs(nomenclatureReader);
    }

    @ParameterizedTest(name = "should return withdrawal reader for filename \"{0}\"")
    @ValueSource(strings = {"retraits.csv", "2024-08/retraits.csv", "input/retraits_v2.csv"})
    void classify_should_returnWithdrawalReader_when_filenameContainsRetraits(String resourceName) {
      when(readerFactory.createWithdrawalReader()).thenReturn(withdrawalReader);

      var result = classifier.classify(resourceName);

      assertThat(result).isSameAs(withdrawalReader);
    }

    @ParameterizedTest(name = "should return non-renewal reader for filename \"{0}\"")
    @ValueSource(
        strings = {
          "non_renouveles.csv",
          "2024-08/non_renouveles.csv",
          "input/non_renouveles_v2.csv"
        })
    void classify_should_returnNonRenewalReader_when_filenameContainsNonRenouveles(
        String resourceName) {
      when(readerFactory.createNonRenewalReader()).thenReturn(nonRenewalReader);

      var result = classifier.classify(resourceName);

      assertThat(result).isSameAs(nonRenewalReader);
    }

    @Test
    @DisplayName("should throw IllegalArgumentException for unknown filename")
    void classify_should_throwIllegalArgumentException_when_filenameIsUnknown() {
      assertThatThrownBy(() -> classifier.classify("unknown_file.csv"))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Unknown file type for resource: unknown_file.csv");
    }

    @Test
    @DisplayName("should throw IllegalArgumentException for empty filename")
    void classify_should_throwIllegalArgumentException_when_filenameIsEmpty() {
      assertThatThrownBy(() -> classifier.classify(""))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Unknown file type for resource: ");
    }
  }
}
