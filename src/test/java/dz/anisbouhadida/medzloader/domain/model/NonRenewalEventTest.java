package dz.anisbouhadida.medzloader.domain.model;

import dz.anisbouhadida.medzloader.domain.model.enums.MedicineEventType;
import dz.anisbouhadida.medzloader.domain.model.enums.MedicineStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayName("NonRenewalEvent")
class NonRenewalEventTest {

    private Medicine medicine;
    private NonRenewalEvent event;

    @BeforeEach
    void setUp() {
        medicine = new Medicine(
                "027/01 A 004/97", "01 A 004",
                "IBUPROFENE", "ADVIL", "SOL.INJ.", "5MG/ML",
                "B/05 AMP. DE 1ML", "LISTE II", "80.00", "100.00",
                "PFIZER", "USA",
                LocalDateTime.of(1997, 3, 20, 0, 0, 0), null, null, 0
        );
        event = new NonRenewalEvent(
                medicine,
                LocalDateTime.of(2022, 12, 31, 0, 0, 0),
                "Non renouvelé par décision"
        );
    }

    @Test
    @DisplayName("should always return MARKED_NOT_RENEWED status")
    void status_should_returnMarkedNotRenewed() {
        assertThat(event.status()).isEqualTo(MedicineStatus.MARKED_NOT_RENEWED);
    }

    @Test
    @DisplayName("should always return NON_RENEWAL event type")
    void eventType_should_returnNonRenewal() {
        assertThat(event.eventType()).isEqualTo(MedicineEventType.NON_RENEWAL);
    }

    @Test
    @DisplayName("should expose all constructor fields correctly")
    void constructor_should_setAllFields() {
        assertAll(
                () -> assertThat(event.medicine()).isSameAs(medicine),
                () -> assertThat(event.finalRegistrationDate()).isEqualTo(LocalDateTime.of(2022, 12, 31, 0, 0, 0)),
                () -> assertThat(event.observations()).isEqualTo("Non renouvelé par décision")
        );
    }

    @Test
    @DisplayName("should accept null optional fields")
    void constructor_should_acceptNulls_when_optionalFieldsAreNull() {
        var eventWithNulls = new NonRenewalEvent(medicine, null, null);

        assertAll(
                () -> assertThat(eventWithNulls.medicine()).isSameAs(medicine),
                () -> assertThat(eventWithNulls.finalRegistrationDate()).isNull(),
                () -> assertThat(eventWithNulls.observations()).isNull(),
                () -> assertThat(eventWithNulls.status()).isEqualTo(MedicineStatus.MARKED_NOT_RENEWED),
                () -> assertThat(eventWithNulls.eventType()).isEqualTo(MedicineEventType.NON_RENEWAL)
        );
    }

    @Test
    @DisplayName("should be an instance of MedicineEvent")
    void should_implementMedicineEvent() {
        assertThat(event).isInstanceOf(MedicineEvent.class);
    }
}

