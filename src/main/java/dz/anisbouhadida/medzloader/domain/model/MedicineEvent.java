package dz.anisbouhadida.medzloader.domain.model;

import dz.anisbouhadida.medzloader.domain.model.enums.MedicineEventType;
import dz.anisbouhadida.medzloader.domain.model.enums.MedicineStatus;

/// Sealed domain interface that represents an event affecting a [Medicine].
///
/// Each permitted implementation captures the data specific to one regulatory
/// lifecycle event: a nomenclature upsert, a withdrawal, or a non-renewal.
///
/// Permitted implementations:
/// - [NomenclatureEvent] — medicine added to or updated in the nomenclature
/// - [WithdrawalEvent]   — medicine officially withdrawn from the market
/// - [NonRenewalEvent]   — medicine registration not renewed
///
/// @author Anis Bouhadida
/// @since 0.0.1
public sealed interface MedicineEvent permits NomenclatureEvent, WithdrawalEvent, NonRenewalEvent {

  /// Returns the [Medicine] this event is associated with.
  Medicine medicine();

  /// Returns the [MedicineStatus] resulting from this event.
  MedicineStatus status();

  /// Returns the [MedicineEventType] that classifies this event.
  MedicineEventType eventType();
}
