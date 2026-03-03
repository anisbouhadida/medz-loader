package dz.anisbouhadida.medzloader.domain.model;

import dz.anisbouhadida.medzloader.domain.model.enums.MedicineEventType;
import dz.anisbouhadida.medzloader.domain.model.enums.MedicineStatus;
import java.time.LocalDateTime;

/// Represents a **non-renewal** event for a [Medicine].
///
/// Created when a medicine registration is not renewed upon expiry.
/// Always carries a [MedicineStatus#MARKED_NOT_RENEWED] status and an event type of
/// [MedicineEventType#NON_RENEWAL].
///
/// @param medicine               the medicine affected by this event
/// @param finalRegistrationDate  date on which the registration expired — may be `null`
/// @param observations           free-text observations from the source file — may be `null`
///
/// @author Anis Bouhadida
/// @since 0.0.1
public record NonRenewalEvent(
    Medicine medicine, LocalDateTime finalRegistrationDate, String observations)
    implements MedicineEvent {

  /// {@inheritDoc}
  ///
  /// Always returns [MedicineStatus#MARKED_NOT_RENEWED].
  @Override
  public MedicineStatus status() {
    return MedicineStatus.MARKED_NOT_RENEWED;
  }

  /// {@inheritDoc}
  ///
  /// Always returns [MedicineEventType#NON_RENEWAL].
  @Override
  public MedicineEventType eventType() {
    return MedicineEventType.NON_RENEWAL;
  }
}
