package dz.anisbouhadida.medzloader.domain.model.enums;

/// Classifies the type of [dz.anisbouhadida.medzloader.domain.model.MedicineEvent].
///
/// This enum is used to distinguish between the different regulatory events that can affect a
/// medicine,
/// such as being added to the nomenclature, withdrawn from the market, or not renewed upon expiry.
///
/// @author Anis Bouhadida
/// @since 0.0.1
public enum MedicineEventType {
  /// A medicine was inserted into or updated in the nomenclature (active registration).
  UPSERT,
  /// A medicine was officially withdrawn from the market.
  WITHDRAWAL,
  /// A medicine registration was not renewed upon expiry.
  NON_RENEWAL
}
