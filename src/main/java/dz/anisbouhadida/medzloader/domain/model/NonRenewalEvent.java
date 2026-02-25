package dz.anisbouhadida.medzloader.domain.model;

import dz.anisbouhadida.medzloader.domain.model.enums.MedicineEventType;
import dz.anisbouhadida.medzloader.domain.model.enums.MedicineStatus;

import java.time.LocalDateTime;

public record NonRenewalEvent(Medicine medicine, LocalDateTime finalRegistrationDate, String observations) implements MedicineEvent {

    @Override
    public MedicineStatus status() {
        return MedicineStatus.MARKED_NOT_RENEWED;
    }

    @Override
    public MedicineEventType eventType() {
        return MedicineEventType.NON_RENEWAL;
    }
}
