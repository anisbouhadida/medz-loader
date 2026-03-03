INSERT INTO medicine
            (
                        registration_number,
                        code,
                        icd,
                        brand_name,
                        form,
                        dosage,
                        packaging,
                        list,
                        p1,
                        p2,
                        laboratory_holder,
                        laboratory_country,
                        initial_registration_date,
                        type,
                        origin,
                        version,
                        last_updated
            )
            VALUES
            (
                        :registrationNumber,
                        :code,
                        :internationalCommonDenomination,
                        :brandName,
                        :form,
                        :dosage,
                        :packaging,
                        :list,
                        :p1,
                        :p2,
                        :laboratoryHolder,
                        :laboratoryCountry,
                        :initialRegistrationDate,
                        :type::medicine_type,
                        :origin::medicine_origin,
                        :version,
                        CURRENT_TIMESTAMP
            )
ON CONFLICT
            (
                        registration_number,
                        code,
                        icd,
                        brand_name,
                        laboratory_holder
            )
DO UPDATE SET
       form = excluded.form,
       dosage = excluded.dosage,
       packaging = excluded.packaging,
       list = excluded.list,
       p1 = excluded.p1,
       p2 = excluded.p2,
       initial_registration_date = excluded.initial_registration_date,
       type = excluded.type::medicine_type,
       origin = excluded.origin::medicine_origin,
       version = medicine.version + 1,
       last_updated = CURRENT_TIMESTAMP
WHERE  medicine.version = excluded.version;

