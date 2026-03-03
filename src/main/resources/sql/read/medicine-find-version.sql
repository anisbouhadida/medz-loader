SELECT version
FROM   medicine
WHERE  registration_number = :registrationNumber
  AND  (code IS NOT DISTINCT FROM :code)
  AND  (icd  IS NOT DISTINCT FROM :icd)
  AND  (brand_name IS NOT DISTINCT FROM :brandName)
  AND  (laboratory_holder IS NOT DISTINCT FROM :laboratoryHolder)
LIMIT  1

