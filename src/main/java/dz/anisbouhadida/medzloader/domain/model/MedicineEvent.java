package dz.anisbouhadida.medzloader.domain.model;

import dz.anisbouhadida.medzloader.domain.model.enums.MedicineEventType;
import dz.anisbouhadida.medzloader.domain.model.enums.MedicineStatus;

public sealed interface MedicineEvent permits NomenclatureEvent, WithdrawalEvent, NonRenewalEvent {

    Medicine medicine();

    MedicineStatus status();

    MedicineEventType eventType();


}
