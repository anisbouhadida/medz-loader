package dz.anisbouhadida.medzloader.domain.model.enums;

/// Classifies a [dz.anisbouhadida.medzloader.domain.model.Medicine] by its regulatory type.
///
/// Sourced from the `type` column of the CSV files.
/// Raw values `GE` / `G` map to [#GE], `RE` / `R` map to [#RE], and `BIO` maps to [#BIO].
///
/// @author Anis Bouhadida
/// @since 0.0.1
public enum MedicineType {
    /// `GE` or `G` in the source file.
    GE,
    /// `RE` or `R` in the source file.
    RE,
    /// `BIO` in the source file.
    BIO
}
