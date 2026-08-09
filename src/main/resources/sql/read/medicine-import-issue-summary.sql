SELECT disposition, field_name, COUNT(*) AS issue_count
FROM medicine_import_issue
WHERE job_execution_id = :jobExecutionId
GROUP BY disposition, field_name
ORDER BY disposition, field_name;
