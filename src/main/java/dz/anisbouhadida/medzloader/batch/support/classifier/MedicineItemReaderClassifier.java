package dz.anisbouhadida.medzloader.batch.support.classifier;

import dz.anisbouhadida.medzloader.batch.dto.MedicineLine;
import dz.anisbouhadida.medzloader.batch.support.factory.MedicineItemReaderFactory;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;

/// Classifies a CSV resource by its filename and returns the appropriate
/// [FlatFileItemReader] configured for that file type.
///
/// Supported file types:
/// - `nomenclature` → [MedicineItemReaderFactory#createNomenclatureReader()]
/// - `retraits` → [MedicineItemReaderFactory#createWithdrawalReader()]
/// - `non_renouveles` → [MedicineItemReaderFactory#createNonRenewalReader()]
@RequiredArgsConstructor
public class MedicineItemReaderClassifier {

  /// Factory used to create file-type-specific item readers.
  private final MedicineItemReaderFactory readerFactory;

  /// Returns a [FlatFileItemReader] matching the given resource name.
  ///
  /// The resource name is checked for known keywords (`nomenclature`,
  /// `retraits`, `non_renouveles`) to determine the correct reader.
  ///
  /// @param currentResource the resource path or filename to classify
  /// @return the reader configured for the detected file type
  /// @throws IllegalArgumentException if the resource does not match any known type
  public FlatFileItemReader<? extends MedicineLine> classify(@NonNull String currentResource) {
    return switch (MedicineCsvFileType.fromFilename(currentResource)) {
      case NOMENCLATURE -> readerFactory.createNomenclatureReader();
      case WITHDRAWAL -> readerFactory.createWithdrawalReader();
      case NON_RENEWAL -> readerFactory.createNonRenewalReader();
    };
  }
}
