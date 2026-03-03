INSERT INTO medicine_status_history (medicine_id, status, status_timestamp)
VALUES (
  (SELECT medicine_id FROM medicine
   WHERE registration_number = :registrationNumber
     AND code = :code
     AND icd = :icd
     AND brand_name = :brandName
     AND laboratory_holder = :laboratoryHolder
     ORDER BY last_updated DESC LIMIT 1),
  :status::medicine_status,
  CURRENT_TIMESTAMP
)
ON CONFLICT (medicine_id, status) DO NOTHING;

