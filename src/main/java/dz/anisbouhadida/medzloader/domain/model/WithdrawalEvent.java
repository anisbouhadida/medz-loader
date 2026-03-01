package dz.anisbouhadida.medzloader.domain.model;

import dz.anisbouhadida.medzloader.domain.model.enums.MedicineEventType;
import dz.anisbouhadida.medzloader.domain.model.enums.MedicineStatus;

import java.time.LocalDateTime;

/// Represents a **withdrawal** event for a [Medicine].
///
/// Created when a medicine is officially withdrawn from the market.
/// Always carries a [MedicineStatus#WITHDRAWN] status and an event type of
/// [MedicineEventType#WITHDRAWAL].
///
/// @param medicine          the medicine affected by this event
/// @param withdrawalDate    date the withdrawal took effect — may be `null`
/// @param withdrawalReason  reason stated for the withdrawal — may be `null`
///
/// @author Anis Bouhadida
/// @since 0.0.1
public record WithdrawalEvent(Medicine medicine, LocalDateTime withdrawalDate, String withdrawalReason) implements MedicineEvent {

    /// {@inheritDoc}
    ///
    /// Always returns [MedicineStatus#WITHDRAWN].
    @Override
    public MedicineStatus status() {
        return MedicineStatus.WITHDRAWN;
    }

    /// {@inheritDoc}
    ///
    /// Always returns [MedicineEventType#WITHDRAWAL].
    @Override
    public MedicineEventType eventType() {
        return MedicineEventType.WITHDRAWAL;
    }
}
