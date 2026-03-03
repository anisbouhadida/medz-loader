package dz.anisbouhadida.medzloader.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import dz.anisbouhadida.medzloader.domain.model.enums.MedicineEventType;
import dz.anisbouhadida.medzloader.domain.model.enums.MedicineStatus;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("WithdrawalEvent")
class WithdrawalEventTest {

  private Medicine medicine;
  private WithdrawalEvent event;

  @BeforeEach
  void setUp() {
    medicine =
        new Medicine(
            "156/01 A 001/99",
            "01 A 001",
            "AMOXICILLINE",
            "CLAMOXYL",
            "GÉLULE",
            "500MG",
            "B/20",
            "LISTE II",
            "90.00",
            "110.00",
            "GSK",
            "UK",
            LocalDateTime.of(1999, 5, 10, 0, 0, 0),
            null,
            null,
            0);
    event = new WithdrawalEvent(medicine, LocalDateTime.of(2023, 6, 1, 0, 0, 0), "Lot contaminé");
  }

  @Test
  @DisplayName("should always return WITHDRAWN status")
  void status_should_returnWithdrawn() {
    assertThat(event.status()).isEqualTo(MedicineStatus.WITHDRAWN);
  }

  @Test
  @DisplayName("should always return WITHDRAWAL event type")
  void eventType_should_returnWithdrawal() {
    assertThat(event.eventType()).isEqualTo(MedicineEventType.WITHDRAWAL);
  }

  @Test
  @DisplayName("should expose all constructor fields correctly")
  void constructor_should_setAllFields() {
    assertAll(
        () -> assertThat(event.medicine()).isSameAs(medicine),
        () -> assertThat(event.withdrawalDate()).isEqualTo(LocalDateTime.of(2023, 6, 1, 0, 0, 0)),
        () -> assertThat(event.withdrawalReason()).isEqualTo("Lot contaminé"));
  }

  @Test
  @DisplayName("should accept null optional fields")
  void constructor_should_acceptNulls_when_optionalFieldsAreNull() {
    var eventWithNulls = new WithdrawalEvent(medicine, null, null);

    assertAll(
        () -> assertThat(eventWithNulls.medicine()).isSameAs(medicine),
        () -> assertThat(eventWithNulls.withdrawalDate()).isNull(),
        () -> assertThat(eventWithNulls.withdrawalReason()).isNull(),
        () -> assertThat(eventWithNulls.status()).isEqualTo(MedicineStatus.WITHDRAWN),
        () -> assertThat(eventWithNulls.eventType()).isEqualTo(MedicineEventType.WITHDRAWAL));
  }

  @Test
  @DisplayName("should be an instance of MedicineEvent")
  void should_implementMedicineEvent() {
    assertThat(event).isInstanceOf(MedicineEvent.class);
  }
}
