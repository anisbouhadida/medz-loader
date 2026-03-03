package dz.anisbouhadida.medzloader.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import dz.anisbouhadida.medzloader.domain.model.enums.MedicineEventType;
import dz.anisbouhadida.medzloader.domain.model.enums.MedicineStatus;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("NomenclatureEvent")
class NomenclatureEventTest {

  private Medicine medicine;
  private NomenclatureEvent event;

  @BeforeEach
  void setUp() {
    medicine =
        new Medicine(
            "035/01 A 003/17/23",
            "01 A 003",
            "PARACETAMOL",
            "DOLIPRANE",
            "COMPRIMÉ",
            "500MG",
            "B/16",
            "LISTE I",
            "120.00",
            "150.00",
            "SANOFI",
            "FRANCE",
            LocalDateTime.of(2017, 1, 15, 0, 0, 0),
            null,
            null,
            0);
    event =
        new NomenclatureEvent(
            medicine, LocalDateTime.of(2025, 1, 15, 0, 0, 0), "24 MOIS", "Observation test");
  }

  @Test
  @DisplayName("should always return ACTIVE status")
  void status_should_returnActive() {
    assertThat(event.status()).isEqualTo(MedicineStatus.ACTIVE);
  }

  @Test
  @DisplayName("should always return UPSERT event type")
  void eventType_should_returnUpsert() {
    assertThat(event.eventType()).isEqualTo(MedicineEventType.UPSERT);
  }

  @Test
  @DisplayName("should expose all constructor fields correctly")
  void constructor_should_setAllFields() {
    assertAll(
        () -> assertThat(event.medicine()).isSameAs(medicine),
        () ->
            assertThat(event.finalRegistrationDate())
                .isEqualTo(LocalDateTime.of(2025, 1, 15, 0, 0, 0)),
        () -> assertThat(event.stabilityDuration()).isEqualTo("24 MOIS"),
        () -> assertThat(event.observations()).isEqualTo("Observation test"));
  }

  @Test
  @DisplayName("should accept null optional fields")
  void constructor_should_acceptNulls_when_optionalFieldsAreNull() {
    var eventWithNulls = new NomenclatureEvent(medicine, null, null, null);

    assertAll(
        () -> assertThat(eventWithNulls.medicine()).isSameAs(medicine),
        () -> assertThat(eventWithNulls.finalRegistrationDate()).isNull(),
        () -> assertThat(eventWithNulls.stabilityDuration()).isNull(),
        () -> assertThat(eventWithNulls.observations()).isNull(),
        () -> assertThat(eventWithNulls.status()).isEqualTo(MedicineStatus.ACTIVE),
        () -> assertThat(eventWithNulls.eventType()).isEqualTo(MedicineEventType.UPSERT));
  }

  @Test
  @DisplayName("should be an instance of MedicineEvent")
  void should_implementMedicineEvent() {
    assertThat(event).isInstanceOf(MedicineEvent.class);
  }
}
