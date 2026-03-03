INSERT INTO withdrawal_event (
  medicine_id, withdrawal_date, withdrawal_reason
)
VALUES (
  (SELECT medicine_id FROM medicine
   WHERE registration_number = :registrationNumber
     AND code = :code
     AND icd = :icd
     AND brand_name = :brandName
     AND laboratory_holder = :laboratoryHolder
   ORDER BY last_updated DESC LIMIT 1),
  :withdrawalDate, :withdrawalReason
)
ON CONFLICT (medicine_id) DO UPDATE SET
  withdrawal_date = EXCLUDED.withdrawal_date,
  withdrawal_reason = EXCLUDED.withdrawal_reason,
  updated_at = CURRENT_TIMESTAMP;

