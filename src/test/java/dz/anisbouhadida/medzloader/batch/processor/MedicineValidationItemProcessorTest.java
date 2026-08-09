package dz.anisbouhadida.medzloader.batch.processor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import dz.anisbouhadida.medzloader.batch.dto.MedicineLine;
import dz.anisbouhadida.medzloader.batch.dto.WithdrawalLine;
import dz.anisbouhadida.medzloader.batch.support.audit.MedicineImportIssue;
import dz.anisbouhadida.medzloader.batch.support.audit.MedicineImportIssueDisposition;
import dz.anisbouhadida.medzloader.batch.support.audit.MedicineImportIssueRecorder;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import jakarta.validation.Validator;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("MedicineValidationItemProcessor")
@ExtendWith(MockitoExtension.class)
class MedicineValidationItemProcessorTest {

  @Mock private Validator validator;
  @Mock private MedicineImportIssueRecorder issueRecorder;
  @Mock private ConstraintViolation<MedicineLine> violation;
  @Mock private Path propertyPath;

  private MedicineValidationItemProcessor processor;

  @BeforeEach
  void setUp() {
    processor = new MedicineValidationItemProcessor(validator, issueRecorder, "2026-08", 42L);
  }

  @Test
  @DisplayName("should pass a valid item without recording an issue")
  void shouldPassValidItem() throws Exception {
    var line = withdrawalLine("I");
    when(validator.validate((MedicineLine) line)).thenReturn(Set.of());

    assertThat(processor.process(line)).isSameAs(line);
    verifyNoInteractions(issueRecorder);
  }

  @Test
  @DisplayName("should load a blank origin and record a warning")
  void shouldRecordBlankOriginWarning() throws Exception {
    var line = withdrawalLine("");
    when(validator.validate((MedicineLine) line)).thenReturn(Set.of());

    assertThat(processor.process(line)).isSameAs(line);

    var captor = ArgumentCaptor.forClass(MedicineImportIssue.class);
    verify(issueRecorder).record(captor.capture());
    assertThat(captor.getValue().disposition())
        .isEqualTo(MedicineImportIssueDisposition.LOADED_WITH_WARNING);
    assertThat(captor.getValue().fieldName()).isEqualTo("status");
    assertThat(captor.getValue().sourceFile()).isEqualTo("retraits.csv");
  }

  @Test
  @DisplayName("should filter an invalid item and record its violation")
  void shouldFilterAndRecordInvalidItem() throws Exception {
    var line = withdrawalLine("I");
    when(validator.validate((MedicineLine) line)).thenReturn(Set.of(violation));
    when(violation.getPropertyPath()).thenReturn(propertyPath);
    when(propertyPath.toString()).thenReturn("type");
    when(violation.getInvalidValue()).thenReturn("FPREMIX");
    when(violation.getMessage()).thenReturn("unsupported medicine type");

    assertThat(processor.process(line)).isNull();

    var captor = ArgumentCaptor.forClass(MedicineImportIssue.class);
    verify(issueRecorder).record(captor.capture());
    assertThat(captor.getValue().disposition()).isEqualTo(MedicineImportIssueDisposition.FILTERED);
    assertThat(captor.getValue().fieldName()).isEqualTo("type");
    assertThat(captor.getValue().rejectedValue()).isEqualTo("FPREMIX");
  }

  private static WithdrawalLine withdrawalLine(String status) {
    return new WithdrawalLine(
        7,
        "REG",
        "CODE",
        "INN",
        "BRAND",
        "FORM",
        "DOSAGE",
        "PACKAGING",
        "LIST",
        "P1",
        "P2",
        "LAB",
        "DZ",
        "",
        "GE",
        status,
        "",
        "");
  }
}
