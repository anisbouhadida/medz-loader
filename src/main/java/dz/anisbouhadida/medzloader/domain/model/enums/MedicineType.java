package dz.anisbouhadida.medzloader.domain.model.enums;

/// Classifies a [dz.anisbouhadida.medzloader.domain.model.Medicine] by its regulatory type.
///
/// Sourced from the `type` column of the CSV files.
/// Raw values `GE` / `G` / `Gé` map to [#GE], `RE` / `R` / `Ré` map to [#RE], and `BIO` maps to
/// [#BIO].
///
/// @author Anis Bouhadida
/// @since 0.0.1
public enum MedicineType {
  /// `GE`, `G`, or `Gé` in the source file.
  GE,
  /// `RE`, `R`, or `Ré` in the source file.
  RE,
  /// `BIO` in the source file.
  BIO
}
