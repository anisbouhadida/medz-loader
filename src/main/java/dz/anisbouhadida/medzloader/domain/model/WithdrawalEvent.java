package dz.anisbouhadida.medzloader.domain.model;

import dz.anisbouhadida.medzloader.domain.model.enums.MedicineEventType;
import dz.anisbouhadida.medzloader.domain.model.enums.MedicineStatus;

import java.time.LocalDateTime;

public record WithdrawalEvent(Medicine medicine, LocalDateTime withdrawalDate, String withdrawalReason) implements MedicineEvent {

    @Override
    public MedicineStatus status() {
        return MedicineStatus.WITHDRAWN;
    }

    @Override
    public MedicineEventType eventType() {
        return MedicineEventType.WITHDRAWAL;
    }
}
