INSERT INTO medicine_import_issue (
    release,
    job_execution_id,
    source_file,
    source_item_id,
    registration_number,
    code,
    brand_name,
    laboratory_holder,
    disposition,
    field_name,
    rejected_value,
    message
)
VALUES (
    :release,
    :jobExecutionId,
    :sourceFile,
    :sourceItemId,
    :registrationNumber,
    :code,
    :brandName,
    :laboratoryHolder,
    :disposition,
    :fieldName,
    :rejectedValue,
    :message
);
