package dz.anisbouhadida.medzloader.batch.support.audit;

import dz.anisbouhadida.medzloader.batch.dto.MedicineLine;

/// Describes one field-level data-quality issue found while importing a medicine CSV item.
///
/// @param release release directory supplied to the batch job
/// @param jobExecutionId Spring Batch job execution identifier
/// @param sourceFile logical CSV filename derived from the line type
/// @param sourceItemId sequential identifier from the source file
/// @param registrationNumber official medicine registration number
/// @param code medicine code
/// @param brandName commercial brand name
/// @param laboratoryHolder registration-holder laboratory
/// @param disposition whether the item was filtered or loaded with a warning
/// @param fieldName field responsible for the issue
/// @param rejectedValue raw source value that triggered the issue
/// @param message human-readable issue description
public record MedicineImportIssue(
    String release,
    Long jobExecutionId,
    String sourceFile,
    Integer sourceItemId,
    String registrationNumber,
    String code,
    String brandName,
    String laboratoryHolder,
    MedicineImportIssueDisposition disposition,
    String fieldName,
    String rejectedValue,
    String message) {

  /// Creates an issue from a source line and its validation details.
  ///
  /// @param release release directory supplied to the batch job
  /// @param jobExecutionId Spring Batch job execution identifier
  /// @param line source medicine line
  /// @param disposition effect of the issue on the source item
  /// @param fieldName invalid or degraded field
  /// @param rejectedValue raw source value
  /// @param message validation or data-quality message
  /// @return a populated import issue
  public static MedicineImportIssue from(
      String release,
      Long jobExecutionId,
      MedicineLine line,
      MedicineImportIssueDisposition disposition,
      String fieldName,
      Object rejectedValue,
      String message) {
    return new MedicineImportIssue(
        release,
        jobExecutionId,
        sourceFile(line),
        line.id(),
        line.registrationNumber(),
        line.code(),
        line.brandName(),
        line.registrationHolderLaboratory(),
        disposition,
        fieldName,
        rejectedValue == null ? null : rejectedValue.toString(),
        message);
  }

  private static String sourceFile(MedicineLine line) {
    return switch (line) {
      case dz.anisbouhadida.medzloader.batch.dto.NomenclatureLine _ -> "nomenclature.csv";
      case dz.anisbouhadida.medzloader.batch.dto.NonRenewalLine _ -> "non_renouveles.csv";
      case dz.anisbouhadida.medzloader.batch.dto.WithdrawalLine _ -> "retraits.csv";
    };
  }
}
