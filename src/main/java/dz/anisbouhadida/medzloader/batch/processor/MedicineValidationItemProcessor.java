package dz.anisbouhadida.medzloader.batch.processor;

import dz.anisbouhadida.medzloader.batch.dto.MedicineLine;
import dz.anisbouhadida.medzloader.batch.support.audit.MedicineImportIssue;
import dz.anisbouhadida.medzloader.batch.support.audit.MedicineImportIssueDisposition;
import dz.anisbouhadida.medzloader.batch.support.audit.MedicineImportIssueRecorder;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.Comparator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.infrastructure.item.ItemProcessor;

/// Validates medicine lines while retaining details about filtered and degraded items.
///
/// Invalid items return `null`, preserving Spring Batch filter-count semantics. A missing optional
/// origin is accepted, mapped to `null` later, and recorded as a data-quality warning.
@Slf4j
@RequiredArgsConstructor
public class MedicineValidationItemProcessor implements ItemProcessor<MedicineLine, MedicineLine> {

  private static final String BLANK_ORIGIN_MESSAGE = "origin is blank and will be stored as null";

  private final Validator validator;
  private final MedicineImportIssueRecorder issueRecorder;
  private final String release;
  private final Long jobExecutionId;

  @Override
  public MedicineLine process(MedicineLine item) {
    var violations =
        validator.validate(item).stream()
            .sorted(Comparator.comparing(violation -> violation.getPropertyPath().toString()))
            .toList();

    if (!violations.isEmpty()) {
      violations.forEach(violation -> recordFilteredItem(item, violation));
      return null;
    }

    if (item.status() == null || item.status().isBlank()) {
      recordIssue(
          MedicineImportIssue.from(
              release,
              jobExecutionId,
              item,
              MedicineImportIssueDisposition.LOADED_WITH_WARNING,
              "status",
              item.status(),
              BLANK_ORIGIN_MESSAGE));
    }
    return item;
  }

  private void recordFilteredItem(MedicineLine item, ConstraintViolation<MedicineLine> violation) {
    recordIssue(
        MedicineImportIssue.from(
            release,
            jobExecutionId,
            item,
            MedicineImportIssueDisposition.FILTERED,
            violation.getPropertyPath().toString(),
            violation.getInvalidValue(),
            violation.getMessage()));
  }

  private void recordIssue(MedicineImportIssue issue) {
    issueRecorder.record(issue);
    log.warn(
        "Medicine import issue: disposition={}, release={}, sourceFile={}, sourceItemId={}, "
            + "registrationNumber={}, field={}, rejectedValue={}, message={}",
        issue.disposition(),
        issue.release(),
        issue.sourceFile(),
        issue.sourceItemId(),
        issue.registrationNumber(),
        issue.fieldName(),
        issue.rejectedValue(),
        issue.message());
  }
}
