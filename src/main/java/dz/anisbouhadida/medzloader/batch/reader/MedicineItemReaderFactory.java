package dz.anisbouhadida.medzloader.batch.reader;

import dz.anisbouhadida.medzloader.batch.dto.NomenclatureLine;
import dz.anisbouhadida.medzloader.batch.dto.NonRenewalLine;
import dz.anisbouhadida.medzloader.batch.dto.WithdrawalLine;
import dz.anisbouhadida.medzloader.batch.support.constant.MedicineCsvColumns;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.infrastructure.item.file.mapping.RecordFieldSetMapper;

/// Factory responsible for creating [FlatFileItemReader] instances
/// for each type of medicine CSV file.
///
/// Each factory method builds a reader pre-configured with the correct
/// field names and line-skip settings for its target file format.
///
/// Column names and ordered field arrays are defined in [MedicineCsvColumns].
///
/// @see NomenclatureLine
/// @see WithdrawalLine
/// @see NonRenewalLine
/// @see MedicineCsvColumns
///
/// @author Anis Bouhadida
/// @since 0.0.1
/// @version 0.1.0
public class MedicineItemReaderFactory {

  /// Creates a [FlatFileItemReader] configured to read **nomenclature** CSV files.
  ///
  /// The reader skips the header line and maps each row to a [NomenclatureLine] record.
  ///
  /// @return a pre-configured reader for nomenclature data
  public FlatFileItemReader<NomenclatureLine> createNomenclatureReader() {
    return new FlatFileItemReaderBuilder<NomenclatureLine>()
        .name("nomenclatureItemReader")
        .linesToSkip(1)
        .delimited()
        .names(MedicineCsvColumns.nomenclatureFields())
        .fieldSetMapper(new RecordFieldSetMapper<>(NomenclatureLine.class))
        .build();
  }

  /// Creates a [FlatFileItemReader] configured to read **withdrawal** CSV files.
  ///
  /// The reader skips the header line and maps each row to a [WithdrawalLine] record.
  ///
  /// @return a pre-configured reader for withdrawal data
  public FlatFileItemReader<WithdrawalLine> createWithdrawalReader() {
    return new FlatFileItemReaderBuilder<WithdrawalLine>()
        .name("withdrawalItemReader")
        .linesToSkip(1)
        .delimited()
        .names(MedicineCsvColumns.withdrawalFields())
        .fieldSetMapper(new RecordFieldSetMapper<>(WithdrawalLine.class))
        .build();
  }

  /// Creates a [FlatFileItemReader] configured to read **non-renewal** CSV files.
  ///
  /// The reader skips the header line and maps each row to a [NonRenewalLine] record.
  ///
  /// @return a pre-configured reader for non-renewal data
  public FlatFileItemReader<NonRenewalLine> createNonRenewalReader() {
    return new FlatFileItemReaderBuilder<NonRenewalLine>()
        .name("nonRenewalItemReader")
        .linesToSkip(1)
        .delimited()
        .names(MedicineCsvColumns.nonRenewalFields())
        .fieldSetMapper(new RecordFieldSetMapper<>(NonRenewalLine.class))
        .build();
  }
}
