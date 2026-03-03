INSERT INTO medicine_event_history (medicine_id, event_type, event_date)
VALUES (
  (SELECT medicine_id FROM medicine
   WHERE registration_number = :registrationNumber
     AND code = :code
     AND icd = :icd
     AND brand_name = :brandName
     AND laboratory_holder = :laboratoryHolder
   ORDER BY last_updated DESC LIMIT 1),
  :eventType::medicine_event_type,
  CURRENT_TIMESTAMP
)
ON CONFLICT (medicine_id, event_type) DO UPDATE SET
  event_date = EXCLUDED.event_date,
  updated_at = CURRENT_TIMESTAMP;

