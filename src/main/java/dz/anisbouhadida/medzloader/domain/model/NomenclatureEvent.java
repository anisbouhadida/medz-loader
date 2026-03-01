package dz.anisbouhadida.medzloader.domain.model;

import dz.anisbouhadida.medzloader.domain.model.enums.MedicineEventType;
import dz.anisbouhadida.medzloader.domain.model.enums.MedicineStatus;

import java.time.LocalDateTime;

/// Represents a **nomenclature upsert** event for a [Medicine].
///
/// Created when a medicine is added to or updated in the official nomenclature.
/// Always carries a [MedicineStatus#ACTIVE] status and an event type of
/// [MedicineEventType#UPSERT].
///
/// @param medicine                the medicine affected by this event
/// @param finalRegistrationDate   date until which the registration is valid — may be `null`
/// @param stabilityDuration       declared stability duration (e.g. `24 MOIS`) — may be `null`
/// @param observations            free-text observations from the source file — may be `null`
///
/// @author Anis Bouhadida
/// @since 0.0.1
public record NomenclatureEvent(Medicine medicine, LocalDateTime finalRegistrationDate, String stabilityDuration, String observations) implements MedicineEvent {

        /// {@inheritDoc}
        ///
        /// Always returns [MedicineStatus#ACTIVE].
        @Override
        public MedicineStatus status() {
            return MedicineStatus.ACTIVE;
        }

    /// {@inheritDoc}
    ///
    /// Always returns [MedicineEventType#UPSERT].
    @Override
    public MedicineEventType eventType() {
        return MedicineEventType.UPSERT;
    }
}
