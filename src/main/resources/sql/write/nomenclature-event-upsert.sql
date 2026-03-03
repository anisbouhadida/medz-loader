INSERT INTO nomenclature_event (
  medicine_id, final_registration_date, stability_duration, observations
)
VALUES (
  (SELECT medicine_id FROM medicine
   WHERE registration_number = :registrationNumber
     AND code = :code
     AND icd = :icd
     AND brand_name = :brandName
     AND laboratory_holder = :laboratoryHolder
   ORDER BY last_updated DESC LIMIT 1),
  :finalRegistrationDate, :stabilityDuration, :observations
)
ON CONFLICT (medicine_id) DO UPDATE SET
  final_registration_date = EXCLUDED.final_registration_date,
  stability_duration = EXCLUDED.stability_duration,
  observations = EXCLUDED.observations,
  updated_at = CURRENT_TIMESTAMP;

