package dz.anisbouhadida.medzloader.domain.model;

import dz.anisbouhadida.medzloader.domain.model.enums.MedicineEventType;
import dz.anisbouhadida.medzloader.domain.model.enums.MedicineStatus;

import java.time.LocalDateTime;

public record NomenclatureEvent(Medicine medicine, LocalDateTime finalRegistrationDate, String stabilityDuration, String observations) implements MedicineEvent {

        @Override
        public MedicineStatus status() {
            return MedicineStatus.ACTIVE;
        }

    @Override
    public MedicineEventType eventType() {
        return MedicineEventType.UPSERT;
    }
}
