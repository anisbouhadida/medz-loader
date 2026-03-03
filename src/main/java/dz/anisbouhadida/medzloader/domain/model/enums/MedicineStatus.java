package dz.anisbouhadida.medzloader.domain.model.enums;

/// Represents the regulatory lifecycle status of a
/// [dz.anisbouhadida.medzloader.domain.model.Medicine].
///
/// The status is derived from the type of [dz.anisbouhadida.medzloader.domain.model.MedicineEvent]
/// being processed.
///
/// @author Anis Bouhadida
/// @since 0.0.1
public enum MedicineStatus {
  /// The medicine registration is currently active (sourced from the nomenclature file).
  ACTIVE,
  /// The medicine has been officially withdrawn from the market.
  WITHDRAWN,
  /// The medicine registration was not renewed upon expiry.
  MARKED_NOT_RENEWED
}
